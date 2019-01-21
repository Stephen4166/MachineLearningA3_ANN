package ANN;
import java.io.FileReader;

import java.io.IOException;
import java.util.*;

import com.opencsv.CSVReader;



public class ANNet {
	
	Map<Double, String> dictionary;
	Map<Integer, ArrayList<Integer>> preLayer = new HashMap<Integer, ArrayList<Integer>>(); //done
	Map<Integer, ArrayList<Integer>> nextLayer = new HashMap<Integer, ArrayList<Integer>>(); //done
	double[][] weight; //done
	double[] delta;
	double[] delWeight;
	double[] outputR;

	Map<Integer, ArrayList<Integer>> layer = new HashMap<Integer, ArrayList<Integer>>(); //done
	ANNet() {
		
	}
	ANNet(int[] scale, int input_size, int output_size, double[][] data) {
		Random r = new Random();
		// initialize weight
		int count = 0;
		for (int i = 0; i < scale.length; i++) {
			count += scale[i];
		}
		count = count + input_size + output_size + 1; 
		this.weight = new double[count][count];
		for (int i = 0; i < count ; i++) {
			for (int j = 0; j < count; j++) {
				this.weight[i][j] = -1 + (2) * r.nextDouble();;
			}
		}
		for (int i = 0; i < count; i++) {
			this.weight[i][2] = 0.0;
			this.weight[2][i] = 0.0;
		}
		// initialize layer
		ArrayList<Integer> inputLayer = new ArrayList<Integer>();
		for (int i = 1; i <= input_size; i++) {
			inputLayer.add(i);
		}
		layer.put(0, inputLayer);
		int temp = input_size + 1;
		for (int i = 0; i < scale.length; i++) {
			ArrayList<Integer> newLayer = new ArrayList<Integer>();
			for (int j = 0; j < scale[i]; j++) {
				newLayer.add(temp);
				temp++;
			}
			layer.put(i + 1, newLayer);
		}
		ArrayList<Integer> outputLayer = new ArrayList<Integer>();
		for (int i = temp; i < count; i++) {
			outputLayer.add(i);
		}
		layer.put(scale.length + 1, outputLayer);
		
		// initialize preLayer
		for (int i = 0; i < layer.get(0).size(); i++) {
			this.preLayer.put(i, null);
		}
		for (int i = 1; i < layer.size(); i++) {
			ArrayList<Integer> pLayer = new ArrayList<Integer>();
			pLayer.add(0);
			for (int j = 0; j < layer.get(i - 1).size(); j ++) {
				pLayer.add(layer.get(i - 1).get(j));
			}
			for (int k = 0; k < layer.get(i).size(); k++) {
				int temp2 = layer.get(i).get(k);
				this.preLayer.put(temp2, pLayer);
			}			
		}
		
		// initialize nextLayer
		this.nextLayer.put(count - 1, null);
		ArrayList<Integer> biasArr = new ArrayList<Integer>();
		for (int i = 1; i < count; i++) {
			biasArr.add(i);
		}
		this.nextLayer.put(0, biasArr);
		for (int i = 0; i < layer.size() - 1; i++) {
			ArrayList<Integer> nLayer = new ArrayList<Integer>();
			for (int j = 0; j < layer.get(i + 1).size(); j ++) {
				nLayer.add(layer.get(i + 1).get(j));
			}
			for (int k = 0; k < layer.get(i).size(); k++) {
				int temp2 = layer.get(i).get(k);
				this.nextLayer.put(temp2, nLayer);
			}
		}
		
		// initialize delta
		this.delta = new double[count];
		
		// initialize delWeight
		this.delta = new double[count];
		
		// initialize outputR
		this.outputR = new double[count];
		for (int i = 0; i < count; i++) {
			this.outputR[i] = this.output(i, data, 0);
		}
	}

	//refine data	
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
	
	// generate the output result(class)
	//public boolean isClassified(ANNet net, double[] target) {
	public boolean isClassified(double[] target) {
		double[] res = new double[layer.get(layer.size() - 1).size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = outputR[layer.get(layer.size() - 1).get(i)];
			if (res[i] >= 0.5) {
				res[i] = 1.0;
			} else {
				res[i] = 0.0;
			}
		}
		for (int i = 0; i < res.length; i++) {
			if (res[i] != target[i]) {
				return false;
			}			
		}
		return true;
	}
	
