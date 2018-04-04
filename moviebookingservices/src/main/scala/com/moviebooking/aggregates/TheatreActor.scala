package com.moviebooking.aggregates

import akka.event.Logging
import akka.persistence.PersistentActor

case class Address(street: String, city: String) {}

case class TheatreState(name: String, address: Address)

case class InitialiseTheatre(id: String, address: Address) extends Command

case class TheatreInitialized(id: String, address: Address) extends Event

object Theatre {
  val shardName = "Theatre"
}

class TheatreActor extends PersistentActor {
  val log = Logging(context.system, this)
  val receiveRecover: Receive = {
    case evt @ TheatreInitialized(id, address) ⇒ updateState(evt)
  }
  var theatreState: Option[TheatreState] = None

  override def receiveCommand: Receive = {
    case InitialiseTheatre(id, address) ⇒ {
      log.info("Initializing Theatre")
      persist(TheatreInitialized(id, address)) { event ⇒
        updateState(event)
        sender() ! "Theatre Initialized"
      }
    }
  }

  def updateState(event: TheatreInitialized): Unit = {
    log.info(s"handling event ${event}")
    theatreState = Some(TheatreState(event.id, event.address))
  }

  override def persistenceId: String = {
    val id = self.path.parent.name + "-" + self.path.name
    id
  }
}
