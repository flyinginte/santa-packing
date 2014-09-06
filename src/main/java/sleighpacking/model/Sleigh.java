package sleighpacking.model;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.*;

import java.io.*;
import java.util.*;


public abstract class Sleigh extends Configured implements Comparable<Sleigh>, WritableComparable<Sleigh> {
	public int l;
	public int w;
	public PackedPresent[] presents;
    public int numPresents;
    public int offset; //offset for present window (id fix)
    public final String header = "PresentId,x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4,x5,y5,z5,x6,y6,z6,x7,y7,z7,x8,y8,z8";

    public Sleigh(){
        this(1000, 1000, 1000000, 0);
    }

    public Sleigh(int l, int w, int numPresents, int offset){
		this.l = l;
		this.w = w;
		this.presents = new PackedPresent[numPresents];
        this.offset = offset;
        this.numPresents = numPresents;
	}

    public abstract void pack(Present p);

    public abstract void unpack(int index);

	public void shiftZValsUp(){
        int minZ = minZ();

        if (minZ > 0)
            return; //we dont shift past this

        for (PackedPresent p : presents){
            if (p != null){
                for (int i = 0; i < p.z.length; i++){
                    p.z[i] = p.z[i] - minZ + 1;
                }
            }
        }
	}

    public void shiftZValsDown(){
        int maxZ = maxZ();

        if (maxZ < 0)
            return; //we dont shift past this

        for (PackedPresent p : presents){
            if (p != null){
                for (int i = 0; i < p.z.length; i++){
                    p.z[i] = p.z[i] - maxZ - 1;
                }
            }
        }
    }

	public int maxZ(){
		int maxZ = Integer.MIN_VALUE;
		for (PackedPresent p : presents){
			if (p != null){
				for (int z : p.z){
					if (z > maxZ){
						maxZ = z;
					}
				}
			}
		}
		return maxZ;
	}

	public int minZ(){
		int minZ = Integer.MAX_VALUE;
		for (PackedPresent p : presents){
			if (p != null){
				for (int z : p.z){
					if (z < minZ){
						minZ = z;
					}
				}
			}
		}
		return minZ;
	}

	private ArrayList<Integer> idealOrder(){
		ArrayList<Integer> idealOrder = new ArrayList<Integer>();
		for (PackedPresent p : presents){
			if (p != null){
				idealOrder.add(p.present.pId);
			}
		}
		Collections.sort(idealOrder);
		return idealOrder;
	}

	public int evaluatePacking(){
		shiftZValsUp();
		int maxZ = maxZ();

		int pMaxZ;
		Map<Integer, ArrayList<PackedPresent>> presentMaxZ = new HashMap<Integer, ArrayList<PackedPresent>>();
		for (PackedPresent p : presents){
			if (p != null){
				pMaxZ = p.maxZ();
				if (!presentMaxZ.containsKey(pMaxZ)){
					presentMaxZ.put(pMaxZ, new ArrayList<PackedPresent>());
				}
				presentMaxZ.get(pMaxZ).add(p);
			}
		}

		ArrayList<Integer> stackOrder = new ArrayList<Integer>();

		ArrayList<Integer> levelOrder;

		for (int i = maxZ; i > 0; i--){
			if (presentMaxZ.containsKey(i)){
				levelOrder = new ArrayList<Integer>();
				for (PackedPresent p : presentMaxZ.get(i)){
					levelOrder.add(p.present.pId);
				}
				Collections.sort(levelOrder);
				stackOrder.addAll(levelOrder);
			}
		}

		ArrayList<Integer> idealOrder = idealOrder();

		int idealDiff = 0;

		for (int i = 0; i < idealOrder.size(); i++){
			idealDiff += Math.abs(idealOrder.get(i) - stackOrder.get(i));
		}

        shiftZValsDown();

		int score = 2 * maxZ + idealDiff;
		return score;
	}

    public boolean checkCollisions(){
        for (int i = 0; i < presents.length; i++){
            for (int j = i+1; j < presents.length; j++){
                PackedPresent p1 = presents[i];
                PackedPresent p2 = presents[j];

                if (p1.maxZ() < p2.minZ() || p1.minZ() > p2.maxZ())
                    continue;

                if (p1.maxX() < p2.minX() || p1.minX() > p2.maxX())
                    continue;

                if (p1.maxY() < p2.minY() || p1.minY() > p2.maxY())
                    continue;

                //if we reach this point we overlap on all 3 axes  (collision)
                System.out.println(p1);
                System.out.println(p2);
                return false;
            }
        }

        return true;
    }

    @Override
    public int compareTo(Sleigh other){
        if (this.offset == other.offset){
            return 0;
        }

        return this.offset < other.offset ? -1 : 1;
    }

    public String currentPacking(int start, int end){


        //predict size of string

        shiftZValsDown();
        String score = String.valueOf(evaluatePacking()) + "\n";
        shiftZValsUp();

        int size = score.length();

        //count how much space we will need
        for (int index = start; index <= end; index++){
            PackedPresent p = presents[index];

            String ln = "";
            ln += p.present.pId;

            for (int i = 0; i < p.x.length; i++){
                ln += "," + p.x[i] + "," + p.y[i] + "," + p.z[i];
            }

            ln += "\n";

            size += ln.length();
        }
//
//       StringWriter sw = new StringWriter(size);
//       sw.append(score);
//        FileIO.writeLine(header, "solution-hadoop.csv");

        StringBuffer sb = new StringBuffer(size);

        for (int index = start; index <= end; index++){
            PackedPresent p = presents[index];

            String ln = "";
            ln += p.present.pId;

            for (int i = 0; i < p.x.length; i++){
                ln += "," + p.x[i] + "," + p.y[i] + "," + p.z[i];
            }

            ln += "\n";

            sb.append(ln);
        }

        return sb.toString();

    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(l);
        dataOutput.writeInt(w);
        dataOutput.writeInt(numPresents);
        dataOutput.writeInt(offset);

        dataOutput.writeInt(presents.length);
        for (PackedPresent p : presents){
            p.write(dataOutput);
        }
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        l = dataInput.readInt();
        w = dataInput.readInt();
        numPresents = dataInput.readInt();
        offset = dataInput.readInt();

        int length = dataInput.readInt();
        presents = new PackedPresent[length];

        for (int i = 0; i < presents.length; i++){
            presents[i] = new PackedPresent();
            presents[i].readFields(dataInput);
        }


    }
}
