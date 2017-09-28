package com.zuhlke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;

import com.zuhlke.models.Streaming.Hashtag;
import com.zuhlke.models.Streaming.HashtagCount;
import com.zuhlke.models.Streaming.StreamedTweet;
import com.zuhlke.utils.PerWindowFiles;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PopularHashtags {

    static class ExtractHashtagsFromMsg extends DoFn<PubsubMessage, String> {

        private static final Logger LOG = LoggerFactory.getLogger(ExtractHashtagsFromMsg.class);
        private ObjectMapper mapper;

        public ExtractHashtagsFromMsg() {
            mapper = new ObjectMapper();
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            String payload;
            try {
                payload = new String(c.element().getPayload(), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                LOG.error("Error extracting payload", e);
                return;
            }

            StreamedTweet tweet;
            try {
                tweet = mapper.readValue(payload, StreamedTweet.class);
            }
            catch (IOException e) {
                LOG.error("Error extracting payload", e);
                return;
            }

            // Avoid job spam
            if (tweet.getUsr().startsWith("tmj_")) return;

            for (Hashtag ht: tweet.getHashtags()) {
                c.output(ht.getText());
            }
        }
    }

    static class FlattenHashtagCounts extends DoFn<List<KV<String, Long>>, HashtagCount> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            Instant timestamp = c.timestamp();
            for (KV<String, Long> item: c.element()) {
                c.output(new HashtagCount(item.getKey(), item.getValue(), timestamp));
            }
        }
    }

    static class HashtagCountToString extends DoFn<HashtagCount, String> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            String timestamp = c.element().getTimestamp().toString(ISODateTimeFormat.dateTimeNoMillis());
            c.output(c.element().getHashtag() + "," + c.element().getCount().toString() + "," + timestamp);
        }
    }

    static class HashtagCountToTableRow extends DoFn<HashtagCount, TableRow> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            String timestamp = c.element().getTimestamp().toString(ISODateTimeFormat.dateTimeNoMillis());

            TableRow row = new TableRow()
                    .set("hashtag", c.element().getHashtag())
                    .set("count", c.element().getCount())
                    .set("timestamp", c.element().getTimestamp().toString());
            c.output(row);
        }

        static TableSchema getSchema() {
            List<TableFieldSchema> fields = new ArrayList<>();
            fields.add(new TableFieldSchema().setName("hashtag").setType("STRING"));
            fields.add(new TableFieldSchema().setName("count").setType("INTEGER"));
            fields.add(new TableFieldSchema().setName("timestamp").setType("TIMESTAMP"));
            TableSchema schema = new TableSchema().setFields(fields);
            return schema;
        }
    }

    static class HashtagComparator implements Comparator<KV<String, Long>>, Serializable {
        @Override
        public int compare(KV<String, Long> o1, KV<String, Long> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }

    public interface PopularHashtagsOptions extends DataflowPipelineOptions {

        @Description("Path of the file to write to")
        String getFileOutput();
        void setFileOutput(String fileOutput);

        @Description("The PubSub topic to read the tweets from")
        @Default.String("tweets")
        String getTopic();
        void setTopic(String topic);

        @Description("The sliding window duration")
        @Default.Integer(1)
        Integer getWindowDuration();
        void setWindowDuration(Integer windowDuration);

        @Description("How often the sliding window moves")
        @Default.Integer(1)
        Integer getWindowSlideEvery();
        void setWindowSlideEvery(Integer windowSlideEvery);

        @Description("How many top hashtags to calculate")
        @Default.Integer(10)
        Integer getTopItemsCount();
        void setTopItemsCount(Integer topItemsCount);

        @Description("True if the output if to go to BigQuery, False if to files")
        @Default.Boolean(true)
        Boolean getOutputToBigQuery();
        void setOutputToBigQuery(Boolean outputToBigQuery);

        @Default.String("dataflow_demo")
        String getBigQueryDataset();
        void setBigQueryDataset(String bigQueryDataset);

        @Default.String("popular_hashtags")
        String getBigQueryTable();
        void setBigQueryTable(String bigQueryTable);
    }

    public static void main(String[] args) throws IOException {
        PopularHashtagsOptions options = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .as(PopularHashtagsOptions.class);

        Pipeline pipeline = Pipeline.create(options);

        String topicName = "projects/" + options.getProject() + "/topics/" + options.getTopic();

        PCollection<HashtagCount> counts = pipeline
                .apply(PubsubIO.readMessages().fromTopic(topicName))
                .apply("Extract hashtags", ParDo.of(new ExtractHashtagsFromMsg()))
                .apply("Apply window", Window.into(SlidingWindows.of(
                        Duration.standardMinutes(options.getWindowDuration())).
                        every(Duration.standardMinutes(options.getWindowSlideEvery()))))
                .apply("Count per hashtag", Count.perElement())
                .apply("Top N hashtags", Top.of(options.getTopItemsCount(), new HashtagComparator()).withoutDefaults())
                .apply(ParDo.of(new FlattenHashtagCounts()));

        if (options.getOutputToBigQuery()) {

            TableReference tableRef = new TableReference();
            tableRef.setProjectId(options.getProject());
            tableRef.setDatasetId(options.getBigQueryDataset());
            tableRef.setTableId(options.getBigQueryTable());

            counts
                    .apply(ParDo.of(new HashtagCountToTableRow()))
                    .apply("Write Counts", BigQueryIO
                                    .writeTableRows()
                                    .to(tableRef)
                                    .withSchema(HashtagCountToTableRow.getSchema())
                                    .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                                    .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
        } else {
            counts
                    .apply(ParDo.of(new HashtagCountToString()))
                    .apply("Write Counts", TextIO
                            .write()
                            .to(options.getFileOutput())
                            .withFilenamePolicy(new PerWindowFiles("part"))
                            .withWindowedWrites()
                            .withNumShards(1));
        }


        // Run the pipeline.
        PipelineResult result = pipeline.run();
    }
}
