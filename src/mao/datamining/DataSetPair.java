/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mao.datamining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mao.datamining.Main.MainLogger;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.LinearForwardSelection;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveUseless;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

/**
 *
 * @author mao
 */
public class DataSetPair {
//    static String missingValuesHole = "hole";
//    static String missingValuesNoHole = "nohole";
    static String resampleNone = "none";
    static String resampleUnder = "under";
    static String resampleOver = "over";
    static String resampleMatrix = "matrix";
    
    static String featureSelectionA = "selectA";
    static String featureSelectionB = "selectB";
    static String featureSelectionNo = "none";
    
    /**
     * Under or Over Sample options
     */
    //weka.filters.supervised.instance.SpreadSubsample -M 0.0 -X 150.0 -S 1
    static String[] underSampleFilterOptions =      {"-M","0.0","-X","150.0","-S","1"};
    //weka.filters.supervised.instance.SMOTE -C 0 -K 5 -P 100.0 -S 1 smoteOptions
    static String[] overSampleSmoteOptions = {"-C", "0", "-K", "5", "-P", "800.0", "-S", "1"};
    
    //target directory to save the target data sets
//    static String processedDSHome = "c:/DS1/processedDS";
    
    //Srouce files
    static String trainSourceFileName = Main.OrangeDSHome+"/orange_train.arff";
    static String testSourceFileName = Main.OrangeDSHome+"/orange_test.arff";

//    static String columns2Nominal[] = {"Var7","Var35","Var65","Var72","Var78","Var132","Var144","Var181"};
    static String columns2Delete[] = {}; //Nominal
//    static String columns2Delete[] = {"Var44","Var143","Var173", //Numeric
//                                      "Var192","Var197","Var198","Var199","Var202","Var204","Var212","Var216",
//                                      "Var217","Var220","Var222"}; //Nominal
    
