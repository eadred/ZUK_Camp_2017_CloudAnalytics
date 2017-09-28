### Example DataFlow jobs

The source code for the DataFlow jobs are in the dataflow-job-source-code folder.

#### Batch job

The pipeline for a batch job to remove duplicate tweets is defined in the _com.zuhlke.RemoveDuplicates_ class.

To run this job the _runRemoveDuplicates.bat_ script should be executed after changing the variables defined in it.

#### Streaming job

A pipeline for running an example streaming job for determining the top 10 hashtags within 1 minute windows is defined in the _com.zuhlke.PopularHashtags_ class.

To run this job do the following:

+ Ensure a PubSub topic exists to receive the collected tweets by running the _createTopic.bat_ script.
+ Ensure a BigQuery table exists to hold the results by running the _create_bq_tables.bat_ script (actually strictly speaking you don't need to do this since the DataFlow job should automatically create this table if it doesn't already exist).
+ Run the Python 3 script at _twitter-collector-source-code/TweetsApp.py_
+ Finally, execute the _runStreamingTweets.bat_ script to start the DataFlow job.

All the scripts above should be modified to update the script variables to appropriate values (eg the BigQuery dataset name to use).
In the case of the Python script these are defined in the _.env_ and _.private.env_ files.
The _.private.env_ file defines Twitter OAuth keys which can be obtained from [dev.twitter.com](https://dev.twitter.com).