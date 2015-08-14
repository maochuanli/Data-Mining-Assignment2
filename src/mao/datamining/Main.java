/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mao.datamining;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mao.datamining.Boostings.votingClassifier;

/**
 *
 * @author Mao Chuan Li
 */
public class Main {

    //One logger

    public static final Logger MainLogger = Logger.getLogger(Main.class.getName());
    //Datasets Home
    
    public static String userHome = System.getProperty("user.home");
//    static{
//        
//        if(System.getProperty ("os.name").toLowerCase().contains("win")){
//            userHome = "D:";        
//        }
//    }
    
    public static final String dataMiningHome = userHome + "/Data.Mining";

    public static final String OrangeDSHome = dataMiningHome + "/DS1";
    public static final String OrangeProcessedDSHome = OrangeDSHome + "/processedDS";
    
    public static boolean useBoost = false;
    public static boolean runDummy = false;    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //default suite name
        String suiteName = "default.suite";
        String cases = "all";
        
        
        for(int i=0;i<args.length;i++){
            String s = args[i];
            if(s.equalsIgnoreCase("--boost")) {                
                useBoost = true;                
            }else if(s.equalsIgnoreCase("--suite")){
                suiteName = args[++i];                
            }else if(s.equalsIgnoreCase("--cases")){
                cases = args[++i];                
            }else if(s.equalsIgnoreCase("--dummy")){
                runDummy = true;  
            }
        }
        if(useBoost && suiteName.equalsIgnoreCase("default.suite")){
            suiteName = "boost.suite";
        }
        
        String[] testCaseArray = cases.split(",");
        
        System.out.println("User Home: " + userHome);
        System.out.println("OrangeProcessedDS Home: " + OrangeProcessedDSHome);
        
        System.out.println("Use Boosting Methods: " + useBoost);
        System.out.println("Test Suite: " + suiteName);
        System.out.println("Test Cases: " + cases);
        System.out.println("Dummy Run: " + runDummy);
        
        //process training dataset
        final Main main = new Main();
        List<DataSetPair> allProcessedDataSets = DataSetPair.getAllProcessedDataSets();
        List<Models.Model> allModels = Models.getAllModels();



