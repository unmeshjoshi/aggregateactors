package com.moviebooking.writeside.aggregates

import akka.persistence.PersistentActor
import enumeratum.{Enum, EnumEntry}

sealed trait OrderStatus extends EnumEntry

object OrderStatus extends Enum[PaymentStatus] {
  case object UnInitialized extends OrderStatus
  case object Submitted     extends OrderStatus
  case object Confirmed     extends OrderStatus
  case object Declined      extends OrderStatus

  val values = findValues
}

case class User(fistName: String = "", lastName: String = "", email: String = "", mobileNumber: String = "") {}

case class OrderDetails(id: String = "",
                        totalPrice: BigDecimal = BigDecimal(0),
                        screenId: String = "",
                        movieName: String = "",
                        seatNumbers: List[SeatNumber] = List(),
                        user: User = User()) {}

case class SubmitOder(id: String, orderDetails: OrderDetails) extends Command
case class ConfirmOrder(id: String)                           extends Command

sealed trait OrderEvent {
  val id: String
}
case class OrderSubmited(id: String, orderDetails: OrderDetails) extends OrderEvent

case class OrderConfirmed(id: String, orderDetails: OrderDetails) extends OrderEvent

case class OrderState(id: String, orderStatus: OrderStatus, orderDetails: OrderDetails = OrderDetails()) {
  def confirm() = {
    copy(orderStatus = OrderStatus.Confirmed)
  }
}

object OrderActor {
  val shardName = "Order"
}

class OrderActor extends PersistentActor {
  var orderState = OrderState("", OrderStatus.UnInitialized)

  def updateState(event: OrderEvent): Unit = {
    event match {
      case o: OrderSubmited =>
        orderState = OrderState(o.id, OrderStatus.Submitted, o.orderDetails)
      case o: OrderConfirmed => orderState = orderState.confirm()
    }
  }

  override def receiveRecover: Receive = {
    case event: OrderSubmited  ⇒ updateState(event)
    case event: OrderConfirmed ⇒ updateState(event)

  }

  override def receiveCommand: Receive = {
    case SubmitOder(id, orderDetails) ⇒
      persist(OrderSubmited(id, orderDetails))(updateState)
    case ConfirmOrder(id) ⇒ {
      persist(OrderConfirmed(id, orderState.orderDetails))(updateState)
    }
  }

  override def persistenceId: String =
    self.path.parent.name + "-" + self.path.name
}
