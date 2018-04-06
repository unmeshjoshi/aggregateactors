package com.moviebooking.readside.services

import java.util.Optional

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
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
import scala.concurrent.{ExecutionContextExecutor, Future}

object CinemaService extends App with JsonSupport {
  implicit val actorSystem                     = ActorSystem("SeatAvailability")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  val client                                                   = RedisClient.create("redis://localhost")
  val redisConnection: StatefulRedisConnection[String, String] = client.connect

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case request @ HttpRequest(GET, Uri.Path("/theatres-shows"), _, _, _) =>
      Future {
        val theatreName: Optional[String] = request.getUri().query().get("theatreName")
        println(s"Getting shows for ${theatreName}")
        val value: Source[ByteString, Any] = getValue(theatreName)
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, value), status = StatusCodes.OK)
      }
    case request @ HttpRequest(GET, Uri.Path("/theatres"), _, _, _) =>
      Future {
        val seatAvailabilityJson = getValue(Optional.of("theatres"))
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, seatAvailabilityJson), status = StatusCodes.OK)
      }
    case request @ HttpRequest(GET, Uri.Path("/movie"), _, _, _) =>
      Future {
        val movieName: Optional[String] =
          request.getUri().query().get("movieName")
        val movieJson = getValue(movieName)
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, movieJson), status = StatusCodes.OK)
      }

    case request @ HttpRequest(GET, Uri.Path("/movies"), _, _, _) => {
      val seatAvailabilityJson: Mono[String]        = redisConnection.reactive().get("movies")
      val movieNamesSource: Source[String, NotUsed] = Source.fromPublisher(seatAvailabilityJson)

      val movieNamesResponse: Future[String] = movieNamesSource.runFold("")(_ ++ _)
      val movieNamesListF                    = movieNamesResponse.map(response ⇒ Json.parse(response).as[List[String]])
      val eventualEventualStates: Future[immutable.Seq[MovieState]] =
        movieNamesListF.flatMap((movieNames: immutable.Seq[String]) ⇒ {
          val eventualStates: immutable.Seq[Future[MovieState]] = movieNames.map(name ⇒ {
            val movieJsonPublisher: Mono[String] = redisConnection.reactive().get(name)
            println(s"parsing ${movieJsonPublisher}")
            val movieJsonSource: Source[String, NotUsed] = Source.fromPublisher(movieJsonPublisher)
            val movieJsonResponseF: Future[String]       = movieJsonSource.runFold("")(_ ++ _)
            movieJsonResponseF.map(movieJson ⇒ Json.parse(movieJson).as[MovieState])
          })
          Future.sequence(eventualStates)
        })

      eventualEventualStates.map(movies ⇒ {
        val moviesJson = Json.toJson(movies).toString()
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, moviesJson), status = StatusCodes.OK)
      })
    }
    case request @ HttpRequest(GET, Uri.Path("/screens"), _, _, _) => {
      val movieName: Optional[String] =
        request.getUri().query().get("movieName")
      val movieMapKey                            = s"${movieName.get()}_show"
      val showMapJson                            = redisConnection.reactive().get(movieMapKey)
      val showMapSource: Source[String, NotUsed] = Source.fromPublisher(showMapJson)
      val showMapResponseF: Future[String]       = showMapSource.runFold("")(_ ++ _)
      showMapResponseF.map(showMapJson ⇒ {
        val movieShows = Json.parse(showMapJson).as[Map[String, Seq[String]]]
        val showIds    = movieShows(s"${movieName.get()}_show")
        val value      = Json.toJson(showIds).toString()
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, value), status = StatusCodes.OK)
      })
    }
    case request @ HttpRequest(GET, Uri.Path("/available-seats"), _, _, _) =>
      Future {
        val screenId: Optional[String] =
          request.getUri().query().get("screenId")
        val seatAvailability: Source[ByteString, Any] = getValue(screenId)
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, seatAvailability), status = StatusCodes.OK)
      }
  }

  private def getValue(key: Optional[String]) = {
    val valuePublisher                 = redisConnection.reactive().get(s"${key.get()}")
    val value: Source[ByteString, Any] = Source.fromPublisher(valuePublisher).map(string ⇒ ByteString.fromString(string))
    value
  }

  Http().bindAndHandleAsync(requestHandler, new Networks("").hostname(), 8085)
}
