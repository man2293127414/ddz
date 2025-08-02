package com.ddz.server.shuffle;

import com.ddz.common.Card;
import com.ddz.server.ClientHandler;

import java.util.*;
import java.util.stream.Collectors;

public class Passion2ShuffleStrategy implements ShuffleStrategy{
    private static final Random RANDOM = new Random();

    // 定义各类牌组
    private static final List<Card> POWER_CARDS = Arrays.asList(Card.C2, Card.S, Card.B, Card.A);
    private static final List<Card> HIGH_CARDS = Arrays.asList(Card.K, Card.Q, Card.J);
    private static final List<Card> MEDIUM_CARDS = Arrays.asList(Card.T, Card.C9, Card.C8);
    private static final List<Card> LOW_CARDS = Arrays.asList(Card.C7, Card.C6, Card.C5, Card.C4, Card.C3);

    @Override
    public List<Card> shuffle(String lastWinnerId, Map<String, ClientHandler> players, List<Card> lastLandlordCards) {
        System.out.println("执行策略：激情洗牌 - 保证爽感体验！");

        // 10%概率触发极限模式
        boolean extremeMode = RANDOM.nextInt(100) < 10;
        if (extremeMode) {
            System.out.println("🔥 触发极限模式！某位玩家将获得超强手牌！");
            return createExtremeDeck(players);
        }

        // 正常激情模式
        return createPassionDeck(players);
    }

    /**
     * 创建激情牌组 - 保证每人都有强牌
     */
    private List<Card> createPassionDeck(Map<String, ClientHandler> players) {
        List<Card> deck = createFullDeck();
        List<List<Card>> playerHands = initializePlayerHands();

        // 第一步：为每个玩家分配至少一个炸弹
        distributeBombs(deck, playerHands);

        // 第二步：分配大牌，确保每人都有强牌
        distributePowerCards(deck, playerHands);

        // 第三步：平衡手牌，确保有连子和对子的可能性
        balanceHands(deck, playerHands);

        // 第四步：填充剩余手牌
        fillRemainingCards(deck, playerHands);

        // 第五步：为底牌留一些好牌
        List<Card> landlordCards = createGoodLandlordCards(deck);

        // 组装最终牌组
        return assembleFinalDeck(playerHands, landlordCards, deck);
    }

    /**
     * 创建极限牌组 - 某个玩家获得超强手牌
     */
    private List<Card> createExtremeDeck(Map<String, ClientHandler> players) {
        List<Card> deck = createFullDeck();
        List<List<Card>> playerHands = initializePlayerHands();

        // 随机选择一个幸运玩家
        int luckyPlayer = RANDOM.nextInt(3);
        System.out.println("🚀 幸运玩家是：" + luckyPlayer + " 号位！");

        // 给幸运玩家分配超强手牌
        createSuperHand(deck, playerHands.get(luckyPlayer));

        // 其他玩家分配正常手牌
        for (int i = 0; i < 3; i++) {
            if (i != luckyPlayer) {
                distributeBombs(deck, Arrays.asList(playerHands.get(i)));
                distributePowerCards(deck, Arrays.asList(playerHands.get(i)));
            }
        }

        // 填充剩余手牌
        fillRemainingCards(deck, playerHands);

        List<Card> landlordCards = createGoodLandlordCards(deck);
        return assembleFinalDeck(playerHands, landlordCards, deck);
    }

    /**
     * 创建超强手牌
     */
    private void createSuperHand(List<Card> deck, List<Card> hand) {
        // 给2-3个炸弹
        int bombCount = 2 + RANDOM.nextInt(2);
        List<Card> availableBombCards = Arrays.asList(Card.C3, Card.C4, Card.C5, Card.C6, Card.C7,
                Card.C8, Card.C9, Card.T, Card.J, Card.Q, Card.K, Card.A);

        for (int i = 0; i < bombCount && !availableBombCards.isEmpty(); i++) {
            Card bombCard = availableBombCards.get(RANDOM.nextInt(availableBombCards.size()));
            availableBombCards = availableBombCards.stream()
                    .filter(c -> c != bombCard)
                    .collect(Collectors.toList());

            // 添加4张同样的牌作为炸弹
            for (int j = 0; j < 4; j++) {
                if (deck.remove(bombCard)) {
                    hand.add(bombCard);
                }
            }
        }

        // 添加王炸
        if (RANDOM.nextInt(100) < 70) { // 70%概率获得王炸
            if (deck.remove(Card.S)) hand.add(Card.S);
            if (deck.remove(Card.B)) hand.add(Card.B);
        }

        // 添加一些2和A
        addMultipleCards(deck, hand, Card.C2, 2 + RANDOM.nextInt(2));
        addMultipleCards(deck, hand, Card.A, 2 + RANDOM.nextInt(2));
    }

