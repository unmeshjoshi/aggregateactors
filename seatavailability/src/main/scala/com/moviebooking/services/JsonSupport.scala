package com.moviebooking.services

import com.moviebooking.aggregates.{Event, _}
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json._

trait JsonSupport extends PlayJsonSupport {
  implicit val addressFormat: OFormat[Address] = Json.format[Address]
  implicit val theatreFormat: OFormat[TheatreInitialized] = Json.format[TheatreInitialized]
  implicit val movieFormat: OFormat[MovieInitiazed] = Json.format[MovieInitiazed]
  implicit val showIdFormat: OFormat[ShowId] = Json.format[ShowId]
  implicit val seatNumberFormat: OFormat[SeatNumber] = Json.format[SeatNumber]
  implicit val seatFormat: OFormat[Seat] = Json.format[Seat]
  implicit val seatAvailabilityFormat: OFormat[Show] =
    Json.format[Show]
  implicit val orderFormat: OFormat[Order] = Json.format[Order]

  implicit val initializedFormat: OFormat[Initialized] =
    Json.format[Initialized]
  implicit val seatsReservedFormat: OFormat[SeatsReserved] =
    Json.format[SeatsReserved]

  implicit val eventReads =
    __.read[Initialized].map(x => x: Event) orElse __
      .read[SeatsReserved]
      .map(x => x: Event) orElse __
      .read[TheatreInitialized]
      .map(x => x: Event) orElse __
      .read[MovieInitiazed]
      .map(x => x: Event)

  implicit val eventWrites: Writes[Event] = new Writes[Event] {
    def writes(ins: Event): JsValue = ins match {
      case l: Initialized   => Json.toJson(l)(Json.writes[Initialized])
      case s: SeatsReserved => Json.toJson(s)(Json.writes[SeatsReserved])
      case s: TheatreInitialized => Json.toJson(s)(Json.writes[TheatreInitialized])
      case s: MovieInitiazed => Json.toJson(s)(Json.writes[MovieInitiazed])
    }
  }
}
