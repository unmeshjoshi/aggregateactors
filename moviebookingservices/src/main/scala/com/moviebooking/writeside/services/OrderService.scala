package com.moviebooking.writeside.services

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.{ByteString, Timeout}
import com.moviebooking.writeside.aggregates._
import com.moviebooking.writeside.common.{ClusterSettings, ClusterShard}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.duration._

case class Order(screenId: ShowId, seatNumbers: List[SeatNumber])

object OrderService extends App with JsonSupport {
  val logger = LoggerFactory.getLogger(this.getClass)
  val settings = new ClusterSettings(8081)
  implicit val system = settings.system
  implicit val materializer = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case request@HttpRequest(POST, Uri.Path("/order"), _, _, _) =>
      implicit val timeout: Timeout = 15.seconds
      val requestF: Future[ByteString] =
        request.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)

      val eventualEventualResponse = requestF.flatMap(request ⇒ {
        val order = Json.parse(request.toArray).as[Order]
        val screenShard = ClusterShard.shardRegion(ShowActor.shardName)
        log(s"Reserving seats for ${order}")
        val responseFuture: Future[Any] = screenShard ? ReserveSeats(order.screenId, order.seatNumbers)
        val mapFuture: Future[HttpResponse] = responseFuture.map(response ⇒ {
          response match {
            case r: SeatsRejected ⇒ {
              val entity = HttpEntity(ContentTypes.`application/json`, response.toString)
              log(s"Rejecting ${response}")
              HttpResponse(entity = entity, status = StatusCodes.Conflict)
            }
            case r: SeatsAccepted ⇒ {
              val entity = HttpEntity(ContentTypes.`application/json`, response.toString)
              HttpResponse(entity = entity, status = StatusCodes.Created)
            }
          }
        })
        mapFuture
      })
      eventualEventualResponse
    case any@_ ⇒
      Future {
        log(any.toString())
        HttpResponse(status = StatusCodes.OK)
      }
  }

  def log(str: String) = logger.info(str)

  ClusterShard.start()
  //GET seat-availability json
  //POST reserve seats
  // reserve-seats command
  //handle seats reserved

  Http().bindAndHandleAsync(requestHandler, settings.hostname, 8083)
}
