import reinforcementLearning.Function;
import reinforcementLearning.LFA;
import reinforcementLearning.MLP;
import reinforcementLearning.Matrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;

public class Machine extends Player {
	
	private Function fun;
	private int[] featureVectorLengths = {8,90,115,116};
	private double[] moves = {0,0,-1,0.33,0.5,0.75,1,1.5,3};
	private double[] movesToOpen = {0,0.33,0.5,0.75,1,1.5,3};
	private double[] movesToComplete = {0,-1,0.33,0.5,0.75,1,1.5,3};
	private double epsilon;
	private double betPenaltyParameter;
	private final static double MLP_InitialBPP = 20;
	private final static double LFA_Initial_BPP = 1.5;
	
	public Machine(String name, int chips, double epsilon, String function, boolean train) {
		super(name, chips);
		this.epsilon = epsilon;
		if (train) {
		  if (function.equals("lfa")) {
		    this.betPenaltyParameter = LFA_Initial_BPP;
		  } else if (function.equals("mlp")) {
		    this.betPenaltyParameter = MLP_InitialBPP;
		  } else {
		    throw new IllegalArgumentException();
		  }
		} else {
		  this.betPenaltyParameter = 1;
		}
		if (function.equals("lfa")) {
			this.fun = new LFA(epsilon, 0.96, 0.000001, train);
		} else {
			this.fun = new MLP(epsilon, 0.96, train);
		}
	}

	/*
	* The machine has essentially these moves, and not all at the same time:
	* check (1), call (2), fold (3), and bet/raise, where betting and raising can have the sizing
	* 1/3 (4), 1/2 (5), 3/4 (6), 1 (7), 1.5 (8), 3x or all-in (9)
	*/
	
	public void decreaseEpsilon(double factor) {
		this.epsilon = this.epsilon/factor;
	}
	
	public void decreaseBPP() {
      this.betPenaltyParameter = this.betPenaltyParameter - 0.33*(this.betPenaltyParameter - 1);
    }
	
	public void storeWeights() {
		fun.storeWeights();
	}
	
