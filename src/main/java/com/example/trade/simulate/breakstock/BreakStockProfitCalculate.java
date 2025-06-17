package com.example.trade.simulate.breakstock;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.NumberUtil;
import com.example.trade.DataCenter;
import com.example.trade.Snapshot;
import com.example.trade.simulate.ProfitCalculate;
import com.example.trade.simulate.TradeType;
import com.example.trade.simulate.TradingPoint;
import com.example.trade.simulate.TradingResult;
import com.example.trade.simulate.breakstock.model.BreakStock;
import com.example.trade.simulate.breakstock.model.BreakStockStockProfit;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BreakStockProfitCalculate extends ProfitCalculate {

    public List<BreakStockStockProfit> calculateBreakStockProfit(List<BreakStock> breakStockList) {
        List<BreakStockStockProfit> profits = new ArrayList<>();
        for (BreakStock breakStock : breakStockList) {
            BreakStockStockProfit profit = this.calculateOne(breakStock);
            if(profit == null) {
                continue;
            }
            profits.add(profit);
        }
        return profits;
    }

    private BreakStockStockProfit calculateOne(BreakStock breakStock) {
        BreakStockStockProfit profit = new BreakStockStockProfit();
        // 基础信息
        BigDecimal gap = breakStock.getCallAuctionChg().multiply(BigDecimal.valueOf(100));
        profit.setCode(breakStock.getCode());
        profit.setName(breakStock.getName());
        profit.setBuyDate(breakStock.getTradeDate());
        profit.setGap(gap);
        profit.setCallAuctionTradeAmount(breakStock.getCallAuctionTradeAmount());
        profit.setCallAuctionTradeAmountPrev(breakStock.getCallAuctionTradeAmountPrev());

        // 交易信息
        TradingResult tradingResult = calculateTradingResult(breakStock.getCode(), breakStock.getTradeDate(), gap);
        if (tradingResult == null) {
            profit.setRemark("未达到买入条件");
            return null;
        }
        profit.setProfit(tradingResult.getProfit());
        profit.setBuyAmount(tradingResult.getBuyAmount());
        profit.setBuyAvgPrice(tradingResult.getBuyAvgPrice());
        profit.setBuyPrices(tradingResult.getBuyPrices());
        profit.setSellAmount(tradingResult.getSellAmount());
        profit.setSellAvgPrice(tradingResult.getSellAvgPrice());
        profit.setSellPoints(tradingResult.getSellPoints());
        profit.setSellPrices(tradingResult.getSellPrices());
        profit.setHoldingDays(tradingResult.getHoldingDays());
        profit.setProfitRate(tradingResult.getProfitRate());
        profit.setBuyPoints(tradingResult.getBuyPoints());
        profit.setSellDate(tradingResult.getSellDate());
        log.info("计算结果：{}", tradingResult);
        return profit;
    }


    private TradingResult calculateTradingResult(String code, LocalDate tradeDate, BigDecimal gap) {
        if (gap.compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }
        TradingResult result = new TradingResult();
        List<TradingPoint> buyPoints = new ArrayList<>();
        result.setBuyPoints(buyPoints);

        // 第一天只能买
        Snapshot firstSnapshot = DataCenter.getSnapshot(code, tradeDate, null);
        if (firstSnapshot == null) {
            return null;
        }
        Snapshot firstSnapshotPrev = DataCenter.getSnapshot(code, DataCenter.getPrevTradeDate(tradeDate), false);
        if (firstSnapshotPrev == null) {
            return null;
        }

        BigDecimal[] floatingPoBigDecimals;
        if (NumberUtil.isLess(gap, BigDecimal.valueOf(2))) {
            // 高开2个点之内
            floatingPoBigDecimals = new BigDecimal[]{BigDecimal.valueOf(2), BigDecimal.valueOf(4)};
        } else if (NumberUtil.isLess(gap, BigDecimal.valueOf(4))) {
            // 高开4个点之内
            floatingPoBigDecimals = new BigDecimal[]{BigDecimal.valueOf(3), BigDecimal.valueOf(5)};
        } else if (NumberUtil.isLess(gap, BigDecimal.valueOf(8))) {
            // 高开4-8个点之内
            floatingPoBigDecimals = new BigDecimal[]{BigDecimal.valueOf(3.5), gap};
        } else if (NumberUtil.isIn(gap, BigDecimal.valueOf(8), BigDecimal.valueOf(9.8))) {
            // 高开8个点到9.8以上
            floatingPoBigDecimals = new BigDecimal[]{BigDecimal.valueOf(5), gap};
        } else {
            // 一字开
            floatingPoBigDecimals = new BigDecimal[]{BigDecimal.valueOf(4), gap};
        }
        for (BigDecimal floatingPoBigDecimal : floatingPoBigDecimals) {
            BigDecimal changeRate = gap.subtract(floatingPoBigDecimal).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal buyPrice = calAfterPrice(firstSnapshotPrev.getClosePrice(), changeRate);
            if (NumberUtil.isGreaterOrEqual(buyPrice, firstSnapshot.getLowestPrice())) {
                // 确保买点不低于最低价
                BigDecimal quantity = calBuyQuantity(buyPrice);
                BigDecimal point = changeRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                TradingPoint tradingPoint = TradingPoint.builder()
                        .price(buyPrice)
                        .point(point)
                        .quantity(quantity)
                        .amount(calBuyAmount(Lists.newArrayList(buyPrice), Lists.newArrayList(quantity)))
                        .tradeType(TradeType.BUY)
                        .tradeDate(tradeDate)
                        .build();
                buyPoints.add(tradingPoint);
            } else {
                break; // 买点低于最低价，停止买入
            }
        }
        // 第一天没有买入，则返回null
        if (buyPoints.isEmpty()) {
            return null;
        }
        // 第二天和第三天 - 可以买或卖
        for (int day = 1; day <= 2; day++) {
            tradeDate = DataCenter.getNextTradeDate(tradeDate);
            Snapshot snapshot = DataCenter.getSnapshot(code, tradeDate, true);
            if (snapshot == null) {
                return null;
            }
            Pair<BigDecimal, LocalDate> sellPair = trySell(code, result.getBuyAvgPrice(), tradeDate);
            if (sellPair != null) {
                // 符合卖出条件，结束
                TradingPoint sellPoint = getSellPoint(result, sellPair);
                result.setSellPoint(sellPoint);
                return result;
            } else {
                // 不符合卖出，尾盘若跌超过3个点则买入
                if (snapshot.getChangeRate().compareTo(BigDecimal.valueOf(-3)) <= 0) {
                    BigDecimal buyPrice = snapshot.getClosePrice();
                    BigDecimal quantity = calBuyQuantity(buyPrice);
                    BigDecimal point = snapshot.getChangeRate();
                    TradingPoint tradingPoint = TradingPoint.builder()
                            .price(buyPrice)
                            .point(point)
                            .quantity(quantity)
                            .amount(calBuyAmount(Lists.newArrayList(buyPrice), Lists.newArrayList(quantity)))
                            .tradeType(TradeType.BUY)
                            .tradeDate(tradeDate)
                            .build();
                    buyPoints.add(tradingPoint);
                }
            }
        }
        // 第四天和第五天 - 只能卖
        for (int day = 3; day <= 4; day++) {
            tradeDate = DataCenter.getNextTradeDate(tradeDate);
            Pair<BigDecimal, LocalDate> sellPair = trySell(code, result.getBuyAvgPrice(), tradeDate);
            if (sellPair != null) {
                TradingPoint sellPoint = getSellPoint(result, sellPair);
                result.setSellPoint(sellPoint);
                return result;
            }
        }
        Snapshot lastSnapshot = DataCenter.getSnapshot(code, tradeDate, true);
        if (lastSnapshot == null) {
            return null;
        }
        // 持仓第五天，依然亏损，尾盘卖出
        Pair<BigDecimal, LocalDate> sellPair = Pair.of(lastSnapshot.getClosePrice(), lastSnapshot.getDate());
        TradingPoint sellPoint = getSellPoint(result, sellPair);
        result.setSellPoint(sellPoint);
        return result;
    }

    private TradingPoint getSellPoint(TradingResult result, Pair<BigDecimal, LocalDate> sellPair) {
        BigDecimal sellPrice = sellPair.getKey();
        BigDecimal quantity = result.getBuyQuantity();
        LocalDate tradeDate = sellPair.getValue();
        return TradingPoint.builder()
                .price(sellPrice)
                .point(null)
                .quantity(quantity)
                .amount(calBuyAmount(Lists.newArrayList(sellPrice), Lists.newArrayList(quantity)))
                .tradeType(TradeType.SELL)
                .tradeDate(tradeDate)
                .build();
    }
}
