package com.recommendation

import org.neo4j.graphdb.GraphDatabaseService
import scala.collection.JavaConverters._

class RecommendationRepository(val db: GraphDatabaseService) {

  def addBooking(userName: String, movieName: String): Unit = {
    val cypher =
      "MERGE (user:Person{name: $userName}) WITH user MERGE (movie:Movie{name: $movieName}) WITH user,movie MERGE (user)-[:BOOKED]->(movie) return user.name as userName,movie.name as movieName";
    val params = Map(("userName" → userName), ("movieName" → movieName))
      .asInstanceOf[Map[String, AnyRef]]
      .asJava
    val result = db.executeTransactionally(cypher, params)
  }
}
