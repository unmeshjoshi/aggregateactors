package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.moviebooking.writeside.aggregates.{MovieState, SeatNumber, Show, ShowId}
import com.moviebooking.writeside.common.Networks
import com.moviebooking.writeside.services.{JsonSupport, Order}
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MovieController @Inject()(cc: ControllerComponents)(implicit assetsFinder: AssetsFinder)
    extends AbstractController(cc)
    with JsonSupport {
  import org.slf4j.LoggerFactory
  val logger = LoggerFactory.getLogger(this.getClass)

  implicit val system                          = ActorSystem("moviebookingactorsystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val hostIp = new Networks().hostname()

  def index(): Action[AnyContent] =
    Action.async({
      val responseF = getResponse("http://" + hostIp + ":8085/movies")
      val jsonF = responseF.flatMap(response ⇒ {
        readResponse(response)
      })
      jsonF.map((response: ByteString) ⇒ {
        val movies = Json.parse(response.toArray).as[List[MovieState]]
        Ok(views.html.main(movies))
      })
    })

  def shows(movieName: String): Action[AnyContent] =
    Action.async({
      val screensF =
        getResponse(s"http://$hostIp:8085/screens?movieName=${java.net.URLEncoder.encode(movieName, "UTF-8")}")
      val movieDetailsF =
        getResponse(s"http://$hostIp:8085/movie?movieName=${java.net.URLEncoder.encode(movieName, "UTF-8")}")
      val screensJson = screensF.flatMap(response ⇒ {
        readResponse(response)
      })
      val showIdsF = screensJson.map((response: ByteString) ⇒ {
        Json.parse(response.toArray).as[Seq[String]]
      })
      showIdsF.flatMap((showIdStrs: Seq[String]) ⇒ {
        movieDetailsF.flatMap(movieResponse ⇒ {
          val movieDetailsJson = readResponse(movieResponse)
          movieDetailsJson.map((response: ByteString) ⇒ {
            log(s"parsing ${response.utf8String}")
            val movie      = Json.parse(response.toArray).as[MovieState]
            val showIds    = showIdStrs.map(idString ⇒ ShowId.fromKey(idString))
            var theatreMap = Map[String, Map[String, List[String]]]()
            for (showId ← showIds) {
              if (!theatreMap.exists(_._1 == showId.theatreName)) {
                val showMap = Map[String, List[String]]()
                theatreMap = theatreMap + (showId.theatreName → showMap)
              }
              var showMap = theatreMap(showId.theatreName)
              if (!showMap.exists(_._1 == showId.screenName)) {
                val showList = List[String]()
                showMap = showMap + (showId.screenName        → showList)
                theatreMap = theatreMap + (showId.theatreName → showMap)
              }

              var shows = showMap(showId.screenName)
              shows = shows :+ showId.showTimeSlot
              showMap = showMap + (showId.screenName        → shows)
              theatreMap = theatreMap + (showId.theatreName → showMap)
            }
            log(theatreMap.toString())
            Ok(views.html.shows(theatreMap, movie))
          })
        })
      })
    })

  def getResponse(url: String): Future[HttpResponse] = {
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
    responseFuture
  }

  private def readResponse(response: HttpResponse) = {
    val requestF: Future[ByteString] =
      response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    requestF
  }

  def bookSeats(showId: String): Action[AnyContent] =
    Action.async({
      val httpResponseF =
        getResponse(s"http://${hostIp}:8085/available-seats?screenId=${java.net.URLEncoder.encode(showId, "UTF-8")}")
      val jsonF = httpResponseF.flatMap(response ⇒ {
        readResponse(response)
      })
      jsonF.map((json: ByteString) ⇒ {
        val show = Json.parse(json.toArray).as[Show]
        val html = views.html.bookseats(show)
        Ok(html)
      })
    })

  case class TheatreShows(theatreName: String, screenShows: List[ScreenShows])

  case class ScreenShows(screenName: String, shows: List[ShowId])

  import play.api.data.Forms._
  import play.api.data._
  import play.api.data.format.Formats._

  def confirmBooking(): Action[AnyContent] =
    Action.async({ implicit request ⇒
      val orderForm = Form(tuple("showId" → of[String], "tickets" → of[String])).bindFromRequest()
      log(orderForm.data.toString())
      val selectedSeatsCsv: Option[String] = orderForm("tickets").value
      val selectedSeats                    = selectedSeatsCsv.get.split(",")
      val seatNumbers: Array[SeatNumber] = selectedSeats.map(seat ⇒ {
        val strings = seat.split("_")
        SeatNumber(strings(0), strings(1).toInt)
      })

      val orderRequest: JsValue =
        Json.toJson(Order(ShowId.fromKey(orderForm("showId").value.get), seatNumbers.toList))

      log(orderRequest.toString())

      val url = s"http://${hostIp}:8083/order"

      // needed for the future flatMap/onComplete in the end
      implicit val executionContext = system.dispatcher

      val orderResponse: Future[HttpResponse] = Http(system).singleRequest(
        HttpRequest(
          HttpMethods.POST,
          url,
          entity = HttpEntity(ContentTypes.`application/json`, orderRequest.toString())
        )
      )

      orderResponse.map(r ⇒ {
        if (r.status.isSuccess())
          Ok(views.html.confirm())
        else
          Conflict(views.html.conflict())
      })

    })

  def log(str: String) = logger.info(str)

}
