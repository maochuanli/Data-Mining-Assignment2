/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mao.datamining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.NumericToNominal;

/**
 *
 * @author mao
 */
public class Util {
    public static String padRight(String s, int n) {
         return String.format("%1$-" + n + "s", s);  
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);  
    }
    
    public static String pad(String str, int size, char padChar)
    {
      StringBuffer padded = new StringBuffer(str);
      while (padded.length() < size)
      {
        padded.append(padChar);
      }
      return padded.toString();
    }
    
    public static String padRightSpace(String str, int size)
    {
      return pad(str,size,' ');
    }
    
    public static void splitTrainTestFiles(DataSetFiles files){
        BufferedWriter trainWriter = null;
        BufferedWriter testWriter = null;
        BufferedReader reader = null;
        String line = null;
        try {
            File trainFile = new File(files.getTrainFileName());
            File testFile = new File(files.getTestFileName());
            File mergeFile = new File(files.getMergeFileName());
            
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(mergeFile)));
            trainWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile)));
            testWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile)));
            
            while( (line=reader.readLine())!=null ){
                if(line.endsWith(",train")){
                    trainWriter.write(line.substring(0,line.length()-6));
                    trainWriter.newLine();
                }else if(line.endsWith(",test")){
                    testWriter.write(line.substring(0,line.length()-5));
                    testWriter.newLine();
                }else if(line.endsWith("@attribute set {train,test}")){
                    //ignore this line
                }else{
                    trainWriter.write(line);
                    trainWriter.newLine();
                    testWriter.write(line);
                    testWriter.newLine();
                }
            }
            
        }catch(Exception e){
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, e);
        }finally{
            try {
                reader.close();
                trainWriter.close();
                testWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * @relation
     * 
     * @attribute Var1 numeric
     * ...
     * @attribute Var230 {-1,1}
     * @param files 
     */
    public static void mergeTrainTestFiles(DataSetFiles files) {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        String line = null;
        try {
            File trainFile = new File(files.getTrainFileName());
            File testFile = new File(files.getTestFileName());
            File mergeFile = new File(files.getMergeFileName());
            if(!trainFile.exists() || !testFile.exists()){
                System.err.println("Either train or test Files not exist!!!");
                return;
            }   
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(new FileOutputStream(mergeFile));
            writer = new BufferedWriter(outStreamWriter);
            
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(trainFile)));
            //reader train's header + body lines
            while((line=reader.readLine())!=null){             
                if(line.trim().equalsIgnoreCase("@attribute Var230 {-1,1}")){
                    writer.write(line);
                    writer.newLine();
                    writer.write("@attribute set {train,test}");//add a new variable @attribute set {train,test} 
                    writer.newLine();
                }else if(line.endsWith(",1") || line.endsWith(",-1")){
                    writer.write(line+",train");
                    writer.newLine();
                }else{
                    writer.write(line);
                    writer.newLine();
                }
            }
            //read the test's body only
            reader.close();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)));
            
            while((line=reader.readLine())!=null){             
                if(line.endsWith(",1") || line.endsWith(",-1")){
                    writer.write(line+",test");
                    writer.newLine();
                }
            }
            
        } catch (Exception ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static class DataSetFiles{
        private String trainFileName;
        private String testFileName;
        private String mergeFileName;

        public String getTrainFileName() {
            return trainFileName;
        }

        public String getTestFileName() {
            return testFileName;
        }

        public String getMergeFileName() {
            return mergeFileName;
        }

        public DataSetFiles(String trainFileName, String testFileName, String mergeFileName) {
            this.trainFileName = trainFileName;
            this.testFileName = testFileName;
            this.mergeFileName = mergeFileName;
        }
        
        
        
    }
    
        /**
     * Based on the defined list of attributes, transform them into nominal from numeric type
     * weka.filters.unsupervised.attribute.NumericToNominal -R first-last
     * @param newData
     * @param columns2Nominal
     * @return 
     */
    public static Instances transformNum2Nominal(Instances newData, String[]columns2Nominal) {
        StringBuilder indexArrayStr = new StringBuilder();
        for(int i=0;i<columns2Nominal.length;i++){
            String attrName = columns2Nominal[i];
            Attribute attr = newData.attribute(attrName);
            if(attr != null){
                indexArrayStr.append(attr.index()+1).append(",");
            }
        }       
        try{
            NumericToNominal transform = new NumericToNominal();
            transform.setInputFormat(newData);        
            transform.setAttributeIndices(indexArrayStr.toString());
            newData = Filter.useFilter(newData, transform);
        }catch(Exception e){
            Main.logging(null,e);
        }
//        Main.logging("== New Data after transforming numeric data : ===\n" + newData.toSummaryString());
        return newData;
    }
    
    /**
     * To normalize all the attributes in a dataset
     * @param newData
     * @return 
     */
    public static Instances normalizeDS(Instances newData){
        try {
            Normalize normal = new Normalize();
            normal.setInputFormat(newData);
            normal.setIgnoreClass(true);
            normal.setScale(1.0);
            newData = Filter.useFilter(newData, normal);
//            Main.logging("== New Data after normalizing data : ===\n" + newData.toSummaryString());
                        
        } catch (Exception ex) {
            Logger.getLogger(DataSetPair.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newData;
    }
}
