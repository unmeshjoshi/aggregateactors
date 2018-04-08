package com.moviebooking.readside.repository

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection

import scala.concurrent.Future

class ReactiveRedis {
  val client                                                   = RedisClient.create("redis://localhost")
  val redisConnection: StatefulRedisConnection[String, String] = client.connect

  def get(key: String)(implicit materializer: ActorMaterializer): Future[String] = {
    val valuePublisher = redisConnection.reactive().get(key)
    Source.fromPublisher(valuePublisher).runFold("")(_ ++ _)
  }
}
