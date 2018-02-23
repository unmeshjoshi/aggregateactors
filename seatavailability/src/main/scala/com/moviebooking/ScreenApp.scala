package com.moviebooking

import com.moviebooking.common.{ClusterSettings, ClusterShard}

object ScreenApp extends App {
  private val settings = new ClusterSettings(2555)
  implicit val system = settings.system

  ClusterShard.start()
}
