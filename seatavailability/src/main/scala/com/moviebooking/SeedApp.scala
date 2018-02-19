package com.moviebooking

import com.moviebooking.common.ClusterSettings

object SeedApp extends App {
  new ClusterSettings(2552).system

}
