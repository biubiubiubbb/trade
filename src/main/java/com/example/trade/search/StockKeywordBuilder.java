package com.example.trade.search;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class StockKeywordBuilder {

    private static final DateTimeFormatter CHINESE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年M月d号");

    /**
     * Build stock screening keyword with dynamic dates
     *
     * @param today     今日日期 (e.g. 2025-06-13)
     * @param yesterday 昨日日期 (e.g. 2025-06-12)
     * @return 组合好的关键词字符串
     */
    public static String buildKeyword(LocalDate yesterday, LocalDate today, BigDecimal gap) {
        Objects.requireNonNull(today, "Today date cannot be null");
        Objects.requireNonNull(yesterday, "Yesterday date cannot be null");

        String todayStr = formatChineseDate(today);
        String yesterdayStr = formatChineseDate(yesterday);

        return String.join(";",
                // 昨日条件
                yesterdayStr + "上影线阳线",
                yesterdayStr + "上影线曾涨停",
                // 今日条件
                todayStr + "竞价高开" + gap + "%以上",
                todayStr + "的竞价金额大于" + yesterdayStr + "的竞价金额",
                // 通用条件
                "流通市值小于200亿",
                "不要st股及不要退市股",
                "不要北交所",
                "不要科创板",
                "不要ST股及不要退市股"
        ) + ";";
    }

    /**
     * Build stock screening keyword with dynamic dates
     *
     * @param today     今日日期 (e.g. 2025-06-13)
     * @param yesterday 昨日日期 (e.g. 2025-06-12)
     * @return 组合好的关键词字符串
     */
    public static String buildKeywordV2(LocalDate yesterday, LocalDate today, BigDecimal gap) {
        Objects.requireNonNull(today, "Today date cannot be null");
        Objects.requireNonNull(yesterday, "Yesterday date cannot be null");

        String todayStr = formatChineseDate(today);
        String yesterdayStr = formatChineseDate(yesterday);

        return String.join(";",
                // 昨日条件
                yesterdayStr + "上影线阳线",
                yesterdayStr + "最高涨幅超过15cm",
                // 今日条件
                todayStr + "竞价高开" + gap + "%以上",
                todayStr + "的竞价金额大于" + yesterdayStr + "的竞价金额",
                // 通用条件
                "流通市值小于200亿",
                "不要st股及不要退市股",
                "不要北交所",
                "不要ST股及不要退市股"
        ) + ";";
    }


    private static String formatChineseDate(LocalDate date) {
        return date.format(CHINESE_DATE_FORMATTER)
                .replace("月0", "月")  // 处理单数日期前导零
                .replace("号0", "号");
    }

}
