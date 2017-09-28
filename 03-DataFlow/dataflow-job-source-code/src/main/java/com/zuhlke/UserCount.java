package com.zuhlke;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

public class UserCount {

    /** Extract the user name from a single line in a tweets file */
    static class ExtractUsersFn extends DoFn<String, String> {

        @ProcessElement
        public void processElement(ProcessContext c) {
            // Split the line into individual parts.
            String[] parts = c.element().split("\\|");

            if (parts.length > 3) {
                c.output(parts[3]); // User is at index 3
            }
        }
    }

    /** A SimpleFunction that converts a User and Count into a printable string. */
    public static class FormatAsTextFn extends SimpleFunction<KV<String, Long>, String> {
        @Override
        public String apply(KV<String, Long> input) {
            return input.getKey() + "," + input.getValue();
        }
    }


    public static class CountTweetsByUser extends PTransform<PCollection<String>, PCollection<KV<String, Long>>> {
        @Override
        public PCollection<KV<String, Long>> expand(PCollection<String> lines) {

            // Convert each line of text into a users.
            PCollection<String> users = lines.apply(
                    ParDo.of(new UserCount.ExtractUsersFn()));

            // Count the number of times each word occurs.
            PCollection<KV<String, Long>> userCounts =
                    users.apply(Count.<String>perElement());

            return userCounts;
        }
    }

    /** Only include users who have tweeted at least twice */
    static class FilterUsersFn implements SerializableFunction<KV<String, Long>, Boolean> {
        private final Long minTweets;

        public FilterUsersFn(Long minTweets) {
           this.minTweets = minTweets;
        }

        @Override
        public Boolean apply(KV<String, Long> userAndCount) {
            return userAndCount.getValue() >= minTweets;
        }
    }


    public interface UserCountOptions extends PipelineOptions {

        /**
         * Set this required option to specify where to read the data from
         */
        @Description("Path of the file(s) to read from")
        @Default.String("gs://intalert-backup/tweets_partial/")
        String getInputFiles();
        void setInputFiles(String value);

        /**
         * Set this required option to specify where to write the output.
         */
        @Description("Path of the file to write to")
        @Required
        String getOutput();
        void setOutput(String value);

        @Description("Minimum number of tweets for user to be included in results")
        @Default.Long(2)
        Long getMinTweets();
        void setMinTweets(Long value);
    }

    public static void main(String[] args) {
        UserCount.UserCountOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                .as(UserCount.UserCountOptions.class);

        Pipeline p = Pipeline.create(options);

        // Our pipeline applies the composite CountUsers transform, and passes the
        // static FormatAsTextFn() to the ParDo transform.
        p.apply("Read Lines", TextIO.read().from(options.getInputFiles()))
                .apply("Count users", new CountTweetsByUser())
                .apply("Filter users", Filter.by(new FilterUsersFn(options.getMinTweets())))
                .apply("Format results", MapElements.<KV<String, Long>, String>via(new FormatAsTextFn()))
                .apply("Write Counts", TextIO.write().to(options.getOutput()));

        p.run().waitUntilFinish();
    }
}
