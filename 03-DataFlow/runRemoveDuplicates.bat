REM Change these as appropriate
SET project=myproject-123456
SET output=gs://my-bucket/output/dedup/part
SET input=gs://my-bucket/tweets-data/*
SET staging=gs://my-bucket/staging/

mvn -f dataflow-job-source-code/pom.xml exec:java -Dexec.mainClass=com.zuhlke.RemoveDuplicates -Dexec.args="--output=%output% --inputFiles=%input% --maxNumWorkers=8 --stagingLocation=%staging% --runner=DataflowRunner --project=%project% --zone=europe-west1-b"