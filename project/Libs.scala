import sbt._
import scalapb.compiler.Version.scalapbVersion

object Libs {
  val ScalaVersion = "2.11.8"

  val `scalatest`                    = "org.scalatest"          %% "scalatest"                    % "3.0.4" //Apache License 2.0
  val `scala-java8-compat`           = "org.scala-lang.modules" %% "scala-java8-compat"           % "0.8.0" //BSD 3-clause "New" or "Revised" License
  val `scala-async`                  = "org.scala-lang.modules" %% "scala-async"                  % "0.9.7" //BSD 3-clause "New" or "Revised" License
  val `scopt`                        = "com.github.scopt"       %% "scopt"                        % "3.7.0" //MIT License
  val `acyclic`                      = "com.lihaoyi"            %% "acyclic"                      % "0.1.7" % Provided //MIT License
  val `junit`                        = "junit"                  % "junit"                         % "4.12" //Eclipse Public License 1.0
  val `junit-interface`              = "com.novocode"           % "junit-interface"               % "0.11" //BSD 2-clause "Simplified" License
  val `mockito-core`                 = "org.mockito"            % "mockito-core"                  % "2.12.0" //MIT License
  val `logback-classic`              = "ch.qos.logback"         % "logback-classic"               % "1.2.3" //Dual license: Either, Eclipse Public License v1.0 or GNU Lesser General Public License version 2.1
  val `akka-management-cluster-http` = "com.lightbend.akka"     %% "akka-management-cluster-http" % "0.5" //N/A at the moment
  val svnkit                         = "org.tmatesoft.svnkit"   % "svnkit"                        % "1.9.0" //TMate Open Source License
  val `commons-codec`                = "commons-codec"          % "commons-codec"                 % "1.10" //Apache 2.0
  val `persist-json`                 = "com.persist"            %% "persist-json"                 % "1.2.1" //Apache 2.0
  val `joda-time`                    = "joda-time"              % "joda-time"                     % "2.9.9" //Apache 2.0
  val `scala-reflect`                = "org.scala-lang"         % "scala-reflect"                 % ScalaVersion //BSD-3
  val `gson`                         = "com.google.code.gson"   % "gson"                          % "2.8.2" //Apache 2.0
  val `play-json`                    = "com.typesafe.play"      %% "play-json"                    % "2.6.7" //Apache 2.0
  val `play-json-extensions`         = "ai.x"                   %% "play-json-extensions"         % "0.10.0" //Simplified BSD License
  val `akka-http-play-json`          = "de.heikoseeberger"      %% "akka-http-play-json"          % "1.18.1" //Apache 2.0
  val `scalapb-runtime`              = "com.trueaccord.scalapb" %% "scalapb-runtime"              % scalapbVersion % "protobuf"
  val `scalapb-json4s`               = "com.trueaccord.scalapb" %% "scalapb-json4s"               % "0.3.3"
  val `derby`                        = "org.apache.derby" % "derby" % "10.14.1.0"
}

object Messaging {
  val `artemis-client`               = "org.apache.activemq" % "artemis-core-client" % "2.4.0"
  val `jms`                          = "javax.jms" % "javax.jms-api" % "2.0"
  val `qpid`                         = "org.apache.qpid" % "qpid-client" % "6.3.0"
  val `qpid-aqmp-client`             = "org.apache.qpid" % "qpid-amqp-1-0-client-jms" % "0.32"
  val `qpid-jms-client`              =  "org.apache.qpid" % "qpid-jms-client" % "0.28.0"
  val `rabbitmq-aqmp-client`         =  "com.rabbitmq" % "amqp-client" % "4.1.1"
  val `jedis`                        =  "redis.clients" % "jedis" % "2.9.0"
}

object Neo4JDriver {
  val `neo4jJava` = "org.neo4j.driver" % "neo4j-java-driver" % "1.5.1"
}

object Kafka {
  val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % "0.19"
  val `scalatest-embedded-kafka`     = "net.manub"              %% "scalatest-embedded-kafka"     % "1.0.0"
}

object Alpakka {
  val alpakkaFtp = "com.lightbend.akka" %% "akka-stream-alpakka-ftp" % "0.16"
}

