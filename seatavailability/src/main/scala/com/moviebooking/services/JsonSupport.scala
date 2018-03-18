package com.moviebooking.services

import com.moviebooking.aggregates.{Seat, SeatAvailability, SeatNumber}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._

trait JsonSupport extends PlayJsonSupport {
  implicit val seatNumberFormat: OFormat[SeatNumber] = Json.format[SeatNumber]
  implicit val seatFormat: OFormat[Seat] = Json.format[Seat]
  implicit val seatAvailabilityFormat: OFormat[SeatAvailability] =
    Json.format[SeatAvailability]
  implicit val orderFormat: OFormat[Order] = Json.format[Order]

}
