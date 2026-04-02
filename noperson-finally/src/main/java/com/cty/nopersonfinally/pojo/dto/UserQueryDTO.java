// UserQueryDTO.java - 用户查询参数
package com.cty.nopersonfinally.pojo.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private int pageNum = 1;
    private int pageSize = 10;
    private Integer roleType; // 1-农户 2-飞手 3-机主
    private String keyword; // 搜索关键词（用户名/手机号）
    private Integer status; // 用户状态


    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

