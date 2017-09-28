name := "SparkTest"

version := "1.0"

scalaVersion := "2.11.8"

// https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.11
libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.1.1" % "provided"

assemblyJarName in assembly := "SparkTest-Assm.jar"
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)