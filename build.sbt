lazy val akkaHttpVersion      = "10.0.9"
lazy val akkaVersion          = "2.5.4"
lazy val mongoDBCasbahVersion = "3.1.1"
lazy val scalajHttpVersion    = "2.3.0"
lazy val scalaTestVersion     = "3.0.1"
lazy val salatVersion         = "1.11.2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.technologyconversations.api",
      scalaVersion    := "2.12.3",
      version         := "1.0"
    )),
    name := "books-ms",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

      "org.mongodb" %% "casbah" % mongoDBCasbahVersion,

      "com.github.salat" %% "salat" % salatVersion,


      "org.scalatest"     %% "scalatest"         % scalaTestVersion  % Test,
      "org.scalaj"        %% "scalaj-http"       % scalajHttpVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion   % Test
    )
  )
