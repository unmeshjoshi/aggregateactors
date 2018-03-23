package com.moviebooking.apps

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.moviebooking.aggregates.{Event, Initialized, SeatsReserved, Show}
import com.moviebooking.services.JsonSupport
import io.lettuce.core.RedisClient
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
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
  val partition = 0
  val fromOffset = 0L
  val subscription = Subscriptions.assignmentWithOffset(
    new TopicPartition("seatAvailability", partition) → fromOffset)
  val done: Future[Done] =
    Consumer
      .plainSource(consumerSettings, subscription)
      .mapAsync(1)(readJson)
      .runWith(Sink.ignore)
  private val consumer: KafkaConsumer[String, String] =
    consumerSettings.createKafkaConsumer()

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
            .set(
              init.showId.showKey(),
              Json
                .toJson(
                  Show(init.showId, init.showTime, init.movieName, init.seats))
                .toString())
        case reserved: SeatsReserved ⇒
          println(s"Reserving seats ${reserved}")
          val seatJson = redisConnection.sync().get(reserved.id.toString)
          val seatAvailability = Json.parse(seatJson).as[Show]
          val availability = seatAvailability.reserve(reserved)
          redisConnection
            .sync()
            .set(reserved.id.toString, Json.toJson(availability).toString())
      }
    } catch {
      case any @ _ ⇒ {
        any.printStackTrace()
      }
    }
    Done
  }
}
