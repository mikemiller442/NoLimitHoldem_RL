package reinforcementLearning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class LFA implements Function {

	private int[] featureVectorLengths = {8,83,108,109};
	
	private double[] weightsPreIP;
	private double[] weightsPreOP;
	private double[] weightsFlopIP;
	private double[] weightsFlopOP;
	private double[] weightsTurnIP;
	private double[] weightsTurnOP;
	private double[] weightsRiverIP;
	private double[] weightsRiverOP;
	
	private double currentSAP;
	private double previousSAP;
	private double[] previousFeatures;
	private double[] currentFeatures;
	
	private double gamma;
	private double alpha;
	
	public LFA(double epsilon, double gamma, double alpha, boolean train) {
		this.gamma = gamma;
		this.alpha = alpha;
		setWeights(train);
	}

	public double getPreviousSAP() {
		return this.previousSAP;
	}
	
	public double getCurrentSAP() {
		return this.currentSAP;
	}
	
	public void setPreviousSAP(double SAP) {
		this.previousSAP = SAP;
	}
	
	public void setCurrentSAP(double SAP) {
		this.currentSAP = SAP;
	}
	
	public void setPreviousFeatures(double[] previousFeatures) {
		this.previousFeatures = previousFeatures;
	}
	
	public double[] getCurrentFeatures() {
		return this.currentFeatures;
	}
	
	public void beginBackPropagation(boolean ip) {
		backPropagation(previousFeatures, gamma*currentSAP, previousSAP, ip);
	}
	
	public void beginBackPropagation(double reward, boolean ip) {
		backPropagation(currentFeatures, reward, currentSAP, ip);
	}
	
	public double functionOutput(double[] features, boolean ip) {
		double value = 0;
		if (features.length == featureVectorLengths[0]) {
			if (ip) {
				for (int i = 0; i < weightsPreIP.length; i++) {
					value = value + weightsPreIP[i]*features[i];
				}
			} else {
				for (int i = 0; i < weightsPreOP.length; i++) {
					value = value + weightsPreOP[i]*features[i];
				}
			}
		} else if (features.length == featureVectorLengths[1]) {
			if (ip) {
				for (int i = 0; i < weightsFlopIP.length; i++) {
					value = value + weightsFlopIP[i]*features[i];
				}
			} else {
				for (int i = 0; i < weightsFlopOP.length; i++) {
					value = value + weightsFlopOP[i]*features[i];
				}
			}
		} else if (features.length == featureVectorLengths[2]) {
			if (ip) {
				for (int i = 0; i < weightsTurnIP.length; i++) {
					value = value + weightsTurnIP[i]*features[i];
				}
			} else {
				for (int i = 0; i < weightsTurnOP.length; i++) {
					value = value + weightsTurnOP[i]*features[i];
				}
			}
		} else if (features.length == featureVectorLengths[3]){
			if (ip) {
				for (int i = 0; i < weightsRiverIP.length; i++) {
					value = value + weightsRiverIP[i]*features[i];
				}
			} else {
				for (int i = 0; i < weightsRiverOP.length; i++) {
					value = value + weightsRiverOP[i]*features[i];
				}
			}
		} else {
			throw new IllegalArgumentException("the feature vector length is incorrect");
		}
		currentFeatures = features;
		return value;
	}
	
	public void backPropagation(double[] inputs, double target, double output, boolean ip) {
		
		System.out.println("current SAP");
		System.out.println(inputs.length);
		System.out.println(output);
		System.out.println(target);
		
		if (inputs.length == featureVectorLengths[0]) {
			if (ip) {
				for (int i = 0; i < weightsPreIP.length; i++) {
					weightsPreIP[i] = weightsPreIP[i] + alpha*(target - output)*previousFeatures[i];
				}
			} else {
				for (int i = 0; i < weightsPreOP.length; i++) {
					weightsPreOP[i] = weightsPreOP[i] + alpha*(target - output)*previousFeatures[i];
				}
			}
		} else if (inputs.length == featureVectorLengths[1]) {
			if (ip) {
				for (int i = 0; i < weightsFlopIP.length; i++) {
//					System.out.println("ip update weights test");
//					System.out.println(i);
					weightsFlopIP[i] = weightsFlopIP[i] + alpha*(target - output)*previousFeatures[i];
				}
			} else {
				for (int i = 0; i < weightsFlopOP.length; i++) {
//					System.out.println("op update weights test");
//					System.out.println(i);
//					System.out.println(previousFeatures[i]);
					weightsFlopOP[i] = weightsFlopOP[i] + alpha*(target - output)*previousFeatures[i];
				}
			}
		} else if (inputs.length == featureVectorLengths[2]) {
			if (ip) {
				for (int i = 0; i < weightsTurnIP.length; i++) {
					weightsTurnIP[i] = weightsTurnIP[i] + alpha*(target - output)*previousFeatures[i];
				}
			} else {
				for (int i = 0; i < weightsTurnOP.length; i++) {
					weightsTurnOP[i] = weightsTurnOP[i] + alpha*(target - output)*previousFeatures[i];
				}
			}
		} else if (inputs.length == featureVectorLengths[3]) {
//			System.out.println("weights river testing");
//			System.out.println(weightsRiverOP.length);
//			System.out.println(previousFeatures.length);
			if (ip) {
				for (int i = 0; i < weightsRiverIP.length; i++) {
					weightsRiverIP[i] = weightsRiverIP[i] + alpha*(target - output)*previousFeatures[i];
				}
			} else {
				for (int i = 0; i < weightsRiverOP.length; i++) {
					weightsRiverOP[i] = weightsRiverOP[i] + alpha*(target - output)*previousFeatures[i];
				}
			}
		} else {
			throw new IllegalArgumentException("feature vector had invalid length");
		}
	}
	
//	public void endHandWeightsUpdate(int pot, int amountToCall, int effective, boolean ip, int finalPot, boolean fold, int finalPotWithoutLastBet) {
//		System.out.println("final SAP");
//		System.out.println(currentFeatures.length);
//		System.out.println(fold);
//		System.out.println(finalPot);
//		System.out.println(pot-amountToCall);
//		System.out.println(reward);
//		System.out.println(currentSAP);
//		if (currentFeatures.length == featureVectorLengths[0]) {
//			if (ip) {
//				for (int i = 0; i < weightsPreIP.length; i++) {
//					weightsPreIP[i] = weightsPreIP[i] + alpha*(reward - currentSAP)*currentFeatures[i];
//				}
//			} else {
//				for (int i = 0; i < weightsPreOP.length; i++) {
//					weightsPreOP[i] = weightsPreOP[i] + alpha*(reward - currentSAP)*currentFeatures[i];
//				}
//			}
//		} else if (currentFeatures.length == featureVectorLengths[1]) {
//			if (ip) {
//				for (int i = 0; i < weightsFlopIP.length; i++) {
////					System.out.println("ip update weights test");
////					System.out.println(i);
//					weightsFlopIP[i] = weightsFlopIP[i] + alpha*(reward - currentSAP)*currentFeatures[i];
//				}
//			} else {
//				for (int i = 0; i < weightsFlopOP.length; i++) {
////					System.out.println("op update weights test");
////					System.out.println(i);
//					weightsFlopOP[i] = weightsFlopOP[i] + alpha*(reward - currentSAP)*currentFeatures[i];
//				}
//			}
//		} else if (currentFeatures.length == featureVectorLengths[2]) {
//			if (ip) {
//				for (int i = 0; i < weightsTurnIP.length; i++) {
//					weightsTurnIP[i] = weightsTurnIP[i] + alpha*(reward - currentSAP)*currentFeatures[i];
//				}
//			} else {
//				for (int i = 0; i < weightsTurnOP.length; i++) {
//					weightsTurnOP[i] = weightsTurnOP[i] + alpha*(reward - currentSAP)*currentFeatures[i];
//				}
//			}
//		} else {
//			if (ip) {
//				for (int i = 0; i < weightsRiverIP.length; i++) {
//					weightsRiverIP[i] = weightsRiverIP[i] + alpha*(reward - currentSAP)*currentFeatures[i];
//				}
//			} else {
//				for (int i = 0; i < weightsRiverOP.length; i++) {
//					weightsRiverOP[i] = weightsRiverOP[i] + alpha*(reward - currentSAP)*currentFeatures[i];
//				}
//			}
//		}
//	}
	
	public void setWeights(boolean train) {
		
		String row;
		double[] numbers;
		int counter = 0;
		
		if (train) {
			weightsPreIP = new double[featureVectorLengths[0]];
			for (int i = 0; i < featureVectorLengths[0]; i++) weightsPreIP[i] = 0;
			weightsPreOP = new double[featureVectorLengths[0]];
			for (int i = 0; i < featureVectorLengths[0]; i++) weightsPreOP[i] = 0;
			weightsFlopIP = new double[featureVectorLengths[1]];
			for (int i = 0; i < featureVectorLengths[1]; i++) weightsFlopIP[i] = 0;
			weightsFlopOP = new double[featureVectorLengths[1]];
			for (int i = 0; i < featureVectorLengths[1]; i++) weightsFlopOP[i] = 0;
			weightsTurnIP = new double[featureVectorLengths[2]];
			for (int i = 0; i < featureVectorLengths[2]; i++) weightsTurnIP[i] = 0;
			weightsTurnOP = new double[featureVectorLengths[2]];
			for (int i = 0; i < featureVectorLengths[2]; i++) weightsTurnOP[i] = 0;
			weightsRiverIP = new double[featureVectorLengths[3]];
			for (int i = 0; i < featureVectorLengths[3]; i++) weightsRiverIP[i] = 0;
			weightsRiverOP = new double[featureVectorLengths[3]];
			for (int i = 0; i < featureVectorLengths[3]; i++) weightsRiverOP[i] = 0;
		} else {
			BufferedReader br = null;
			FileReader fr = null;
			try {
				fr = new FileReader("weights.txt");
				br = new BufferedReader(fr);
				while (true) {
					row = br.readLine();
					
					if (row == null) {
						break;
					}
					
					String[] line = row.split(",");
					numbers = new double[line.length];
					for (int i = 0; i < line.length; i++) {
						numbers[i] = Double.parseDouble(line[i]);
					}
					
					switch (counter) {
					
					case 0:
						weightsPreIP = new double[numbers.length];
						for (int i = 0; i < numbers.length; i++) weightsPreIP[i] = numbers[i];
						break;
					case 1:
						weightsPreOP = new double[numbers.length];
						for (int i = 0; i < numbers.length; i++) weightsPreOP[i] = numbers[i];
						break;
					case 2:
						weightsFlopIP = new double[numbers.length];
						for (int i = 0; i < numbers.length; i++) weightsFlopIP[i] = numbers[i];
						break;
					case 3:
						weightsFlopOP = new double[numbers.length];
						for (int i = 0; i < numbers.length; i++) weightsFlopOP[i] = numbers[i];
						break;
					case 4:
						weightsTurnIP = new double[numbers.length];
						for (int i = 0; i < numbers.length; i++) weightsTurnIP[i] = numbers[i];
						break;
					case 5:
						weightsTurnOP = new double[numbers.length];
						for (int i = 0; i < numbers.length; i++) weightsTurnOP[i] = numbers[i];
						break;
					case 6:
						weightsRiverIP = new double[numbers.length];
						for (int i = 0; i < numbers.length; i++) weightsRiverIP[i] = numbers[i];
						break;
					case 7:
						weightsRiverOP = new double[numbers.length];
						for (int i = 0; i < numbers.length; i++) weightsRiverOP[i] = numbers[i];
						break;
					
					}
					
					counter++;
					
				}
				br.close();
			} catch (IOException ioe) {
				System.out.println(ioe);
			}		
		}
	}
	
	public void storeWeights() {
		PrintWriter writer;
		try {
			writer = new PrintWriter("weights.txt", "UTF-8");
			String lineIP = "";
			String lineOP = "";
			for (int i = 0; i < weightsPreIP.length; i++) {
				lineIP = lineIP + Double.toString(weightsPreIP[i]) + ",";
				lineOP = lineOP + Double.toString(weightsPreOP[i]) + ",";
			}
			writer.println(lineIP.substring(0, lineIP.length() - 1));
			writer.println(lineOP.substring(0, lineOP.length() - 1));
			
			lineIP = "";
			lineOP = "";
			for (int i = 0; i < weightsFlopIP.length; i++) {
				lineIP = lineIP + Double.toString(weightsFlopIP[i]) + ",";
				lineOP = lineOP + Double.toString(weightsFlopOP[i]) + ",";
			}
			writer.println(lineIP.substring(0, lineIP.length() - 1));
			writer.println(lineOP.substring(0, lineOP.length() - 1));
			
			lineIP = "";
			lineOP = "";
			for (int i = 0; i < weightsTurnIP.length; i++) {
				lineIP = lineIP + Double.toString(weightsTurnIP[i]) + ",";
				lineOP = lineOP + Double.toString(weightsTurnOP[i]) + ",";
			}
			writer.println(lineIP.substring(0, lineIP.length() - 1));
			writer.println(lineOP.substring(0, lineOP.length() - 1));
			
			lineIP = "";
			lineOP = "";
			for (int i = 0; i < weightsRiverIP.length; i++) {
				lineIP = lineIP + Double.toString(weightsRiverIP[i]) + ",";
				lineOP = lineOP + Double.toString(weightsRiverOP[i]) + ",";
			}
			writer.println(lineIP.substring(0, lineIP.length() - 1));
			writer.println(lineOP.substring(0, lineOP.length() - 1));
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
	
}
