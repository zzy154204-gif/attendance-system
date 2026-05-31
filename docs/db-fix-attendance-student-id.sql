-- 目的：修复 attendance.student_id 列类型与 student.id 不一致导致的 Hibernate 启动告警/运行异常
-- 场景：student.id 是 bigint，但 attendance.student_id 目前是 varchar

BEGIN;

-- 如果历史数据是纯数字字符串，可直接转换为 bigint
ALTER TABLE attendance
    ALTER COLUMN student_id TYPE bigint
    USING NULLIF(trim(student_id), '')::bigint;

-- 先删除旧外键（如果存在）
ALTER TABLE attendance
    DROP CONSTRAINT IF EXISTS fknq6vm31it076obtjf2qp5coim;

-- 重新建立外键
ALTER TABLE attendance
    ADD CONSTRAINT fknq6vm31it076obtjf2qp5coim
    FOREIGN KEY (student_id) REFERENCES student(id);

COMMIT;

