import java.util.ArrayList;
import java.util.Collections;

public class Game {
	
	private Player smallBlind;
	private Player bigBlind;
	private int pot;
	private boolean pvp;
	private final String[] suits = {"Spades", "Hearts", "Diamonds", "Clubs"};
	
	public Game(Player smallBlind, Player bigBlind, boolean pvp) {
	
		this.smallBlind = smallBlind;
		this.bigBlind = bigBlind;
		this.pvp = pvp;
		smallBlind.setIP();
		bigBlind.setOP();
	
	}
	
	public void switchBlinds() {
		Player temp;
		temp = bigBlind;
		bigBlind = smallBlind;
		smallBlind = temp;
		smallBlind.switchPosition();
		bigBlind.switchPosition();
		System.out.println();
	}
	
	private void updateState(int street, int[] bettingRoundResult) {
		smallBlind.updateStreet(street, bettingRoundResult);
		bigBlind.updateStreet(street, bettingRoundResult);
	}
	
	private void printPositions(boolean post) {
		if (!post) {
			System.out.println(smallBlind.getName() + ": " + Integer.toString(smallBlind.getChips()));
			System.out.println(bigBlind.getName() + ": " + Integer.toString(bigBlind.getChips()));
		} else {
			System.out.println("Effective stacks: " + smallBlind.getEffective());
		}
		if (smallBlind.getName().equals("Hero")) {
			smallBlind.printHand("SB");
			if (pvp) {
				bigBlind.printHand("BB");
			}
		} else {
			bigBlind.printHand("BB");
			if (pvp) {
				smallBlind.printHand("SB");
			}
		}
	}
	
