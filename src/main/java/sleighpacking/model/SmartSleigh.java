package sleighpacking.model;

import java.util.*;

public class SmartSleigh extends Sleigh{

    List<LevelOccupancy> occupancies;
    List<Double> percentOccupied;
    HashMap<Integer, ArrayList<PackedPresent>> presentsAtZ;

    public SmartSleigh(){
        this(1000, 1000, 1000000, 0);
    }

    public SmartSleigh(int l, int w, int numPresents, int offset){
        super(l, w, numPresents, offset);
        this.occupancies = new ArrayList<LevelOccupancy>();
        this.percentOccupied = new ArrayList<Double>();
        this.presentsAtZ = new HashMap<Integer, ArrayList<PackedPresent>>();
    }

    public void pack(Present p){
        LevelOccupancy curLevel;            //maintain occupied spots of curr z level
        int[] coords = new int[]{-1,-1};    //eventual x and y coords of packed present
        boolean firstRun = true;
        int curZ = -1 - percentOccupied.size();

        if (p.pId % 100 == 0){
            System.out.println(p.pId + " " + curZ);  //logging
        }

        while (coords[0] == -1){ //while we still havent found a free space to occupy

            if (occupancies.isEmpty())   {
                occupancies.add(new LevelOccupancy(l, w));
            }
            else if (!firstRun) {
                percentOccupied.add(occupancies.get(0).percentFilled());    //track percent occupied in each level
                occupancies.remove(0);        //this level wont work, let's move to the next
            }

            curZ = -1 - percentOccupied.size();

            curLevel = occupancies.get(0);
            coords = curLevel.occupy(p.l, p.w);  //try to occupy this level with our length and width

            firstRun = false;
        }

        for (int h = 1; h < p.h; h++)   {
            if (occupancies.size() <= h){
                occupancies.add(new LevelOccupancy(l, w));   //add new level occupancies if we havent been this low yet
            }

            occupancies.get(h).fill(coords[0], coords[1], p.w, p.l);   //fill level occupancies with current present
        }

        PackedPresent packedP;
        int[] x = new int[8];
        int[] y = new int[8];
        int[] z = new int[8];

        int curX = coords[0]+1;
        int curY = coords[1]+1;

        //set coordinates on packedpresent
        for (int i = 0; i < 8; i++){
            if (i%2 == 0){
                x[i] = curX;
                y[i] = curY;
                z[i] = curZ;
            }else{
                x[i] = curX + p.w - 1;
                y[i] = curY + p.l - 1;
                z[i] = curZ - p.h + 1;
            }
        }

        packedP = new PackedPresent(p, x, y, z);
        presents[p.pId - offset - 1] = packedP;

        if (presentsAtZ.containsKey(packedP.maxZ())){
            presentsAtZ.get(curZ).add(packedP);
        }
        else {
            ArrayList<PackedPresent> temp = new ArrayList<PackedPresent>();
            temp.add(packedP);
            presentsAtZ.put(packedP.maxZ(), temp);
        }
    }

    //removes p at index and all presents following
    public void unpack(int index){

        if (index == 0){
            presents = new PackedPresent[presents.length];
            occupancies = new ArrayList<LevelOccupancy>();
            percentOccupied = new ArrayList<Double>();
            return;
        }

        // find z for top of present with id before p
        // this is how far we need our new level occupancies to go
        // (bc we wont place p higher than this present)
        int previousPresentTop = presents[index-1].maxZ();

        //remove presents from z-index->present top map
        for (int i = index; i < numPresents; i++){
            int top = presents[i].maxZ();
            presentsAtZ.get(top).remove(presents[i]);
        }

        //remove presents that will be repacked
        presents = Arrays.copyOfRange(presents, 0, index);

        rebuildOccupancy(previousPresentTop);

        //make room for presents that will be repacked
        presents = Arrays.copyOfRange(presents, 0, numPresents);

    }

    private void rebuildOccupancy(int start){
        HashMap<Integer, LevelOccupancy> occupancyMap = new HashMap<Integer, LevelOccupancy>();
        int z;

        if (start >= -Present.MAX_DIMENSION){
            z = 0;
        }
        else {
            z = start + Present.MAX_DIMENSION + 1;
        }

        while (z >= minZ()){
            z--;

            if (!presentsAtZ.containsKey(z))
                continue;

            for (PackedPresent p : presentsAtZ.get(z)) {
                for (int i = p.maxZ(); i >= p.minZ(); i--){
                    if (occupancyMap.containsKey(i)){
                        occupancyMap.get(i).fill(p.minX()-1, p.minY()-1, p.present.w, p.present.l);
                    }
                    else {
                        LevelOccupancy temp = new LevelOccupancy(l, w);
                        temp.fill(p.minX()-1, p.minY()-1, p.present.w, p.present.l);
                        occupancyMap.put(i, temp);
                    }

                }
            }

        }

        occupancies.clear();
        SortedSet<Integer> keys = new TreeSet<Integer>(occupancyMap.keySet());
        for (Integer key : keys){
            if (key <= start){
                occupancies.add(occupancyMap.get(key));
            }
            else {
                break;
            }
        }

        Collections.reverse(occupancies);

        //fix percent occupied
        while (percentOccupied.size() >= Math.abs(start)){
            percentOccupied.remove(percentOccupied.size()-1);
        }
    }


    public double averageOccupancy(){
        double total = 0.0;
        for (Double d : percentOccupied){
            total += d;
        }
        return total/percentOccupied.size();
    }


}
