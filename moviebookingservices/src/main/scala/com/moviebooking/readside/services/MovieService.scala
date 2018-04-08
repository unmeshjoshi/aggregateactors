package com.moviebooking.readside.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.moviebooking.readside.repository.{MovieBookingRepository, ReactiveRedis}
import com.moviebooking.writeside.common.Networks
import com.moviebooking.writeside.services.JsonSupport

import scala.concurrent.{ExecutionContextExecutor, Future}

object HttpRequestExtension {

  implicit class RichHttpRequest(val request: HttpRequest) {
    def getQueryParam(name: String): String = {
      val value = request.getUri().query().get(name)
      if (!value.isPresent) throw new IllegalArgumentException
      value.get()
    }
  }

}

object MovieService extends App with JsonSupport {
  implicit val actorSystem                     = ActorSystem("MovieService")
  implicit val ec: ExecutionContextExecutor    = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import HttpRequestExtension.RichHttpRequest

  val movieRepository = new MovieBookingRepository(actorSystem, new ReactiveRedis())

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case request @ HttpRequest(GET, Uri.Path("/theatres-shows"), _, _, _) =>
      httpOk(movieRepository.getTheatreShows(request.getQueryParam("theatreName")))

    case request @ HttpRequest(GET, Uri.Path("/theatres"), _, _, _) =>
      httpOk(movieRepository.getTheatres)

    case request @ HttpRequest(GET, Uri.Path("/movie"), _, _, _) =>
      httpOk(movieRepository.getMovie(request.getQueryParam("movieName")))

    case request @ HttpRequest(GET, Uri.Path("/movies"), _, _, _) =>
      httpOk(movieRepository.getAllMovies())

    case request @ HttpRequest(GET, Uri.Path("/screens"), _, _, _) =>
      httpOk(movieRepository.getScreensForMovie(request.getQueryParam("movieName")))

    case request @ HttpRequest(GET, Uri.Path("/available-seats"), _, _, _) =>
      httpOk(movieRepository.getAvailableSeatsForScreen(request.getQueryParam("screenId")))
  }

  private def httpOk(responseF: Future[String]) = {
    responseF.map(response ⇒ {
      HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, ByteString.fromString(response)), status = StatusCodes.OK)
    })
  }

  Http().bindAndHandleAsync(requestHandler, new Networks("").hostname(), 8085)
}
