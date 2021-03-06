import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class TrainMLP {

    public static void main(String[] args) {
        
        Machine Hero = new Machine("Hero", 200, 0.6, "mlp", true);
        Machine Villian = new Machine("Villian", 200, 0.6, "mlp", true);
        Game game = new Game(Hero, Villian, true);
        
        int turn = 1; // start at one so BPP doesn't go to zero
        
        while (turn < 9600000) {
            game.Hand();
            game.switchBlinds();
            System.out.println("num chips gained");
            System.out.println(Hero.numChipsGained);
            System.out.println(Villian.numChipsGained);
            if (Hero.numChipsGained + Villian.numChipsGained != 0) {
                break;
            }
            if (turn % 800000 == 0) {
                Hero.decreaseEpsilon(1.15);
                Villian.decreaseEpsilon(1.15);
                Hero.decreaseBPP();
                Villian.decreaseBPP();
            }
            turn++;
        }
        
        Hero.storeWeights();
        
    }
    
}
