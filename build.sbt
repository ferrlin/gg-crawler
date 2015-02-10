name := "gg-crawler"

version := "0.3.0"

crossScalaVersions := Seq("2.10.4", "2.11.5")

scalaVersion := "2.11.5"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

libraryDependencies ++= {
  val akkaV = "2.3.8"
  val sprayV = "1.3.2"
  val jsonV = "1.3.0"
  val sioV = "0.4.3"
  val konV = "0.3.2"
  val tikaV = "1.6"
  val testV = "2.2.1"
  val argoV = "6.0.4"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "io.spray" %% "spray-can" % sprayV,
    "io.spray" %% "spray-client" % sprayV,
    "io.spray" %% "spray-routing" % sprayV,
    "io.argonaut" %% "argonaut" % argoV,
    "in.ferrl" %% "aktic" % "0.1.0",
    "com.github.scala-incubator.io" %% "scala-io-core" % sioV,
    "com.github.scala-incubator.io" %% "scala-io-file" % sioV,
    "org.apache.tika" % "tika-core" % tikaV,
    "org.apache.tika" % "tika-parsers" % tikaV,
    "com.datastax.cassandra" % "cassandra-driver-core" % "1.0.1" exclude ("org.xerial.snappy", "snappy-java"),
    "org.xerial.snappy" % "snappy-java" % "1.0.5",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    "org.scalatest" %% "scalatest" % testV % "test",
    "com.novocode" % "junit-interface" % "0.7" % "test->default",
    "io.kamon" %% "kamon-core" % konV,
    "io.kamon" %% "kamon-spray" % konV,
    "io.kamon" %% "kamon-statsd" % konV)
}

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8")

parallelExecution in Test := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")