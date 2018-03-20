package com.moviebooking.apps

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.moviebooking.aggregates.{
  Event,
  Initialized,
  SeatAvailability,
  SeatsReserved
}
import com.moviebooking.services.JsonSupport
import io.lettuce.core.RedisClient
import org.apache.kafka.clients.consumer.{
  ConsumerConfig,
  ConsumerRecord,
  KafkaConsumer
}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  StringDeserializer
}
import play.api.libs.json.Json

import scala.concurrent.Future

object KafkaSubscriber extends App with JsonSupport {
  val client = RedisClient.create("redis://localhost")
  val redisConnection = client.connect

  implicit val system = ActorSystem("KafkaSubscriber")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import scala.concurrent.ExecutionContext.Implicits.global

  val consumerSettings =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:29092")
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  private val consumer: KafkaConsumer[String, String] =
    consumerSettings.createKafkaConsumer()
  val partition = 0
  val fromOffset = 0L
  val subscription = Subscriptions.assignmentWithOffset(
    new TopicPartition("seatAvailability", partition) → fromOffset)

  val done: Future[Done] =
    Consumer
      .plainSource(consumerSettings, subscription)
      .mapAsync(1)(readJson)
      .runWith(Sink.ignore)

  def readJson(record: ConsumerRecord[String, String]): Future[Done] = Future {
    println(s"Reading record ${record}")
    record.key()
    try {
      val event = Json.parse(record.value()).as[Event]
      println(s"Parsed json ${event}")

      event match {
        case init: Initialized ⇒
          println(s"Initializing screen ${init}")
          redisConnection
            .sync()
            .set(init.id,
              Json.toJson(SeatAvailability(init.movie, init.seats)).toString())
        case reserved: SeatsReserved ⇒
          println(s"Reserving seats ${reserved}")
          val seatJson = redisConnection.sync().get(reserved.id)
          val seatAvailability = Json.parse(seatJson).as[SeatAvailability]
          val availability = seatAvailability.reserve(reserved)
          redisConnection
            .sync()
            .set(reserved.id, Json.toJson(availability).toString())
      }
    } catch {
      case any@_ ⇒ {
        println(any)
        any.printStackTrace()
      }
    }
    Done
  }
}
