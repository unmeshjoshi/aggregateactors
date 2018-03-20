package com.moviebooking.services

import com.moviebooking.aggregates._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._

trait JsonSupport extends PlayJsonSupport {
  implicit val movieFormat: OFormat[Movie] = Json.format[Movie]
  implicit val seatNumberFormat: OFormat[SeatNumber] = Json.format[SeatNumber]
  implicit val seatFormat: OFormat[Seat] = Json.format[Seat]
  implicit val seatAvailabilityFormat: OFormat[SeatAvailability] =
    Json.format[SeatAvailability]
  implicit val orderFormat: OFormat[Order] = Json.format[Order]
  implicit val initializedFormat: OFormat[Initialized] =
    Json.format[Initialized]
  implicit val seatsReservedFormat: OFormat[SeatsReserved] =
    Json.format[SeatsReserved]

  implicit val eventReads =
    __.read[Initialized].map(x => x: Event) orElse __
      .read[SeatsReserved]
      .map(x => x: Event)

  implicit val eventWrites: Writes[Event] = new Writes[Event] {
    def writes(ins: Event): JsValue = ins match {
      case l: Initialized   => Json.toJson(l)(Json.writes[Initialized])
      case s: SeatsReserved => Json.toJson(s)(Json.writes[SeatsReserved])
    }
  }
}
