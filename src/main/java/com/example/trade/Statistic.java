package com.example.trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Statistic {

    private static final String ONE_BOARD_FILE_NAME = "一进二模式收益.xlsx";

    private static final String TWO_BOARD_FILE_NAME = "二进三模式收益.xlsx";

    private static final String THREE_BOARD_FILE_NAME = "三进四模式收益.xlsx";

    private static final String FOUR_BOARD_FILE_NAME = "四进五模式收益.xlsx";

    public static void main(String[] args) {
        run(TWO_BOARD_FILE_NAME);
        run(THREE_BOARD_FILE_NAME);
        run(FOUR_BOARD_FILE_NAME);
    }

    private static void run(String fileName) {
        List<SealProfit> list = Exec.read(fileName, SealProfit.class);
        String staticFileName = fileName.replace(".xlsx", "_统计.xlsx");
        List<SealProfitStatistic> sealProfitStatistics = calculateMonthlyStatistics(list);
        Exec.write(staticFileName, "统计", sealProfitStatistics, SealProfitStatistic.class);
    }

    public static List<SealProfitStatistic> calculateMonthlyStatistics(List<SealProfit> sealProfits) {
        // 按月份分组
        Map<String, List<SealProfit>> monthlyProfits = sealProfits.stream()
                .collect(Collectors.groupingBy(p -> p.getSellDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        List<SealProfitStatistic> statistics = new ArrayList<>();

        for (Map.Entry<String, List<SealProfit>> entry : monthlyProfits.entrySet()) {
            String month = entry.getKey();
            List<SealProfit> profits = entry.getValue();

            // 初始化统计数据
            BigDecimal totalProfit = BigDecimal.ZERO;
            BigDecimal totalBuyAmount = BigDecimal.ZERO;
            BigDecimal totalSellAmount = BigDecimal.ZERO;
            int profitCount = 0;
            int lossCount = 0;
            BigDecimal maxProfit = BigDecimal.ZERO;
            BigDecimal maxProfitRate = BigDecimal.ZERO;
            BigDecimal maxLoss = BigDecimal.ZERO;
            BigDecimal maxLossRate = BigDecimal.ZERO;
            String maxProfitStock = "";
            String maxProfitRateStock = "";
            String maxLossStock = "";
            String maxLossRateStock = "";

            for (SealProfit profit : profits) {
                totalProfit = totalProfit.add(profit.getProfit());
                totalBuyAmount = totalBuyAmount.add(profit.getBuyAmount());
                totalSellAmount = totalSellAmount.add(profit.getSellAmount());

                if (profit.getProfit().compareTo(BigDecimal.ZERO) > 0) {
                    profitCount++;
                    if (profit.getProfit().compareTo(maxProfit) > 0) {
                        maxProfit = profit.getProfit();
                        maxProfitStock = profit.getName();
                    }
                    if (profit.getProfitRate().compareTo(maxProfitRate) > 0) {
                        maxProfitRate = profit.getProfitRate();
                        maxProfitRateStock = profit.getName();
                    }
                } else {
                    lossCount++;
                    if (profit.getProfit().compareTo(maxLoss) < 0) {
                        maxLoss = profit.getProfit();
                        maxLossStock = profit.getCode();
                    }
                    if (profit.getProfitRate().compareTo(maxLossRate) < 0) {
                        maxLossRate = profit.getProfitRate();
                        maxLossRateStock = profit.getCode();
                    }
                }
            }

            // 计算总收益率
            BigDecimal totalProfitRate = totalBuyAmount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                    totalProfit.divide(totalBuyAmount, 4, RoundingMode.HALF_UP);

            // 创建统计数据对象
            SealProfitStatistic statistic = new SealProfitStatistic();
            statistic.setMonth(month);
            statistic.setProfit(totalProfit);
            statistic.setProfitRate(totalProfitRate);
            statistic.setProfitCount(profitCount);
            statistic.setLossCount(lossCount);
            statistic.setBuyAmount(totalBuyAmount);
            statistic.setSellAmount(totalSellAmount);
            statistic.setMaxProfitStock(maxProfitStock);
            statistic.setMaxProfit(maxProfit);
            statistic.setMaxProfitRate(maxProfitRate);
            statistic.setMaxProfitRateStock(maxProfitRateStock);
            statistic.setMaxLoss(maxLoss);
            statistic.setMaxLossStock(maxLossStock);
            statistic.setMaxLossRate(maxLossRate);
            statistic.setMaxLossRateStock(maxLossRateStock);

            statistics.add(statistic);
        }
        statistics.sort(Comparator.comparing(SealProfitStatistic::getMonth).reversed());
        return statistics;
    }
}