	// BP algorithm
	public static ANNet BP(String inputData, double percent, int maxIteration, int numLayer, String numEachHiddenLayer) {
		String[] numEachLayer = numEachHiddenLayer.split(",");
		int[] scale = new int[numEachLayer.length];
		for (int i = 0; i < numEachLayer.length; i++) {
			scale[i] = Integer.parseInt(numEachLayer[i]);
		}
				
		
		READ read = new READ();
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> testData = new ArrayList<ArrayList<String>>();
	
		data = Seprate.getTrainingData(inputData, testData, percent);
		read.convertType(data);
		int inSize = read.convertType(data)[0].length - 1;
		int outSize = read.getOutputSize(read.convertType(data));
		
		ANNet test = new ANNet(scale, inSize, outSize, read.convertType(data));
		int[] result = new int[read.convertType(data).length];
		test.BP_helper(maxIteration, numLayer, read.convertType(data), scale, result);
		test.print();
		System.out.println("\ntraining error: " + test.testError(read.convertType(data)));
		System.out.println("test error: " + test.testError(read.convertType(testData)));
		System.out.println("\n\n\n");
		
		return test;
	}
	
	public void BP_helper(int iter, int num_layer, double[][] data, int[] scale, int[] result) {
		double[] correctRes = new double[data.length];
		int targetSize = getOutputSize(data);
		int x = 0;
		boolean sign = false;
		for (int i = 0; i < data.length; i++) {
			correctRes[i] = data[i][data[0].length - 1];
		}
	    while (x < iter) {
			for (int i = 0; i < data.length; i++) {
				//double[] target = getTarget(data[i][data[0].length - 1], targetSize);
				loop(getTarget(data[i][data[0].length - 1], targetSize), data, i);
				result[i] = getResultClass(data, i);
				x++;
				if (x >= iter) {
					break;
				}
				if (is0Error(result, correctRes)) {
					sign = true;
					break;					
				}
			}
			if (sign == true) {
				break;
			}
		}				
	}
	
	public boolean is0Error(int[] res, double[] correctRes) {
		for (int i = 0; i < res.length; i++) {
			double resTemp = (double) res[i];
			if (resTemp != correctRes[i]) {
				return false;
			}
		}
		return true;
	}
	
