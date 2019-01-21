package ANN;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.opencsv.CSVReader;

public class READ {
	public void read (String csvPath,
			  ArrayList<ArrayList<String>> data) {


		String csvFile = csvPath;
		CSVReader reader = null;
		String [] line;
		try {
			reader = new CSVReader (new FileReader (csvFile));
	
	
			//	Read the data set
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
	}

	// convert the data type
	public double[][] convertType(ArrayList<ArrayList<String>> data) {
		double[][] res = new double[data.size()][data.get(0).size()];
		for (int i = 0; i < data.size(); i++) {
			for (int j = 0; j < data.get(0).size(); j++) {
				res[i][j] = Double.parseDouble(data.get(i).get(j));
			}
		}
		return res;
	}
	
	public int getOutputSize(double[][] data) {
		Set<Double> set = new HashSet<Double>();
		for (int i = 0; i < data.length; i++) {
			if (!set.contains(data[i][data[0].length - 1])) {
				set.add(data[i][data[0].length - 1]);
			}
		}
		return set.size();
	}
	
	// generate the input target
	public double[] getTarget(double i, int size) {
		int index = (int) i;
		double[] res = new double[size];
		res[index] = 1.0;
		return res;
	}
}
