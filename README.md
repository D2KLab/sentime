# **Sentime**
SentiME is an ensemble system consisted by five individual sub-classifiers.
![image](https://github.com/MultimediaSemantics/sentime/blob/master/diagram_ISWC3.png)  
With the red color it has been illustrated the training process.  
  
With the blue it has been illustrated the test process. 

Our Tweet Corpus downloaded
------------------------------
|   Corpus   |   Available   |   Total number in [paper][1]   |
| :-- | :-----: | :----------: |
|SemEval2013-train-cleansed-B|7,658|9,728
|SemEval2013-dev-gold-B|1,315|1,654
|SemEval2013-test-gold-B|3,072|3,813|
|SemEval2014-test-gold-B|1,513|1,853|
|SemEval2015-test-gold-B|2,390|2,392|

## Approach

SentiME system is a system created in order to classify sentences in natural language.  
SentiME classifies tweets and Amazon reviews given in standard format.
  
The training of the system is done by the first 4 sub-classifiers. Each one of them classifies each sentence separately and the final classification result is computed by the average of confident scores for each one of the classes. The class with the bigger average score is the dominant. Also during the training process we aplly bagging (Bootsrap Aggregating Algorithm) of 150% of the initial dataset.
  
The test of the system is been done by all of the classifiers. We use the 5th (Stanford Sentiment System) sub-classifier with pre-trained models.


## How to run
### **External Libraries needed**
There are a lot of external libraries needed for the Sentiment Ensemble System. You can [**Download Them From Google Drive**](https://drive.google.com/open?id=0B1rzzflJW8zeOHByeTBBOVFIMXc), put them in directory: sentime/lib/ and add them to your Java Build Path. :)

### Main class
> package: fr.eurecom.sentime
> SentimeSystem

### Training commands
You can train a single individual system using the command below:

    train <training_data> -trainmodel <select the system> ...
 
You can train all the system on the same training data using the command below:

	train <training_dataset> [-arffname name_of_the_arff_file]...
Other parameters:  

-testmodel	Only test one sub-classifier, use 0-4 to specify which sub-classifier to be tested. Use 5 to test only the original Stanford Sentiment System.  
-trainmodel	Only train one sub-classifier, use 0-3 to specify which sub-classifer to be trained  
-bsize		Enables bagging training process and specify the size of bootstrap samples.  
-experiment	Using <nostanford> to disable Stanford Sentiment System; using <noteamx> to exclude TeamX; using <nost> to exclude both systems  
-disablefilter	Disable the default filter mechanism: using <train> to disable duplicate input tweets; using <test> to disable duplicate tweet filtering when scoring.  
-format		Change the input format to xml which converts to tsv files.Use <xml> for xml input or <tsv> for tsv input dataset  
-folder		Choose the folder of the 10 fold cross-validation folder: using <1> for the first folder, <2 for the second>...  
Example  
	java -Xms512m -Xmx120g -jar SentiMEa.jar train SemEval2013-train+dev-B -arffname _Bagging_150a -bsize 17007 -format tsv  
###Test command

	test <test_dataset> [-arffname name_of_the_arff_file]
Example  

	java -Xms512m -Xmx110g -jar SentiMEb.jar test SemEval2015-test-gold-B -arffname _Bagging_150b1  -format tsv


### Classify one Tweet
   	java -Xms512m -Xmx110g -jar SentiMEb.jar single
	Please enter the tweet:

### Usage

* If you don't explicitly specify the path of system parameter file, the default system parameter files will be used.

### Input and output

* Training and Testing dataset in SemEval's form (tweets) have to be put into resources/Amazon-reviews/eval_tsv/  
* Training and Testing dataset in ESWC's form (Amazon Reviews) have to be put into resources/Amazon-reviews/eval_xml/
* The output (the classifications for input tweets) of system will be generated in output/result.txt
* All the wrong classifications will be generated in output/SentiMEa/ with a filename ending with "WrongClassified.tsv"

## Examples

    "I drove a Linconl and it's a truly dream" -> positive

    "I drove a Linconl and it was awful" -> negative

## Team
* Sygkounas Efstratios
* Giuseppe Rizzo
* Raphael Troncy
* Enrico Palumbo
* Li Xianglei

## Our publications
* Palumbo E., Sygkounas E., Troncy R., Rizzo G. (2017) [SentiME++ at SemEval-2017 Task 4A: Stacking State-of-the-Art Classifiers to Enhance Sentiment Classification.][5] In International Workshop on Semantic Evaluation (SemEval), Vancouver, Canada

* Sygkounas E., Rizzo G., Troncy R. (2016) [A Replication Study of the Top Performing Systems in SemEval Twitter Sentiment Analysis.][3] In: 15th International Semantic Web Conference (ISWC'16), resources Track, Kobe, Japan.
  
* Sygkounas E., Rizzo G., Troncy R. (2016) [Sentiment Polarity Detection From Amazon Reviews: An Experimental Study.][2] In: 13th Extended Semantic Web Conference (ESWC'16), Challenges Track, Heraklion, Greece.  

* Sygkounas E., Rizzo G., Troncy R. (2016) [The SentiME System at the SSA Challenge.][4] In: (ESWC'16), Challenge on Fine-Grained Sentiment Analysis, Heraklion, Greece. 1st ranked system.

[1]: http://www.anthology.aclweb.org/S/S15/S15-2078.pdf
[2]: https://github.com/MultimediaSemantics/sentime/blob/master/Sygkounas_Rizzo-ESWC2016Challenges.pdf
[3]: https://github.com/MultimediaSemantics/sentime/blob/master/Sygkounas_Rizzo-ISWC2016.pdf
[4]: https://github.com/MultimediaSemantics/sentime/blob/master/Sygkounas_Rizzo-SSA2016.pdf
[5]: http://nlp.arizona.edu/SemEval-2017/pdf/SemEval107.pdf
