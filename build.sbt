name         := "ij-plugins_Toolkit"
organization := "net.sf.ij-plugins"
version      := "1.10.0-SNAPSHOT" // + svnRevision.value.revision

javaSource in Compile := baseDirectory.value / "src"
javaSource in Test    := baseDirectory.value / "test" / "src"
resourceDirectory in Compile := baseDirectory.value / "src"
resourceDirectory in Test    := baseDirectory.value / "test" / "src"

libraryDependencies ++= Seq(
//  "net.imagej"   % "ij"              % "1.49v",
  "junit"        % "junit"           % "4.11" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test->default"
)

fork := true

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
// Do not append Scala versions to the generated artifacts
crossPaths        := false
// This forbids including Scala related libraries into the dependency
autoScalaLibrary  := false