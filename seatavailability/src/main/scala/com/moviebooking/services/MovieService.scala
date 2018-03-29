package com.moviebooking.services

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.moviebooking.common.{ClusterSettings, ClusterShard}

import scala.concurrent.Future

object MovieService extends App {

  private val settings                         = new ClusterSettings(8080)
  implicit val system                          = settings.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import scala.concurrent.ExecutionContext.Implicits.global

  ClusterShard.start()

  var json = """{

  }"""

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/movies"), _, _, _) =>
      Future {
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, "available-seats\r\n"))
      }
  }

  Http().bindAndHandleAsync(requestHandler, settings.hostname, 8081)

}
