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
	
	public void CreateTrainDataset(String path) throws IOException {
		//File dir = new File("resources/tweets/" + path);

		//System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
		//List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		//for (File file : files) {
		//	System.out.println("file: " + file.getCanonicalPath());
		//}
	       File folder = new File("resources/Amazon-reviews/Amazon-train");
	       //File[] files = folder.listFiles();
	       String[] extensions = new String[] { "xml"};
	       List<File> files = (List<File>) FileUtils.listFiles(folder, extensions, true);
	       for (File file : files) {
	           if (file.isFile() && file.getName().endsWith(".xml")) {
	           	System.out.println("file opened: " + file.getCanonicalPath());
	           	try
	           	{
	           	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        	Document doc = dBuilder.parse(file);
	        	//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	        	NodeList nList = doc.getElementsByTagName("sentence");
				
	        	//System.out.println("----------------------------");	
	        	for (int temp = 0; temp < nList.getLength(); temp++) {

	        		Node nNode = nList.item(temp);
	        				
	        		//System.out.println("\nCurrent Element :" + nNode.getNodeName());
	        				
	        		if (nNode.getNodeType() == Node.ELEMENT_NODE) {

	        			Element eElement = (Element) nNode;
	        			
	        			String str;

	        	        str = eElement.getElementsByTagName("text").item(0).getTextContent();
	        	        str = str.replaceAll("(\\r|\\n|\\t)", "");
	        			
	        			//System.out.println("Sentence id : " + eElement.getAttribute("id"));
	        			//System.out.println("Polarity : " + eElement.getElementsByTagName("polarity").item(0).getTextContent());
	        			//System.out.println("Summary : " + eElement.getElementsByTagName("summary").item(0).getTextContent());
	        			//System.out.println("Domain : " + eElement.getElementsByTagName("domain").item(0).getTextContent());
	        			//System.out.println("Text : " + str);
	        		    File txt = new File("resources/tweets/" + path + ".txt");
	        		    try{
	        		        if(txt.exists()==false){
	        		                System.out.println("We had to make a new file.");
	        		                txt.createNewFile();
	        		        }
	        		        PrintWriter out = new PrintWriter(new FileWriter(txt, true));
	        		        //System.out.println("taggg "+eElement.getElementsByTagName("polarity").item(0).getTextContent());
	        		        out.print("");
	        		        //out.close();
	        		        out.append(eElement.getAttribute("id") + "\t" + eElement.getAttribute("id") + "\t" + eElement.getElementsByTagName("polarity").item(0).getTextContent() + "\t" + str + "\n");	 
	        		        //out.append("\t"+eElement.getElementsByTagName("polarity").item(0).getTextContent()+ "\t" + str + "\n");
	        		        
	        		        out.close();
	        		        //System.out.println("WRITEEEEEEEE DONE");
	        		        }catch(IOException e){
	        		            System.out.println("COULD NOT LOG!!");
	        		        }
	        		}
	        	}
	           	} catch (Exception e) {
	           		e.printStackTrace();
	           	    }
	           	  }
	        	
	               //System.out.println("data saved to: " + "resources/tweets/" + path + ".txt");
	           }
	       }
		
	
	protected void loadTweets(String path, boolean nofilter, boolean xml, int fold) throws IOException{
		File file = new File("resources/Amazon-reviews/" + path + ".txt"); // General case
		
		if (xml == true){
			this.CreateTrainDataset(path); //Creating the .txt file from the folders of .xml files
			file = new File("resources/Amazon-reviews/" + path + ".txt");
			//System.out.println("The file opened ");
		}
		
		if (fold!=0){
			file = new File("resources/Amazon-reviews/cross_validation/" + fold + "/" + path + ".txt"); // For 10 fold cross validation
			System.out.println("The file opened: " + file.getCanonicalPath());
		}
		
		Scanner scanner = new Scanner(file);
		//System.out.println("Scan begun ");
		int multiple = 0;
		int lines = 0;
		while (scanner.hasNextLine()) {
			lines++;
			//System.out.println("Line: " + lines);
			//System.out.println("Scanner next line: " + scanner.hasNextLine());
			String[] line = scanner.nextLine().split("\t");
			if (line.length == 4){
				//System.out.println("Line: " + storeTweetUni(line[3], line[2], line[1]));
				if (line[0].equals("NA")){
					if (!storeTweetUni(line[3], line[2], line[1])){
						//System.out.println("Tweet already in list: " + line[1]);
						multiple++;
					}
				}
				else{
					if(!line[3].equals("Not Available")){
						if (!storeTweetUni(line[3], line[2], line[0])){
							//System.out.println("Tweet already in list: " + line[0]);
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
			else{
			    //System.out.println("Wrong format: " + line[0]);
			}
		}
		//System.out.println("multiple Tweets: " + multiple);
		scanner.close();
	     
	}

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
			//System.out.println("random.nextInt(randomSize): " + random.nextInt(randomSize));
			Tweet tweetTraining = new Tweet(tweetInList.getRawTweetString(), tweetInList.getSentiment(), String.valueOf(i));
			bootstrapTweets.add(tweetTraining);
			
		}
		
		System.out.println("The bootstrap tweets' size: " + bootstrapTweets.size());
		return bootstrapTweets;
	}

	public void bootstrapAllSystems(int system, String savename, int bootstrapnumber, boolean without, int folder) throws IOException, ClassNotFoundException {
		
		int realNumber = bootstrapnumber;
		System.out.println("realNumber......: " + realNumber);
		if(bootstrapnumber==0){
			realNumber = tweetList.size();
		}
		Set<Tweet> bootstrapTweets;
		bootstrapTweets = this.bootstrapTweet(realNumber);
		SentimentSystemNRC nrcSystem = new SentimentSystemNRC(bootstrapTweets);
		//comment
		//
		if (folder != 0 ){
			nrcSystem.train(savename + "_" + folder);
		}
		else{
			nrcSystem.train(savename);
		}
		
		bootstrapTweets = this.bootstrapTweet(realNumber);
		SentimentSystemGUMLTLT gumltltSystem = new SentimentSystemGUMLTLT(bootstrapTweets);
		
		if (folder != 0 ){
			gumltltSystem.train(savename + "_" + folder);
		}
		else{
			gumltltSystem.train(savename);
		}
		
		bootstrapTweets = this.bootstrapTweet(realNumber);
		SentimentSystemKLUE klueSystem = new SentimentSystemKLUE(bootstrapTweets);
		
		if (folder != 0 ){
			klueSystem.train(savename + "_" + folder);
		}
		else{
			klueSystem.train(savename);
		}
		
		if(!without){
			bootstrapTweets = this.bootstrapTweet(realNumber);
			SentimentSystemTeamX teamXSystem = new SentimentSystemTeamX(bootstrapTweets);
			
			if (folder != 0 ){
				teamXSystem.train(savename + "_" + folder);
			}
			else{
				teamXSystem.train(savename);
			}
		}
		
	
	}
	
	
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
			for (Map.Entry<String, ClassificationResult> tweet : resultMap.entrySet()){
				String tweetID = tweet.getKey();
				ClassificationResult senti = tweet.getValue();
				double[] useSentiArray = {0,0,0};
				for (int i = 0; i < 3; i++){
					useSentiArray[i] = (senti.getResultDistribution()[i]);
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
		} else {
			File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".txt");
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				String[] line = scanner.nextLine().split("\t");
				String tweetID = line[0];
	            ClassificationResult senti = resultMap.get(tweetID);
	            double[] useSentiArray = {0,0,0};
				for (int i = 0; i < 3; i++){
					useSentiArray[i] = (senti.getResultDistribution()[i]);
				}
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
	}

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
			this.evalAllModelsWithStanford(nrcResult, gumltltResult, klueResult, teamxResult, stanfordResult);
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
	
	protected void evalAllModelsWithStanford(Map<String, ClassificationResult> nrcResult, Map<String, ClassificationResult> gumltltResult, Map<String, ClassificationResult> klueResult, Map<String, ClassificationResult> teamxResult,Map<String, ClassificationResult> stanfordResult) throws Exception {
		System.out.println("\n\n--------------------\nSentiME System: ");
		double[][] matrix = new double[3][3];
		Map<String, Integer> classValue = new HashMap<String, Integer>();
		classValue.put("positive", 0);
		classValue.put("neutral", 1);
		classValue.put("negative", 2);
	
		Map<String, Integer> resultMapToPrint = new HashMap<String, Integer>();
		if((nrcResult != null && gumltltResult != null && klueResult != null && teamxResult != null && stanfordResult != null)  && (nrcResult.size() == gumltltResult.size()) && (nrcResult.size() == klueResult.size()) && (klueResult.size() == stanfordResult.size()) && (stanfordResult.size() == teamxResult.size())){
			if(!SCORE){
				for (Map.Entry<String, ClassificationResult> tweet : nrcResult.entrySet()){
					String tweetID = tweet.getKey();
					ClassificationResult nRCSenti = tweet.getValue();
					ClassificationResult gUMLTLTSenti = gumltltResult.get(tweet.getKey());
					ClassificationResult kLUESenti = klueResult.get(tweet.getKey());
					ClassificationResult teamxSenti = teamxResult.get(tweet.getKey());
					ClassificationResult stanfordSenti = stanfordResult.get(tweet.getKey());
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
				File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".txt");
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
		printResultToFile(resultMapToPrint);
	}

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
				File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".txt");
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
			System.out.println("resultMaps null or diffrent size");
		}
		if (matrix.length != 0){
			score(matrix);
		}
		printResultToFile(resultMapToPrint);
	}

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
					File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".txt");
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
			printResultToFile(resultMapToPrint);
		}

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
				File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".txt");
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
		printResultToFile(resultMapToPrint);
	}

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

	@Override
	protected void printResultToFile (Map<String, Integer> resultMapToPrint) throws FileNotFoundException {
		int errorcount = 0;
		int multiple = 0;
	    Map<Integer, String> classValue = new HashMap<Integer, String>();
	    classValue.put(0, "positive");
	    classValue.put(1, "neutral");
	    classValue.put(2, "negative");
	    File file = new File("resources/Amazon-reviews/cross_validation/" + this.FOLDER + "/" + this.PATH + ".txt");
	    //PrintStream tweetPrintStream = new PrintStream(new File("output/RightClassification.txt"));
	    //PrintStream tweetPrintStreamError = new PrintStream(new File("output/WrongClassification.txt"));
	    PrintStream scoringFile = new PrintStream(new File("output/result.txt"));
	    //tweetPrintStream.println("    TweetId    Tweet_Number   Golden_Standard            Tweet_Text");
	    //tweetPrintStreamError.println("    TweetId     Golden_Standard   Classification          Tweet_Text");
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
	                    scoringFile.println("NA\t" + line[1] + "\t" + line[2]);
	                    if (!tell.equals(line[2])){
	                    	String midman = line[1];
	                    	line[1] = "GS:" + tell;
	                    	line[3] = line[3].toLowerCase();
	                		line[3] = line[3].replaceAll("@[^\\s]+", "");
	                		line[3] = line[3].replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
	                    	//tweetPrintStreamError.print(StringUtils.join(line, "\t"));
	                    	//tweetPrintStreamError.println();
	                    	line[1] = midman;
	                    } else {
	                    	line[3] = line[3].toLowerCase();
	                		line[3] = line[3].replaceAll("@[^\\s]+", "");
	                		line[3] = line[3].replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
	                		//tweetPrintStream.print(StringUtils.join(line, "\t"));
	                        //tweetPrintStream.println();
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
	        	String senti = classValue.get(resultMapToPrint.get(id));
	        	line[2] = senti;
	        	scoringFile.println("NA\t" + line[1] + "\t" + line[2]);
	        	multiple ++;
	        }
	    }
	    scanner.close();
	    //tweetPrintStream.close();
	    //tweetPrintStreamError.close();
	    scoringFile.close();
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

	protected void stanfordEval(Map<String, ClassificationResult> resultMap) throws Exception {
		System.out.println("Starting eval Model");
		System.out.println("Tweets: " +  tweetList.size());
		double[][] matrix = new double[3][5];
		Map<String, Integer> classValue = new HashMap<String, Integer>();
		classValue.put("positive", 0);
		classValue.put("neutral", 1);
		classValue.put("negative", 2);
		Map<String, Integer> resultMapToPrint = new HashMap<String, Integer>();
		for (Map.Entry<String, ClassificationResult> tweet : resultMap.entrySet()){
			String tweetID = tweet.getKey();
			ClassificationResult senti = tweet.getValue();
			double[] useSentiArray = {0,0,0,0,0};
			for (int i = 0; i < 5; i++){
				useSentiArray[i] = (senti.getResultDistribution()[i]);
			}
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

