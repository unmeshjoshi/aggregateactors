import sbt._

object Dependencies {

  val Version = "0.1-SNAPSHOT"
  val Service = Seq(
    Libs.`mockito-core` % Test,
    Libs.`scalatest` % Test,
    AkkaHttp.`akka-http`,
    Neo4JDriver.`neo4jJava`
  )
  val Spark = Seq(
    Libs.`scalatest` % Test,
    SparkLibs.sparkCore,
    SparkLibs.sparkStreaming,
    SparkLibs.sparkSQL,
    SparkLibs.sparkHiveSQL,
    SparkLibs.sparkTestingBase
  )

  val Aggregates = Seq(
    Akka.`akka-actor`,
    Akka.`akka-persistence`,
    Akka.`leveldb`,
    Akka.`leveldb-jni`,
    Akka.`akka-multi-node-testkit` % Test,
    Kafka.akkaStreamKafka,
    Enumeratum.`enumeratum`,
    Enumeratum.`enumeratum-play`
  )
}