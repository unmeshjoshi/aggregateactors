package com.recommendation

object Neo4JTest extends App {
  val db = DatabaseFixture.createDatabase
  db.executeTransactionally(ExampleData.movieGraph)
  val finder = new RecommendationFinder(db)
  private val strings: Iterator[String] = finder.findRecommendationFor("User1")
  strings.foreach(movie ⇒ println(movie))
}
