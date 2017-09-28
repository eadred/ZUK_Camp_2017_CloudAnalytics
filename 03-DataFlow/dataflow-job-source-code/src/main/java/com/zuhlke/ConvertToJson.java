package com.zuhlke;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuhlke.models.Tweet;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertToJson {

    static class ConvertToJsonFn extends DoFn<String, String> {

        private static final Logger LOG = LoggerFactory.getLogger(ConvertToJsonFn.class);

        private ObjectMapper mapper;

        public ConvertToJsonFn() {
            mapper = new ObjectMapper();
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            // Split the line into individual parts.
            String[] parts = c.element().split("\\|");

            Tweet tweet;
            try {
                tweet = Tweet.fromArray(parts);
            }
            catch (Exception ex) {
                LOG.error("Error converting text to tweet for row '" + c.element() + "'", ex);
                return;
            }

            try {
                c.output(mapper.writeValueAsString(tweet));
            }
            catch (JsonProcessingException ex) {
                LOG.error("Error converting tweet to JSON for row '" + c.element() + "'", ex);
            }
        }
    }

    public interface ConvertToJsonOptions extends PipelineOptions {

        @Description("Path of the file(s) to read from")
        @Default.String("gs://intalert-backup/tweets_partial/")
        String getInputFiles();
        void setInputFiles(String value);

        @Description("Path of the file to write to")
        @Required
        String getOutput();
        void setOutput(String value);
    }

    public static void main(String[] args) {
        ConvertToJson.ConvertToJsonOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                .as(ConvertToJson.ConvertToJsonOptions.class);

        Pipeline p = Pipeline.create(options);

        p.apply("Read Lines", TextIO.read().from(options.getInputFiles()))
                .apply("To JSON", ParDo.of(new ConvertToJsonFn()))
                .apply("Write Results", TextIO.write().to(options.getOutput()));

        p.run().waitUntilFinish();
    }
}