	public int makeDecision(int needToCall, int pot) {
		double[] features;
		double[] actions = new double[9];
		double max = Integer.MIN_VALUE;
		int maxIndex = 0;
		int bet = 0;
		
		Random rand = new Random();
		double randDouble = Math.random();
		
		for (int i = 0; i < actions.length; i++) actions[i] = Integer.MIN_VALUE;
		if (randDouble < epsilon) {
			if (needToCall == 0) {
				maxIndex = rand.nextInt(movesToOpen.length);
				while(calculateSAP(movesToOpen[maxIndex]) == Integer.MIN_VALUE) {
					maxIndex = rand.nextInt(movesToOpen.length);
				}
				bet = (int) Math.round(movesToOpen[maxIndex]*pot);
				if (bet < -1) {
		          System.out.println("bet < -1 test 1");
		          System.exit(0);
		        }
			} else {
				maxIndex = rand.nextInt(movesToComplete.length);
				while(calculateSAP(movesToComplete[maxIndex]) == Integer.MIN_VALUE) {
					maxIndex = rand.nextInt(movesToComplete.length);
				}
				if (movesToComplete[maxIndex] == -1) {
				  bet = -1;
				} else {
				  bet = (int) Math.round(needToCall + (pot + needToCall)*movesToComplete[maxIndex]);
	                if (bet < -1) {
	                  System.out.println("bet < -1 test 2");
	                  System.exit(0);
	                }
				}
			}
		} else {
			if (needToCall == 0) {
				actions[0] = calculateSAP(0);
				actions[3] = calculateSAP(0.33);
				actions[4] = calculateSAP(0.5);
				actions[5] = calculateSAP(0.75);
				actions[6] = calculateSAP(1);
				actions[7] = calculateSAP(1.5);
				actions[8] = calculateSAP(3);
			} else {
				actions[1] = calculateSAP(0);
				actions[2] = calculateSAP(-1);
				actions[3] = calculateSAP(0.33);
				actions[4] = calculateSAP(0.5);
				actions[5] = calculateSAP(0.75);
				actions[6] = calculateSAP(1);
				actions[7] = calculateSAP(1.5);
				actions[8] = calculateSAP(3);
			}
			System.out.println("Printing SAP values");
			for (int i = 0; i < actions.length; i++) System.out.println(actions[i]);
			for (int i = 0; i < actions.length; i++) {
				if (actions[i] > max) {
					max = actions[i];
					maxIndex = i;
				}
			}
			if (needToCall == 0) {
				if (maxIndex == 0) {
					bet = 0;
				}
				bet = (int) Math.round(moves[maxIndex]*pot);
				if (bet < -1) {
                  System.out.println("bet < -1 test 3");
                  System.exit(0);
                }
			} else {
				if (maxIndex == 1) {
					bet = Math.min(needToCall, effective);
				} else if (maxIndex == 2) {
					bet = -1;
				} else {
					bet = (int) Math.round(needToCall + (pot + needToCall)*moves[maxIndex]);
				}
				if (bet < -1) {
                  System.out.println("bet < -1 test 4");
                  System.exit(0);
                }
			}
		}
//		if (needToCall == 0) {
//			currentSAP = calculateSAP(movesToOpen[maxIndex]);
//		} else {
//			currentSAP = calculateSAP(movesToComplete[maxIndex]);
//		}
		double currentSAP = calculateSAP(moves[maxIndex]);
		fun.setCurrentSAP(currentSAP);
//		System.out.println("testing move selection in Machine");
//		System.out.println(maxIndex);
//		System.out.println(bet);
//		System.out.println(currentSAP);
		if (Double.isNaN(currentSAP)) {
		  System.out.println("NaN SAP");
		  System.exit(0);
		}
		if (currentSAP < -1000 || currentSAP > 1000) {
		    System.out.println("SAP value beyond reasonable limits during training");
			System.out.println(randDouble);
			System.out.println(maxIndex);
			System.out.println(moves[maxIndex]);
			System.out.println(fun.getPreviousSAP());
			System.out.println(currentSAP);
			System.exit(0);
		}
		if (bet < 0) {
			immediateReward = 0;
			bet = -1;
		} else {
			immediateReward = -1*bet/this.betPenaltyParameter;
			placeBet(bet);
		}
		if (!newHand) {
			fun.beginBackPropagation(immediateReward,ip);
		} else {
			newHand = false;
		}
		fun.setPreviousSAP(currentSAP);
		double[] previousFeatures = fun.getCurrentFeatures().clone();
//		for (int i = 0; i < previousFeatures.length; i++) previousFeatures[i] = currentFeatures[i];
		fun.setPreviousFeatures(previousFeatures);
		System.out.println(immediateReward);
		System.out.println("Machine bets " + Integer.toString(bet));
		return bet;
	}
	
	public double calculateSAP(double action) {
		
		double value;
		double[] features;
		// first term is how much villian bet and second term is how much we are raising
		if (action != 0 & (amountToCall + (pot + amountToCall)*action) > effective) {
			return Integer.MIN_VALUE;
		}
		if (action == -1) {
//			return -1*(pot-amountToCall)/2;
		    return 0;
		}
//		if ((pot/(1+this.actionJustCommitted))*this.actionJustCommitted + (pot + (pot/(1+this.actionJustCommitted))*this.actionJustCommitted)*action > this.effective) {
		if (this.getStreet() == 0) {
			features = getFeaturesPre(action);
		} else if (this.getStreet() == 1) {
			features = getFeaturesFlop(action);
		} else if (this.getStreet() == 2) {
			features = getFeaturesTurn(action);
		} else if (this.getStreet() == 3){
			features = getFeaturesRiver(action);
		} else {
			throw new IllegalArgumentException("the feature vector length is incorrect");
		}
		value = fun.functionOutput(features, ip);
		if (Double.isNaN(value)) {
		  System.out.println("debugging calculateSAP");
		  System.out.println(betPenaltyParameter);
		  System.out.println(epsilon);
		  System.out.println(this.getStreet());
		  System.out.println(pot);
		  System.out.println(amountToCall);
		}
		return value;
		
	}
	
	public void endHandWeightsUpdate(double reward) {
//		System.out.println("final SAP");
//		System.out.println(currentFeatures.length);
//		System.out.println(fold);
//		System.out.println(finalPot);
//		System.out.println(pot-amountToCall);
//		System.out.println(reward);
//		System.out.println(currentSAP);
	    if (this.getStreet() > 0) {
	      fun.beginTerminalBackPropagation(reward, ip);
	    }
	}
	
