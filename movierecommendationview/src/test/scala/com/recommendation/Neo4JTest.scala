package com.recommendation

object Neo4JTest extends App {

  val finder = new RecommendationFinder(DatabaseFixture
                        .createDatabase
                        .populateWith(ExampleData.movieGraph).applyMigrations(List[Migration]()).database)
  finder.findRecommendationFor("User1")
}
