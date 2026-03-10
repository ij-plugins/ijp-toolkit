name             := "ijp-toolkit"
organization     := "net.sf.ij-plugins"
organizationName := "IJ-Plugins"
version          := "2.3.2.1-SNAPSHOT"

homepage    := Some(url("https://github.com/ij-plugins/ijp-toolkit"))
startYear   := Some(2002)
licenses    := Seq("LGPL-2.1" -> url("http://opensource.org/licenses/LGPL-2.1"))
description := "<html>" +
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
organizationHomepage := Some(url("https://github.com/ij-plugins"))
ThisBuild / scmInfo  := Option(
  ScmInfo(
    url("https://github.com/ij-plugins/ijp-toolkit"),
    "scm:git@github.com:ij-plugins/ijp-toolkit.git"
  )
)

scalaVersion := "3.3.7"

libraryDependencies ++= Seq(
  "org.apache.commons"      % "commons-math3"              % "3.6.1",
  "com.jgoodies"            % "jgoodies-binding"           % "2.13.0",
  "net.imagej"              % "ij"                         % "1.54p",
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
  // Test
  "junit"          % "junit"     % "4.13.2" % "test",
  "org.scalatest" %% "scalatest" % "3.2.19" % "test",
  // JUnit runner SBT plugin
  "com.novocode" % "junit-interface" % "0.11" % "test->default"
)

// Add example directories to test compilation
Test / unmanagedSourceDirectories += baseDirectory.value / "examples/scala"
Test / unmanagedSourceDirectories += baseDirectory.value / "examples/java"

// Set the Java version target for compatibility for the current FIJI distribution
// We do not want to be over the FIJI Java version.
lazy val javaTargetVersion = "21"

scalacOptions ++= Seq(
  "-java-output-version",
  javaTargetVersion, // Target Java 21
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-explain",
  "-explain-types",
  "-rewrite",
  "-source:3.3-migration",
  "-Wunused:all"
)

// Options for ScalaDoc
Compile / doc / scalacOptions ++= Seq(
  "-doc-title",
  "IJ-Plugins Toolkit",
  "-doc-version",
  version.value,
  "-doc-root-content",
  baseDirectory.value + "/src/main/scala/overview.txt",
  "-doc-footer",
  s"IJ-Plugins Toolkit API v.${version.value}"
)

// fork a new JVM for 'run' and 'test:run'
fork := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions ++= Seq("-Xmx2G", "-server")
Compile / compile / javacOptions ++= Seq(
//  "-deprecation", "-Xlint:all",
  "--release",
  javaTargetVersion
)

//
// Setup sbt-imagej plugin
//
enablePlugins(SbtImageJ)
ijRuntimeSubDir         := "sandbox"
ijPluginsSubDir         := "ij-plugins"
ijCleanBeforePrepareRun := true
cleanFiles += ijPluginsDir.value

run / baseDirectory := baseDirectory.value / "sandbox"

//
// Customize Java style publishing
//
// Enables publishing to maven repo
publishMavenStyle      := true
Test / publishArtifact := false
ThisBuild / publishTo  := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}

developers := List(
  Developer(id = "jpsacha", name = "Jarek Sacha", email = "jpsacha@gmail.com", url = url("https://github.com/jpsacha"))
)
