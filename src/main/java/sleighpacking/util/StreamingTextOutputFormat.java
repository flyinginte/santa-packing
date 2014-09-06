package sleighpacking.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;


public class StreamingTextOutputFormat<K, V> extends TextOutputFormat<K, V> {
    protected static class StreamingLineRecordWriter<K, V> extends RecordWriter<K, V>{
        private static final String utf8 = "UTF-8";
        private static final byte[] newline;

        static {
            try {
                newline = "\n".getBytes(utf8);
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalArgumentException("can't find " + utf8
                        + " encoding");
            }
        }

        protected DataOutputStream out;
        private final byte[] keyValueSeparator;
        private final byte[] valueDelimiter;
        private boolean dataWritten = false;

        public StreamingLineRecordWriter(DataOutputStream out,
                                         String keyValueSeparator, String valueDelimiter) {
            this.out = out;
            try {
                this.keyValueSeparator = keyValueSeparator.getBytes(utf8);
                this.valueDelimiter = valueDelimiter.getBytes(utf8);
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalArgumentException("can't find " + utf8
                        + " encoding");
            }
        }

        public StreamingLineRecordWriter(DataOutputStream out) {
            this(out, "\t", ",");
        }

        /**
         * Write the object to the byte stream, handling Text as a special case.
         *
         * @param o
         *            the object to print
         * @throws IOException
         *             if the write throws, we pass it on
         */
        private void writeObject(Object o) throws IOException {
            if (o instanceof Text) {
                Text to = (Text) o;
                out.write(to.getBytes(), 0, to.getLength());
            } else {
                out.write(o.toString().getBytes(utf8));
            }
        }

        public synchronized void write(K key, V value) throws IOException {

            // write out the value
            writeObject(value);

            // track that we've written some data
            dataWritten = true;
        }

        public synchronized void close(TaskAttemptContext reporter) throws IOException {
            // if we've written out any data, append a closing newline
            if (dataWritten) {
                out.write(newline);
            }

            out.close();
        }
    }

    @Override
    public RecordWriter<K, V> getRecordWriter(TaskAttemptContext job) throws IOException {
        Configuration conf = job.getConfiguration();
        boolean isCompressed = getCompressOutput(job);

        CompressionCodec codec = null;
        String extension = "";
        if (isCompressed) {
            Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(
                    job, GzipCodec.class);
            codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass,
                    conf);
            extension = codec.getDefaultExtension();
        }
        Path file = getDefaultWorkFile(job, extension);
        FileSystem fs = file.getFileSystem(conf);
        if (!isCompressed) {
            FSDataOutputStream fileOut = fs.create(file, false);
            return new StreamingLineRecordWriter<K, V>(new DataOutputStream(fileOut));
        } else {
            FSDataOutputStream fileOut = fs.create(file, false);
            return new StreamingLineRecordWriter<K, V>(new DataOutputStream(
                    codec.createOutputStream(fileOut)));
        }

    }
}