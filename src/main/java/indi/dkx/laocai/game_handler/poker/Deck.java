package indi.dkx.laocai.game_handler.poker;

import java.util.*;

public class Deck {
    private static final Poker[] pokers = {
            new Poker(Suit.SPADES, Rank.TWO, "♠2"),
            new Poker(Suit.SPADES, Rank.THREE, "♠3"),
            new Poker(Suit.SPADES, Rank.FOUR, "♠4"),
            new Poker(Suit.SPADES, Rank.FIVE, "♠5"),
            new Poker(Suit.SPADES, Rank.SIX, "♠6"),
            new Poker(Suit.SPADES, Rank.SEVEN, "♠7"),
            new Poker(Suit.SPADES, Rank.EIGHT, "♠8"),
            new Poker(Suit.SPADES, Rank.NINE, "♠9"),
            new Poker(Suit.SPADES, Rank.TEN, "♠10"),
            new Poker(Suit.SPADES, Rank.JACK, "♠J"),
            new Poker(Suit.SPADES, Rank.QUEEN, "♠Q"),
            new Poker(Suit.SPADES, Rank.KING, "♠K"),
            new Poker(Suit.SPADES, Rank.ACE, "♠A"),

            new Poker(Suit.HEARTS, Rank.TWO, "♥2"),
            new Poker(Suit.HEARTS, Rank.THREE, "♥3"),
            new Poker(Suit.HEARTS, Rank.FOUR, "♥4"),
            new Poker(Suit.HEARTS, Rank.FIVE, "♥5"),
            new Poker(Suit.HEARTS, Rank.SIX, "♥6"),
            new Poker(Suit.HEARTS, Rank.SEVEN, "♥7"),
            new Poker(Suit.HEARTS, Rank.EIGHT, "♥8"),
            new Poker(Suit.HEARTS, Rank.NINE, "♥9"),
            new Poker(Suit.HEARTS, Rank.TEN, "♥10"),
            new Poker(Suit.HEARTS, Rank.JACK, "♥J"),
            new Poker(Suit.HEARTS, Rank.QUEEN, "♥Q"),
            new Poker(Suit.HEARTS, Rank.KING, "♥K"),
            new Poker(Suit.HEARTS, Rank.ACE, "♥A"),

            new Poker(Suit.DIAMONDS, Rank.TWO, "♦2"),
            new Poker(Suit.DIAMONDS, Rank.THREE, "♦3"),
            new Poker(Suit.DIAMONDS, Rank.FOUR, "♦4"),
            new Poker(Suit.DIAMONDS, Rank.FIVE, "♦5"),
            new Poker(Suit.DIAMONDS, Rank.SIX, "♦6"),
            new Poker(Suit.DIAMONDS, Rank.SEVEN, "♦7"),
            new Poker(Suit.DIAMONDS, Rank.EIGHT, "♦8"),
            new Poker(Suit.DIAMONDS, Rank.NINE, "♦9"),
            new Poker(Suit.DIAMONDS, Rank.TEN, "♦10"),
            new Poker(Suit.DIAMONDS, Rank.JACK, "♦J"),
            new Poker(Suit.DIAMONDS, Rank.QUEEN, "♦Q"),
            new Poker(Suit.DIAMONDS, Rank.KING, "♦K"),
            new Poker(Suit.DIAMONDS, Rank.ACE, "♦A"),

            new Poker(Suit.CLUBS, Rank.TWO, "♣2"),
            new Poker(Suit.CLUBS, Rank.THREE, "♣3"),
            new Poker(Suit.CLUBS, Rank.FOUR, "♣4"),
            new Poker(Suit.CLUBS, Rank.FIVE, "♣5"),
            new Poker(Suit.CLUBS, Rank.SIX, "♣6"),
            new Poker(Suit.CLUBS, Rank.SEVEN, "♣7"),
            new Poker(Suit.CLUBS, Rank.EIGHT, "♣8"),
            new Poker(Suit.CLUBS, Rank.NINE, "♣9"),
            new Poker(Suit.CLUBS, Rank.TEN, "♣10"),
            new Poker(Suit.CLUBS, Rank.JACK, "♣J"),
            new Poker(Suit.CLUBS, Rank.QUEEN, "♣Q"),
            new Poker(Suit.CLUBS, Rank.KING, "♣K"),
            new Poker(Suit.CLUBS, Rank.ACE, "♣A"),

            new Poker(Suit.JOKER, Rank.BLACK, "♠JOKER"),
            new Poker(Suit.JOKER, Rank.RED, "♦JOKER"),
    };

    private boolean[] usedPokers = new boolean[54];

    public Poker drawOne(){
        Random rand = new Random();
        int randDraw = rand.nextInt(54);
        while(usedPokers[randDraw]) randDraw = rand.nextInt(54);
        usedPokers[randDraw] = true;
        return pokers[randDraw];
    }

    public void shuffle(){
        Arrays.fill(usedPokers, false);
    }

    public static void main(String[] args){
        Deck deck = new Deck();
        deck.shuffle();
        System.out.println(deck.drawOne().name());
        deck.shuffle();
        for (int i=0;i<54;i++) {
            System.out.println(deck.drawOne().name());
        }
        boolean flag = true;
        for (int i=0;i<54;i++) {
            if (!deck.usedPokers[i]) flag = false;
        }
        System.out.println(flag);
    }
}