    private void doNotSupport() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }



    private final List<String> finalTrainAttrList = new ArrayList<>();
    
    //target datasets name, and instances objects
    private String trainFileName;
    private String testFileName;
    
    
    private Instances finalTrainDataSet = null;
    private Instances finalTestDataSet = null;
 
    //The target datasets modes
    private String missingProcessMode;
    private String resampleMethod;
    private String featureSelectionMode;
        
    public DataSetPair(){  }
    
   
    public void setFeatures(String _resampleMethod, String _featureName){
        this.missingProcessMode = "none";
        this.resampleMethod = _resampleMethod;
        this.featureSelectionMode = _featureName;
        
        this.trainFileName = Main.OrangeProcessedDSHome+"/"+missingProcessMode+"_"+resampleMethod+"_"+featureSelectionMode+"_train.arff";
        this.testFileName = Main.OrangeProcessedDSHome+"/"+missingProcessMode+"_"+resampleMethod+"_"+featureSelectionMode+"_test.arff";
    }
        

    public DataSetPair(File f, File testDSFile) {
        this.trainFileName = f.getAbsolutePath();
        this.testFileName = testDSFile.getAbsolutePath();
        
        String name = f.getName();
        String segments[] = name.split("_");
        
        this.missingProcessMode = segments[0];
        this.resampleMethod = segments[1];
        this.featureSelectionMode = segments[2];
    }
    
    public DataSetPair(String trainPath, String testPath){
        this(new File(trainPath), new File(testPath));
    }
    
    boolean didIt = false;
    private void doItOnce4All(){
        if(didIt) return;
        didIt = true;
        try{
            //step 0, remove all those empty columns, which has more than 50% missing values
            Instances orangeDataSet = ConverterUtils.DataSource.read(trainSourceFileName);
            orangeDataSet.setClassIndex(orangeDataSet.numAttributes() - 1);
            Attribute classAttr = orangeDataSet.attribute(orangeDataSet.numAttributes() - 1);
            MainLogger.log(Level.INFO,"Class Attribute: {0}",classAttr.toString());
            
            //step 0-1, to remove all columns which has more than half missing values
            Instances newData =  orangeDataSet;          
            RemoveUselessColumnsByMissingValues removeMissingValuesColumns = new RemoveUselessColumnsByMissingValues();
            removeMissingValuesColumns.setM_maxMissingPercentage(50);
            removeMissingValuesColumns.setManualDeleteColumns(columns2Delete);
            removeMissingValuesColumns.setInputFormat(newData);
            newData = Filter.useFilter(newData, removeMissingValuesColumns);
            Main.logging("== New Data After Removing all Columns having >50% missing values: ===\n" + newData.toSummaryString());
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.OrangeProcessedDSHome+"/afterRemoveMissingColumns1.arff")))) {
                writer.write(newData.toString());
            } 

            //step 0-2 to transform those numeric columns to Nominal
            //to delete those instances with more than half missing values
            BufferedReader reader70 = new BufferedReader(new InputStreamReader(new FileInputStream(Main.OrangeProcessedDSHome+"/afterRemoveMissingColumns1.arff")));
            BufferedWriter writerAfterDeleteRows = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.OrangeProcessedDSHome+"/afterRemoveRows2.arff")));
            int columnNum = newData.numAttributes();
            int totalInstanceNum = newData.numInstances(), deleteM1Num =0, delete1Num=0;
            String line = null;
            int missingColumnNum = 0;
            while((line=reader70.readLine())!=null){
                missingColumnNum = 0;
                for(int i=0;i<line.length();i++){
                    if(line.charAt(i) == '?') missingColumnNum++;
                }
                if(missingColumnNum*100/columnNum <50){
                    writerAfterDeleteRows.write(line);
                    writerAfterDeleteRows.newLine();
                }else{
                    System.out.println("Delete Row: ["+line+"]");
                    if(line.endsWith("-1")){
                        deleteM1Num++;
                    }else{
                        delete1Num++;
                    }
                }
            }
            System.out.println("Total: "+totalInstanceNum+", delete class -1: "+ deleteM1Num+", delete class 1:  "+delete1Num);
            reader70.close();
            writerAfterDeleteRows.close();
            
            //create sample files:
            createSampleDataSets();

        }catch(Exception e){
            Main.logging(null,e);
        }
    }
       
    private void createSampleDataSets(){
        try {
            //reload the new data from new arff file: Main.OrangeProcessedDSHome+"/afterRemoveRows.arff"
            Instances newData = ConverterUtils.DataSource.read(Main.OrangeProcessedDSHome+"/afterRemoveRows2.arff");
            newData.setClassIndex(newData.numAttributes() - 1);
            //create none sample file
//            Main.logging("== New Data After Doing Nothing, waiting for CostMatrix: ===\n" + newData.toSummaryString());
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.OrangeProcessedDSHome+"/afterNoneSampling.arff")))) {
                writer.write(newData.toString());
            }
            //create under sample file
//            System.out.println("Under Samplessssssssssssssssssssssssssssssssssssss");
            SpreadSubsample underSampleFilter = new weka.filters.supervised.instance.SpreadSubsample();
            underSampleFilter.setInputFormat(newData);
            String underOptionsClone[] = new String[underSampleFilterOptions.length];
            System.arraycopy(underSampleFilterOptions, 0, underOptionsClone, 0, underSampleFilterOptions.length);
            underSampleFilter.setOptions(underOptionsClone);
            Instances underNewData = Filter.useFilter(newData, underSampleFilter);  

//            Main.logging("== New Data After Under Sampling: ===\n" + underNewData.toSummaryString());
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.OrangeProcessedDSHome+"/afterUnderSampling.arff")))) {
                writer.write(underNewData.toString());
            } 
            //create over sample file
