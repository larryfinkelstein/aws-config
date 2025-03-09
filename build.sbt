ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

organization      := "com.larryfinkelstein"
name              := "aws-config"
version           := "0.1.0"
description       := "A Scala library for fetching AWS parameters and secrets."
scalaVersion      := "2.13.10"
crossScalaVersions:= Seq("2.12.17", "2.13.10")

// Repository, SCM, and licensing info for publishing
homepage          := Some(url("https://github.com/mycompany/aws-parameter-library"))
licenses          += ("MIT", url("https://opensource.org/licenses/MIT"))
scmInfo           := Some(
  ScmInfo(
    url("https://github.com/mycompany/aws-parameter-library"),
    "scm:git@github.com:mycompany/aws-parameter-library.git"
  )
)
developers += Developer(
  id    = "my-company",
  name  = "My Company Team",
  email = "contact@mycompany.com",
  url   = url("https://github.com/mycompany")
)

lazy val root = (project in file("."))
  .settings(
    name := "aws-config"
  )

// Library dependencies, marking test libraries with % Test to avoid publishing them
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "2.0.16",
  "com.typesafe" % "config" % "1.4.3",
  // AWS SDK dependencies
  "software.amazon.awssdk" % "ec2" % "2.26.18",               // AWS EC2 SDK
  "software.amazon.awssdk" % "secretsmanager" % "2.25.31",    // AWS Secrets Manager SDK
  "software.amazon.awssdk" % "ssm" % "2.28.21",               // AWS Parameter Store SDK

  // Testing dependencies
  "org.mockito" %% "mockito-scala" % "1.17.37" % Test,
  "org.mockito" % "mockito-inline" % "5.2.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test
)

// Publishing settings
publishMavenStyle := true
// Prevent publishing test artifacts
publishArtifact in Test := false
// Don’t include repository information in your published POM:
pomIncludeRepository := { _ => false }

// Optionally specify snapshot vs. release repositories (replace with your repository URLs)
publishTo := {
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
  else
    Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}

// Additional scalac options for stricter compiler checks
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8")