package com.moviebooking.aggregates

import akka.persistence.PersistentActor
import enumeratum._

sealed trait PaymentStatus extends EnumEntry

object PaymentStatus extends Enum[PaymentStatus] {
  case object UnInitialized extends PaymentStatus
  case object Submitted extends PaymentStatus
  case object Confirmed extends PaymentStatus
  case object Declined extends PaymentStatus

  val values = findValues
}

sealed trait PaymentEvent
case class SubmitPayment(amount:BigDecimal)
case class PaymentSubmited(amount:BigDecimal) extends PaymentEvent
case class PaymentSuccessful() extends PaymentEvent
case class PaymentDeclined() extends PaymentEvent
case class PaymentState(amount:BigDecimal, status:PaymentStatus = PaymentStatus.UnInitialized) {
  def updateStatus(status:PaymentStatus): PaymentState = {
    copy(amount, status)
  }
}


class Payment() extends PersistentActor {

  var paymentState:PaymentState = PaymentState(0, PaymentStatus.UnInitialized)

  def updateState(event:PaymentEvent): Unit = {
    event match  {
      case PaymentSubmited(amount) ⇒ paymentState = PaymentState(amount, PaymentStatus.Submitted)

    }
  }

  override def receiveRecover: Receive = {
    case event:PaymentEvent ⇒ updateState(event)
  }

  override def receiveCommand: Receive = {
    case sbt@SubmitPayment(amount) ⇒
      persist(PaymentSubmited(amount))(updateState)
  }

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name
}
