package com.recommendation

import com.moviebooking.aggregates.OrderConfirmed

class BookingEventHandler(recommendationRepository: RecommendationRepository) {

  def handleBookingEvent(orderConfirmed: OrderConfirmed): Unit = {
    recommendationRepository.addBooking(orderConfirmed.userName, orderConfirmed.movieName)
  }
}
