package com.example.trade.simulate;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.NumberUtil;
import com.example.trade.DataCenter;
import com.example.trade.Snapshot;
import com.example.trade.profit.StatisticProfit;
import com.example.trade.profit.StockProfit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfitCalculate {

    // 每次买入大约1w
    private BigDecimal buyAmount = BigDecimal.valueOf(10000);

    protected BigDecimal calChangeRate(BigDecimal before, BigDecimal after) {
        return after.subtract(before).divide(before, 4, RoundingMode.HALF_DOWN);
    }

    protected BigDecimal calAfterPrice(BigDecimal beforePrice, BigDecimal changeRate) {
        return beforePrice.multiply(BigDecimal.ONE.add(changeRate));
    }


    protected List<BigDecimal> calBuyQuantity(List<BigDecimal> buyPrices) {
        return buyPrices.stream()
                .map(buyPrice -> buyAmount.divide(buyPrice, 0, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
    }

    protected BigDecimal calBuyQuantity(BigDecimal buyPrice) {
        return buyAmount.divide(buyPrice, 0, RoundingMode.HALF_UP);
    }

    protected Pair<BigDecimal, LocalDate> trySell(String code, BigDecimal avgPrice, LocalDate sellDate) {
        Snapshot snapshot = DataCenter.getSnapshot(code, sellDate, true);
        Snapshot preSnapshot = DataCenter.getSnapshot(code, DataCenter.getPrevTradeDate(sellDate), false);
        if (snapshot == null || preSnapshot == null) {
            return null;
        }
        BigDecimal preClosePrice = preSnapshot.getClosePrice();
        BigDecimal topPrice = calAfterPrice(preClosePrice, BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal openPrice = snapshot.getOpenPrice();
        if (NumberUtil.equals(topPrice, snapshot.getOpenPrice())) {
            // 一字开，盘中炸板，则9个点卖出，否则直到不开一字或盘中炸板
            if (NumberUtil.equals(snapshot.getAmplitude(), BigDecimal.ZERO)) {
                LocalDate nextSellDate = DataCenter.getNextTradeDate(sellDate);
                if (nextSellDate.isAfter(LocalDate.now())) {
                    return Pair.of(openPrice, sellDate);
                }
                return trySell(code, avgPrice, nextSellDate);
            } else {
                return Pair.of(calAfterPrice(preClosePrice, BigDecimal.valueOf(0.09)), sellDate);
            }
        } else if (snapshot.getOpenPrice().compareTo(avgPrice) >= 0) {
            // 开盘有盈利，竞价卖出，盈利
            return Pair.of(openPrice, sellDate);
        } else if (snapshot.getHighestPrice().compareTo(avgPrice) >= 0) {
            // 非一字开，如果大于或等于买入均价，则均价卖出，不亏本
            return Pair.of(avgPrice, sellDate);
        } else {
            // 否则未达到买入均价，不卖出，亏损
            return null;
        }
    }


    protected BigDecimal calBuyAmount(List<BigDecimal> buyPrices, List<BigDecimal> buyQuantityList) {
        BigDecimal buyAmount = BigDecimal.ZERO;
        for (int i = 0; i < buyPrices.size(); i++) {
            BigDecimal buyPrice = buyPrices.get(i);
            BigDecimal buyQuantity = buyQuantityList.get(i);
            buyAmount = buyAmount.add(buyPrice.multiply(buyQuantity));
        }
        return buyAmount;
    }

    public <T extends StockProfit> List<StatisticProfit> calMonthlyProfit(List<T> stockProfits) {
        // 按月份分组
        Map<String, List<StockProfit>> monthlyProfits = stockProfits.stream()
                .collect(Collectors.groupingBy(p -> p.getSellDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        List<StatisticProfit> statistics = new ArrayList<>();

        for (Map.Entry<String, List<StockProfit>> entry : monthlyProfits.entrySet()) {
            String month = entry.getKey();
            List<StockProfit> profits = entry.getValue();

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

            for (StockProfit profit : profits) {
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
            StatisticProfit statistic = new StatisticProfit();
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
            statistic.setMonth(month);
            statistics.add(statistic);
        }
        statistics.sort(Comparator.comparing(StatisticProfit::getMonth).reversed());
        return statistics;
    }

}

