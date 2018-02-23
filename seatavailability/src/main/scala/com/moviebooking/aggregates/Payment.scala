package com.moviebooking.aggregates

import akka.persistence.PersistentActor
import com.moviebooking.aggregates.messages.Command
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
case class SubmitPayment(id:String, amount:BigDecimal) extends Command
case class PaymentSubmited(id:String, amount:BigDecimal) extends PaymentEvent
case class PaymentSuccessful(id:String) extends PaymentEvent
case class PaymentDeclined(id:String) extends PaymentEvent
case class PaymentState(id:String, amount:BigDecimal, status:PaymentStatus = PaymentStatus.UnInitialized) {
  def updateStatus(status:PaymentStatus): PaymentState = {
    copy(id, amount, status)
  }
}

object Payment {
  val shardName = "Payment"
}
class Payment() extends PersistentActor {

  var paymentState:PaymentState = PaymentState("", 0, PaymentStatus.UnInitialized)

  def updateState(event:PaymentEvent): Unit = {
    event match  {
      case PaymentSubmited(id, amount) ⇒ paymentState = PaymentState(id, amount, PaymentStatus.Submitted)

    }
  }

  override def receiveRecover: Receive = {
    case event:PaymentEvent ⇒ updateState(event)
  }

  override def receiveCommand: Receive = {
    case sbt@SubmitPayment(id, amount) ⇒
      persist(PaymentSubmited(id, amount))(updateState)
  }

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name
}
