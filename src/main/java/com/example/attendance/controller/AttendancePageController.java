package com.example.attendance.controller;

import com.example.attendance.dto.RegisterForm;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

/**
 * 页面跳转控制器：处理登录、注册和仪表盘页面渲染。
 */
@Controller
public class AttendancePageController {

    @Autowired
    private UserService userService;

    /**
     * 登录页。
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 注册页。
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 注册提交：校验与保存后跳转到登录页。
     */
    @PostMapping("/register")
    public String register(RegisterForm form, Model model) {
        try {
            userService.registerWithConfirm(form.username(), form.password(), form.confirmPassword());
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMsg", ex.getMessage());
            model.addAttribute("form", form);
            return "register";
        } catch (DataIntegrityViolationException ex) {
            // 注册失败兜底：处理数据库约束错误，避免直接 500。
            model.addAttribute("errorMsg", "注册失败：用户名已存在或数据不合法");
            model.addAttribute("form", form);
            return "register";
        } catch (Exception ex) {
            // 兜底异常：提供友好提示并保留表单输入。
            model.addAttribute("errorMsg", "注册失败，请稍后再试");
            model.addAttribute("form", form);
            return "register";
        }
    }

    /**
     * 系统首页/仪表盘。
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String name = principal == null ? "" : principal.getName();
        String message = name.isBlank() ? "欢迎进入系统" : "欢迎，" + name;
        model.addAttribute("welcomeMsg", message);
        return "dashboard";
    }
}
