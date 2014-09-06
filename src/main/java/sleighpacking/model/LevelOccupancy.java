package sleighpacking.model;
public class LevelOccupancy {
    boolean[] levelOccupancy;
    int length;
    int width;

    public LevelOccupancy(int length, int width){
        this.levelOccupancy = new boolean[length * width];
        this.length = length;
        this.width = width;
    }
    
    public boolean isOccupied(int x, int y){
        return levelOccupancy[y * length + x];
    }
    
    public void makeOccupied(int x, int y){
        levelOccupancy[y * length + x] = true;
    }
    
    public int[] occupy(int length, int width){
        boolean broken = false;
        for (int y = 0; y < this.length - length + 1; y++){
            for (int x = 0; x < this.width - width + 1; x++){
                if (!isOccupied(x, y)){
                    int y2 = y;
                    int x2 = x;
                    for (; y2 < y + length; y2++){
                    	x2 = x;
                        for (; x2 < x + width; x2++){
                            if(isOccupied(x2, y2)){
                                broken = true;
                                break;
                            }
                        }
                        if (broken){
                            break;
                        }
                    }
                    if (broken){
                        broken = false;
                        x = x2;
                    }else{
                        fill(x,y,width,length);
                        return new int[]{x,y};
                    }
                }
            }
        }
        return new int[]{-1,-1};
    }
    
    public void fill(int x, int y, int width, int length){
        int lastY = y + length;
        int lastX = x + width;
        
        int temp = x;
        
        for (; y < lastY; y++){
        	x = temp;
            for (; x < lastX; x++){
                makeOccupied(x, y);
            }
        }
    }
    
    public double percentFilled(){
        int numFilled = 0;
        for (boolean b : levelOccupancy){
            if (b){
                numFilled ++;
            }
        }
        return numFilled/((double)length * width);
    }
    
    public String toString(){
        StringBuffer buffer = new StringBuffer();
    	for (int x = 0; x < length * width; x++){
    		if ((x+1) % length == 0){
    			if (levelOccupancy[x]){
    				buffer.append("1\n");
    			}else{
    				buffer.append("0\n");
    			}
    		}else{
    			if (levelOccupancy[x]){
    				buffer.append("1");
    			}else{
    				buffer.append("0");
    			}
    		}
    	}
    	return buffer.toString();
    }

    //return a leveloccupancy object where occupied spaces
    //are difference between the 2
    public LevelOccupancy diff(LevelOccupancy other){
        LevelOccupancy difference = new LevelOccupancy(length,width);

        for (int x = 0; x < width; x++){
            for (int y = 0; y < length; y++){
                if (this.isOccupied(x,y) != other.isOccupied(x,y)){
                    difference.makeOccupied(x,y);
                }
            }
        }

        return difference;
    }

    public boolean equals(LevelOccupancy other){

        for (int x = 0; x < width; x++){
            for (int y = 0; y < length; y++){
                if (this.isOccupied(x,y) != other.isOccupied(x,y)){
                    return false;
                }
            }
        }

        return true;

    }
    
}