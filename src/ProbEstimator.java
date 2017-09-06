import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.lang.Math;

/**
 * This class takes a text file as input and tokenize sentences and tokens.
 * The output is a stream of tokens, with <s> and </s> indicating sentence boundaries.
 */
public class ProbEstimator{

    /**
     * args[0]: source text file
     */
    public static void main(String[] args) throws IOException {
        String inputFileName = args[0];

        //Open input file
        FileReader fr = new FileReader(inputFileName);
        BufferedReader br = new BufferedReader(fr);

        //HashMap for single word
        Map<String, Integer> wordMap = new HashMap<>();
        //HashMap for token
        Map<String, Integer> tokenMap = new HashMap<>();

        //Read token one by one
        String preLine = null;
        String line = null;
        int maxTokenCount = 0;
        int N = 0;
        int V = 0;
        int IN = 0;

        while ((line = br.readLine()) != null) {
            //Count words' frequencies
            Integer count;
            if((count = wordMap.get(line)) == null){
                V++;
                wordMap.put(line, 1);
            }
            else wordMap.put(line, count+1);

            //Count all seen tokens' frequencies
            if(preLine != null){
                N++;
                String key = preLine + " " + line;
                if((count = tokenMap.get(key)) == null){
                    IN = 0;
                    tokenMap.put(key, 1);
                    maxTokenCount = maxTokenCount > 1 ? maxTokenCount : 1;
                }
                else{
                    tokenMap.put(key, count+1);
                    maxTokenCount = maxTokenCount > count + 1 ? maxTokenCount : count + 1;
                }
            }

            preLine = line;
        }
        br.close();
        fr.close();

        //Save C(v) and C(w,v) to bigrams.text
        FileWriter fw = new FileWriter("results/bigrams.txt");
        BufferedWriter bw = new BufferedWriter(fw);

        for(Map.Entry<String, Integer> entry:wordMap.entrySet()){    
            String key = entry.getKey();
            Integer value=entry.getValue();  
            //bw.write(key+"\n"+value+"\n");
        }
        //bw.write("\n");

        //Count ff
        int[] ff = new int[maxTokenCount + 1];
        ff[0] = V*V - IN;
        for(Map.Entry<String, Integer> entry:tokenMap.entrySet()){    
            String key=entry.getKey();
            Integer value=entry.getValue();  
            bw.write(key+"\n"+value+"\n");
            
            ff[value.intValue()] += 1;
        }
        
        bw.close();
        fw.close();

        fw = new FileWriter("results/ff.txt");
        bw = new BufferedWriter(fw);
        for(int i : ff)bw.write(i + "\n");
        bw.close();
        fw.close();

        //Save C(v) and C(w,v) to bigrams.text
        fw = new FileWriter("results/GTTable.txt");
        bw = new BufferedWriter(fw);

        SimpleRegression rg = new SimpleRegression();
        for(int i = 0; i < ff.length; i++){
            if(i > 0 && ff[i] > 0){
                rg.addData(Math.log(i), Math.log(ff[i]));
            }
        }

        bw.write((V*V - IN) + "\n");
        for(int i = 1; i < ff.length; i++){
            if(ff[i] > 0)bw.write(ff[i] + "\n");
            else bw.write(Math.exp(rg.predict(Math.log(i))) + "\n");
        }
        bw.close();
        fw.close();

    }
}
