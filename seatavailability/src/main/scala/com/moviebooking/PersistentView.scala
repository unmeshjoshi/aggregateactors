package com.moviebooking

import akka.NotUsed
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.moviebooking.common.ClusterSettings

object PersistentView extends App {
  private val settings = new ClusterSettings(2556)
  implicit val system = settings.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val readJournal =
    PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)
  private val value: Source[String, NotUsed] = readJournal.persistenceIds()

  value.runForeach((id: String) ⇒ {
    println(s"reading events for persisten id ${id}")
    val events = readJournal.eventsByPersistenceId(id, 0, Long.MaxValue)
    events.runForeach(event ⇒ println(event.event))
  })

}
