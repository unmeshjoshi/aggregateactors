package com.recommendation

import com.recommendation.ExampleData.BetterString
import org.neo4j.graphdb.{GraphDatabaseService, Result}
import org.scalatest.FunSuite
import scala.collection.JavaConverters._


class ExampleDataTest extends FunSuite {
  test("should create cypher query from list of names") {
    println(ExampleData.movieGraph)

    val database: GraphDatabaseService = DatabaseFixture.createDatabase

    val personsQuery = ExampleData.personQuery("Vireshwar Gokhale",
      "Raghunath Balwant Phadke",
      "Janaki Raghunath Phadke",
    "Gopal Raghunath Phadke",
    "Radhabai Govind Deodhar",
    "Govind Mahadev Deodhar",
    "Anandi", "Indira")

    println(personsQuery)

    database.executeTransactionally(personsQuery)

    implicit def stringToString(s: String) = new BetterString(s, database)

    "janaki_raghunath_phadke" childOf "vireshwar_gokhale"
    "janaki_raghunath_phadke" marriedTo "gopal_raghunath_phadke"
    "gopal_raghunath_phadke" childOf "janaki_raghunath_phadke"
    "gopal_raghunath_phadke" marriedTo "anandi"
    "gopal_raghunath_phadke" marriedTo "indira"
     "radhabai_govind_deodhar" childOf "janaki_raghunath_phadke"
    "radhabai_govind_deodhar" marriedTo "govind_mahadev_deodhar"

    val ans = findAns("Janaki Raghunath Phadke", database)
    println(ans)
  }

  def findAns(name:String, db: GraphDatabaseService) = {
    val cypher =
      "MATCH (p:Person)<-[:CHILD_OF]-(c:Person) WHERE c.name = $name RETURN p.name";
    val params = Map("name" → name)
      .asInstanceOf[Map[String, AnyRef]]
      .asJava
    val tx             = db.beginTx();
    val result: Result = tx.execute(cypher, params);
    val parents =
      result.asScala.map(result ⇒ result.get("name").asInstanceOf[String])
    parents
  }

}