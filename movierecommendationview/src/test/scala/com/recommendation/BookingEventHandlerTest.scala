package com.recommendation

import com.moviebooking.aggregates.OrderConfirmed
import org.scalatest.FunSuite

class BookingEventHandlerTest extends FunSuite {

  test("should create booked relationship for new booking") {
    val database = DatabaseFixture
      .createDatabase
      .populateWith(ExampleData.movieGraph).applyMigrations(List[Migration]()).database

    val repository = new RecommendationRepository(database)
    val bookingHandler = new BookingEventHandler(repository)
    bookingHandler.handleBookingEvent(OrderConfirmed("1000", "User10", "Kaun Banega Karodpati"))

    val finder = new RecommendationFinder(database)

    val movieNames = finder.findRecommendationFor("User10")
    assert(movieNames.contains("Kaun Banega Karodpati"))
  }

}
