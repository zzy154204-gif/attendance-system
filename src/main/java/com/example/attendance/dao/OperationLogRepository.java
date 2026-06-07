package com.example.attendance.dao;

import com.example.attendance.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    /** 按操作类型查询 */
    List<OperationLog> findByOperation(String operation);

    /** 按用户名查询（分页） */
    Page<OperationLog> findByUsernameOrderByCreateTimeDesc(String username, Pageable pageable);

    /** 按时间范围查询（分页） */
    Page<OperationLog> findByCreateTimeBetweenOrderByCreateTimeDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    /** 全部按时间倒序查询（分页） */
    Page<OperationLog> findAllByOrderByCreateTimeDesc(Pageable pageable);
}
