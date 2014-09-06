package sleighpacking.util;

import org.apache.hadoop.io.Text;
import sleighpacking.model.PackedPresent;
import sleighpacking.model.Present;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class FileIO {

	public static Present[] getPresents(String filename) {
		String inputFileName = filename;
		String filePrefix = "input/";
		String splitChar = ",";
		BufferedReader br = null;
        Present[] presents = new Present[1000000];

        try{
			br = new BufferedReader(new FileReader(filePrefix + inputFileName));
			String line;
			String[] els = null;
			line = br.readLine(); // for the headers
			int position = 0;
			while ((line = br.readLine()) != null){
				els = line.split(splitChar);
				presents[position] = new Present(toInt(els[0]), toInt(els[1]), toInt(els[2]), toInt(els[3]));
				position++;
			}
			br.close();
        } catch(Exception e){
            e.printStackTrace();
        }

        return presents;
    }
	
	public static void writePresents(Present[] presents, String outputFileName){
		String header = "PresentId,Dimension1,Dimension2,Dimension3";
		String filePrefix = "output/";
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(filePrefix + outputFileName);
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		pw.println(header);
		for (Present p : presents){
			pw.println(p.pId + "," + p.w + "," + p.l + "," + p.h);
		}
		pw.close();
	}

    public static PackedPresent[] getPackedPresents(String filename){
        String inputFileName = filename;
        String filePrefix = "output/";
        String splitChar = ",";
        BufferedReader br = null;
        ArrayList<PackedPresent> packedPresents = new ArrayList<PackedPresent>();
        Present[] presents = getPresents("presents.csv");

        try{
            br = new BufferedReader(new FileReader(filePrefix + inputFileName));
            String line;
            String[] els = null;
            line = br.readLine(); // for the headers
            int position = 0;
            int offset = 0;

            ArrayList<Integer> x;
            ArrayList<Integer> y;
            ArrayList<Integer> z;

            while ((line = br.readLine()) != null){
                x = new ArrayList<Integer>();
                y = new ArrayList<Integer>();
                z = new ArrayList<Integer>();

                els = line.split(splitChar);

                if (position == 0){ //first run
                    offset = toInt(els[0]) - 1;
                }

                for (int i = 1; i < els.length; i++){
                    if (i % 3 == 0) {//z
                        z.add(toInt(els[i]));
                    }
                    else if ((i-1) % 3 == 0) {//x
                        x.add(toInt(els[i]));
                    }
                    else {
                        y.add(toInt(els[i]));
                    }
                }

                Present p = presents[position+offset];
                packedPresents.add(new PackedPresent(p, toArray(x), toArray(y), toArray(z)));

                position++;
            }
            br.close();
        } catch(Exception e){
            e.printStackTrace();
        }

        return packedPresents.toArray(new PackedPresent[packedPresents.size()]);
    }

    public static void writeSolution(PackedPresent[] presents, String outputFileName) {
        String header = "PresentId,x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4,x5,y5,z5,x6,y6,z6,x7,y7,z7,x8,y8,z8";
        String filePrefix = "output/";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(filePrefix + outputFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        pw.println(header);
        for (PackedPresent p : presents){
            String ln = "";
            ln += p.present.pId;
            for (int i = 0; i < p.x.length; i++){
                ln += "," + p.x[i] + "," + p.y[i] + "," + p.z[i];
            }
            pw.println(ln);
        }
        pw.close();
    }

    public static void writeLine(String line, String outputFileName) {
        String filePrefix = "output/";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(new File(filePrefix + outputFileName), true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.println(line);
        pw.close();
    }

    public static void keyPresents(String inputFileName, String outputFolder, int chunks){
        Present[] presents = getPresents(inputFileName);
        String filePrefix = "input/"+outputFolder+"/";

        int numPresents = presents.length;
        int chunkSize = numPresents / chunks;
        int currKey = 1;

        PrintWriter pw = null;
        try{
            pw = new PrintWriter(filePrefix + currKey + ".csv");
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        pw.print(currKey + "$");

        for (Present p : presents){
            if (p.pId % chunkSize == 1 && p.pId != numPresents){
                currKey++;
                try {
                    pw.close();
                    pw = new PrintWriter(filePrefix + currKey + ".csv");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                pw.print(currKey + "$");
            }

            if ((p.pId + 1) % chunkSize == 1 && p.pId != numPresents-1 || p.pId == numPresents)
                pw.print(p.pId + "," + p.w + "," + p.l + "," + p.h);
            else
                pw.print(p.pId + "," + p.w + "," + p.l + "," + p.h + ":");


        }
        pw.close();
    }
	
	public static int toInt(String str){
		return Integer.parseInt(str);
	}

    private static int[] toArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        int i = 0;
        for(Iterator<Integer> it = list.iterator(); it.hasNext(); ret[i++] = it.next());
        return ret;
    }

    public static Present[] getPresentsFromValue(Text value){
        StringTokenizer stColon = new StringTokenizer(value.toString(), ":");
        StringTokenizer stComma;
        ArrayList<Present> presentList = new ArrayList<Present>();

        while(stColon.hasMoreElements()){
            stComma = new StringTokenizer((String) stColon.nextElement(), ",");

            Integer pId = Integer.parseInt((String) stComma.nextElement());
            Integer w = Integer.parseInt((String) stComma.nextElement());
            Integer l = Integer.parseInt((String) stComma.nextElement());
            Integer h = Integer.parseInt((String) stComma.nextElement());

            Present p = new Present(pId, w, l, h);

            presentList.add(p);
        }

        return presentList.toArray(new Present[presentList.size()]);
    }


}
