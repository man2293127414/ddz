package com.ddz.server.shuffle;

import com.ddz.common.Card;
import com.ddz.server.ClientHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RandomShuffleStrategy implements ShuffleStrategy {
    @Override
    public List<Card> shuffle(String lastWinnerId, Map<String, ClientHandler> players, List<Card> lastLandlordCards) {
        System.out.println("执行策略：标准随机洗牌");
        List<Card> deck = new ArrayList<>(54);
        for (Card card : Card.values()) {
            if (card == Card.S || card == Card.B) {
                deck.add(card);
            } else {
                for (int i = 0; i < 4; i++) {
                    deck.add(card);
                }
            }
        }
        Collections.shuffle(deck);
        return deck;
    }
}
