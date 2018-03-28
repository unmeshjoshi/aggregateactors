package com.moviebooking.apps

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.moviebooking.aggregates._
import com.moviebooking.services.JsonSupport
import io.lettuce.core.RedisClient
import org.apache.kafka.clients.consumer.{
  ConsumerConfig,
  ConsumerRecord,
  KafkaConsumer
}
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
        case m: MovieInitiazed ⇒ {
          setMovie(m)
        }
        case t: TheatreInitialized ⇒ {
          setTheatre(t)
        }
        case init: ShowInitialized ⇒
          setShow(init)
        case reserved: SeatsReserved ⇒
          markReservedSeats(reserved)
      }
    } catch {
      case any @ _ ⇒ {
        any.printStackTrace()
      }
    }
    Done
  }

  private def setMovie(m: MovieInitiazed) = {
    val redisCommand = redisConnection
      .sync()
    redisCommand.set(m.id, Json.toJson(m)(movieFormat).toString())
    var movieList = List[String]()
    val movieListJson = redisCommand.get("movies")
    if (Option(movieListJson) == None) {
      movieList = List()
    } else {
      movieList = Json.parse(movieListJson).as[List[String]]
    }
    val newList = movieList :+ m.id
    println(s"++++++++++++++++++++++++++New list is ${newList}")
    redisCommand.set("movies", Json.toJson(newList.distinct).toString())

  }

  private def setTheatre(t: TheatreInitialized) = {
    val redisCommand = redisConnection
      .sync()
    redisCommand.set(t.id, Json.toJson(t)(theatreFormat).toString())

    var theatreList = List[String]()
    val theatreListJson = redisCommand.get("theatres")
    if (Option(theatreListJson) == None) {
      theatreList = List()
    } else {
      theatreList = Json.parse(theatreListJson).as[List[String]]
    }
    val newList = theatreList :+ t.id
    println(
      s"++++++++++++++++++++++++++++++++++++++++++++New list is ${newList}")
    redisCommand.set("theatres", Json.toJson(newList.distinct).toString())
  }

  private def markReservedSeats(reserved: SeatsReserved) = {
    println(s"Reserving seats ${reserved}")
    val seatJson = redisConnection.sync().get(reserved.id.toString)
    val seatAvailability = Json.parse(seatJson).as[Show]
    val availability = seatAvailability.reserve(reserved)
    redisConnection
      .sync()
      .set(reserved.id.toString, Json.toJson(availability).toString())
  }

  private def setShow(init: ShowInitialized) = {
    println(s"Initializing screen ${init}")
    setMovie2ShowMap(init)
    setTheatre2ShowMap(init)
    setShowDetails(init)
  }

  private def setTheatre2ShowMap(init: ShowInitialized) = {
    val redisCommand = redisConnection
      .sync()
    val theatreMapKey = s"${init.showId.theatreName}_show"
    val showMap: String = redisCommand.get(theatreMapKey)
    if (Option(showMap) == None) {
      val map = Map(theatreMapKey → List(init.showId.showKey()))
      redisCommand.set(theatreMapKey, Json.toJson(map).toString())
    } else {
      val map = Json.parse(showMap).as[Map[String, Seq[String]]]
      val showIds: Seq[String] = map(theatreMapKey)
      val shows: Seq[String] = showIds :+ init.showId.toString()
      val newMap = Map(theatreMapKey → shows)
      println(
        s"+++++++++++++++++ Setting theatreshows ${theatreMapKey} => ${newMap}")
      redisCommand.set(theatreMapKey, Json.toJson(newMap).toString())
    }
  }

  private def setMovie2ShowMap(init: ShowInitialized) = {
    val redisCommand = redisConnection
      .sync()
    val movieMapKey = s"${init.movieName}_show"
    val showMap: String = redisCommand.get(movieMapKey)
    if (Option(showMap) == None) {
      val map = Map(movieMapKey → List(init.showId.showKey()))
      redisCommand.set(movieMapKey, Json.toJson(map).toString())
    } else {
      val map = Json.parse(showMap).as[Map[String, Seq[String]]]
      val showIds: Seq[String] = map(movieMapKey)
      val serializables: Seq[String] = showIds :+ init.showId.toString()
      val newMap = Map(movieMapKey → serializables)
      redisCommand.set(movieMapKey, Json.toJson(newMap).toString())
    }
  }

  private def setShowDetails(init: ShowInitialized) = {
    redisConnection
      .sync()
      .set(
        init.showId.showKey(),
        Json
          .toJson(Show(init.showId, init.showTime, init.movieName, init.seats))
          .toString())
  }
}
