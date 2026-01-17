package indi.dkx.laocai.game_handler.poker;
import java.util.*;

public class deck {
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

    public int drawOne(){
        Random rand = new Random();
        randDraw = rand.nextInt(52);
        usedPokers[randDraw] = 1;
        return pokers[randDraw];
    }

    public void shuffle(){
        Arrays.fill(usedPokers, 0);
    }

}
