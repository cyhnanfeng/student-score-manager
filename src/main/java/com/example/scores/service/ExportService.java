package com.example.scores.service;

import com.example.scores.pojo.ScoreDetail;
import com.example.scores.util.BusinessException;
import com.example.scores.util.CsvExporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 导出业务层：把成绩查询结果写成 CSV。
 */
public class ExportService {

    /**
     * 导出成绩明细到指定路径。
     *
     * @param file    目标文件
     * @param details 要导出的数据
     * @return 实际写入的绝对路径
     */
    public Path exportScores(Path file, List<ScoreDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new BusinessException("没有可导出的数据，请先查询出结果");
        }
        try {
            return CsvExporter.exportScores(file, details);
        } catch (IOException e) {
            throw new BusinessException("导出 CSV 失败：" + e.getMessage(), e);
        }
    }

    /** 默认导出目录：项目根目录下的 exports/ */
    public Path defaultExportFile() {
        return CsvExporter.defaultFile("exports");
    }
}
