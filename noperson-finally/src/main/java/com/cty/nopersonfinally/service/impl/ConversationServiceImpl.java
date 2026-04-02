package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cty.nopersonfinally.mapper.ConversationMapper;
import com.cty.nopersonfinally.pojo.entity.Conversation;
import com.cty.nopersonfinally.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Override
    public Conversation createConversation(String userId1, String userId2) {
        Conversation existingConversation = getConversation(userId1, userId2);
        if (existingConversation != null) {
            return existingConversation;
        }

        Conversation conversation = new Conversation();
        conversation.setUser1Id(userId1);
        conversation.setUser2Id(userId2);
        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public Conversation getConversation(String userId1, String userId2) {
        return conversationMapper.findByUsers(userId1, userId2);
    }
}