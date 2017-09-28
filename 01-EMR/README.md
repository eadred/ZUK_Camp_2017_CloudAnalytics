### Basic Spark job code for running in EMR

When creating the Spark job step for the cluster set these parameters as follows:

*Spark-submit options:*
```
--class TweetsPerUser
```

*Application location:*
```
s3://my-bucket/path/to/jar/SparkTest-Assm.jar
```

*Arguments:*
```
s3://my-bucket/path/to/tweets/data/* s3://my-bucket/path/to/output
```