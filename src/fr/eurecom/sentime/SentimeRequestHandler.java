package fr.eurecom.sentime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import en.weimar.webis.ClassificationResult;
import en.weimar.webis.SentimentSystemGUMLTLT;
import en.weimar.webis.SentimentSystemKLUE;
import en.weimar.webis.SentimentSystemNRC;
import en.weimar.webis.SentimentanalysisSemEval;
import en.weimar.webis.Tweet;

public class SentimeRequestHandler extends SentimentanalysisSemEval {
	
	private Set<Tweet> inputTweet = new HashSet<Tweet>();
	private Set<String> id_cache = new HashSet<String>();

	public SentimeRequestHandler(String path) throws FileNotFoundException, UnsupportedEncodingException {
		super(path);
	}
	
	public SentimeRequestHandler(Tweet sgTweet) {
		this.inputTweet.add(sgTweet);
	}
	
	@Override
	protected void printResultToFile (Map<String, Integer> resultMapToPrint) throws FileNotFoundException {
		int errorcount = 0;
		int multiple = 0;
        Map<Integer, String> classValue = new HashMap<Integer, String>();
        classValue.put(0, "positive");
        classValue.put(1, "neutral");
        classValue.put(2, "negative");
        File file = new File("resources/tweets/" + this.PATH + ".txt");
        PrintStream tweetPrintStream = new PrintStream(new File("output/result.txt"));
        PrintStream tweetPrintStreamError = new PrintStream(new File("output/error_analysis/error.txt"));
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split("\t");
            String id = line[0];
            if (line[0].equals("NA")){
            	id = line[1];
            }
            if(this.id_cache.add(id)){
	            if (line.length == 4 && !line[3].equals("Not Available")){        
	                String senti = classValue.get(resultMapToPrint.get(id));
	                String tell = line[2];
	                if (senti != null){
	                    line[2] = senti;
	                    if (!tell.equals(line[2])){
	                    	String midman = line[1];
	                    	line[1] = "Actual:" + tell;
	                    	line[3] = line[3].toLowerCase();
	                		line[3] = line[3].replaceAll("@[^\\s]+", "");
	                		line[3] = line[3].replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
	                    	tweetPrintStreamError.print(StringUtils.join(line, "\t"));
	                    	tweetPrintStreamError.println();
	                    	line[1] = midman;
	                    } else {
	                    	line[3] = line[3].toLowerCase();
	                		line[3] = line[3].replaceAll("@[^\\s]+", "");
	                		line[3] = line[3].replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
	                		tweetPrintStream.print(StringUtils.join(line, "\t"));
	                        tweetPrintStream.println();
	                    }
	                } else {
	                    System.out.println("Error while printResultToFile: tweetID:" + id);
	                    errorcount++;
	                    line[2] = "neutral";
	                }
	            } else if (line.length == 4 && line[3].equals("Not Available")){
	                errorcount++;
	            } else {
	            	System.out.println(line[0]);
	            }
            } else {
            	multiple ++;
            }
        }
        scanner.close();
        tweetPrintStream.close();
        tweetPrintStreamError.close();
        if (errorcount != 0) System.out.println("Not Available tweets: " + errorcount);
        if (multiple != 0) System.out.println("Multiple Tweets: " + multiple);
	}

	public void process(String trainnameNRC, String trainnameGUMLTLT, String trainnameKLUE) throws Exception{
		String classification = null;
		SentimentSystemNRC nrcSystem = new SentimentSystemNRC(inputTweet);
		Map<String, ClassificationResult> nrcResult = nrcSystem.test(trainnameNRC);

		SentimentSystemGUMLTLT gumltltSystem = new SentimentSystemGUMLTLT(inputTweet);
		Map<String, ClassificationResult> gumltltResult = gumltltSystem.test(trainnameGUMLTLT);

		SentimentSystemKLUE klueSystem = new SentimentSystemKLUE(inputTweet);
		Map<String, ClassificationResult> klueResult = klueSystem.test(trainnameKLUE);
		
		
		if((nrcResult != null && gumltltResult != null && klueResult != null)  && (nrcResult.size() == gumltltResult.size()) && (nrcResult.size() == klueResult.size())){
			for (Map.Entry<String, ClassificationResult> tweet : nrcResult.entrySet()){
				ClassificationResult nRCSenti = tweet.getValue();
				ClassificationResult gUMLTLTSenti = gumltltResult.get(tweet.getKey());
				ClassificationResult kLUESenti = klueResult.get(tweet.getKey());
				if(gUMLTLTSenti != null && kLUESenti != null ){
					double[] useSentiArray = {0,0,0};
					for (int i = 0; i < 3; i++){
						useSentiArray[i] = (nRCSenti.getResultDistribution()[i] + gUMLTLTSenti.getResultDistribution()[i] + kLUESenti.getResultDistribution()[i] ) / 3;
					}
					classification = "Neutral";
					if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
						classification = "Positive";
					}
					if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
						classification = "Negative";
					}
					System.out.println(classification);
				}
			}
		}
	}
	
	@Override
	protected void loadTweets(String path) throws FileNotFoundException, UnsupportedEncodingException{
		File file = new File("resources/tweets/" + path + ".txt");
		PrintStream stanford_file = new PrintStream(new File("resources/file/tmp.txt"));
		Scanner scanner = new Scanner(file);
		int multiple = 0;
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("\t");
			if (line.length == 4){
				if (line[0].equals("NA")){
					if (!storeTweetUni(line[3], line[2], line[1])){
						System.out.println("Tweet already in list: " + line[1]);
						multiple++;
					}
				}
				else{
					if(!line[3].equals("Not Available")){
						if (!storeTweetUni(line[3], line[2], line[0])){
							System.out.println("Tweet already in list: " + line[0]);
							multiple++;
						} else {
							line[3] = line[3].toLowerCase();
	                		line[3] = line[3].replaceAll("@[^\\s]+", "");
	                		line[3] = line[3].replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
	                		line[3] = line[3].trim().replaceAll("\\.+", ";");
	                		line[3] = line[3].replaceAll("!+[^$]", ";");
	                		line[3] = line[3].replaceAll("\\?+[^$]", ";");
	                		line[3] = line[3].replaceAll(";$", "\\.");
	                		line[3] = line[3].replaceAll("!+", "!");
	                		line[3] = line[3].replaceAll("\\?+", "\\?");
	                		if(!line[3].matches(".*!$") && !line[3].matches(".*\\?$") && !line[3].matches(".*\\.$")){
	                			line[3] = line[3] + ".";
	                		}
							stanford_file.println(line[3]);
						}
					}
				}
			}
			else{
			    System.out.println("Wrong format: " + line[0]);
			}
		}
		scanner.close();
		stanford_file.close();
	}
}

