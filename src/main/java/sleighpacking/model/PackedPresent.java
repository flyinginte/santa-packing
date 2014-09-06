package sleighpacking.model;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PackedPresent implements WritableComparable<PackedPresent>{
	public Present present;
	public int[] x;
	public int[] y;
	public int[] z;

    public PackedPresent(){
        this(new Present(),
                new int[]{0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0},
                new int[]{0, 0, 0, 0, 0, 0, 0, 0});
    }
	
	public PackedPresent(Present present, int[] x, int[] y, int[] z) {
		this.present = present;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int maxZ(){
		return max(z);
	}

    public int minZ(){
        return min(z);
    }

    public int maxX(){
        return max(x);
    }

    public int minX(){
        return min(x);
    }

    public int maxY(){
        return max(y);
    }

    public int minY(){
        return min(y);
    }

    private int max(int[] arr){
        int max = Integer.MIN_VALUE;
        for (int i : arr){
            if (i > max){
                max = i;
            }
        }
        return max;
    }

    private int min(int[] arr){
        int min = Integer.MAX_VALUE;
        for (int i : arr){
            if (i < min){
                min = i;
            }
        }
        return min;
    }

    public String toString(){
        String s = present.toString() +"\n";
        s += "x: " + maxX() + ":" + minX() +"\n";
        s += "y: " + maxY() + ":" + minY() + "\n";
        s += "z: " + maxZ() + ":" + minZ();
        return s;
    }

    @Override
    public int compareTo(PackedPresent o) {
        return this.present.compareTo(o.present);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        present.write(dataOutput);
        for (int i = 0; i < x.length; i++){
            dataOutput.writeInt(x[i]);
            dataOutput.writeInt(y[i]);
            dataOutput.writeInt(z[i]);
        }
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        present.readFields(dataInput);

        for (int i = 0; i < x.length; i++){
            x[i] = dataInput.readInt();
            y[i] = dataInput.readInt();
            z[i] = dataInput.readInt();
        }
    }
}
