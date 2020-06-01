package com.recommendation

import org.neo4j.graphdb.GraphDatabaseService

object ExampleData {

  def personQuery(names:String*) = {
    val namesQueryPart = names.toList.map(name ⇒ {
      person(name)
    }).mkString(",\n")

    s"CREATE ${namesQueryPart}"
  }


  def person(name:String) = {
    s"(${identifier(name)}:Person{name:'${name}'})"
  }

  private def identifier(name: String) = {
    name.toLowerCase.strip().replace(" ", "_")
  }


  class BetterString(person1:String, val database: GraphDatabaseService) {
    def childOf(person2:String) = {
      val query = s"CREATE (${person1})-[:CHILD_OF]->(${person2})"
      println(query)
      database.executeTransactionally(query)
    }
    def marriedTo(person2:String) = {
      val query = s"MATCH (child: Person) MATCH (parent:Person) CREATE (child)-[:CHILD_OF]->(parent)"
      println(query)
      database.executeTransactionally(query)
    }
  }


  val movieGraph = "CREATE (movie1:Movie{name:'Justice League'}), \n" +
  "              (movie2:Movie{name:'Jurasic Park'}),\n" +
  "              (movie3:Movie{name:'Green Lantern'}),\n" +
  "              (movie4:Movie{name:'Spider Man'}),\n" +
  "              (movie5:Movie{name:'Kaun Banega Karodpati'}),\n" +
  "              (bill:Person{name:'Bill'}), \n" +
  "              (gill:Person{name:'gill'}), \n" +
  "              (joyce:Person{name:'Joyce'}), \n" +
  "              (bill)-[:ACTED_IN]->(movie1), \n" +
  "              (gill)-[:ACTED_IN]->(movie3), \n" +
  "              (joyce)-[:ACTED_IN]->(movie4), \n" +
  "              (user1:Person{name:'User1'}), \n" +
  "              (user1)-[:BOOKED]->(movie1), \n" +
  "              (user1)-[:BOOKED]->(movie3), \n" +
  "              (bill)-[:ACTED_IN]->(movie2), \n" +
  "              (bill)-[:ACTED_IN]->(movie5)"

  val smallGraph = "CREATE (ian:Person {name:'Ian'}),\n" +
  "       (bill:Person {name:'Bill'}),\n" +
  "       (lucy:Person {name:'Lucy'}),\n" +
  "       (acme:Company {name:'Acme'}),\n" +
  "       (java:Skill {name:'Java'}),\n" +
  "       (csharp:Skill {name:'C#'}),\n" +
  "       (neo4j:Skill {name:'Neo4j'}),\n" +
  "       (ruby:Skill {name:'Ruby'}),\n" +
  "       (ian)-[:WORKS_FOR]->(acme),\n" +
  "       (bill)-[:WORKS_FOR]->(acme),\n" +
  "       (lucy)-[:WORKS_FOR]->(acme),\n" +
  "       (ian)-[:HAS_SKILL]->(java),\n" +
  "       (ian)-[:HAS_SKILL]->(csharp),\n" +
  "       (ian)-[:HAS_SKILL]->(neo4j),\n" +
  "       (bill)-[:HAS_SKILL]->(neo4j),\n" +
  "       (bill)-[:HAS_SKILL]->(ruby),\n" +
  "       (lucy)-[:HAS_SKILL]->(java),\n" +
  "       (lucy)-[:HAS_SKILL]->(neo4j)";

  val largeGraph = "CREATE\n" +
  "(ben:Person {name:'Ben'}),\n" +
  "(arnold:Person {name:'Arnold'}),\n" +
  "(charlie:Person {name:'Charlie'}),\n" +
  "(gordon:Person {name:'Gordon'}),\n" +
  "(lucy:Person {name:'Lucy'}),\n" +
  "(emily:Person {name:'Emily'}),\n" +
  "(ian:Person {name:'Ian'}),\n" +
  "(kate:Person {name:'Kate'}),\n" +
  "(acme:Company {name:'Acme, Inc'}),\n" +
  "(startup:Company {name:'Startup, Ltd'}),\n" +
  "(neo4j:Skill {name:'Neo4j'}),\n" + //graphs
  "(rest:Skill {name:'REST'}),\n" +
  "(dotNet:Skill {name:'DotNet'}),\n" +             //art
  "(ruby:Skill {name:'Ruby'}),\n" +                 //design
  "(sql:Skill {name:'SQL'}),\n" +                   // medicine
  "(architecture:Skill {name:'Architecture'}),\n" + //drama
  "(java:Skill {name:'Java'}),\n" +
  "(python:Skill {name:'Python'}),\n" +         //music
  "(javascript:Skill {name:'Javascript'}),\n" + //cars
  "(clojure:Skill {name:'Clojure'}),\n" +       //travel
  "(phoenix:Project {name:'Phoenix'}),\n" +
  "(quantumLeap:Project {name:'Quantum Leap'}),\n" +
  "(nextGenPlatform:Project {name:'Next Gen Platform'}),\n" +
  "ben-[:WORKS_FOR]->acme,\n" +
  "charlie-[:WORKS_FOR]->acme,\n" +
  "lucy-[:WORKS_FOR]->acme,\n" +
  "ian-[:WORKS_FOR]->acme,\n" +
  "arnold-[:WORKS_FOR]->startup,\n" +
  "gordon-[:WORKS_FOR]->startup,\n" +
  "emily-[:WORKS_FOR]->startup,\n" +
  "kate-[:WORKS_FOR]->startup,\n" +
  "ben-[:HAS_SKILL]->neo4j,\n" +
  "ben-[:HAS_SKILL]->rest,\n" +
  "arnold-[:HAS_SKILL]->neo4j,\n" +
  "arnold-[:HAS_SKILL]->java,\n" +
  "arnold-[:HAS_SKILL]->rest,\n" +
  "arnold-[:HAS_SKILL]->clojure,\n" +
  "charlie-[:HAS_SKILL]->neo4j,\n" +
  "charlie-[:HAS_SKILL]->javascript,\n" +
  "charlie-[:HAS_SKILL]->sql,\n" +
  "gordon-[:HAS_SKILL]->neo4j,\n" +
  "gordon-[:HAS_SKILL]->dotNet,\n" +
  "gordon-[:HAS_SKILL]->python,\n" +
  "lucy-[:HAS_SKILL]->dotNet,\n" +
  "lucy-[:HAS_SKILL]->architecture,\n" +
  "lucy-[:HAS_SKILL]->python,\n" +
  "emily-[:HAS_SKILL]->dotNet,\n" +
  "emily-[:HAS_SKILL]->ruby,\n" +
  "ian-[:HAS_SKILL]->java,\n" +
  "ian-[:HAS_SKILL]->neo4j,\n" +
  "ian-[:HAS_SKILL]->rest,\n" +
  "kate-[:HAS_SKILL]->architecture,\n" +
  "kate-[:HAS_SKILL]->python,\n" +
  "arnold-[:WORKED_ON]->phoenix,\n" +
  "kate-[:WORKED_ON]->phoenix,\n" +
  "kate-[:WORKED_ON]->quantumLeap,\n" +
  "emily-[:WORKED_ON]->quantumLeap,\n" +
  "ben-[:WORKED_ON]->nextGenPlatform,\n" +
  "emily-[:WORKED_ON]->nextGenPlatform,\n" +
  "charlie-[:WORKED_ON]->nextGenPlatform,\n" +
  "ian-[:WORKED_ON]->nextGenPlatform,\n" +
  "ian-[:WORKED_ON]->quantumLeap";
}
