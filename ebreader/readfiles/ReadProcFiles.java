package readfiles;
import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import enums.Commands;
import enums.Metrics;
import enums.interfaces.AppName;
import enums.interfaces.ExpData;
import utils.ConversionTools;

public class ReadProcFiles extends ReadFiles{
   
    private enum MetricsData implements Metrics{
        DATA_MEMORY_LOW("pss_low"),
        DATA_MEMORY_MED("pss_med"),
        DATA_MEMORY_HIGH("pss_high");
    
        private String value;
        MetricsData(String value){
            this.value = value;
        }
        public String value(){
            return value;
        }
    }

    public static Metrics[] metrics(){
        return MetricsData.values();
    }

    private double memDataLow;
    private double memDataMed;
    private double memDataHigh;

    public ReadProcFiles(AppName framework, Commands metricCMD){
        super(framework, metricCMD);
    }

    public Map<Integer, Map<Metrics, Double>> readSingle(ExpData benchmark) throws InvalidParameterException{
        for (int i = 1; i < numberOfFiles + 1; i++) {
            mapMetrics = new HashMap<>();
            executeReadFile(i, benchmark);
            map.put(i,mapMetrics);
        }
        return map;
    }

    private void executeReadFile(int fileNumber, ExpData benchmark) {
        
        try ( Scanner scnr = new Scanner(getFile(benchmark, fileNumber));){
            while (scnr.hasNextLine()) {
                String line = scnr.nextLine();
                if(line.contains(framework.getAppName())){
                    getMemoryConsumption(scnr.nextLine());
                }
            }
            setMemoryConsumption();
        } catch (FileNotFoundException e) {
             e.printStackTrace();
        }  
    }

    private void setMemoryConsumption() {
        mapMetrics.put(MetricsData.DATA_MEMORY_LOW,memDataLow);
        mapMetrics.put(MetricsData.DATA_MEMORY_MED,memDataMed);
        mapMetrics.put(MetricsData.DATA_MEMORY_HIGH,memDataHigh);
        memDataHigh = 0;
        memDataLow = 0;
        memDataMed = 0;
    }

    private void getMemoryConsumption(String line) {
        if(line.contains("(Cached)") || !line.contains("TOTAL: 100% ")) return; 
        String bar = "/";
        String totalString = "TOTAL: 100% ";
        String memory = line.split(totalString)[1].substring(1).split(bar)[0]; //TOTAL: 100% (181MB-680MB-848MB/176MB-671MB-838MB over 4)
        
        for (int i = 0; i < 3; i++) {
            Double memoryData = ConversionTools.memoryInMB(memory.split("-")[i]);
            switch(i){
                case 0 -> memDataLow = memoryData;
                case 1 -> memDataMed = memoryData;
                case 2 -> memDataHigh = memoryData;
                default -> System.out.println("error");
            }
        }

    }

   
}