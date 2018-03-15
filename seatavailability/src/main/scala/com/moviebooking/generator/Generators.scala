package com.moviebooking.generator

import com.moviebooking.aggregates.{Seat, SeatNumber}

object Generators {
  def generateScreenIds = (1 to 100).map(n ⇒ s"Screen${n}").toList
  def generateSeatMap = {
    val rowNumbers = 'A' to 'M'
    val seatNumbers = 1 to 22
    rowNumbers.flatMap(row ⇒ seatNumbers.map(no ⇒ SeatNumber(s"${row}", no))).map(seatNo ⇒ Seat(seatNo)).toList
  }
}
