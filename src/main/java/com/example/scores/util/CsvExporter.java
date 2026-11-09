package com.example.scores.util;

import com.example.scores.pojo.ScoreDetail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * CSV 导出工具类。
 * <p>
 * 关键点：
 * 1) 文件编码使用 UTF-8，并在开头写入 BOM（EF BB BF），Excel 打开中文才不会乱码；
 * 2) 字段中出现逗号、引号、换行时，用双引号包裹并把内部引号转义为两个引号（RFC 4180）。
 */
public final class CsvExporter {

    /** UTF-8 BOM，Excel 识别编码用 */
    private static final String UTF8_BOM = "\uFEFF";

    private CsvExporter() {
    }

    /**
     * 导出成绩明细为 CSV 文件。
     *
     * @param file    目标文件路径
     * @param details 成绩明细列表
     * @return 实际写入的文件路径
     * @throws IOException 写文件失败
     */
    public static Path exportScores(Path file, List<ScoreDetail> details) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(UTF8_BOM);
            writer.write("成绩ID,学号,姓名,班级,课程编号,课程名称,学分,分数,考试日期");
            writer.newLine();
            for (ScoreDetail d : details) {
                writer.write(String.join(",",
                        csv(d.getScoreId() == null ? "" : String.valueOf(d.getScoreId())),
                        csv(d.getStudentNo()),
                        csv(d.getStudentName()),
                        csv(d.getClassName()),
                        csv(d.getCourseNo()),
                        csv(d.getCourseName()),
                        csv(d.getCredit() == null ? "" : d.getCredit().toPlainString()),
                        csv(d.getScore() == null ? "" : d.getScore().toPlainString()),
                        csv(d.getExamDate() == null ? "" : d.getExamDate().toString())));
                writer.newLine();
            }
        }
        return file.toAbsolutePath();
    }

    /**
     * 生成带时间戳的默认文件路径。
     * <p>
     * 目录固定为“当前工作目录下的 exports”，这样在 IDEA 中运行和命令行运行
     * 都会落在项目的 exports/ 目录下，不会因为相对路径解析差异跑到别处。
     */
    public static Path defaultFile(String dir) {
        String stamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path base = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        return base.resolve(dir).resolve("score_" + stamp + ".csv");
    }

    /** 单个字段的 CSV 转义 */
    private static String csv(String value) {
        String v = value == null ? "" : value;
        boolean needQuote = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        if (!needQuote) {
            return v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
