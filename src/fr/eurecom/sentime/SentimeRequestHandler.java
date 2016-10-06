package fr.eurecom.sentime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import en.weimar.webis.ClassificationResult;
import en.weimar.webis.SentimentSystemGUMLTLT;
import en.weimar.webis.SentimentSystemKLUE;
import en.weimar.webis.SentimentSystemNRC;
import en.weimar.webis.SentimentSystemTeamX;
import en.weimar.webis.SentimentanalysisSemEval;
import en.weimar.webis.Tweet;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jdk.internal.org.xml.sax.InputSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import weka.core.Instances;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class SentimeRequestHandler extends SentimentanalysisSemEval {
	
	private Set<Tweet> inputTweet = new HashSet<Tweet>();
	private Set<String> id_cache = new HashSet<String>();
	private boolean SCORE;
	private int FOLDER;
	private File result_storage;
	private PrintStream result_stream;

	public SentimeRequestHandler(String path, boolean nofilter, boolean score, boolean xml, int folder) throws IOException {
		this.PATH = path;
		this.FOLDER = folder;
		loadTweets(this.PATH, nofilter, xml, folder);
		this.SCORE = score;
	}
	
	public SentimeRequestHandler(Tweet sgTweet) {
		this.inputTweet.add(sgTweet);
	}
	//Creating .tsv file containing all the elements needed from .xml files (ESWC2016 challenge)
	public void createTrainingDataset(String path) throws IOException {
	       File folder = new File("resources/Amazon-reviews/eval_xml/");
	       String[] extensions = new String[] { "xml"};
	       List<File> files = (List<File>) FileUtils.listFiles(folder, extensions, true);
	       for (File file : files) {
	           if (file.isFile() && file.getName().endsWith(".xml")) {
	           	System.out.println("file has been opened: " + file.getCanonicalPath());
	           	try
	           	{
	           	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        	Document doc = dBuilder.parse(file);
	        	NodeList nList = doc.getElementsByTagName("sentence");
				
	        	for (int temp = 0; temp < nList.getLength(); temp++) {

	        		Node nNode = nList.item(temp);
	        				
	        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {

	        			Element eElement = (Element) nNode;
	        			String str;
	        	        str = eElement.getElementsByTagName("text").item(0).getTextContent();
	        	        str = str.replaceAll("(\\r|\\n|\\t)", "");
	        	        String polarity;
	        	        polarity = eElement.getElementsByTagName("polarity").item(0).getTextContent();
	        	        if (polarity==""){
	        	        	polarity="positive";
	        	        }
	        	        
	        		    File tsv = new File("resources/Amazon-reviews/eval_tsv/" + path + ".tsv");
	        		    try{
	        		        if(tsv.exists()==false){
	        		                System.out.println("We had to make a new file.");
	        		                tsv.createNewFile();
	        		        }
	        		        PrintWriter out = new PrintWriter(new FileWriter(tsv, true));
	        		        out.print("");
	        		        out.append(eElement.getAttribute("id") + "\t" + eElement.getAttribute("id") + "\t" + polarity + "\t" + str + "\n");	 
	        		        
	        		        out.close();
	        		        }catch(IOException e){
	        		            System.out.println("COULD NOT LOG!!");
	        		        }
	        		}
	        	}
	           	} catch (Exception e) {
	           		e.printStackTrace();
	           	    }
	           	  }
	           }
	       }
		
	//Loading all the sentences.
	protected void loadTweets(String path, boolean nofilter, boolean xml, int fold) throws IOException{
		File file = new File("resources/Amazon-reviews/eval_tsv/" + path + ".tsv"); // General case
		if (xml==false && fold == 0 ){
		System.out.println("The tsv file has been opened " + file.getCanonicalPath());
		}
		if (xml == true){
			this.createTrainingDataset(path); //Creating the .tsv file from the folders of .xml files
			//file = new File("resources/Amazon-reviews/eval_tsv/" + path + ".tsv");
			System.out.println("The tsv created from the xml file, opened " + file.getCanonicalPath());
		}
		
		if (fold!=0) {
			//file = new File("resources/Amazon-reviews/cross_validation/" + fold + "/" + path + ".tsv"); // For 10 fold cross validation
			System.out.println("FOLD: " + fold);
			System.out.println("The .tsv file has been openedv: " + file.getCanonicalPath());
		}
		
		Scanner scanner = new Scanner(file);
		int multiple = 0;
		int lines = 0;//counter for lines of the file been scanned
		while (scanner.hasNextLine()) {
			lines++;
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
							if(nofilter){
								storeTweetUni(line[3], line[2], String.valueOf(multiple));
							}
							multiple++;
						} else {
							line[3] = line[3].toLowerCase();
	                		line[3] = line[3].replaceAll("@[^\\s]+", "");
	                		line[3] = line[3].replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
	                		line[3] = line[3].trim();
						}
					}
				}
			}
			else if (line.length == 3){
				//System.out.println("3 column format for SemEval2016: " +line[0]+ line[1] + line[2]);
				if (line[0].equals("NA")){
					if (!storeTweetUni(line[2], line[1], line[0])){
						System.out.println("Tweet already in list: " + line[0]);
						multiple++;
					}
				}
				else{
					if(!line[2].equals("Not Available")){
						if (!storeTweetUni(line[2], line[1], line[0])){
							System.out.println("Tweet already in list: " + line[0]);
							if(nofilter){
								storeTweetUni(line[2], line[1], String.valueOf(multiple));
							}
							multiple++;
						} else {
							line[2] = line[2].toLowerCase();
	                		line[2] = line[2].replaceAll("@[^\\s]+", "");
	                		line[2] = line[2].replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
	                		line[2] = line[2].trim();
						}
					}
				}
			}
			else{
			    System.out.println("Wrong format: " + line[0]);
			}
		}
		System.out.println("multiple Tweets: " + multiple);
		scanner.close();
	     
	}
//Generating bootsrtap dataset.
	private Set<Tweet> bootstrapTweet (int bootstrapnumber) {
		System.out.println("\n-------------------\nGenerating bootstrap tweets");
		Set<Tweet> bootstrapTweets = new HashSet<>();
		Random random = new Random();
		int randomSize = tweetList.size();
		System.out.println("randomSize:"+ randomSize);
		List<Tweet> allTweet = new ArrayList<>(tweetList);
		System.out.println("The original tweets' size: " + allTweet.size());
		
		for (int i = 0; i < bootstrapnumber; i++) {
			Tweet tweetInList = allTweet.get(random.nextInt(randomSize));
			Tweet tweetTraining = new Tweet(tweetInList.getRawTweetString(), tweetInList.getSentiment(), String.valueOf(i));
			bootstrapTweets.add(tweetTraining);
		}
		
		System.out.println("The bootstrap tweets' size: " + bootstrapTweets.size());
		return bootstrapTweets;
	}
	
