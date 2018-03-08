package com.moviebooking.aggregates

import akka.event.Logging
import akka.persistence.PersistentActor
import com.moviebooking.aggregates.messages.Command

case class SeatNumber(row: String, value: Int)

case class Seat(seatNumber: SeatNumber, isReserved: Boolean = false) {
  def reserve(): Seat = {
    copy(seatNumber, true)
  }
}

case class SeatAvailability(seats: List[Seat]) {
  def reserve(seatsToReserve: SeatsReserved): SeatAvailability = {
    val seatsWithReservations = seats.map(
      seat ⇒ seat.reserve() //TODO reserving all the seats as of now.
    )
    copy(seatsWithReservations)
  }
}

case class InitializeAvailability(id: String, seats: List[Seat]) extends Command

case class ReserveSeats(id: String, seats: List[SeatNumber]) extends Command

case class SeatsReserved(seats: List[SeatNumber])

case class Initialized(seats: List[Seat])

object Screen {
  val shardName: String = "Screen"
}

class Screen extends PersistentActor {
  val log = Logging(context.system, this)
  var seatAvailability: SeatAvailability = SeatAvailability(List())

  def initializeState(event: Initialized): Unit = {
    seatAvailability = SeatAvailability(event.seats)
  }

  def updateState(event: SeatsReserved): Unit = {
    log.info(s"handling event ${event}")
    seatAvailability = seatAvailability.reserve(event)
  }

  val receiveRecover: Receive = {
    case evt @ Initialized(availableSeats) ⇒ initializeState(evt)
    case evt @ SeatsReserved(count) => updateState(evt)
  }

  override def receiveCommand: Receive = {
    case InitializeAvailability(id, availableSeats) ⇒ {
      log.info("Initializing seat availability")
      persist(Initialized(availableSeats)) { event ⇒
        initializeState(event)
      }
    }
    case ReserveSeats(id, count) ⇒ {
      log.info(s"Received reserve seats event for ${count}")
      persist(SeatsReserved(count)) { event ⇒
        updateState(event)
        context.system.eventStream.publish(event)
      }
    }

  }

  override def persistenceId: String = {
    val id = self.path.parent.name + "-" + self.path.name
    println(s"ID IS ${id}")
    id
  }
}
