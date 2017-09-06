import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
  * This class takes a text file as input and tokenize sentences and tokens.
  * The output is a stream of tokens, with <s> and </s> indicating sentence boundaries.
  */
public class Predictor {

    static interface Estimator{
        public double estimate(Integer count);
    }

    static class LEstimator implements Estimator{
        public double estimate(Integer count){
            if(count != null)return count;
            else return 0;
        }
    }

    static class GTEstimator implements Estimator{

        Map<Integer, Double> FFTable;

        public GTEstimator(String tableFile) throws IOException{
            FFTable = new HashMap<>();

            FileReader fr = new FileReader(tableFile);
            BufferedReader br = new BufferedReader(fr);

            int key = 0;
            String line = null;
            while((line = br.readLine()) != null){
                FFTable.put(key, Double.parseDouble(line));
                key++;
            }

            br.close();
            fr.close();
        }

        public double estimate(Integer count){
            if(count == null)count = 0;
            //if(count > 3)return count;
            Double Nc = FFTable.get(count);
            System.out.println(count + " "+Nc);
            Double Nc1 = FFTable.get(count + 1);
            if(Nc1 == null)return 0;
            return (count+1) * (Nc1 / Nc); 
        }
    }

	/**
	  * args[0]: source text file
	  * args[1]: output tokenized file
	  */
	public static void main(String[] args) throws IOException {
		String inputFileName = args[0];
		String outputFileName = args[1];

        FileReader fr = new FileReader("data/all_confusingWords.txt");
        BufferedReader br = new BufferedReader(fr);

        Map<String, List<String> > confusingPairs = new HashMap<>();
        String line = null;
        while((line = br.readLine()) != null){
            String[] stringPair = line.split(":");
            List<String> list = confusingPairs.get(stringPair[0]);
            if(list == null){
                list = new ArrayList<>();
                confusingPairs.put(stringPair[0], list);
            }
            list.add(stringPair[1]);

            list = confusingPairs.get(stringPair[1]);
            if(list == null){
                list = new ArrayList<>();
                confusingPairs.put(stringPair[1], list);
            }
            list.add(stringPair[0]);
        }

        br.close();
        fr.close();

        fr = new FileReader("results/bigrams.txt");
        br = new BufferedReader(fr);

        Map<String, Integer> wordHm = new HashMap<>();
        Map<String, Integer> tokenHm = new HashMap<>();
        boolean isWord = false;
        while((line = br.readLine()) != null){
            if(line == ""){
                isWord = false;
                continue;
            }
            if(isWord){
                String key = line;
                line = br.readLine();
                //System.out.println(key +" " +line);
                wordHm.put(key, Integer.parseInt(line));
            }
            else{
                String key = line;
                line = br.readLine();
                tokenHm.put(key, Integer.parseInt(line));
            }
        }

        fr = new FileReader("data/test_tokens_fake.txt");
        br = new BufferedReader(fr);

        FileWriter fw = new FileWriter("results/test_prdictions.txt");
        BufferedWriter bw = new BufferedWriter(fw);

        String preWord = null;
        int sentenceId = -1;
        int wordId = 0;
        
        //Estimator estimator = new LEstimator();
        Estimator estimator = new GTEstimator("results/GTTable.txt");

        int hit = 0;
        while((line = br.readLine()) != null){
            if(sentenceId >= 0){
                List<String> confusingWords = null;
                if((confusingWords = confusingPairs.get(line)) != null){
                    hit++;
                    double estimate_count = estimator.estimate(tokenHm.get(preWord + " " + line));
                    System.out.println(line + " : " + estimate_count);
                    for(String word : confusingWords){
                        double esc = estimator.estimate(tokenHm.get(preWord + " " + word));
                        System.out.println("--- " + word + " : " + esc);
                        if(esc > estimate_count){
                            System.out.println("break");
                            bw.write(sentenceId + ":" + wordId + "\n");
                            break;
                        }
                    }
                }
            }
            if(line.equals("<s>")){
                sentenceId++;
                wordId = 0;
            }
            else wordId++;
            preWord = line;
        }
        System.out.println("hit "+ hit );

        bw.close();
        fw.close();

        br.close();
        fr.close();
	}
}
