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
public class Boostings {
    //weka.classifiers.meta.Bagging    -P 100 -S 1 -I 10 -W weka.classifiers.trees.REPTree -- -M 2 -V 0.001 -N 3 -S 1 -L -1
    //weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.trees.J48     -- -C 0.25 -M 2
    static final String usedClassifierHolder = "__usedClassifier__";
    static final String usedClassifierParmsHolder = "__usedClassifierParms__";
    
    static final String baggingClassifier = "weka.classifiers.meta.Bagging";
    static final String baggingParms1 = "-P 100 -S 1 -I 10 -W "+ usedClassifierHolder + " -- ";
//    static final String baggingParms2 = "-P 100 -S 1 -I 10 -W "+ usedClassifierHolder + " -- ";
    
    static final String boostClassifier = "weka.classifiers.meta.AdaBoostM1";
    static final String boostParms1 = "-P 100 -S 1 -I 10 -W "+ usedClassifierHolder + " -- ";
//    static final String boostParms2 = "-P 100 -S 1 -I 10 -W "+ usedClassifierHolder + " -- ";
    
    //weka.classifiers.meta.Vote -S 1 -B "weka.classifiers.trees.J48 -C 0.25 -M 2" -B "weka.classifiers.trees.J48 -C 0.25 -B -M 5" -B "weka.classifiers.bayes.BayesNet -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5" -B "weka.classifiers.bayes.NaiveBayes -D" -R AVG
    static final String votingClassifier = "weka.classifiers.meta.Vote";
    static final String votingParms1 = "-S 1 -B \"weka.classifiers.trees.J48 -C 0.25 -M 2\" -B \"weka.classifiers.trees.J48 -C 0.25 -B -M 5\" -B \"weka.classifiers.bayes.BayesNet -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5\" -B \"weka.classifiers.bayes.NaiveBayes -D\" -R AVG";
    
    static final List<Boostings.Boosting> allBoostMethods = new ArrayList<>();

    static{
        Boostings.Boosting baggingBoost1 = new Boostings.Boosting();
        baggingBoost1.setBoostMethod(baggingClassifier);
        baggingBoost1.setBoostParms(baggingParms1);        
        allBoostMethods.add(baggingBoost1);
        
//        Boostings.Boosting baggingBoost2 = new Boostings.Boosting();
//        baggingBoost2.setBoostMethod(baggingClassifier);
//        baggingBoost2.setBoostParms(baggingParms2);
//        allBoostMethods.add(baggingBoost2);
        
        Boostings.Boosting adaBoost1 = new Boostings.Boosting();
        adaBoost1.setBoostMethod(boostClassifier);
        adaBoost1.setBoostParms(boostParms1);
        allBoostMethods.add(adaBoost1);
       
        Boostings.Boosting voting1 = new Boostings.Boosting();
        voting1.setBoostMethod(votingClassifier);
        voting1.setBoostParms(votingParms1);
        allBoostMethods.add(voting1);
    }
    /**
     * Get the 2*2 possible boosting methods
     * @return 
     */
    public static synchronized List<Boosting> getAllBoostings(){        
        return allBoostMethods;
    }
    
    public static void main(String []args){
        List<Boosting> boostings = getAllBoostings();
        Boosting one = boostings.get(0);
        one.setUsedModel(Models.kNNModel1);
        System.out.println(one.createBoostModel().getOptionsAsString());
    }
    
    public static class Boosting{
        private String boostMethod;
        private String boostParms;
        private Models.Model usedModel;
        
        public Models.Model createBoostModel(){
            if(usedModel == null){
                //weka.classifiers.meta.Vote -S 1  
                //-B "weka.classifiers.trees.J48 -C 0.25 -M 2" 
                //-B "weka.classifiers.trees.J48 -C 0.25 -B -M 5" 
                //-B "weka.classifiers.bayes.BayesNet -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5" 
                //-B "weka.classifiers.bayes.NaiveBayes -D" 
                //-R 
                //AVG
                //weka.classifiers.meta.Vote -S 1 -B "weka.classifiers.trees.J48 -C 0.25 -M 2" -B "weka.classifiers.trees.J48 -C 0.25 -B -M 5" -B "weka.classifiers.bayes.BayesNet -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5" -B "weka.classifiers.bayes.NaiveBayes -D" -R AVG
                String votingParms[] = {"-S","1","-B","weka.classifiers.trees.J48 -C 0.25 -M 2",
                                                 "-B","weka.classifiers.trees.J48 -C 0.25 -B -M 5",
                                                 "-B","weka.classifiers.bayes.BayesNet -D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5",
                                                 "-B","weka.classifiers.bayes.NaiveBayes -D",
                                                 "-R","AVG"};
                Models.Model boostModel = new Models.Model(boostMethod, votingParms); 
                return boostModel;
            }
        
            ArrayList<String> joinOptionsList = new ArrayList<>();
            
            String newBoostParms = boostParms.replace(usedClassifierHolder, usedModel.getClassifier());
//                                    .replace(usedClassifierParmsHolder, usedModel.getOptionsAsString());
            String[] boostFirstOptions = newBoostParms.split(" ");
            for(String s: boostFirstOptions){
                joinOptionsList.add(s);
            }
            for(String s: usedModel.getOptions()){
                joinOptionsList.add(s);
            }
            String joinOptions[] = new String[joinOptionsList.size()];
            for(int i=0;i<joinOptions.length;i++){
                joinOptions[i] = joinOptionsList.get(i);
            }
            
            Models.Model boostModel = new Models.Model(boostMethod, joinOptions); 
            return boostModel;
        }

        public String getBoostMethod() {
            return boostMethod;
        }

        public void setBoostMethod(String boostMethod) {
            this.boostMethod = boostMethod;
        }
        public String getBoostParms() {
            return boostParms;
        }

        public void setBoostParms(String boostParms) {
            this.boostParms = boostParms;
        }

        public Models.Model getUsedModel() {
            return usedModel;
        }

        public void setUsedModel(Models.Model usedModel) {
            this.usedModel = usedModel;
        }

        private void unsupported() {
            throw new UnsupportedOperationException("Boost Method Embedded Model Not set yet!"); //To change body of generated methods, choose Tools | Templates.
        }
        
        
    }
}
