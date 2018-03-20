package com.moviebooking.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.moviebooking.aggregates.{InitializeAvailability, Screen}
import com.moviebooking.common.{ClusterSettings, ClusterShard}
import com.moviebooking.generator.Generators

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

object ScreenAdminService extends App with JsonSupport {
  private val settings = new ClusterSettings(8080)
  implicit val system = settings.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  ClusterShard.start()

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case request @ HttpRequest(POST, Uri.Path("/init-screens"), _, _, _) =>
      new ScreenAdmin()
        .initializeScreens()
        .map(
          list ⇒
            HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
                                             list.toString()),
                         status = StatusCodes.Created))
  }

  Http().bindAndHandleAsync(requestHandler, settings.hostname, 8082)
}

class ScreenAdmin {
  def initializeScreens()(implicit system: ActorSystem) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val timeout: Timeout = 5.seconds
    val screenShard = ClusterShard.shardRegion(Screen.shardName)
    val screenIds = Generators.generateScreenIds
    val futures = screenIds.map(
      screenId ⇒
        screenShard ? InitializeAvailability(
          screenId,
          Generators.movies(new Random().nextInt(Generators.movies.size)),
          Generators.generateSeatMap))
    Future
      .sequence(futures)
  }
}
