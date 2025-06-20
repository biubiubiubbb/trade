package com.example.trade.simulate;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.NumberUtil;
import com.example.trade.DataCenter;
import com.example.trade.Snapshot;
import com.example.trade.profit.StatisticProfit;
import com.example.trade.profit.StockProfit;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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

    protected Pair<BigDecimal, LocalDate> mustSell(String code, BigDecimal avgPrice, LocalDate sellDate) {
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
                return mustSell(code, avgPrice, nextSellDate);
            } else {
                return Pair.of(calAfterPrice(preClosePrice, BigDecimal.valueOf(0.09)), sellDate);
            }
        } else if (openPrice.compareTo(avgPrice) >= 0) {
            // 竞价盈利卖出
            return Pair.of(openPrice, sellDate);
        } else if (snapshot.getHighestPrice().compareTo(avgPrice) >= 0) {
            // 竞价亏损，挂成本价卖出
            return Pair.of(avgPrice, sellDate);
        } else {
            // 收盘亏损卖出
            return Pair.of(snapshot.getClosePrice(), sellDate);
        }
    }

    protected Pair<BigDecimal, LocalDate> mustSellV2(String code, BigDecimal avgPrice, LocalDate sellDate) {
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
                return mustSellV2(code, avgPrice, nextSellDate);
            } else {
                return Pair.of(calAfterPrice(preClosePrice, BigDecimal.valueOf(0.09)), sellDate);
            }
        } else {
            return Pair.of(openPrice, sellDate);
        }
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

    protected Pair<BigDecimal, LocalDate> trySellV2(String code, BigDecimal avgPrice, LocalDate sellDate) {
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
                return trySellV2(code, avgPrice, nextSellDate);
            } else {
                return Pair.of(calAfterPrice(preClosePrice, BigDecimal.valueOf(0.09)), sellDate);
            }
        } else if (snapshot.getOpenPrice().compareTo(avgPrice) >= 0) {
            // 开盘有盈利，竞价卖出，盈利
            return Pair.of(openPrice, sellDate);
        } else if (calChangeRate(avgPrice, snapshot.getHighestPrice()).multiply(BigDecimal.valueOf(100)).compareTo(BigDecimal.valueOf(3)) >= 0) {
            // 非一字开，盈利2个点卖出
            BigDecimal sellPrice = calAfterPrice(avgPrice, BigDecimal.valueOf(0.03));
            return Pair.of(sellPrice, sellDate);
        } else {
            // 否则未达到买入均价，不卖出，亏损
            return null;
        }
    }

    protected Pair<BigDecimal, LocalDate> trySellV3(String code, BigDecimal avgPrice, LocalDate buyDate, LocalDate sellDate) {
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
                return trySellV3(code, avgPrice, buyDate, nextSellDate);
            } else {
                return Pair.of(calAfterPrice(preClosePrice, BigDecimal.valueOf(0.09)), sellDate);
            }
        } else if (snapshot.getOpenPrice().compareTo(avgPrice) >= 0) {
            // 开盘有盈利，竞价卖出，盈利
            return Pair.of(openPrice, sellDate);
        } else if (calChangeRate(avgPrice, snapshot.getHighestPrice()).multiply(BigDecimal.valueOf(100)).compareTo(BigDecimal.valueOf(2)) >= 0) {
            // 非一字开，盈利2个点卖出
            BigDecimal sellPrice = calAfterPrice(avgPrice, BigDecimal.valueOf(0.02));
            return Pair.of(sellPrice, sellDate);
        } else {
            // 否则未达到买入均价，不卖出，亏损
            int holdDays = DataCenter.getHoldDays(buyDate, sellDate);
            if(holdDays > 5) {
                return Pair.of(snapshot.getClosePrice(), sellDate);
            } else {
                LocalDate nextSellDate = DataCenter.getNextTradeDate(sellDate);
                return trySellV3(code, avgPrice, buyDate, nextSellDate);
            }
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
                .collect(Collectors.groupingBy(p -> p.getBuyDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        BigDecimal allProfit = stockProfits.stream().map(StockProfit::getProfit).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<StatisticProfit> statistics = new ArrayList<>();

        //    @ExcelProperty("盈利前三")
        //    private BigDecimal winHot3;
        //    @ExcelProperty("亏损前三")
        //    private BigDecimal lossHot3;

        for (Map.Entry<String, List<StockProfit>> entry : monthlyProfits.entrySet()) {
            String month = entry.getKey();
            List<StockProfit> profits = entry.getValue();

            // 初始化统计数据
            BigDecimal totalProfit = BigDecimal.ZERO;
            BigDecimal totalBuyAmount = BigDecimal.ZERO;
            BigDecimal totalSellAmount = BigDecimal.ZERO;
            int profitCount = 0;
            int lossCount = 0;
            int count = 0;
            BigDecimal maxProfit = BigDecimal.ZERO;
            BigDecimal maxProfitRate = BigDecimal.ZERO;
            BigDecimal maxLoss = BigDecimal.ZERO;
            BigDecimal maxLossRate = BigDecimal.ZERO;
            String maxProfitStock = "";
            String maxLossStock = "";
            // 盈利前三
            String winHot3 = profits.stream()
                    .filter(profit -> profit.getProfit().compareTo(BigDecimal.valueOf(500)) >= 0)
                    .sorted(Comparator.comparing(StockProfit::getProfit).reversed())
                    .limit(3)
                    .map(item -> item.getName() + "(" + item.getProfit() + ")")
                    .toList()
                    .toString();
            // 亏损前三
            String lossHot3 = profits.stream()
                    .filter(profit -> profit.getProfit().compareTo(BigDecimal.valueOf(-500)) <= 0)
                    .sorted(Comparator.comparing(StockProfit::getProfit))
                    .limit(3)
                    .map(item -> item.getName() + "(" + item.getProfit() + ")")
                    .toList()
                    .toString();

            for (StockProfit profit : profits) {
                totalProfit = totalProfit.add(profit.getProfit());
                totalBuyAmount = totalBuyAmount.add(profit.getBuyAmount());
                totalSellAmount = totalSellAmount.add(profit.getSellAmount());

                if (profit.getBuyAvgPrice().compareTo(profit.getSellAvgPrice()) == 0) {
                    count++;
                } else if (profit.getProfit().compareTo(BigDecimal.ZERO) > 0) {
                    profitCount++;
                    if (profit.getProfit().compareTo(maxProfit) > 0) {
                        maxProfit = profit.getProfit();
                        maxProfitStock = profit.getName() + "(" + profit.getBuyDate() + "买入)";
                        maxProfitRate = profit.getProfitRate();
                    }
                } else if (profit.getProfit().compareTo(BigDecimal.ZERO) < 0) {
                    lossCount++;
                    if (profit.getProfit().compareTo(maxLoss) < 0) {
                        maxLoss = profit.getProfit();
                        maxLossStock = profit.getName() + "(" + profit.getBuyDate() + "买入)";
                        maxLossRate = profit.getProfitRate();
                    }
                } else {
                    log.info("无法判断盈利亏损");
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
            statistic.setMaxLoss(maxLoss);
            statistic.setMaxLossStock(maxLossStock);
            statistic.setMaxLossRate(maxLossRate);
            statistic.setMonth(month);
            statistic.setCount(count);
            statistic.setLossHot3(lossHot3);
            statistic.setWinHot3(winHot3);
            statistic.setRemark(allProfit.toPlainString());
            statistics.add(statistic);
        }
        statistics.sort(Comparator.comparing(StatisticProfit::getMonth).reversed());
        return statistics;
    }

}

