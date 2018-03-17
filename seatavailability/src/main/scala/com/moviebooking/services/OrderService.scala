package com.moviebooking.services

import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Source
import akka.util.{ByteString, Timeout}
import com.moviebooking.aggregates.{ReserveSeats, Screen, SeatNumber}
import com.moviebooking.common.{ClusterSettings, ClusterShard}
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.pattern.ask

class OrderService extends JsonSupport {
  case class Order(screenId: String, seatNumbers: List[SeatNumber])
  implicit val orderFormat: OFormat[Order] = Json.format[Order]

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
//      Json.fromJson()
      val screenShard = ClusterShard.shardRegion(Screen.shardName)
      val screenId = request.getUri().query().get("screenId")
      val response: Future[Any] = screenShard ? ReserveSeats(
        screenId.get(),
        List(SeatNumber("A", 1)))
      val mapFuture: Future[HttpResponse] = response.map(
        any ⇒
          HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
                                           any.asInstanceOf[String]),
                       status = StatusCodes.Created))
      mapFuture
  }

  //POST confirm booking

  //send command initialize payment
  //send command to initialize order
  //
}
