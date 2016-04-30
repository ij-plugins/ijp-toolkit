import java.net.URL

name         := "ij-plugins_toolkit"
organization := "net.sf.ij-plugins"
version      := "1.10.0-SNAPSHOT" // + svnRevision.value.revision

homepage     := Some(new URL("https://ij-plugins.sf.net"))
startYear    := Some(2002)
licenses     := Seq(("LGPL-2.1", new URL("http://opensource.org/licenses/LGPL-2.1")))
description  := "<html>" +
    "IJ Plugins Toolkit is a set of ImageJ plugins grouped into:" +
    "<ul>" +
    "<li>3D IO - import and export of data in 3D formats.</li>" +
    "<li>3D Toolkit - operations on stacks interpreted as 3D images, including morphological operations.</li>" +
    "<li>Color - color space conversion, color edge detection (color and multi-band images).</li>" +
    "<li>Filters - fast median filters and various anisotropic diffusion filters.</li>" +
    "<li>Graphics - Texture Synthesis - A plugin to perform texture synthesis using the image quilting algorithm of Efros and Freeman.</li>" +
    "<li>Segmentation - image segmentation through clustering, thresholding, and region growing.</li>" +
    "</ul>" +
    "</html>"

libraryDependencies ++= Seq(
  "com.jgoodies"      % "jgoodies-binding"  % "2.13.0",
  "net.imagej"        % "ij"                % "1.49v",
  // Test
  "junit"             % "junit"             % "4.12" % "test",
  // JUnit runner SBT plugin
  "com.novocode"      % "junit-interface"   % "0.11" % "test->default"
)

// Add example directories to test compilation
unmanagedSourceDirectories in Test += baseDirectory.value / "example/src"

// fork a new JVM for 'run' and 'test:run'
fork := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions ++= Seq("-Xmx2G", "-server")
javacOptions in(Compile, compile) ++= Seq("-deprecation", "-Xlint:all", "-source",  "1.7", "-target",  "1.7")

// Set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> "}

//
// Setup sbt-imagej plugin
//
enablePlugins(SbtImageJ)
ijRuntimeSubDir := "sandbox"
ijPluginsSubDir := "ij-plugins"
ijCleanBeforePrepareRun := true
cleanFiles += ijPluginsDir.value

baseDirectory in run := baseDirectory.value / "sandbox"

//
// Customize Java style publishing
//
// Enables publishing to maven repo
publishMavenStyle := true
// This is a Java project, disable using the Scala version in output paths and artifacts
crossPaths        := false
// This forbids including Scala related libraries into the dependency
autoScalaLibrary  := false
pomExtra :=
    <scm>
      <url>https://github.com/ij-plugins/ijp-toolkit</url>
      <connection>scm:https://github.com/ij-plugins/ijp-toolkit.git</connection>
    </scm>
    <developers>
      <developer>
        <id>jpsacha</id>
        <name>Jarek Sacha</name>
      </developer>
    </developers>