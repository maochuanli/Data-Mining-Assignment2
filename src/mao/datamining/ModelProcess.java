/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mao.datamining;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mao.datamining.ModelProcess.folds;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Utils;

/**
 * java -Xmx10g -Djava.util.logging.config.file=c:/DS1/logging.properties -jar
 * datamining.jar c:/DS1/processed/train.arff c:/DS1/processed/test.arff
 * classifier opt1 opt2 opt3
 *
 * @author mao
 */
public class ModelProcess {

    //One logger
    public static final Logger ProcessLogger = Logger.getLogger(ModelProcess.class.getName());
    //How many folds for cross validatioin
    static int folds = 10;
    private String caseNum;
    private boolean useCostMatrix = false;

    public static void main(String[] args) {
        String trainDSPath = null;
        String testDSPath = null;
        String classifier = null;
        String optionsFilePath = null;
        String reportFilePath = null;
        String testCaseNum = null;
        boolean runDummy = false;

//        String options[] = null;
        //weka.classifiers.meta.Bagging -P 100 -S 1 -I 10 -W weka.classifiers.lazy.IBk -- 
        //    -K 1 -W 0 -A "weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\"" 
        //weka.classifiers.meta.AdaBoostM1 -P 100 -S 1 -I 10 -W weka.classifiers.lazy.IBk -- -K 1 -W 0 -A "weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\""
        ArrayList<String> optionList = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].endsWith(".arff")) {

                trainDSPath = args[i];
                testDSPath = args[++i];
                classifier = args[++i];
                optionsFilePath = args[++i];
                reportFilePath = args[++i];
                testCaseNum = args[++i];
                ++i;
                if (args[i] != null && args[i].equals("dummy")) {
                    runDummy = true;
                }

                break;
            }
        }

        logging("trainDSPath file path: " + trainDSPath);
        logging("testDSPath file path: " + testDSPath);
        logging("classifier: " + classifier);
        logging("optionsFilePath file path: " + optionsFilePath);
        logging("report file path: " + reportFilePath);
        logging("Testcase Num: " + testCaseNum);
        logging("Dummy Run: " + runDummy);

        ModelProcess m = new ModelProcess();
        m.setCaseNum(testCaseNum);
        if (trainDSPath.contains(DataSetPair.resampleMatrix)) {
            m.useCostMatrix = true;
        }
        logging("Use Cost Matrix: " + m.useCostMatrix);

        String testCaseDetailFile = optionsFilePath + ".summary.txt";

        try {
            FileInputStream is = new FileInputStream(optionsFilePath);
            InputStreamReader r = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(r);
            String line = null;
            while ((line = reader.readLine()) != null) {
                optionList.add(line);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ModelProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ModelProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        ModelProcess.logging(trainDSPath + "," + testDSPath + "," + classifier);

        DataSetPair ds = new DataSetPair(trainDSPath, testDSPath);
        ds.loadBothDataSets();
        String optionArray[] = new String[optionList.size()];
        for (int i = 0; i < optionList.size(); i++) {
            optionArray[i] = optionList.get(i);
        }
        //run test case
        m.runTestCase(ds, classifier, reportFilePath, optionArray, testCaseNum, runDummy, testCaseDetailFile);
    }

    /**
     * To create and verify the classifier models.
     *
     * @param classifierClazz
     * @param options
     */
    private void runTestCase(DataSetPair ds, String classifierClazz, String reportFilePath, String[] options, String caseNum, boolean runDummy, String testCaseDetailFile) {
        
        String optionsCopy[] = new String[options.length];
        System.arraycopy(options, 0, optionsCopy, 0, options.length);
        //write the summary
        FileOutputStream testCaseSummaryOut = null;
        try {
            testCaseSummaryOut = new FileOutputStream(testCaseDetailFile);
            testCaseSummaryOut.write( ("Train File: "+ds.getTrainFileName()).getBytes());
            testCaseSummaryOut.write("\n\n".getBytes());
        } catch (Exception ex) {
            Logger.getLogger(ModelProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Prepare the TestResult
        TestResult result = new TestResult();
        result.setCaseNum(caseNum);
        result.setMissingValueMode(ds.getMissingProcessMode());
        result.setResampleMode(ds.getResampleMethod());
        result.setFeatureSelectMode(ds.getFeatureSelectionMode());
        result.setClassifier(classifierClazz);
        StringBuilder optionsStr = new StringBuilder();
        for (String s : optionsCopy) {
            if (s == null) {
                break;
            }
            optionsStr.append(s).append(" ");
        }
        result.setClassifierOptions(optionsStr.toString());

        //get the training and test data sets
        Instances finalTrainDataSet = ds.getFinalTrainDataSet();
        Instances finalTestDataSet = ds.getFinalTestDataSet();
        //start building and testing
        ModelProcess.logging("\n\n\n=========================================START=================================================\n"
                + "======= " + classifierClazz + "," + optionsStr.toString() + "========\n"
                + "=========================================START=================================================");
        if (runDummy) {
            ModelProcess.logging("Dummy Run the process");
        } else {

            Classifier classifier = null;
            String tmpClazz = null;
            String tmpOption[] = null;
            //if use matrix, 
            //weka.classifiers.meta.CostSensitiveClassifier -cost-matrix "[0.0 2.0; 5.0 0.0]" -S 1 -W weka.classifiers.trees.J48 -- -C 0.25 -M 2
            if (this.useCostMatrix) {
                List<String> costOptionsList = new ArrayList<>();
                costOptionsList.add("-cost-matrix");
                costOptionsList.add("\"[0.0 3.0; 30.0 0.0]\"");
                costOptionsList.add("-S");
                costOptionsList.add("1");
                costOptionsList.add("-W");
                costOptionsList.add(classifierClazz);
                costOptionsList.add("--");
                for (String s : optionsCopy) {
                    costOptionsList.add(s);
                }
                String newOptions[] = new String[costOptionsList.size()];
                for (int i = 0; i < newOptions.length; i++) {
                    newOptions[i] = costOptionsList.get(i);
                }
                tmpClazz = "weka.classifiers.meta.CostSensitiveClassifier";
                tmpOption = newOptions;
            } else {
                tmpClazz = classifierClazz;
                tmpOption = optionsCopy;
            }//end create the classifier
            
            try {
                
                try {
                    //finalTrainDataSet
                    testCaseSummaryOut.write( ("Data Set Summary: "+finalTrainDataSet.toSummaryString()+"\n\n").getBytes());
                    testCaseSummaryOut.write( ("classifier: "+tmpClazz+"\n").getBytes());
                    testCaseSummaryOut.write( ("options: "+Arrays.toString(tmpOption)+"\n\n").getBytes());
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ModelProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                classifier = (Classifier) Utils.forName(Classifier.class, tmpClazz, tmpOption);
            } catch (Exception ex) {
                Logger.getLogger(ModelProcess.class.getName()).log(Level.SEVERE, null, ex);
            }//end create the classifier

//            this.testCV(classifier, finalTrainDataSet, testCaseSummaryOut, result);
            this.testWithExtraDS(classifier, finalTrainDataSet, finalTestDataSet, testCaseSummaryOut, result);

            try {
                testCaseSummaryOut.close();
            } catch (IOException ex) {
                Logger.getLogger(ModelProcess.class.getName()).log(Level.SEVERE, null, ex);
            }

            ModelProcess.logging("========================================END==================================================\n"
                    + result.toString()
                    + "========================================END==================================================");

            this.lockFile(reportFilePath);
            try (FileOutputStream fout = new FileOutputStream(reportFilePath, true)) {
                fout.write(result.toString().getBytes());
                fout.write("\n".getBytes());
                fout.flush();
                fout.close();
            } catch (Exception ex) {
                Main.logging(null, ex);
            }
            this.unLockFile(reportFilePath);
        }
    }

    private boolean lockFile(String reportFilePath) {
        String lockFile = reportFilePath + ".lock";

        while (new File(lockFile).exists()) {
            System.out.println("[" + this.getCaseNum() + "] Waiting 100 milliseconds...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ModelProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try (FileOutputStream fout = new FileOutputStream(lockFile)) {
            fout.write(this.getCaseNum().getBytes());
            fout.flush();
            fout.close();
        } catch (Exception ex) {
            Main.logging(null, ex);
        }

        return true;
    }

    private void unLockFile(String reportFilePath) {
        String lockFilePath = reportFilePath + ".lock";
        File lockFile = new File(lockFilePath);
        if (lockFile.exists()) {
            lockFile.delete();
        }
    }

    private static void logging(String msg) {
        ProcessLogger.info(msg);
    }

    private static void logging(String msg, Throwable t) {
        ProcessLogger.log(Level.SEVERE, null, t);
    }

    private String getCaseNum() {
        return this.caseNum;
    }

    private void setCaseNum(String n) {
        this.caseNum = n;
    }

    private void testWithExtraDS(Classifier classifier, Instances finalTrainDataSet, Instances finalTestDataSet, FileOutputStream testCaseSummaryOut,TestResult result) {
        //Use final training dataset and final test dataset
        double confusionMatrix[][] = null;
        
        long start, end, trainTime = 0, testTime = 0;
            if (finalTestDataSet != null) {
            try {
                //counting training time
                start = System.currentTimeMillis();
                classifier.buildClassifier(finalTrainDataSet);
                end = System.currentTimeMillis();
                trainTime += end - start;
                
                //counting test time
                start = System.currentTimeMillis();
                Evaluation testEvalOnly = new Evaluation(finalTrainDataSet);
                testEvalOnly.evaluateModel(classifier, finalTestDataSet);
                end = System.currentTimeMillis();
                testTime += end - start;

                testCaseSummaryOut.write("=====================================================\n".getBytes());
                testCaseSummaryOut.write((testEvalOnly.toSummaryString("=== Test Summary ===", true)).getBytes());
                testCaseSummaryOut.write("\n".getBytes());
                testCaseSummaryOut.write((testEvalOnly.toClassDetailsString("=== Test Class Detail ===\n")).getBytes());
                testCaseSummaryOut.write("\n".getBytes());
                testCaseSummaryOut.write((testEvalOnly.toMatrixString("=== Confusion matrix for Test ===\n")).getBytes());
                testCaseSummaryOut.flush();

                confusionMatrix = testEvalOnly.confusionMatrix();
                result.setConfusionMatrix4Test(confusionMatrix);

                result.setAUT(testEvalOnly.areaUnderROC(1));
                result.setPrecision(testEvalOnly.precision(1));
                result.setRecall(testEvalOnly.recall(1));
            } catch (Exception e) {
                ModelProcess.logging(null, e);
            }
            result.setTrainingTime(trainTime);
            result.setTestTime(testTime);
        }//using test data set , end
        
    }

    private void testCV(Classifier classifier, Instances finalTrainDataSet, FileOutputStream testCaseSummaryOut,TestResult result) {
        long start, end, trainTime = 0, testTime = 0;
        Evaluation evalAll = null;
        double confusionMatrix[][] = null;
        // randomize data, and then stratify it into 10 groups
            Random rand = new Random(1);
            Instances randData = new Instances(finalTrainDataSet);
            randData.randomize(rand);
            if (randData.classAttribute().isNominal()) {
                //always run with 10 cross validation
                randData.stratify(folds);
            }

        try {
                evalAll = new Evaluation(randData);
                for (int i = 0; i < folds; i++) {
                    Evaluation eval = new Evaluation(randData);
                    Instances train = randData.trainCV(folds, i);
                    Instances test = randData.testCV(folds, i);
                    //counting traininig time
                    start = System.currentTimeMillis();
                    Classifier j48ClassifierCopy = Classifier.makeCopy(classifier);
                    j48ClassifierCopy.buildClassifier(train);
                    end = System.currentTimeMillis();
                    trainTime += end - start;

                    //counting test time
                    start = System.currentTimeMillis();
                    eval.evaluateModel(j48ClassifierCopy, test);
                    evalAll.evaluateModel(j48ClassifierCopy, test);
                    end = System.currentTimeMillis();
                    testTime += end - start;
                }

            } catch (Exception e) {
                ModelProcess.logging(null, e);
            }//end test by cross validation
            
            
            // output evaluation
            try {
                ModelProcess.logging("");
                //write into summary file
                testCaseSummaryOut.write((evalAll.toSummaryString("=== Cross Validation Summary ===", true)).getBytes());
                testCaseSummaryOut.write("\n".getBytes());
                testCaseSummaryOut.write((evalAll.toClassDetailsString("=== " + folds + "-fold Cross-validation Class Detail ===\n")).getBytes());
                testCaseSummaryOut.write("\n".getBytes());
                testCaseSummaryOut.write((evalAll.toMatrixString("=== Confusion matrix for all folds ===\n")).getBytes());
                testCaseSummaryOut.flush();

                confusionMatrix = evalAll.confusionMatrix();
                result.setConfusionMatrix10Folds(confusionMatrix);
            } catch (Exception e) {
                ModelProcess.logging(null, e);
            }
    }

}
