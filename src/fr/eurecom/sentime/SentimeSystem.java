package fr.eurecom.sentime;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import en.weimar.webis.SentimentanalysisSemEval;
import en.weimar.webis.Tweet;

public class SentimeSystem {
	
	static String PATH = "";
	private static long startTime = System.currentTimeMillis();
	
	public static void main(String[] args) throws Exception{
		
		String nameOfNRCTrain = "";
		String nameOfGUMLTTrain = "";
		String nameOfKLUETrain = "";
		String nameOfTeamXTrain = "";
		String nameOfTheOne = "";
		int bootstrapNumber = 0;
		Tweet singleTweet;
		//String nameOfTeamXTrain = "";
		int evalmodelmode = 0;
		int trainmodelmode = 0;
		boolean stanford = false;
		boolean without = false;
		boolean nofilter = false;
		boolean score = false;
		Options options = new Options();
		
		options.addOption("on", true, "output Name");
		options.addOption("tf", true, "Name of the NRC Trainfile");
		options.addOption("tf2", true, "Name of the GU-MLT-LT Trainfile");
		options.addOption("tf3", true, "Name of the KLUE Trainfile");
	    options.addOption("tf4", true, "Name of the TeamX Trainfile");
	    options.addOption("trainfile", true, "Name of all the train file");
		options.addOption("em", true, "Eval Modelmode");
		options.addOption("tm", true, "Train Modelmode");
		options.addOption("stanford", true, "Enable Stanford System In Ensemble");
		options.addOption("without", true, "Exclude TeamX");
		options.addOption("bn", true, "Bootstrap number");
		options.addOption("nofilter", true, "Do not filter out the duplicate tweets when reading from the file");
		options.addOption("score", true, "Do not filter out the duplicate tweets when scoring");
		
		CommandLineParser parser = new GnuParser();
		try {
			String name = "";
			CommandLine line = parser.parse(options, args);
			if(line.hasOption("on")){
				name = "_" + line.getOptionValue("on");
			}
			if(line.hasOption("tf")){
				nameOfNRCTrain = line.getOptionValue("tf");
			}
			if(line.hasOption("tf2")){
				nameOfGUMLTTrain = line.getOptionValue("tf2");
			}			
			if(line.hasOption("tf3")){
				nameOfKLUETrain = line.getOptionValue("tf3");
			}
	        if(line.hasOption("tf4")){
	            nameOfTeamXTrain = line.getOptionValue("tf4");
	        }
			if(line.hasOption("em")){
				evalmodelmode = Integer.parseInt(line.getOptionValue("em"));
			}
			if(line.hasOption("tm")){
				trainmodelmode = Integer.parseInt(line.getOptionValue("tm"));
			}
			if(line.hasOption("stanford")){
				stanford = true;
			}
			if(line.hasOption("without")){
				without = true;
			}
			if(line.hasOption("bn")){
				bootstrapNumber = Integer.parseInt(line.getOptionValue("bn"));
			}
			if(line.hasOption("trainfile")){
				nameOfNRCTrain = "Trained-Features-NRC_" + line.getOptionValue("trainfile");
				nameOfGUMLTTrain = "Trained-Features-GUMLTLT_" + line.getOptionValue("trainfile");
				nameOfKLUETrain = "Trained-Features-KLUE_" + line.getOptionValue("trainfile");
				nameOfTeamXTrain = "Trained-Features-TeamX_" + line.getOptionValue("trainfile");
			}
			if(line.hasOption("nofilter")){
				nofilter = true;
			}
			if(line.hasOption("nofilter")){
				score = true;
			}
	
			
			String[] argList = line.getArgs();
			if(argList[0].equals("single")){
				BufferedReader strin=new BufferedReader(new InputStreamReader(System.in));
				System.out.println("Please enter the tweet:"); 
				String str = strin.readLine();
				singleTweet= new Tweet(str, "neutral", "1");
				SentimeRequestHandler sentimentanalysis = new SentimeRequestHandler(singleTweet);
				sentimentanalysis.process(nameOfNRCTrain, nameOfGUMLTTrain, nameOfKLUETrain);
				return;
			}
			
			PATH = argList[1];
			
			SentimeRequestHandler sentimentanalysis = new SentimeRequestHandler(PATH, nofilter, score);
			
			switch (argList[0]){
				case "eval":
					switch(evalmodelmode){
					case 0: nameOfTheOne = nameOfNRCTrain;
					break;
					case 1: nameOfTheOne = nameOfGUMLTTrain;
					break;
					case 2: nameOfTheOne = nameOfKLUETrain;
					break;
					case 3: nameOfTheOne = nameOfTeamXTrain;
					break;
					}
					sentimentanalysis.testSystem(evalmodelmode, nameOfTheOne);
					break;
				case "evalAll":
					sentimentanalysis.testAllSystem(nameOfNRCTrain, nameOfGUMLTTrain, nameOfKLUETrain, nameOfTeamXTrain, stanford, without);
					break;
				case "train":
					sentimentanalysis.trainSystem(trainmodelmode, name);
					break;
				case "trainAll":
					sentimentanalysis.trainAllSystems(trainmodelmode, name);
					break;
				case "bootstrap":
					sentimentanalysis.bootstrapAllSystems(trainmodelmode, name, bootstrapNumber, without);
					break;
				default:
					throw new IllegalArgumentException("Invalid mode: " + argList[0]);
			}					
		}
		catch(ParseException exp){
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}
		long endTime = System.currentTimeMillis();
        System.out.println("It took " + ((endTime - startTime) / 1000) + " seconds");	
	}
	
}