//Generating bootstrap datasets for all the classifiers 
	public void bootstrapAllSystems(int system, String savename, int bootstrapnumber, boolean without, int folder) throws IOException, ClassNotFoundException {
		
		int realNumber = bootstrapnumber;
		System.out.println("realNumber......: " + realNumber);
		if(bootstrapnumber==0){
			realNumber = tweetList.size();
		}
		Set<Tweet> bootstrapTweets;
		bootstrapTweets = this.bootstrapTweet(realNumber);
		SentimentSystemNRC nrcSystem = new SentimentSystemNRC(bootstrapTweets);
		// for 10 fold cross validation
		if (folder != 0 ){
			nrcSystem.train(savename + "_" + folder);
		}
		else{
			nrcSystem.train(savename);
		}
		bootstrapTweets = this.bootstrapTweet(realNumber);
		SentimentSystemGUMLTLT gumltltSystem = new SentimentSystemGUMLTLT(bootstrapTweets);
		// for 10 fold cross validation
		if (folder != 0 ){
			gumltltSystem.train(savename + "_" + folder);
		}
		else{
			gumltltSystem.train(savename);
		}
		bootstrapTweets = this.bootstrapTweet(realNumber);
		SentimentSystemKLUE klueSystem = new SentimentSystemKLUE(bootstrapTweets);
		// for 10 fold cross validation
		if (folder != 0 ){
			klueSystem.train(savename + "_" + folder);
		}
		else{
			klueSystem.train(savename);
		}
		
		if(!without){
			bootstrapTweets = this.bootstrapTweet(realNumber);
			SentimentSystemTeamX teamXSystem = new SentimentSystemTeamX(bootstrapTweets);
			// for 10 fold cross validation
			if (folder != 0 ){
				teamXSystem.train(savename + "_" + folder);
			}
			else{
				teamXSystem.train(savename);
			}
		}
		
	
	}
	
	// Test a single system
	@Override
	public void testSystem(int system, String trainname) throws Exception {
		switch (system){
			case 0:
				SentimentSystemNRC nrcSystem = new SentimentSystemNRC(tweetList);
				this.evalModel(nrcSystem.test(trainname));
				break;
			case 1:
				SentimentSystemGUMLTLT gumltltSystem = new SentimentSystemGUMLTLT(tweetList);
				this.evalModel(gumltltSystem.test(trainname));
				break;
			case 2:
				SentimentSystemKLUE klueSystem = new SentimentSystemKLUE(tweetList);
				this.evalModel(klueSystem.test(trainname));
				break;
			case 3:
				SentimentSystemTeamX teamXSystem = new SentimentSystemTeamX(tweetList);
				this.evalModel(teamXSystem.test(trainname));
				break;
			case 4:
				SentimentSystemStanford stanfordSystem = new SentimentSystemStanford(tweetList);
				this.evalModel(stanfordSystem.test());
				break;
			case 5:
				SentimentSystemStanford stanfordSystem1 = new SentimentSystemStanford(tweetList);
				this.stanfordEval(stanfordSystem1.standfordTest(trainname));
				break;
			default:
				throw new IllegalArgumentException("Invalid system: " + system);
		}
	}
	
	//Evaluate a single model
	
	protected void evalModel(Map<String, ClassificationResult> resultMap) throws Exception {
		System.out.println("Starting eval Model");
		System.out.println("Tweets: " +  tweetList.size());
		double[][] matrix = new double[3][3];
		Map<String, Integer> classValue = new HashMap<String, Integer>();
		classValue.put("positive", 0);
		classValue.put("neutral", 1);
		classValue.put("negative", 2);
		Map<String, Integer> resultMapToPrint = new HashMap<String, Integer>();
		if(!SCORE){
			//int j=0;
			for (Map.Entry<String, ClassificationResult> tweet : resultMap.entrySet()){
				//j++;
				String tweetID = tweet.getKey();
				ClassificationResult senti = tweet.getValue();
				double[] useSentiArray = {0,0,0};
				for (int i = 0; i < 3; i++){
					useSentiArray[i] = (senti.getResultDistribution()[i]);
				}
				System.out.println("\n------------------------------------");
				int useSenti = 1;
				if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
					useSenti = 0;
				}
				if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
					useSenti = 2;
				}
				
				resultMapToPrint.put(tweetID, useSenti);
				if (!tweet.getValue().getTweet().getSentiment().equals("unknwn")){
					Integer actualSenti = classValue.get(tweet.getValue().getTweet().getSentiment());
					matrix[actualSenti][useSenti]++;
				}
			}
		} else {
			File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".tsv");
			Scanner scanner = new Scanner(file);
			int j=0;
			while (scanner.hasNextLine()) {
				j++;
				System.out.println("Sentence No:" + j);
				String[] line = scanner.nextLine().split("\t");
				String tweetID = line[0];
				System.out.println("tweetID:" + tweetID);
	            ClassificationResult senti = resultMap.get(tweetID);
				System.out.println("tweetID:" + senti);
	            double[] useSentiArray = {0,0,0};
				for (int i = 0; i < 3; i++){
					useSentiArray[i] = (senti.getResultDistribution()[i]);
					System.out.println("getResultDistribution()[i]: " +  senti.getResultDistribution()[i]);
				}
				System.out.println("\n------------------------------------");
				int useSenti = 1;
				if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
					useSenti = 0;
				}
				if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
					useSenti = 2;
				}
				resultMapToPrint.put(tweetID, useSenti);
				if (!senti.getTweet().getSentiment().equals("unknwn")){
					Integer actualSenti = classValue.get(senti.getTweet().getSentiment());
					matrix[actualSenti][useSenti]++;
				}
			}
			scanner.close();
		}
		if (matrix.length != 0){
			score(matrix);
		}
		printResultToFile(resultMapToPrint);
		printResultToXMLFile(resultMapToPrint);
	}
