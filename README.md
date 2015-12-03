# sentime
... Tweet Sentiment Analysis ...

## Approach
// to fill up


## How to run
### Main class
SentimeSystem   package: fr.eurecom.sentime

### Training command
    train <training_data> -tm <which_system_to_train> [-tf system01_parameter_file] [-tf2 system02_parameter_file] ...
    trainAll <training_data> [-tf system01_parameter_file] [-tf2 system02_parameter_file] ...

### Evaluating command
    eval <testing_data> -em <which_system_to_test> [-tf system01_parameter_file] [-tf2 system02_parameter_file] ...
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
* ..
* ...
* Giuseppe Rizzo
* Raphael Troncy
