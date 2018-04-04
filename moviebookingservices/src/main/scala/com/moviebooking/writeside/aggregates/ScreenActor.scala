package com.moviebooking.writeside.aggregates

import java.time.LocalTime

import akka.event.Logging
import akka.persistence.PersistentActor

case class SeatNumber(row: String, value: Int)

case class Seat(seatNumber: SeatNumber, isReserved: Boolean = false) {
  def reserve(): Seat = {
    copy(seatNumber, true)
  }
}

object ShowId {
  def fromKey(key: String): ShowId = {
    val strings = key.split('_')
    ShowId(strings(0), strings(1), strings(2))
  }
}
case class ShowId(screenName: String, showTimeSlot: String, theatreName: String) {
  override def toString() = showKey()
  def showKey()           = s"${screenName}_${showTimeSlot}_${theatreName}"
}

case class Show(showId: ShowId, showTime: LocalTime, movieName: String, seats: List[Seat]) {
  def areAvailable(seatNumbers: List[SeatNumber]) = {
    val seatsToBeReserved =
      seats.filter(seat ⇒ seatNumbers.contains(seat.seatNumber))
    val availableSeats = seatsToBeReserved.filter(seat ⇒ !seat.isReserved)
    availableSeats.size == seatNumbers.size
  }

  def reserve(seatsToReserve: SeatsReserved): Show = {
    val seatsToBeReserved =
      seats.filter(seat ⇒ seatsToReserve.seats.contains(seat.seatNumber))
    val remainingSeats =
      seats.filter(seat ⇒ !seatsToReserve.seats.contains(seat.seatNumber))

    val seatsWithReservations = seatsToBeReserved.map(
      seat ⇒ seat.reserve()
    )
    copy(showId, showTime, movieName, seatsWithReservations ++ remainingSeats)
  }
}

case class InitializeShow(showId: ShowId, showTime: LocalTime, movieName: String, seats: List[Seat]) extends Command {
  def id = showId.toString

}

case class ReserveSeats(showId: ShowId, seats: List[SeatNumber]) extends Command {
  def id = showId.toString

}

case class SeatsReserved(showId: ShowId, seats: List[SeatNumber]) extends Event {
  def id = showId.toString

}

case class ShowInitialized(showId: ShowId, showTime: LocalTime, movieName: String, seats: List[Seat]) extends Event {
  def id = showId.toString

}

object ShowActor {
  val shardName: String = "Screen"
}

class ShowActor extends PersistentActor {
  val log = Logging(context.system, this)
  val receiveRecover: Receive = {
    case evt @ ShowInitialized(id, showTime, movie, availableSeats) ⇒
      initializeState(evt)
    case evt @ SeatsReserved(id, count) => updateState(evt)
  }
  var seatAvailability: Option[Show] = None

  override def receiveCommand: Receive = {
    case InitializeShow(id, showTime, movie, availableSeats) ⇒ {
      log.info("Initializing seat availability")
      persist(ShowInitialized(id, showTime, movie, availableSeats)) { event ⇒
        initializeState(event)
        sender() ! "Seats Initialized"
      }
    }
    case ReserveSeats(id, seatNumbers) ⇒ {
      log.info(s"Received reserve seats event for ${seatNumbers}")
      println(s"Current state is ${seatAvailability}")
      if (seatAvailability.get.areAvailable(seatNumbers)) {
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

  def initializeState(event: ShowInitialized): Unit = {
    seatAvailability = Some(Show(event.showId, event.showTime, event.movieName, event.seats))
  }

  def updateState(event: SeatsReserved): Unit = {
    log.info(s"handling event ${event}")
    seatAvailability = Some(seatAvailability.get.reserve(event))
  }

  override def persistenceId: String = {
    val id = self.path.parent.name + "-" + self.path.name
    println(s"ID IS ${id}")
    id
  }
}