//Select which 5 systems will be tested 
	public void testAllSystem(String trainnameNRC, String trainnameGUMLTLT, String trainnameKLUE, String trainnameTeamX, boolean stanford, boolean without, int folder) throws Exception {
		result_storage = new File("output/result/"+trainnameNRC+this.PATH);
		result_stream = new PrintStream(result_storage);
		SentimentSystemNRC nrcSystem = new SentimentSystemNRC(tweetList);
		Map<String, ClassificationResult> nrcResult = nrcSystem.test(trainnameNRC);

		SentimentSystemGUMLTLT gumltltSystem = new SentimentSystemGUMLTLT(tweetList);
		Map<String, ClassificationResult> gumltltResult = gumltltSystem.test(trainnameGUMLTLT);

		SentimentSystemKLUE klueSystem = new SentimentSystemKLUE(tweetList);
		Map<String, ClassificationResult> klueResult = klueSystem.test(trainnameKLUE);
		
		if(stanford && !without){
			SentimentSystemTeamX teamxSystem = new SentimentSystemTeamX(tweetList);
			Map<String, ClassificationResult> teamxResult = teamxSystem.test(trainnameTeamX);
			SentimentSystemStanford stanfordSystem = new SentimentSystemStanford(tweetList);
			Map<String, ClassificationResult> stanfordResult = stanfordSystem.test();
			this.evalAllModelsWithStanford(nrcResult, gumltltResult, klueResult, teamxResult, stanfordResult, trainnameNRC, trainnameGUMLTLT, trainnameKLUE, trainnameTeamX);
			return;
		}
		
		if(!stanford && without){
			this.evalWithoutTeamX(nrcResult, gumltltResult, klueResult);
			return;
		}
		
		if(!stanford && !without){
			SentimentSystemTeamX teamxSystem = new SentimentSystemTeamX(tweetList);
			Map<String, ClassificationResult> teamxResult = teamxSystem.test(trainnameTeamX);
			this.evalAllModels(nrcResult, gumltltResult, klueResult, teamxResult);
			return;
		}
		
		if (stanford && without) {
			SentimentSystemStanford stanfordSystem = new SentimentSystemStanford(tweetList);
			Map<String, ClassificationResult> stanfordResult = stanfordSystem.test();
			this.evalWithStanfordWithoutTeamX(nrcResult, gumltltResult, klueResult, stanfordResult);
			return;
		}
		
	}
	//Evaluate all 5 systems 
	protected void evalAllModelsWithStanford(Map<String, ClassificationResult> nrcResult, Map<String, ClassificationResult> gumltltResult, Map<String, ClassificationResult> klueResult, Map<String, ClassificationResult> teamxResult,Map<String, ClassificationResult> stanfordResult, String trainnameNRC, String trainnameGUMLTLT, String trainnameKLUE, String trainnameTeamX) throws Exception {		System.out.println("\n\n--------------------\nSentiME System: ");
	double[][] matrix = new double[3][3];
	
	ArrayList<Double> nrcscore = new ArrayList<Double>();
	ArrayList<Double> gumltltscore = new ArrayList<Double>();
	ArrayList<Double> kluescore = new ArrayList<Double>();
	ArrayList<Double> temaxscore = new ArrayList<Double>();
	ArrayList<Double> stanfordscore = new ArrayList<Double>();
	
	Map<String, String> sentence = new HashMap<String, String>();
	
	Map<String, Integer> classValue = new HashMap<String, Integer>();
	classValue.put("positive", 0);
	classValue.put("neutral", 1);
	classValue.put("negative", 2);
	ArrayList<Double> classificationScores =  new ArrayList<Double>();
	Map<String, ArrayList<Double>> resultMapToPrint = new HashMap<String, ArrayList<Double>>();
	if((nrcResult != null && gumltltResult != null && klueResult != null && teamxResult != null && stanfordResult != null)  && (nrcResult.size() == gumltltResult.size()) && (nrcResult.size() == klueResult.size()) && (klueResult.size() == stanfordResult.size()) && (stanfordResult.size() == teamxResult.size())){
		if(!SCORE){
			
			int c=0;
			Map<String, ClassificationResult> treeMap = new TreeMap<String, ClassificationResult>(nrcResult);
			for (Map.Entry<String, ClassificationResult> tweet : treeMap.entrySet()){
				String tweetID = tweet.getKey();

				classificationScores = new ArrayList<Double>();
				
				ClassificationResult nRCSenti = tweet.getValue();
				ClassificationResult gUMLTLTSenti = gumltltResult.get(tweet.getKey());
				ClassificationResult kLUESenti = klueResult.get(tweet.getKey());
				ClassificationResult teamxSenti = teamxResult.get(tweet.getKey());
				ClassificationResult stanfordSenti = stanfordResult.get(tweet.getKey());
				//System.out.println("\nNRC Senti:" + nRCSenti);
				nrcscore.add(nRCSenti.getResult());
				//System.out.println("\nnrc scoreee:" + nrcscore.add(nRCSenti.getResult()));
				gumltltscore.add(gUMLTLTSenti.getResult());
				kluescore.add(kLUESenti.getResult());
				temaxscore.add(teamxSenti.getResult());
				stanfordscore.add(stanfordSenti.getResult());
				
				//System.out.println("NRC"+c+":"+nrcscore.get(c));
		        //System.out.println("Gu"+c+":"+gumltltscore.get(c));
		        //System.out.println("Klue"+c+":"+kluescore.get(c));
		        //System.out.println("TeamX"+c+":"+temaxscore.get(c));
		        //System.out.println("Stanford"+c+":"+stanfordscore.get(c));
				
		        classificationScores.add(nrcscore.get(c));
		        //System.out.println("\nListtttt1:" + classificationScores);
		        classificationScores.add(gumltltscore.get(c));
		        //System.out.println("\nListtttt2:" + classificationScores);
		        classificationScores.add(kluescore.get(c));
		        //System.out.println("\nListtttt3:" + classificationScores);
		        classificationScores.add(temaxscore.get(c));
		        //System.out.println("\nListtttt4:" + classificationScores);
		        classificationScores.add(stanfordscore.get(c));
		        
		        //System.out.println("\nListtttt5:" + classificationScores);
		        
				for (int i = 0; i<3; i++){
					classificationScores.add(nRCSenti.getResultDistribution()[i]);
					//System.out.println("\nListtttt:" + classificationScores);
					
					classificationScores.add(gUMLTLTSenti.getResultDistribution()[i]);
					classificationScores.add(kLUESenti.getResultDistribution()[i]);
					classificationScores.add(teamxSenti.getResultDistribution()[i]);
					classificationScores.add(stanfordSenti.getResultDistribution()[i]);
				}
				//System.out.println("\nNRC Result222:" + nrcDist.get(c));
				
				if(gUMLTLTSenti != null && kLUESenti != null && stanfordSenti != null && teamxSenti != null){
					double[] useSentiArray = {0,0,0};

					
					
					for (int i = 0; i < 3; i++){
						useSentiArray[i] = (nRCSenti.getResultDistribution()[i] + gUMLTLTSenti.getResultDistribution()[i] + kLUESenti.getResultDistribution()[i] + stanfordSenti.getResultDistribution()[i] + teamxSenti.getResultDistribution()[i]) / 5;
					}
					int useSenti = 1;
					if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
						useSenti = 0;
					}
					if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
						useSenti = 2;
					}		
					classificationScores.add(new Double(useSenti));
					//System.out.println("\nListtttt:" + classificationScores);
					//System.out.println("\ntweetID:" + tweetID);
					resultMapToPrint.put(tweetID, classificationScores);
					sentence.put(tweetID, tweet.getValue().getTweet().getTweetString());
					if (!tweet.getValue().getTweet().getSentiment().equals("unknwn")){
						Integer actualSenti = classValue.get(tweet.getValue().getTweet().getSentiment());
						matrix[actualSenti][useSenti]++;
					}
				}
				else{
					System.out.println(tweet.getValue().getTweet().getTweetString());
				}
				c=c+1;
			}
		} else {
			File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".tsv");
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				String[] line = scanner.nextLine().split("\t");
				String tweetID = line[0];
				ClassificationResult nRCSenti = nrcResult.get(tweetID);
				ClassificationResult gUMLTLTSenti = gumltltResult.get(tweetID);
				ClassificationResult kLUESenti = klueResult.get(tweetID);
				ClassificationResult stanfordSenti = stanfordResult.get(tweetID);
				ClassificationResult teamxSenti = teamxResult.get(tweetID);
				if(gUMLTLTSenti != null && kLUESenti != null && stanfordSenti != null && teamxSenti != null){
					double[] useSentiArray = {0,0,0};
					for (int i = 0; i < 3; i++){
						useSentiArray[i] = (nRCSenti.getResultDistribution()[i] + gUMLTLTSenti.getResultDistribution()[i] + kLUESenti.getResultDistribution()[i] + stanfordSenti.getResultDistribution()[i] + teamxSenti.getResultDistribution()[i]) / 5;
					}
					int useSenti = 1;
					if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
						useSenti = 0;
					}
					if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
						useSenti = 2;
					}					
					classificationScores.add(new Double (useSenti));
					resultMapToPrint.put(tweetID, classificationScores);
					if (!nRCSenti.getTweet().getSentiment().equals("unknwn")){
						Integer actualSenti = classValue.get(nRCSenti.getTweet().getSentiment());
						matrix[actualSenti][useSenti]++;
					}
				}
				else{
					System.out.println(nRCSenti.getTweet().getTweetString());
				}
			}
			scanner.close();
		}
	}
	else{
		System.out.println("resultMaps null or different size");
	}
	if (matrix.length != 0){
		score(matrix);
	}
	convertTSVtoXML();
	printResultToFile(resultMapToPrint, sentence, trainnameNRC);
	printResultToXMLFile(resultMapToPrint, sentence);
	}
