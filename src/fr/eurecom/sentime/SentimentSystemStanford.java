package fr.eurecom.sentime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import en.weimar.webis.ClassificationResult;
import en.weimar.webis.Tweet;

public class SentimentSystemStanford {

	private Set<Tweet> tweetList;
	
	public SentimentSystemStanford(Set<Tweet> tweetList) {
		this.tweetList = tweetList;
	}
	//Test Stanford Sentiment System for 3 and 2 polarities. Change of mapping.
	@SuppressWarnings("null")
	public Map<String,ClassificationResult> test() throws Exception{
		System.out.println("Starting Stanford Test");
		System.out.println("Tweets: " +  this.tweetList.size());
		Map<String, ClassificationResult> results = new HashMap<>();
		
		// Build the Pipeline for Tokenize and Conversion.
		Properties pipelineProps = new Properties();
	    Properties tokenizerProps = new Properties();
	    pipelineProps.setProperty("annotators", "parse, sentiment");
	    pipelineProps.setProperty("enforceRequirements", "false");
	    pipelineProps.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishRNN.ser.gz");
	    pipelineProps.setProperty("sentiment.model", "edu/stanford/nlp/models/sentiment/sentiment.ser.gz");
	    tokenizerProps.setProperty("annotators", "tokenize, ssplit");
	    tokenizerProps.setProperty("ssplit.eolonly", "true");
	    StanfordCoreNLP tokenizer = new StanfordCoreNLP(tokenizerProps);
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(pipelineProps);
	    int i=0;
	    for (Tweet tweet: tweetList){
	    	i++;
	    	System.out.println("Sentence No:" +  i);
	    	preProcessTweet(tweet);
	    	Annotation annotation = tokenizer.process(tweet.getTweetString());
	    	pipeline.annotate(annotation);
	        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	        	Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
	        	SimpleMatrix vector = RNNCoreAnnotations.getPredictions(tree);
	        	double result = 10;
	        	double[] resultDistribution = new double[3];
	        	switch(sentence.get(SentimentCoreAnnotations.SentimentClass.class).toLowerCase()){
	        	case "positive":
	        		result = 0;
	        		break;
	        	case "very positive":
	        		result = 0;
	        		break;
	        	case "neutral":
	        		result = 1;
	        		break;
	        	case "negative":
	        		result = 2;
	        		break;
	        	case "very negative":
	        		result = 2;
	        		break;
	        	}
	        	
	        	//mapping for 3 classifications
	        	
	        	resultDistribution[2] = vector.get(0)+vector.get(1);
	        	resultDistribution[1] = vector.get(2);
	        	resultDistribution[0] = vector.get(3) + vector.get(4);
	        	
	        	//System.out.println("Positive confident:"+resultDistribution[2]);
	        	//System.out.println("Neutral confident:"+resultDistribution[1]);
	        	//System.out.println("Negative confident:"+resultDistribution[0]);
	        	//System.out.println("Stanford confident:"+result);
	        	//mapping for binary classification
	        	//hybrid neutral 
	        	/*
	        	if(vector.get(2) > vector.get(0) && vector.get(2) > vector.get(1) && vector.get(2) > vector.get(3) && vector.get(2) > vector.get(4)){
	        		resultDistribution[1] = 0;
	        		if(resultDistribution[0] > resultDistribution[2]){
	        			resultDistribution[0] = vector.get(2) + vector.get(3) + vector.get(4);
	        		}
	        		else{
	        			resultDistribution[2] = vector.get(0) + vector.get(1) + vector.get(2);
	        		}
	        	}
	        	
	        	//System.out.println("result:"+result);
	        	*/
	        	results.put(tweet.getTweetID(), new ClassificationResult(tweet, resultDistribution, result));
	        }
	    }
	    return results;
	}
	//Test original Stanford Sentiment System for 5 polarities
	@SuppressWarnings("null")
	public Map<String,ClassificationResult> standfordTest(String nameOfTrain) throws Exception{
		System.out.println("Starting Stanford Test");
		System.out.println("Tweets: " +  this.tweetList.size());
		Map<String, ClassificationResult> results = new HashMap<>();
		
		// Build the Pipeline for Tokenize and Conversion.
		Properties pipelineProps = new Properties();
	    Properties tokenizerProps = new Properties();
	    pipelineProps.setProperty("annotators", "parse, sentiment");
	    pipelineProps.setProperty("enforceRequirements", "false");
	    tokenizerProps.setProperty("annotators", "tokenize, ssplit");
	    tokenizerProps.setProperty("ssplit.eolonly", "true");
	    StanfordCoreNLP tokenizer = new StanfordCoreNLP(tokenizerProps);
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(pipelineProps);
	    int i=0;
	    for (Tweet tweet: tweetList){
	    	i++;
	    	System.out.println("Sentence No:" +  i);
	    	preProcessTweet(tweet);
	    	Annotation annotation = tokenizer.process(tweet.getTweetString());
	    	pipeline.annotate(annotation);
	        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	        	Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
	        	SimpleMatrix vector = RNNCoreAnnotations.getPredictions(tree);
	        	double result = 10;
	        	double[] resultDistribution = new double[5];
	        	switch(sentence.get(SentimentCoreAnnotations.SentimentClass.class).toLowerCase()){
	        	case "positive":
	        		result = 0;
	        		break;
	        	case "very positive":
	        		result = 1;
	        		break;
	        	case "neutral":
	        		result = 2;
	        		break;
	        	case "negative":
	        		result = 3;
	        		break;
	        	case "very negative":
	        		result = 4;
	        		break;
	        	}
	        	resultDistribution[0] = vector.get(4);
	        	resultDistribution[1] = vector.get(3);
	        	resultDistribution[2] = vector.get(2);
	        	resultDistribution[3] = vector.get(1);
	        	resultDistribution[4] = vector.get(0);
	        	results.put(tweet.getTweetID(), new ClassificationResult(tweet, resultDistribution, result));
	        }
	    }
	    return results;
	}
	//Pre-process for sentences
	private void preProcessTweet(Tweet tweet){
		String rawTweet = tweet.getRawTweetString();
		rawTweet = rawTweet.toLowerCase();
		//filter Usernames
		rawTweet = rawTweet.replaceAll("@[^\\s]+", "");
		//filter Urls
		rawTweet = rawTweet.replaceAll("((www\\.[^\\s]+)|(https?://[^\\s]+))", "");
		tweet.setTweetString(rawTweet.trim());
	}
	
	static int setIndexLabels(Tree tree, int index) {
	    if (tree.isLeaf()) {
	      return index;
	    }

	    tree.label().setValue(Integer.toString(index));
	    index++;
	    for (Tree child : tree.children()) {
	      index = setIndexLabels(child, index);
	    }
	    return index;
	}
}
