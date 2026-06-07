package com.example.attendance.controller;

import com.example.attendance.dao.OperationLogRepository;
import com.example.attendance.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 操作日志查看页面控制器。
 */
@Controller
@RequestMapping("/admin/logs")
public class OperationLogPageController {

    private final OperationLogRepository operationLogRepository;

    public OperationLogPageController(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    /**
     * 操作日志列表页（管理员专属）。
     */
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size,
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<OperationLog> logPage;

        if (username != null && !username.isBlank()) {
            logPage = operationLogRepository.findByUsernameOrderByCreateTimeDesc(username.trim(), pageable);
        } else {
            logPage = operationLogRepository.findAllByOrderByCreateTimeDesc(pageable);
        }

        model.addAttribute("logs", logPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("username", username == null ? "" : username);
        return "operation-log";
    }
}
