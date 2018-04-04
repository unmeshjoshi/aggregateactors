lazy val aggregatedProjects: Seq[ProjectReference] = Seq(
  `moviebookingservices`,
  `movierecommendationview`,
  `moviebookingapp`)


lazy val `moviebookingservices` = project
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