### BigQuery Commands

#### Pre-requisites

The following queries depend on the tweets data being in json format.
In the *../03-DataFlow* directory the *runConvertTojson.bat* script will run a DataFlow job which will convert the csv tweets data into the expected json format.
The environment variables in this script should first be changed to appropriate values.

The *tweets_json_tabledef.json* file should also be modified such that the *sourceUris* property refers to where the json tweets data is located, as should any commands below that refers to the data location.

#### Running an ad-hoc query

The following will run a query without needing to define a dataset / table:
```bash
bq query --external_table_definition=tweets::tweets_json_tabledef.json "SELECT usr,tstamp,content FROM tweets LIMIT 10"
```

#### Define data set and external query

The following creates a dataset and an external table:
```bash
bq mk --data_location=EU bqdemo
bq mk --external_table_definition=tweets_json_tabledef.json bqdemo.tweets_external
```

The table can then be queried, either from the BigQuery web console or the command line:
```bash
bq query "SELECT usr,tstamp,content FROM [bqdemo.tweets_external] LIMIT 10"
```

#### Loading data into BigQuery

To preload data into BigQuery:
```bash
bq load --source_format=NEWLINE_DELIMITED_JSON bqdemo.tweets_loaded "gs://my-bucket/tweets-data-json/*" tweets_json_schema.json
```

And once this is done the following user count query is very performant:
```SQL
  SELECT usr, COUNT(usr) as count
  FROM [bqdemo.tweets_loaded]
  GROUP BY usr
  ORDER BY count DESC
  LIMIT 100;
  ```

#### Other stuff

Example use of query parameters:
```bash
bq query --nouse_legacy_sql --parameter=usr:STRING:NigeriaNewsdesk "SELECT usr,tstamp,content FROM `bqdemo_prep.tweets_loaded` WHERE usr=@usr LIMIT 10"
```

Writing results to another table:
```bash
bq query --destination_schema=tweets_json_out_schema.json --destination_table=bqdemo.myresults "SELECT usr,tstamp,content FROM [bqdemo.tweets_loaded] LIMIT 10"
```

Inserting new data from [newline delimited json](http://ndjson.org):

```bash
bq insert bqdemo.myresults new_tweets.json
```
