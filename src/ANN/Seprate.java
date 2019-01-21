package ANN;

import java.io.*;
import java.util.*;

import com.opencsv.CSVReader;

public class Seprate {
	public static ArrayList<ArrayList<String>> getTrainingData (String inputPath, ArrayList<ArrayList<String>> testData, double percent) {
		
		String csvFile = inputPath;
		CSVReader reader = null;
		String [] line;
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>> ();
		
		try {
			reader = new CSVReader (new FileReader (csvFile));
			
//			Read the data set
            while ((line = reader.readNext()) != null) {
            	ArrayList<String> dataLine = new ArrayList<String> ();
            	for (int i=0; i<line.length; i++) {
            		dataLine.add(line[i]);
            	}
            	data.add(dataLine);
            }
            
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		int total = data.size();
		List<Integer> numbers = new ArrayList<Integer> ();
		for (int i=0; i<total; i++) {
			numbers.add(i);
		}
		
		ArrayList<ArrayList<String>> trainingData = new ArrayList<ArrayList<String>> ();
		double _percent = percent / 100;
		int max = (int) (_percent * total);
		for (int i=0; i<max; i++) {
			Collections.shuffle(numbers);
			int index = numbers.remove(0);
			trainingData.add(data.get(index));
		}
		
		for (int i : numbers) {
			testData.add(data.get(i));
		}
		
		return trainingData;
		
	}

}