//            System.out.println("Over Samplessssssssssssssssssssssssssssssssssssss");
            //weka.filters.supervised.instance.SMOTE -C 0 -K 5 -P 1000.0 -S 1 smoteOptions
            SMOTE smote = new weka.filters.supervised.instance.SMOTE();
            smote.setInputFormat(newData);
            String overOptionsClone[] = new String[overSampleSmoteOptions.length];
            System.arraycopy(overSampleSmoteOptions, 0, overOptionsClone, 0, overSampleSmoteOptions.length);
            smote.setOptions(overOptionsClone);
            Instances overNewData = Filter.useFilter(newData, smote);

            
//            Main.logging("== New Data After Over Sampling: ===\n" + overNewData.toSummaryString());
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.OrangeProcessedDSHome+"/afterOverSampling.arff")))) {
                writer.write(overNewData.toString());
            } 
            
        } catch (Exception ex) {
            Logger.getLogger(DataSetPair.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Pre-Process the training data set with:
     * RemoveUselessColumnsByMissingValues filter
     * SpreadSubsample filter to shrink the majority class instances 
     * AttributeSelection filter with CfsSubsetEval and LinearForwardSelection
     */
    private void processTrainRawData() {
        System.out.println("===================="+this.trainFileName+ "====================");
        System.out.println("===================="+this.trainFileName+ "====================");
        System.out.println("===================="+this.trainFileName+ "====================");
        finalTrainAttrList.clear();
        try {
            doItOnce4All();
            String sampleFilePath = null;
            //step 2, either over sample, or under sample
            //weka.filters.supervised.instance.SpreadSubsample
            if(this.resampleMethod.equalsIgnoreCase(resampleUnder)){
                System.out.println("Under Samplessssssssssssssssssssssssssssssssssssss");
                sampleFilePath = Main.OrangeProcessedDSHome+"/afterUnderSampling.arff"  ;  
            }else if(resampleMethod.equalsIgnoreCase(resampleOver)){
                System.out.println("Over Samplessssssssssssssssssssssssssssssssssssss");
                sampleFilePath = Main.OrangeProcessedDSHome+"/afterOverSampling.arff";
            }else if(resampleMethod.equalsIgnoreCase(resampleNone)){
                //do nothing,
                System.out.println("None Samplessssssssssssssssssssssssssssssssssssss");
                sampleFilePath = Main.OrangeProcessedDSHome+"/afterNoneSampling.arff";
            }else if(resampleMethod.equalsIgnoreCase(resampleMatrix)){
                //do nothing
                System.out.println("Matrix Samplessssssssssssssssssssssssssssssssssssss");
                sampleFilePath = Main.OrangeProcessedDSHome+"/afterNoneSampling.arff";
            }else{
                doNotSupport();
            }
            Instances newData = ConverterUtils.DataSource.read(sampleFilePath);
            newData.setClassIndex(newData.numAttributes() - 1);
//            Main.logging("== New Data After Resampling class instances: ===\n" + newData.toSummaryString());
            
            //Step 3, select features
            AttributeSelection attrSelectionFilter = new AttributeSelection();
            ASEvaluation eval = null;
            ASSearch search = null;
               
            //ranker
            if(this.featureSelectionMode.equalsIgnoreCase(featureSelectionA)){
                System.out.println("Ranker ssssssssssssssssssssssssssssssssssssss");
                System.out.println("Ranker ssssssssssssssssssssssssssssssssssssss");
                System.out.println("Ranker ssssssssssssssssssssssssssssssssssssss");
                eval = new weka.attributeSelection.InfoGainAttributeEval();
                //weka.attributeSelection.Ranker -T 0.02 -N -1
                search = new Ranker();
                String rankerOptios[] = {"-T", "0.01", "-N", "-1"};
                if( resampleMethod.equalsIgnoreCase(resampleOver) ){
                    rankerOptios[1] = "0.1";
                }
                ((Ranker)search).setOptions(rankerOptios);
                Main.logging("== Start to Select Features with InfoGainAttributeEval and Ranker" );                
            }
            //weka.attributeSelection.LinearForwardSelection -D 0 -N 5 -I -K 50 -T 0
            else if(this.featureSelectionMode.equalsIgnoreCase(featureSelectionB)){
                System.out.println("CfsSubset ssssssssssssssssssssssssssssssssssssss");
                System.out.println("CfsSubset ssssssssssssssssssssssssssssssssssssss");
                System.out.println("CfsSubset ssssssssssssssssssssssssssssssssssssss");
                eval = new CfsSubsetEval();
                search = new LinearForwardSelection();
                String linearOptios[] = {"-D", "0", "-N", "5","-I","-K","50","-T","0"};
                ((LinearForwardSelection)search).setOptions(linearOptios);                 
                Main.logging("== Start to Select Features with CfsSubsetEval and LinearForwardSelection" );
            }else if(this.featureSelectionMode.equalsIgnoreCase(featureSelectionNo)){
                System.out.println("None Selection ssssssssssssssssssssssssssssssssssssss");
                Main.logging("No Feature Selection Method");
            }else{
                doNotSupport();
            }           
            
            if(eval != null){
                attrSelectionFilter.setEvaluator(eval);
                attrSelectionFilter.setSearch(search);
                attrSelectionFilter.setInputFormat(newData);            
                newData = Filter.useFilter(newData, attrSelectionFilter);
            }
                        
            Main.logging("== New Data After Selecting Features: ===\n" + newData.toSummaryString());
            
            //finally, write the final dataset to file system
            
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.trainFileName)))) {
                writer.write(newData.toString());
            }            

            int numAttributes = newData.numAttributes();
            for (int i = 0; i < numAttributes; i++) {
                String attrName = newData.attribute(i).name();
                finalTrainAttrList.add(attrName);
            }
            Main.logging(finalTrainAttrList.toString());