	public void Hand() {
		ArrayList<Integer> cards = new ArrayList<Integer>();
		for (int i = 0; i < 52; i++) {
			cards.add(i);
		}
		Collections.shuffle(cards);
		Card[] player_cards = new Card[2];
		Card temp_card = new Card(cards.get(0) / 4, cards.get(0) % 4);
		player_cards[0] = temp_card;
		temp_card = new Card(cards.get(1) / 4, cards.get(1) % 4);
		player_cards[1] = temp_card;
		smallBlind.setHand(player_cards);
		temp_card = new Card(cards.get(2) / 4, cards.get(2) % 4);
		player_cards[0] = temp_card;
		temp_card = new Card(cards.get(3) / 4, cards.get(3) % 4);
		player_cards[1] = temp_card;
		bigBlind.setHand(player_cards);
		
		this.printPositions(false);
		
		if (smallBlind.getChips() > bigBlind.getChips()) {
			smallBlind.setEffective(bigBlind.getChips());
			bigBlind.setEffective(bigBlind.getChips());
		} else {
			smallBlind.setEffective(smallBlind.getChips());
			bigBlind.setEffective(smallBlind.getChips());
		}
		
		smallBlind.setSurplus();
		bigBlind.setSurplus();
		
		smallBlind.resetState();
		bigBlind.resetState();
		
		boolean playerAllIn = false;
		smallBlind.placeBet(1);
		if (smallBlind.getEffective() >=2 && bigBlind.getEffective() >= 2) {
			bigBlind.placeBet(2);
		} else {
			playerAllIn = true;
			bigBlind.placeBet(1);
		}
		int[] bettingRoundResult;
		
		pot = 3;
		
		Card[] cc = new Card[5];
		bettingRoundResult = bettingRound(true);
		updateState(0, bettingRoundResult);
		pot = bettingRoundResult[0];
		
		if (pot < 0) {
//			System.out.println(pot);
//			System.out.println("pot less than zero preflop");
			System.exit(0);
		}
		
		if (bettingRoundResult[1] != 0) {
			foldRound(bettingRoundResult[1], bettingRoundResult[4]);
			return;
		}
		
		cc[0] = new Card(cards.get(4) / 4, cards.get(4) % 4);
		cc[1] = new Card(cards.get(5) / 4, cards.get(5) % 4);
		cc[2] = new Card(cards.get(6) / 4, cards.get(6) % 4);
		smallBlind.setFlop(cc[0], cc[1], cc[2]);
		bigBlind.setFlop(cc[0], cc[1], cc[2]);
		int numCC = 3;
		System.out.println();
		this.printPositions(true);
		System.out.println("Here's the flop:");
		for(int i = 0; i < numCC; i++) {
			System.out.print(cc[i].toString() + " ");
		}
		System.out.println();
		if (bigBlind.getEffective() == 0 || smallBlind.getEffective() == 0) {
			playerAllIn = true;
		}
		if (!playerAllIn) {
			bettingRoundResult = bettingRound(false);
			updateState(1, bettingRoundResult);
			pot = bettingRoundResult[0];
			if (bettingRoundResult[1] != 0) {
				foldRound(bettingRoundResult[1], bettingRoundResult[4]);
				return;
			}
		}
		
		if (pot < 0) {
//			System.out.println(pot);
//			System.out.println("pot less than zero on the flop");
			System.exit(0);
		}
		
		cc[3] = new Card(cards.get(7) / 4, cards.get(7) % 4);
		smallBlind.setTurn(cc[3]);
		bigBlind.setTurn(cc[3]);
		numCC = 4;
		System.out.println();
		this.printPositions(true);
		System.out.println("Here's the turn:");
		for(int i = 0; i < numCC; i++) {
			System.out.print(cc[i].toString() + " ");
		}
		System.out.println();
		if (bigBlind.getEffective() == 0 || smallBlind.getEffective() == 0) {
			playerAllIn = true;
		}
		if (!playerAllIn) {
			bettingRoundResult = bettingRound(false);
			updateState(2, bettingRoundResult);
			pot = bettingRoundResult[0];
			if (bettingRoundResult[1] != 0) {
				foldRound(bettingRoundResult[1], bettingRoundResult[4]);
				return;
			}
		}
		
		if (pot < 0) {
//			System.out.println(pot);
//			System.out.println("pot less than zero on the turn");
			System.exit(0);
		}
		
		cc[4] = new Card(cards.get(8) / 4, cards.get(8) % 4);
		smallBlind.setRiver(cc[4]);
		bigBlind.setRiver(cc[4]);
		numCC = 5;
		System.out.println();
		this.printPositions(true);
		System.out.println("Here's the river:");
		for(int i = 0; i < numCC; i++) {
			System.out.print(cc[i].toString() + " ");
		}
		System.out.println();
		if (bigBlind.getEffective() == 0 || smallBlind.getEffective() == 0) {
			playerAllIn = true;
		}
		if (!playerAllIn) {
			bettingRoundResult = bettingRound(false);
			updateState(3, bettingRoundResult);
			pot = bettingRoundResult[0];
			if (bettingRoundResult[1] != 0) {
				foldRound(bettingRoundResult[1], bettingRoundResult[4]);
				return;
			}
		}
		
		int[] sb_result;
		int[] bb_result;
		String handToString;
		
		System.out.println("Showdown:");
		if (smallBlind.getName() == "Villian") {
			smallBlind.printHand("SB");
		} else {
			bigBlind.printHand("BB");
		}
		System.out.println();
		for(int i = 0; i < numCC; i++) {
			System.out.print(cc[i].toString() + " ");
		}
		
		System.out.println();
		System.out.print("SB's hand: ");
		sb_result = smallBlind.bestHand(7, true);
		handToString = printHands(sb_result);
		System.out.println(handToString);
		
		System.out.print("BB's hand: ");
		bb_result = bigBlind.bestHand(7, true);
		handToString = printHands(bb_result);
		System.out.println(handToString);
		
		if (pot < 0) {
//			System.out.println(pot);
//			System.out.println("pot less than zero at showdown");
			System.exit(0);
		}
		
		for (int i = 0; i < 7; i++) {
			if (sb_result[i] > bb_result[i]) {
				smallBlind.winsPot(pot, false, 0);
				bigBlind.winsPot(0, false, 0);
				System.out.println(smallBlind.getName() + " wins the pot of $" + Integer.toString(pot));
				return;
			} else if (bb_result[i] > sb_result[i]){
				smallBlind.winsPot(0, false, 0);
				bigBlind.winsPot(pot, false, 0);
				System.out.println(bigBlind.getName() + " wins the pot of $" + Integer.toString(pot));
				return;
			} else {
				continue;
			}
		}
		System.out.println("Split pot.");
		System.out.println(pot);
		smallBlind.winsPot(pot/2, false, 0);
		bigBlind.winsPot(pot/2, false, 0);
		return;
	}

