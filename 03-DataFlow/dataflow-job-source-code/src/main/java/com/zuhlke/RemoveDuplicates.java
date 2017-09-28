package com.zuhlke;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;

import java.util.Iterator;

public class RemoveDuplicates {
    static class KeyByTweetIdFn extends DoFn<String, KV<String, String>> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            // Split the line into individual parts.
            String[] parts = c.element().split("\\|");

            // If there is a tweet ID then this is the key
            // If not use the HBase key (meaning if there are true duplicate tweets
            // without a tweet ID they won't get removed)
            String key = !parts[1].equals("") ? parts[1] : parts[0];

            c.output(KV.of(key, c.element()));
        }
    }

    static class TakeFirstDuplicateFn extends DoFn<KV<String, Iterable<String>>, String> {

        @ProcessElement
        public void processElement(ProcessContext c) {

            // All the duplicates for this key
            Iterator<String> duplicates = c.element().getValue().iterator();

            // Shouldn't really have to check hasNext
            if (duplicates.hasNext()) {
                c.output(duplicates.next());
            }
        }
    }

    public interface RemoveDuplicatesOptions extends PipelineOptions {
        @Description("Path of the file(s) to read from")
        @Default.String("gs://intalert-backup/tweets_partial/raw/")
        String getInputFiles();
        void setInputFiles(String value);

        @Description("Path of the file to write to")
        @Validation.Required
        String getOutput();
        void setOutput(String value);
    }

    public static void main(String[] args) {
        RemoveDuplicates.RemoveDuplicatesOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                .as(RemoveDuplicates.RemoveDuplicatesOptions.class);

        Pipeline p = Pipeline.create(options);

        p.apply("Read Lines", TextIO.read().from(options.getInputFiles()))
                .apply("Key tweets by tweet id", ParDo.of(new KeyByTweetIdFn()))
                .apply("Group by tweetid", GroupByKey.create())
                .apply("Taking first duplicate", ParDo.of(new TakeFirstDuplicateFn()))
                .apply("Write Results", TextIO.write().to(options.getOutput()));

        p.run().waitUntilFinish();
    }
}
