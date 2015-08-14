/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mao.datamining;

import java.io.File;
import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mao Chuan Li
 */
public class TestOptions {

    public static void main(String[] args) {
        new TestOptions().testMerge();

    }

    public void testMerge(){
        String trainF= "D:\\Data.Mining\\DS1\\processedDS\\none_none_none_train.arff";
        String testF= "D:\\Data.Mining\\DS1\\processedDS\\none_none_none_test.arff";
        String mergF = "D:\\Data.Mining\\DS1\\processedDS\\none_none_none_both.arff";
        
        Util.mergeTrainTestFiles(new Util.DataSetFiles(trainF, testF, mergF));
        trainF= "D:\\Data.Mining\\DS1\\processedDS\\none_none_none_train_new.arff";
        testF= "D:\\Data.Mining\\DS1\\processedDS\\none_none_none_test_new.arff";
        Util.splitTrainTestFiles(new Util.DataSetFiles(trainF, testF, mergF));
    }
    
    public void testDuplicateRecords() {
        Reader inputStreamReader = null;
        try {
            String fileName = "D:\\AUT\\AUT.Class.Materials\\Data Mining\\Assignment2222\\Ds1\\orange_train.arff";
            Set set = new HashSet();
            inputStreamReader = new InputStreamReader(new FileInputStream(fileName));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = reader.readLine()) != null) {
                int length = line.length();
                if(line.endsWith(",-1")) {
                    line = line.substring(0,length-3);
                }else if(line.endsWith(",1")) {
                    line = line.substring(0,length-2);
                }
                
                System.out.println(line);
                set.add(line);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestOptions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestOptions.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                inputStreamReader.close();
            } catch (IOException ex) {
                Logger.getLogger(TestOptions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void testOSFeatures() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long total = osBean.getTotalPhysicalMemorySize();
        long totalSwap = osBean.getTotalSwapSpaceSize();
        long freeTotal = osBean.getFreePhysicalMemorySize();
        long freeSwap = osBean.getFreeSwapSpaceSize();

        System.out.println("total: " + total / (1024 * 1024));
        System.out.println("freeTotal: " + freeTotal / (1024 * 1024));
        System.out.println("totalSwap: " + totalSwap / (1024 * 1024));
        System.out.println("freeSwap: " + freeSwap / (1024 * 1024));

        /* Total number of processors or cores available to the JVM */
        System.out.println("Available processors (cores): "
                + Runtime.getRuntime().availableProcessors());

        /* Total amount of free memory available to the JVM */
        System.out.println("Free memory (bytes): "
                + Runtime.getRuntime().freeMemory());

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        System.out.println("Maximum memory (bytes): "
                + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));

        /* Total memory currently in use by the JVM */
        System.out.println("Total memory (bytes): "
                + Runtime.getRuntime().totalMemory());

        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();

        /* For each filesystem root, print some info */
        for (File root : roots) {
            System.out.println("File system root: " + root.getAbsolutePath());
            System.out.println("Total space (bytes): " + root.getTotalSpace());
            System.out.println("Free space (bytes): " + root.getFreeSpace());
            System.out.println("Usable space (bytes): " + root.getUsableSpace());
        }
    }
}