    /**
     * 分配炸弹给所有玩家
     */
    private void distributeBombs(List<Card> deck, List<List<Card>> playerHands) {
        List<Card> bombCandidates = Arrays.asList(Card.C3, Card.C4, Card.C5, Card.C6, Card.C7,
                Card.C8, Card.C9, Card.T, Card.J, Card.Q, Card.K);
        Collections.shuffle(bombCandidates);

        for (int i = 0; i < playerHands.size() && i < bombCandidates.size(); i++) {
            Card bombCard = bombCandidates.get(i);
            List<Card> hand = playerHands.get(i);

            // 每个玩家分配一个炸弹
            for (int j = 0; j < 4; j++) {
                if (deck.remove(bombCard)) {
                    hand.add(bombCard);
                }
            }
        }

        // 30%概率给某个玩家额外的炸弹
        if (RANDOM.nextInt(100) < 30) {
            int luckyPlayer = RANDOM.nextInt(playerHands.size());
            for (int i = 3; i < bombCandidates.size(); i++) {
                Card extraBomb = bombCandidates.get(i);
                if (Collections.frequency(deck, extraBomb) >= 4) {
                    for (int j = 0; j < 4; j++) {
                        if (deck.remove(extraBomb)) {
                            playerHands.get(luckyPlayer).add(extraBomb);
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * 分配大牌
     */
    private void distributePowerCards(List<Card> deck, List<List<Card>> playerHands) {
        // 平均分配2
        for (int i = 0; i < playerHands.size(); i++) {
            addMultipleCards(deck, playerHands.get(i), Card.C2, 1 + RANDOM.nextInt(2));
        }

        // 平均分配A
        for (int i = 0; i < playerHands.size(); i++) {
            addMultipleCards(deck, playerHands.get(i), Card.A, 1 + RANDOM.nextInt(2));
        }

        // 分配王牌
        List<Integer> kingDistribution = Arrays.asList(0, 1, 2);
        Collections.shuffle(kingDistribution);

        if (deck.remove(Card.S)) {
            playerHands.get(kingDistribution.get(0)).add(Card.S);
        }
        if (deck.remove(Card.B)) {
            playerHands.get(kingDistribution.get(1)).add(Card.B);
        }
    }

    /**
     * 平衡手牌，确保有连子可能性
     */
    private void balanceHands(List<Card> deck, List<List<Card>> playerHands) {
        // 为每个玩家创建一些连续牌的可能性
        List<Card> straightCards = Arrays.asList(Card.C3, Card.C4, Card.C5, Card.C6, Card.C7,
                Card.C8, Card.C9, Card.T, Card.J, Card.Q, Card.K, Card.A);

        for (List<Card> hand : playerHands) {
            // 随机选择一个起始点，给玩家一些连续的牌
            int startIdx = RANDOM.nextInt(Math.max(1, straightCards.size() - 5));
            int length = 3 + RANDOM.nextInt(3); // 3-5张连续牌

            for (int i = startIdx; i < Math.min(startIdx + length, straightCards.size()); i++) {
                Card card = straightCards.get(i);
                if (deck.contains(card)) {
                    deck.remove(card);
                    hand.add(card);

                    // 30%概率添加第二张相同的牌（形成对子）
                    if (RANDOM.nextInt(100) < 30 && deck.contains(card)) {
                        deck.remove(card);
                        hand.add(card);
                    }
                }
            }
        }
    }

    /**
     * 填充剩余手牌
     */
    private void fillRemainingCards(List<Card> deck, List<List<Card>> playerHands) {
        Collections.shuffle(deck);

        for (int i = 0; i < playerHands.size(); i++) {
            List<Card> hand = playerHands.get(i);

            // 每个玩家手牌补充到17张
            while (hand.size() < 17 && !deck.isEmpty()) {
                hand.add(deck.remove(0));
            }
        }
    }

    /**
     * 创建有价值的底牌
     */
    private List<Card> createGoodLandlordCards(List<Card> deck) {
        List<Card> landlordCards = new ArrayList<>();

        // 50%概率底牌包含大牌
        if (RANDOM.nextInt(100) < 50) {
            for (Card powerCard : POWER_CARDS) {
                if (deck.contains(powerCard) && landlordCards.size() < 2) {
                    deck.remove(powerCard);
                    landlordCards.add(powerCard);
                }
            }
        }

        // 填充到3张
        while (landlordCards.size() < 3 && !deck.isEmpty()) {
            landlordCards.add(deck.remove(0));
        }

        return landlordCards;
    }

    /**
     * 工具方法：创建完整牌组
     */
    private List<Card> createFullDeck() {
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
        return deck;
    }

    /**
     * 工具方法：初始化玩家手牌
     */
    private List<List<Card>> initializePlayerHands() {
        List<List<Card>> hands = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            hands.add(new ArrayList<>());
        }
        return hands;
    }

    /**
     * 工具方法：添加多张相同的牌
     */
    private void addMultipleCards(List<Card> deck, List<Card> hand, Card card, int count) {
        for (int i = 0; i < count; i++) {
            if (deck.remove(card)) {
                hand.add(card);
            }
        }
    }

    /**
     * 组装最终牌组
     */
    private List<Card> assembleFinalDeck(List<List<Card>> playerHands, List<Card> landlordCards, List<Card> remainingDeck) {
        List<Card> finalDeck = new ArrayList<>(54);

        // 添加玩家手牌 (0-16, 17-33, 34-50)
        for (List<Card> hand : playerHands) {
            Collections.shuffle(hand); // 打乱手牌顺序
            finalDeck.addAll(hand);
        }

        // 添加底牌 (51-53)
        finalDeck.addAll(landlordCards);

        return finalDeck;
    }
}