object HBase {
  val hbaseVersion = "1.2.4"
  val hadoopVersion = "2.5.1"
  val hbaseClient =  "org.apache.hbase" % "hbase-client" % hbaseVersion exclude ("log4j", "log4j") exclude ("org.slf4j", "slf4j-log4j12")// ApacheV2,
  val hbaseCommon =  "org.apache.hbase" % "hbase-common" % hbaseVersion exclude ("log4j", "log4j") exclude ("org.slf4j", "slf4j-log4j12") // ApacheV2,
  val hadoopCommon = "org.apache.hadoop" % "hadoop-common" % hadoopVersion exclude ("log4j", "log4j") exclude ("org.slf4j", "slf4j-log4j12") // ApacheV2,
  val hadoopMapReduce = "org.apache.hadoop" % "hadoop-mapreduce-client-core" % hadoopVersion exclude ("log4j", "log4j") exclude ("org.slf4j", "slf4j-log4j12") // ApacheV2,
}

object SparkLibs {
  val Version        = "2.2.1"
  val sparkCore      = "org.apache.spark"  %% "spark-core"      % Version
  val sparkStreaming = "org.apache.spark"  %% "spark-streaming" % Version
  val sparkSQL       = "org.apache.spark"  %% "spark-sql"       % Version
  val sparkHiveSQL   = "org.apache.spark"  %% "spark-hive"      % Version
  val sparkTestingBase = "com.holdenkarau" %% "spark-testing-base" % "2.2.0_0.8.0" % "test"
  //FIXME  val sparkRepl      = "org.apache.spark"  %% "spark-repl"      % Version

}

object Jackson {
  val Version                = "2.9.2"
  val `jackson-core`         = "com.fasterxml.jackson.core" % "jackson-core" % Version
  val `jackson-databind`     = "com.fasterxml.jackson.core" % "jackson-databind" % Version
  val `jackson-module-scala` = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Version
}
object Enumeratum {
  val version           = "1.5.12"
  val `enumeratum`      = "com.beachape" %% "enumeratum" % version //MIT License
  val `enumeratum-play` = "com.beachape" %% "enumeratum-play" % version //MIT License
}

object Chill {
  val Version           = "0.9.2"
  val `chill-akka`      = "com.twitter" %% "chill-akka" % Version //Apache License 2.0
  val `chill-bijection` = "com.twitter" %% "chill-bijection" % Version //Apache License 2.0
}

object Akka {
  val Version                   = "2.5.10" //all akka is Apache License 2.0
  val `akka-stream`             = "com.typesafe.akka" %% "akka-stream" % Version
  val `akka-remote`             = "com.typesafe.akka" %% "akka-remote" % Version
  val `akka-stream-testkit`     = "com.typesafe.akka" %% "akka-stream-testkit" % Version
  val `akka-actor`              = "com.typesafe.akka" %% "akka-actor" % Version
  val `akka-typed`              = "com.typesafe.akka" %% "akka-typed" % Version
  val `akka-persistence`        =   "com.typesafe.akka"           %% "akka-persistence" % Version
  val `akka-persistence-query`        =   "com.typesafe.akka" %% "akka-persistence-query" % Version
  val `leveldb`                 =   "org.iq80.leveldb"            % "leveldb"          % "0.7"
  val `leveldb-jni`             =   "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8"
  val `akka-typed-testkit`      = "com.typesafe.akka" %% "akka-typed-testkit" % Version
  val `akka-distributed-data`   = "com.typesafe.akka" %% "akka-distributed-data" % Version
  val `akka-multi-node-testkit` = "com.typesafe.akka" %% "akka-multi-node-testkit" % Version
  val `akka-cluster-tools`      = "com.typesafe.akka" %% "akka-cluster-tools" % Version
  val `akka-cluster-sharding`                        = "com.typesafe.akka" %% "akka-cluster-sharding" % Version
  val `akka-persistence-cassandra` = "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.83"
  val `akka-persistence-cassandra-launcher` = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % "0.83"
  val `akka-slf4j`              = "com.typesafe.akka" %% "akka-slf4j" % Version
}

object AkkaHttp {
  val Version             = "10.0.10"
  val `akka-http`         = "com.typesafe.akka" %% "akka-http" % Version //ApacheV2
  val `akka-http-testkit` = "com.typesafe.akka" %% "akka-http-testkit" % Version //ApacheV2
  val `akka-http2`        = "com.typesafe.akka" %% "akka-http2-support" % Version
}
  // http://doc.akka.io/docs/akka/2.4.1/scala/persistence.html#Local_LevelDB_journal

object Neo4J {
  val Version = "3.3.3"
  val `neo4j-java` = "org.neo4j.driver" % "neo4j-java-driver" % "1.5.1"
  val `neo4j-test` = "org.neo4j.test" % "neo4j-harness" % Version
  val `neo4j-core` =  "org.neo4j" % "neo4j" % Version
  val `neo4j-cypher` =  "org.neo4j" % "neo4j-cypher" % Version
}