	public double[] getFeaturesPre(double action) {
		double[] features = new double[featureVectorLengths[0]];
		double handStrength = this.calculateHandStrength();
		double actionFolds = 0;
		double actionCompletes = 0;
		double actionChecks = 0;
		if (action == -1) {
			actionFolds = 1;
		} else if (action == 0){
			if (amountToCall != 0) {
				actionCompletes = action;
			} else {
				actionChecks = 1;
			}
		} else {
			actionCompletes = action;
		}
		
		features[0] = handStrength;
		features[1] = actionJustCommitted;
		features[2] = actionCompletes;
		features[3] = actionJustCommitted*actionCompletes;
		features[4] = handStrength*actionCompletes;
		features[5] = handStrength*actionJustCommitted;
		features[6] = handStrength*actionFolds;
		features[7] = actionJustCommitted*actionFolds;
		
		return features;
	}
	
	public double[] getFeaturesFlop(double action) {
		double[] features = new double[featureVectorLengths[1]];
		double handStrength = this.calculateHandStrength();
		int suitedCards = suitedCards(3);
		int numOverCards = overCards(3);
		int openEnders = straightDraws(3);
		int[] hand = bestHand(5, true);
		int[] possibleHands = new int[8]; // from pair to quads, excluding high card and straight flush
		for (int i = 0; i < possibleHands.length; i++) possibleHands[i] = 0;
		if (hand[0] > 0) {
          possibleHands[hand[0] - 1] = 1; // you had the hand indicated by the bestHand method
        }
		int[] texture = bestHand(3, false);
		int[] possibleTextures = new int[8];
		for (int i = 0; i < possibleTextures.length; i++) possibleTextures[i] = 0;
		if (texture[0] > 0) {
		  possibleTextures[texture[0] - 1] = 1;
        }
		double actionFolds = 0;
		double actionCompletes = 0;
		double actionChecks = 0;
		if (action == -1) {
			actionFolds = 1;
		} else if (action == 0){
			if (amountToCall != 0) {
				actionCompletes = action;
			} else {
				actionChecks = 1;
			}
		} else {
			actionCompletes = action;
		}
		
		features[0] = handStrength;
		features[1] = actionJustCommitted;
		features[2] = actionCompletes;
		features[3] = actionJustCommitted*actionCompletes;
		features[4] = handStrength*actionCompletes;
		
		features[5] = sb_streets[0];
		features[6] = bb_streets[0];
		features[7] = sb_sizings[0];
		features[8] = bb_sizings[0];
		
		features[9] = suitedCards;
		features[10] = numOverCards;
		features[11] = openEnders;
		
		features[12] = possibleHands[0];
		features[13] = possibleHands[1];
		features[14] = possibleHands[2];
		features[15] = possibleHands[3];
		features[16] = possibleHands[4];
		features[17] = possibleHands[5];
		features[18] = possibleHands[6];
		
		features[19] = suitedCards*actionCompletes;
		features[20] = numOverCards*actionCompletes;
		features[21] = openEnders*actionCompletes;
		features[22] = suitedCards*sb_sizings[0];
		features[23] = numOverCards*sb_sizings[0];
		features[24] = openEnders*sb_sizings[0];
		features[25] = suitedCards*bb_sizings[0];
		features[26] = numOverCards*bb_sizings[0];
		features[27] = openEnders*bb_sizings[0];
		features[28] = possibleHands[0]*actionJustCommitted;
		features[29] = possibleHands[1]*actionJustCommitted;
		features[30] = possibleHands[2]*actionJustCommitted;
		features[31] = possibleHands[3]*actionJustCommitted;
		features[32] = possibleHands[4]*actionJustCommitted;
		features[33] = possibleHands[5]*actionJustCommitted;
		features[34] = possibleHands[0]*sb_sizings[0];
		features[35] = possibleHands[1]*sb_sizings[0];
		features[36] = possibleHands[2]*sb_sizings[0];
		features[37] = possibleHands[3]*sb_sizings[0];
		features[38] = possibleHands[4]*sb_sizings[0];
		features[39] = possibleHands[5]*sb_sizings[0];
		features[40] = possibleHands[0]*bb_sizings[0];
		features[41] = possibleHands[1]*bb_sizings[0];
		features[42] = possibleHands[2]*bb_sizings[0];
		features[43] = possibleHands[3]*bb_sizings[0];
		features[44] = possibleHands[4]*bb_sizings[0];
		features[45] = possibleHands[5]*bb_sizings[0];
		
		features[46] = possibleHands[0]*handStrength;
		features[47] = possibleHands[1]*handStrength;
		features[48] = possibleHands[2]*handStrength;
		features[49] = possibleHands[3]*handStrength;
		features[50] = possibleHands[4]*handStrength;
		features[51] = possibleHands[5]*handStrength;
		features[52] = possibleHands[6]*handStrength;
		
		features[53] = possibleHands[0]*actionCompletes;
		features[54] = possibleHands[1]*actionCompletes;
		features[55] = possibleHands[2]*actionCompletes;
		features[56] = possibleHands[3]*actionCompletes;
		features[57] = possibleHands[4]*actionCompletes;
		features[58] = possibleHands[5]*actionCompletes;
		features[59] = possibleHands[6]*actionCompletes;
		
		features[60] = handStrength*actionJustCommitted;
		features[61] = handStrength*actionFolds;
		features[62] = actionJustCommitted*actionFolds;
		
		features[63] = possibleHands[0]*actionFolds;
		features[64] = possibleHands[1]*actionFolds;
		features[65] = possibleHands[2]*actionFolds;
		features[66] = possibleHands[3]*actionFolds;
		features[67] = possibleHands[4]*actionFolds;
		features[68] = possibleHands[5]*actionFolds;
		features[69] = possibleHands[6]*actionFolds;
		
		features[70] = actionChecks;
		features[71] = actionJustCommitted*actionChecks;
		features[72] = handStrength*actionChecks;
		features[73] = suitedCards*actionChecks;
		features[74] = numOverCards*actionChecks;
		features[75] = openEnders*actionChecks;
		features[76] = possibleHands[0]*actionChecks;
		features[77] = possibleHands[1]*actionChecks;
		features[78] = possibleHands[2]*actionChecks;
		features[79] = possibleHands[3]*actionChecks;
		features[80] = possibleHands[4]*actionChecks;
		features[81] = possibleHands[5]*actionChecks;
		features[82] = possibleHands[6]*actionChecks;
		
		features[83] = possibleTextures[0];
        features[84] = possibleTextures[1];
        features[85] = possibleTextures[2];
        features[86] = possibleTextures[3];
        features[87] = possibleTextures[4];
        features[88] = possibleTextures[5];
        features[89] = possibleTextures[6];
		
		return features;
	}
	
