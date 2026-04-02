package com.cty.nopersonfinally.service;

import com.cty.nopersonfinally.pojo.dto.LoginDTO;
import com.cty.nopersonfinally.pojo.dto.RegisterDTO;
import com.cty.nopersonfinally.pojo.vo.LoginResponseVO;
import com.cty.nopersonfinally.pojo.vo.LoginVO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface AuthService {

    // 新增注册方法
    void register(RegisterDTO registerDTO);

    LoginResponseVO login(LoginDTO loginDTO) throws JsonProcessingException;
    
    // 发送验证码
    void sendCode(String phone, String role);
}
