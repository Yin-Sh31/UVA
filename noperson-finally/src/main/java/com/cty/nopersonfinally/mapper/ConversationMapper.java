package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    @Select("SELECT * FROM conversation WHERE (user1_id = #{userId1} AND user2_id = #{userId2}) OR (user1_id = #{userId2} AND user2_id = #{userId1}) LIMIT 1")
    Conversation findByUsers(String userId1, String userId2);
}