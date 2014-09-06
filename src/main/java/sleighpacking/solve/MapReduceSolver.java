package sleighpacking.solve;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import sleighpacking.model.SmartSleigh;
import sleighpacking.model.StupidSleigh;
import sleighpacking.util.StreamingTextOutputFormat;

public class MapReduceSolver {

    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();
        conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "$");
        conf.set("mapreduce.tasktracker.map.tasks.maximum", "8");

        Job job = new Job(conf, "santa");

        job.setJarByClass(MapReduceSolver.class);
        job.setMapperClass(SleighMapper.class);
        job.setReducerClass(SleighReducer.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);

        job.setMapOutputKeyClass(IntWritable.class);

        if (SleighMapper.packSmart){
            job.setMapOutputValueClass(SmartSleigh.class);
        }
        else {
            job.setMapOutputValueClass(StupidSleigh.class);
        }

        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setOutputFormatClass(StreamingTextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setNumReduceTasks(1);

        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);
    }

}
