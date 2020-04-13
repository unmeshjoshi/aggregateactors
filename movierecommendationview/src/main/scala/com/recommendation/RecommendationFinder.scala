package com.recommendation

import org.neo4j.graphdb.{GraphDatabaseService, Result, ResultTransformer}

import scala.collection.JavaConverters._

class RecommendationFinder(val db: GraphDatabaseService) {

  def findRecommendationFor(userName: String): Iterator[String] = {
    val cypher =
      "MATCH (user:Person)-[:BOOKED]->(movie)<-[:ACTED_IN]-(actor:Person) MATCH (actor:Person)-[:ACTED_IN]->(movie1:Movie) WHERE user.name = $userName RETURN movie1.name as movieName";
    val params = Map("userName" → userName)
      .asInstanceOf[Map[String, AnyRef]]
      .asJava
    val tx             = db.beginTx();
    val result: Result = tx.execute(cypher, params);
    val movieNames =
      result.asScala.map(result ⇒ result.get("movieName").asInstanceOf[String])
    movieNames
  }
}