	public double testError(double[][] data) {
		double sum = 0.0;
		int[] res = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			ouputUpdate(data, i);
			res[i] = getResultClass(data, i);
		}
		for (int i = 0; i < data.length; i++) {
			double temp = (double) res[i];
			sum = sum + (temp - data[i][data[0].length - 1]) * (temp - data[i][data[0].length - 1]);
		}
		double size = (double) data.length;
		return sum / (2 * size);		
		//return sum;
	}
	
	// BP Loop
	//public void loop(int iter, double[] target, double[][] data, int index) {
	public void loop(double[] target, double[][] data, int index) {

			ouputUpdate(data, index);
			bpPass(target, 0.9);

	}
	
	
	// forwarding single data
	public void getClassSingleData(ArrayList<String> data, Map<Double, String> map) {
		
		double[][] conData = new double[1][data.size()];
		for (int i = 0; i < data.size(); i++) {
			conData[0][i] = Double.parseDouble(data.get(i));
		}
		ouputUpdate(conData, 0);
		double predictedClass = (double)getResultClassSingle(layer.get(layer.size() - 1).size(), conData);
		
		if (predictedClass == -1.0) {
			System.out.println("Cannot predict");
		} else {
			System.out.println ("The predicted class is " + map.get(predictedClass));
		}
	}
	public int getResultClassSingle(int x, double[][] conData) {
		int sizeOP = x;
		int sizeTotal = outputR.length;
		int startIndex = sizeTotal - sizeOP;
		ouputUpdate(conData, 0);
		List<Integer> set = new ArrayList<Integer>(); 
		for (int i = 0; i < sizeOP; i++) {
			if (outputR[i + startIndex] > 0.5) {
				set.add(i);
			}
		}
		if (set.size() != 1) {
			return -1;
		}
		return set.get(0);
	}
	// forwarding
	public void ouputUpdate(double[][] data, int index) {
		int count = 1;
		for (int i = 0; i < layer.size(); i++) {
			for (int j = 0; j < layer.get(i).size(); j++) {
				count++;
			}
		}
		for (int i = 0; i < count; i++) {
			outputR[i] = output(i, data, index);
		}
	}
	public double output(int x, double[][] data, int index) {
		if (x <= layer.get(0).size()) {
			return netRes(x, data, index);
		}
		
		return sigmoid(netRes(x, data, index));
	}
	public double netRes(int x, double[][] data, int index) {
		if (x == 0) {
			return 1;
		}
		if (x > 0 && x <= layer.get(0).size()) {
			return data[index][x];
		}
		double result = 0.0;
		for (int i = 0; i < preLayer.get(x).size(); i++) {
			int tempArr = preLayer.get(x).get(i);
			result += output(tempArr, data, index) * weight[x][tempArr];
		}
		return result;
	}
	public double sigmoid(double x) {
		return 1.0 / (Math.exp(-x) + 1.0);
	}
	
	// back propagation
	public void bpPass(double[] target, double eta) {
		int num_l = layer.size();
		
		// calculate delta for output layer
		for (int i = 0; i <layer.get(num_l - 1).size(); i++) {
			calDeltaOL(layer.get(num_l - 1).get(i), target);
		}
		
		// calculate delta for hidden layer
		for (int i = num_l - 2; i > 0; i--) {
			for (int j = 0; j < layer.get(i).size(); j++) {
				calDeltaHL(layer.get(i).get(j));
			}
		}
		// update weight
		for (int i = 1; i < layer.size(); i++) {
			for (int j = 0; j <layer.get(i).size(); j++) {
				int indexTemp = layer.get(i).get(j);
				updateWeight(indexTemp, 0, eta);
			}
		}
		for (int i = num_l - 2; i >= 0; i--) {
			for (int j = 0; j < layer.get(i).size(); j++) {
				for (int k = 0; k < nextLayer.get(layer.get(i).get(j)).size(); k++) {
					int indexTemp = nextLayer.get(layer.get(i).get(j)).get(k);
					updateWeight(indexTemp, layer.get(i).get(j), eta);
				}
			}
		}
	}
	// for output layer
	public double calDeltaOL(int x, double[] target) {
		int num_pre = 1;
		for (int i = 0; i < layer.size() - 1; i++) {
			for (int j = 0; j < layer.get(i).size(); j++) {
				num_pre++;
			}
		}
		int outIndex = x - num_pre;
		double origin = outputR[x];
		double temp2 = origin * (1 - origin);

		delta[x] = temp2 * (target[outIndex] - origin);
		return temp2 * (target[outIndex] - origin);
	}
	
	// for hidden layer
	public double calDeltaHL(int x) {
		double origin = outputR[x];
		double temp2 = origin * (1 - origin);
		ArrayList<Integer> nextL = nextLayer.get(x);
		double temp3 = 0.0;
		for (int i = 0; i < nextL.size(); i++) {
			double temp4 = weight[nextL.get(i)][x] * delta[nextL.get(i)];
			temp3 = temp3 + temp4;
		}
		delta[x] = temp3 * temp2;
		return temp3 * temp2;
	}
	
	// update the weight
	public void updateWeight(int j, int i, double eta) {
		/*if (i == 2 || j == 2) {
			weight[j][i] = 0;
		}*/
		weight[j][i] = weight[j][i] + calDeltaWeight(j, i, eta);
		weight[i][j] = weight[i][j] + calDeltaWeight(j, i, eta);
	}
	// get the deltaWeight
	public double calDeltaWeight(int j, int i, double eta) {
		double temp2 = eta * delta[j];
		return temp2 * outputR[i]; 
	}
	
	// print ANN
	public void print() {
		int num = 1;
		System.out.println("Layer 0 (Input Layer):");
		for (int i = 0; i < layer.get(0).size(); i++) {
			System.out.println("Neuron" + (num++) +"  weights:");
			for (int j = 0; j < nextLayer.get(layer.get(0).get(i)).size(); j++) {
				System.out.print("w" + "(" + nextLayer.get(layer.get(0).get(i)).get(j)+"," + layer.get(0).get(i) + "): ");
				System.out.println(weight[nextLayer.get(layer.get(0).get(i)).get(j)][layer.get(0).get(i)] + ";");
			}
		}
		System.out.println("-----------------------------------------");
		for (int ii = 1; ii < layer.size() - 1; ii++) {
			System.out.println("Hidden layer No." + ii + ":");
			for (int i = 0; i < layer.get(ii).size(); i++) {
				System.out.println("Neuron" + (num++) +"  weights:");
				for (int j = 0; j < nextLayer.get(layer.get(ii).get(i)).size(); j++) {
					System.out.print("w" + "(" + nextLayer.get(layer.get(ii).get(i)).get(j)+"," + layer.get(ii).get(i) + "): ");
					System.out.println(weight[nextLayer.get(layer.get(ii).get(i)).get(j)][layer.get(ii).get(i)] + ";");
				}
			}
			System.out.println("-----------------------------------------");
		}

		System.out.println("Layer"+ (layer.size() - 1) + "(Last hidden layer):");
		for (int i = 0; i < layer.get(layer.size() - 1).size(); i++) {
			System.out.println("Neuron" + (num ++));
		}
		System.out.println("-----------------------------------------");
		System.out.println("Weight for the bias (marked as Neuron 0):");
		for (int i = 1; i < layer.size(); i++) {
			for (int j = 0; j < layer.get(i).size(); j++) {
				System.out.print("w(" + layer.get(i).get(j) + "," + "0):");
				System.out.println(weight[layer.get(i).get(j)][0]);
			}
		}
	}
	
	public int getResultClass(double[][] data, int index) {
		int sizeOP = getOutputSize(data);
		int sizeTotal = outputR.length;
		int startIndex = sizeTotal - sizeOP;
		ouputUpdate(data, index);
		List<Integer> set = new ArrayList<Integer>(); 
		for (int i = 0; i < sizeOP; i++) {
			if (outputR[i + startIndex] > 0.5) {
				set.add(i);
			}
		}
		if (set.size() != 1) {
			return -1;
		}
		return set.get(0);
	}
	
	
	/*public int getResultClass(double[][] data) {
		int sizeOP = getOutputSize(data);
		int sizeTotal = outputR.length;
		int startIndex = sizeTotal - sizeOP;
		for (int i = 0; i < sizeOP; i++) {
			if (outputR[i + startIndex] > 0.5) {
				return i;
			}
		}
		return -1;
		//return 1;
	}*/
	
	public static void main(String[] args) {
		
//		Preprocess the dataset : preProcess(String inputPath, String outputPath);      The dictionary is used for converting the numeric result into categorical value
//		Map<Double, String> dictionary;
//		dictionary = ProProcessing.preProcess("D:\\CS17Fall\\Machine Learning\\Assignment\\Assignment3\\car.data", "D:\\CS17Fall\\Machine Learning\\Assignment\\Assignment3\\car_processed.data");
		
		
//		***********This is the place where you could tune the model:    ******************
//		BP(String inputData, double percent, int maxIteration, int numLayer, String numEachHiddenLayer)
		ANNet ann = BP("D:\\CS17Fall\\Machine Learning\\Assignment\\Assignment3\\iris_processed.data", 80.0, 12000, 3, "2,3,4");
		
		ann = BP("D:\\CS17Fall\\Machine Learning\\Assignment\\Assignment3\\iris_processed.data", 80.0, 22000, 3, "4,4,4");
		
		ann = BP("D:\\CS17Fall\\Machine Learning\\Assignment\\Assignment3\\iris_processed.data", 80.0, 32000, 5, "3,3,3,3,3");
		
		
		
		
//		An example for predicting with given values of attributes :
		
//		ArrayList<String> a = new ArrayList<String> ();
////		Give the values of features
//		a.add("2.0");
//		a.add("2.0");
//		a.add("0.0");
//		a.add("1.0");
//		a.add("1.0");
//		a.add("1.0");
//		a.add("0.0");  //This one should always be 0 (Suppose 0 is the real class)
//		ann.getClassSingleData(a, dictionary);
//		System.out.print("\n");
	
	}
	
	
	
}
