package com.recommendation

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.test.TestGraphDatabaseFactory

object DatabaseFixture {
  def createDatabase = new DatabaseFixtureBuilder(new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder.newGraphDatabase)

  def useExistingDatabase(db: GraphDatabaseService) = new DatabaseFixtureBuilder(db)

  class DatabaseFixtureBuilder(val db: GraphDatabaseService) {
    private var initialContents: String = _

    def populateWith(cypher: String): DatabaseFixtureBuilder = {
      initialContents = cypher
      this
    }

    def applyMigrations(migrations: Iterable[Migration]) = new DatabaseFixture(db, initialContents, migrations)

    def noMigrations = new DatabaseFixture(db, initialContents, List[Migration]())
  }

}

class DatabaseFixture private (val db: GraphDatabaseService, val initialContents: String, val migrations: Iterable[Migration]) {
  populateWith(initialContents)
  applyMigrations(migrations)

  def database: GraphDatabaseService = db

  def shutdown(): Unit = {
    db.shutdown()
  }

  private def populateWith(cypher: String) = db.execute(cypher)

  private def applyMigrations(migrations: Iterable[Migration]): Unit = {
    for (migration <- migrations) {
      migration.apply(db)
    }
  }
}
