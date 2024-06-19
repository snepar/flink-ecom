name := "flink-ecom"

version := "0.1"

scalaVersion := "2.12.15"

val flinkVersion = "1.16.3"
val postgresVersion = "42.6.0"
val logbackVersion = "1.2.10"

val flinkDependencies = Seq(
  "org.apache.flink" % "flink-clients" % flinkVersion,
  "org.apache.flink" %% "flink-scala" % flinkVersion,
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion
)

val flinkConnectors = Seq(
  "org.apache.flink" % "flink-connector-kafka" % "1.16.3",
  "org.apache.flink" % "flink-connector-jdbc" % "1.16.3",
  "org.apache.flink" % "flink-connector-elasticsearch7" % "1.16.3",
  "org.postgresql" % "postgresql" % postgresVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.1"
)

val logging = Seq(
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion
)

libraryDependencies ++= flinkDependencies ++ flinkConnectors ++ logging