	public double[] getFeaturesTurn(double action) {
		double[] features = new double[featureVectorLengths[2]];
		double handStrength = this.calculateHandStrength();
		int suitedCards = suitedCards(3);
		int numOverCards = overCards(3);
		int openEnders = straightDraws(3);
		int[] hand = bestHand(6, true);
		int[] possibleHands = new int[8]; // from pair to quads, excluding high card and straight flush
		for (int i = 0; i < possibleHands.length; i++) possibleHands[i] = 0;
		if (hand[0] > 0) {
			possibleHands[hand[0] - 1] = 1; // you had the hand indicated by the bestHand method
		}
		int[] texture = bestHand(3, false);
        int[] possibleTextures = new int[8];
        for (int i = 0; i < possibleTextures.length; i++) possibleTextures[i] = 0;
        if (texture[0] > 0) {
          possibleTextures[texture[0] - 1] = 1;
        }
        
		double actionFolds = 0;
		double actionCompletes = 0;
		double actionChecks = 0;
		if (action == -1) {
			actionFolds = 1;
		} else if (action == 0){
			if (amountToCall != 0) {
				actionCompletes = action;
			} else {
				actionChecks = 1;
			}
		} else {
			actionCompletes = action;
		}
		
		features[0] = handStrength;
		features[1] = actionJustCommitted;
		features[2] = actionCompletes;
		features[3] = actionJustCommitted*actionCompletes;
		features[4] = handStrength*actionCompletes;
		
		features[5] = sb_streets[0];
		features[6] = bb_streets[0];
		features[7] = sb_sizings[0];
		features[8] = bb_sizings[0];
		
		features[9] = sb_streets[1];
		features[10] = bb_streets[1];
		features[11] = sb_sizings[1];
		features[12] = bb_sizings[1];
		
		features[13] = suitedCards;
		features[14] = numOverCards;
		features[15] = openEnders;
		
		features[16] = possibleHands[0];
		features[17] = possibleHands[1];
		features[18] = possibleHands[2];
		features[19] = possibleHands[3];
		features[20] = possibleHands[4];
		features[21] = possibleHands[5];
		features[22] = possibleHands[6];
		
		features[23] = suitedCards*actionCompletes;
		features[24] = numOverCards*actionCompletes;
		features[25] = openEnders*actionCompletes;
		features[26] = suitedCards*sb_sizings[0];
		features[27] = numOverCards*sb_sizings[0];
		features[28] = openEnders*sb_sizings[0];
		features[29] = suitedCards*bb_sizings[0];
		features[30] = numOverCards*bb_sizings[0];
		features[31] = openEnders*bb_sizings[0];
		features[32] = possibleHands[0]*actionJustCommitted;
		features[33] = possibleHands[1]*actionJustCommitted;
		features[34] = possibleHands[2]*actionJustCommitted;
		features[35] = possibleHands[3]*actionJustCommitted;
		features[36] = possibleHands[4]*actionJustCommitted;
		features[37] = possibleHands[5]*actionJustCommitted;
		features[38] = possibleHands[0]*sb_sizings[0];
		features[39] = possibleHands[1]*sb_sizings[0];
		features[40] = possibleHands[2]*sb_sizings[0];
		features[41] = possibleHands[3]*sb_sizings[0];
		features[42] = possibleHands[4]*sb_sizings[0];
		features[43] = possibleHands[5]*sb_sizings[0];
		features[44] = possibleHands[0]*bb_sizings[0];
		features[45] = possibleHands[1]*bb_sizings[0];
		features[46] = possibleHands[2]*bb_sizings[0];
		features[47] = possibleHands[3]*bb_sizings[0];
		features[48] = possibleHands[4]*bb_sizings[0];
		features[49] = possibleHands[5]*bb_sizings[0];

		features[50] = suitedCards*sb_sizings[1];
		features[51] = numOverCards*sb_sizings[1];
		features[52] = openEnders*sb_sizings[1];
		features[53] = suitedCards*bb_sizings[1];
		features[54] = numOverCards*bb_sizings[1];
		features[55] = openEnders*bb_sizings[1];
		features[56] = possibleHands[0]*actionJustCommitted;
		features[57] = possibleHands[1]*actionJustCommitted;
		features[58] = possibleHands[2]*actionJustCommitted;
		features[59] = possibleHands[3]*actionJustCommitted;
		features[60] = possibleHands[4]*actionJustCommitted;
		features[61] = possibleHands[5]*actionJustCommitted;
		features[62] = possibleHands[0]*sb_sizings[1];
		features[63] = possibleHands[1]*sb_sizings[1];
		features[64] = possibleHands[2]*sb_sizings[1];
		features[65] = possibleHands[3]*sb_sizings[1];
		features[66] = possibleHands[4]*sb_sizings[1];
		features[67] = possibleHands[5]*sb_sizings[1];
		features[68] = possibleHands[0]*bb_sizings[1];
		features[69] = possibleHands[1]*bb_sizings[1];
		features[70] = possibleHands[2]*bb_sizings[1];
		features[71] = possibleHands[3]*bb_sizings[1];
		features[72] = possibleHands[4]*bb_sizings[1];
		features[73] = possibleHands[5]*bb_sizings[1];
		
		features[74] = possibleHands[0]*handStrength;
		features[75] = possibleHands[1]*handStrength;
		features[76] = possibleHands[2]*handStrength;
		features[77] = possibleHands[3]*handStrength;
		features[78] = possibleHands[4]*handStrength;
		features[79] = possibleHands[5]*handStrength;
		features[80] = possibleHands[6]*handStrength;
		
		features[81] = possibleHands[0]*actionCompletes;
		features[82] = possibleHands[1]*actionCompletes;
		features[83] = possibleHands[2]*actionCompletes;
		features[84] = possibleHands[3]*actionCompletes;
		features[85] = possibleHands[4]*actionCompletes;
		features[86] = possibleHands[5]*actionCompletes;
		features[87] = possibleHands[6]*actionCompletes;
		
		features[88] = possibleHands[0]*actionFolds;
		features[89] = possibleHands[1]*actionFolds;
		features[90] = possibleHands[2]*actionFolds;
		features[91] = possibleHands[3]*actionFolds;
		features[92] = possibleHands[4]*actionFolds;
		features[93] = possibleHands[5]*actionFolds;
		features[94] = possibleHands[6]*actionFolds;
		
		features[95] = actionChecks;
		features[96] = actionJustCommitted*actionChecks;
		features[97] = handStrength*actionChecks;
		features[98] = suitedCards*actionChecks;
		features[99] = numOverCards*actionChecks;
		features[100] = openEnders*actionChecks;
		features[101] = possibleHands[0]*actionChecks;
		features[102] = possibleHands[1]*actionChecks;
		features[103] = possibleHands[2]*actionChecks;
		features[104] = possibleHands[3]*actionChecks;
		features[105] = possibleHands[4]*actionChecks;
		features[106] = possibleHands[5]*actionChecks;
		features[107] = possibleHands[6]*actionChecks;
		
		features[108] = possibleTextures[0];
        features[109] = possibleTextures[1];
        features[110] = possibleTextures[2];
        features[111] = possibleTextures[3];
        features[112] = possibleTextures[4];
        features[113] = possibleTextures[5];
        features[114] = possibleTextures[6];
		
		return features;
	}
	
