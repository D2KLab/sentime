# **Sentime**
This is an ensemble system consisted in five individual sub-classifiers.
![image](https://docs.google.com/drawings/d/12Xa2rtz8C09qSq0Gk5rDrYbmwRn-4-_6v0Ur276OAyY/pub?w=479&h=229)
**Note**: TeamX is not available due to some library problems. So the whole ensemble system works on the 4 remaining sub-classifiers.

Our Tweet Corpus downloaded
------------------------------
|   Corpus   |   Available   |   Total number in [paper][1]   |
| :-- | :-----: | :----------: |
|SemEval2013-train-cleansed-B|7,658|9,728
|SemEval2013-dev-gold-B|1,315|1,654
|SemEval2013-test-gold-B|3,072|3,813|
|SemEval2014-test-gold-B|1,513|1,853|

## Approach
// to fill up


## How to run
### **External Libraries needed**
There are a lot of external libraries needed for the Sentiment Ensemble System. You can [**Download Them From Google Drive**](https://drive.google.com/open?id=0B1rzzflJW8zeOHByeTBBOVFIMXc), put them in directory: sentime/lib/ and add them to your Java Build Path. :)

### Main class
> package: fr.eurecom.sentime
> SentimeSystem

### Training command
You can train a single individual system using the command below:

    train <training_data> -tm <which_system_to_train> [-tf system01_parameter_file] [-tf2 system02_parameter_file] ...

You can train all the system on the same training data using the command below:

	trainAll <training_data> [-tf system01_parameter_file] [-tf2 system02_parameter_file] ...

### **Evaluating command**
You can evaluate a single system using the command below:

	eval <testing_data> -em <which_system_to_test> [-tf system01_parameter_file] [-tf2 system02_parameter_file] ...

   **Note**: <kbd>-em 0</kbd> represents NRC sub-classifier. <kbd>-em 1</kbd> represents GU-MLT-LT sub-classifier. <kbd>-em 2</kbd> represents KLUE sub-classifier. <kbd>-em 3</kbd> represents TeamX sub-classifier(*dysfunctional*). <kbd>-em 4</kbd> represents **Stanford_System** sub-classifier.

Your can evaluate the whole ensemble system using the command below:

	evalAll <testing_data> [-tf system01_parameter_file] [-tf2 system02_parameter_file] ...

### Classify one Tweet
    single
    enter the input tweet in console

### Usage
* If you don't explicitly specify the path of system parameter file, the default system parameter files will be used.
* The command "train" and "trainAll" are used to train the individual subsystems. You can train subsystems one by one with difference training data. Or you can train them at the same time using the same training data.
* The command eval is used to evaluate the individual subsystem. You have to specify which system you want to evaluate.
* The command evalAll is used to evaluate the ensemble system which will consume the classifications from the subsystems and produce the final classification.

### Input and output
* Training and Testing data must be put into sources/tweets/  
* The output(the classifications for input tweets) of system will be generated in output/result.txt
* All the error classifications will be generated in output/error_anlysis/error.txt

## Examples
    "I drove a Linconl and it's a truly dream" -> positive

    "I drove a Linconl and it was awful" -> negative

## Team
* Li Xianglei
* Sygkounas Stratos
* Giuseppe Rizzo
* Raphael Troncy

[1]: http://www.anthology.aclweb.org/S/S15/S15-2078.pdf
