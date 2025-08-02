package com.ddz.server.shuffle;
import com.ddz.common.Card;
import com.ddz.common.Player;
import com.ddz.server.ClientHandler;

import java.util.*;
import java.util.stream.Collectors;


public class PassionShuffleStrategy implements ShuffleStrategy{
    private final Random random = new Random();

    @Override
    public List<Card> shuffle(String lastWinnerId, Map<String, ClientHandler> players, List<Card> lastLandlordCards) {
        System.out.println("执行策略：激情模式洗牌");

        // 1. 初始化完整牌堆
        List<Card> allCards = new ArrayList<>(54);
        for (Card card : Card.values()) {
            if (card == Card.S || card == Card.B) allCards.add(card);
            else for (int i = 0; i < 4; i++) allCards.add(card);
        }
        Collections.shuffle(allCards); // 先进行一次完全随机的洗牌，打乱基础顺序

        // 2. 创建三个玩家的临时手牌列表
        List<List<Card>> playerHands = new ArrayList<>();
        for (int i = 0; i < 3; i++) playerHands.add(new ArrayList<>());

        // 3. 制造并分配炸弹
        makeAndDealBombs(allCards, playerHands, lastWinnerId, new ArrayList<>(players.keySet()));

        // 4. 分配剩余的牌
        dealRemainingCards(allCards, playerHands);

        // 5. 轻微内部洗牌，让牌看起来更自然
        for (List<Card> hand : playerHands) {
            Collections.shuffle(hand);
        }

        // 6. 组合成最终牌堆
        List<Card> finalDeck = new ArrayList<>(54);
        finalDeck.addAll(playerHands.get(0));
        finalDeck.addAll(playerHands.get(1));
        finalDeck.addAll(playerHands.get(2));
        finalDeck.addAll(allCards); // allCards此时只剩下3张底牌

        return finalDeck;
    }

    private void makeAndDealBombs(List<Card> allCards, List<List<Card>> playerHands, String lastWinnerId, List<String> playerIds) {
        // 优先分配王炸或4个2给赢家
        if (lastWinnerId != null && random.nextInt(100) < 70) { // 70%的概率给赢家好牌
            int winnerHandIndex = playerIds.indexOf(lastWinnerId);
            if (winnerHandIndex != -1) {
                if (random.nextBoolean()) { // 一半概率王炸
                    playerHands.get(winnerHandIndex).add(Card.S);
                    playerHands.get(winnerHandIndex).add(Card.B);
                    allCards.remove(Card.S);
                    allCards.remove(Card.B);
                } else { // 一半概率4个2
                    playerHands.get(winnerHandIndex).addAll(findAndRemove(allCards, Card.C2, 4));
                }
            }
        }

        // 随机制造1到2个额外炸弹
        int extraBombs = 1 + random.nextInt(2);
        List<Card> potentialBombValues = Arrays.asList(Card.A, Card.K, Card.Q, Card.J, Card.T);
        Collections.shuffle(potentialBombValues);

        for (int i = 0; i < extraBombs; i++) {
            Card bombValue = potentialBombValues.get(i);
            List<Card> bombCards = findAndRemove(allCards, bombValue, 4);
            if (!bombCards.isEmpty()) {
                playerHands.get(random.nextInt(3)).addAll(bombCards); // 随机分配给一个玩家
            }
        }
    }

    private void dealRemainingCards(List<Card> allCards, List<List<Card>> playerHands) {
        for (int i = 0; i < 3; i++) {
            while (playerHands.get(i).size() < 17) {
                if(allCards.size() <= 3) break; // 保证剩下3张底牌
                playerHands.get(i).add(allCards.remove(0));
            }
        }
    }

    private List<Card> findAndRemove(List<Card> source, Card value, int count) {
        List<Card> found = source.stream().filter(c -> c == value).limit(count).collect(Collectors.toList());
        if (found.size() == count) {
            source.removeAll(found);
            return found;
        }
        return new ArrayList<>(); // 如果不够，则不移除
    }
}
