package com.moviebooking.apps

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import com.moviebooking.aggregates.messages.Event
import com.moviebooking.common.ClusterSettings
import com.moviebooking.services.JsonSupport
import org.apache.kafka.clients.producer.{
  Callback,
  KafkaProducer,
  ProducerRecord
}
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json._

import scala.concurrent.{Future, Promise}

object EventReader extends App with JsonSupport {
  private val settings = new ClusterSettings(2556)
  implicit val system = settings.system

  val producerSettings: ProducerSettings[String, String] =
    producerSettings("localhost", 29092)
  val kafkaProducer: KafkaProducer[String, String] =
    producerSettings.createKafkaProducer()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val kafkaProducerSink: Sink[ProducerRecord[String, String], Future[Done]] =
    Producer.plainSink(producerSettings, kafkaProducer)
  val readJournal =
    PersistenceQuery(system)
      .readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)
  val persistentIds: Source[String, NotUsed] =
    readJournal.persistenceIds()

  persistentIds.runForeach((id: String) ⇒ {

    println(s"reading events for persisten id ${id}")

    val eventEnvelopeSource: Source[EventEnvelope, NotUsed] =
      readJournal.eventsByPersistenceId(id, 0, Long.MaxValue)

    val eventSource: Source[Event, NotUsed] =
      eventEnvelopeSource
        .map(event ⇒ event.event.asInstanceOf[Event])
        .map(event ⇒ {
          println(event)
          event
        })

    eventSource.map(convert).to(kafkaProducerSink).run()
    //    eventSource.map(convert).to(Sink.ignore).run()
    //    value.map(f ⇒ println(f))
  })

  def producerRecord(event: Event) = {
    new ProducerRecord("seatAvailability", event.id, event)
  }

  private def convert(event: Event): ProducerRecord[String, String] = {
    try {
      val node = Json.toJson(event)
      val str = node.toString()
      println(s"json event is ${str}")
      new ProducerRecord("seatAvailability", event.id, str)
    } catch {
      case any @ _ ⇒ {
        any.printStackTrace()
        throw any
      }
    }
  }

  private def producerSettings(host: String, port: Int)(
      implicit actorSystem: ActorSystem) =
    ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
      .withBootstrapServers(s"$host:$port")

  private def complete(p: Promise[Done]): Callback = {
    case (_, null) ⇒ p.success(Done)
    case (_, ex) ⇒ p.failure(ex)
  }
}
