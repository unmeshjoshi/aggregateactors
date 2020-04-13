package com.moviebooking.writeside.aggregates

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import org.scalatest.FunSuite

class SubSourceTest extends FunSuite {

  case class Message(members: List[String])

  class OnePullGraphStage extends GraphStage[SinkShape[Int]] {
    val inlet                 = Inlet[Int]("in")
    val shape: SinkShape[Int] = SinkShape[Int](inlet)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
      val logic = new GraphStageLogic(shape) with InHandler {
        override def preStart(): Unit = {
          pull(inlet)
        }

        override def onPush(): Unit = {
          val elem = grab(inlet)
          println(elem)
        }
        setHandler(inlet, this)
      }
      logic
    }

  }

  test("should process each element in a list") {
    implicit val system                      = ActorSystem("SubFlow")
    implicit val materializer                = ActorMaterializer()
    val bufferedSource: Source[Int, NotUsed] = Source(1 to 10000).buffer(64, OverflowStrategy.backpressure)
    val messages: Source[Message, NotUsed]   = bufferedSource.map(i ⇒ Message(List(1 to 4).map(_ + "")))
    val sink: Sink[Int, NotUsed]             = Sink.fromGraph(new OnePullGraphStage())
    bufferedSource.runWith(sink)
  }
}