	public double[] getFeaturesRiver(double action) {
		double[] features = new double[featureVectorLengths[3]];
		double handStrength = this.calculateHandStrength();
		int[] hand = bestHand(7, true);
		int[] possibleHands = new int[8]; // from pair to quads, excluding high card and straight flush
		for (int i = 0; i < possibleHands.length; i++) possibleHands[i] = 0;
		if (hand[0] > 0) {
			possibleHands[hand[0] - 1] = 1; // you had the hand indicated by the bestHand method
		}
		int[] texture = bestHand(3, false);
        int[] possibleTextures = new int[8];
        for (int i = 0; i < possibleTextures.length; i++) possibleTextures[i] = 0;
        if (texture[0] > 0) {
          possibleTextures[texture[0] - 1] = 1;
        }
        
		double actionFolds = 0;
		double actionCompletes = 0;
		double actionChecks = 0;
		if (action == -1) {
			actionFolds = 1;
		} else if (action == 0){
			if (amountToCall != 0) {
				actionCompletes = action;
			} else {
				actionChecks = 1;
			}	
		} else {
			actionCompletes = action;
		}
		
		features[0] = handStrength;
		features[1] = actionJustCommitted;
		features[2] = actionCompletes;
		features[3] = actionJustCommitted*actionCompletes;
		features[4] = handStrength*actionCompletes;
		
		features[5] = sb_streets[0];
		features[6] = bb_streets[0];
		features[7] = sb_sizings[0];
		features[8] = bb_sizings[0];
		
		features[9] = sb_streets[1];
		features[10] = bb_streets[1];
		features[11] = sb_sizings[1];
		features[12] = bb_sizings[1];
		
		features[13] = sb_streets[2];
		features[14] = bb_streets[2];
		features[15] = sb_sizings[2];
		features[16] = bb_sizings[2];
		
		features[17] = possibleHands[0];
		features[18] = possibleHands[1];
		features[19] = possibleHands[2];
		features[20] = possibleHands[3];
		features[21] = possibleHands[4];
		features[22] = possibleHands[5];
		features[23] = possibleHands[6];
		
		features[24] = possibleHands[0]*actionJustCommitted;
		features[25] = possibleHands[1]*actionJustCommitted;
		features[26] = possibleHands[2]*actionJustCommitted;
		features[27] = possibleHands[3]*actionJustCommitted;
		features[28] = possibleHands[4]*actionJustCommitted;
		features[29] = possibleHands[5]*actionJustCommitted;
		features[30] = possibleHands[0]*sb_sizings[0];
		features[31] = possibleHands[1]*sb_sizings[0];
		features[32] = possibleHands[2]*sb_sizings[0];
		features[33] = possibleHands[3]*sb_sizings[0];
		features[34] = possibleHands[4]*sb_sizings[0];
		features[35] = possibleHands[5]*sb_sizings[0];
		features[36] = possibleHands[0]*bb_sizings[0];
		features[37] = possibleHands[1]*bb_sizings[0];
		features[38] = possibleHands[2]*bb_sizings[0];
		features[39] = possibleHands[3]*bb_sizings[0];
		features[40] = possibleHands[4]*bb_sizings[0];
		features[41] = possibleHands[5]*bb_sizings[0];

		features[42] = possibleHands[0]*actionJustCommitted;
		features[43] = possibleHands[1]*actionJustCommitted;
		features[44] = possibleHands[2]*actionJustCommitted;
		features[45] = possibleHands[3]*actionJustCommitted;
		features[46] = possibleHands[4]*actionJustCommitted;
		features[47] = possibleHands[5]*actionJustCommitted;
		features[48] = possibleHands[0]*sb_sizings[1];
		features[49] = possibleHands[1]*sb_sizings[1];
		features[50] = possibleHands[2]*sb_sizings[1];
		features[51] = possibleHands[3]*sb_sizings[1];
		features[52] = possibleHands[4]*sb_sizings[1];
		features[53] = possibleHands[5]*sb_sizings[1];
		features[54] = possibleHands[0]*bb_sizings[1];
		features[55] = possibleHands[1]*bb_sizings[1];
		features[56] = possibleHands[2]*bb_sizings[1];
		features[57] = possibleHands[3]*bb_sizings[1];
		features[58] = possibleHands[4]*bb_sizings[1];
		features[59] = possibleHands[5]*bb_sizings[1];
		
		features[60] = possibleHands[0]*actionJustCommitted;
		features[61] = possibleHands[1]*actionJustCommitted;
		features[62] = possibleHands[2]*actionJustCommitted;
		features[63] = possibleHands[3]*actionJustCommitted;
		features[64] = possibleHands[4]*actionJustCommitted;
		features[65] = possibleHands[5]*actionJustCommitted;
		features[66] = possibleHands[0]*sb_sizings[2];
		features[67] = possibleHands[1]*sb_sizings[2];
		features[68] = possibleHands[2]*sb_sizings[2];
		features[69] = possibleHands[3]*sb_sizings[2];
		features[70] = possibleHands[4]*sb_sizings[2];
		features[71] = possibleHands[5]*sb_sizings[2];
		features[72] = possibleHands[0]*bb_sizings[2];
		features[73] = possibleHands[1]*bb_sizings[2];
		features[74] = possibleHands[2]*bb_sizings[2];
		features[75] = possibleHands[3]*bb_sizings[2];
		features[76] = possibleHands[4]*bb_sizings[2];
		features[77] = possibleHands[5]*bb_sizings[2];
		
		features[78] = possibleHands[0]*handStrength;
		features[79] = possibleHands[1]*handStrength;
		features[80] = possibleHands[2]*handStrength;
		features[81] = possibleHands[3]*handStrength;
		features[82] = possibleHands[4]*handStrength;
		features[83] = possibleHands[5]*handStrength;
		features[84] = possibleHands[6]*handStrength;
		
		features[85] = possibleHands[0]*actionCompletes;
		features[86] = possibleHands[1]*actionCompletes;
		features[87] = possibleHands[2]*actionCompletes;
		features[88] = possibleHands[3]*actionCompletes;
		features[89] = possibleHands[4]*actionCompletes;
		features[90] = possibleHands[5]*actionCompletes;
		features[91] = possibleHands[6]*actionCompletes;
		
		features[92] = possibleHands[0]*actionFolds;
		features[93] = possibleHands[1]*actionFolds;
		features[94] = possibleHands[2]*actionFolds;
		features[95] = possibleHands[3]*actionFolds;
		features[96] = possibleHands[4]*actionFolds;
		features[97] = possibleHands[5]*actionFolds;
		features[98] = possibleHands[6]*actionFolds;
		
		features[99] = actionChecks;
		features[100] = actionJustCommitted*actionChecks;
		features[101] = handStrength*actionChecks;
		features[102] = possibleHands[0]*actionChecks;
		features[103] = possibleHands[1]*actionChecks;
		features[104] = possibleHands[2]*actionChecks;
		features[105] = possibleHands[3]*actionChecks;
		features[106] = possibleHands[4]*actionChecks;
		features[107] = possibleHands[5]*actionChecks;
		features[108] = possibleHands[6]*actionChecks;
		
		features[109] = possibleTextures[0];
        features[110] = possibleTextures[1];
        features[111] = possibleTextures[2];
        features[112] = possibleTextures[3];
        features[113] = possibleTextures[4];
        features[114] = possibleTextures[5];
        features[115] = possibleTextures[6];
		
//		features[78] = sb_sizings[0]*actionJustCommitted;
//		features[79] = sb_sizings[1]*actionJustCommitted;
//		features[80] = sb_sizings[2]*actionJustCommitted;
//		features[81] = bb_sizings[0]*actionJustCommitted;
//		features[82] = bb_sizings[1]*actionJustCommitted;
//		features[83] = bb_sizings[2]*actionJustCommitted;
//		features[84] = sb_sizings[0]*sb_sizings[2];
//		features[85] = sb_sizings[1]*sb_sizings[2];
//		features[86] = sb_sizings[2]*sb_sizings[2];
//		features[87] = bb_sizings[3]*sb_sizings[2];
//		features[88] = bb_sizings[4]*sb_sizings[2];
//		features[89] = bb_sizings[5]*sb_sizings[2];
//		features[90] = sb_sizings[0]*bb_sizings[2];
//		features[91] = sb_sizings[1]*bb_sizings[2];
//		features[92] = sb_sizings[2]*bb_sizings[2];
		
		return features;
	}
	