//Evaluate all 4 models without Stanford
	protected void evalAllModels(Map<String, ClassificationResult> nrcResult, Map<String, ClassificationResult> gumltltResult, Map<String, ClassificationResult> klueResult, Map<String, ClassificationResult> teamxResult) throws Exception {
		System.out.println("\n\n--------------------\nOur Replicate System: ");
		double[][] matrix = new double[3][3];
		Map<String, Integer> classValue = new HashMap<String, Integer>();
		classValue.put("positive", 0);
		classValue.put("neutral", 1);
		classValue.put("negative", 2);
		
		Map<String, Integer> resultMapToPrint = new HashMap<String, Integer>();
		if((nrcResult != null && gumltltResult != null && klueResult != null)  && (nrcResult.size() == gumltltResult.size()) && (nrcResult.size() == klueResult.size()) && (klueResult.size() == teamxResult.size())){
			if(!SCORE){
				for (Map.Entry<String, ClassificationResult> tweet : nrcResult.entrySet()){
					String tweetID = tweet.getKey();
					ClassificationResult nRCSenti = tweet.getValue();
					ClassificationResult gUMLTLTSenti = gumltltResult.get(tweet.getKey());
					ClassificationResult kLUESenti = klueResult.get(tweet.getKey());
					ClassificationResult teamxSenti = teamxResult.get(tweet.getKey());
					if(gUMLTLTSenti != null && kLUESenti != null && teamxResult != null ){
						double[] useSentiArray = {0,0,0};
						for (int i = 0; i < 3; i++){
							useSentiArray[i] = (nRCSenti.getResultDistribution()[i] + gUMLTLTSenti.getResultDistribution()[i] + kLUESenti.getResultDistribution()[i] + teamxSenti.getResultDistribution()[i] ) / 4;
						}
						
						//Weighting the aggregation according to the performance of individual sub-classifiers
						/*
						useSentiArray[0] = (0.7680412371134021*nRCSenti.getResultDistribution()[0] + 0.7430117222723174*gUMLTLTSenti.getResultDistribution()[0] + 0.7629399585921325*kLUESenti.getResultDistribution()[0] + 0.7787418655097614*teamxSenti.getResultDistribution()[0])/(0.7680412371134021+0.7430117222723174+0.7629399585921325+0.7787418655097614);
						useSentiArray[1] = (nRCSenti.getResultDistribution()[1] + gUMLTLTSenti.getResultDistribution()[1] + kLUESenti.getResultDistribution()[1] + teamxSenti.getResultDistribution()[1] ) / 4;
						useSentiArray[2] = (0.5873015873015873*nRCSenti.getResultDistribution()[2] + 0.5072992700729927*gUMLTLTSenti.getResultDistribution()[2] + 0.48013245033112584*kLUESenti.getResultDistribution()[2] + 0.6538461538461539*teamxSenti.getResultDistribution()[2])/(0.5873015873015873+0.5072992700729927+0.48013245033112584+0.6538461538461539);
						*/
						
						int useSenti = 1;
						if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
							useSenti = 0;
						}
						if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
							useSenti = 2;
						}					
						resultMapToPrint.put(tweetID, useSenti);
						if (!tweet.getValue().getTweet().getSentiment().equals("unknwn")){
							Integer actualSenti = classValue.get(tweet.getValue().getTweet().getSentiment());
							matrix[actualSenti][useSenti]++;
						}
					}
					else{
						System.out.println(tweet.getValue().getTweet().getTweetString());
					}
				}
			} else {
				File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".tsv");
				Scanner scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String[] line = scanner.nextLine().split("\t");
					String tweetID = line[0];
					ClassificationResult nRCSenti = nrcResult.get(tweetID);
					ClassificationResult gUMLTLTSenti = gumltltResult.get(tweetID);
					ClassificationResult kLUESenti = klueResult.get(tweetID);
					ClassificationResult teamxSenti = teamxResult.get(tweetID);
					if(gUMLTLTSenti != null && kLUESenti != null && teamxResult != null ){
						double[] useSentiArray = {0,0,0};
						for (int i = 0; i < 3; i++){
							useSentiArray[i] = (nRCSenti.getResultDistribution()[i] + gUMLTLTSenti.getResultDistribution()[i] + kLUESenti.getResultDistribution()[i] + teamxSenti.getResultDistribution()[i] ) / 4;
						}
						
						//Weighting the aggregation according to the performance of individual sub-classifiers
						/*
						useSentiArray[0] = (0.7680412371134021*nRCSenti.getResultDistribution()[0] + 0.7430117222723174*gUMLTLTSenti.getResultDistribution()[0] + 0.7629399585921325*kLUESenti.getResultDistribution()[0] + 0.7787418655097614*teamxSenti.getResultDistribution()[0])/(0.7680412371134021+0.7430117222723174+0.7629399585921325+0.7787418655097614);
						useSentiArray[1] = (nRCSenti.getResultDistribution()[1] + gUMLTLTSenti.getResultDistribution()[1] + kLUESenti.getResultDistribution()[1] + teamxSenti.getResultDistribution()[1] ) / 4;
						useSentiArray[2] = (0.5873015873015873*nRCSenti.getResultDistribution()[2] + 0.5072992700729927*gUMLTLTSenti.getResultDistribution()[2] + 0.48013245033112584*kLUESenti.getResultDistribution()[2] + 0.6538461538461539*teamxSenti.getResultDistribution()[2])/(0.5873015873015873+0.5072992700729927+0.48013245033112584+0.6538461538461539);
						*/
						
						int useSenti = 1;
						if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
							useSenti = 0;
						}
						if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
							useSenti = 2;
						}					
						resultMapToPrint.put(tweetID, useSenti);
						if (!nRCSenti.getTweet().getSentiment().equals("unknwn")){
							Integer actualSenti = classValue.get(nRCSenti.getTweet().getSentiment());
							matrix[actualSenti][useSenti]++;
						}
					}
					else{
						System.out.println(nRCSenti.getTweet().getTweetString());
					}
				}
				scanner.close();
			}
		}
		else{
			System.out.println("resultMaps null or different size");
		}
		if (matrix.length != 0){
			score(matrix);
		}
		convertTSVtoXML();
		printResultToFile(resultMapToPrint);
		printResultToXMLFile(resultMapToPrint);
	}
