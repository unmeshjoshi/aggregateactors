package com.recommendation

import org.neo4j.graphdb.{GraphDatabaseService, Result}

import scala.collection.JavaConverters._

class RecommendationFinder(val db: GraphDatabaseService) {

  def findRecommendationFor(userName:String): Unit = {
    val cypher = "MATCH (user:Person)-[:BOOKED]->(movie)<-[:ACTED_IN]-(actor:Person) MATCH (actor:Person)-[:ACTED_IN]->(movie1:Movie) WHERE user.name = {userName} RETURN movie1.name as movieName";
    val params = Map("userName"→ userName).asInstanceOf[Map[String, AnyRef]].asJava
    val result: Result = db.execute(cypher, params)
    while(result.hasNext) {
     println(result.next().get("movieName"))
    }
  }
}
