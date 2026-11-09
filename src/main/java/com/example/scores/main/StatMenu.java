package com.example.scores.main;

import com.example.scores.service.StatService;
import com.example.scores.util.BusinessException;
import com.example.scores.util.ConsoleUtil;

import java.math.BigDecimal;

/**
 * 统计菜单：按课程统计、按学生统计、全局平均分。
 */
public class StatMenu {

    private final ConsoleInput input;
    private final StatService statService;

    public StatMenu(ConsoleInput input, StatService statService) {
        this.input = input;
        this.statService = statService;
    }

    /** 显示统计菜单 */
    public void show() {
        while (true) {
            System.out.println();
            System.out.println("=== 成绩统计 ===");
            System.out.println("1. 按课程统计（平均分/最高分/最低分/及格率）");
            System.out.println("2. 按学生统计（平均分/最高分/最低分/及格率）");
            System.out.println("3. 全局平均分");
            System.out.println("0. 返回主菜单");
            String choice = input.readLine("请选择：");
            try {
                switch (choice) {
                    case "1" -> {
                        System.out.println();
                        System.out.println("--- 按课程统计 ---");
                        ConsoleView.printCourseStats(statService.byCourse());
                    }
                    case "2" -> {
                        System.out.println();
                        System.out.println("--- 按学生统计 ---");
                        ConsoleView.printStudentStats(statService.byStudent());
                    }
                    case "3" -> {
                        BigDecimal avg = statService.overallAverage();
                        System.out.println("全局平均分：" + (avg == null ? "暂无成绩数据" : ConsoleUtil.num(avg)));
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("无效选项，请重新选择。");
                }
            } catch (BusinessException e) {
                System.out.println("【操作失败】" + e.getMessage());
            }
            input.pause();
        }
    }
}
