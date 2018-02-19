import Settings._


lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  `moviebookingview`,
  `seatavailability`,
  `payment`,
  `booking`)


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
lazy val `moviebookingview` = project
  .settings(
    libraryDependencies ++= Dependencies.Aggregates
  )