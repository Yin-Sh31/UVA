package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.LoginDTO;
import com.cty.nopersonfinally.pojo.dto.RegisterDTO;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.vo.LoginResponseVO;
import com.cty.nopersonfinally.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器（登录/注册）
 */
@RestController
@RequestMapping({"/auth", "/api/auth"})
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户登录、注册相关接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 发送验证码接口
     */
    @PostMapping("/send-code")
    @Operation(summary = "发送验证码", description = "发送登录验证码到手机")
    public Result<?> sendCode(
            @Parameter(description = "手机号", example = "13800138000", required = true) @RequestParam String phone,
            @Parameter(description = "用户角色（farmer/flyer/owner）", example = "farmer", required = true) @RequestParam String role) {
        authService.sendCode(phone, role);
        return Result.ok("验证码已发送");
    }

    /**
     * 多身份登录接口
     * 支持农户、飞手、机主三种身份登录，并返回对应角色信息
     */
    @PostMapping(value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE )
    @Operation(summary = "用户登录", description = "支持管理员、农户、飞手、机主登录，支持密码登录和验证码登录（管理员仅支持密码登录，管理员使用username字段，其他用户使用phone字段）")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginDTO loginDTO) throws JsonProcessingException {
        LoginResponseVO loginVO = authService.login(loginDTO);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = objectMapper.writeValueAsString(Result.ok(loginVO));
            System.out.println("实际返回的响应体：" + responseBody);
        } catch (Exception e) {
            // 打印详细错误信息
            System.err.println("序列化响应体失败：");
            e.printStackTrace();
        }
        return Result.ok(loginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "支持农户、飞手、机主三种角色注册")
    public Result<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        System.out.println("接收到的注册数据：" + registerDTO);
        authService.register(registerDTO);
        return Result.ok("注册成功，请登录");
    }
}
