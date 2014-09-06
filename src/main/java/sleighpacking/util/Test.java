package sleighpacking.util;
import org.apache.hadoop.io.Text;
import sleighpacking.solve.DDS;
import sleighpacking.model.*;

import java.util.*;


public class Test {

    public static void main(String[] args){
//    	  compareStupidToSmart();
//        runSmartPacking();
//        runStupidPacking();
//        testDDS();
//        testUnpack();
//        FileIO.keyPresents("presents.csv", "keyed", 8);
    }

    private static void testUnpack(){
        int numPresents = 100;
        int unpackIndex = 70;
        Sleigh s = new SmartSleigh(1000,1000,numPresents, 0);
        Present[] presents = FileIO.getPresents("presents.csv");

        for (int i = 0; i < numPresents; i++){
            s.pack(presents[i]);
        }

        s.unpack(unpackIndex);
        for (int i = unpackIndex; i < numPresents; i++){
            s.pack(presents[i]);
        }

        System.out.println(s.checkCollisions());
    }

    private static void testDDS(){
        Present[] presents = FileIO.getPresents("presents-default.csv");
        presents = Arrays.copyOfRange(presents, 997099, 1000000);

        DDS test = new DDS(presents, 30, 997099, true);
        Sleigh s = test.call();

        int score = s.evaluatePacking();
        System.out.println(score);

        System.out.println(s.checkCollisions());

        FileIO.writeSolution(s.presents, "dds.csv");
    }

    private static void combineSolutions(){
        PackedPresent[] base = FileIO.getPackedPresents("solution1.csv");
        PackedPresent[] top = FileIO.getPackedPresents("solution2.csv");  //need to have height added

        Sleigh s = new SmartSleigh(1000, 1000, base.length, base.length);
        s.presents = base;
        int baseHeight = s.maxZ();

        int count = 0;
        for (PackedPresent p : top){
//            count++;
//            System.out.println(count);
            for (int z = 0; z < 8; z++) {
                p.z[z] += baseHeight;
            }
        }

        PackedPresent[] masterPresents = new PackedPresent[base.length + top.length];
        System.arraycopy(base, 0, masterPresents, 0, base.length);
        System.arraycopy(top, 0, masterPresents, base.length, top.length);

        Sleigh masterSleigh = new SmartSleigh(1000, 1000, 1000000, 0);
        masterSleigh.presents = masterPresents;
        System.out.println(masterSleigh.evaluatePacking());

        FileIO.writeSolution(masterPresents, "combined.csv");
    }




    private static void runSmartPacking(){
        int numPresents = 50;
        Sleigh s = new SmartSleigh(1000,1000,numPresents, 0);
        Present[] presents = FileIO.getPresents("presents.csv");
        for (int i = 0; i < numPresents; i++){
            s.pack(presents[i]);
        }

        int score = s.evaluatePacking();
        System.out.println(score);
//    	FileIO.writeSolution(s.presents, "solution.csv");
    }

    private static void runStupidPacking(){
        int numPresents = 1000000;
        Sleigh s = new StupidSleigh(1000,1000,numPresents, 0);
        Present[] presents = FileIO.getPresents("presents.csv");
        for (int i = 0; i < numPresents; i++){
            s.pack(presents[i]);
        }

        s.evaluatePacking();
        System.out.println(s.checkCollisions());
        FileIO.writeSolution(s.presents, "solution-stupid.csv");
    }

    private static void compareStupidToSmart(){
        int numTrials = 10;
        int presentWindow = 1000;
        int numPresents = numTrials * presentWindow;
        int presentStart = 15000;
        int presentEnd = presentWindow;

        Present[] presents = FileIO.getPresents("presents.csv");
        Present[] presentsSlice;
        Sleigh s;
        String line;

        while (presentEnd < numPresents){
            presentsSlice = Arrays.copyOfRange(presents, presentStart, presentEnd);

            s = new SmartSleigh(1000,1000,presentWindow,presentStart);

            for (Present p : presentsSlice){
                s.pack(p);
            }

            line = s.evaluatePacking() + "";
//            FileIO.writeSolution(s.presents, "solution-stupid.csv");

            s = new StupidSleigh(1000,1000,presentWindow,presentStart);

            for (Present p : presentsSlice){
                s.pack(p);
            }

            line += "," + s.evaluatePacking();
            System.out.println(line);

//            FileIO.writeSolution(s.presents, "solution.csv");

            presentStart += presentWindow;
            presentEnd += presentWindow;

            FileIO.writeLine(line, "compare.csv");

        }
    }

    public static void rotateToMinimizeHeight(){

        Present[] presents = FileIO.getPresents("presents.csv");

        ArrayList<Integer> temp;
        for (Present p : presents){
            temp = new ArrayList<Integer>();

            temp.add(p.h);
            temp.add(p.l);
            temp.add(p.w);

            Collections.sort(temp);

            p.h = temp.get(0);
            p.l = temp.get(1);
            p.w = temp.get(2);
        }

        FileIO.writePresents(presents, "presentsLowZ.csv");

    }

}
