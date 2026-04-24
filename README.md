# 西南财经大学 Java EE 开发实践：课堂考勤系统

### 个人基本信息
* **姓名**：张宗元
* **学号**：42411134
* **专业**：计算机科学与技术
* **任务阶段**：完成第四周分层架构任务
*  完成第六周JDBC-template开发

---

## 项目现状（已完成功能）

| 模块 | 内容 |
|---|---|
| 实体层 | `User`、`Student`、`Attendance` 三张表，JPA 自动建表 |
| 数据访问层 | `UserDao`、`StudentDao`（JpaRepository）、`AttendanceRepository`（含自定义查询） |
| 服务层 | `UserService`/`UserServiceImpl`、`StudentService`/`StudentServiceImpl`、`AttendanceService`/`AttendanceServiceImpl` |
| 接口层 | 学生 CRUD、用户 CRUD、**考勤 CRUD + 真实签到写库**、**登录接口** |
| 统一响应 | `Result<T>` 泛型封装（code / message / data） |

---

## 当前可用 API 列表

### 用户模块 `/user`
| 方法 | 路径 | 描述 |
|---|---|---|
| POST | `/user/login` | 登录（返回用户信息） |
| GET | `/user/all` | 查询所有用户 |
| GET | `/user/id/{id}` | 按 ID 查用户 |
| GET | `/user/info/{username}` | 按用户名查用户 |
| PUT | `/user/update` | 更新用户 |
| DELETE | `/user/delete/{id}` | 删除用户 |

### 学生模块 `/students`
| 方法 | 路径 | 描述 |
|---|---|---|
| POST | `/students` | 新增学生 |
| GET | `/students` | 查询所有学生 |
| GET | `/students/{id}` | 按 ID 查学生 |
| PUT | `/students/{id}` | 更新学生信息 |
| DELETE | `/students/{id}` | 删除学生 |

### 考勤模块 `/attendance`
| 方法 | 路径 | 描述 |
|---|---|---|
| POST | `/attendance/checkin` | 签到（写入数据库） |
| GET | `/attendance/all` | 查询所有考勤记录 |
| GET | `/attendance/student/{studentId}` | 查询某学生所有记录 |
| GET | `/attendance/status/{status}` | 按状态筛选（正常/迟到/缺勤） |
| DELETE | `/attendance/{id}` | 删除一条考勤记录 |
| POST | `/student/attendance` | 兼容旧路径签到 |

---

## 下一步方向与开发计划

### 第一优先级：安全认证
- [ ] 引入 **Spring Security** 或 JWT，实现 Token 登录，保护需要鉴权的接口
- [ ] 密码改用 **BCrypt** 加密存储，不再明文保存

### 第二优先级：课程管理
- [ ] 新增 `Course` 实体（课程名、教师、时间段）
- [ ] 建立 `Student ↔ Course` 多对多关系表（选课）
- [ ] 考勤记录关联具体课程（`Attendance` 增加 `course` 字段）

### 第三优先级：教师端功能
- [ ] 新增 `Teacher` 实体及 CRUD 接口
- [ ] 教师可以发起一次考勤（创建考勤任务），学生在时间窗口内签到
- [ ] 教师查看班级出勤报表

### 第四优先级：统计与报表
- [ ] 统计某学生的出勤率（正常 / 总次数）
- [ ] 统计某课程的全班出勤情况
- [ ] 新增 `/attendance/stats/student/{id}` 和 `/attendance/stats/course/{id}` 接口

### 第五优先级：前端页面
- [ ] 用 **Vue 3 + Element Plus**（或 React）搭建管理后台
- [ ] 学生端：查看个人考勤、一键签到
- [ ] 教师端：发起考勤、查看报表
- [ ] 管理员端：学生/课程/用户管理

### 第六优先级：工程质量
- [ ] 统一异常处理：使用 `@ControllerAdvice` + `@ExceptionHandler` 捕获所有异常，返回统一 `Result` 格式
- [ ] 参数校验：`@NotBlank`、`@Size` 等注解配合 `spring-boot-starter-validation` 使用
- [ ] 分页查询：`StudentController` 和 `AttendanceController` 支持 `page` / `size` 参数
- [ ] Docker + docker-compose 一键部署
- [ ] 单元测试覆盖 Service 层，集成测试覆盖 Controller 层

---

## 快速启动

```bash
# 1. 确保本地 PostgreSQL 已启动，创建数据库
createdb attendance_system

# 2. 修改 src/main/resources/application.yml 中的数据库密码

# 3. 启动项目
./mvnw spring-boot:run
```

项目启动后访问 `http://localhost:8080/`

