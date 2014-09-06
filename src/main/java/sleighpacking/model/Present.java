package sleighpacking.model;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Present implements WritableComparable<Present> {

    public int pId;
	public int l;
	public int w;
	public int h;
    public static final int MAX_DIMENSION = 200;

    public Present(){
        this(0, 0, 0, 0);
    }

	public Present(int pId, int w, int l, int h){ // rearranged w and l to correspond to x and y correctly
		this.pId = pId;
		this.l = l;
		this.w = w;
		this.h = h;
	}
	
	public void rotate(){
		int temp = l;
		l = w;
		w = h;
		h = temp;
	}

    @Override
    public int compareTo(Present o) {
        if (this.pId == o.pId){
            return 0;
        }

        return this.pId < o.pId ? -1 : 1;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(pId);
        dataOutput.writeInt(l);
        dataOutput.writeInt(w);
        dataOutput.writeInt(h);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        pId = dataInput.readInt();
        l = dataInput.readInt();
        w = dataInput.readInt();
        h = dataInput.readInt();
    }
	
	public String toString(){
		return pId + " " + l + " " + w + " " + h;
	}
}
