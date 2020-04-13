package com.recommendation

import java.io.File

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME

object DatabaseFixture {
  private val databaseDirectory = new File("target/java-query-db")
  def createDatabase = {
    import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
    val managementService = new DatabaseManagementServiceBuilder(databaseDirectory).build
    managementService.database(DEFAULT_DATABASE_NAME)
  }

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

  def shutdown(): Unit = {}

  private def populateWith(cypher: String) = {
    val tx = db.beginTx()
    tx.execute(cypher);
    tx.commit();
  }

  private def applyMigrations(migrations: Iterable[Migration]): Unit = {
    for (migration <- migrations) {
      migration.apply(db)
    }
  }
}
