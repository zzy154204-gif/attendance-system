package com.example.attendance.entity;

public class User {
    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String role;

    // 1. 无参构造（Spring 必须要求有这个，哪怕你不用它）
    public User() {}

    // 2. 有参构造（方便你写测试代码时一行搞定：new User("admin", "123"...)）
    public User(String username, String password, String realName, String role) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
    }

    // 3. Getter 和 Setter（最关键，JdbcTemplate 靠它们读写数据）
    // 快捷键技巧：按 Alt + Insert，选 Getter and Setter，然后 Ctrl+A 全选，回车！

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


}