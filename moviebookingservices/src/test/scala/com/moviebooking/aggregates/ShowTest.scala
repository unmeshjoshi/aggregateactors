package com.moviebooking.aggregates

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import org.scalatest.FunSuite

class ShowActorTest extends FunSuite {

  def showTime =
  LocalTime.parse("11:30", DateTimeFormatter
    .ofPattern("HH:mm"))

  test("should reserve seats if available") {
    val showId = ShowId("Screen1", "11:20", "CityPride")
    val seatAvailability = Show(showId, showTime,
      "testMovieName", List(Seat(SeatNumber("A", 1)),Seat(SeatNumber("A", 2))))
    val seatAvailabilityAfterReservation = seatAvailability.reserve(SeatsReserved(showId, List(SeatNumber("A", 1))))
    assert(seatAvailabilityAfterReservation.seats == List(Seat(SeatNumber("A", 1), true),Seat(SeatNumber("A", 2))))
  }

  test("should check if seats are available") {
    val showId = ShowId("Screen1", "11:20", "CityPride")
    val seatAvailability = Show(showId, showTime,
      "testMovieName", List(Seat(SeatNumber("A", 1)),Seat(SeatNumber("A", 2))))
    assert(seatAvailability.areAvailable(List(SeatNumber("A", 1), SeatNumber("A", 2))))
  }

  test("should check if seats are not available") {
    val showId = ShowId("Screen1", "11:20", "CityPride")
    val seatAvailability = Show(showId, showTime,
      "testMovieName", List(Seat(SeatNumber("A", 1), true),Seat(SeatNumber("A", 2))))
    assert(!seatAvailability.areAvailable(List(SeatNumber("A", 1), SeatNumber("A", 2))))
  }
}
