package com.moviebooking.services

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Source
import akka.util.{ByteString, Timeout}
import com.moviebooking.aggregates.{ReserveSeats, Screen, SeatNumber}
import com.moviebooking.common.{ClusterSettings, ClusterShard}
import com.moviebooking.services.ScreenAdminService.{requestHandler, settings}
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.duration._

case class Order(screenId: String, seatNumbers: List[SeatNumber])
//
object Main extends App with JsonSupport {
  println(
    Json.toJson(Order("Screen1", List(SeatNumber("A", 1), SeatNumber("A", 2)))))
}

object OrderService extends App with JsonSupport {

  //GET seat-availability json
  //POST reserve seats
  // reserve-seats command
  //handle seats reserved
  private val settings = new ClusterSettings(8081)
  implicit val system = settings.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  ClusterShard.start()

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case request @ HttpRequest(POST, Uri.Path("/order"), _, _, _) =>
      implicit val timeout: Timeout = 15.seconds
      val requestBytes: Source[ByteString, AnyRef] =
        request.entity.getDataBytes()
      val requestF: Future[ByteString] =
        request.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)

      val eventualEventualResponse = requestF.flatMap(request ⇒ {
        val order = Json.parse(request.toArray).as[Order]
        val screenShard = ClusterShard.shardRegion(Screen.shardName)
        val response: Future[Any] = screenShard ? ReserveSeats(
          order.screenId,
          order.seatNumbers)
        val mapFuture: Future[HttpResponse] = response.map(any ⇒ {
          val entity = HttpEntity(ContentTypes.`application/json`,
                                  any.asInstanceOf[String])
          HttpResponse(entity = entity, status = StatusCodes.Created)
        })
        mapFuture
      })
      eventualEventualResponse
  }

  Http().bindAndHandleAsync(requestHandler, settings.hostname, 8083)

  //POST confirm booking

  //send command initialize payment
  //send command to initialize order
  //
}
