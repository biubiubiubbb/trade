package com.example.trade;

import cn.hutool.http.HttpUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SealStockCrawler {

    private static final String CONSECUTIVE_FILE_NAME = "consecutive.xlsx";

    private static final String CONSECUTIVE_FILE_NAME_NEW = "consecutive-new.xlsx";


    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2025, 5, 23);
        LocalDate endDate = LocalDate.of(2025, 6, 13);
        List<SealStock> allList = new ArrayList<>(loadSealStock(null, null));
        List<SealStock> newList = Exec.exec(SealStockCrawler::listSealStockV2, startDate, endDate);
        LocalDate minDate = newList.stream()
                .min(Comparator.comparing(SealStock::getTradeDate))
                .map(SealStock::getTradeDate)
                .orElseThrow();
        LocalDate maxDate = newList.stream()
                .max(Comparator.comparing(SealStock::getTradeDate))
                .map(SealStock::getTradeDate)
                .orElseThrow();
        log.info("之前总数：{}", allList.size());
        allList.removeIf(sealStock -> !sealStock.getTradeDate().isBefore(minDate) && !sealStock.getTradeDate().isAfter(maxDate));
        allList.addAll(newList);
        log.info("之后总数：{}", allList.size());
        Exec.write(CONSECUTIVE_FILE_NAME_NEW, "涨停统计", allList, SealStock.class);
    }

    public static List<SealStock> listSealStock(LocalDate tradeDate) {
        log.info("开始爬取 {} 的涨停数据", tradeDate);
        // 要爬取的网页地址
        String url = String.format("https://www.aijingu.com/zt/%s.html", tradeDate);
        // 使用Jsoup连接网页并获取HTML文档
        Document document;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("爬取失败", e);
            return listSealStock(tradeDate);
        }
        // 解析HTML文档，提取需要的数据
        // 假设网页中有一个表格包含涨停分析数据，表格的id为"t_zt"
        // 你可以根据实际网页的结构修改选择器
        Elements tables = document.select("table");
        if (tables.isEmpty()) {
            return new ArrayList<>();
        }
        Element table = tables.first();
        assert table != null;
        Elements rows = table.select("tr");
        List<SealStock> sealStocks = new ArrayList<>(rows.size());
        // 遍历表格的每一行
        for (int i = 0; i < rows.size(); i++) {
            Elements cols = rows.get(i).select("td");
            List<String> limitUpLines = cols.eachText();
            if (limitUpLines.isEmpty()) {
                continue;
            }
            String limitUpStats = limitUpLines.get(0);
            int j = i + 1;
            while (j < rows.size()) {
                Elements element = rows.get(j).select("td");
                List<String> lines = element.eachText();
                if (lines.size() <= 1) {
                    break;
                }
                // 序号	代码	名称	现价	大单	现涨幅%	换手率	封板额	首次封板	最后封板	打开次数	板块	流通市值
                int consecutiveBoardCount = getConsecutiveBoardCount(limitUpStats);
                long id = Long.parseLong(lines.get(0));
                String code = lines.get(1);
                String name = lines.get(2);
                BigDecimal latestPrice = convertChineseToNumber(lines.get(3));
                BigDecimal tradeAmount = convertChineseToNumber(lines.get(4));
                BigDecimal changeRate = convertPercentageToNumber(lines.get(5));
                BigDecimal turnoverRate = convertPercentageToNumber(lines.get(6));
                BigDecimal sealBoardFunds = convertChineseToNumber(lines.get(7));
                String firstSealTime = lines.get(8);
                String lastSealTime = lines.get(9);
                int breakBoardCount = convertCountToNumber(lines.get(10));
                String industry = lines.get(11);
                BigDecimal circulatingMarketValue = convertChineseToNumber(lines.get(12));
                SealStock sealStock = new SealStock();
                sealStock.setConsecutiveBoardCount(consecutiveBoardCount);
                sealStock.setCode(code);
                sealStock.setName(name);
                sealStock.setLatestPrice(latestPrice);
                sealStock.setTradeAmount(tradeAmount);
                sealStock.setChangeRate(changeRate);
                sealStock.setTurnoverRate(turnoverRate);
                sealStock.setSealBoardFunds(sealBoardFunds);
                sealStock.setFirstSealTime(firstSealTime);
                sealStock.setLastSealTime(lastSealTime);
                sealStock.setBreakBoardCount(breakBoardCount);
                sealStock.setIndustry(industry);
                sealStock.setCirculatingMarketValue(circulatingMarketValue);
                sealStock.setLimitUpStats(limitUpStats);
                sealStock.setTradeDate(tradeDate);
                sealStocks.add(sealStock);
                j++;
            }
            i = j - 1;
        }
        return sealStocks;
    }

    public static List<SealStock> listSealStockV2(LocalDate tradeDate) {
        String dateStr = tradeDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Map<String, Object> params = new HashMap<>();
        params.put("date", dateStr);
        String body = HttpUtil.get("http://127.0.0.1:8067/api/public/stock_zt_pool_em", params);
        List<SealStock> list = JSON.parseArray(body, SealStock.class);
        for (SealStock sealStock : list) {
            sealStock.setChangeRate(sealStock.getChangeRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            sealStock.setTurnoverRate(sealStock.getTurnoverRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            sealStock.setLimitUpStats(convertToLimitUpStats(sealStock.getLimitUpStats()));
            sealStock.setTradeDate(tradeDate);
        }
        return list;
    }

    // 字符串5/4 转 字符串5天4板，1/1 转首板
    private static String convertToLimitUpStats(String input) {
        if (input.contains("/")) {
            String[] split = input.split("/");
            if (split.length == 2) {
                int day = Integer.parseInt(split[0]);
                int board = Integer.parseInt(split[1]);
                if (day == 1 && board == 1) {
                    return "首板";
                }
                return day + "天" + board + "板";
            }
        }
        return input;
    }

    public static List<SealStock> loadSealStock(LocalDate startDate, LocalDate endDate) {
        List<SealStock> sealStocks = new ArrayList<>();
        EasyExcel.read(CONSECUTIVE_FILE_NAME_NEW, SealStock.class, new PageReadListener<SealStock>(sealStocks::addAll)).sheet().doRead();
        if (startDate == null && endDate == null) {
            return sealStocks;
        }
        return sealStocks.stream()
                // 筛选指定时间段数据，2023-03-07 ~ 2023-03-09，包括2023-03-07和2023-03-09
                .filter(sealStock -> !sealStock.getTradeDate().isBefore(startDate) && !sealStock.getTradeDate().isAfter(endDate))
                .toList();
    }

    private static int getConsecutiveBoardCount(String input) {
        // 4天3板  2天2板  首板
        if (Objects.equals(input, "首板")) {
            return 1;
        }
        // 使用正则表达式提取数字
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);
        // 存储提取的数字
        int[] numbers = new int[2];
        int index = 0;

        while (matcher.find()) {
            numbers[index++] = Integer.parseInt(matcher.group());
            if (index == 2) {
                break; // 提取两个数字后停止
            }
        }
        if (numbers[0] == numbers[1]) {
            return numbers[0];
        }
        return 1;
    }

    // 根据字符串的中文单位，转换为对应的数字，如100万转换为100000
    public static BigDecimal convertChineseToNumber(String input) {
        if (input.contains("元")) {
            return new BigDecimal(input.replace("元", "").trim());
        } else if (input.contains("万")) {
            return new BigDecimal(input.replace("万", "").trim()).multiply(new BigDecimal("10000"));
        } else if (input.contains("亿")) {
            return new BigDecimal(input.replace("亿", "").trim()).multiply(new BigDecimal("100000000"));
        } else {
            return new BigDecimal(input);
        }
    }

    // 根据百分比字符串，转换为对应的数字，如10.0%转换为0.1
    public static BigDecimal convertPercentageToNumber(String input) {
        return new BigDecimal(input.replace("%", "").trim()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }

    // 根据次数字符串，转换为对应的数字，如10次转换为10
    public static int convertCountToNumber(String input) {
        return Integer.parseInt(input.replace("次", "").trim());
    }
}
