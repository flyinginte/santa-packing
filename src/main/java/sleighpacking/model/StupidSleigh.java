package sleighpacking.model;

public class StupidSleigh extends Sleigh{

    int curX;
    int curY;
    int curZ;
    int[] lastRowIdxs;
    int[] lastLayerIdxs;
    int numInRow;
    int numInLayer;

    public StupidSleigh(){
        this(1000, 1000, 1000000, 0);
    }

    public StupidSleigh(int l, int w, int numPresents, int offset){
        super(l, w, numPresents, offset);
        this.curX = 1;
        this.curY = 1;
        this.curZ = -1;
        this.lastRowIdxs = new int[200];
        this.lastLayerIdxs = new int[1000];
        this.numInRow = 0;
        this.numInLayer = 0;
    }

    public void pack(Present p){
        PackedPresent packedP;
        int[] x = new int[8];
        int[] y = new int[8];
        int[] z = new int[8];

        if (curX + p.w > w + 1){
            curY += maxPresentLengthInLastRow();
            curX = 1;
            numInRow = 0;
        }
        if (curY + p.l > l + 1){
            curZ -= maxPresentHeightInLastLayer();
            curX = 1;
            curY = 1;
            numInLayer = 0;
        }
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

        curX += p.w;
        numInRow++;
        numInLayer++;
        lastRowIdxs[numInRow - 1] = p.pId;
        lastLayerIdxs[numInLayer - 1] = p.pId;

        packedP = new PackedPresent(p, x, y, z);
//        System.out.println(p.pId);
//        System.out.println(offset);
        presents[p.pId - offset - 1] = packedP;
    }

    public void unpack(int index){};

    private int maxPresentLengthInLastRow(){
        int maxLen = 0;
        int curLen = 0;
        for (int i = 0; i < numInRow; i++){
            curLen = presents[lastRowIdxs[i] - offset - 1].present.l;
            if (curLen > maxLen){
                maxLen = curLen;
            }
        }
        return maxLen;
    }

    private int maxPresentHeightInLastLayer(){
        int maxHeight = 0;
        int curHeight = 0;
        for (int i = 0; i < numInLayer; i++){
            curHeight = presents[lastLayerIdxs[i] - offset - 1].present.h;
            if (curHeight > maxHeight){
                maxHeight = curHeight;
            }
        }
        return maxHeight;
    }

}
