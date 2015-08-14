/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mao.datamining;

/**
 *
 * @author Mao Chuan Li
 */
public class TestCase {

    private int caseNumber;
    private DataSetPair dataSetPair;
    private String classifier;


    private String optionsFilePath;
    private String[] options;

    private String resultFile;

    private boolean completed = false;

    TestCase(DataSetPair pair, String clazz) {
        this.dataSetPair = pair;
        this.classifier = clazz;
//        this.optionsFilePath = optsFile;
//        this.resultFile = testResultFile;
    }
    
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(int caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }
    private Process process;

    public DataSetPair getDataSetPair() {
        return dataSetPair;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getResultFile() {
        return this.resultFile;
    }
    
    public String getOptionsFilePath() {
        return optionsFilePath;
    }
        
    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }
    
    public void setDataSetPair(DataSetPair dataSetPair) {
        this.dataSetPair = dataSetPair;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void setOptionsFilePath(String optionsFilePath) {
        this.optionsFilePath = optionsFilePath;
    }

    public void setResultFile(String resultFile) {
        this.resultFile = resultFile;
    }
}
