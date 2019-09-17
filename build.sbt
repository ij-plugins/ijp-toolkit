import java.net.URL

// @formatter:off

name         := "ijp-toolkit"
organization := "net.sf.ij-plugins"
version      := "2.1.2-SNAPSHOT"

homepage     := Some(new URL("https://ij-plugins.sf.net"))
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

scalaVersion       := "2.13.0"
crossScalaVersions := Seq("2.11.12", "2.10.7", "2.12.9", "2.13.0")

def isScala2_13plus(scalaVersion: String): Boolean = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, n)) if n >= 13 => true
    case _ => false
  }
}

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-math3"    % "3.6.1",
  "com.jgoodies"       % "jgoodies-binding" % "2.13.0",
  "net.imagej"         % "ij"               % "1.52k",
  // Test
  "junit"              % "junit"            % "4.12"   % "test",
  "org.scalatest"     %% "scalatest"        % "3.0.8"  % "test",
  // JUnit runner SBT plugin
  "com.novocode"       % "junit-interface"  % "0.11"   % "test->default"
)

libraryDependencies ++= (
    if (isScala2_13plus(scalaVersion.value)) {
      Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0")
    } else {
      Seq.empty[ModuleID]
    }
  )

// Add example directories to test compilation
unmanagedSourceDirectories in Test += baseDirectory.value / "example/src"

// fork a new JVM for 'run' and 'test:run'
fork := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions ++= Seq("-Xmx2G", "-server")
javacOptions in(Compile, compile) ++= Seq("-deprecation", "-Xlint:all", "-source",  "1.8", "-target",  "1.8")

// Set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> "}

//
// Setup sbt-imagej plugin
//
enablePlugins(SbtImageJ)
ijRuntimeSubDir         := "sandbox"
ijPluginsSubDir         := "ij-plugins"
ijCleanBeforePrepareRun := true
cleanFiles              += ijPluginsDir.value

baseDirectory in run := baseDirectory.value / "sandbox"

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