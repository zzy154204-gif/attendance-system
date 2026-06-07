package com.example.attendance.aspect;

import com.example.attendance.dao.OperationLogRepository;
import com.example.attendance.entity.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.security.Principal;
import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面。
 * <p>拦截带有 @LogOperation 注解的方法，自动写入操作日志。</p>
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogRepository operationLogRepository;

    public OperationLogAspect(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @Pointcut("@annotation(com.example.attendance.aspect.LogOperation)")
    public void logPointcut() {
    }

    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        saveLog(joinPoint, true, null);
    }

    @AfterThrowing(pointcut = "logPointcut()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Exception ex) {
        saveLog(joinPoint, false, ex.getMessage());
    }

    private void saveLog(JoinPoint joinPoint, boolean success, String errorMsg) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogOperation annotation = method.getAnnotation(LogOperation.class);
            if (annotation == null) return;

            // 获取当前用户
            String username = "anonymous";
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof Principal principal) {
                    username = principal.getName();
                    break;
                }
            }

            // 获取请求 IP
            String ip = "unknown";
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ip = getClientIp(request);
            }

            // 构建详情
            String detail = annotation.detail();
            if (detail == null || detail.isBlank()) {
                detail = method.getDeclaringClass().getSimpleName() + "." + method.getName();
            }
            if (errorMsg != null) {
                detail = detail + " [异常: " + errorMsg + "]";
            }

            OperationLog operationLog = new OperationLog();
            operationLog.setUsername(username);
            operationLog.setOperation(annotation.operation());
            operationLog.setTarget(annotation.target());
            operationLog.setTargetId(null);
            operationLog.setDetail(detail);
            operationLog.setIp(ip);
            operationLog.setCreateTime(LocalDateTime.now());
            operationLog.setSuccess(success);

            operationLogRepository.save(operationLog);
        } catch (Exception e) {
            log.warn("记录操作日志失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
