package com.moviebooking.apps

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.moviebooking.aggregates._
import com.moviebooking.common.{ClusterSettings, ClusterShard}
import com.moviebooking.generator.Generators

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global


object ScreenApp extends App {
  private val settings = new ClusterSettings(2553)
  implicit val system = settings.system

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  ClusterShard.start()

  val screenShard = ClusterShard.shardRegion(Screen.shardName)
  //create the actor system

  val paymentShard = ClusterShard.shardRegion(Payment.shardName)
  val orderShard = ClusterShard.shardRegion(Order.shardName)
  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case request@HttpRequest(POST, Uri.Path("/init-screens"), _, _, _) =>
      initializeScreens()
        .map(
          list ⇒
            HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,
              list.toString()),
              status = StatusCodes.Created))
  }

  initializeScreens()

  Http().bindAndHandleAsync(requestHandler, settings.hostname, 8082)

  def initializeScreens()(implicit system: ActorSystem) = {
    implicit val timeout: Timeout = 5.seconds
    val screenShard = ClusterShard.shardRegion(Screen.shardName)
    val showIds = Generators.generateShowIds
    val futures = showIds.map(
      showId ⇒
        screenShard ? InitializeAvailability(
          showId,
          LocalTime.parse(showId.showTimeSlot,
            DateTimeFormatter
              .ofPattern("HH:mm")),
          Generators.movies(new Random().nextInt(Generators.movies.size)).name,
          Generators.generateSeatMap
        ))
    Future.sequence(futures)
  }
}
