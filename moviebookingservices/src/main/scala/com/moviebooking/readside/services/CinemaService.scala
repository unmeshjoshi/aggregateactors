package com.moviebooking.readside.services

import java.util.Optional

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity.IndefiniteLength
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.moviebooking.writeside.aggregates.MovieState
import com.moviebooking.writeside.common.Networks
import com.moviebooking.writeside.services.JsonSupport
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import play.api.libs.json.Json
import reactor.core.publisher.Mono

import scala.collection.immutable
import scala.concurrent.Future

object CinemaService extends App with JsonSupport {
  implicit val system                          = ActorSystem("SeatAvailability")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  val client                                                   = RedisClient.create("redis://localhost")
  val redisConnection: StatefulRedisConnection[String, String] = client.connect

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case request @ HttpRequest(GET, Uri.Path("/theatres-shows"), _, _, _) =>
      Future {
        val theatreName: Optional[String] =
          request.getUri().query().get("theatreName")
        println(s"Getting shows for ${theatreName}")
        val shows: Mono[String]            = redisConnection.reactive().get(s"${theatreName.get()}_show")
        val value: Source[ByteString, Any] = Source.fromPublisher(shows).map(string ⇒ ByteString.fromString(string))
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, value), status = StatusCodes.OK)
      }
    case request @ HttpRequest(GET, Uri.Path("/theatres"), _, _, _) =>
      Future {
        val seatAvailabilityJson = redisConnection.sync().get("theatres")
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, seatAvailabilityJson), status = StatusCodes.OK)
      }
    case request @ HttpRequest(GET, Uri.Path("/movie"), _, _, _) =>
      Future {
        val movieName: Optional[String] =
          request.getUri().query().get("movieName")
        val movieJson = redisConnection.sync().get(movieName.get())
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, movieJson), status = StatusCodes.OK)
      }
    case request @ HttpRequest(GET, Uri.Path("/movies"), _, _, _) => {
      Future {
        val seatAvailabilityJson              = redisConnection.sync().get("movies")
        val movieNames: immutable.Seq[String] = Json.parse(seatAvailabilityJson).as[List[String]]
        val movies = movieNames.map(movieName ⇒ {
          val movieJson = redisConnection.sync().get(movieName)
          println(s"parsing ${movieJson}")
          val movie = Json.parse(movieJson).as[MovieState]
          movie
        })
        val moviesJson = Json.toJson(movies).toString()
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, moviesJson), status = StatusCodes.OK)
      }
    }
    case request @ HttpRequest(GET, Uri.Path("/screens"), _, _, _) => {
      Future {
        val movieName: Optional[String] =
          request.getUri().query().get("movieName")
        val movieMapKey = s"${movieName.get()}_show"
        println(s"checking ${movieMapKey}")
        val showMapJson = redisConnection.sync().get(movieMapKey)
        val movieShows  = Json.parse(showMapJson).as[Map[String, Seq[String]]]
        val showIds     = movieShows(s"${movieName.get()}_show")
        val value       = Json.toJson(showIds).toString()
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, value), status = StatusCodes.OK)
      }
    }
    case request @ HttpRequest(GET, Uri.Path("/available-seats"), _, _, _) =>
      Future {
        val screenId: Optional[String] =
          request.getUri().query().get("screenId")
        val seatAvailabilityJson = redisConnection.sync().get(screenId.get())
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, seatAvailabilityJson), status = StatusCodes.OK)
      }
  }
  Http().bindAndHandleAsync(requestHandler, new Networks("").hostname(), 8085)
}
