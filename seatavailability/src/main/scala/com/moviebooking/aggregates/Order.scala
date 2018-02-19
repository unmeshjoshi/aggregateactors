package com.moviebooking.aggregates

import akka.persistence.PersistentActor
import enumeratum.{Enum, EnumEntry}


sealed trait OrderStatus extends EnumEntry

object OrderStatus extends Enum[PaymentStatus] {
  case object UnInitialized extends OrderStatus
  case object Submitted extends OrderStatus
  case object Confirmed extends OrderStatus
  case object Declined extends OrderStatus

  val values = findValues
}

case class SubmitOder()
case class OrderSubmited()
case class OrderConfirmed()

case class OrderState(orderStatus:OrderStatus)

class Order(orderId:String) extends PersistentActor {
  var orderState = OrderState(OrderStatus.UnInitialized)

  def updateState(event:OrderSubmited): Unit = {
    orderState = OrderState(OrderStatus.Submitted)
  }

  override def receiveRecover: Receive = {
    case event:OrderSubmited ⇒ updateState(event)

  }

  override def receiveCommand: Receive = {
    case SubmitOder ⇒ persist(OrderSubmited())(updateState)
  }

  override def persistenceId: String = orderId
}
