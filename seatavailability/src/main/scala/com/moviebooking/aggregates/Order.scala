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

case class User(fistName: String,
                lastName: String,
                email: String,
                mobileNumber: String)
case class OrderDetails(id: String,
                        totalPrice: BigDecimal,
                        seatNumbers: List[SeatNumber],
                        user: User)
case class SubmitOder(id: String, orderDetails: OrderDetails) extends Command
case class OrderSubmited(id: String)
case class OrderConfirmed(id: String)

case class OrderState(id: String, orderStatus: OrderStatus)

object Order {
  val shardName = "Order"
}

class Order() extends PersistentActor {
  var orderState = OrderState("", OrderStatus.UnInitialized)

  def updateState(event: OrderSubmited): Unit = {
    orderState = OrderState(event.id, OrderStatus.Submitted)
  }

  override def receiveRecover: Receive = {
    case event: OrderSubmited ⇒ updateState(event)

  }

  override def receiveCommand: Receive = {
    case SubmitOder(id, orderDetails) ⇒ persist(OrderSubmited(id))(updateState)
  }

  override def persistenceId: String =
    self.path.parent.name + "-" + self.path.name
}
