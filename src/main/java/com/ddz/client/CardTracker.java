package com.ddz.client;

import com.ddz.common.Card;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端记牌器类（纯客户端对称信息版）
 * 通过追踪所有已打出的牌来计算全局剩余牌。
 * 所有客户端的计算结果将完全一致。
 */
public class CardTracker {

    private boolean isActive = false;
    private final Map<Card, Integer> initialDeck;
    private final List<Card> allPlayedCards;

    public CardTracker() {
        this.initialDeck = new EnumMap<>(Card.class);
        this.allPlayedCards = new ArrayList<>();
        // 预先填充完整的牌堆，作为计算基准
        for (Card card : Card.values()) {
            if (card == Card.S || card == Card.B) {
                initialDeck.put(card, 1);
            } else {
                initialDeck.put(card, 4);
            }
        }
    }

    /**
     * 激活记牌器。
     * 在地主确认后调用，表示可以开始追踪出牌。
     */
    public void activate() {
        this.isActive = true;
        this.allPlayedCards.clear(); // 清空上一局的出牌记录
        System.out.println("\n[提示] 对称信息记牌器已激活。输入 'tracker' 查看剩余牌。");
    }

    /**
     * 记录每一手打出的牌。
     * @param playedCards 最新打出的一手牌。
     */
    public void recordPlay(List<Card> playedCards) {
        if (!isActive || playedCards == null) {
            return;
        }
        this.allPlayedCards.addAll(playedCards);
    }

    /**
     * 动态计算并获取当前全局剩余牌。
     * 算法: 54张总牌 - 所有已打出的牌
     * @return 一个包含每种牌及其剩余数量的Map。
     */
    public Map<Card, Integer> getRemainingCards() {
        if (!isActive) {
            return new EnumMap<>(Card.class);
        }
        Map<Card, Integer> remaining = new EnumMap<>(this.initialDeck);
        for (Card card : this.allPlayedCards) {
            remaining.merge(card, -1, Integer::sum);
        }
        return remaining;
    }

    /**
     * 重置记牌器状态，为新一局做准备。
     */
    public void reset() {
        this.isActive = false;
        this.allPlayedCards.clear();
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * 新增方法：用于显示记牌器内容
     * @param remainingCards 从 CardTracker 获取的剩余牌数据
     */
    public void displayCardTracker(Map<Card, Integer> remainingCards) {
        System.out.println("\n============== 记牌器 (场上剩余牌) ==============");

        List<Card> sortedKeys = new ArrayList<>(remainingCards.keySet());
        sortedKeys.sort(Card.cardComparator()); // 按 B, S, 2, A, K... 顺序排序

        StringBuilder sb = new StringBuilder();
        for (Card card : sortedKeys) {
            int count = remainingCards.getOrDefault(card, 0);
            if (count > 0) {
                sb.append(card.getDisplay()).append(":").append(count).append("   ");
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 3); // 去掉最后的空格
        }

        System.out.println(sb.toString());
        System.out.println("==============================================");
    }
}