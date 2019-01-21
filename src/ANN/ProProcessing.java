package ANN;

import java.io.*;
import java.util.*;

import com.opencsv.*;

public class ProProcessing {
	/*
	public void read (String csvPath) {
		String csvFile = csvPath;
		CSVReader reader = null;
		String [] line;
		try {
			reader = new CSVReader (new FileReader (csvFile));
			
//			Read the data set
            while ((line = reader.readNext()) != null) {
            	Instance a = new Instance(line.length);
            	for (int i=0; i<line.length; i++) {
            		if (line[i].equals("1")) {
            			a.ins[i] = true;
            		} else {
            			a.ins[i] = false;
            		}
            	}
            	data.add(a);
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	*/
	
	private static boolean isNumeric(String s) {
		if (s != null && !"".equals(s.trim()))
			return s.matches("^[0-9]*$");
		else
			return false;
	}
	
	private static double standardize (double old, double avg, double sum, int numLines) {
		double var = sum / ((double)numLines-1);
		double std = Math.sqrt(var);
		return (old - avg) / std;
	}
	
	public static Map<Double, String> preProcess(String inputPath, String outputPath) {
		
		String csvFile = inputPath;
		CSVReader reader = null, reader2 = null, reader3 = null, reader4 = null, reader5 = null;
		String [] line, line2, line3, line4, line5;
		
		int numFeatures = 0;
		int numLines = 0;
		
//		get the number of features
		try {
			reader = new CSVReader (new FileReader (csvFile));
			
			while ((line = reader.readNext()) != null) {
				if (line.length > numFeatures) {
					numFeatures = line.length;
				}
			}
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		
//		recognize whice colunm is nummeric
		boolean[] isNum = new boolean[numFeatures];
		for (int i=0; i<numFeatures; i++) {
			isNum[i] = true;
		}
		
		try {
			reader5 = new CSVReader (new FileReader (csvFile));
			
	        while ((line5 = reader5.readNext()) != null) {
	        	if (line5.length < numFeatures) {
	        		continue;
	        	}
	        	
	        	boolean flagSkip = false;
	        	for (int i=0; i<line5.length; i++) {
	        		if (line5[i].isEmpty() || line5[i]==null) {
	        			flagSkip = true;
	        			break;
	        		}
	        	}
	        	
	        	if (flagSkip) {
	        		continue;
	        	}
	        	
	        	numLines ++;
	        	for (int i=0; i<line5.length; i++) {
	        		if (!isNumeric(line5[i])) {
	        			isNum[i] = false;
	        		}
	        	}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		prepare containers for sum of numeric values and categorical values
		double[] total = new double[numFeatures];
		for (int i=0; i<numFeatures; i++) {
			total[i] = 0;
		}
		
		List<HashSet<String>> ctgList = new ArrayList<HashSet<String>> ();
		for (int i=0; i<numFeatures; i++) {
			ctgList.add(new HashSet<String> ());
		}
		
//		Read the data set and collect information for standardization and scaling
		List<HashMap<String, Double>> encodeList = new ArrayList<HashMap<String, Double>> ();
		double avg[] = new double[numFeatures];
		try {
			reader2 = new CSVReader (new FileReader (csvFile));
			
            while ((line2 = reader2.readNext()) != null) {
            	if (line2.length < numFeatures) {
            		continue;
            	}
            	
            	boolean flagSkip = false;
            	for (int i=0; i<line2.length; i++) {
            		if (line2[i].isEmpty() || line2[i]==null) {
            			flagSkip = true;
            			break;
            		}
            	}
            	
            	if (flagSkip) {
            		continue;
            	}
            	
            	for (int i=0; i<line2.length; i++) {
            		if (isNum[i]) {
            			total[i] += Double.parseDouble(line2[i]);
            		} else {
            			ctgList.get(i).add(line2[i]);
            		}
            	}
            }
            
            
//            encoding map for categorical variables
            for (int i=0; i<numFeatures; i++) {
            	encodeList.add(new HashMap<String, Double> ());
            }
            for (int i=0; i<encodeList.size(); i++) {
            	int j = 0;
            	for (String s : ctgList.get(i)) {
            		encodeList.get(i).put(s, (double)j);
            		j++;
            	}
            }
            
            
//            compute the average for numeric features
            for (int i=0; i<avg.length; i++) {
            	avg[i] = total[i] / (double)numLines;
            }
        
//			FileWriter w = new FileWriter ("e:/lolol.data"£¬ true);
//			Try.writeLine(w, list);
//			w.flush();
//			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		compute the variance
		double[] sum = new double [numFeatures];
		for (int i=0; i<numFeatures; i++) {
			sum[i] = 0;
		}
		
		try {
			reader3 = new CSVReader (new FileReader (csvFile));
			
			while ((line3 = reader3.readNext()) != null) {
				if (line3.length < numFeatures) {
            		continue;
            	}
            	
            	boolean flagSkip = false;
            	for (int i=0; i<line3.length; i++) {
            		if (line3[i].isEmpty() || line3[i]==null) {
            			flagSkip = true;
            			break;
            		}
            	}
            	
            	if (flagSkip) {
            		continue;
            	}
            	
            	for (int i=0; i<line3.length; i++) {
            		if (isNum[i]) {
            			sum[i] += (Double.parseDouble(line3[i])-avg[i]) * (Double.parseDouble(line3[i])-avg[i]);
            		}
            	}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		output the scaled data set
		
		try {
			FileWriter w = new FileWriter (outputPath, true);
			reader4 = new CSVReader (new FileReader (csvFile));
			
			while ((line4 = reader4.readNext()) != null) {
				if (line4.length < numFeatures) {
            		continue;
            	}
            	
            	boolean flagSkip = false;
            	for (int i=0; i<line4.length; i++) {
            		if (line4[i].isEmpty() || line4[i]==null) {
            			flagSkip = true;
            			break;
            		}
            	}
            	
            	if (flagSkip) {
            		continue;
            	}
            	
            	List<String> list = new ArrayList<String> ();
            	for (int i=0; i<line4.length; i++) {
            		if (isNum[i]) {
            			list.add(String.valueOf(standardize (Double.parseDouble (line4[i]), avg[i], sum[i], numLines)));
            		} else {
            			list.add(encodeList.get(i).get(line4[i]).toString());
            		}
            	}
            	
            	WriteCSV.writeLine(w, list);
			}
			
			w.flush();
			w.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<Double, String> label = new HashMap<Double, String> ();
		for (Map.Entry<String, Double> entry : encodeList.get(numFeatures-1).entrySet()) {
			label.put(entry.getValue(), entry.getKey());
		}
		return label;
	}

	public static void main(String[] args) {
//		preProcess(String inputPath, String outputPath)
		Map a = preProcess("D:\\CS17Fall\\Machine Learning\\Assignment\\Assignment3\\iris.data", "D:\\CS17Fall\\Machine Learning\\Assignment\\Assignment3\\iris_processed.data");
	}
}
