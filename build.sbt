import Settings._


lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  `seatavailability`,
  `payment`,
  `booking`,
  `seatavailabilityview`,
  `movierecommendationview`)


val `aggregatesactors` = project
  .in(file("."))
  .enablePlugins(DeployApp, DockerPlugin)
  .aggregate(aggregatedProjects: _*)


lazy val `seatavailability` = project
  .settings(
    libraryDependencies ++= Dependencies.Aggregates
  )


lazy val `payment` = project
  .settings(
    libraryDependencies ++= Dependencies.Aggregates
  )

//Write side
lazy val `booking` = project
  .settings(
    libraryDependencies ++= Dependencies.Aggregates
  )

  //Read side
lazy val `seatavailabilityview` = project
  .settings(
    libraryDependencies ++= Dependencies.Aggregates
  )


//Read side
lazy val `movierecommendationview` = project
  .settings(
    libraryDependencies ++= Dependencies.MovieRecommendations
  )