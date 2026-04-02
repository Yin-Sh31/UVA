package com.cty.nopersonfinally.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cty.nopersonfinally.mapper.AdminUserMapper;
import com.cty.nopersonfinally.mapper.SysUserMapper;
import com.cty.nopersonfinally.mapper.UserFlyerMapper;
import com.cty.nopersonfinally.mapper.UserOwnerMapper;
import com.cty.nopersonfinally.pojo.dto.LoginDTO;
import com.cty.nopersonfinally.pojo.dto.RegisterDTO;
import com.cty.nopersonfinally.pojo.entity.AdminUser;
import com.cty.nopersonfinally.pojo.entity.SysUser;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import com.cty.nopersonfinally.pojo.enums.ResultCode;
import com.cty.nopersonfinally.pojo.vo.LoginResponseVO;
import com.cty.nopersonfinally.pojo.vo.LoginVO;
import com.cty.nopersonfinally.service.AuthService;
import com.cty.nopersonfinally.utils.BusinessException;
import com.cty.nopersonfinally.utils.JWTUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cty.nopersonfinally.pojo.entity.UserOwner;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    @Autowired
    private  SysUserMapper sysUserMapper;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private UserFlyerMapper userFlyerMapper;
    @Autowired
    private UserOwnerMapper userOwnerMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AdminUserMapper adminUserMapper;


    @Override
    public LoginResponseVO login(LoginDTO loginDTO) throws JsonProcessingException { 
        // 管理员登录处理
        if ("admin".equals(loginDTO.getRole())) {
            return loginAdmin(loginDTO);
        }
        
        // 普通用户登录处理
        // 1. 查询用户信息（支持手机号）
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, loginDTO.getPhone())
                .last("LIMIT 1")
        );
        // 2. 验证用户是否存在
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST.getMsg());
        }
        
        // 3. 根据登录方式进行验证
        if ("password".equals(loginDTO.getLoginType())) {
            // 密码登录
            if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
                throw new BusinessException("密码不能为空");
            }
            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
            }
        } else if ("code".equals(loginDTO.getLoginType())) {
            // 验证码登录
            if (loginDTO.getCode() == null || loginDTO.getCode().isEmpty()) {
                throw new BusinessException("验证码不能为空");
            }
            // 验证验证码
            validateCode(user, loginDTO.getCode());
        } else {
            throw new BusinessException("不支持的登录方式");
        }

        // 4. 验证飞手/机主审核状态（农户不需要审核）
        validateAuditStatus(user);

        // 5. 生成JWT令牌（包含用户ID和角色）
        String token = JWTUtil.generateToken(user.getUserId(), getRoleCode(user.getRoleType()));
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 6. 构建用户信息（与前端 UserInfo 字段对应）
        LoginResponseVO.UserInfo userInfo = new LoginResponseVO.UserInfo();
        userInfo.setId(user.getUserId()); // 对应前端的 id
        userInfo.setName(user.getRealName()); // 对应前端的 name
        userInfo.setPhone(user.getPhone());
        userInfo.setRole(getRoleCode(user.getRoleType())); // 确保返回小写角色（farmer/flyer/owner）
        userInfo.setRoleName(getRoleName(user.getRoleType()));

        // 补充审核状态（飞手/机主）
        if (user.getRoleType() != 1) {
            userInfo.setAuditStatus(user.getAuditStatus());
            userInfo.setAuditStatusDesc(getAuditStatusDesc(user.getAuditStatus()));
        }

        // 7. 构建最终响应对象（包含 token 和 user）
        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setUser(userInfo);
        System.out.println("用户登陆成功"+ response.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(response);
        System.out.println("登录响应JSON：" + responseJson); // 打印实际JSON结构
        System.out.println(response);
        return response;

    }
    
    /**
     * 管理员登录处理
     */
    private LoginResponseVO loginAdmin(LoginDTO loginDTO) {
        // 只支持密码登录
        if (!"password".equals(loginDTO.getLoginType())) {
            throw new BusinessException("管理员只支持密码登录");
        }
        
        // 验证用户名和密码是否为空
        if (loginDTO.getUsername() == null || loginDTO.getUsername().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        
        // 1. 使用用户名查询管理员用户
        AdminUser admin = adminUserMapper.selectByUsername(loginDTO.getUsername());
        
        // 2. 验证管理员是否存在
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        
        // 3. 验证管理员状态
        if (admin.getStatus() != 1) {
            throw new BusinessException("管理员账号已禁用");
        }
        
        // 4. 验证密码
        if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        
        // 检查密码是否正确（支持明文密码和加密密码）
        boolean passwordMatches = false;
        System.out.println("[管理员登录] 开始验证密码，输入密码: " + loginDTO.getPassword());
        System.out.println("[管理员登录] 数据库中存储的密码: " + admin.getPassword());
        
        if (admin.getPassword().startsWith("$2a$")) {
            // BCrypt加密密码
            System.out.println("[管理员登录] 检测到BCrypt加密密码，开始验证");
            passwordMatches = passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword());
            System.out.println("[管理员登录] BCrypt密码验证结果: " + passwordMatches);
        } else {
            // 明文密码（仅用于初始化）
            System.out.println("[管理员登录] 检测到明文密码，开始验证");
            passwordMatches = loginDTO.getPassword().equals(admin.getPassword());
            System.out.println("[管理员登录] 明文密码验证结果: " + passwordMatches);
        }
        
        if (!passwordMatches) {
            System.out.println("[管理员登录] 密码验证失败");
            throw new BusinessException("密码错误");
        }
        System.out.println("[管理员登录] 密码验证成功");
        
        // 5. 生成JWT令牌（包含管理员ID和角色）
        String token = JWTUtil.generateToken(admin.getId(), "admin");
        
        // 6. 将token保存到Redis，设置过期时间
        String redisKey = "token:" + admin.getId() + ":admin";
        redisTemplate.opsForValue().set(redisKey, token, 5, TimeUnit.MINUTES);
        System.out.println("管理员token已保存到Redis，key: " + redisKey);
        
        // 7. 更新最后登录时间
        admin.setLastLoginTime(LocalDateTime.now());
        adminUserMapper.updateById(admin);
        
        // 7. 构建管理员信息
        LoginResponseVO.UserInfo adminInfo = new LoginResponseVO.UserInfo();
        adminInfo.setId(admin.getId());
        adminInfo.setName(admin.getUsername());
        adminInfo.setPhone(admin.getPhone());
        adminInfo.setRole("admin");
        adminInfo.setRoleName("管理员");
        
        // 8. 构建响应对象
        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setUser(adminInfo);
        
        System.out.println("管理员登录成功: " + admin.getUsername());
        return response;
    }
    /**
     * 验证飞手/机主的审核状态
     * 未通过审核的用户不能登录系统
     */
    private void validateAuditStatus(SysUser user) {
        // 角色类型：1-农户（无需审核），2-飞手，3-机主
        if (user.getRoleType() == 2 || user.getRoleType() == 3) {
            // 审核状态：0-待审核，1-通过，2-拒绝
            // 临时修改：允许待审核状态的飞手登录
            if (user.getAuditStatus() == 0) {
                System.out.println("用户处于待审核状态，已允许登录");
                // 不抛出异常，允许登录
                return;
            }
            if (user.getAuditStatus() == 2) {
                System.out.println("你的资质审核未通过，无法登录");
                throw new BusinessException("您的资质审核未通过，无法登录");
            }
        }
    }

    /**
     * 将角色类型转换为角色编码（用于JWT和前端权限控制）
     */
    private String getRoleCode(Integer roleType) {
        return switch (roleType) {
            case 1 -> "farmer";
            case 2 -> "flyer";
            case 3 -> "owner";
            default -> throw new BusinessException("无效的角色类型");
        };
    }

    /**
     * 获取角色名称（用于前端展示）
     */
    private String getRoleName(Integer roleType) {
        return switch (roleType) {
            case 1 -> "农户";
            case 2 -> "飞手";
            case 3 -> "机主";
            default -> "未知角色";
        };
    }

    /**
     * 获取审核状态描述
     */
    private String getAuditStatusDesc(Integer auditStatus) {
        return switch (auditStatus) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "未知状态";
        };
    }
    
    /**
     * 发送验证码
     */
    @Override
    public void sendCode(String phone, String role) {
        // 管理员不支持验证码登录
        if ("admin".equals(role)) {
            throw new BusinessException("管理员不支持验证码登录");
        }
        
        // 1. 验证手机号格式（可以在这里添加更严格的手机号验证）
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        
        // 2. 查询用户是否存在
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone)
                .last("LIMIT 1")
        );
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 3. 验证用户角色
        Integer expectedRoleType = switch (role) {
            case "farmer" -> 1;
            case "flyer" -> 2;
            case "owner" -> 3;
            default -> throw new BusinessException("暂不支持该角色的验证码登录");
        };
        
        if (!expectedRoleType.equals(user.getRoleType())) {
            throw new BusinessException("用户角色不匹配");
        }
        
        // 4. 生成6位随机验证码
        String code = generateRandomCode();
        
        // 5. 根据角色生成Redis key
        String redisKey;
        if ("farmer".equals(role)) {
            redisKey = "farmerId:" + user.getUserId() + ":code";
        } else if ("flyer".equals(role)) {
            redisKey = "flyerId:" + user.getUserId() + ":code";
        } else {
            redisKey = "ownerId:" + user.getUserId() + ":code";
        }
        
        // 6. 存储验证码到Redis，设置1分钟过期
        redisTemplate.opsForValue().set(redisKey, code, 60, TimeUnit.SECONDS);
        
        // 7. 模拟发送验证码（打印到控制台）
        System.out.println("==== 验证码发送 ====");
        System.out.println("手机号: " + phone);
        System.out.println("用户ID: " + user.getUserId());
        System.out.println("角色: " + role);
        System.out.println("验证码: " + code);
        System.out.println("Redis Key: " + redisKey);
        System.out.println("有效期: 5分钟");
        System.out.println("=================");
    }
    
    /**
     * 生成6位随机验证码
     */
    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 验证验证码
     */
    private void validateCode(SysUser user, String inputCode) {
        String redisKey;
        if (user.getRoleType() == 1) { // 农户
            redisKey = "farmerId:" + user.getUserId() + ":code";
        } else if (user.getRoleType() == 2) { // 飞手
            redisKey = "flyerId:" + user.getUserId() + ":code";
        } else if (user.getRoleType() == 3) { // 机主
            redisKey = "ownerId:" + user.getUserId() + ":code";
        } else {
            throw new BusinessException("该角色不支持验证码登录");
        }
        
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        if (storedCode == null) {
            throw new BusinessException("验证码已过期或不存在");
        }
        
        if (!inputCode.equals(storedCode)) {
            throw new BusinessException("验证码错误");
        }
        
        // 验证码验证成功后删除，防止重复使用
        redisTemplate.delete(redisKey);
    }


    @Override
