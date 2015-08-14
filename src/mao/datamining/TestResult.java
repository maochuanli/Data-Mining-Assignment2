/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mao.datamining;

/**
 *
 * @author mao
 */
public class TestResult {
    private String missingValueMode;
    private String resampleMode;
    private String featureSelectMode;
    private String classifier;
    private String classifierOptions;
    private String boostingMethod;
    
    private long trainingTime;
    private long testTime;
    
    private double confusionMatrix10Folds[][];
    private double confusionMatrix4Test[][];
    private String caseNum;
    private double aut;
    private double precision;
    private double recall;


    public void setConfusionMatrix10Folds(double confusionMatrix10Folds[][]) {
        this.confusionMatrix10Folds = confusionMatrix10Folds;
    }

    public double[][] getConfusionMatrix4Test() {
        return confusionMatrix4Test;
    }

    public void setConfusionMatrix4Test(double confusionMatrix4Test[][]) {
        this.confusionMatrix4Test = confusionMatrix4Test;
    }
    
    public TestResult(){}
    
    public static String getHeadLine(){
//        return "CaseNum,ReSampleMode,FeatureSelection,Classifier,Options,TrainingTime,TestTime,CV-True Negative,False Positive,False Negative,True Positive,Test-True Negative,False Positive,False Negative,True Positive";
        return "CaseNum,ReSampleMode,FeatureSelection,Classifier,Options,TrainingTime,TestTime,True Negative,False Positive,False Negative,True Positive,Precision,Recall,AUC";
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(caseNum).append(",")
//                .append(missingValueMode).append(",")
                .append(resampleMode).append(",")
                .append(featureSelectMode).append(",")
                .append(classifier).append(",")
                .append(classifierOptions).append(",")
//                .append(boostingMethod).append(",")
                .append(trainingTime).append(",")
                .append(testTime).append(",")
//                .append(confusionMatrix10Folds[0][0]).append(",")
//                .append(confusionMatrix10Folds[0][1]).append(",")
//                .append(confusionMatrix10Folds[1][0]).append(",")
//                .append(confusionMatrix10Folds[1][1]).append(",")
                .append(confusionMatrix4Test[0][0]).append(",")
                .append(confusionMatrix4Test[0][1]).append(",")
                .append(confusionMatrix4Test[1][0]).append(",")
                .append(confusionMatrix4Test[1][1]).append(",")
                .append(precision).append(",")
                .append(recall).append(",")
                .append(aut);
        
        return sb.toString();
    }
    
    public String getMissingValueMode() {
        return missingValueMode;
    }

    public void setMissingValueMode(String missingValueMode) {
        this.missingValueMode = missingValueMode;
    }

    public String getResampleMode() {
        return resampleMode;
    }

    public void setResampleMode(String resampleMode) {
        this.resampleMode = resampleMode;
    }

    public String getFeatureSelectMode() {
        return featureSelectMode;
    }

    public void setFeatureSelectMode(String featureSelectMode) {
        this.featureSelectMode = featureSelectMode;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getClassifierOptions() {
        return classifierOptions;
    }

    public void setClassifierOptions(String classifierOptions) {
        this.classifierOptions = classifierOptions;
    }

    public String getBoostingMethod() {
        return boostingMethod;
    }

    public void setBoostingMethod(String boostingMethod) {
        this.boostingMethod = boostingMethod;
    }

    public long getTrainingTime() {
        return trainingTime;
    }

    public void setTrainingTime(long trainingTime) {
        this.trainingTime = trainingTime;
    }

    public long getTestTime() {
        return testTime;
    }

    public void setTestTime(long testTime) {
        this.testTime = testTime;
    }

    public double[][] getConfusionMatrix10Folds() {
        return confusionMatrix10Folds;
    }    

    public String getCaseNum() {
        return caseNum;
    }

    public void setCaseNum(String caseNum) {
        this.caseNum = caseNum;
    }    

    void setAUT(double areaUnderROC) {
        this.aut = areaUnderROC;
    }

    void setPrecision(double precisionX) {
        this.precision = precisionX;
    }

    void setRecall(double recallx) {
        this.recall = recallx;
    }
}
