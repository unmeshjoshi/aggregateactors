package com.moviebooking.generator

import com.moviebooking.aggregates.{Movie, Seat, SeatNumber}

object Generators {
  def movies =
    List(Movie("Justice League", List("Ben")),
         Movie("Spider Main", List("Bruce")))

  def generateScreenIds = (1 to 5).map(n ⇒ s"Screen${n}").toList
  def generateSeatMap = {
    val rowNumbers = 'A' to 'M'
    val seatNumbers = 1 to 20
    rowNumbers
      .flatMap(row ⇒ seatNumbers.map(no ⇒ SeatNumber(s"${row}", no)))
      .map(seatNo ⇒ Seat(seatNo))
      .toList
  }
}
