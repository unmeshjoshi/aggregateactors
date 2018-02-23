package com.moviebooking.aggregates

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.event.Logging
import akka.persistence.PersistentActor
import com.moviebooking.ScreenApp.system
import com.moviebooking.SharedStoreApp
import com.moviebooking.SharedStoreApp.system
import com.moviebooking.common.ClusterSettings

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

object Command {

  val idExtractor: ShardRegion.ExtractEntityId = {
    case s: Command => (s.id, s)
  }

  val shardResolver: ShardRegion.ExtractShardId = msg => msg match {
    case s: Command => (math.abs(s.id.hashCode) % 100).toString
  }

}

sealed trait Command {
  def id: String
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
  val system = new ClusterSettings(2553).system
  Thread.sleep(5000)
  SharedStoreApp.registerSharedJournal(system)

  private val screenName = "demo-screen-actor-"

  val shard = ClusterSharding(system).start(
    typeName = "Screen",
    entityProps = Props[Screen],
    settings = ClusterShardingSettings(system),
    extractEntityId = Command.idExtractor,
    extractShardId = Command.shardResolver)
  //  val screen1 = createSingleton(system, Props.create(classOf[Screen], "screen1"), screenName)
//
//  println(screen1.path.toStringWithoutAddress)
//
//  val proxy = system.actorOf(
//    ClusterSingletonProxy.props(
//      singletonManagerPath = "/user/" + screenName,
//      settings = ClusterSingletonProxySettings(system)),
//    name = "demo-screen-actor-proxy-1")

//  val payment1 =
//    system.actorOf(Props.create(classOf[Payment], "Payment-screen1-account1-customer1"),
//      "demo-payment-actor1")
//
//  val screen2 =
//    system.actorOf(Props.create(classOf[Screen], "screen2"),
//      "demo-screen-actor-2")

  println("sending messages to actor")
  shard ! InitializeAvailability(screenName + 1, List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
  shard ! InitializeAvailability(screenName + 2, List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
  shard ! InitializeAvailability(screenName + 3, List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
  shard ! InitializeAvailability(screenName + 4, List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))

  shard !  ReserveSeats(screenName, List(SeatNumber("A", 1)))


//  payment1 ! SubmitPayment(100)
//
//  val order1 =
//    system.actorOf(Props.create(classOf[Order], "order1"),
//      "demo-order-actor-1")
//
//  order1 ! SubmitOder()

  def createSingleton(system: ActorSystem, actorProps:Props, name:String) = {
    val singletonManagerProps = ClusterSingletonManager.props(
      singletonProps = actorProps,
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(system)
    )
    system.actorOf(singletonManagerProps, name)
  }

}
