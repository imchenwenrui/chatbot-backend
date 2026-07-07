
# 强制规则
- 类、方法、变量都要有完整的 javadoc 注释
- 方法的注释还需要有入参、出参、返回值的说明，注释中不要出现markdown语法和html语法
- 不要使用行尾注释
- 优先使用 Lombok
- 优先使用构造器注入
- 禁止使用字段注入 @Autowired
- ORM 使用 Mybatis-Plus
- 类都要在顶部import进来，不要直接用全限定名，除非有重名类，否则禁止直接这样使用，例：com.very.chatbot.entity.ConversationEntity
- 接口全部使用 POST 请求，不要使用除此之外的其他请求方式
---

# 包结构

统一使用：

controller  
service  
service.impl  
mapper  
entity  
dto  
vo  
convert  
config  
constant  
enums  
exception

---

# Controller 规范

- controller 仅负责参数接收与结果返回
- 不允许在 controller 写业务逻辑
- 不允许 controller 直接调用 mapper
- 接口参数的校验在DTO中处理，比如@NotNull、@NotEmpty等等

---

# DTO 规范

接口入参统一使用 DTO 结尾。

DTO 必须按接口功能拆分。

示例：

UserAddDTO  
UserUpdateDTO  
UserDeleteDTO  
UserPageDTO  
UserQueryDTO

禁止：

UserDTO  
CommonDTO

规则：

- 一个接口对应一个 DTO
- DTO 仅保留当前接口需要的字段
- 禁止大而全 DTO
- 禁止 DTO 直接继承实体类
- 禁止使用 Entity 接收前端参数

---

# VO 规范

接口返回统一使用 VO 结尾。

示例：

UserVO  
UserDetailVO  
UserPageVO

禁止：

- 返回 Entity
- 返回 Map
- 返回 Object

---

# Entity 规范

- Entity 仅用于数据库映射
- 禁止返回给前端
- 禁止作为接口入参
- Entity 必须保留无参构造

主键统一：

@TableId(type = IdType.ASSIGN_ID)

---

# MyBatis-Plus 规范

Mapper 要继承 BaseMapper

单表查询：

- 优先使用 LambdaQueryWrapper
- 优先使用 LambdaUpdateWrapper

复杂 SQL：

- 多表联查使用 XML
- 复杂统计使用 XML
- 超过 3 个表 JOIN 使用 XML

禁止：

- 使用 Wrapper 拼接超复杂 SQL
- Java 中拼接 SQL 字符串

---

# Service 规范

- 业务逻辑必须放在 Service
- Service 方法名称必须明确表达业务含义

推荐：

addUser  
updateUser  
deleteUser  
pageUser  
getDetail

禁止：

handle  
process  
execute

事务统一放在 Service 层：

@Transactional(rollbackFor = Exception.class)

---

# 日志规范

统一使用：

@Slf4j

要求：

- 核心业务打印日志
- 异常必须打印 error 日志

禁止：

System.out.println

---

# 异常规范

统一使用全局异常处理：

@RestControllerAdvice

业务异常统一使用：

BusinessException

禁止：

- RuntimeException 到处乱抛
- 吞异常

---

# 数据库规范

表名：

t_user  
t_order

字段：

create_time  
update_time  
deleted

禁止：

- 驼峰字段
- 单字符字段
- 拼音缩写

---
