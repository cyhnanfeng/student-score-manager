package com.example.scores.service;

import com.example.scores.dao.CourseDao;
import com.example.scores.pojo.Course;
import com.example.scores.util.BusinessException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * 课程业务层：参数校验 + 唯一性检查。
 */
public class CourseService {

    private final CourseDao courseDao = new CourseDao();

    /** 新增课程 */
    public int add(Course course) {
        validate(course);
        try {
            if (courseDao.findByCourseNo(course.getCourseNo()) != null) {
                throw new BusinessException("课程编号已存在：" + course.getCourseNo());
            }
            return courseDao.insert(course);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new BusinessException("课程编号已存在：" + course.getCourseNo());
            }
            throw new BusinessException("新增课程失败：" + e.getMessage(), e);
        }
    }

    /** 修改课程（课程编号不可改） */
    public void update(Course course) {
        if (course.getId() == null) {
            throw new BusinessException("缺少课程主键，无法修改");
        }
        validate(course);
        try {
            if (courseDao.findById(course.getId()) == null) {
                throw new BusinessException("课程不存在，可能已被删除");
            }
            courseDao.update(course);
        } catch (SQLException e) {
            throw new BusinessException("修改课程失败：" + e.getMessage(), e);
        }
    }

    /**
     * 删除课程。
     *
     * @return 被级联删除的成绩条数
     */
    public int delete(int id) {
        try {
            if (courseDao.findById(id) == null) {
                throw new BusinessException("课程不存在，可能已被删除");
            }
            int scoreCount = courseDao.countScores(id);
            courseDao.deleteById(id);
            return scoreCount;
        } catch (SQLException e) {
            throw new BusinessException("删除课程失败：" + e.getMessage(), e);
        }
    }

    /** 按课程编号/名称查询，参数为空则查全部 */
    public List<Course> search(String courseNo, String name) {
        try {
            return courseDao.search(courseNo, name);
        } catch (SQLException e) {
            throw new BusinessException("查询课程失败：" + e.getMessage(), e);
        }
    }

    /** 按主键查询 */
    public Course findById(int id) {
        try {
            Course c = courseDao.findById(id);
            if (c == null) {
                throw new BusinessException("未找到 id = " + id + " 的课程");
            }
            return c;
        } catch (SQLException e) {
            throw new BusinessException("查询课程失败：" + e.getMessage(), e);
        }
    }

    /** 全部课程，供成绩录入时选择 */
    public List<Course> findAll() {
        try {
            return courseDao.findAll();
        } catch (SQLException e) {
            throw new BusinessException("查询课程失败：" + e.getMessage(), e);
        }
    }

    private void validate(Course c) {
        if (c == null) {
            throw new BusinessException("课程信息不能为空");
        }
        if (c.getCourseNo() == null || c.getCourseNo().isBlank()) {
            throw new BusinessException("课程编号不能为空");
        }
        if (c.getCourseNo().trim().length() > 20) {
            throw new BusinessException("课程编号长度不能超过 20 个字符");
        }
        if (c.getName() == null || c.getName().isBlank()) {
            throw new BusinessException("课程名称不能为空");
        }
        if (c.getName().trim().length() > 50) {
            throw new BusinessException("课程名称长度不能超过 50 个字符");
        }
        if (c.getCredit() == null) {
            throw new BusinessException("学分不能为空");
        }
        if (c.getCredit().compareTo(BigDecimal.ZERO) <= 0
                || c.getCredit().compareTo(new BigDecimal("99.9")) > 0) {
            throw new BusinessException("学分必须在 0.1 ~ 99.9 之间");
        }
        if (c.getTeacher() == null) {
            c.setTeacher("");
        }
        c.setCourseNo(c.getCourseNo().trim());
        c.setName(c.getName().trim());
    }
}
