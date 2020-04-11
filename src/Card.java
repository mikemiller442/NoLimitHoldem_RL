public class Card implements Comparable<Card> {
	
	private int value;
	private int suit;

	// constructor takes value followed by suit
	public Card(int value, int suit) {
		this.value = value;
		this.suit = suit;
	}
	
	public String toString() {
		String valueLetter = "x";
		String suitLetter = "x";
		
		if (this.value <= 7) {
			valueLetter = Integer.toString(2+this.value);
		} else {
			switch (this.value) {
				case 8:
					valueLetter = "T";
					break;
				case 9:
					valueLetter = "J";
					break;
				case 10:
					valueLetter = "Q";
					break;
				case 11:
					valueLetter = "K";
					break;
				case 12:
					valueLetter = "A";
					break;
			}
		}
		switch (this.suit) {
			case 0:
				suitLetter = "s";
				break;
			case 1:
				suitLetter = "h";
				break;
			case 2:
				suitLetter = "d";
				break;
			case 3:
				suitLetter = "c";
				break;
		}
		return valueLetter + suitLetter;
	}
	
	public int compareTo(Card anotherCard) {
        return this.getValue() - anotherCard.getValue();
    }
	
	public int getSuit() {
		return this.suit;
	}
	
	public int getValue() {
		return this.value;
	}

	
}