	private int[] bettingRound(boolean pre) {
		Player firstToAct = bigBlind;
		Player secondToAct = smallBlind;
		int needToCall = 0;
		if (pre) {
			firstToAct = smallBlind;
			secondToAct = bigBlind;
			needToCall = 1;
		}
		
		int[] bettingRoundResult = new int[5];
		bettingRoundResult[0] = pot;
		bettingRoundResult[1] = 0; // 0 if no one folded, 1 if small blind folded, 2 if big blind folded
		bettingRoundResult[2] = pot; // initial pot
		bettingRoundResult[3] = 1; // second to act closed the action
		bettingRoundResult[4] = pot; // pot one time step removed in case of a fold
		
		smallBlind.updateAJC(0, bettingRoundResult[0], needToCall);
		bigBlind.updateAJC(0, bettingRoundResult[0], needToCall);
		int firstToActBet = firstToAct.makeDecision(needToCall, bettingRoundResult[0]);
		if (firstToActBet < -1) {
//			System.out.println("firstToActBet 1");
//			System.out.println(firstToActBet);
			System.exit(0);
		}
		if (firstToActBet != -1) {
			bettingRoundResult[0] = bettingRoundResult[0] + firstToActBet;
		} else {
			bettingRoundResult[1] = 2;
			if (pre) {
				bettingRoundResult[1] = 1;
			}
			bettingRoundResult[3] = 0;
			return bettingRoundResult;
		}
		needToCall = firstToActBet;
		if (pre) {
			needToCall = needToCall - 1;
		}
		
		smallBlind.updateAJC((bettingRoundResult[0] - bettingRoundResult[2])/bettingRoundResult[2], bettingRoundResult[0], needToCall);
		bigBlind.updateAJC((bettingRoundResult[0] - bettingRoundResult[2])/bettingRoundResult[2], bettingRoundResult[0], needToCall);
		if (pre) {
			smallBlind.updateAJC((bettingRoundResult[0] - (bettingRoundResult[2] + 1))/(bettingRoundResult[2] + 1), bettingRoundResult[0], needToCall);
			bigBlind.updateAJC((bettingRoundResult[0] - (bettingRoundResult[2] + 1))/(bettingRoundResult[2] + 1), bettingRoundResult[0], needToCall);
		}
		int secondToActBet = secondToAct.makeDecision(needToCall, bettingRoundResult[0]);
		if (secondToActBet < -1) {
//			System.out.println(firstToActBet);
//			System.out.println("secondToActBet 1");
//			System.out.println(secondToActBet);
			System.exit(0);
		}
		if (secondToActBet != -1) {
			bettingRoundResult[4] = bettingRoundResult[0] + needToCall;
			bettingRoundResult[0] = bettingRoundResult[0] + secondToActBet;
		} else {
			bettingRoundResult[1] = 1;
			if (pre) {
				bettingRoundResult[1] = 2;
			}
			bettingRoundResult[3] = 1;
			return bettingRoundResult;
		}
		needToCall = secondToActBet - needToCall;
		
		int turn = 0;
		
		while (needToCall != 0) {
			if (turn % 2 == 0) {
				smallBlind.updateAJC((bettingRoundResult[0] - bettingRoundResult[2])/bettingRoundResult[2], bettingRoundResult[0], needToCall);
				bigBlind.updateAJC((bettingRoundResult[0] - bettingRoundResult[2])/bettingRoundResult[2], bettingRoundResult[0], needToCall);
				if (pre) {
					smallBlind.updateAJC((bettingRoundResult[0] - (bettingRoundResult[2] + 1))/(bettingRoundResult[2] + 1), bettingRoundResult[0], needToCall);
					bigBlind.updateAJC((bettingRoundResult[0] - (bettingRoundResult[2] + 1))/(bettingRoundResult[2] + 1), bettingRoundResult[0], needToCall);
				}
				firstToActBet = firstToAct.makeDecision(needToCall, bettingRoundResult[0]);
				if (firstToActBet < -1) {
//					System.out.println("firstToActBet 2");
//					System.out.println(firstToActBet);
					System.exit(0);
				}
				if (firstToActBet != -1) {
					bettingRoundResult[4] = bettingRoundResult[0] + needToCall;
					bettingRoundResult[0] = bettingRoundResult[0] + firstToActBet;
				} else {
					bettingRoundResult[1] = 2;
					if (pre) {
						bettingRoundResult[1] = 1;
					}
					bettingRoundResult[3] = 0;
					return bettingRoundResult;
				}
				needToCall = firstToActBet - needToCall;
			} else {
				smallBlind.updateAJC((bettingRoundResult[0] - bettingRoundResult[2])/bettingRoundResult[2], bettingRoundResult[0], needToCall);
				bigBlind.updateAJC((bettingRoundResult[0] - bettingRoundResult[2])/bettingRoundResult[2], bettingRoundResult[0], needToCall);
				if (pre) {
					smallBlind.updateAJC((bettingRoundResult[0] - (bettingRoundResult[2] + 1))/(bettingRoundResult[2] + 1), bettingRoundResult[0], needToCall);
					bigBlind.updateAJC((bettingRoundResult[0] - (bettingRoundResult[2] + 1))/(bettingRoundResult[2] + 1), bettingRoundResult[0], needToCall);
				}
				secondToActBet = secondToAct.makeDecision(needToCall, bettingRoundResult[0]);
				if (secondToActBet < -1) {
//					System.out.println("secondToActBet 2");
//					System.out.println(secondToActBet);
					System.exit(0);
				}
				if (secondToActBet != -1) {
					bettingRoundResult[4] = bettingRoundResult[0] + needToCall;
					bettingRoundResult[0] = bettingRoundResult[0] + secondToActBet;
				} else {
					bettingRoundResult[1] = 1;
					if (pre) {
						bettingRoundResult[1] = 2;
					}
					bettingRoundResult[3] = 1;
					return bettingRoundResult;
				}
				needToCall = secondToActBet - needToCall;
			}
			turn++;
		}
		smallBlind.updateAJC((bettingRoundResult[0] - bettingRoundResult[2])/bettingRoundResult[2], bettingRoundResult[0], needToCall);
		bigBlind.updateAJC((bettingRoundResult[0] - bettingRoundResult[2])/bettingRoundResult[2], bettingRoundResult[0], needToCall);
		if (turn % 2 == 0) {
			bettingRoundResult[3] = 1;
		} else {
			bettingRoundResult[3] = 0;
		}
		
		return bettingRoundResult;
		
	}
	
