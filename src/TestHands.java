import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TestHands {
	
	// index corresponds with the suit number in this game
	public static String[] suits = {"Spades", "Hearts", "Diamonds", "Clubs"};
	
	public static void main(String args[]) {
		
		Person Hero = new Person("Hero", 200);
		
		String row;
		String handToString;
		int[] hand_result = new int[7];
		int[] numbers = new int[14];
		
		Card[] cc = new Card[5];
		Card[] hc = new Card[2];
		
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader("hands.txt");
			br = new BufferedReader(fr);
			while (true) {
				row = br.readLine();
				
				if (row == null) {
					break;
				}
				
				String[] line = row.split(",");
				for (int i = 0; i < line.length; i++) {
					numbers[i] = Integer.parseInt(line[i]);
				}
				cc[0] = new Card(numbers[0], numbers[5]);
				cc[1] = new Card(numbers[1], numbers[6]);
				cc[2] = new Card(numbers[2], numbers[7]);
				cc[3] = new Card(numbers[3], numbers[8]);
				cc[4] = new Card(numbers[4], numbers[9]);
				
				hc[0] = new Card(numbers[10], numbers[12]);
				hc[1] = new Card(numbers[11], numbers[13]);
				
				Hero.setHand(hc);
				hand_result = Hero.bestHand(7);
				handToString = printHands(hand_result);
				System.out.println(handToString);
			}
			br.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
		}		
		
	}
	
	public static String printHands(int[] results) {
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
				finalResults = suits[results[1]] + " Flush " + Integer.toString(2+results[2]) + " high";
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
	
}