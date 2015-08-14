/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mao.datamining;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mao Chuan Li
 */
public class TestSuite {
    
    private String suiteName;
    private File suiteHomeDir;
    private File suiteResultsFile;

    private ArrayList<TestCase> allTests = new ArrayList<>();
    private Properties suiteProperties;
    int testsNumber = 0;
    
    public TestSuite(String name){
        this.suiteProperties = new Properties();
        this.suiteName = name;
        suiteHomeDir = new File(Main.dataMiningHome + "/" + name);
        suiteResultsFile = new File(suiteHomeDir,"testResults.csv");
    }
    
    public void addTestCase(TestCase test){
        allTests.add(test);
        testsNumber++;
    }
    
    public List<TestCase> getAllTests(){
        return allTests;
    }
    
    public void loadTests(){
        try {
            FileInputStream inStream = new FileInputStream(suiteHomeDir+"/testSuite.properties");
            suiteProperties.clear();
            suiteProperties.load(inStream);
            inStream.close();
            
            for(int i=1;i<100000;i++){
                if(suiteProperties.get("test."+i+".completed") != null){
                    String trainFile = suiteProperties.getProperty("test."+i+".trainFile");
                    String testFile = suiteProperties.getProperty("test."+i+".testFile");
                    String clazz = suiteProperties.getProperty("test."+i+".classifier");
                    boolean completed = Boolean.valueOf(suiteProperties.getProperty("test."+i+".completed"));
                    String optFilePath = suiteProperties.getProperty("test."+i+".optionsFile");
                    TestCase test = new TestCase(new DataSetPair(trainFile,testFile), clazz );
                    
                    test.setCaseNumber(i);
                    test.setResultFile(suiteResultsFile.getAbsolutePath());                    
                    test.setOptionsFilePath(optFilePath);
                    test.setCompleted(completed);
                    allTests.add(test);
                }else{
                    break;
                }
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestSuite.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestSuite.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    /**
     * Save all test cases to a properties file for future loading.
     */
    public void saveTests(){
                
        for(int i=1;i<=allTests.size();i++){
            FileOutputStream testOptFileOut = null;
            try {                
                TestCase t = allTests.get(i-1);
                File testOptFile = new File(suiteHomeDir+"/test."+i+".txt");
                testOptFileOut = new FileOutputStream(testOptFile);
                for(String s: t.getOptions()){
                    testOptFileOut.write((s+"\n").getBytes());
                } 
                t.setOptionsFilePath(testOptFile.getAbsolutePath());
                suiteProperties.put("test."+i+".trainFile", t.getDataSetPair().getTrainFileName());
                suiteProperties.put("test."+i+".testFile", t.getDataSetPair().getTestFileName());
                suiteProperties.put("test."+i+".classifier", t.getClassifier());
                suiteProperties.put("test."+i+".completed", "false");
                suiteProperties.put("test."+i+".optionsFile", testOptFile.getAbsolutePath());
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TestSuite.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(TestSuite.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    testOptFileOut.close();
                } catch (IOException ex) {
                    Logger.getLogger(TestSuite.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
        this.saveProperties();
    }
    /**
     * Check if this suite exists by checking if the "testResults.csv" file exists
     * @return 
     */
    public boolean exists() {
        return suiteResultsFile.exists();
    }
    
    public int getTestsNumber() {
        return testsNumber;
    }
    
    public String getSuiteName() {
        return suiteName;
    }

    void setTestCompleted(TestCase t, boolean b) {
//        for(TestCase test: allTests){
            suiteProperties.put("test."+t.getCaseNumber()+".completed", "true");
            t.setCompleted(true);
            saveProperties();
//        }
    }
    
    public File getSuiteResultsFile() {
        return suiteResultsFile;
    }

    private void saveProperties() {
        try {
            FileOutputStream outStream = new FileOutputStream(new File(suiteHomeDir, "testSuite.properties"));            
            suiteProperties.store(outStream, suiteName);
            outStream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestSuite.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestSuite.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    void init() {
        if(!suiteHomeDir.exists()){
            suiteHomeDir.mkdirs();
        }
    }
}
