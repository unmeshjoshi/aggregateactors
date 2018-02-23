package com.moviebooking.common

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

object ClusterSettings {
  val clusterName = "aggregate-cluster"
  val seedPort = 2552
}

class ClusterSettings(listenPort:Int = 0) {
  def hostname: String = new Networks("").hostname()

  //Get the port for current ActorSystem to start. If no port is provided 0 will be used default.
  //SeedNode should start on a fixed port and rest all can start on random port.
  private def port: Int = listenPort

  //Get the managementPort to start akka cluster management service.
  def managementPort: Option[Any] = None

  //Prepare a list of seedNodes provided via clusterSeeds
  def seedNodes: List[String] = {
    val seeds = List(s"${hostname}:${ClusterSettings.seedPort}").flatMap(_.toString.split(",")).map(_.trim)
    seeds.map(seed ⇒ s"akka.tcp://$ClusterSettings.clusterName@$seed")
  }
  //Prepare config for ActorSystem to join csw-cluster
  def config: Config = {
    val computedValues: Map[String, Any] = Map(
      "akka.remote.netty.tcp.hostname"        → hostname,
      "akka.remote.netty.tcp.port"            → port,
      "akka.cluster.seed-nodes"               → seedNodes.asJava,
      "akka.cluster.http.management.hostname" → hostname,
      "akka.cluster.http.management.port"     → managementPort.getOrElse(19999),
      "startManagement"                       → managementPort.isDefined
    )

    println(s"ClusterSettings using following configuration: [${computedValues.mkString(", ")}]")
    ConfigFactory
      .parseMap(computedValues.asJava)
      .withFallback(ConfigFactory.load().getConfig(clusterName))
      .withFallback(ConfigFactory.defaultApplication().resolve())

  }

  def system: ActorSystem = ActorSystem(clusterName, config)

  def sharedStorePath = s"akka.tcp://aggregate-cluster@${hostname}:${seedPort}/user/store"
}
