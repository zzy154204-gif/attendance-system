package com.example.attendance;

import com.example.attendance.dao.UserDao;
import com.example.attendance.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class AttendanceSystemApplicationTests {

    @Autowired
    private UserDao userDao; // 把你写的 UserDao 借过来用

    @Test
    void testInsert() {
        // 1. 创建一个用户对象（当前 User 使用 JPA 实体结构）
        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        User user = new User();
        user.setUsername("zy_student_" + suffix);
        user.setPassword("123456");
        user.setRealName("张同学");
        user.setRole("STUDENT");

        // 2. 调用你写的 save 方法
        userDao.save(user);

        System.out.println("恭喜！代码运行到这里没报错，说明数据已经发出去了！");
    }
    @Test
    void testQuery() {
        // 1. 调用你刚写的查询方法
        User user = userDao.findByUsername("zy_student");

        // 2. 打印结果看看
        if (user != null) {
            System.out.println("查询成功！真实姓名是：" + user.getRealName());
            System.out.println("他的角色是：" + user.getRole());
        } else {
            System.out.println("没找到这个用户哦！");
        }
    }
}