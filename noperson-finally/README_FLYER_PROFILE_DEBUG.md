# 飞手个人资料401未授权问题排查指南

## 问题概述
前端在获取飞手个人资料数据时返回 `code: 401, message: '未授权访问'` 错误，导致数据无法正常展示。

## 已完成的排查工作

### 1. 增强日志记录

#### JWTUtil.java
- 在 `getUserIdFromToken()` 方法中添加了详细日志，记录token解析过程和结果
- 在 `getRoleFromToken()` 方法中添加了类似的详细日志
- 在 `isTokenValid()` 方法中添加了详细的验证过程和结果日志

#### JwtAuthenticationFilter.java
- 添加了对请求URI和Authorization头的详细日志记录
- 改进了token提取逻辑，添加了安全性检查
- 记录了JWT验证的完整流程和结果

#### FlyerController.java
- 重写了 `getFlyerInfo()` 方法，添加了对Bearer前缀的处理和更详细的日志
- 增加了备用验证方案，提高了系统的健壮性

#### 日志配置优化
- 修复了错误的包路径 `com.hmdp`（非项目包）
- 提高了项目日志级别为 `trace`，确保所有调试信息都能被记录
- 添加了文件日志配置，便于离线分析问题

### 2. 代码优化
- 修复了潜在的空指针异常和索引越界问题
- 增强了token验证的健壮性和错误处理
- 确保了在各种异常情况下都能提供清晰的日志信息

## 排查建议

### 启动项目并收集日志
```bash
# 启动项目
mvn spring-boot:run

# 查看控制台输出日志（推荐使用命令行工具，如PowerShell）
# 或者查看日志文件
type logs/noperson-app.log
```

### 关键日志信息
请特别关注以下格式的日志信息，它们将帮助定位问题：

- `[JwtAuthFilter] 请求URI: xxx, Authorization头是否存在: xxx`
- `[JwtAuthFilter] 成功从token中提取userId: xxx`
- `[JWTUtil] 开始解析token获取userId，token是否为空: xxx`
- `[JWTUtil] 成功解析token，subject: xxx`
- `[FlyerController] 收到获取飞手个人信息请求`
- `[FlyerController] 原始Authorization头: xxx`

### 常见问题排查

1. **Authorization头问题**
   - 确保前端正确发送了Authorization头
   - 检查Authorization头格式是否包含`Bearer `前缀
   - 检查token是否为空或格式错误

2. **token解析问题**
   - 检查token是否过期
   - 检查token是否被篡改
   - 检查JWT密钥配置是否正确

3. **用户角色问题**
   - 确保用户角色（flyer）被正确存储在token中
   - 检查角色提取是否成功

## 下一步建议

1. 启动项目并尝试重现问题
2. 收集并分析日志，特别是上面提到的关键日志信息
3. 检查前端请求头的格式和内容是否正确
4. 如果问题仍然存在，可以考虑进一步添加更多的调试信息

通过这些详细的日志和排查步骤，应该能够定位并解决导致401未授权错误的具体原因。