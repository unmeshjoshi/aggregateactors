package com.moviebooking.writeside.apps

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import com.moviebooking.writeside.aggregates.Event
import com.moviebooking.writeside.services.JsonSupport
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import play.api.libs.json._

import scala.concurrent.Future

object EventPublisher extends App with JsonSupport {
  import org.slf4j.LoggerFactory
  val logger = LoggerFactory.getLogger(this.getClass)

  val kafkaTopic = "seatAvailability"

  implicit val system = ActorSystem("EventPublisher")

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

  def log(str: String) = logger.info(str)

  persistentIds.runForeach((id: String) ⇒ {

    log(s"reading events for persisten id ${id}")

    val eventEnvelopeSource: Source[EventEnvelope, NotUsed] =
      readJournal.eventsByPersistenceId(id, 0, Long.MaxValue)

    val eventSource: Source[Event, NotUsed] =
      eventEnvelopeSource
        .map(event ⇒ event.event.asInstanceOf[Event])
        .map(event ⇒ {
          log(event.toString)
          event
        })

    eventSource.map(convert).to(kafkaProducerSink).run()
  })

  def producerRecord(event: Event) = {
    new ProducerRecord(kafkaTopic, event.id, event)
  }

  private def convert(event: Event): ProducerRecord[String, String] = {
    try {
      val node = Json.toJson(event)
      val str  = node.toString()
      log(s"json event is ${str}")
      new ProducerRecord("seatAvailability", event.id, str)
    } catch {
      case any: Throwable ⇒ {
        any.printStackTrace()
        throw any
      }
    }
  }

  private def producerSettings(host: String, port: Int)(implicit actorSystem: ActorSystem) =
    ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
      .withBootstrapServers(s"$host:$port")
}
