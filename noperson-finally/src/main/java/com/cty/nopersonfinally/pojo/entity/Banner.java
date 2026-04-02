package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
@TableName("banner")
public class Banner {
    
    @TableId(type = IdType.AUTO)
    private Long id;            // 轮播图ID（主键）
    
    private String title;       // 轮播图标题
    
    private String imageUrl;    // 轮播图图片URL
    
    private String type;        // 用户类型：farmer(农户) 或 flyer(飞手)
    
    private Integer sort;       // 排序，数字越小越靠前
    
    private Integer status;     // 状态：0-禁用 1-启用
    
    private LocalDateTime createTime;  // 创建时间
    
    private LocalDateTime updateTime;  // 更新时间


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}