//Evaluate 3 models excluding TeamX
	protected void evalWithoutTeamX(Map<String, ClassificationResult> nrcResult, Map<String, ClassificationResult> gumltltResult, Map<String, ClassificationResult> klueResult) throws Exception{
			System.out.println("\n\n--------------------\nOur Replicate System Without TeamX");
			double[][] matrix = new double[3][3];
			Map<String, Integer> classValue = new HashMap<String, Integer>();
			classValue.put("positive", 0);
			classValue.put("neutral", 1);
			classValue.put("negative", 2);
			
	//	    Map<Integer, String> classValue2 = new HashMap<Integer, String>();
	//	        classValue2.put(0, "positive");
	//	        classValue2.put(1, "neutral");
	//	        classValue2.put(2, "negative");
			Map<String, Integer> resultMapToPrint = new HashMap<String, Integer>();
			if((nrcResult != null && gumltltResult != null && klueResult != null)  && (nrcResult.size() == gumltltResult.size()) && (nrcResult.size() == klueResult.size())){
				if(!SCORE){
					for (Map.Entry<String, ClassificationResult> tweet : nrcResult.entrySet()){
						String tweetID = tweet.getKey();
						ClassificationResult nRCSenti = tweet.getValue();
						ClassificationResult gUMLTLTSenti = gumltltResult.get(tweet.getKey());
						ClassificationResult kLUESenti = klueResult.get(tweet.getKey());
						if(gUMLTLTSenti != null && kLUESenti != null ){
							double[] useSentiArray = {0,0,0};
							for (int i = 0; i < 3; i++){
								useSentiArray[i] = (nRCSenti.getResultDistribution()[i] + gUMLTLTSenti.getResultDistribution()[i] + kLUESenti.getResultDistribution()[i]) / 3;
							}
							int useSenti = 1;
							if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
								useSenti = 0;
							}
							if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
								useSenti = 2;
							}					
							resultMapToPrint.put(tweetID, useSenti);
							if (!tweet.getValue().getTweet().getSentiment().equals("unknwn")){
								Integer actualSenti = classValue.get(tweet.getValue().getTweet().getSentiment());
								matrix[actualSenti][useSenti]++;
							}
						}
						else{
							System.out.println(tweet.getValue().getTweet().getTweetString());
						}
					}
				} else {
					File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".tsv");
					Scanner scanner = new Scanner(file);
					while (scanner.hasNextLine()) {
						String[] line = scanner.nextLine().split("\t");
						String tweetID = line[0];
						ClassificationResult nRCSenti = nrcResult.get(tweetID);
						ClassificationResult gUMLTLTSenti = gumltltResult.get(tweetID);
						ClassificationResult kLUESenti = klueResult.get(tweetID);
						if(gUMLTLTSenti != null && kLUESenti != null ){
							double[] useSentiArray = {0,0,0};
							for (int i = 0; i < 3; i++){
								useSentiArray[i] = (nRCSenti.getResultDistribution()[i] + gUMLTLTSenti.getResultDistribution()[i] + kLUESenti.getResultDistribution()[i]) / 3;
							}
							int useSenti = 1;
							if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
								useSenti = 0;
							}
							if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
								useSenti = 2;
							}					
							resultMapToPrint.put(tweetID, useSenti);
							if (!nRCSenti.getTweet().getSentiment().equals("unknwn")){
								Integer actualSenti = classValue.get(nRCSenti.getTweet().getSentiment());
								matrix[actualSenti][useSenti]++;
							}
						}
						else{
							System.out.println(nRCSenti.getTweet().getTweetString());
						}
					}
					scanner.close();
				}
			}
			else{
				System.out.println("resultMaps null or diffrent size");
			}
			if (matrix.length != 0){
				score(matrix);
			}
			convertTSVtoXML();
			printResultToFile(resultMapToPrint);
			printResultToXMLFile(resultMapToPrint);
		}
//Evaluating 4 systems including Stanford , excluding TeamX
	protected void evalWithStanfordWithoutTeamX(Map<String, ClassificationResult> nrcResult, Map<String, ClassificationResult> gumltltResult, Map<String, ClassificationResult> klueResult, Map<String, ClassificationResult> stanfordResult) throws Exception{
		System.out.println("\n\n--------------------\nOur Sentime System Without TeamX");
		double[][] matrix = new double[3][3];
		Map<String, Integer> classValue = new HashMap<String, Integer>();
		classValue.put("positive", 0);
		classValue.put("neutral", 1);
		classValue.put("negative", 2);
		
		Map<String, Integer> resultMapToPrint = new HashMap<String, Integer>();
		if((nrcResult != null && gumltltResult != null && klueResult != null)  && (nrcResult.size() == gumltltResult.size()) && (nrcResult.size() == klueResult.size()) && (klueResult.size() == stanfordResult.size())){
			if(!SCORE){
				for (Map.Entry<String, ClassificationResult> tweet : nrcResult.entrySet()){
					String tweetID = tweet.getKey();
					ClassificationResult nRCSenti = tweet.getValue();
					ClassificationResult gUMLTLTSenti = gumltltResult.get(tweet.getKey());
					ClassificationResult kLUESenti = klueResult.get(tweet.getKey());
					ClassificationResult stanfordSenti = stanfordResult.get(tweet.getKey());
					if(gUMLTLTSenti != null && kLUESenti != null && stanfordSenti != null ){
						double[] useSentiArray = {0,0,0};
						for (int i = 0; i < 3; i++){
							useSentiArray[i] = (nRCSenti.getResultDistribution()[i] + gUMLTLTSenti.getResultDistribution()[i] + kLUESenti.getResultDistribution()[i] + stanfordSenti.getResultDistribution()[i]) / 4;
						}
						int useSenti = 1;
						if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
							useSenti = 0;
						}
						if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
							useSenti = 2;
						}					
						resultMapToPrint.put(tweetID, useSenti);
						if (!tweet.getValue().getTweet().getSentiment().equals("unknwn")){
							Integer actualSenti = classValue.get(tweet.getValue().getTweet().getSentiment());
							matrix[actualSenti][useSenti]++;
						}
					}
					else{
						System.out.println(tweet.getValue().getTweet().getTweetString());
					}
				}
			} else {
				File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".tsv");
				Scanner scanner = new Scanner(file);
				while (scanner.hasNextLine()) {
					String[] line = scanner.nextLine().split("\t");
					String tweetID = line[0];
					ClassificationResult nRCSenti = nrcResult.get(tweetID);
					ClassificationResult gUMLTLTSenti = gumltltResult.get(tweetID);
					ClassificationResult kLUESenti = klueResult.get(tweetID);
					ClassificationResult stanfordSenti = stanfordResult.get(tweetID);
					if(gUMLTLTSenti != null && kLUESenti != null && stanfordSenti != null ){
						double[] useSentiArray = {0,0,0};
						for (int i = 0; i < 3; i++){
							useSentiArray[i] = (nRCSenti.getResultDistribution()[i] + gUMLTLTSenti.getResultDistribution()[i] + kLUESenti.getResultDistribution()[i] + stanfordSenti.getResultDistribution()[i]) / 4;
						}
						int useSenti = 1;
						if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2]){
							useSenti = 0;
						}
						if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1]){
							useSenti = 2;
						}					
						resultMapToPrint.put(tweetID, useSenti);
						if (!nRCSenti.getTweet().getSentiment().equals("unknwn")){
							Integer actualSenti = classValue.get(nRCSenti.getTweet().getSentiment());
							matrix[actualSenti][useSenti]++;
						}
					}
					else{
						System.out.println(nRCSenti.getTweet().getTweetString());
					}
				}
				scanner.close();
			}
		}
		else{
			System.out.println("resultMaps null or diffrent size");
		}
		if (matrix.length != 0){
			score(matrix);
		}
		convertTSVtoXML();
		printResultToFile(resultMapToPrint);
		printResultToXMLFile(resultMapToPrint);
	}
