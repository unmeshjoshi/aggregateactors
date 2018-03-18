package com.moviebooking.aggregates

import org.scalatest.FunSuite

class SeatAvailabilityTest extends FunSuite {

  test("should reserve seats if available") {
    val seatAvailability = SeatAvailability(List(Seat(SeatNumber("A", 1)),Seat(SeatNumber("A", 2))))
    val seatAvailabilityAfterReservation = seatAvailability.reserve(SeatsReserved("screen1", List(SeatNumber("A", 1))))
    assert(seatAvailabilityAfterReservation.seats == List(Seat(SeatNumber("A", 1), true),Seat(SeatNumber("A", 2))))
  }

  test("should check if seats are available") {
    val seatAvailability = SeatAvailability(List(Seat(SeatNumber("A", 1)),Seat(SeatNumber("A", 2))))
    assert(seatAvailability.areAvailable(List(SeatNumber("A", 1), SeatNumber("A", 2))))
  }

  test("should check if seats are not available") {
    val seatAvailability = SeatAvailability(List(Seat(SeatNumber("A", 1), true),Seat(SeatNumber("A", 2))))
    assert(!seatAvailability.areAvailable(List(SeatNumber("A", 1), SeatNumber("A", 2))))
  }
}
