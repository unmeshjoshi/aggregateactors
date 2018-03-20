package com.moviebooking.apps

import com.moviebooking.aggregates._
import com.moviebooking.common.{ClusterSettings, ClusterShard}
import com.moviebooking.generator.Generators

import scala.util.Random

object ScreenApp extends App {
  //create the actor system
  implicit val system = new ClusterSettings(2553).system

  ClusterShard.start()

  val screenShard = ClusterShard.shardRegion(Screen.shardName)
  val paymentShard = ClusterShard.shardRegion(Payment.shardName)
  val orderShard = ClusterShard.shardRegion(Order.shardName)

  initializeScreens()

//  sendTestMessages("Screen1")
//
//  private def sendTestMessages(screenName: String) = {
//
//    screenShard ! ReserveSeats(screenName + 1, List(SeatNumber("A", 1)))
//
//    paymentShard ! SubmitPayment("payment1", 100)
//
//    orderShard ! SubmitOder(
//      "order1",
//      OrderDetails("order1",
//                   BigDecimal(100),
//                   "10",
//                   "Justice League",
//                   List(SeatNumber("A", 1)),
//                   User("scott", "davis", "scott@st.com", "12882882828")))
//  }

  private def initializeScreens() = {
    val screenIds = Generators.generateScreenIds
    screenIds.foreach(
      screenId ⇒
        screenShard ! InitializeAvailability(
          screenId,
          Generators.movies(new Random().nextInt(Generators.movies.size)),
          Generators.generateSeatMap))
  }
}
