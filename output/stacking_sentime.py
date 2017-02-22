import numpy as np
from sklearn import svm, datasets
from sklearn.svm import SVC
from sklearn import cross_validation
from sklearn import grid_search
from sklearn.metrics import f1_score
from numpy import loadtxt
import time

#for x in xrange(1,10):

start_time = time.time()

#load training and test data

data_train = loadtxt('Trained-Features-NRC_Everything4_Everything_Classifiers_scores+GS.tsv',delimiter='\t')

data_test = loadtxt('Trained-Features-NRC_Everything4_out_Classifiers_scores+GS.tsv',delimiter='\t')

X_train = data_train[:,0:-1] 

y_train = data_train[:,-1]

X_test = data_test[:,0:-1]

y_test = data_test[:,-1]

#create an instance of SVM with rbf kernel
#optimize hyper parameters through 10 fold cv

clf = SVC( kernel = 'rbf',cache_size = 1000, C=0.17433288221999882, gamma=0.028072162039411756)

#parameters = {'gamma' : np.logspace(-9,3,30),'C': np.logspace(-2,10,30)}

#gs_rbf = grid_search.GridSearchCV(clf,param_grid=parameters,cv = 10,n_jobs=-1, verbose=10)

clf.fit(X_train,y_train)

#select the best parameters

#clf = gs_rbf.best_estimator_
#print "Best pair of C and Gamma:", gs_rbf.best_params_
#find predictions on the test set

prediction = clf.predict(X_test)

with open('result.tsv','r') as res:
	results = res.readlines()

id_list = []

for line in results:
	line = line.split('\t')
	id_list.append(line[0])

c = 0
with open('predict.tsv','w') as f:
	for i in prediction:
		if i == 0:
			sent = 'positive'
		elif i == 1:
			sent = 'neutral'
		elif i == 2:
			sent = 'negative'
		f.write(id_list[c])
		f.write('\t')
		f.write(sent)
		f.write('\n')
		c += 1
#find score

print f1_score(y_test, prediction, average='micro')

 
print("--- %s seconds ---" % (time.time() - start_time))

