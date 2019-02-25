import com.typesafe.sbt.packager.SettingsHelper

val root = (project in file("."))
  // Enables packaging of Java application and publishing of package
  .enablePlugins(JavaAppPackaging, UniversalDeployPlugin)
  .settings(
    organization := "com.axa.hw",
    name := "business-view-hw",
    version := "0.1.1-SNAPSHOT",

    scalaVersion := "2.11.11",

    // Provided dependencies will be by 'spark-submit'
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "2.1.0" % Provided,
      "org.apache.spark" %% "spark-hive" % "2.1.0" % Provided,
      "com.databricks" %% "spark-xml" % "0.4.0" % Provided,
      "com.holdenkarau" %% "spark-testing-base" % "2.1.0_0.6.0" % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "com.databricks" %% "spark-avro" % "3.2.0" % Test
    ),

    // Resolvers used to retrieve when running on developer machine (will be overridden when running in Jenkins)
    resolvers ++= Seq(
      "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
      "Releases" at "http://hdp-master.com:8081/content/repositories/releases/",
      "Snapshots" at "http://hdp-master.com:8081/content/repositories/apache-snapshots/"
    ),

    // ----------------------------------------------------------------------------------
    // --- Publish artifacts
    // ----------------------------------------------------------------------------------

    // URL of the repository to which artifacts are published
    publishTo := {
      // URL used when running job in Jenkins
      val nexusRepositoriesUrl = "http://hdp-master.com:8081/content/repositories"

      if (isSnapshot.value)
        Some("Snapshots" at s"$nexusRepositoriesUrl/apache-snapshots")
      else
        Some("Releases" at s"$nexusRepositoriesUrl/releases")
    },

    // Credential available when running job in Jenkins
   // credentials += Credentials(Path.userHome / ".ivy2" / ".credentials-new-nexus"),
//    credentials += Credentials("/root/zk/jk.credentials-new-nexus"),
    credentials += Credentials("Sonatype Nexus Repository Manager", "hdp-master.com", "admin", "admin123"),

   
    // Specifies JVM memory options (will be used when forking)
    javaOptions ++= Seq("-Xms512M","-Xmx6144M", "-Xss100M","-XX:MaxPermSize=2048M",  "-XX:+CMSClassUnloadingEnabled"),
  
    
    // --------------------------------------------------------------------------------
    // --- Run
    // --------------------------------------------------------------------------------

    // Forks JVM when running
    fork in run := true,

    // Re-enables inclusion of provided libraries when executing 'run' and 'runMain' tasks
    run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated,
    runMain in Compile := Defaults.runMainTask(fullClasspath in Compile, runner in (Compile, run)).evaluated,

    // --------------------------------------------------------------------------------
    // --- Test
    // --------------------------------------------------------------------------------

    // Forks JVM when launching tests
    fork in Test := true,
    // Disables parallel execution of tests
    parallelExecution in Test := false,

    // --------------------------------------------------------------------------------
    // --- Package fat JAR
    // --------------------------------------------------------------------------------

    assemblyJarName in assembly := s"${name.value}-assembly.jar",
    // Disables inclusion of Scala standard libraries in assembly (will be provided by 'spark-submit')
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),

    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    },

    // Disables tests when building assembly
    test in assembly := {},

    // --------------------------------------------------------------------------------
    // --- Package deployment ZIP
    // --------------------------------------------------------------------------------

    // Creates ZIP when launching 'universal:publish'
    SettingsHelper.makeDeploymentSettings(Universal, packageBin in Universal, "zip"),

   // topLevelDirectory in Universal := Some("Oozie-Business-View-Gulf"),

    // Removes all JARs and appends the fat JAR only
    mappings in Universal := {
      val defaultMappings = (mappings in Universal).value
      val fatJar = (assembly in Compile).value
      val filteredMappings = defaultMappings.filter({ case (file, name) =>  ! name.endsWith(".jar") })
      filteredMappings :+ (fatJar -> s"bin/lib/${fatJar.getName}")
    },

    // Includes only fat JAR in script classpath
    scriptClasspath := Seq((assemblyJarName in assembly).value),
    // Disables Bash script generation
    makeBashScripts := { Nil },
    // Disables Bat script generation
    makeBatScripts := { Nil }
  )
