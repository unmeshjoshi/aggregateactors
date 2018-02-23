package com.moviebooking.aggregates

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.cluster.sharding.ClusterSharding
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.event.Logging
import akka.persistence.PersistentActor
import com.moviebooking.aggregates.messages.Command
import com.moviebooking.common.{ClusterSettings, ClusterShard}

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

case class InitializeAvailability(id:String, seats:List[Seat]) extends Command
case class ReserveSeats(id:String, seats:List[SeatNumber]) extends Command
case class SeatsReserved(seats:List[SeatNumber])
case class Initialized(seats:List[Seat])

object Screen {
  val shardName: String = "Screen"
}

class Screen() extends PersistentActor {
  val log = Logging(context.system, this)
  var seatAvailability:SeatAvailability = SeatAvailability(List())

  def initializeState(event:Initialized):Unit = {
    seatAvailability = SeatAvailability(event.seats)
  }

  def updateState(event: SeatsReserved): Unit = {
    log.info(s"handling event ${event}")
    seatAvailability = seatAvailability.reserve(event)
  }

  val receiveRecover: Receive = {
    case evt@Initialized(availableSeats) ⇒ initializeState(evt)
    case evt@SeatsReserved(count) => updateState(evt)
  }

  override def receiveCommand: Receive = {
    case InitializeAvailability(id, availableSeats) ⇒ {
      log.info("Initializing seat availability")
      persist(Initialized(availableSeats)){event ⇒
        initializeState(event)
        context.system.eventStream.publish(event)
      }
    }
    case ReserveSeats(id, count) ⇒ {
      log.info(s"Received reserve seats event for ${count}")
      persist(SeatsReserved(count)){ event ⇒
        updateState(event)
        context.system.eventStream.publish(event)
      }
    }

  }
  override def persistenceId: String = self.path.parent.name + "-" + self.path.name
}

object ScreenMain extends App {
  //create the actor system
  implicit val system = new ClusterSettings(2553).system
  Thread.sleep(5000)

  ClusterShard.start()
  val screenShard = ClusterShard.shardRegion(Screen.shardName)
  val paymentShard = ClusterShard.shardRegion(Payment.shardName)
  val orderShard = ClusterShard.shardRegion(Order.shardName)

  private val screenName = "demo-screen-actor-"

  //  val payment1 =
//    system.actorOf(Props.create(classOf[Payment], "Payment-screen1-account1-customer1"),
//      "demo-payment-actor1")
//

  println("sending messages to actor")
  screenShard ! InitializeAvailability(screenName + 1, List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
  screenShard ! InitializeAvailability(screenName + 2, List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
  screenShard ! InitializeAvailability(screenName + 3, List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
  screenShard ! InitializeAvailability(screenName + 4, List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))

  screenShard !  ReserveSeats(screenName + 1, List(SeatNumber("A", 1)))

  paymentShard ! SubmitPayment("payment1", 100)

  orderShard ! SubmitOder("order1")

  def createSingleton(system: ActorSystem, actorProps:Props, name:String) = {
    val singletonManagerProps = ClusterSingletonManager.props(
      singletonProps = actorProps,
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(system)
    )
    system.actorOf(singletonManagerProps, name)
  }

}
