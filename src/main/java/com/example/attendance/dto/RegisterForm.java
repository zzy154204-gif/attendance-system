package com.example.attendance.dto;

/**
 * 注册页面表单对象（普通 JavaBean，确保 Spring MVC 表单绑定可靠）。
 */
public class RegisterForm {

    private String username;
    private String password;
    private String confirmPassword;
    private String role;

    public RegisterForm() {
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
