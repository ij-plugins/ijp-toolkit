import java.net.URL

// @formatter:off

name         := "ijp-toolkit"
organization := "net.sf.ij-plugins"
version      := "2.3.1.1-SNAPSHOT"

homepage     := Some(new URL("https://github.com/ij-plugins/ijp-toolkit"))
startYear    := Some(2002)
licenses     := Seq(("LGPL-2.1", new URL("http://opensource.org/licenses/LGPL-2.1")))
description  := "<html>" +
    "IJ Plugins Toolkit is a set of ImageJ plugins grouped into:" +
    "<ul>" +
    "  <li>3D IO - import and export of data in 3D formats.</li>" +
    "  <li>3D Toolkit - operations on stacks interpreted as 3D images, including morphological operations.</li>" +
    "  <li>Color - color space conversion, color edge detection (color and multi-band images).</li>" +
    "  <li>Filters - fast median filters and various anisotropic diffusion filters.</li>" +
    "  <li>Graphics - Texture Synthesis - A plugin to perform texture synthesis using the image quilting algorithm of " +
  "        Efros and Freeman.</li>" +
    "  <li>Segmentation - image segmentation through clustering, thresholding, and region growing.</li>" +
    "</ul>" +
    "</html>"

crossScalaVersions := Seq("2.13.8", "3.0.2", "2.12.15")
scalaVersion       := crossScalaVersions.value.head

def isScala2_13plus(scalaVersion: String): Boolean = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((major, minor)) if major > 2 || (major == 2 && minor >= 13) => true
    case _ => false
  }
}

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-math3"    % "3.6.1",
  "com.jgoodies"       % "jgoodies-binding" % "2.13.0",
  "net.imagej"         % "ij"               % "1.53j",
  // Test
  "junit"              % "junit"            % "4.13.2" % "test",
  "org.scalatest"     %% "scalatest"        % "3.2.9"  % "test",
  // JUnit runner SBT plugin
  "com.novocode"       % "junit-interface"  % "0.11"   % "test->default"
)

libraryDependencies ++= (
    if (isScala2_13plus(scalaVersion.value)) {
      Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3")
    } else {
      Seq.empty[ModuleID]
    }
  )

// Add example directories to test compilation
Test / unmanagedSourceDirectories += baseDirectory.value / "examples/scala"
Test / unmanagedSourceDirectories += baseDirectory.value / "examples/java"

scalacOptions ++= {
  Seq(
    "-encoding", "UTF-8",
    "-feature",
    "-language:implicitConversions",
    // disabled during the migration
    // "-Xfatal-warnings"
  ) ++
    (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) => Seq(
        "-unchecked"
      )
      case _ => Seq(
        "-deprecation",
//        "-Xfatal-warnings",
//        "-Wunused:imports,privates,locals",
//        "-Wvalue-discard"
      )
    })
}

// Options for ScalaDoc
Compile / doc / scalacOptions ++= Seq(
  "-doc-title",        "IJ-Plugins Toolkit",
  "-doc-version",      version.value,
  "-doc-root-content", baseDirectory.value + "/src/main/scala/overview.txt",
  "-doc-footer",       s"IJ-Plugins Toolkit API v.${version.value}"
)


// fork a new JVM for 'run' and 'test:run'
fork := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions ++= Seq("-Xmx2G", "-server")
Compile / compile / javacOptions ++= Seq(
//  "-deprecation", "-Xlint:all",
  "--release",  "8")

//
// Setup sbt-imagej plugin
//
enablePlugins(SbtImageJ)
ijRuntimeSubDir         := "sandbox"
ijPluginsSubDir         := "ij-plugins"
ijCleanBeforePrepareRun := true
cleanFiles              += ijPluginsDir.value

run / baseDirectory := baseDirectory.value / "sandbox"

//
// Customize Java style publishing
//
// Enables publishing to maven repo
publishMavenStyle := true
publishTo := sonatypePublishToBundle.value
import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("ij-plugins", "ijp-toolkit", "jpsacha@gmail.com"))
developers := List(
  Developer(id="jpsacha", name="Jarek Sacha", email="jpsacha@gmail.com", url=url("https://github.com/jpsacha"))
)