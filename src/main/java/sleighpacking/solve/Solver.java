package sleighpacking.solve;
import org.apache.hadoop.mapreduce.Mapper;
import sleighpacking.model.Present;
import sleighpacking.model.Sleigh;
import sleighpacking.model.SmartSleigh;
import sleighpacking.model.StupidSleigh;

import java.util.concurrent.Callable;


public class Solver implements Callable<Sleigh> {

    Present[] presents;
    int numPresents;
    int offset;
    boolean packSmart;
    Mapper.Context context;

    public Solver(Present[] presents, int offset, boolean packSmart){
        this.presents = presents;
        this.numPresents = presents.length;
        this.offset = offset;
        this.packSmart = packSmart;
    }

    public Solver(Present[] presents, int offset, boolean packSmart, Mapper.Context context){
        this.presents = presents;
        this.numPresents = presents.length;
        this.offset = offset;
        this.packSmart = packSmart;
        this.context = context;
    }

    @Override
    public Sleigh call() {
        Sleigh s;
        if (packSmart){
            s = new SmartSleigh(1000, 1000, numPresents, offset);
        } else {
            s = new StupidSleigh(1000, 1000, numPresents, offset);
        }

        for (int i = 0; i < numPresents; i++){
            if (context != null && i % 50 == 0){
                context.progress();
            }

            s.pack(presents[i]);
        }

        return s;
    }

}
