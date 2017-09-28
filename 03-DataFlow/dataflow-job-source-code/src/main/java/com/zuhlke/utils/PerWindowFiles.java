package com.zuhlke.utils;

import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.io.FileBasedSink;
import org.apache.beam.sdk.io.fs.ResolveOptions;
import org.apache.beam.sdk.io.fs.ResourceId;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class PerWindowFiles extends FileBasedSink.FilenamePolicy {
    private static final DateTimeFormatter FORMATTER = ISODateTimeFormat.hourMinuteSecond();

    private final String prefix;

    public PerWindowFiles(String prefix) {
        this.prefix = prefix;
    }

    private String filenamePrefixForWindow(IntervalWindow window) {
        return String.format("%s-%s-%s",
                prefix, FORMATTER.print(window.start()), FORMATTER.print(window.end()));
    }

    @Override
    public ResourceId windowedFilename(
            ResourceId outputDirectory,
            WindowedContext context,
            String extension) {
        IntervalWindow window = (IntervalWindow) context.getWindow();
        String filename = String.format(
                "%s-%s-of-%s%s",
                filenamePrefixForWindow(window), context.getShardNumber(), context.getNumShards(), extension);

        return outputDirectory.resolve(filename, ResolveOptions.StandardResolveOptions.RESOLVE_FILE);
    }

    @Nullable
    @Override
    public ResourceId unwindowedFilename(ResourceId resourceId, Context context, String s) {
        throw new UnsupportedOperationException("Unsupported.");
    }
}
