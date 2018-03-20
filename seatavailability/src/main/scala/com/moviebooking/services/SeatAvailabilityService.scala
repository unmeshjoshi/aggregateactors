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
