package com.moviebooking.apps

import akka.actor.Props
import com.moviebooking.common.{ClusterSettings, ClusterShard}
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}

object SeedApp extends App {
  private val settings = new ClusterSettings(ClusterSettings.seedPort)
  implicit val system  = settings.system
//  val store            = system.actorOf(Props[SharedLeveldbStore], "store")
//  SharedLeveldbJournal.setStore(store, system)
  ClusterShard.start()
}
