package com.moviebooking.services

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.moviebooking.aggregates.{ReserveSeats, Screen, SeatNumber}
import com.moviebooking.common.{ClusterSettings, ClusterShard}

import scala.concurrent.Future
import scala.concurrent.duration._

object SeatAvailabilityService extends App {
  //GET seat-availability json
  //POST reserve seats
  // reserve-seats command
  //handle seats reserved
  private val settings = new ClusterSettings(8080)
  implicit val system = settings.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  ClusterShard.start()

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/available-seats"), _, _, _) =>
      Future {
        HttpResponse(
          entity =
            HttpEntity(ContentTypes.`application/json`, "available-seats\r\n"))
      }

    case HttpRequest(POST, Uri.Path("/reserve-seats"), _, _, _) =>
      implicit val timeout: Timeout = 5.seconds
      val screenShard = ClusterShard.shardRegion(Screen.shardName)

      val response: Future[Any] = screenShard ? ReserveSeats(
        "demo-screen-actor",
        List(SeatNumber("A", 1)))
      val mapFuture: Future[HttpResponse] = response.map(
        any ⇒
          HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
                                           "seats-reserved"),
                       status = StatusCodes.Created))
      mapFuture
  }

  Http().bindAndHandleAsync(requestHandler, settings.hostname, 8082)

}
