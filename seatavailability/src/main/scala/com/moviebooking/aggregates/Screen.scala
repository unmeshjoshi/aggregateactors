package com.moviebooking.aggregates

import akka.actor.{ActorSystem, Props}
import akka.persistence.PersistentActor

case class SeatNumber(row:String, value:Int)
case class Seat(seatNumber:SeatNumber, isReserved:Boolean = false) {
  def reserve(): Seat = {
    copy(seatNumber, true)
  }
}
case class SeatAvailability(seats:List[Seat]) {
  def reserve(seatsToReserve:SeatsReserved): SeatAvailability = {
    val seatsWithReservations = seats.map(
      seat ⇒ seat.reserve() //TODO reserving all the seats as of now.
    )
    copy(seatsWithReservations)
  }
}

case class InitializeAvailability(seats:List[Seat])
case class ReserveSeats(seats:List[SeatNumber])
case class SeatsReserved(seats:List[SeatNumber])
case class Initialized(seats:List[Seat])

class Screen(screenId: String) extends PersistentActor {
  var seatAvailability:SeatAvailability = SeatAvailability(List())

  def initializeState(event:Initialized):Unit = {
    seatAvailability = SeatAvailability(event.seats)
  }

  def updateState(event: SeatsReserved): Unit = {
    println(s"handling event ${event}")
    seatAvailability = seatAvailability.reserve(event)
  }

  val receiveRecover: Receive = {
    case evt@SeatsReserved(count) => updateState(evt)
  }

  override def receiveCommand: Receive = {
    case InitializeAvailability(availableSeats) ⇒ {
      persist(Initialized(availableSeats))(initializeState)
    }
    case ReserveSeats(count) ⇒ {
      println(s"Received reserve seats event for ${count}")
      persist(SeatsReserved(count))(updateState)
    }

  }
  override def persistenceId: String = screenId
}

object ScreenMain extends App {
  //create the actor system
  val system = ActorSystem("PersitenceSystem")

  val screen1 =
    system.actorOf(Props.create(classOf[Screen], "screen1"),
      "demo-screen-actor-1")

  val payment1 =
    system.actorOf(Props.create(classOf[Payment], "Payment-screen1-account1-customer1"),
      "demo-payment-actor1")

  val screen2 =
    system.actorOf(Props.create(classOf[Screen], "screen2"),
      "demo-screen-actor-2")

  screen1 ! InitializeAvailability(List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))

  screen1 ! ReserveSeats(List(SeatNumber("A", 1)))

  payment1 ! SubmitPayment(100)

  val order1 =
    system.actorOf(Props.create(classOf[Order], "order1"),
      "demo-order-actor-1")

  order1 ! SubmitOder()

}
