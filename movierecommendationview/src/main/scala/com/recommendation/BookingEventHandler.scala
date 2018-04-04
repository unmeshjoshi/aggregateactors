package com.recommendation

import com.moviebooking.writeside.aggregates.OrderConfirmed

class BookingEventHandler(recommendationRepository: RecommendationRepository) {

  def handleBookingEvent(orderConfirmed: OrderConfirmed): Unit = {
    recommendationRepository.addBooking(orderConfirmed.orderDetails.user.fistName, orderConfirmed.orderDetails.movieName)
  }
}
