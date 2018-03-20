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
  def areAvailable(seatNumbers: List[SeatNumber]) = {
    val seatsToBeReserved =
      seats.filter(seat ⇒ seatNumbers.contains(seat.seatNumber))
    val availableSeats = seatsToBeReserved.filter(seat ⇒ !seat.isReserved)
    availableSeats.size == seatNumbers.size
  }

  def reserve(seatsToReserve: SeatsReserved): SeatAvailability = {
    val seatsToBeReserved =
      seats.filter(seat ⇒ seatsToReserve.seats.contains(seat.seatNumber))
    val remainingSeats =
      seats.filter(seat ⇒ !seatsToReserve.seats.contains(seat.seatNumber))

    val seatsWithReservations = seatsToBeReserved.map(
      seat ⇒ seat.reserve()
    )
    copy(seatsWithReservations ++ remainingSeats)
  }
}

sealed trait Event {
  def id: String
}

case class InitializeAvailability(id: String, seats: List[Seat]) extends Command

case class ReserveSeats(id: String, seats: List[SeatNumber]) extends Command

case class SeatsReserved(id: String, seats: List[SeatNumber]) extends Event

case class Initialized(id: String, seats: List[Seat]) extends Event

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
    case evt @ Initialized(id, availableSeats) ⇒ initializeState(evt)
    case evt @ SeatsReserved(id, count) => updateState(evt)
  }

  override def receiveCommand: Receive = {
    case InitializeAvailability(id, availableSeats) ⇒ {
      log.info("Initializing seat availability")
      persist(Initialized(id, availableSeats)) { event ⇒
        initializeState(event)
        sender() ! "Seats Initialized"
      }
    }
    case ReserveSeats(id, seatNumbers) ⇒ {
      log.info(s"Received reserve seats event for ${seatNumbers}")
      println(s"Current state is ${seatAvailability}")
      if (seatAvailability.areAvailable(seatNumbers)) {
        persist(SeatsReserved(id, seatNumbers)) { event ⇒
          println("Updating reservations")
          updateState(event)
          sender() ! "Seats Accepted"
        }
      } else {
        sender() ! "Seats Rejected"
      }
    }
  }

  override def persistenceId: String = {
    val id = self.path.parent.name + "-" + self.path.name
    println(s"ID IS ${id}")
    id
  }
}
