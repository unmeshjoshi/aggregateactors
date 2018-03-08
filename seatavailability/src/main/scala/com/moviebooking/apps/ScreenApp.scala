package com.moviebooking.apps

import com.moviebooking.aggregates._
import com.moviebooking.common.{ClusterSettings, ClusterShard}

object ScreenMain extends App {
  //create the actor system
  implicit val system = new ClusterSettings(2553).system

  ClusterShard.start()

  val screenShard = ClusterShard.shardRegion(Screen.shardName)
  val paymentShard = ClusterShard.shardRegion(Payment.shardName)
  val orderShard = ClusterShard.shardRegion(Order.shardName)

  val screenName = "demo-screen-actor1"

  initializeScreens(screenName)

  sendTestMessages(screenName)

  private def sendTestMessages(screenName: String) = {

    screenShard ! ReserveSeats(screenName + 1, List(SeatNumber("A", 1)))

    paymentShard ! SubmitPayment("payment1", 100)

    orderShard ! SubmitOder(
      "order1",
      OrderDetails("order1",
                   BigDecimal(100),
                   "10",
                   "Justice League",
                   List(SeatNumber("A", 1)),
                   User("scott", "davis", "scott@st.com", "12882882828")))
  }

  private def initializeScreens(screenName: String) = {
    screenShard ! InitializeAvailability(
      screenName + 1,
      List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
    screenShard ! InitializeAvailability(
      screenName + 2,
      List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
    screenShard ! InitializeAvailability(
      screenName + 3,
      List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
    screenShard ! InitializeAvailability(
      screenName + 4,
      List(Seat(SeatNumber("A", 1)), Seat(SeatNumber("B", 2))))
  }
}