	public double calculateHandStrength() {
		
		double handStrength = 0;
		double difference = Math.abs(card1.getValue() - card2.getValue());
		double highCard = Math.max(card1.getValue(), card2.getValue());
		if (highCard > 10) {
			if (highCard == 14) {
				handStrength = handStrength + 10;
			} else {
				handStrength = handStrength + highCard - 5;
			}
		} else {
			handStrength = handStrength + highCard/2;
		}
		if (card1.getValue() == card2.getValue()) {
			handStrength = handStrength*2;
		}
		if (card1.getSuit() == card2.getSuit()) {
			handStrength = handStrength + 2;
		}
		if (difference == 2) {
			handStrength = handStrength + highCard - 1;
		}
		if (difference == 3) {
			handStrength = handStrength + highCard - 2;
		}
		if (difference == 4) {
			handStrength = handStrength + highCard - 4;
		}
		if (difference > 5) {
			handStrength = handStrength + highCard - 5;
		}
		
		return handStrength;
		
	}
	
	private int overCards(int cards) {
		int firstOver = 1;
		int secondOver = 1;
		for (int i = 0; i < cards; i++) {
			if (card1.getValue() <= board[i].getValue()) {
				firstOver = 0;
			}
			if (card2.getValue() <= board[i].getValue()) {
				secondOver = 0;
			}
		}
		return firstOver + secondOver;
	}
	
