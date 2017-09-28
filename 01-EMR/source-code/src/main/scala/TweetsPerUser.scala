import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TweetsPerUser {
  val USER_NAME_INDEX = 3

  def main(args: Array[String]): Unit = {
    val sourceFiles = args(0)
    val outFile = args(1)

    val sc = new SparkContext(new SparkConf().setAppName("User Count"))

    sc
      .textFile(sourceFiles)
      .map(line => {
        val tweetProperties = line.split('|')
        if (tweetProperties.length > USER_NAME_INDEX)
          Some((tweetProperties(USER_NAME_INDEX),1))
        else
          None
      })
      .flatMap(_.toList) // Turn the Options into either empty or singleton lists
      .reduceByKey(_+_)
      .map(x => x._1 + "," + x._2.toString)
      .saveAsTextFile(outFile)
  }
}
