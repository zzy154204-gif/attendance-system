-- ============================================
-- 考勤系统模拟数据脚本
-- 使用方法：复制到 PostgreSQL 查询工具中执行
-- ============================================

-- 1. 插入课程数据（配合 Course 实体，支持数据库管理课程）
INSERT INTO course (name, code, teacher_name, classroom, start_time, end_time, week_day, semester) VALUES
('Java程序设计', 'CS101', '张老师', 'A101', '08:00', '10:00', 'MONDAY', 1),
('数据库原理',   'CS201', '李老师', 'B202', '10:00', '12:00', 'TUESDAY', 1),
('Java EE开发',  'CS301', '王老师', 'C303', '14:00', '16:00', 'WEDNESDAY', 1)
ON CONFLICT (code) DO NOTHING;

-- 2. 插入 20 名学生
INSERT INTO student (student_number, name, clazz, gender, birth_date, contact) VALUES
('20240001', '张三',   '软件工程1班', '男', '2005-03-15', '13800001001'),
('20240002', '李四',   '软件工程1班', '女', '2005-07-22', '13800001002'),
('20240003', '王五',   '软件工程1班', '男', '2005-01-10', '13800001003'),
('20240004', '赵六',   '软件工程2班', '女', '2004-11-28', '13800001004'),
('20240005', '钱七',   '软件工程2班', '男', '2005-05-06', '13800001005'),
('20240006', '孙八',   '软件工程2班', '女', '2005-09-14', '13800001006'),
('20240007', '周九',   '计算机科学1班', '男', '2004-12-03', '13800001007'),
('20240008', '吴十',   '计算机科学1班', '女', '2005-04-19', '13800001008'),
('20240009', '郑十一', '计算机科学1班', '男', '2005-08-25', '13800001009'),
('20240010', '冯十二', '计算机科学2班', '女', '2005-02-14', '13800001010'),
('20240011', '陈十三', '计算机科学2班', '男', '2004-10-30', '13800001011'),
('20240012', '褚十四', '计算机科学2班', '女', '2005-06-08', '13800001012'),
('20240013', '卫十五', '软件工程1班', '男', '2005-01-22', '13800001013'),
('20240014', '蒋十六', '软件工程2班', '女', '2004-09-05', '13800001014'),
('20240015', '沈十七', '计算机科学1班', '男', '2005-07-17', '13800001015'),
('20240016', '韩十八', '计算机科学2班', '女', '2005-03-30', '13800001016'),
('20240017', '杨十九', '软件工程1班', '男', '2004-08-12', '13800001017'),
('20240018', '朱二十', '软件工程2班', '女', '2005-05-29', '13800001018'),
('20240019', '秦廿一', '计算机科学1班', '男', '2005-11-01', '13800001019'),
('20240020', '尤廿二', '计算机科学2班', '女', '2004-12-25', '13800001020')
ON CONFLICT (student_number) DO NOTHING;

-- 3. 为过去 15 天生成考勤记录（每天随机出勤）
--    用 DO 块批量生成，模拟真实上课场景
DO $$
DECLARE
    stu RECORD;
    course_id INT;
    course_name TEXT;
    course_start TIME;
    check_date DATE;
    check_time TIMESTAMP;
    att_status TEXT;
BEGIN
    FOR stu IN SELECT id, student_number FROM student LOOP
        -- 过去 15 天，每个工作日
        FOR check_date IN
            SELECT generate_series(
                CURRENT_DATE - INTERVAL '15 days',
                CURRENT_DATE,
                '1 day'
            )::DATE
        LOOP
            -- 跳过周末
            IF EXTRACT(DOW FROM check_date) IN (0, 6) THEN
                CONTINUE;
            END IF;

            -- 课程 1: Java程序设计 08:00
            IF random() > 0.15 THEN  -- 85% 出勤概率
                course_start := '08:00'::TIME;
                check_time := check_date + course_start
                    + (random() * INTERVAL '20 minutes')  -- 7:50-8:10 随机到达
                    - INTERVAL '10 minutes';
                att_status := CASE
                    WHEN check_time::TIME > '08:00'::TIME THEN 'LATE'
                    ELSE 'NORMAL'
                END;
                INSERT INTO attendance (check_in_time, status, course_id, course_name, student_id, remark, create_time)
                VALUES (check_time, att_status, 1, 'Java程序设计', stu.id,
                        CASE WHEN att_status = 'LATE' THEN '路上堵车' ELSE NULL END,
                        check_time)
                ON CONFLICT DO NOTHING;
            ELSE
                -- 偶尔缺勤
                INSERT INTO attendance (check_in_time, status, course_id, course_name, student_id, remark, create_time)
                VALUES (check_date + '08:05'::TIME, 'ABSENT', 1, 'Java程序设计', stu.id, '身体不适请假', check_date + '08:05'::TIME)
                ON CONFLICT DO NOTHING;
            END IF;

            -- 课程 2: 数据库原理 10:00
            IF random() > 0.12 THEN  -- 88% 出勤概率
                course_start := '10:00'::TIME;
                check_time := check_date + course_start
                    + (random() * INTERVAL '25 minutes')
                    - INTERVAL '12 minutes';
                att_status := CASE
                    WHEN check_time::TIME > '10:00'::TIME THEN 'LATE'
                    ELSE 'NORMAL'
                END;
                INSERT INTO attendance (check_in_time, status, course_id, course_name, student_id, remark, create_time)
                VALUES (check_time, att_status, 2, '数据库原理', stu.id,
                        NULL, check_time)
                ON CONFLICT DO NOTHING;
            END IF;

            -- 课程 3: Java EE开发 14:00 (70% 概率出勤，下午容易翘课)
            IF random() > 0.30 THEN
                course_start := '14:00'::TIME;
                check_time := check_date + course_start
                    + (random() * INTERVAL '20 minutes')
                    - INTERVAL '10 minutes';
                att_status := CASE
                    WHEN check_time::TIME > '14:00'::TIME THEN 'LATE'
                    ELSE 'NORMAL'
                END;
                INSERT INTO attendance (check_in_time, status, course_id, course_name, student_id, remark, create_time)
                VALUES (check_time, att_status, 3, 'Java EE开发', stu.id,
                        NULL, check_time)
                ON CONFLICT DO NOTHING;
            END IF;
        END LOOP;
    END LOOP;
END $$;
