package com.example.trade;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SealStockMain {

    private static final String ONE_BOARD_FILE_NAME = "一进二模式收益.xlsx";

    private static final String TWO_BOARD_FILE_NAME = "二进三模式收益.xlsx";

    private static final String THREE_BOARD_FILE_NAME = "三进四模式收益.xlsx";

    private static final String FOUR_BOARD_FILE_NAME = "四进五模式收益.xlsx";


    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 11);
        List<SealStock> sealStocks = Exec.exec(DataCenter::listSealStock, startDate, endDate);
        sealStocks.sort(Comparator.comparing(SealStock::getTradeDate).reversed());
        // 首板
//        List<SealStock> oneList = sealStocks.stream()
//                .filter(sealStock -> MarketType.getMarketType(sealStock.getCode()) == MarketType.MAIN_BOARD)
//                .filter(sealStock -> sealStock.getConsecutiveBoardCount() == 1)
//                .toList();
//        run(oneList, ONE_BOARD_FILE_NAME);
        // 二板
        List<SealStock> twoList = sealStocks.stream()
                .filter(sealStock -> MarketType.getMarketType(sealStock.getCode()) == MarketType.MAIN_BOARD)
                .filter(sealStock -> sealStock.getConsecutiveBoardCount() == 2)
                .toList();
        run(twoList, TWO_BOARD_FILE_NAME);
        // 三板
        List<SealStock> threeList = sealStocks.stream()
                .filter(sealStock -> MarketType.getMarketType(sealStock.getCode()) == MarketType.MAIN_BOARD)
                .filter(sealStock -> sealStock.getConsecutiveBoardCount() == 3)
                .toList();
        run(threeList, THREE_BOARD_FILE_NAME);
        // 四板
        List<SealStock> fourList = sealStocks.stream()
                .filter(sealStock -> MarketType.getMarketType(sealStock.getCode()) == MarketType.MAIN_BOARD)
                .filter(sealStock -> sealStock.getConsecutiveBoardCount() == 4)
                .toList();
        run(fourList, FOUR_BOARD_FILE_NAME);
    }

    private static void run(List<SealStock> sealStocks, String fileName) {
        List<SealProfit> sealProfits = calProfit(sealStocks);
        Exec.write(fileName, "Sheet1", sealProfits, SealProfit.class);
    }

    private static BigDecimal calChangeRate(BigDecimal before, BigDecimal after) {
        return after.subtract(before).divide(before, 4, RoundingMode.HALF_DOWN);
    }

    private static BigDecimal calAfterPrice(BigDecimal beforePrice, BigDecimal changeRate) {
        return beforePrice.multiply(BigDecimal.ONE.add(changeRate));
    }


    private static List<SealProfit> calProfit(List<SealStock> sealStocks) {
        List<SealProfit> profits = new ArrayList<>();
        for (SealStock sealStock : sealStocks) {
            log.info("开始计算：{}-{}", sealStock.getName(), sealStock.getTradeDate());
            // 第二天开始买入
            LocalDate buyDate = DataCenter.getNextTradeDate(sealStock.getTradeDate());
            Snapshot snapshot = DataCenter.getSnapshot(sealStock.getCode(), buyDate, true);
            if (snapshot == null) {
                continue;
            }
            buyDate = snapshot.getDate();
            Pair<List<BigDecimal>, List<BigDecimal>> buyPair = buy(sealStock, snapshot);
            // 计算竞价开的点数
            BigDecimal gap = calChangeRate(sealStock.getLatestPrice(), snapshot.getOpenPrice()).multiply(BigDecimal.valueOf(100));
            if (buyPair == null || buyPair.getValue().isEmpty()) {
                continue;
            }
            List<BigDecimal> buyPrices = buyPair.getKey();
            List<BigDecimal> buyPoints = buyPair.getValue();
            // 买入股数
            List<BigDecimal> buyQuantity = calBuyQuantity(buyPrices);
            // 买入金额
            BigDecimal buyAmount = calBuyAmount(buyPrices, buyQuantity);
            // 买入第二天开始卖出
            LocalDate sellDate = DataCenter.getNextTradeDate(buyDate);
            // 卖出价格
            Pair<BigDecimal, LocalDate> sellPair = sell(sealStock.getCode(), sellDate);
            if (sellPair == null) {
                continue;
            }
            BigDecimal sellPrice = sellPair.getKey();
            sellDate = sellPair.getValue();
            // 卖出金额 = 总股数 * 卖出价格
            BigDecimal totalQuantity = buyQuantity.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            // 买入均价
            BigDecimal buyAvgPrice = buyAmount.divide(totalQuantity, 2, RoundingMode.HALF_DOWN);
            BigDecimal sellAmount = totalQuantity.multiply(sellPrice);
            BigDecimal profit = sellAmount.subtract(buyAmount);
            BigDecimal profitRate = calChangeRate(buyAmount, sellAmount);
            if (NumberUtil.equals(profitRate, BigDecimal.ZERO)) {
                continue;
            }
            SealProfit sealProfit = new SealProfit();
            sealProfit.setCode(sealStock.getCode());
            sealProfit.setName(sealStock.getName());
            sealProfit.setBuyAvgPrice(buyAvgPrice);
            sealProfit.setBuyPrices(buyPrices.toString());
            sealProfit.setBuyPoints(buyPoints.toString());
            sealProfit.setGap(gap);
            sealProfit.setBuyDate(buyDate);
            sealProfit.setSellDate(sellDate);
            sealProfit.setBuyAmount(buyAmount);
            sealProfit.setSellPrice(sellPrice);
            sealProfit.setSellAmount(sellAmount);
            sealProfit.setProfit(profit);
            sealProfit.setProfitRate(profitRate.multiply(BigDecimal.valueOf(100)));
            sealProfit.setHoldingDays(DataCenter.getHoldDays(buyDate, sellDate));
//            sealProfit.setHoldingProfitOfBuyDate(profit.divide(BigDecimal.valueOf(sealProfit.getHoldingDays()), 2, RoundingMode.HALF_DOWN));
//            sealProfit.setHoldingProfitRateOfBuyDate(sealProfit.getHoldingProfitOfBuyDate().divide(buyAmount, 2, RoundingMode.HALF_DOWN));
            profits.add(sealProfit);
        }
        return profits;
    }

    private static List<BigDecimal> calBuyQuantity(List<BigDecimal> buyPrices) {
        // 每次买入大约1w
        BigDecimal buyAmount = BigDecimal.valueOf(10000);
        return buyPrices.stream()
                .map(buyPrice -> buyAmount.divide(buyPrice, 0, RoundingMode.HALF_UP))
                .collect(Collectors.toList());
    }

    private static BigDecimal calBuyAmount(List<BigDecimal> buyPrices, List<BigDecimal> buyQuantityList) {
        BigDecimal buyAmount = BigDecimal.ZERO;
        for (int i = 0; i < buyPrices.size(); i++) {
            BigDecimal buyPrice = buyPrices.get(i);
            BigDecimal buyQuantity = buyQuantityList.get(i);
            buyAmount = buyAmount.add(buyPrice.multiply(buyQuantity));
        }
        return buyAmount;
    }

    private static Pair<List<BigDecimal>, List<BigDecimal>> buy(SealStock sealStock, Snapshot snapshot) {
        // 开盘价
        BigDecimal openPrice = snapshot.getOpenPrice();
        // 最低价
        BigDecimal lowestPrice = snapshot.getLowestPrice();
        // 昨日收盘价
        BigDecimal yesterdayClosePrice = sealStock.getLatestPrice();
        // 计算竞价开的点数
        BigDecimal gap = calChangeRate(yesterdayClosePrice, openPrice).multiply(BigDecimal.valueOf(100));
        // 竞价低开或平开不买
        if (gap.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal[] floatingPoBigDecimal;
        if (NumberUtil.isLess(gap, BigDecimal.valueOf(4))) {
            // 高开4个点之内
            floatingPoBigDecimal = new BigDecimal[]{BigDecimal.valueOf(0), gap};
        } else if (NumberUtil.isIn(gap, BigDecimal.valueOf(4), BigDecimal.valueOf(8))) {
            // 高开4-6个点之内
            floatingPoBigDecimal = new BigDecimal[]{BigDecimal.valueOf(0), gap};
        } else if (NumberUtil.isIn(gap, BigDecimal.valueOf(8), BigDecimal.valueOf(9.8))) {
            // 高开8个点以上
            floatingPoBigDecimal = new BigDecimal[]{BigDecimal.valueOf(2), BigDecimal.valueOf(7)};
        } else {
            // 一字开
            floatingPoBigDecimal = new BigDecimal[]{BigDecimal.valueOf(3), gap};
        }
        return calBuyPoints(yesterdayClosePrice, lowestPrice, floatingPoBigDecimal, gap);
    }

    private static Pair<List<BigDecimal>, List<BigDecimal>> calBuyPoints(BigDecimal yesterdayClosePrice, BigDecimal lowestPrice, BigDecimal[] floatingPoBigDecimals, BigDecimal gap) {
        List<BigDecimal> buyPriceList = new ArrayList<>();
        List<BigDecimal> buyPointList = new ArrayList<>();
        Pair<List<BigDecimal>, List<BigDecimal>> pair = Pair.of(buyPriceList, buyPointList);
        for (BigDecimal floatingPoBigDecimal : floatingPoBigDecimals) {
            BigDecimal changeRate = gap.subtract(floatingPoBigDecimal).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal buyPrice = calAfterPrice(yesterdayClosePrice, changeRate);
            if (NumberUtil.isGreaterOrEqual(buyPrice, lowestPrice)) { // 确保买点不低于最低价
                buyPriceList.add(buyPrice);
                buyPointList.add(changeRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
            } else {
                break; // 买点低于最低价，停止计算
            }
        }
        return pair;
    }

    private static Pair<BigDecimal, LocalDate> sell(String code, LocalDate sellDate) {
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
                return sell(code, nextSellDate);
            } else {
                return Pair.of(calAfterPrice(preClosePrice, BigDecimal.valueOf(0.09)), sellDate);
            }
        } else {
            // 非一字开
            return Pair.of(openPrice, sellDate);
        }
    }


}