//    @Transactional
    public void register(RegisterDTO registerDTO) {
        // 1. 检查用户是否已存在（用户名/手机号）
        SysUser existUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, registerDTO.getPhone())
                .last("LIMIT 1")
        );
        if (existUser != null) {
            throw new BusinessException("用户名或手机号已被注册");
        }

        // 2. 转换角色类型（字符串转数字编码）
        Integer roleType = switch (registerDTO.getRole()) {
            case "farmer" -> 1;
            case "flyer" -> 2;
            case "owner" -> 3;
            default -> throw new BusinessException("无效的角色类型");
        };

        // 3. 创建用户实体
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 密码加密
        user.setPhone(registerDTO.getPhone());
        user.setRealName(registerDTO.getRealName());
        user.setRoleType(roleType);
        user.setCreateTime(LocalDateTime.now());
        user.setStatus(1); // 账号状态：1-正常

        // 4. 设置审核状态（农户无需审核，飞手/机主默认待审核）
        if (roleType == 1) { // 农户
            user.setAuditStatus(1); // 直接通过
        } else { // 飞手/机主
            user.setAuditStatus(0); // 待审核
        }

        // 5. 保存用户
        sysUserMapper.insert(user);

        // 6. 为飞手/机主创建对应角色表记录
        if (roleType == 2) { // 飞手
            UserFlyer flyer = new UserFlyer();
            flyer.setUserId(user.getUserId());
            flyer.setAuditStatus(0); // 待审核
            userFlyerMapper.insert(flyer);
        } else if (roleType == 3) { // 机主
            UserOwner owner = new UserOwner();
            owner.setUserId(user.getUserId());
            owner.setAuditStatus(0); // 待审核
            userOwnerMapper.insert(owner);
        }
    }
}
