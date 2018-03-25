package com.moviebooking.common

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.moviebooking.aggregates._

object ClusterShard {

  def shardRegion(name: String)(implicit system: ActorSystem) = {
    ClusterSharding.get(system).shardRegion(name)
  }

  def start()(implicit system: ActorSystem) = {
    val screenShard: ActorRef = ClusterSharding(system).start(
      typeName = ShowActor.shardName,
      entityProps = Props[ShowActor],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )

    val paymentShard = ClusterSharding(system).start(
      typeName = Payment.shardName,
      entityProps = Props[Payment],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )

    val orderShard = ClusterSharding(system).start(
      typeName = Order.shardName,
      entityProps = Props[Order],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )

    val theatreShard = ClusterSharding(system).start(
      typeName = Theatre.shardName,
      entityProps = Props[TheatreActor],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )

    val movieShard = ClusterSharding(system).start(
      typeName = MovieActor.shardName,
      entityProps = Props[MovieActor],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )
  }
}
