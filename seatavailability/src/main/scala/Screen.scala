import akka.actor.{ActorSystem, Props}
import akka.persistence.PersistentActor

case class Seat(row:String, number:Int, isReserved:Boolean = false) {
  def reserve(): Seat = {
    copy(row, number, true)
  }
}
case class SeatAvailability(seats:List[Seat]) {
  def reserve(count:SeatsReserved): SeatAvailability = {
    val seatsWithReservations = seats.map(seat ⇒ seat.reserve())
    copy(seatsWithReservations)
  }
}

case class InitializeAvailability(seats:List[Seat])
case class ReserveSeats(count:Int)
case class SeatsReserved(count:Int)
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
      "demo-persistent-actor-1")

  val screen2 =
    system.actorOf(Props.create(classOf[Screen], "screen2"),
      "demo-persistent-actor-2")

  screen1 ! InitializeAvailability(List(Seat("A", 1), Seat("B", 2)))
  screen1 ! ReserveSeats(1)

}
