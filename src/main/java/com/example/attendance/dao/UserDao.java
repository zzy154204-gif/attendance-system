package com.example.attendance.dao;

import com.example.attendance.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository  // 告诉 Spring：这是一个操作数据库的仓库类
public class UserDao {

    @Autowired // 核心：Spring 会自动把配置好的“数据库操作员” JdbcTemplate 借给你用
    private JdbcTemplate jdbcTemplate;

    /**
     * 保存用户（新增）
     */
    public void save(User user) {
        // 注意：PostgreSQL 建议给 user 表加双引号，防止关键词冲突
        String sql = "INSERT INTO \"user\" (username, password, real_name, role) VALUES (?, ?, ?, ?)";

        // 调用 update 方法执行 SQL，后面跟着的参数会按顺序填补上面的问号
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getRole());
    }
    // 1. 查詢所有
    public List<User> findAll() {
        String sql = "SELECT * FROM \"user\"";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    // 2. 根據 ID 查詢
    public User getById(Integer id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
    }

    // 3. 更新
    public void update(User user) {
        String sql = "UPDATE \"user\" SET real_name = ?, role = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getRealName(), user.getRole(), user.getId());
    }

    // 4. 刪除
    public void deleteById(Integer id) {
        String sql = "DELETE FROM \"user\" WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    public User findByUsername(String username) {
        String sql = "SELECT * FROM \"user\" WHERE username = ?";
        // 这里的 BeanPropertyRowMapper 会自动把数据库字段映射到 User 属性
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), username);}


}