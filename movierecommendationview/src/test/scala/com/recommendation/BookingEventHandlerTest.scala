package com.recommendation

import com.moviebooking.writeside.aggregates.{OrderConfirmed, OrderDetails, User}
import org.scalatest.FunSuite

class BookingEventHandlerTest extends FunSuite {

  test("should create booked relationship for new booking") {
    val database = DatabaseFixture.createDatabase
      .populateWith(ExampleData.movieGraph)
      .applyMigrations(List[Migration]())
      .database

    val repository     = new RecommendationRepository(database)
    val bookingHandler = new BookingEventHandler(repository)
    bookingHandler.handleBookingEvent(
      OrderConfirmed("1000", OrderDetails("10", BigDecimal(100), "1", "Kaun Banega Karodpati", List(), User("User10")))
    )

    val finder = new RecommendationFinder(database)

    val movieNames = finder.findRecommendationFor("User10")
    assert(movieNames.contains("Kaun Banega Karodpati"))
  }

}
