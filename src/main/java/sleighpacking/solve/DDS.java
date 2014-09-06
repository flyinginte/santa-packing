package sleighpacking.solve;

import org.apache.hadoop.mapreduce.Mapper;
import sleighpacking.model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class DDS implements Callable<Sleigh> {

    Present[] curPresents;
    int maxIterations;
    int offset;
    boolean packSmart;
    Mapper.Context context;

    public DDS(Present[] presents, int maxIterations, int offset, boolean packSmart){
        this.curPresents = presents;
        this.maxIterations = maxIterations;
        this.offset = offset;
        this.packSmart = packSmart;
    }

    public DDS(Present[] presents, int maxIterations, int offset, boolean packSmart, Mapper.Context context){
        this.curPresents = presents;
        this.maxIterations = maxIterations;
        this.offset = offset;
        this.packSmart = packSmart;
        this.context = context;
    }

    @Override
    public Sleigh call() {
        return runDDS();
    }

    private Sleigh runDDS(){
        int numPresents = curPresents.length;
        PackedPresent[] curPackedPresents;
        Present[] nextPresents;
        int nextScore;
        int curScore;

        double rotationScalingFactor = 0.001;
        double permuteScalingFactor = 0.0001;

        double pRotation;
        double pPermute;

        double logMax = Math.log(maxIterations);

        Sleigh s;
        if (packSmart)
            s = new SmartSleigh(1000,1000,numPresents,offset);
        else
            s = new StupidSleigh(1000,1000,numPresents,offset);

        for (int j = 0; j < curPresents.length; j++){
            Present p = curPresents[j];
            s.pack(p);

            if (context != null && j % 50 == 0){
                context.progress();
            }
        }

        curScore = s.evaluatePacking();
        curPackedPresents = s.presents;

        for (int i = 1; i < maxIterations; i++){
            System.out.println("Current: " + curScore);

            pRotation = (1.0 - Math.log(i)/logMax) * rotationScalingFactor;
            pPermute = (1.0 - Math.log(i)/logMax) * permuteScalingFactor;

            HashMap<String, Object> mutationResult = mutateDDS(curPresents, pRotation, pPermute);
            nextPresents = (Present[]) mutationResult.get("presents");
            Integer startPackingIndex = (Integer) mutationResult.get("startPackingIndex");
            if (startPackingIndex == -1)
                startPackingIndex++;

            s.unpack(startPackingIndex);
            Present[] nextPresentsSlice = Arrays.copyOfRange(nextPresents, startPackingIndex, nextPresents.length);

            for (int j = 0; j < nextPresentsSlice.length; j++){
                Present p = nextPresentsSlice[j];
                s.pack(p);

                if (context != null && j % 50 == 0){
                    context.progress();
                }
            }

            nextScore = s.evaluatePacking();
            System.out.println("Next: " + nextScore);
            if (nextScore <= curScore){
                System.out.println("IMPROVEMENT!");
                curPresents = nextPresents;
                curPackedPresents = s.presents;
                curScore = nextScore;
            }

        }

        s.presents = curPackedPresents;
        return s;
//        FileIO.writePresents(curPresents, outputFileName);
    }

    private HashMap<String, Object> mutateDDS(Present[] presents, double pRotation, double pPermute){
        HashMap<String, Object> map = new HashMap<String, Object>();
        Present[] newPresents = copy(presents);
        double rotateCoin;
        double permuteCoin;
        int startPackingIndex = -1;  //track where to start the repacking

        for (int i = 0; i < newPresents.length; i++){
            Present p = newPresents[i];
            rotateCoin = Math.random();
            permuteCoin = Math.random();
            if (rotateCoin <= pRotation){
                if (startPackingIndex == -1){
                    startPackingIndex = i;
                }

                if (rotateCoin <= pRotation/2.0){ // gives 50% chance of rotating twice
                    p.rotate();
                }
                p.rotate();
            }
            if (permuteCoin <= pPermute){
                if (startPackingIndex == -1){
                    startPackingIndex = i;
                }

                newPresents[i] = newPresents[i+1];
                newPresents[i+1] = p;
            }
        }

        map.put("presents", newPresents);
        map.put("startPackingIndex", startPackingIndex);

        return map;
    }

    private static Present[] copy(Present[] presents){
        Present[] newCopy = new Present[presents.length];
        for (int i = 0; i < presents.length; i++){
            Present p = presents[i];
            newCopy[i] = new Present(p.pId, p.w, p.l, p.h);
        }
        return newCopy;
    }
}
