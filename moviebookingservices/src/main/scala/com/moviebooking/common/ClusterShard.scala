package com.moviebooking.common

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.moviebooking.aggregates._

object ClusterShard {

  def shardRegion(name: String)(implicit system: ActorSystem): ActorRef = {
    ClusterSharding.get(system).shardRegion(name)
  }

  def start()(implicit system: ActorSystem): Unit = {
    val screenShard: ActorRef = ClusterSharding(system).start(
      typeName = ShowActor.shardName,
      entityProps = Props[ShowActor],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )

    val paymentShard: ActorRef = ClusterSharding(system).start(
      typeName = PaymentActor.shardName,
      entityProps = Props[PaymentActor],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )

    val orderShard: ActorRef = ClusterSharding(system).start(
      typeName = OrderActor.shardName,
      entityProps = Props[OrderActor],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )

    val theatreShard: ActorRef = ClusterSharding(system).start(
      typeName = Theatre.shardName,
      entityProps = Props[TheatreActor],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )

    val movieShard: ActorRef = ClusterSharding(system).start(
      typeName = MovieActor.shardName,
      entityProps = Props[MovieActor],
      settings = ClusterShardingSettings(system),
      extractEntityId = Command.idExtractor,
      extractShardId = Command.shardResolver
    )
  }
}
