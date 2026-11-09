package com.example.scores.service;

import com.example.scores.dao.ScoreDao;
import com.example.scores.pojo.Score;
import com.example.scores.pojo.ScoreDetail;
import com.example.scores.util.BusinessException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * 成绩业务层：录入、查询、修改、删除，以及分数合法性校验。
 */
public class ScoreService {

    private final ScoreDao scoreDao = new ScoreDao();

    /**
     * 录入成绩。同一学生同一课程只允许一条记录，重复录入会提示修改。
     *
     * @return 新成绩主键
     */
    public int add(Score score) {
        validate(score);
        try {
            Score exists = scoreDao.findByStudentAndCourse(score.getStudentId(), score.getCourseId());
            if (exists != null) {
                throw new BusinessException("该学生这门课程已有成绩（" + exists.getScore()
                        + " 分），请使用“修改成绩”功能，id = " + exists.getId());
            }
            return scoreDao.insert(score);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new BusinessException("该学生这门课程已有成绩，不能重复录入");
            }
            // 外键约束失败：学生或课程不存在
            if (e.getErrorCode() == 1452) {
                throw new BusinessException("学生或课程不存在，请检查 id 是否正确");
            }
            throw new BusinessException("录入成绩失败：" + e.getMessage(), e);
        }
    }

    /** 修改成绩分数与考试日期 */
    public void update(Score score) {
        if (score.getId() == null) {
            throw new BusinessException("缺少成绩主键，无法修改");
        }
        if (score.getScore() == null) {
            throw new BusinessException("分数不能为空");
        }
        checkScoreRange(score.getScore());
        try {
            if (scoreDao.findById(score.getId()) == null) {
                throw new BusinessException("成绩记录不存在，可能已被删除");
            }
            scoreDao.update(score);
        } catch (SQLException e) {
            throw new BusinessException("修改成绩失败：" + e.getMessage(), e);
        }
    }

    /** 删除成绩 */
    public void delete(int id) {
        try {
            if (scoreDao.findById(id) == null) {
                throw new BusinessException("成绩记录不存在，可能已被删除");
            }
            scoreDao.deleteById(id);
        } catch (SQLException e) {
            throw new BusinessException("删除成绩失败：" + e.getMessage(), e);
        }
    }

    /** 全部成绩明细 */
    public List<ScoreDetail> findAll() {
        try {
            return scoreDao.findAllDetails();
        } catch (SQLException e) {
            throw new BusinessException("查询成绩失败：" + e.getMessage(), e);
        }
    }

    /** 按学生查询（学号或姓名） */
    public List<ScoreDetail> findByStudent(String keyword) {
        requireKeyword(keyword, "学号或姓名");
        try {
            return scoreDao.findByStudent(keyword.trim());
        } catch (SQLException e) {
            throw new BusinessException("按学生查询成绩失败：" + e.getMessage(), e);
        }
    }

    /** 按课程查询（课程编号或名称） */
    public List<ScoreDetail> findByCourse(String keyword) {
        requireKeyword(keyword, "课程编号或名称");
        try {
            return scoreDao.findByCourse(keyword.trim());
        } catch (SQLException e) {
            throw new BusinessException("按课程查询成绩失败：" + e.getMessage(), e);
        }
    }

    /** 按分数段查询（含边界） */
    public List<ScoreDetail> findByScoreRange(BigDecimal min, BigDecimal max) {
        if (min == null || max == null) {
            throw new BusinessException("分数段的最小值和最大值都不能为空");
        }
        if (min.compareTo(max) > 0) {
            throw new BusinessException("最小分不能大于最大分");
        }
        checkScoreRange(min);
        checkScoreRange(max);
        try {
            return scoreDao.findByScoreRange(min, max);
        } catch (SQLException e) {
            throw new BusinessException("按分数段查询成绩失败：" + e.getMessage(), e);
        }
    }

    /** 组合条件查询，条件都可以为空 */
    public List<ScoreDetail> search(String studentKeyword, String courseKeyword,
                                    BigDecimal min, BigDecimal max) {
        if (min != null) {
            checkScoreRange(min);
        }
        if (max != null) {
            checkScoreRange(max);
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BusinessException("最小分不能大于最大分");
        }
        try {
            return scoreDao.search(studentKeyword, courseKeyword, min, max);
        } catch (SQLException e) {
            throw new BusinessException("查询成绩失败：" + e.getMessage(), e);
        }
    }

    private void validate(Score score) {
        if (score == null) {
            throw new BusinessException("成绩信息不能为空");
        }
        if (score.getStudentId() == null) {
            throw new BusinessException("必须指定学生 id");
        }
        if (score.getCourseId() == null) {
            throw new BusinessException("必须指定课程 id");
        }
        if (score.getScore() == null) {
            throw new BusinessException("分数不能为空");
        }
        checkScoreRange(score.getScore());
    }

    private void checkScoreRange(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("分数必须在 0 ~ 100 之间，当前值：" + value.toPlainString());
        }
    }

    private void requireKeyword(String keyword, String fieldName) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException("请输入" + fieldName);
        }
    }
}
