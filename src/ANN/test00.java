package ANN;

import java.io.FileReader;

import java.io.IOException;
import java.util.*;

import com.opencsv.CSVReader;



public class test00 {
	Map<Integer, ArrayList<Integer>> preLayer = new HashMap<Integer, ArrayList<Integer>>(); //done
	Map<Integer, ArrayList<Integer>> nextLayer = new HashMap<Integer, ArrayList<Integer>>(); //done
	double[][] weight; //done
	double[] delta;
	double[] delWeight;
	double[] outputR;
	Map<Integer, ArrayList<Integer>> layer = new HashMap<Integer, ArrayList<Integer>>(); //done
	test00(int[] scale, int input_size, int output_size, double[][] data) {
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
				this.weight[i][j] = -2 + (4) * r.nextDouble();;
			}
		}
		for (int i = 0 ; i < count; i++) {
			this.weight[i][2] = -0.000012 + (0.000024) * r.nextDouble();
			//this.weight[i][2] = 0;
			//this.weight[2][i] = 0;
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
	public void BP(String inputData, int percent, int maxIteration, int numLayer, String numEachHiddenLayer) {
		String[] numEachLayer = numEachHiddenLayer.split(",");
		int[] scale = new int[numEachLayer.length];
		for (int i = 0; i < numEachLayer.length; i++) {
			scale[i] = Integer.parseInt(numEachLayer[i]);
		}
		
	}
	
	public void BP_helper(int iter, int num_layer, double[][] data, int[] scale, int[] result) {
		
		int targetSize = getOutputSize(data);
		for (int i = 0; i < data.length; i++) {
			double[] target = getTarget(data[i][data[0].length - 1], targetSize);
			loop(iter, target, data, i);
			result[i] = getResultClass(data);
		}	
		
	}

	// BP Loop
	public void loop(int iter, double[] target, double[][] data, int index) {
		int i = 0;
		while(i < iter) {
			//ouputUpdate(net, data, index);
			ouputUpdate(data, index);
			bpPass(target, 0.9);
			i++;
			/*int num_pre = 1;
			for (int j = 0; j < layer.size() - 1; j++) {
				for (int k = 0; k < layer.get(j).size(); k++) {
					num_pre++;
				}
			}*/
			//if (isClassified(net, target)) {
			if (isClassified(target)) {
				break;
			}
			/*if (judgment(num_pre, outputR, target)) {
				break;
			}*/
		}
		//System.out.println("done!");
		//print();
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
			return 1.0;
		}
		if (x > 0 && x <= layer.get(0).size()) {
			return data[index][x];
		}
		double result = 0.0;
		for (int i = 0; i < preLayer.get(x).size(); i++) {
			int tempArr = preLayer.get(x).get(i);
			//result += output(tempArr, data, index) * weight[x][tempArr];
			result += outputR[tempArr] * weight[x][tempArr];
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
	
	public int getResultClass(double[][] data) {
		int sizeOP = getOutputSize(data);
		int sizeTotal = outputR.length;
		int startIndex = sizeTotal - sizeOP;
		for (int i = 0; i < sizeOP; i++) {
			if (outputR[i + startIndex] > 0.5) {
				return i;
			}
		}
		return -1;
	}
	
	public double testError(double[][] data) {
		double sum = 0.0;
		int[] res = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			ouputUpdate(data, i);
			res[i] = getResultClass(data);
		}
		for (int i = 0; i < data.length; i++) {
			double temp = (double) res[i];
			sum += (temp - data[i][data[0].length - 1]) * (temp - data[i][data[0].length - 1]);
		}
		double size = (double) data.length;
		return sum / (2 * size);		
	}
	
	public static void main(String[] args) {
		
		READ read = new READ();
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		read.read ("C:/Users/LIcy/workspace/ANN/src/lol2.data", data);
		read.convertType(data);
		int inSize = read.convertType(data)[0].length - 1;
		int outSize = read.getOutputSize(read.convertType(data));
		int[] scale = {3,5,7};
		
		System.out.println(inSize + " " + outSize);
		
		test00 test = new test00(scale, inSize, outSize, read.convertType(data));
		
		
		for (int i = 0; i < test.nextLayer.get(21).size(); i++) {
			System.out.print(test.nextLayer.get(21).get(i) + " ");
		}
		
		
		test.loop(100, test.getTarget(read.convertType(data)[read.convertType(data).length - 1][read.convertType(data)[0].length - 1], test.getOutputSize(read.convertType(data))), read.convertType(data), read.convertType(data).length - 1);
		System.out.println( test.getResultClass(read.convertType(data)));
		//test.bpPass(test.getTarget(read.convertType(data)[0][read.convertType(data)[0].length - 1], test.getOutputSize(read.convertType(data))), 0.5);
		for (int i = 30; i < 32; i++) {
			System.out.println(test.outputR[i] + " get");
		}
		
		System.out.println(test.getResultClass(read.convertType(data)) + " get");
		int[] result = new int[read.convertType(data).length];
		//test.BP_helper(1, 3, read.convertType(data), scale, result);
		//test.bpPass(test.getTarget(read.convertType(data)[read.convertType(data).length - 1][read.convertType(data)[0].length - 1], test.getOutputSize(read.convertType(data))), 0.5);
		//test.print();
		for (int i = 2; i < 15; i++) {
			test.ouputUpdate(read.convertType(data), i);
			System.out.println(read.convertType(data)[i][5] + " " + read.convertType(data)[i][14]);
			System.out.println(test.output(5, read.convertType(data), i) + "!!! " + test.output(14, read.convertType(data), i));
			System.out.println(test.outputR[5] + " " + test.outputR[6] + " " + test.outputR[7] + " " + test.outputR[14] + " THIS:" + test.outputR[15]);
			//System.out.println(read.convertType(data)[i][5] + " " + read.convertType(data)[i][14]);
			System.out.println(i + " " +test.getResultClass(read.convertType(data)) + " " + test.outputR[30] + " " + test.outputR[31]);
		}
		System.out.println("mark");
		for (int i = 0; i < test.preLayer.get(15).size(); i++) {
			System.out.print("i: " + test.preLayer.get(15).get(i) + " ");
			System.out.println(" ");
			System.out.print("i2: " + test.outputR[test.preLayer.get(15).get(i)] + " ");
			System.out.println(" ");
			System.out.print("i3: " +test.weight[15][test.preLayer.get(15).get(i)] + " ");
			System.out.println(" ");
		}
		for (int i = 30; i < 32; i++) {
			System.out.println(test.outputR[i]);
		}
		System.out.println(test.testError(read.convertType(data)));
		//System.out.println(test.getTarget(read.convertType(data)[read.convertType(data).length - 1][read.convertType(data)[0].length - 1], test.getOutputSize(read.convertType(data)))[1]);
		
	}
	
	
	
}