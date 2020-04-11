import java.util.Scanner;

public class Person extends Player {
	
	public Person(String name, int chips) {
		super(name, chips);
	}
	
	public int makeDecision(int needToCall, int pot) {
		String input;
		int bet;
		@SuppressWarnings("resource")
		Scanner scn = new Scanner(System.in);
		System.out.println("Pot: $" + pot + ". Need to call: $" + needToCall);
		if (needToCall > 0) {
			while (true) {
				System.out.println(this.getName() + " to bet. (fold/call/raise/shove)");
				System.out.println("Raise minimum is $" + Integer.toString(needToCall) + ", otherwise shove.");
				input = scn.nextLine();
				if (input.equals("fold")) {
					return -1;
				} else if (input.equals("call")) {
					if (this.getEffective() >= needToCall) {
						this.placeBet(needToCall);
						return needToCall;
					} else {
						System.out.println("You don't have enough chips and need to shove all in to call.");
					}
				} else if (input.equals("raise")) {
					System.out.println("Enter your raise, minimum $" + Integer.toString(needToCall));
					bet = Integer.parseInt(scn.nextLine());
					if (bet >= needToCall + needToCall) {
						if (this.getEffective() >= bet) {
							this.placeBet(bet);
							return bet;
						} else {
							System.out.println("You don't have enough chips and need to shove all in.");
						}
					} else {
						System.out.println("Bet isn't above minimum raise. Either call, raise higher, or shove.");
					}
				} else if (input.equals("shove")) {
					bet = this.getEffective();
					this.placeBet(bet);
					return bet;
				} else {
					System.out.println("Your input was not valid. Please try again.");
				}
			}
		} else {
			while (true) {
				System.out.println(this.getName() + " to bet. (check/bet/shove)");
				input = scn.nextLine();
				if (input.equals("check")) {
					return 0;
				} else if (input.equals("bet")) {
					System.out.println("Enter your bet, minimum $2");
					bet = Integer.parseInt(scn.nextLine());
					if (bet >= 2) {
						if (this.getEffective() >= bet) {
							this.placeBet(bet);
							return bet;
						} else {
							System.out.println("You don't have enough chips and need to shove all in.");
						}
					} else {
						System.out.println("Bet isn't above minimum bet. Either check or bet higher.");
					}
				} else if (input.equals("shove")) {
					bet = this.effective;
					this.placeBet(bet);
					return bet;
				} else {
					System.out.println("Your input was not valid. Please try again.");
				}
			}
		}
	}
	
	public void endHandWeightsUpdate(double reward) {
		return;
	}
	
}