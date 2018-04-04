package com.moviebooking.writeside.aggregates

import akka.event.Logging
import akka.persistence.PersistentActor

case class MovieState(name: String = "",
                      cast: List[String] = List(),
                      synopsis: String,
                      genre: String,
                      metadata: Map[String, String] = Map())

case class InitializeMovie(id: String, cast: List[String], synopsis: String, genre: String, metadata: Map[String, String])
    extends Command
case class MovieInitiazed(id: String, cast: List[String], synopsis: String, genre: String, metadata: Map[String, String])
    extends Event

object MovieActor {
  val shardName = "Movie"
}

class MovieActor extends PersistentActor {

  val log = Logging(context.system, this)
  val receiveRecover: Receive = {
    case evt: MovieInitiazed ⇒ updateState(evt)
  }
  var movieState: Option[MovieState] = None

  override def receiveCommand: Receive = {
    case init: InitializeMovie ⇒ {
      log.info("Initializing Movie")
      persist(MovieInitiazed(init.id, init.cast, init.synopsis, init.genre, init.metadata)) { event ⇒
        updateState(event)
        sender() ! "Movie Initialized"
      }
    }
  }

  def updateState(event: MovieInitiazed): Unit = {
    log.info(s"handling event ${event}")
    movieState = Some(MovieState(event.id, event.cast, event.synopsis, event.genre, event.metadata))
  }

  override def persistenceId: String = {
    val id = self.path.parent.name + "-" + self.path.name
    id
  }
}
