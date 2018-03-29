package controllers

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.moviebooking.aggregates.{MovieState, Show, ShowId}
import com.moviebooking.services.JsonSupport
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MovieController @Inject()(cc: ControllerComponents)(implicit assetsFinder: AssetsFinder)
    extends AbstractController(cc)
    with JsonSupport {
  implicit val system                          = ActorSystem("moviebookingactorsystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def index() =
    Action.async({
      val eventualResponse = getResponse("http://192.168.0.111:8085/movies")
      println(eventualResponse)
      val eventualString = eventualResponse.flatMap(response ⇒ {
        readResponse(response)
      })
      eventualString.map((response: ByteString) ⇒ {
        val movies = Json.parse(response.toArray).as[List[MovieState]]
        Ok(views.html.main(movies))
      })
    })

  def getResponse(url: String): Future[HttpResponse] = {
    implicit val system       = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
    responseFuture
  }

  def shows(movieName: String): Action[AnyContent] =
    Action.async({
      val eventualResponse =
        getResponse(s"http://192.168.0.111:8085/screens?movieName=${java.net.URLEncoder.encode(movieName, "UTF-8")}")
      val futureMovieResponse =
        getResponse(s"http://192.168.0.111:8085/movie?movieName=${java.net.URLEncoder.encode(movieName, "UTF-8")}")
      val eventualString = eventualResponse.flatMap(response ⇒ {
        readResponse(response)
      })
      val eventualResult = eventualString.map((response: ByteString) ⇒ {
        Json.parse(response.toArray).as[Seq[String]]
      })
      eventualResult.flatMap((showId: Seq[String]) ⇒ {
        futureMovieResponse.flatMap(movieResponse ⇒ {
          val eventualString = readResponse(movieResponse)
          eventualString.map((response: ByteString) ⇒ {
            val movie   = Json.parse(response.toArray).as[MovieState]
            val showIds = showId.map(idString ⇒ ShowId.fromKey(idString))

//            theatre -> (screens -> List[ShowId])
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
            println(theatreMap)
            Ok(views.html.shows(showIds, movie))
          })
        })
      })
    })

  case class TheatreShows(theatreName: String, screenShows: List[ScreenShows])
  case class ScreenShows(screenName: String, shows: List[ShowId])

  private def readResponse(response: HttpResponse) = {
    val requestF: Future[ByteString] =
      response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    requestF
  }

  def bookSeats(showId: String): Action[AnyContent] = Action {
    Ok(views.html.bookseats())
  }

}
