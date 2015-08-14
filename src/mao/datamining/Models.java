/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mao.datamining;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mao
 */
public class Models {
    public static class Model{
        String classifier;
        String options[];

        public String getClassifier() {
            return classifier;
        }

        public String[] getOptions() {
            return options;
        }
        Model(String _classifier, String _options[]){
            this.classifier = _classifier;
            this.options = _options;
        }

        public String getOptionsAsString() {
            StringBuilder builder = new StringBuilder();
            for(String s: options){
                if(s.indexOf(" ")>0){
                    s = s.replace("\"", "\\\"");
                    builder.append("\"").append(s).append("\"").append(" ");
                }else{
                    builder.append(s).append(" ");
                }                
            }
            return builder.toString();
        }
    }
    
    public static void main(String args[]){
        System.out.println(kNNModel1.getOptionsAsString());
        for(String s: kNNModel1.getOptions()){
            System.out.println(s);
        }
    }
    
    //J48 Models
    static final String J48Classifier = "weka.classifiers.trees.J48";
    //default -C 0.25 -M 2
    static final String[] J48Option1 = {"-C","0.25","-M","2"}; 
    static final Model j48Model1 = new Model(J48Classifier, J48Option1);
    //binary split+Mininal Obj Num(5):  -C 0.25 -B -M 5
    static final String[] J48Option2 = {"-C","0.25","-B","-M","5"}; 
    static final Model j48Model2 = new Model(J48Classifier, J48Option2);
    //binary split+Mininal Obj Num(5)+reducedErrorPruning=true:   -R -N 3 -Q 1 -B -M 5
    static final String[] J48Option3 = {"-R","-N","3","-Q","1","-B","-M","5"}; 
    static final Model j48Model3 = new Model(J48Classifier, J48Option3);
    
    //Support Vector Machine, SMO
    //weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0"
    static final String smoClassifier = "weka.classifiers.functions.SMO";
    static final String[] smoOptions1 = {"-C", "1.0", "-L", "0.001", "-P", "1.0E-12", "-N", "0", "-V", "-1", "-W", "1", "-K", 
                                                "weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0"};
    static final Model smoModel1 = new Model(smoClassifier, smoOptions1);
    //                                   -C     1.0    -L    0.001    -P    1.0E-12    -N    2    -V    -1    -W    1    -K 
    //                                          "weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.01"
    static final String[] smoOptions2 = {"-C", "1.0", "-L", "0.001", "-P", "1.0E-12", "-N", "2", "-V", "-1", "-W", "1", "-K", 
                                                "weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.01"};
    static final Model smoModel2 = new Model(smoClassifier, smoOptions2);
    
    //weka.classifiers.bayes.NaiveBayes -D
    static final String bayesClassifier = "weka.classifiers.bayes.NaiveBayes";
    static final String[] bayesOptions1 = {};
    static final Model naiveModel1 = new Model(bayesClassifier, bayesOptions1);
    static final String[] bayesOptions2 = {"-D"};
    static final Model naiveModel2 = new Model(bayesClassifier, bayesOptions2);
    
    //weka.classifiers.bayes.BayesNet -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5
    static final String bayesNetClassifier = "weka.classifiers.bayes.BayesNet";
    static final String[] bayesNetOptions1 = {"-D","-Q", "weka.classifiers.bayes.net.search.local.K2", 
                "--", "-P", "1", "-S", "BAYES", "-E", "weka.classifiers.bayes.net.estimate.SimpleEstimator", "--", "-A", "0.5"};
    static final Model bayesNetModel1 = new Model(bayesNetClassifier, bayesNetOptions1);
    //weka.classifiers.bayes.BayesNet -D -Q weka.classifiers.bayes.net.search.local.TAN 
    //           --    -S    BAYES    -E    weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5
    static final String[] bayesNetOptions2 = {"-D","-Q", "weka.classifiers.bayes.net.search.local.TAN", 
                "--", "-S", "BAYES", "-E", "weka.classifiers.bayes.net.estimate.SimpleEstimator", "--", "-A", "0.5"};
    static final Model bayesNetModel2 = new Model(bayesNetClassifier, bayesNetOptions2);
    
    //weka.classifiers.lazy.IBk -K 1 -W 0 -A "weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\""
    static final String kNNClassifier = "weka.classifiers.lazy.IBk";
    static final String[] kNNOptions1 = {"-K", "1", "-W", "0", "-A", "weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\""};  
    static final Model kNNModel1 = new Model(kNNClassifier, kNNOptions1);
    static final String[] kNNOptions2 = {"-K", "5", "-W", "0", "-A", "weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\""};
    static final Model kNNModel2 = new Model(kNNClassifier, kNNOptions2);
    static final String[] kNNOptions3 = {"-K", "10", "-W", "0", "-A", "weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\""};
    static final Model kNNModel3 = new Model(kNNClassifier, kNNOptions3);
    
    final static List<Model> allModels = new ArrayList<>();
    static{
        allModels.add(j48Model1);
        allModels.add(j48Model2);
//        allModels.add(j48Model3);
        
//        allModels.add(smoModel1);
//        allModels.add(smoModel2);
        
//        allModels.add(naiveModel1);
        allModels.add(naiveModel2);
        
        allModels.add(bayesNetModel1);
//        allModels.add(bayesNetModel2);
        
//        allModels.add(kNNModel1);
//        allModels.add(kNNModel2);
//        allModels.add(kNNModel3);
        
        
    }
    
    public static synchronized List<Model> getAllModels(){
        return allModels;
    }
    
    private Models(){}
}
