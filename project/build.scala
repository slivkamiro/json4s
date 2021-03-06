import sbt._
import Keys._
import xml.Group
import com.typesafe.sbt.SbtStartScript
import MimaSettings.mimaSettings
import com.typesafe.tools.mima.plugin.MimaKeys.mimaPreviousArtifacts
import com.typesafe.sbt.JavaVersionCheckPlugin.autoImport._

object build {
  import Dependencies._

  val manifestSetting = packageOptions += {
    val (title, v, vendor) = (name.value, version.value, organization.value)
      Package.ManifestAttributes(
        "Created-By" -> "Simple Build Tool",
        "Built-By" -> System.getProperty("user.name"),
        "Build-Jdk" -> System.getProperty("java.version"),
        "Specification-Title" -> title,
        "Specification-Version" -> v,
        "Specification-Vendor" -> vendor,
        "Implementation-Title" -> title,
        "Implementation-Version" -> v,
        "Implementation-Vendor-Id" -> vendor,
        "Implementation-Vendor" -> vendor
      )
  }

  val mavenCentralFrouFrou = Seq(
    homepage := Some(new URL("https://github.com/json4s/json4s")),
    startYear := Some(2009),
    licenses := Seq(("Apache-2.0", new URL("http://www.apache.org/licenses/LICENSE-2.0"))),
    pomExtra := {
      pomExtra.value ++ Group(
      <scm>
        <url>http://github.com/json4s/json4s</url>
        <connection>scm:git:git://github.com/json4s/json4s.git</connection>
      </scm>
      <developers>
        <developer>
          <id>casualjim</id>
          <name>Ivan Porto Carrero</name>
          <url>http://flanders.co.nz/</url>
        </developer>
        <developer>
          <id>seratch</id>
          <name>Kazuhiro Sera</name>
          <url>http://git.io/sera</url>
        </developer>
      </developers>
      )
    }
  )

  val json4sSettings = mavenCentralFrouFrou ++ Seq(
    organization := "org.json4s",
    scalaVersion := "2.12.3",
    crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.3", "2.13.0-M2"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:existentials", "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps", "-Xfuture"),
    scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
      case Some((2, 10)) => "-optimize"
    }.toList,
    scalacOptions in (Compile, doc) ++= {
      val base = (baseDirectory in LocalRootProject).value.getAbsolutePath
      val hash = sys.process.Process("git rev-parse HEAD").lines_!.head
      Seq("-sourcepath", base, "-doc-source-url", "https://github.com/json4s/json4s/tree/" + hash + "€{FILE_PATH}.scala")
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 =>
          Seq("-Ywarn-unused-import", "-Ywarn-unused")
        case _ =>
          Nil
      }
    },
    version := "3.6.0-SNAPSHOT",
    javacOptions ++= Seq("-target", "1.6", "-source", "1.6"),
    javaVersionPrefix in javaVersionCheck := Some{
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 11 => "1.7"
        case _ => "1.8"
      }
    },
    parallelExecution in Test := false,
    manifestSetting,
    resolvers ++= Seq(Opts.resolver.sonatypeSnapshots, Opts.resolver.sonatypeReleases),
    crossVersion := CrossVersion.binary
  )

  val noPublish = Seq(
    mimaPreviousArtifacts := Set(),
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
}
