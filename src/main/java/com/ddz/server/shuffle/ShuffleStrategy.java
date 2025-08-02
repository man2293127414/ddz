package com.ddz.server.shuffle;

import com.ddz.common.Card;
import com.ddz.server.ClientHandler;

import java.util.List;
import java.util.Map;

public interface ShuffleStrategy {
    List<Card> shuffle(String lastWinnerId, Map<String, ClientHandler> players, List<Card> lastLandlordCards);
}
