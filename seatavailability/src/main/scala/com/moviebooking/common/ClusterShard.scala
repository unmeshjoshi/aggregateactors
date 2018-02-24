package com.moviebooking.common

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.moviebooking.SharedStoreSeedApp
import com.moviebooking.aggregates.messages.Command
import com.moviebooking.aggregates.{Order, Payment, Screen}

object ClusterShard {

  def shardRegion(name: String)(implicit system: ActorSystem) = {
    ClusterSharding.get(system).shardRegion(name)
  }

  def start()(implicit system: ActorSystem) = {
    val screenShard: ActorRef = ClusterSharding(system).start(
      typeName = Screen.shardName,
      entityProps = Props[Screen],
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

//    SharedStoreSeedApp.registerSharedJournal(system)
  }
}
