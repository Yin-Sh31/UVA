package com.cty.nopersonfinally.service;

import com.cty.nopersonfinally.pojo.entity.Conversation;

public interface ConversationService {

    Conversation createConversation(String userId1, String userId2);

    Conversation getConversation(String userId1, String userId2);
}