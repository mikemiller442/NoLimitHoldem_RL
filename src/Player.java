import java.util.Arrays;

public abstract class Player {
	
	protected String name;
	protected int chips;
	protected int effective;
	protected int surplus;
	protected Card card1;
	protected Card card2;
	protected boolean ip;
	protected boolean newHand;
	protected double immediateReward;
	protected int numChipsGained;
	
	protected Card[] board;
	protected int pot;
	protected int street;
	protected int actionJustCommitted;
	protected int amountToCall;
	protected int[] sb_streets; // 0 check, 1 call, 2 bet
	protected int[] bb_streets;
	protected int[] sb_sizings;
	protected int[] bb_sizings;
	
	public Player(String name, int chips) {
		this.name = name;
		this.chips = chips;
		this.numChipsGained = 0;
		
		this.board = new Card[5];
		this.pot = 0;
		this.street = 0;
		this.sb_streets = new int[4];
		this.bb_streets = new int[4];
		this.sb_sizings = new int[4];
		this.bb_sizings = new int[4];
		
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setIP() {
		this.ip = true;
	}
	
	public void setOP() {
		this.ip = false;
	}
	
	public void switchPosition() {
		this.ip = !this.ip;
	}
	
	public void setHand(Card[] hole_cards) {
		this.card1 = hole_cards[0];
		this.card2 = hole_cards[1];
	}
	
	public void setSurplus() {
		this.surplus = this.chips - this.effective;
	}
	
	public int getSurplus() {
		return this.surplus;
	}
	
	public void setEffective(int chips) {
		this.effective = chips;
	}
	
	public int getEffective() {
		return this.effective;
	}
	
	public int getChips() {
		return this.chips;
	}
	
	public void winsPot(int finalPot, boolean fold, int finalPotWithoutLastBet) {
//		if (this.street != 0 || this.actionJustCommitted != 0) {
//			this.endHandWeightsUpdate(finalPot, fold);
//		}
		if (!newHand) {
			int reward;
			if (fold) { // someone folded
				if (finalPot == 0) { // this player folded
//					reward = -1*(pot/(1+this.actionJustCommitted))/2;
//					reward = -1*(pot-amountToCall)/2;
					reward = 0;
				} else { // this player got their opponent to fold
//					reward = (finalPot+immediateReward)/2;
//					reward = (pot-amountToCall)/2;
					reward = pot-amountToCall;
				}
			} else { // hand went to showdown
				if (finalPot == 0) { // this player lost at showdown
//					reward = -1*(pot)/2;
					reward = 0;
				} else {
//					reward = finalPot/2;
					reward = finalPot;
				}
			}
			this.endHandWeightsUpdate(reward);
		}
		this.effective = this.effective + finalPot;
		this.chips = this.effective + this.surplus;
		this.surplus = 0;
		this.numChipsGained = this.numChipsGained + (this.chips - 200);
		this.chips = 200;
	}
	
	public void placeBet(int bet) {
		this.effective = this.effective - bet;
		if (this.effective < 0) {
			System.out.println(bet);
			System.out.println(this.effective);
			throw new IllegalArgumentException("You tried to bet more chips than you had!");
		}
	}
	
	public void printHand(String position) {
		System.out.println(this.getName() + " in the " + position + " with " + card1.toString() + " " + card2.toString());
	}
	
	public abstract void endHandWeightsUpdate(double reward);
	
	public abstract int makeDecision(int needToCall, int pot);
	
	public void resetState() {
		board = new Card[5];
		street = 0;
		actionJustCommitted = 0;
		amountToCall = 0;
		immediateReward = 0;
		newHand = true;
		sb_streets = new int[4];
		bb_streets = new int[4];
		sb_sizings = new int[4];
		bb_sizings = new int[4];
	}
	
	public int getStreet() {
		return this.street;
	}
	
	public void updateAJC(int AJC, int updatedPot, int needToCall) {
		actionJustCommitted = AJC;
		pot = updatedPot;
		amountToCall = needToCall;
	}
	
	public void setFlop(Card c1, Card c2, Card c3) {
		amountToCall = 0;
		board[0] = c1;
		board[1] = c2;
		board[2] = c3;
		street++;
	}
	
	public void setTurn(Card c4) {
		amountToCall = 0;
		board[3] = c4;
		street++;
	}
	
	public void setRiver(Card c5) {
		amountToCall = 0;
		board[4] = c5;
		street++;
	}
	
	public void updateStreet(int street, int[] bettingRoundResult) {
		int betSize;
		if (street != 0) {
			if (bettingRoundResult[0] - bettingRoundResult[2] == 0) {
				sb_streets[street] = 0;
				bb_streets[street] = 0;
				sb_sizings[street] = 0;
				bb_sizings[street] = 0;
			} else {
				if (bettingRoundResult[3] == 1) { // second to act closed the action
					sb_streets[street] = 1;
					bb_streets[street] = 2;
					betSize = (bettingRoundResult[0] - bettingRoundResult[2])/2;
					sb_sizings[street] = 0;
					bb_sizings[street] = betSize/bettingRoundResult[2];
				} else { // first to act closed the action
					sb_streets[street] = 2;
					bb_streets[street] = 1;
					betSize = (bettingRoundResult[0] - bettingRoundResult[2])/2;
					sb_sizings[street] = betSize/bettingRoundResult[2];
					bb_sizings[street] = 0;
				}
			}
		} else {
			if (bettingRoundResult[0] - bettingRoundResult[2] == 1) {
				sb_streets[street] = 1;
				bb_streets[street] = 0;
			} else {
				if (bettingRoundResult[3] == 1) { // second to act closed the action
					sb_streets[street] = 2;
					bb_streets[street] = 1;
					betSize = (bettingRoundResult[0] - (bettingRoundResult[2] + 1))/2;
					sb_sizings[street] = betSize/(bettingRoundResult[2] + 1);
					bb_sizings[street] = 0;
				} else { // first to act closed the action
					sb_streets[street] = 1;
					bb_streets[street] = 2;
					betSize = (bettingRoundResult[0] - (bettingRoundResult[2] + 1))/2;
					sb_sizings[street] = 0;
					bb_sizings[street] = betSize/(bettingRoundResult[2] + 1);
				}
			}
		}
	}
	
	public int[] bestHand(int numCards) {
		Card[] cardArray = new Card[numCards];
		Card[] aceLowCardArray = new Card[numCards];
		for (int i = 0; i < numCards-2; i++) {
			cardArray[i] = board[i];
			if (board[i].getValue() == 12) {
				aceLowCardArray[i] = new Card(-1, board[i].getSuit());
			} else {
				aceLowCardArray[i] = board[i];
			}
		}
		cardArray[numCards - 2] = this.card1;
		cardArray[numCards - 1] = this.card2;
		
		if (this.card1.getValue() == 12) {
			aceLowCardArray[numCards - 2] = new Card(-1, this.card1.getSuit());
		} else {
			aceLowCardArray[numCards - 2] = this.card1;
		}
		if (this.card2.getValue() == 12) {
			aceLowCardArray[numCards - 1] = new Card(-1, this.card1.getSuit());
		} else {
			aceLowCardArray[numCards - 1] = this.card2;
		}
		
		Arrays.sort(cardArray);
		Arrays.sort(aceLowCardArray);
		boolean straight = false;
		
//		for (int j = 0; j < cardArray.length; j++) {
//			System.out.print(cardArray[j].toString() + " ");
//		}
//		System.out.println();
		
		int[] madeHand = {0,0,0,0,0,0,0};
		
		int flush_suit = 10;
		
		boolean flush = false;
		int highFlushCard = 0;
		int suitCounter = 0;
		
		// Looking for flushes
		for (int i = 0; i < 4; i++) { // looping through the four suits
			suitCounter = 0;
			for (int j = 0; j < cardArray.length; j++) {
				if (cardArray[j].getSuit() == i) {
					if (cardArray[j].getValue() > highFlushCard) {
						highFlushCard = cardArray[j].getValue();
					}
					suitCounter++;
				}
			}
			if (suitCounter >= 5) {
				flush = true;
				flush_suit = i;
				break;
			} else {
				highFlushCard = 0;
			}
		}
		
		// zeroth index is boolean for straight present and first index for high card in the straight
		int[] aceLowResults = findStraights(aceLowCardArray);
		int[] aceHighResults = findStraights(cardArray);
		
		if (aceLowResults[0] == 1 || aceHighResults[0] == 1) {
			straight = true;
		}
		
		int highStraightCardLowAce = aceLowResults[1];
		int highStraightCardHighAce = aceHighResults[1];
		
		int highStraightFlushCard;
		int[] flushResults;
		
		if (flush && straight) {
			if (highStraightCardHighAce > 0) {
				highStraightFlushCard = findStraightFlush(cardArray, flush_suit);
				if (highStraightFlushCard != -1) {
					madeHand[0] = 8;
					madeHand[1] = flush_suit;
					madeHand[2] = highStraightFlushCard;
					return madeHand;
				} else {
					madeHand[0] = 5; 				// flush
					madeHand[1] = flush_suit;
					flushResults = this.findBestFlush(cardArray, flush_suit);
					for (int i = 2; i < 7; i++) {
						madeHand[i] = flushResults[i-2];
					}
				}
			} else if (highStraightCardLowAce > 0) {
				highStraightFlushCard = findStraightFlush(aceLowCardArray, flush_suit);
				if (highStraightFlushCard != -1) {
					madeHand[0] = 8;
					madeHand[1] = flush_suit;
					madeHand[2] = highStraightFlushCard;
					return madeHand;
				} else {
					madeHand[0] = 5; 				// flush
					madeHand[1] = flush_suit;
					flushResults = this.findBestFlush(cardArray, flush_suit);
					for (int i = 2; i < 7; i++) {
						madeHand[i] = flushResults[i-2];
					}
				}
			}
		} else if (flush) {
			madeHand[0] = 5; 				// flush
			madeHand[1] = flush_suit;
			flushResults = this.findBestFlush(cardArray, flush_suit);
			for (int i = 2; i < 7; i++) {
				madeHand[i] = flushResults[i-2];
			}
		} else if (straight) {
			madeHand[0] = 4; 				// straight
			madeHand[1] = Math.max(highStraightCardHighAce, highStraightCardLowAce);
		}
		
		int[] set_num = {0,0,0}; // number of cards in the set, e.g. quads
		int[] set_val = {0,0,0}; // card value of the set, e.g. trip nines
		
		int numInSet = 0;
		int numSets = 0;
		int prevCard = 0;
		
		// Looking for sets
		for (int i = cardArray.length - 1; i >= 0; i--) {
			if (i == cardArray.length-1) {
				prevCard = cardArray[i].getValue();
				numInSet = 1;
			} else {
				if (prevCard == cardArray[i].getValue()) {
					numInSet++;
					set_num[numSets] = numInSet; // this set has numInSet cards
					set_val[numSets] = cardArray[i].getValue(); // this set has the value of the current card
				} else {
					if (numInSet >= 2) {
						numSets++;
					}
					prevCard = cardArray[i].getValue();
					numInSet = 1;
				}
			}
		}
		
		int[] set_num_result = findMax(set_num, set_val); // finds the best sets, e.g. quads over trips
												// returns index and maximum set number of the two best sets
		int maxIndex = set_num_result[0]; // index of the best set
		int secondMaxIndex = set_num_result[2]; // index of the second best set
		
		int[] setHC = new int[3];
		
		int numCardsFound = 0;
		
		// Looking for quads
		if (set_num[maxIndex] == 4) {
			int i = cardArray.length - 1;
			while (cardArray[i].getValue() == set_val[maxIndex] || numCardsFound < 1) {
				if (cardArray[i].getValue() != set_val[maxIndex]) {
					setHC[0] = cardArray[i].getValue();
					madeHand[0] = 7; 						// made quads!!
					madeHand[1] = set_val[maxIndex];
					madeHand[2] = setHC[0];
					return madeHand;
				}
				i--;
			}
		}
		
		int i = cardArray.length - 1;
		
		// Looking for trips, pairs, and boats
		if (set_num[maxIndex] == 3) {
			if (secondMaxIndex != maxIndex && set_num[secondMaxIndex] >= 2) {
				madeHand[0] = 6; 						// full house!
				madeHand[1] = set_val[maxIndex];
				madeHand[2] = set_val[secondMaxIndex];
				return madeHand;
			} else if (madeHand[0] == 5 || madeHand[0] == 4) {
				return madeHand;
			} else {
				while (cardArray[i].getValue() == set_val[maxIndex] || numCardsFound < 2) {
					if (cardArray[i].getValue() != set_val[maxIndex]) {
						setHC[numCardsFound] = cardArray[i].getValue();
						numCardsFound++;
					}
					i--;
					if (i == 0) {
						break;
					}
				}
				madeHand[0] = 3;					// three of a kind
				madeHand[1] = set_val[maxIndex];
				madeHand[2] = setHC[0];
				madeHand[3] = setHC[1];
				return madeHand;
			}
		} else if (madeHand[0] == 5 || madeHand[0] == 4) {
			return madeHand;
		} else if (set_num[maxIndex] == 2) {
			if (secondMaxIndex != maxIndex && set_num[secondMaxIndex] == 2) {
				while (cardArray[i].getValue() == set_val[maxIndex] || cardArray[i].getValue() == set_val[secondMaxIndex] || numCardsFound < 1) {
					if (cardArray[i].getValue() != set_val[maxIndex] && cardArray[i].getValue() != set_val[secondMaxIndex]) {
						setHC[numCardsFound] = cardArray[i].getValue();
						madeHand[0] = 2;							// two pair
						madeHand[1] = set_val[maxIndex];
						madeHand[2] = set_val[secondMaxIndex];
						madeHand[3] = setHC[0];
						return madeHand;
					}
					i--;
					if (i == 0) {
						break;
					}
				}
			} else {
//				System.out.println(i);
//				System.out.println(maxIndex);
//				System.out.println(cardArray[i]);
//				System.out.println("printing set_val");
//				for (int p = 0; p < set_val.length; p++) System.out.println(set_val[p]);
//				System.out.println(cardArray[i].getValue());
//				System.out.println(set_val[maxIndex]);
				while (cardArray[i].getValue() == set_val[maxIndex] || numCardsFound < 3) {
					if (cardArray[i].getValue() != set_val[maxIndex]) {
						setHC[numCardsFound] = cardArray[i].getValue();
						numCardsFound++;
//						System.out.println("numCardsFound");
//						System.out.println(numCardsFound);
					}
					i--;
					if (i == 0) {
						break;
					}
//					System.out.println("printing i");
//					System.out.println(i);
				}
				madeHand[0] = 1; 			// one pair
				madeHand[1] = set_val[maxIndex];
				madeHand[2] = setHC[0];
				madeHand[3] = setHC[1];
				madeHand[4] = setHC[2];
				return madeHand;
			}
		} else {
			for (int j = 1; j < numCards - 1; j++) madeHand[j] = cardArray[numCards - j].getValue();
			return madeHand;
		}
		return madeHand;
	}
	
	// returns index and maximum set number of the two best sets
	private int[] findMax(int[] set_num, int[] set_val) {
		int index = 0;
		int max_set_num = 0;
		int max_set_val = 0;
		for (int i = 0; i < set_num.length; i++) {
			if (set_num[i] >= max_set_num) {
				if (set_num[i] == max_set_num) {
					if (set_val[i] > max_set_val) {
						max_set_num = set_num[i];
						max_set_val = set_val[i];
						index = i;
					} else {
						continue;
					}
				} else {
					max_set_num = set_num[i];
					max_set_val = set_val[i];
					index = i;
				}
			}
		}
		int second_index = 0;
		int second_max_set_num = 0;
		int second_max_set_val = 0;
		for (int i = 0; i < set_num.length; i++) {
			if (i != index) {
				if (set_num[i] >= second_max_set_num) {
					if (set_num[i] == second_max_set_num) {
						if (set_val[i] > second_max_set_val) {
							second_max_set_num = set_num[i];
							second_max_set_val = set_val[i];
							second_index = i;
						} else {
							continue;
						}
					} else {
						second_max_set_num = set_num[i];
						second_max_set_val = set_val[i];
						second_index = i;
					}
				}
			}
		}
		int[] result = {index, max_set_num, second_index, second_max_set_num};
		return result;
	}
	
	private int findStraightFlush(Card[] cardArray, int flush_suit) {
		
		int flushCardCounter = 0;
		for (int i = 0; i < cardArray.length; i++) {
			if (cardArray[i].getSuit() == flush_suit) {
				flushCardCounter++;
			}
		}
		Card[] flushCards = new Card[flushCardCounter];
		flushCardCounter = 0;
		for (int i = 0; i < cardArray.length; i++) {
			if (cardArray[i].getSuit() == flush_suit) {
				flushCards[flushCardCounter] = cardArray[i];
				flushCardCounter++;
			}
		}
		int[] results = findStraights(flushCards);
		return results[1];
		
	}
	
	private int[] findBestFlush(Card[] cardArray, int suit) {	
		int[] flushArray = new int[5];
		int counter = 0;
		for (int j = cardArray.length - 1; j >= 0; j--) {
			if (cardArray[j].getSuit() == suit) {
				flushArray[counter] = cardArray[j].getValue();
				counter++;
				if (counter >= 5) {
					break;
				}
			}
		}
		return flushArray;
	}
	
	// returns int array with zeroth index boolean for straight present and first index for high card in the straight
	private int[] findStraights(Card[] cardArray) {
		
		int straightCounter = 1;
		int prevCard = 0;
		int highStraightCard = 0;
		int[] results = new int[2];
		results[0] = 0;
		
		for (int i = cardArray.length - 1; i >= 0; i--) {
			if (i == cardArray.length - 1) {
				highStraightCard = cardArray[i].getValue();
				prevCard = cardArray[i].getValue();
			} else {
				if (prevCard - cardArray[i].getValue() == 0) {
					continue;
				} else if (prevCard - cardArray[i].getValue() == 1) {
					straightCounter++;
					prevCard = cardArray[i].getValue();
				} else {
					if (straightCounter >= 5) {
						results[0] = 1;
						break;
					} else {
						prevCard = cardArray[i].getValue();
						highStraightCard = cardArray[i].getValue();
						straightCounter = 1;
					}
					
				}
			}
			if (straightCounter >= 5) {
				results[0] = 1;
				break;
			}
		}
		if (straightCounter >= 5) {
			results[0] = 1;
		}
		if (results[0] == 1) {
			results[1] = highStraightCard;
		} else {
			results[1] = -1;
		}
		
		return results;
		
	}
	
}