//SemEval2015 scoring
	protected void score(double[][] matrix){
		double precisionA = matrix[0][0] / (matrix[0][0] + matrix[1][0] + matrix[2][0]);
		double precisionB = matrix[1][1] / (matrix[1][1] + matrix[2][1] + matrix[0][1]);
		double precisionC = matrix[2][2] / (matrix[2][2] + matrix[0][2] + matrix[1][2]);

		double precision = (precisionA + precisionB + precisionC) / 3;
		
		double recallA = matrix[0][0] / (matrix[0][0] + matrix[0][1] + matrix[0][2]);
		double recallB = matrix[1][1] / (matrix[1][1] + matrix[1][2] + matrix[1][0]);
		double recallC = matrix[2][2] / (matrix[2][2] + matrix[2][0] + matrix[2][1]);
		
		double recall = (recallA + recallB + recallC) / 3;
		
		double f1 = 2 * ((precision * recall) / (precision + recall));
		double f1A = 2 * ((precisionA * recallA) / (precisionA + recallA));
//		double f1B = 2 * ((precisionB * recallB) / (precisionB + recallB));
		double f1C = 2 * ((precisionC * recallC) / (precisionC + recallC));
		
		System.out.println(matrix[0][0] +  " | " + matrix[0][1] + " | " + matrix[0][2]);
		System.out.println(matrix[1][0] +  " | " + matrix[1][1] + " | " + matrix[1][2]);
		System.out.println(matrix[2][0] +  " | " + matrix[2][1] + " | " + matrix[2][2]);
		System.out.println("precision: " + precision);
		System.out.println("recall: " + recall);
		System.out.println("f1: " + f1);
		System.out.println("f1 without neutral: " + (f1A + f1C) /2);
		System.out.println("precisionPos: " + precisionA);
		System.out.println("recallPos: " + recallA);
		System.out.println("precisionNeg: " + precisionC);
		System.out.println("recallNeg: " + recallC);
		result_stream.println(matrix[0][0] +  "\t" + matrix[0][1] + "\t" + matrix[0][2]);
		result_stream.println(matrix[1][0] +  "\t" + matrix[1][1] + "\t" + matrix[1][2]);
		result_stream.println(matrix[2][0] +  "\t" + matrix[2][1] + "\t" + matrix[2][2]);
		result_stream.println(precision);
		result_stream.println(recall);
		result_stream.println(f1);
		result_stream.println((f1A + f1C) /2);
		result_stream.println(precisionA);
		result_stream.println(recallA);
		result_stream.println(precisionC);
		result_stream.println(recallC);
		result_stream.close();
	}
	
	//Pre-process sentences.Then prints results to file. 
	protected void printResultToFile (Map<String, ArrayList<Double>> resultMapToPrint, Map<String, String> sentence, String trainnameNRC) throws Exception {
		int errorcount = 0;
		int multiple = 0;
	    Map<Double, String> classValue = new HashMap<Double, String>();
	    classValue.put(0.0, "positive");
	    classValue.put(1.0, "neutral");
	    classValue.put(2.0, "negative");
	    File file = new File("resources/Amazon-reviews/eval_tsv/" + this.PATH + ".tsv");
	    //Comments below are for testing reasons. Right and wrong classified sentences categorized into 2 files for error analysis.
	    PrintStream tweetPrintStream = new PrintStream(new File("output/SentiMEa/"+ trainnameNRC + "_" + this.PATH +"_RightClassification.tsv"));
	    PrintStream tweetPrintStreamError = new PrintStream(new File("output/SentiMEa/"+ trainnameNRC+ "_" + this.PATH +"_WrongClassification.tsv"));
	    PrintStream classifiersScores = new PrintStream(new File("output/"+ trainnameNRC+ "_" + this.PATH +"_Classifiers_scores+GS.tsv"));
	    PrintStream scoringFile = new PrintStream(new File("output/result.tsv"));
	    tweetPrintStream.println("TweetId	Golden_Standard		NRC R	NRC_POS	NRC_NEU	NRC_NEG		GUMLTLT_R	GUMLTLT_POS	GUMLTLT_NEU	GUMLTLT_NEG		KLUE_R	KLUE_POS	KLUE_NEU	KLUE_NEG	TeamX_R	TeamX_R	TeamX_POS	TeamX_NEU	TeamX_NEG	Stanford_R	Stanford_POS	Stanford_NEU	Stanford_NEG	  	Tweet_Text");
	    tweetPrintStreamError.println("    TweetId    Golden_Standard   Classification  NRC	GUMLTLT		KLUE	TeamX	Stanford      Tweet_Text");
	    Scanner scanner = new Scanner(file);
	    
	    Map<String, ArrayList<Double>> treeMap1 = new TreeMap<String, ArrayList<Double>>(resultMapToPrint);
	    Map<String, String> treeMap2 = new TreeMap<String, String>(sentence);

	    int c=0;
	    while (scanner.hasNextLine()) {
	        String[] line = scanner.nextLine().split("\t");
	        String id = line[0];
	        //System.out.println("line 0000"+c+":"+line[0]);
	        if (line[0].equals("NA")){
	        	id = line[1];
	        }
	        //System.out.println("1stttt"+c+":"+treeMap1.get(id).get(20));
	        //System.out.println("Sentence"+c+":"+treeMap2.get(id));
	        //System.out.println("Klue"+c+":"+kluescore.get(c));
	        //System.out.println("TeamX"+c+":"+temaxscore.get(c));
	        //System.out.println("Stanford"+c+":"+stanfordscore.get(c));
	        if(this.id_cache.add(id)){
	            if (line.length == 4 && !line[3].equals("Not Available")){        
	                String senti = classValue.get(treeMap1.get(id).get(20));
	                String tell = line[2];
	                
	                //System.out.println("classValue.get(treeMap1.get(id).get(20):"+classValue.get(treeMap1.get(id).get(20)));
	                //System.out.println("resultMapToPrint.get(id):"+ resultMapToPrint.get(id));
	                //System.out.println("treeMap1.get(id):"+treeMap1.get(id));
	                //System.out.println("Sentiiiiiii:"+senti);
	                if (senti != null){
	                    line[2] = senti;
	                    scoringFile.println("NA\t" + line[1] + "\t" + line[2]);
	                    if (!tell.equals(line[2])){
	                    	String midman = line[1];
	                    	line[1] = "GS:" + tell;
	                    	line[3] = line[3].toLowerCase();
	                		line[3] = line[3].replaceAll("@[^\\s]+", "");
	                		line[3] = line[3].replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
	                		
	                		//error analysis code
	                    	tweetPrintStreamError.print(line[0] + "\t" + tell + "\t" + line[2] + "\t" );
	                    	
	                    	//NRC result + NRC positive distribution + NRC neutral distribution + NRC negative distribution
	                    	tweetPrintStreamError.print(treeMap1.get(id).get(0) + "\t" + treeMap1.get(id).get(5) + "\t" + treeMap1.get(id).get(10) + "\t" + treeMap1.get(id).get(15) + "\t");
	                    	//GUMLT-LT
	                    	tweetPrintStreamError.print(treeMap1.get(id).get(1) + "\t" + treeMap1.get(id).get(6) + "\t" + treeMap1.get(id).get(11) + "\t" + treeMap1.get(id).get(16) + "\t");
	                    	//KLUE
	                    	tweetPrintStreamError.print(treeMap1.get(id).get(2) + "\t" + treeMap1.get(id).get(7) + "\t" + treeMap1.get(id).get(12) + "\t" + treeMap1.get(id).get(17) + "\t");
	                    	//TeamX
	                    	tweetPrintStreamError.print(treeMap1.get(id).get(3) + "\t" + treeMap1.get(id).get(8) + "\t" + treeMap1.get(id).get(13) + "\t" + treeMap1.get(id).get(18) + "\t");
	                    	//Stanford
	                    	tweetPrintStreamError.print(treeMap1.get(id).get(4) + "\t" + treeMap1.get(id).get(9) + "\t" + treeMap1.get(id).get(14) + "\t" + treeMap1.get(id).get(19) + "\t");
	                    	
	                    	tweetPrintStreamError.print(treeMap2.get(id));
	                    	tweetPrintStreamError.println();
	                    	
	                    	line[1] = midman;
	                    } else {
	                    	line[3] = line[3].toLowerCase();
	                		line[3] = line[3].replaceAll("@[^\\s]+", "");
	                		line[3] = line[3].replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
	                		
	                		tweetPrintStream.print(line[0] + "\t" + line[2] + "\t" );
	                		
	                		tweetPrintStream.print("NRC R:" + treeMap1.get(id).get(0) + "\t");
	                		tweetPrintStream.print("NRC P:" + treeMap1.get(id).get(5) + "\t");
	                    	tweetPrintStream.print("NRC NEU:" + treeMap1.get(id).get(10) + "\t");
	                    	tweetPrintStream.print("NRC NEG:" + treeMap1.get(id).get(15) + "\t");
	                    	
	                    	
	                    	tweetPrintStream.print("GUMLTLT R:" + treeMap1.get(id).get(1) + "\t");
	                    	tweetPrintStream.print("GUMLTLT P:" + treeMap1.get(id).get(6) + "\t");
	                    	tweetPrintStream.print("GUMLTLT NEU:" + treeMap1.get(id).get(11) + "\t");
	                    	tweetPrintStream.print("GUMLTLT NEG:" + treeMap1.get(id).get(16) + "\t");
	                    	
	                    	tweetPrintStream.print("KLUE R:" + treeMap1.get(id).get(2) + "\t");
	                    	tweetPrintStream.print("KLUE P:" + treeMap1.get(id).get(7) + "\t");
	                    	tweetPrintStream.print("KLUE NEU:" + treeMap1.get(id).get(12) + "\t");
	                    	tweetPrintStream.print("KLUE NEG:" + treeMap1.get(id).get(17) + "\t");
	                    	
	                    	tweetPrintStream.print("TeamX R:" + treeMap1.get(id).get(3) + "\t");
	                    	tweetPrintStream.print("TeamX P:" + treeMap1.get(id).get(8) + "\t");
	                    	tweetPrintStream.print("TeamX NEU:" + treeMap1.get(id).get(13) + "\t");
	                    	tweetPrintStream.print("TeamX NEG:" + treeMap1.get(id).get(18) + "\t");
	                    	
	                    	tweetPrintStream.print("Stanford R:" + treeMap1.get(id).get(4) + "\t");
	                    	tweetPrintStream.print("Stanford P:" + treeMap1.get(id).get(9) + "\t");
	                    	tweetPrintStream.print("Stanford NEU:" + treeMap1.get(id).get(14) + "\t");
	                    	tweetPrintStream.print("Stanford NEG:" + treeMap1.get(id).get(19) + "\t");
	                    	
	                    	tweetPrintStream.print(treeMap2.get(id));
	                    	tweetPrintStream.println();
	                    }
	                } else {
	                    System.out.println("Error while printResultToFile: tweetID:" + id);
	                    errorcount++;
	                    line[2] = "neutral";
	                }
	                //code for stacking experiment
                	//creating a tsv file having all the classification of each classifier + the gold standard of the sentence
                	/*classifiersScores.print(treeMap1.get(id).get(0) + "\t");
                	classifiersScores.print(treeMap1.get(id).get(1) + "\t");
                	classifiersScores.print(treeMap1.get(id).get(2) + "\t");
                	classifiersScores.print(treeMap1.get(id).get(3) + "\t");
                	classifiersScores.print(treeMap1.get(id).get(4) + "\t");
                	classifiersScores.print(tell + "\n");
                	*/
                	//creating binary output for SVM
	                for(int t=0;t<=4;t++){
                		if (treeMap1.get(id).get(t)== 0.0){
                			classifiersScores.print( 1 + "\t" + 0 + "\t" + 0 + "\t");
                		}
                		else if (treeMap1.get(id).get(t)== 1.0){
                			classifiersScores.print( 0 + "\t" + 1 + "\t" + 0 + "\t");
                		}
                		else if (treeMap1.get(id).get(t)== 2.0){
                			classifiersScores.print( 0 + "\t" + 0 + "\t" + 1 + "\t");
                		}
                		if(t==4){
                			classifiersScores.print(tell + "\n");
                		}
                	}
	            } else if (line.length == 4 && line[3].equals("Not Available")){
	                errorcount++;
	            } else {
	            	System.out.println(line[0]);
	            }
	        } else {
	        	String senti = classValue.get(resultMapToPrint.get(id));
	        	line[2] = senti;
	        	scoringFile.println("NA\t" + line[1] + "\t" + line[2]);
	        	multiple ++;
	        }
	        c=c+1;
	    }
	    scanner.close();
	    tweetPrintStream.close();
	    tweetPrintStreamError.close();
	    classifiersScores.close();
	    scoringFile.close();
	    if (errorcount != 0) System.out.println("Not Available tweets: " + errorcount);
	    if (multiple != 0) System.out.println("Multiple Tweets: " + multiple);
	}
