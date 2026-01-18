package indi.dkx.laocai.game_handler.poker;
import java.util.*;

public class Deck {
    private final String[] pokers = {
        // ♣ 梅花 Clubs
        "♣2", "♣3", "♣4", "♣5", "♣6", "♣7", "♣8", "♣9", "♣10", "♣J", "♣Q", "♣K", "♣A",
        // ♦ 方片 Diamonds
        "♦2", "♦3", "♦4", "♦5", "♦6", "♦7", "♦8", "♦9", "♦10", "♦J", "♦Q", "♦K", "♦A",
        // ♥ 红桃 Hearts
        "♥2", "♥3", "♥4", "♥5", "♥6", "♥7", "♥8", "♥9", "♥10", "♥J", "♥Q", "♥K", "♥A",
        // ♠ 黑桃 Spades
        "♠2", "♠3", "♠4", "♠5", "♠6", "♠7", "♠8", "♠9", "♠10", "♠J", "♠Q", "♠K", "♠A"
    };

    private boolean[] usedPokers = new boolean[52];

    public String drawOne(){
        Random rand = new Random();
        int randDraw = rand.nextInt(52);
        while(usedPokers[randDraw]) randDraw = rand.nextInt(52);
        usedPokers[randDraw] = true;
        return pokers[randDraw];
    }

    public void shuffle(){
        Arrays.fill(usedPokers, false);
    }

    public static void main(String[] args){
        Deck deck1 = new Deck();
        deck1.shuffle();
        System.out.println(deck1.drawOne());
        deck1.shuffle();
        for(int i=0;i<52;i++){
            System.out.println(deck1.drawOne());
        }
        boolean flag = true;
        for(int i=0;i<52;i++){
            if(!deck1.usedPokers[i])flag = false;
        }
        System.out.println(flag);
    }

}