        //Create a test suite by name;
        final TestSuite suite = new TestSuite(suiteName);
        if(suite.exists ()){// if suite exists, reload the tests,
            System.out.println("Suite: "+suite.getSuiteName()+" exists, load the current test cases...");
            suite.loadTests();

        }else{//otherwise, create a new suite
            System.out.println("Suite: "+suite.getSuiteName()+" NOT exists, create new test cases...");
            suite.init();
            try (FileOutputStream fout = new FileOutputStream(suite.getSuiteResultsFile())) {
                fout.write(TestResult.getHeadLine().getBytes());
                fout.write("\n".getBytes());
                fout.flush();
                fout.close();
            } catch (Exception ex) {
                Main.logging(null, ex);
            }
                        
            int caseNumber = 0;
            for (DataSetPair p : allProcessedDataSets) {//all 8 processed data sets
                Main.logging("======" + p.getMissingProcessMode() + "," + p.getResampleMethod() + "," + p.getFeatureSelectionMode() + "======");
                p.loadBothDataSets();

                if (useBoost) {
                    List<Boostings.Boosting> allBoostings = Boostings.getAllBoostings();
                    for (Boostings.Boosting s : allBoostings) {//all 2 boosting methods * 2 options
                        if(s.getBoostMethod().equals(votingClassifier)){//do not use emebedded models
                            Models.Model boostedModel = s.createBoostModel();
                            TestCase t = new TestCase(p, boostedModel.getClassifier());
                            t.setOptions(boostedModel.getOptions());
                            t.setCaseNumber(++caseNumber);
                            suite.addTestCase(t);
                            continue;
                        }
                        
                        for (Models.Model m : allModels) {  //all classifier * 2 options
                            s.setUsedModel(m);
                            Models.Model boostedModel = s.createBoostModel();
                            TestCase t = new TestCase(p, boostedModel.getClassifier());
                            t.setOptions(boostedModel.getOptions());
                            t.setCaseNumber(++caseNumber);
                            suite.addTestCase(t);
                        }
                    }
                } else {
                    for (Models.Model m : allModels) {  //all classifier * 2 options
                        TestCase t = new TestCase(p, m.getClassifier());
                        t.setOptions(m.getOptions());
                        t.setCaseNumber(++caseNumber);
                        suite.addTestCase(t);
                    }
                }
            }
            suite.saveTests();
        }//end else
            
        
        final List<TestCase> testCases = suite.getAllTests();
        final List<TestCase> testCasesSubSet = new ArrayList<>();
        if(testCaseArray[0].equalsIgnoreCase("all")){
            for(TestCase t: testCases){
                testCasesSubSet.add(t);
            }
            System.out.println("Run Full Set Test Cases: ");
            for(TestCase t: testCasesSubSet){
                System.out.print(t.getCaseNumber()+",");
            }
            System.out.println();
        }else{
            for(TestCase t: testCases){
                int caseNum = t.getCaseNumber();
                for(String no: testCaseArray){
                    if( (caseNum+"").equalsIgnoreCase(no)){
                        testCasesSubSet.add(t);
                        break;
                    }
                }                
            }
            System.out.println("Run Sub Set Test Cases: ");
            for(TestCase t: testCasesSubSet){
                System.out.print(t.getCaseNumber()+",");
                t.setCompleted(false);
            }
            System.out.println();
        }
        //start all remaining test cases, or all if it is a fresh run
        new Thread(){
            
            @Override
            public void run(){
                final HashSet<TestCase> runningTestCaseSet = new HashSet<>();
                
                StringBuilder errorCaseNumbers = new StringBuilder();
                List<TestCase> testCasesToRun = new ArrayList<>();
                testCasesToRun.addAll(testCasesSubSet);
                
                for(TestCase t: testCasesSubSet){
                    if(t.isCompleted()) testCasesToRun.remove(t);
                }
                
                int leftTestCasesNum = testCasesToRun.size();
                if(leftTestCasesNum == testCasesSubSet.size()){
                    System.out.println("Fresh Run...");
                }else{
                    System.out.println("Resume Run with ["+leftTestCasesNum+"] Cases to run.");
                    for(TestCase t: testCasesToRun){
                        System.out.print(t.getCaseNumber()+",");
                    }
                    System.out.println();
                }
                
                OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean( OperatingSystemMXBean.class);
                boolean printDot = false;
                long start = System.currentTimeMillis(), end=0;
                int runningCount = 0;
                while( testCasesToRun.size()>0 ){
                    long total = osBean.getTotalPhysicalMemorySize()/(1024*1024);
                    long freeTotal = osBean.getFreePhysicalMemorySize()/(1024*1024);
                
                    for(TestCase t: runningTestCaseSet){
                        if(t.isCompleted()) continue;
                        try{
                            int exitV = t.getProcess().exitValue();
                            if(exitV != 0) errorCaseNumbers.append(t.getCaseNumber()).append(",");
                            System.out.println("TestCase["+t.getCaseNumber()+"] completed: "+ exitV);
                            suite.setTestCompleted(t,true);
                            runningCount--; //decrease the count
                        }catch(Exception e){}
                    }

                    if(printDot) System.out.print(">");
                    else {
                        System.out.println("Free: "+freeTotal +", Total Memory: "+total+", Running Processes: "+runningCount);
                        printDot = true;
                    }

                    if( (freeTotal*100)/total > 50 && runningCount < 10){ //maximal process in parallel == 10
                        System.out.println("Has more than half size free memory , run process...");
                        TestCase t = testCasesToRun.remove(0);                        
                        Process p = main.createSubProcess(suite, t);
                        t.setProcess(p);
                        System.out.println("Created a new process....\n\n");
                        runningCount++;//increase
                        runningTestCaseSet.add(t);
                        printDot = false;
                    }
                    try {                        
                        Thread.sleep(500);
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }                    
                }//end while

                System.out.println("Waiting for all running test cases to finish...");
                for(TestCase t: runningTestCaseSet){
                    if(!t.isCompleted()) System.out.print(t.getCaseNumber()+",");
                }
                System.out.println();
                
                for(TestCase t: runningTestCaseSet){
                    if(t.isCompleted()) continue;
                    try {
                        t.getProcess().waitFor();
                        suite.setTestCompleted(t,true);
                        int exitV = t.getProcess().exitValue();
                        if(exitV != 0) errorCaseNumbers.append(t.getCaseNumber()).append(",");
                        System.out.println("TestCase["+t.getCaseNumber()+"] completed: "+ exitV);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                end=System.currentTimeMillis();
                System.out.println("\n\nError Case Numbers: ["+errorCaseNumbers+"]");
                System.out.println("===============DONE, Duration: "+(end-start)/1000+" seconds============\n\n");
            }
        }.start();

        
    }

    
    
    public static void logging(String msg) {
        MainLogger.log(Level.INFO, msg);
    }

    public static void logging(String msg, Exception e) {
        MainLogger.log(Level.SEVERE, null, e);
    }

    private Process createSubProcess(TestSuite suite, TestCase test) {

//        java -Xmx10g -Djava.util.logging.config.file=c:/DS1/logging.properties 
//            -classpath "C:/NetBeansProjects/Data.Mining/build/classes;C:/Program Files/Weka-3-6/weka.jar" mao.datamining.ModelProcess c:/DS1/processed/train.arff c:/DS1/processed/test.arff classifier opt1 opt2 opt3
        ProcessBuilder pb = new ProcessBuilder();
        List<String> cmdList = new ArrayList<>();
        cmdList.add("java");
//        cmdList.add("-Xmx25g");
        if(System.getProperty ("os.name").toLowerCase().contains("win")){
            cmdList.add("-Djava.util.logging.config.file=\"" + dataMiningHome + "/logging.sub.properties\"");
            cmdList.add("-classpath");
            cmdList.add("\""+dataMiningHome + "/Data.Mining.jar;" + dataMiningHome + "/lib/weka.jar\"");
            cmdList.add("mao.datamining.ModelProcess");

            cmdList.add("\""+test.getDataSetPair().getTrainFileName()+"\"");
            cmdList.add("\""+test.getDataSetPair().getTestFileName()+"\"");
            cmdList.add("\""+test.getClassifier()+"\"");
            cmdList.add("\""+test.getOptionsFilePath()+"\"");
            cmdList.add("\""+suite.getSuiteResultsFile().getAbsolutePath()+"\"");            
        }else{
            cmdList.add("-Djava.util.logging.config.file=" + dataMiningHome + "/subProcess.properties");
            cmdList.add("-classpath");
            cmdList.add(dataMiningHome + "/Data.Mining.jar:" + dataMiningHome + "/lib/weka.jar");
            cmdList.add("mao.datamining.ModelProcess");

            cmdList.add(test.getDataSetPair().getTrainFileName());
            cmdList.add(test.getDataSetPair().getTestFileName());
            cmdList.add(test.getClassifier());
            cmdList.add(test.getOptionsFilePath());
            cmdList.add(suite.getSuiteResultsFile().getAbsolutePath());
            cmdList.add(test.getCaseNumber()+"");
            if(runDummy){
                cmdList.add("dummy");
            }else{
                cmdList.add("realRun");
            }
            
        }
       
        try {
            pb.command(cmdList);
            Process process = pb.start();
            List<String> command = pb.command();
            String cmdLine = "";
            for (String s : command) {
                cmdLine += s + " ";
            }
            Main.logging("Run TestCase["+test.getCaseNumber()+"]\n" + cmdLine);
            return process;
        } catch (Exception ex) {
            Main.logging(null, ex);
        }

        return null;
    }
}
