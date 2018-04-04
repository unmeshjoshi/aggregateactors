package com.moviebooking.writeside.apps

import com.moviebooking.writeside.common.{ClusterSettings, ClusterShard}

object ShardApp extends App {
  private val settings = new ClusterSettings(2555)
  implicit val system  = settings.system

  ClusterShard.start()
}
