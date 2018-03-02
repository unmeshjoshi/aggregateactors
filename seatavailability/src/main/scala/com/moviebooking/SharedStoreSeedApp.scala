package com.moviebooking

import akka.actor.{ActorIdentity, ActorPath, ActorSystem, Identify, Props}
import akka.pattern.ask
import akka.persistence.journal.leveldb.{
  SharedLeveldbJournal,
  SharedLeveldbStore
}
import akka.util.Timeout
import com.moviebooking.common.{ClusterSettings, ClusterShard}

import scala.concurrent.duration._

object SharedStoreSeedApp extends App {
  private val settings = new ClusterSettings(ClusterSettings.seedPort)
  implicit val system = settings.system

  val numberOfShards = 100

  ClusterShard.start()

}
