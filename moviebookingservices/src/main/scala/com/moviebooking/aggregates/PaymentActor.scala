package com.moviebooking.aggregates

import akka.persistence.PersistentActor
import enumeratum._

sealed trait PaymentStatus extends EnumEntry

object PaymentStatus extends Enum[PaymentStatus] {

  val values = findValues

  case object UnInitialized extends PaymentStatus

  case object Submitted extends PaymentStatus

  case object Confirmed extends PaymentStatus

  case object Declined extends PaymentStatus
}

sealed trait PaymentEvent

case class SubmitPayment(id: String, amount: BigDecimal) extends Command

case class PaymentSubmited(id: String, amount: BigDecimal) extends PaymentEvent

case class PaymentSuccessful(id: String, amount: BigDecimal) extends PaymentEvent

case class PaymentDeclined(id: String) extends PaymentEvent

case class PaymentState(id: String, amount: BigDecimal, status: PaymentStatus = PaymentStatus.UnInitialized) {
  def updateStatus(status: PaymentStatus): PaymentState = {
    copy(id, amount, status)
  }
}

object PaymentActor {
  val shardName = "Payment"
}

class PaymentActor extends PersistentActor {

  var paymentState: PaymentState =
    PaymentState("", 0, PaymentStatus.UnInitialized)

  override def receiveRecover: Receive = {
    case event: PaymentEvent ⇒ updateState(event)
  }

  def updateState(event: PaymentEvent): Unit = {
    event match {
      case PaymentSubmited(id, amount) ⇒
        paymentState = PaymentState(id, amount, PaymentStatus.Submitted)

    }
  }

  override def receiveCommand: Receive = {
    case sbt @ SubmitPayment(id, amount) ⇒
      persist(PaymentSuccessful(id, amount))(updateState)
  }

  override def persistenceId: String =
    self.path.parent.name + "-" + self.path.name
}
