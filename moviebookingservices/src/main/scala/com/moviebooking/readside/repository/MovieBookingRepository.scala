package com.moviebooking.readside.repository

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.moviebooking.writeside.aggregates.MovieState
import com.moviebooking.writeside.services.JsonSupport
import play.api.libs.json.Json

import scala.collection.immutable
import scala.concurrent.{ExecutionContextExecutor, Future}

class MovieBookingRepository(actorSystem: ActorSystem, reactiveRedis: ReactiveRedis)(implicit materializer: ActorMaterializer)
    extends JsonSupport {
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  def getTheatreShows(theatreName: String): Future[String] = {
    reactiveRedis.get(theatreName)
  }

  def getScreensForMovie(movieName: String): Future[String] = {
    val movieMapKey      = s"${movieName}_show"
    val showMapResponseF = reactiveRedis.get(movieMapKey)
    showMapResponseF.map(showMapJson ⇒ {
      val movieShows = Json.parse(showMapJson).as[Map[String, Seq[String]]]
      val showIds    = movieShows(s"${movieName}_show")
      Json.toJson(showIds).toString()
    })
  }

  def getAllMovies(): Future[String] = {
    val movieNamesResponse = reactiveRedis.get("movies")
    val movieNamesListF    = movieNamesResponse.map(response ⇒ Json.parse(response).as[List[String]])
    val eventualEventualStates: Future[immutable.Seq[MovieState]] =
      movieNamesListF.flatMap((movieNames: immutable.Seq[String]) ⇒ {
        val eventualStates: immutable.Seq[Future[MovieState]] = movieNames.map(name ⇒ {
          val movieJsonResponseF: Future[String] = reactiveRedis.get(name)
          movieJsonResponseF.map(movieJson ⇒ Json.parse(movieJson).as[MovieState])
        })
        Future.sequence(eventualStates)
      })

    eventualEventualStates.map(movies ⇒ Json.toJson(movies).toString())
  }

  def getMovie(movieName: String): Future[String] = {
    reactiveRedis.get(movieName)
  }

  def getTheatres: Future[String] = {
    reactiveRedis.get("theatres")
  }

  def getAvailableSeatsForScreen(screenId: String): Future[String] = {
    reactiveRedis.get(screenId)
  }
}
