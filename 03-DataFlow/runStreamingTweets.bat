REM Change these as appropriate
SET project=myproject-123456
SET dataset=dataflow_demo
SET table=popular_hashtags
SET topic=tweets
SET staging=gs://my-bucket/staging/

mvn -f dataflow-job-source-code/pom.xml exec:java -Dexec.mainClass=com.zuhlke.PopularHashtags -Dexec.args="--topic=%tweets% --windowDuration=1 --windowSlideEvery=1 --topItemsCount=10 --outputToBigQuery=true --bigQueryDataset=%dataset% --bigQueryTable=%table% --maxNumWorkers=2 --stagingLocation=%staging% --runner=DataflowRunner --project=%project% --zone=europe-west1-b"