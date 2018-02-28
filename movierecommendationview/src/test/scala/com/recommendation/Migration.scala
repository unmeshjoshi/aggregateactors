package com.recommendation

import org.neo4j.graphdb.GraphDatabaseService

trait Migration {
  def apply(db: GraphDatabaseService): Unit
}