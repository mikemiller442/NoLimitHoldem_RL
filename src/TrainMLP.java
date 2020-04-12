import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class TrainMLP {

    public static void main(String[] args) {
        
        Machine Hero = new Machine("Hero", 200, .75, "mlp", true);
        Machine Villian = new Machine("Villian", 200, .75, "mlp", true);
        Game game = new Game(Hero, Villian, true);
        
        int turn = 0;
        
        while (turn < 100000) {
            game.Hand();
            game.switchBlinds();
            System.out.println("num chips gained");
            System.out.println(Hero.numChipsGained);
            System.out.println(Villian.numChipsGained);
            if (Hero.numChipsGained + Villian.numChipsGained != 0) {
                break;
            }
            if (turn % 10000 == 0) {
                Hero.decreaseEpsilon(2);
                Villian.decreaseEpsilon(2);
            }
            turn++;
        }
        
        Hero.storeWeights();
        
    }
    
}
