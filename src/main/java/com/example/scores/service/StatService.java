package com.example.scores.service;

import com.example.scores.dao.StatDao;
import com.example.scores.pojo.CourseStat;
import com.example.scores.pojo.StudentStat;
import com.example.scores.util.BusinessException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * 统计业务层：平均分、最高分、最低分、及格率。
 */
public class StatService {

    private final StatDao statDao = new StatDao();

    /** 按课程统计 */
    public List<CourseStat> byCourse() {
        try {
            return statDao.statByCourse();
        } catch (SQLException e) {
            throw new BusinessException("按课程统计失败：" + e.getMessage(), e);
        }
    }

    /** 按学生统计 */
    public List<StudentStat> byStudent() {
        try {
            return statDao.statByStudent();
        } catch (SQLException e) {
            throw new BusinessException("按学生统计失败：" + e.getMessage(), e);
        }
    }

    /** 全局平均分，无数据时返回 null */
    public BigDecimal overallAverage() {
        try {
            return statDao.overall()[1];
        } catch (SQLException e) {
            throw new BusinessException("统计全局平均分失败：" + e.getMessage(), e);
        }
    }
}
