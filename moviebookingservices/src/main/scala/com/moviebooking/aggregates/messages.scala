package com.moviebooking.aggregates

import akka.cluster.sharding.ShardRegion

object Command {

  val idExtractor: ShardRegion.ExtractEntityId = {
    case s: Command => (s.id.toString, s)
  }

  val numberOfShards = 3
  val shardResolver: ShardRegion.ExtractShardId = msg =>
    msg match {
      case s: Command => (math.abs(s.id.hashCode) % numberOfShards).toString
  }

}

trait Event {
  def id: String
}

trait Command {
  def id: String
}
