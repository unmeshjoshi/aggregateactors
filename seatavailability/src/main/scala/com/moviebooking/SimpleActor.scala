package com.moviebooking

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class SimpleActor extends Actor {
  override def receive: Receive = {
    case a @ _ ⇒ println(s"Received ${a}")
  }
}

object SimpleActorApp extends App {
  val system = ActorSystem("Testing")
  val ref: ActorRef = system.actorOf(Props[SimpleActor])
  ref ! "Hello World!"

  println("ActorSystem started")
}
