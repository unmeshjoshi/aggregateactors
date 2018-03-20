import Settings._


lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  `seatavailability`,
  `payment`,
  `booking`,
  `seatavailabilityview`,
  `movierecommendationview`)


val `aggregatesactors` = project
  .in(file("."))
  .enablePlugins(DeployApp)
  .enablePlugins(DeployApp, DockerPlugin)
  .aggregate(aggregatedProjects: _*)


lazy val `seatavailability` = project
  .enablePlugins(DeployApp)
  .settings(
    libraryDependencies ++= Dependencies.Aggregates
  )


lazy val `payment` = project
  .settings(
    libraryDependencies ++= Dependencies.Aggregates
  )

//Write side
lazy val `booking` = project
  .enablePlugins(DeployApp)
  .settings(
    libraryDependencies ++= Dependencies.Aggregates
  )

  //Read side
lazy val `seatavailabilityview` = project
  .enablePlugins(DeployApp)
  .settings(
    libraryDependencies ++= Dependencies.ViewService
  )


//Read side
lazy val `movierecommendationview` = project
  .dependsOn(`seatavailability`)
  .enablePlugins(DeployApp)
  .settings(
    libraryDependencies ++= Dependencies.MovieRecommendations
  )