//            //set the final train dataset
            finalTrainDataSet = newData;
            finalTrainDataSet.setClassIndex(finalTrainDataSet.numAttributes()-1);
            
            Main.logging("train dataset class attr: "+finalTrainDataSet.classAttribute().toString());
        } catch (Exception ex) {
            Main.logging(null, ex);
        }

    }//end process training datasets
    
    
    
    /**
     * To drop the useless columns accordingly on the test dataset, if it exists
    */
    private void processTestDataSet() {
        if(!new File(testSourceFileName).exists()) return;
        
        try {
            Instances orangeTestDataSet = ConverterUtils.DataSource.read(testSourceFileName);
            Remove remove = new Remove();
            StringBuilder indexBuffer = new StringBuilder();
            for (String attrName : finalTrainAttrList) {
                int attrIndex = orangeTestDataSet.attribute(attrName).index();
                indexBuffer.append(attrIndex + 1).append(",");
            }
            Main.logging("Attribute Indices: \n" + indexBuffer.toString());
            remove.setAttributeIndices(indexBuffer.toString());
            remove.setInvertSelection(true);

            remove.setInputFormat(orangeTestDataSet);
            Instances testNewDataSet = Filter.useFilter(orangeTestDataSet, remove);
            
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.testFileName)))) {
                writer.write(testNewDataSet.toString());
            }
            
            //set the final test dataset
            finalTestDataSet = testNewDataSet;
            finalTestDataSet.setClassIndex(finalTestDataSet.numAttributes()-1);            
            Main.logging("test dataset class attr: "+finalTestDataSet.classAttribute().toString());
        } catch (Exception e) {
            Main.logging(null, e);
        }
    }

    /**
     * Just load the saved processed 2 datasets
     */
    protected void loadBothDataSets() {
        try {
            finalTrainDataSet = ConverterUtils.DataSource.read(this.trainFileName);
            finalTrainDataSet.setClassIndex(finalTrainDataSet.numAttributes()-1);            
            
            if(new File(this.testFileName).exists()) {
                finalTestDataSet = ConverterUtils.DataSource.read(this.testFileName);
                finalTestDataSet.setClassIndex(finalTestDataSet.numAttributes()-1);            
            }
            
        } catch (Exception ex) {
            Main.logging(null, ex);
        }
        
    }
    
    public static List<DataSetPair> getAllProcessedDataSets(){
        File dir = new File(Main.OrangeProcessedDSHome);
        if(!dir.exists()){
            Main.logging("No Processed Datasets, generate them...");
            dir.mkdirs();
            generateDataSetPairs();
        }
                
        List<DataSetPair> list = new ArrayList<>();
        
        File[] dsFiles = dir.listFiles(new FileFilter(){

            @Override
            public boolean accept(File f) {
                return f.getName().endsWith("train.arff");
            }
        });
        for(File f: dsFiles){
            String fName = f.getName();
            String testFileName = fName.substring(0, fName.length()-10)+"test.arff";
            File testDSFile = new File(Main.OrangeProcessedDSHome+"/"+testFileName);
            list.add(new DataSetPair(f, testDSFile));
        }
        
        return list;
    }
    
    public Instances getFinalTrainDataSet() {
        return finalTrainDataSet;
    }

    public Instances getFinalTestDataSet() {
        return finalTestDataSet;
    }

    public String getMissingProcessMode() {
        return missingProcessMode;
    }

    public String getResampleMethod() {
        return resampleMethod;
    }

    public String getFeatureSelectionMode() {
        return featureSelectionMode;
    }
        
    public String getTrainFileName() {
        return trainFileName;
    }

    public String getTestFileName() {
        return testFileName;
    }

    private static void generateDataSetPairs() {
        
        String matrix[][] = {
            
            {resampleNone,featureSelectionNo},
            {resampleNone,featureSelectionA},
            {resampleNone,featureSelectionB},
            
            {resampleUnder,featureSelectionNo},
            {resampleUnder,featureSelectionA},
            {resampleUnder,featureSelectionB},
            
            {resampleOver, featureSelectionNo},
            {resampleOver, featureSelectionA},
            {resampleOver, featureSelectionB},
            
            {resampleMatrix, featureSelectionNo},
            {resampleMatrix, featureSelectionA},
            {resampleMatrix, featureSelectionB}
            
        };
        
        String mergeFilePath = Main.OrangeProcessedDSHome+"/mergedFile.arff";
        DataSetPair ds1 = new DataSetPair();
        for (String[] row : matrix) {
            try {
                ds1.setFeatures(row[0], row[1]);
                ds1.processTrainRawData();
                ds1.processTestDataSet();
                
                Util.DataSetFiles dsFiles = new Util.DataSetFiles(ds1.getTrainFileName(), ds1.getTestFileName(), mergeFilePath);
                //merge the 2 files
                Util.mergeTrainTestFiles(dsFiles);
                Instances mergeData = null;
//                //numeric to nominal, to be delete ???
//                mergeData = ConverterUtils.DataSource.read(mergeFilePath);
//                Instances transformedDS = Util.transformNum2Nominal(mergeData, columns2Nominal);
//                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mergeFilePath)))) {
//                    writer.write(transformedDS.toString());
//                }
                       
                mergeData = ConverterUtils.DataSource.read(mergeFilePath);
                //normalize the 2 files together
                Instances normalizeData = Util.normalizeDS(mergeData);
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mergeFilePath)))) {
                    writer.write(normalizeData.toString());
                }
                
                
                
                //split them
                Util.splitTrainTestFiles(dsFiles);
            }
       
            catch (Exception ex) {
                Logger.getLogger(DataSetPair.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public static void main(String args[]){
        File dir = new File(Main.OrangeProcessedDSHome);
        if(!dir.exists()){
            Main.logging("No Processed Datasets, generate them...");
            dir.mkdirs();
            generateDataSetPairs();
        }
        
        generateDataSetPairs();
    }

}
