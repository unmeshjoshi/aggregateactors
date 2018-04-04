package com.moviebooking.aggregates

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class SampleActor extends Actor {
  override def receive: PartialFunction[Any, Unit] = {
    case _ ⇒ throw new RuntimeException("Killing actor")
  }
}

object ActorTest extends App {

  val system                   = ActorSystem("test")
  private val sample: ActorRef = system.actorOf(Props[SampleActor])

  while (true) {
    sample ! "test message"
    Thread.sleep(100)
  }

}
