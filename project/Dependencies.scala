import sbt._

object Dependencies {

  val Version = "0.1-SNAPSHOT"
  val Service = Seq(
    Libs.`mockito-core` % Test,
    Libs.`scalatest` % Test,
    Libs.`logback-classic`,
    AkkaHttp.`akka-http`,
    Neo4JDriver.`neo4jJava`
  )
  val Spark = Seq(
    Libs.`scalatest` % Test,
    SparkLibs.sparkCore,
    SparkLibs.sparkStreaming,
    SparkLibs.sparkSQL,
    SparkLibs.sparkHiveSQL,
    SparkLibs.sparkTestingBase,
    SparkLibs.sparkStreamingKafka
  )

  val Aggregates = Seq(
    Libs.`play-json`,
    Libs.`play-json-extensions`,
    Libs.`akka-http-play-json`,
    Libs.`logback-classic`,
    AkkaHttp.`akka-http`,
    Akka.`akka-actor`,
    Akka.`akka-remote`,
    Akka.`akka-cluster-tools`,
    Akka.`akka-cluster-sharding`,
    Akka.`akka-persistence`,
    Akka.`akka-persistence-cassandra`,
    Akka.`akka-persistence-query`,
    Akka.`akka-persistence-cassandra-launcher` % Test,
    Akka.`leveldb`,
    Akka.`leveldb-jni`,
    Akka.`akka-multi-node-testkit` % Test,
    Akka.`alpakka-ftp`,
    Kafka.akkaStreamKafka,
    Kafka.`scalatest-embedded-kafka`,
    Messaging.`lettuce`,
    Enumeratum.`enumeratum`,
    Enumeratum.`enumeratum-play`,
    Prometheus.`hotspot-client`,
    Prometheus.`simpleclient`
  )

  val MovieRecommendations = Seq(
    Kafka.akkaStreamKafka,
    Neo4J.`neo4j-java`,
    Neo4J.`neo4j-core`,
    Neo4J.`neo4j-cypher`,
    Neo4J.`neo4j-test` % Test,
    Libs.`scalatest` % Test,
    Libs.`logback-classic`,
    Kafka.akkaStreamKafka,
    Kafka.`scalatest-embedded-kafka` % Test,
    Enumeratum.`enumeratum`,
    Enumeratum.`enumeratum-play`,
    Prometheus.`hotspot-client`,
    Prometheus.`simpleclient`
  )

  val ViewService = Seq(
    Kafka.akkaStreamKafka,
    Libs.`play-json`,
    Libs.`play-json-extensions`,
    Libs.`akka-http-play-json`,
    Libs.`logback-classic`,
    AkkaHttp.`akka-http`,
    Enumeratum.`enumeratum`,
    Enumeratum.`enumeratum-play`,
    Libs.`scalatest` % Test,
    Messaging.`lettuce`,
    Messaging.`embedded-redis` % Test,
    Libs.`guice`,
    Prometheus.`hotspot-client`,
    Prometheus.`simpleclient`
  )

  val MovieDataIngestion = Seq(
    Kafka.akkaStreamKafka,
    Libs.`play-json`,
    Libs.`play-json-extensions`,
    Libs.`akka-http-play-json`,
    Libs.`logback-classic`,
    AkkaHttp.`akka-http`,
    Enumeratum.`enumeratum`,
    Enumeratum.`enumeratum-play`,
    Libs.`scalatest` % Test,
    Messaging.`lettuce`,
    Messaging.`embedded-redis` % Test,
    Libs.`guice`,
    Prometheus.`hotspot-client`,
    Prometheus.`simpleclient`
  )
}
