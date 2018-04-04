package com.moviebooking.apps

import com.moviebooking.common.{ClusterSettings, ClusterShard}

object ShardApp extends App {
  private val settings = new ClusterSettings(2555)
  implicit val system  = settings.system

  ClusterShard.start()
}