//Prints results to a .xml file. (ESWC2016 challenge)
	protected void printResultToXMLFile (Map<String, ArrayList<Double>> resultMapToPrint, Map<String, String> sentence) throws FileNotFoundException {
		int errorcount = 0;
		int multiple = 0;
	    Map<Integer, String> classValue = new HashMap<Integer, String>();
	    classValue.put(0, "positive");
	    classValue.put(1, "neutral");
	    classValue.put(2, "negative");
	    File file = new File("resources/Amazon-reviews/eval_tsv/" + this.PATH + ".tsv");
	    //Comments below are for testing reasons. Right and wrong classified sentences categorized into 2 files for error analysis.
	    //PrintStream tweetPrintStream = new PrintStream(new File("output/"+ this.PATH +"_RightClassification.tsv"));
	    //PrintStream tweetPrintStreamError = new PrintStream(new File("output/"+ this.PATH +"_WrongClassification.tsv"));
	    PrintStream scoringFile = new PrintStream(new File("resources/Amazon-reviews/output_xml/" + this.PATH + "_CL" + ".xml"));
	    //tweetPrintStream.println("    TweetId    Tweet_Number   Golden_Standard            Tweet_Text");
	    //tweetPrintStreamError.println("    TweetId     Golden_Standard   Classification          Tweet_Text");
	    Scanner scanner = new Scanner(file);
	    scoringFile.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        scoringFile.println("<Sentences>");
	    while (scanner.hasNextLine()) {
	        String[] line = scanner.nextLine().split("\t");
	        String escapedXml = StringEscapeUtils.escapeXml(line[3]);
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
	                    scoringFile.println("\t<sentence id="+"\""+line[0]+"\""+">");
	                    scoringFile.println("\t\t<text>");
	                    scoringFile.println("\t\t\t"+escapedXml);
	                    scoringFile.println("\t\t</text>");
	                    scoringFile.println("\t\t<polarity>");
	                    scoringFile.println("\t\t"+line[2]);
	                    scoringFile.println("\t\t</polarity>");
	                    scoringFile.println("\t</sentence>");
	                } else {
	                    System.out.println("Error while printResultToXMLFile: tweetID:" + id);
	                    errorcount++;
	                    line[2] = "neutral";
	                }
	            } else if (line.length == 4 && line[3].equals("Not Available")){
	                errorcount++;
	            } else {
	            	System.out.println(line[0]);
	            }
	        } else {
	        	String senti = classValue.get(resultMapToPrint.get(id));
	        	line[2] = senti;
                scoringFile.println("\t<sentence id="+"\""+line[0]+"\""+">");
                scoringFile.println("\t\t<text>");
                scoringFile.println("\t\t\t"+escapedXml);
                scoringFile.println("\t\t</text>");
                scoringFile.println("\t\t<polarity>");
                scoringFile.println("\t\t"+line[2]);
                scoringFile.println("\t\t</polarity>");
                scoringFile.println("\t</sentence>");
	        	//multiple ++;
	        }
	    }
	    scoringFile.println("</Sentences>");
	    scanner.close();
	    //tweetPrintStream.close();
	    //tweetPrintStreamError.close();
	    scoringFile.close();
	    if (errorcount != 0) System.out.println("Not Available tweets: " + errorcount);
	    //if (multiple != 0) System.out.println("Multiple Tweets: " + multiple);
	}
	
	//Converts .tsv Golden Standard to .xml Golden Standard form (ESWC2016 challenge) 
		protected void convertTSVtoXML () throws FileNotFoundException {
			//convert tsv golden standard file to xml golden standard file
		    File file = new File("resources/Amazon-reviews/eval_tsv/" + this.PATH + ".tsv");
		    PrintStream convertedfile = new PrintStream(new File("resources/Amazon-reviews/output_xml/" + this.PATH +"_GS"+ ".xml"));
		    Scanner scanner = new Scanner(file);
		    convertedfile.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		    convertedfile.println("<Sentences>");
		    while (scanner.hasNextLine()) {
		        String[] line = scanner.nextLine().split("\t");
		        String escapedXml = StringEscapeUtils.escapeXml(line[3]);
		        convertedfile.println("\t<sentence id=\""+line[0]+"\">");
		        convertedfile.println("\t\t<text>");
		        convertedfile.println("\t\t\t"+escapedXml);
		        convertedfile.println("\t\t</text>");
		        convertedfile.println("\t\t<polarity>");
		        convertedfile.println("\t\t"+line[2]);
		        convertedfile.println("\t\t</polarity>");
		        convertedfile.println("\t</sentence>");
		        
		    }
		    convertedfile.println("</Sentences>");
	                scanner.close();
	                convertedfile.close();
		}
	
	
	public void process(String trainnameNRC, String trainnameGUMLTLT, String trainnameKLUE, String trainnameTeamX) throws Exception{
		String classification = null;
		SentimentSystemNRC nrcSystem = new SentimentSystemNRC(inputTweet);
		Map<String, ClassificationResult> nrcResult = nrcSystem.test(trainnameNRC);
	
		SentimentSystemGUMLTLT gumltltSystem = new SentimentSystemGUMLTLT(inputTweet);
		Map<String, ClassificationResult> gumltltResult = gumltltSystem.test(trainnameGUMLTLT);
	
		SentimentSystemKLUE klueSystem = new SentimentSystemKLUE(inputTweet);
		Map<String, ClassificationResult> klueResult = klueSystem.test(trainnameKLUE);
		
		SentimentSystemTeamX teamxSystem = new SentimentSystemTeamX(tweetList);
		Map<String, ClassificationResult> teamxResult = teamxSystem.test(trainnameTeamX);
				
		SentimentSystemStanford stanfordSystem = new SentimentSystemStanford(tweetList);
		Map<String, ClassificationResult> stanfordResult = stanfordSystem.test();
		
		if((nrcResult != null && gumltltResult != null && klueResult != null)  && (nrcResult.size() == gumltltResult.size()) && (nrcResult.size() == klueResult.size())){
			for (Map.Entry<String, ClassificationResult> tweet : nrcResult.entrySet()){
				ClassificationResult nRCSenti = tweet.getValue();
				ClassificationResult gUMLTLTSenti = gumltltResult.get(tweet.getKey());
				ClassificationResult kLUESenti = klueResult.get(tweet.getKey());
				//ClassificationResult teamxSenti = teamxResult.get(tweet.getKey());
				//ClassificationResult stanfordSenti = stanfordResult.get(tweet.getValue());
				
				if( gUMLTLTSenti != null && kLUESenti != null  ){
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
//Original Stanford Sentiment System Evaluation
	protected void stanfordEval(Map<String, ClassificationResult> resultMap) throws Exception {
		System.out.println("Starting eval Model");
		System.out.println("Tweets: " +  tweetList.size());
		double[][] matrix = new double[3][5];
		Map<String, Integer> classValue = new HashMap<String, Integer>();
		classValue.put("positive", 0);
		classValue.put("neutral", 1);
		classValue.put("negative", 2);
		Map<String, Integer> resultMapToPrint = new HashMap<String, Integer>();
		//int j=0;
		for (Map.Entry<String, ClassificationResult> tweet : resultMap.entrySet()){
			//j++;
			//System.out.println("Sentence No:" + j);
			String tweetID = tweet.getKey();
			ClassificationResult senti = tweet.getValue();
			double[] useSentiArray = {0,0,0,0,0};
			for (int i = 0; i < 5; i++){
				useSentiArray[i] = (senti.getResultDistribution()[i]);
				//System.out.println("senti.getResultDistribution()["+i+"]:" + senti.getResultDistribution()[i]);
			}
			System.out.println("\n------------------------------------");
			int useSenti = 1;
			if(useSentiArray[0] > useSentiArray[1] && useSentiArray[0] > useSentiArray[2] && useSentiArray[0] > useSentiArray[3] && useSentiArray[0] > useSentiArray[4]){
				useSenti = 0;
			}
			if(useSentiArray[2] > useSentiArray[0] && useSentiArray[2] > useSentiArray[1] && useSentiArray[2] > useSentiArray[3] && useSentiArray[2] > useSentiArray[4]){
				useSenti = 2;
			}
			if(useSentiArray[3] > useSentiArray[0] && useSentiArray[3] > useSentiArray[1] && useSentiArray[3] > useSentiArray[2] && useSentiArray[3] > useSentiArray[4]){
				useSenti = 3;
			}
			if(useSentiArray[4] > useSentiArray[0] && useSentiArray[4] > useSentiArray[1] && useSentiArray[4] > useSentiArray[2] && useSentiArray[4] > useSentiArray[3]){
				useSenti = 4;
			}
			resultMapToPrint.put(tweetID, useSenti);
			if (!tweet.getValue().getTweet().getSentiment().equals("unknwn")){
				Integer actualSenti = classValue.get(tweet.getValue().getTweet().getSentiment());
				matrix[actualSenti][useSenti]++;
			}
		}
		if (matrix.length != 0){
			System.out.println(matrix[0][0] +  " | " + matrix[0][1] + " | " + matrix[0][2] + " | " + matrix[0][3] + " | " + matrix[0][4]);
			System.out.println(matrix[1][0] +  " | " + matrix[1][1] + " | " + matrix[1][2] + " | " + matrix[1][3] + " | " + matrix[1][4]);
			System.out.println(matrix[2][0] +  " | " + matrix[2][1] + " | " + matrix[2][2] + " | " + matrix[2][3] + " | " + matrix[2][4]);
		}
	}
	
}