	private int suitedCards(int cards) {
		int suit1Cards = 1;
		int suit1 = card1.getSuit();
		int suit2Cards = 1;
		int suit2 = card2.getSuit();
		if (card1.getSuit() != card2.getSuit()) {
			for (int i = 0; i < cards; i++) {
				if (board[i].getSuit() == suit1) {
					suit1Cards++;
				}
				if (board[i].getSuit() == suit2) {
					suit2Cards++;
				}
			}
		} else {
			suit1Cards++;
			for (int i = 0; i < cards; i++) {
				if (board[i].getSuit() == suit1) {
					suit1Cards++;
				}
			}
		}
		return Math.max(suit1Cards, suit2Cards);
	}
	
	private int straightDraws(int cards) {
		Card[] cardArray = new Card[cards + 2];
		for (int i = 0; i < board.length; i++) cardArray[i] = board[i];
		cardArray[cards] = card1;
		cardArray[cards + 1] = card2;
		Arrays.sort(cardArray);
		
		int straightCounter = 1;
		int prevCard = 0;
		int openEnder = 0;
		
		for (int i = cardArray.length - 1; i >= 0; i--) {
			if (i == cardArray.length - 1) {
				prevCard = cardArray[i].getValue();
			} else {
				if (prevCard - cardArray[i].getValue() == 0) {
					continue;
				} else if (prevCard - cardArray[i].getValue() == 1) {
					straightCounter++;
					prevCard = cardArray[i].getValue();
				} else {
					if (straightCounter >= 4) {
						openEnder = 1;
						break;
					} else {
						prevCard = cardArray[i].getValue();
						straightCounter = 1;
					}
					
				}
			}
			if (straightCounter >= 4) {
				openEnder = 1;
				break;
			}
		}
		return openEnder;
	}
	
	public static void main(String args[]) {
		
		Person Hero = new Person("Hero", 200);
		Machine Villian = new Machine("Villian", 200, 0, "mlp", false); // epsilon = 0 so it makes greedy decisions
		Game game = new Game(Hero, Villian, false); // pvp is false
		
		while (Villian.getChips() > 0 && Hero.getChips() > 0) {
			game.Hand();
			game.switchBlinds();
			System.out.println("num chips gained");
			System.out.println("Hero has gained " + Integer.toString(Hero.numChipsGained));
			System.out.println("Villian has gained " + Integer.toString(Villian.numChipsGained));
		}
		
		if (Villian.getChips() == 0) {
			System.out.println("Congratulations to Hero for winning the game!");
		} else {
			System.out.println("Congratulations to Villian for winning the game!");
		}
		
	}
	
}
