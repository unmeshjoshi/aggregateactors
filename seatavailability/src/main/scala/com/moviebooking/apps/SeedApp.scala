package com.moviebooking.apps

import com.moviebooking.common.{ClusterSettings, ClusterShard}

object SeedApp extends App {
  private val settings = new ClusterSettings(ClusterSettings.seedPort)
  implicit val system = settings.system
  ClusterShard.start()
}