	private void foldRound(int playerFolded, int finalPotWithoutLastBet) {
		if (pot < 0) {
//			System.out.println(pot);
			System.exit(0);
		}
//		System.out.println("testing who folded");
//		System.out.println(playerFolded);
		if (playerFolded == 1) {
			smallBlind.winsPot(0, true, finalPotWithoutLastBet);
			bigBlind.winsPot(pot, true, finalPotWithoutLastBet);
			System.out.println(smallBlind.getName() + " folds and shows " + smallBlind.returnHandString());
			System.out.println(bigBlind.getName() + " wins the pot of $" + Integer.toString(pot));
		} else {
			smallBlind.winsPot(pot, true, finalPotWithoutLastBet);
			bigBlind.winsPot(0, true, finalPotWithoutLastBet);
			System.out.println(bigBlind.getName() + " folds and shows " + bigBlind.returnHandString());
			System.out.println(smallBlind.getName() + " wins the pot of $" + Integer.toString(pot));
		}
	}
	
	private String printHands(int[] results) {
		String finalResults = "x";
		switch (results[0]) {
			case 0:
				finalResults = Integer.toString(2+results[1]) + " high with " + Integer.toString(2+results[2]) + "," + Integer.toString(2+results[3]) + "," + Integer.toString(2+results[4]) + "," + Integer.toString(2+results[5]);
				break;
			case 1:
				finalResults = "Pair " + Integer.toString(2+results[1]) + " with " + Integer.toString(2+results[2]) + "," + Integer.toString(2+results[3]) + "," + Integer.toString(2+results[4]);
				break;
			case 2:
				finalResults = "Two Pair " + Integer.toString(2+results[1]) + " and " + Integer.toString(2+results[2]) + " with " + Integer.toString(2+results[3]) + " high";
				break;
			case 3:
				finalResults = "Trip " + Integer.toString(2+results[1]) + " with " + Integer.toString(2+results[2]) + "," + Integer.toString(2+results[3]);
				break;
			case 4:
				finalResults = "Straight " + Integer.toString(2+results[1]) + " high";
				break;
			case 5:
				String flushSequence = "";
				for (int i = 2; i < 7; i++) flushSequence = flushSequence + results[i] + ",";
				finalResults = suits[results[1]] + " Flush with " + flushSequence.substring(0, flushSequence.length()-1);
				break;
			case 6:
				finalResults = "Full House " + Integer.toString(2+results[1]) + " over " + Integer.toString(2+results[2]);
				break;
			case 7:
				finalResults = "Quad " + Integer.toString(2+results[1]) + " with " + Integer.toString(2+results[2]) + " high";
				break;
			case 8:
				finalResults = suits[results[1]] + " Straight Flush " + Integer.toString(2+results[2]) + " high";
				break;
		}
		
		return finalResults;
		
	}
	
	public static void main(String args[]) {
		
		Person Hero = new Person("Hero", 200);
		Person Villian = new Person("Villian", 200);
		Game game = new Game(Hero, Villian, true);
		
		while (Villian.getChips() > 0 && Hero.getChips() > 0) {
			game.Hand();
			game.switchBlinds();
		}
		
		if (Villian.getChips() == 0) {
			System.out.println("Congratulations to Hero for winning the game!");
		} else {
			System.out.println("Congratulations to Villian for winning the game!");
		}
		
	}
	
}
