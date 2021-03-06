import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class TrainLFA {

	public static void main(String[] args) {
		
		Machine Hero = new Machine("Hero", 200, 0.8, "lfa", true);
		Machine Villian = new Machine("Villian", 200, 0.8, "lfa", true);
		Game game = new Game(Hero, Villian, true);
		
		int turn = 1;
		
		while (turn < 200000) {
			game.Hand();
			game.switchBlinds();
			System.out.println("num chips gained");
			System.out.println(Hero.numChipsGained);
			System.out.println(Villian.numChipsGained);
			if (Hero.numChipsGained + Villian.numChipsGained != 0) {
				break;
			}
			if (turn % 20000 == 0) {
				Hero.decreaseEpsilon(1.5);
				Villian.decreaseEpsilon(1.5);
				Hero.decreaseBPP();
                Villian.decreaseBPP();
			}
			turn++;
		}
		
		Hero.storeWeights();
		
	}
	
}
