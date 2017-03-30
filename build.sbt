name := "togglplot"

version := "1.8"

scalaVersion := "2.11.8"

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.92-R10"

//resolvers += "Comerge Public Releases" at "http://pandora.comerge.net:8081/nexus/content/repositories/public_snapshots"

// Add dependency on JToggl library
//libraryDependencies += "ch.simas.jtoggl" % "jtoggl-api" % "1.0.3-SNAPSHOT"

// add jersey library, since jtoggl only has "jersey-client" as a dependency, but this doesn't contain jersey.api.core
libraryDependencies += "com.sun.jersey" % "jersey-core" % "1.18.1"

libraryDependencies += "com.sun.jersey" % "jersey-client" % "1.18.1"

libraryDependencies += "com.googlecode.json-simple" % "json-simple" % "1.1.1"

libraryDependencies += "junit" % "junit" % "4.10"

// Add dependency on JavaFX library based on JAVA_HOME variable
//unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/jfxrt.jar"))

unmanagedJars in Compile += Attributed.blank(file("/usr/lib/jvm/default/jre/lib/ext/jfxrt.jar"))

fork := true

javaSource in Compile := baseDirectory.value / "src/main/java"
