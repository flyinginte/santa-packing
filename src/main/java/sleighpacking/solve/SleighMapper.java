package sleighpacking.solve;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import sleighpacking.model.Present;
import sleighpacking.model.Sleigh;
import sleighpacking.util.FileIO;

import java.io.IOException;

public class SleighMapper extends Mapper<Text, Text, IntWritable, Sleigh>{

    public static boolean packSmart = false;
    private static boolean useDDS = false;
    private static int maxIterations = 5;

    public void map(Text key, Text value, Context context){

        System.out.println(key.toString());

        //get presents from text
        Present[] presents = FileIO.getPresentsFromValue(value);
        int offset = presents[0].pId-1;
        Sleigh sleigh;

        if (useDDS) {
            DDS dds = new DDS(presents, maxIterations, offset, packSmart, context);
            sleigh = dds.call();
        }
        else {
            Solver solver = new Solver(presents, offset, packSmart, context);
            sleigh = solver.call();
        }

        try {
            context.write(new IntWritable(1), sleigh);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
