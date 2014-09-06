package sleighpacking.solve;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import sleighpacking.model.PackedPresent;
import sleighpacking.model.Sleigh;
import sleighpacking.model.SmartSleigh;

import java.io.IOException;
import java.util.*;

public class SleighReducer extends Reducer<IntWritable, Sleigh, IntWritable, Text> {

    private final static int numPresents = 1000000;

    @Override
    protected void reduce(IntWritable key, Iterable<Sleigh> sleighs, Context context){

        ArrayList<PackedPresent> packedPresents = new ArrayList<PackedPresent>();
        HashMap<Integer, Integer> orderToHeight = new HashMap<Integer, Integer>();
        HashMap<Integer, List<PackedPresent>> orderToPresents = new HashMap<Integer, List<PackedPresent>>();
        ArrayList<Integer> heights = new ArrayList<Integer>();
        boolean firstRun = true, secondRun = false;
        int chunkSize = 0;

        for (Sleigh s: sleighs) {
            s.shiftZValsUp();
            orderToHeight.put(s.offset, s.maxZ());
            orderToPresents.put(s.offset, Arrays.asList(s.presents));

            if (secondRun){
                chunkSize = s.offset - chunkSize;
                secondRun = false;
            }

            if (firstRun){
                chunkSize = s.offset;
                firstRun = false;
                secondRun = true;
            }

        }

        //sort heights in reverse order (starting with last chunk of presents)

        TreeSet<Integer> heightKeys = new TreeSet<Integer>(orderToHeight.keySet());
        for (Integer k : heightKeys.descendingSet()) {
            System.out.println("offset:" + k + " - " + orderToHeight.get(k));
            heights.add(orderToHeight.get(k));
        }

        //presents in normal order (first index points to first present)

        TreeSet<Integer> presentKeys = new TreeSet<Integer>(orderToPresents.keySet());
        for (Integer k : presentKeys) {
            System.out.println("offset:"+k);
            packedPresents.addAll(orderToPresents.get(k));
        }

        //add heights to chunks
//        Collections.reverse(heights);
        int height_index = 0;
        int currentHeight = 0;

        for (int i = numPresents-1; i >= 0; i--) {
            if (i % chunkSize == chunkSize-1 && i != numPresents-1 && i != 0) {
                currentHeight += heights.get(height_index);
                height_index++;
                context.progress();
            }

            for (int z = 0; z < 8; z++) {
                packedPresents.get(i).z[z] += currentHeight;
            }
        }

        PackedPresent[] packedPresentsArray = packedPresents.toArray(new PackedPresent[packedPresents.size()]);
        Sleigh sleigh = new SmartSleigh(1000, 1000, numPresents, 0);
        sleigh.presents = packedPresentsArray;

        //output result in chunks to avoid out of memory errors
        String result;
        chunkSize = 10000;
        try {
            context.write(new IntWritable(1), new Text(sleigh.header + "\n"));
            for (int i = chunkSize-1; i < numPresents; i += chunkSize){
                result = sleigh.currentPacking(i - (chunkSize-1), i);
                context.write(null, new Text(result));
                context.progress();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
