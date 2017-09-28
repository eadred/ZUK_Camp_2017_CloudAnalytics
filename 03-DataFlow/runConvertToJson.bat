REM Change these as appropriate
SET project=myproject-123456
SET output=gs://my-bucket/tweets-data-json/part
SET input=gs://my-bucket/tweets-data/*
SET staging=gs://my-bucket/staging/

mvn exec:java -Dexec.mainClass=com.zuhlke.ConvertToJson -Dexec.args="--project=%project% --maxNumWorkers=8 --stagingLocation=%staging% --output=%output% --inputFiles=%input% --zone=europe-west1-b --runner=DataflowRunner"