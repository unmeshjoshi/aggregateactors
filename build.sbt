


lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  `moviebookingservices`,
  `payment`,
  `booking`,
  `moviebookingapp`)


val `aggregatesactors` = project
  .in(file("."))
  .enablePlugins(DeployApp)
  .enablePlugins(DeployApp, DockerPlugin)
  .aggregate(aggregatedProjects: _*)


lazy val `moviebookingservices` = project
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
lazy val `moviebookingapp` = project
  .enablePlugins(DeployApp,PlayScala)
    .dependsOn(`moviebookingservices`)
  .settings(
    libraryDependencies ++= Dependencies.ViewService :+ guice)



//Read side
lazy val `movierecommendationview` = project
  .dependsOn(`moviebookingservices`)
  .enablePlugins(DeployApp)
  .settings(
    libraryDependencies ++= Dependencies.MovieRecommendations
  )