package com.recommendation

object Neo4JTest extends App {
  val db = DatabaseFixture.createDatabase
  db.executeTransactionally(ExampleData.movieGraph)
  val finder = new RecommendationFinder(db)
  finder.findRecommendationFor("User1")
}
