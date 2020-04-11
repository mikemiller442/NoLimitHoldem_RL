package reinforcementLearning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class MLP implements Function {

	private int[] featureVectorLengths = {8,83,108,109};
	private final int NUM_WEIGHT_MATRICES = 3;
	
	private int[] numNodesPreIP = {8,5,3};
	private int[] numNodesPreOP = {8,5,3};
	private int[] numNodesFlopIP = {83,20,10};
	private int[] numNodesFlopOP = {83,20,10};
	private int[] numNodesTurnIP = {108,20,10};
	private int[] numNodesTurnOP = {108,20,10};
	private int[] numNodesRiverIP = {109,20,10};
	private int[] numNodesRiverOP = {109,20,10};
	
	private double[][] NodesPreIP;
	private double[][] NodesPreOP;
	private double[][] NodesFlopIP;
	private double[][] NodesFlopOP;
	private double[][] NodesTurnIP;
	private double[][] NodesTurnOP;
	private double[][] NodesRiverIP;
	private double[][] NodesRiverOP;
	
	private Matrix[] weightsPreIP;
	private Matrix[] weightsPreOP;
	private Matrix[] weightsFlopIP;
	private Matrix[] weightsFlopOP;
	private Matrix[] weightsTurnIP;
	private Matrix[] weightsTurnOP;
	private Matrix[] weightsRiverIP;
	private Matrix[] weightsRiverOP;
	
	private double currentSAP;
	private double previousSAP;
	private double[] previousFeatures;
	private double[] currentFeatures;
	
	private double gamma;
	
	// In general it will help to think of a MLP with 2 hidden layers as
	// having 3 layers, each with an activation function, where the last activation
	// will be the identity function here
	
	public MLP(double epsilon, double gamma, boolean train) {
		
		this.gamma = gamma;
		
		this.NodesPreIP = new double[numNodesPreIP.length][];
		for (int i = 0; i < numNodesPreIP.length; i++) {
			NodesPreIP[i] = new double[numNodesPreIP[i]];
		}
		this.NodesPreOP = new double[numNodesPreOP.length][];
		for (int i = 0; i < numNodesPreOP.length; i++) {
			NodesPreOP[i] = new double[numNodesPreOP[i]];
		}
		this.NodesFlopIP = new double[numNodesFlopIP.length][];
		for (int i = 0; i < numNodesFlopIP.length; i++) {
			NodesFlopIP[i] = new double[numNodesFlopIP[i]];
		}
		this.NodesFlopOP = new double[numNodesFlopOP.length][];
		for (int i = 0; i < numNodesFlopOP.length; i++) {
			NodesFlopOP[i] = new double[numNodesFlopOP[i]];
		}
		this.NodesTurnIP = new double[numNodesTurnIP.length][];
		for (int i = 0; i < numNodesTurnIP.length; i++) {
			NodesTurnIP[i] = new double[numNodesTurnIP[i]];
		}
		this.NodesTurnOP = new double[numNodesTurnOP.length][];
		for (int i = 0; i < numNodesTurnOP.length; i++) {
			NodesTurnOP[i] = new double[numNodesTurnOP[i]];
		}
		this.NodesRiverIP = new double[numNodesRiverIP.length][];
		for (int i = 0; i < numNodesRiverIP.length; i++) {
			NodesRiverIP[i] = new double[numNodesRiverIP[i]];
		}
		this.NodesRiverOP = new double[numNodesRiverOP.length][];
		for (int i = 0; i < numNodesRiverOP.length; i++) {
			NodesRiverOP[i] = new double[numNodesRiverOP[i]];
		}
		
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
	
	public void beginBackPropagation(double immediateReward, boolean ip) {
		backPropagation(previousFeatures, immediateReward + gamma*currentSAP, previousSAP, ip);
	}
	
	public void beginTerminalBackPropagation(double reward, boolean ip) {
		backPropagation(currentFeatures, reward, currentSAP, ip);
	}
	
	public double functionOutput(double[] inputs, boolean ip) {
		Matrix[] weights;
		double[][] hiddenNodes;
		if (inputs.length == featureVectorLengths[0]) {
			if (ip) {
			  weights = weightsPreIP.clone();
              hiddenNodes = NodesPreIP;
			} else {
                weights = weightsPreOP.clone();
                hiddenNodes = NodesPreOP; 
			}
		} else if (inputs.length == featureVectorLengths[1]) {
			if (ip) {
				weights = weightsFlopIP.clone();
				hiddenNodes = NodesFlopIP;
			} else {
				weights = weightsFlopOP.clone();
				hiddenNodes = NodesFlopOP;
			}
		} else if (inputs.length == featureVectorLengths[2]) {
			if (ip) {
				weights = weightsTurnIP.clone();
				hiddenNodes = NodesTurnIP;
			} else {
				weights = weightsTurnOP.clone();
				hiddenNodes = NodesTurnOP;
			}
		} else if (inputs.length == featureVectorLengths[3]){
			if (ip) {
				weights = weightsRiverIP.clone();
				hiddenNodes = NodesRiverIP;
			} else {
				weights = weightsRiverOP.clone();
				hiddenNodes = NodesRiverOP;
			}
		} else {
			throw new IllegalArgumentException("the feature vector length is incorrect");
		}
		
		double[] features = weights[0].compose(inputs);
//		System.out.println("testing weight dimensions");
//		System.out.println(weights.length);
//		System.out.println(weights[0].getNumColumns());
//        System.out.println(weights[0].getNumRows());
		for (int i = 1; i < weights.length; i++) {
			activation(hiddenNodes, features, i);
//			System.out.println("testing matrix multiplication");
//			System.out.println(hiddenNodes[i].length);
//			System.out.println(weights[i].getNumColumns());
//			System.out.println(weights[i].getNumRows());
			features = weights[i].compose(hiddenNodes[i]);
		}
		currentFeatures = inputs;
		return features[0];
		
	}
	
	private void activation(double[][] hiddenNodes, double[] features, int layer) {
//	    System.out.println(hiddenNodes.length);
//	    System.out.println(hiddenNodes[0].length);
//	    System.out.println(hiddenNodes[1].length);
//	    System.out.println(features.length);
		for (int i = 0; i < features.length; i++) {
		    System.out.println("printing features");
		    System.out.println(features[i]);
		    if (Double.isNaN(hiddenNodes[layer][i])) {
              System.out.println("NaN in the features!!");
              System.out.println(features[i]);
              System.out.println(layer);
              System.out.println(features.length);
              System.exit(0);
            }
			hiddenNodes[layer][i] = (Math.exp(2*features[i])-1)/(Math.exp(2*features[i])+1);
			if (Double.isNaN(hiddenNodes[layer][i])) {
//			  System.out.println("NaN in the activation function!!");
//			  System.out.println(i);
//			  System.out.println(features[i]);
//			  System.out.println(layer);
//			  System.out.println(features.length);
//			  System.exit(0);
			  if (features[i] < 0) {
			    hiddenNodes[layer][i] = -1;
			  } else {
			    hiddenNodes[layer][i] = 1;
			  }
			}
		}
	}
	
	public void backPropagation(double[] inputs, double target, double output, boolean ip) {
		double dummyOutput = functionOutput(inputs, ip);
		double error = target - output;
		double[] temp;
//		double[] features = new double[numHiddenNodes[numHiddenNodes.length-1]];
		double[] delta = {error}; // At the final layer the activation function is just the identity function
		double[][] gradientEntries; // At the final layer the loss function is (target-output)^2, which is univariate
		Matrix gradients;	
		Matrix[] weights;
		double[][] hiddenNodes;
		
		if (inputs.length == featureVectorLengths[0]) {
			if (ip) {
			  weights = weightsPreIP.clone();
              hiddenNodes = NodesPreIP;
			} else {
			  weights = weightsPreOP.clone();
              hiddenNodes = NodesPreOP;
			}
		} else if (inputs.length == featureVectorLengths[1]) {
			if (ip) {
				weights = weightsFlopIP.clone();
				hiddenNodes = NodesFlopIP;
			} else {
				weights = weightsFlopOP.clone();
				hiddenNodes = NodesFlopOP;
			}
		} else if (inputs.length == featureVectorLengths[2]) {
			if (ip) {
				weights = weightsTurnIP.clone();
				hiddenNodes = NodesTurnIP;
			} else {
				weights = weightsTurnOP.clone();
				hiddenNodes = NodesTurnOP;
			}
		} else if (inputs.length == featureVectorLengths[3]){
			if (ip) {
				weights = weightsRiverIP.clone();
				hiddenNodes = NodesRiverIP;
			} else {
				weights = weightsRiverOP.clone();
				hiddenNodes = NodesRiverOP;
			}
		} else {
			throw new IllegalArgumentException("the feature vector length is incorrect");
		}
		
		for (int i = weights.length-1; i >= 0; i--) {
//		  System.out.println("nodes dimensions");
//          System.out.println(delta.length);
//          System.out.println(hiddenNodes[i].length);
			gradientEntries = new double[delta.length][hiddenNodes[i].length];
			for (int j = 0; j < delta.length; j++) {
				for (int k = 0; k < hiddenNodes[i].length; k++) {
					gradientEntries[j][k] = delta[j]*hiddenNodes[i][k];
				}
			}
			gradients = new Matrix(gradientEntries);
//			System.out.println("gradient dimensions");
//            System.out.println(gradients.getNumColumns());
//            System.out.println(gradients.getNumRows());
//            System.out.println(weights[i].getNumColumns());
//            System.out.println(weights[i].getNumRows());
			weights[i].updateWeights(gradients);
			temp = weights[i].transpose().compose(delta);
//			System.out.println(temp.length);
//			System.out.println(hiddenNodes[i].length);
			delta = new double[temp.length];
			for (int j = 0; j < temp.length; j++) {
				delta[j] = (1-Math.pow(hiddenNodes[i][j], 2))*temp[j];
			}
		}
	}
	
	private Matrix randomWeightMatrix(int rows, int columns) {
	  Random rand = new Random();
      double randDouble;
      double[][] weights = new double[rows][columns];
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < columns; j++) {
          randDouble = Math.random();
          weights[i][j] = (rand.nextInt(20) - 10)*randDouble;
        }
      }
//      System.out.println("testing matrix dimensions");
//      System.out.println(weights[0].length);
      return new Matrix(weights);
	}
	
	private Matrix constructMatrix(String weights, int rows, int columns) {
	  String[] line = weights.split(",");
	  double[][] entries = new double[rows][columns];
	  for (int i = 0; i < line.length; i++) {
	    entries[i/columns][i % columns] = Double.parseDouble(line[i]);
	  }
	  return new Matrix(entries);
    }
	
	public void setWeights(boolean train) {
		
		String[] matrixStrings = new String[NUM_WEIGHT_MATRICES];
		int counter = 0;
		weightsPreIP = new Matrix[NUM_WEIGHT_MATRICES];
		weightsPreOP = new Matrix[NUM_WEIGHT_MATRICES];
		weightsFlopIP = new Matrix[NUM_WEIGHT_MATRICES];
		weightsFlopOP = new Matrix[NUM_WEIGHT_MATRICES];
		weightsTurnIP = new Matrix[NUM_WEIGHT_MATRICES];
		weightsTurnOP = new Matrix[NUM_WEIGHT_MATRICES];
		weightsRiverIP = new Matrix[NUM_WEIGHT_MATRICES];
		weightsRiverOP = new Matrix[NUM_WEIGHT_MATRICES];
		if (train) {
		  for (int i = 0; i < NUM_WEIGHT_MATRICES-1; i++) {
//		    System.out.println("setting up weights");
//		    System.out.println(numNodesPreIP[i]);
  	        weightsPreIP[i] = randomWeightMatrix(numNodesPreIP[i+1], numNodesPreIP[i]);
//  	        System.out.println("checking weight dimensions");
//  	        System.out.println(weightsPreIP[i].toString());
//  	        System.out.println(weightsPreIP[i].getNumColumns());
//  	        System.out.println(weightsPreIP[i].getNumRows());
  	        weightsPreOP[i] = randomWeightMatrix(numNodesPreOP[i+1], numNodesPreOP[i]);
  	        weightsFlopIP[i] = randomWeightMatrix(numNodesFlopIP[i+1], numNodesFlopIP[i]);
  	        weightsFlopOP[i] = randomWeightMatrix(numNodesFlopOP[i+1], numNodesFlopOP[i]);
  	        weightsTurnIP[i] = randomWeightMatrix(numNodesTurnIP[i+1], numNodesTurnIP[i]);
  	        weightsTurnOP[i] = randomWeightMatrix(numNodesTurnOP[i+1], numNodesTurnOP[i]);
  	        weightsRiverIP[i] = randomWeightMatrix(numNodesRiverIP[i+1], numNodesRiverIP[i]);
  	        weightsRiverOP[i] = randomWeightMatrix(numNodesRiverOP[i+1], numNodesRiverOP[i]);
		  }
//		  System.out.println("checking dimensions");
//          System.out.println(weightsPreIP[0].getNumColumns());
//          System.out.println(weightsPreIP[0].getNumRows());
          weightsPreIP[NUM_WEIGHT_MATRICES-1] = randomWeightMatrix(1, numNodesPreIP[NUM_WEIGHT_MATRICES-1]);
          weightsPreOP[NUM_WEIGHT_MATRICES-1] = randomWeightMatrix(1, numNodesPreOP[NUM_WEIGHT_MATRICES-1]);
          weightsFlopIP[NUM_WEIGHT_MATRICES-1] = randomWeightMatrix(1, numNodesFlopIP[NUM_WEIGHT_MATRICES-1]);
          weightsFlopOP[NUM_WEIGHT_MATRICES-1] = randomWeightMatrix(1, numNodesFlopOP[NUM_WEIGHT_MATRICES-1]);
          weightsTurnIP[NUM_WEIGHT_MATRICES-1] = randomWeightMatrix(1, numNodesTurnIP[NUM_WEIGHT_MATRICES-1]);
          weightsTurnOP[NUM_WEIGHT_MATRICES-1] = randomWeightMatrix(1, numNodesTurnOP[NUM_WEIGHT_MATRICES-1]);
          weightsRiverIP[NUM_WEIGHT_MATRICES-1] = randomWeightMatrix(1, numNodesRiverIP[NUM_WEIGHT_MATRICES-1]);
          weightsRiverOP[NUM_WEIGHT_MATRICES-1] = randomWeightMatrix(1, numNodesRiverOP[NUM_WEIGHT_MATRICES-1]);
		} else {
			BufferedReader br = null;
			FileReader fr = null;
			try {
				fr = new FileReader("mlpWeights.txt");
				br = new BufferedReader(fr);
				while (true) {
				    if (counter > NUM_WEIGHT_MATRICES*7) {
				      break;
				    }
				    for (int i = 0; i < NUM_WEIGHT_MATRICES; i++) {
				      matrixStrings[i] = br.readLine();
				    }
					switch (counter) {
					
					case 0:
						for (int i = 0; i < NUM_WEIGHT_MATRICES-1; i++) {
						  weightsPreIP[i] = constructMatrix(matrixStrings[i], numNodesPreIP[i], numNodesPreIP[i+1]);
						}
						weightsPreIP[NUM_WEIGHT_MATRICES-1] = constructMatrix(matrixStrings[NUM_WEIGHT_MATRICES-1], numNodesPreIP[NUM_WEIGHT_MATRICES-1], 1);
						break;
					case (NUM_WEIGHT_MATRICES*1):
					    for (int i = 0; i < NUM_WEIGHT_MATRICES-1; i++) {
                          weightsPreOP[i] = constructMatrix(matrixStrings[i], numNodesPreOP[i], numNodesPreOP[i+1]);
                         }
                         weightsPreOP[NUM_WEIGHT_MATRICES-1] = constructMatrix(matrixStrings[NUM_WEIGHT_MATRICES-1], numNodesPreOP[NUM_WEIGHT_MATRICES-1], 1);
						break;
					case (NUM_WEIGHT_MATRICES*2):
					    for (int i = 0; i < NUM_WEIGHT_MATRICES-1; i++) {
                          weightsFlopIP[i] = constructMatrix(matrixStrings[i], numNodesFlopIP[i], numNodesFlopIP[i+1]);
                         }
                         weightsFlopIP[NUM_WEIGHT_MATRICES-1] = constructMatrix(matrixStrings[NUM_WEIGHT_MATRICES-1], numNodesFlopIP[NUM_WEIGHT_MATRICES-1], 1);
						break;
					case (NUM_WEIGHT_MATRICES*3):
					  for (int i = 0; i < NUM_WEIGHT_MATRICES-1; i++) {
                        weightsFlopOP[i] = constructMatrix(matrixStrings[i], numNodesFlopOP[i], numNodesFlopOP[i+1]);
                       }
                       weightsFlopOP[NUM_WEIGHT_MATRICES-1] = constructMatrix(matrixStrings[NUM_WEIGHT_MATRICES-1], numNodesFlopOP[NUM_WEIGHT_MATRICES-1], 1);
						break;
					case (NUM_WEIGHT_MATRICES*4):
					  for (int i = 0; i < NUM_WEIGHT_MATRICES-1; i++) {
                        weightsTurnIP[i] = constructMatrix(matrixStrings[i], numNodesTurnIP[i], numNodesTurnIP[i+1]);
                       }
                       weightsTurnIP[NUM_WEIGHT_MATRICES-1] = constructMatrix(matrixStrings[NUM_WEIGHT_MATRICES-1], numNodesTurnIP[NUM_WEIGHT_MATRICES-1], 1);
						break;
					case (NUM_WEIGHT_MATRICES*5):
					  for (int i = 0; i < NUM_WEIGHT_MATRICES-1; i++) {
                        weightsTurnOP[i] = constructMatrix(matrixStrings[i], numNodesTurnOP[i], numNodesTurnOP[i+1]);
                       }
                       weightsTurnOP[NUM_WEIGHT_MATRICES-1] = constructMatrix(matrixStrings[NUM_WEIGHT_MATRICES-1], numNodesTurnOP[NUM_WEIGHT_MATRICES-1], 1);
						break;
					case (NUM_WEIGHT_MATRICES*6):
					  for (int i = 0; i < NUM_WEIGHT_MATRICES-1; i++) {
                        weightsRiverIP[i] = constructMatrix(matrixStrings[i], numNodesRiverIP[i], numNodesRiverIP[i+1]);
                       }
                       weightsRiverIP[NUM_WEIGHT_MATRICES-1] = constructMatrix(matrixStrings[NUM_WEIGHT_MATRICES-1], numNodesRiverIP[NUM_WEIGHT_MATRICES-1], 1);
						break;
					case (NUM_WEIGHT_MATRICES*7):
					  for (int i = 0; i < NUM_WEIGHT_MATRICES-1; i++) {
                        weightsRiverOP[i] = constructMatrix(matrixStrings[i], numNodesRiverOP[i], numNodesRiverOP[i+1]);
                       }
                       weightsRiverOP[NUM_WEIGHT_MATRICES-1] = constructMatrix(matrixStrings[NUM_WEIGHT_MATRICES-1], numNodesRiverOP[NUM_WEIGHT_MATRICES-1], 1);
						break;
					}
					
					counter = counter + NUM_WEIGHT_MATRICES;
					
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
			writer = new PrintWriter("mlpWeights.txt", "UTF-8");
			String line = "";
			for (int i = 0; i < weightsPreIP.length; i++) {
			  line = weightsPreIP[i].toString();
			  writer.println(line);  
			}
			line = "";
			for (int i = 0; i < weightsPreOP.length; i++) {
              line = weightsPreOP[i].toString();
              writer.println(line);  
            }
			line = "";
            for (int i = 0; i < weightsFlopIP.length; i++) {
              line = weightsFlopIP[i].toString();
              writer.println(line);  
            }
            line = "";
            for (int i = 0; i < weightsFlopOP.length; i++) {
              line = weightsFlopOP[i].toString();
              writer.println(line);  
            }
            line = "";
            for (int i = 0; i < weightsTurnIP.length; i++) {
              line = weightsTurnIP[i].toString();
              writer.println(line);  
            }
            line = "";
            for (int i = 0; i < weightsTurnOP.length; i++) {
              line = weightsTurnOP[i].toString();
              writer.println(line);  
            }
            line = "";
            for (int i = 0; i < weightsRiverIP.length; i++) {
              line = weightsRiverIP[i].toString();
              writer.println(line);  
            }
            line = "";
            for (int i = 0; i < weightsRiverOP.length; i++) {
              line = weightsRiverOP[i].toString();
              writer.println(line);  
            }
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
	
}
