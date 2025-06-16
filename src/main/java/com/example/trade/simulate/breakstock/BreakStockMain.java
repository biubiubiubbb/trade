package com.example.trade.simulate.breakstock;

import com.example.trade.DataCenter;
import com.example.trade.Exec;
import com.example.trade.profit.StatisticProfit;
import com.example.trade.simulate.breakstock.model.BreakStock;
import com.example.trade.simulate.breakstock.model.BreakStockStockProfit;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BreakStockMain {

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 11);
        BreakStockProfitCalculate calculator = new BreakStockProfitCalculate();
        List<BigDecimal> supportGaps = BreakStockDataCenter.getSupportGaps();
        for (BigDecimal gap : supportGaps) {
            Map<String, List<BreakStock>> breakStockMap = BreakStockDataCenter.getBreakStockList(gap);
            List<BreakStockStockProfit> allProfitList = new ArrayList<>();
            LocalDate date = endDate;
            log.info("gap {} date {} 开始计算", gap, date);
            while (date.isAfter(startDate) || date.isEqual(startDate)) {
                if (!DataCenter.isTradeDate(date)) {
                    date = date.minusDays(1);
                    continue;
                }
                List<BreakStock> breakStocks = breakStockMap.get(date.toString());
                List<BreakStockStockProfit> profits = calculator.calculateBreakStockProfit(breakStocks);
                if(profits.isEmpty()) {
                   continue;
                }
                allProfitList.addAll(profits);
                date = DataCenter.getPrevTradeDate(date);
            }
            List<StatisticProfit> statisticProfits = calculator.calMonthlyProfit(allProfitList);
            Exec.write(String.format("昨日上影线曾涨停-今日高开%s点-收益明细", gap), "", allProfitList, BreakStockStockProfit.class);
            Exec.write(String.format("昨日上影线曾涨停-今日高开%s点-收益统计", gap), "", statisticProfits, StatisticProfit.class);
        }
    }


}
