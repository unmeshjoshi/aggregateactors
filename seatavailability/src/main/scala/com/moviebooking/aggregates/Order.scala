package com.moviebooking.aggregates

import akka.persistence.PersistentActor
import com.moviebooking.aggregates.messages.Command
import enumeratum.{Enum, EnumEntry}


sealed trait OrderStatus extends EnumEntry

object OrderStatus extends Enum[PaymentStatus] {
  case object UnInitialized extends OrderStatus
  case object Submitted extends OrderStatus
  case object Confirmed extends OrderStatus
  case object Declined extends OrderStatus

  val values = findValues
}

case class SubmitOder(id:String) extends Command
case class OrderSubmited(id:String)
case class OrderConfirmed(id:String)

case class OrderState(id:String, orderStatus:OrderStatus)

object Order {
  val shardName = "Order"
}

class Order() extends PersistentActor {
  var orderState = OrderState("", OrderStatus.UnInitialized)

  def updateState(event:OrderSubmited): Unit = {
    orderState = OrderState(event.id, OrderStatus.Submitted)
  }

  override def receiveRecover: Receive = {
    case event:OrderSubmited ⇒ updateState(event)

  }

  override def receiveCommand: Receive = {
    case SubmitOder(id) ⇒ persist(OrderSubmited(id))(updateState)
  }

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name
}
