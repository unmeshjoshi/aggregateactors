package com.moviebooking.services

import java.util.Optional

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.moviebooking.common.Networks
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection

import scala.concurrent.Future

object SeatAvailabilityService extends App with JsonSupport {
  implicit val system = ActorSystem("SeatAvailability")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  val client = RedisClient.create("redis://localhost")
  val redisConnection: StatefulRedisConnection[String, String] = client.connect

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case request @ HttpRequest(GET, Uri.Path("/theatres-shows"), _, _, _) =>
      Future {
        val theatreName: Optional[String] =
          request.getUri().query().get("theatreName")
        println(s"Getting shows for ${theatreName}")
        val shows = redisConnection.sync().get(s"${theatreName.get()}_show")
        HttpResponse(
          entity = HttpEntity(ContentTypes.`application/json`, shows),
          status = StatusCodes.OK)
      }
    case request @ HttpRequest(GET, Uri.Path("/theatres"), _, _, _) =>
      Future {
        val seatAvailabilityJson = redisConnection.sync().get("theatres")
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
                                         seatAvailabilityJson),
                     status = StatusCodes.OK)
      }
    case request @ HttpRequest(GET, Uri.Path("/movies"), _, _, _) => {
      Future {
        val seatAvailabilityJson = redisConnection.sync().get("movies")
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
                                         seatAvailabilityJson),
                     status = StatusCodes.OK)
      }
    }
    case request @ HttpRequest(GET, Uri.Path("/available-seats"), _, _, _) =>
      Future {
        val screenId: Optional[String] =
          request.getUri().query().get("screenId")
        val seatAvailabilityJson = redisConnection.sync().get(screenId.get())
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
                                         seatAvailabilityJson),
                     status = StatusCodes.OK)
      }
  }
  Http().bindAndHandleAsync(requestHandler, new Networks("").hostname(), 8085)
}
