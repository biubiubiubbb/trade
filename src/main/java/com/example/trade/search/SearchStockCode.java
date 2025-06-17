package com.example.trade.search;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.example.trade.DataCenter;
import com.example.trade.RedisUtil;
import com.example.trade.simulate.breakstock.BreakStockDataCenter;
import com.example.trade.simulate.breakstock.model.BreakStock;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SearchStockCode {

    private static final String BREAK_STOCK_REDIS_KEY_PREFIX = "break-stock-gap-";

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2025, 6, 10);
        LocalDate endDate = LocalDate.of(2025, 6, 10);
        // 高开的点数
        List<BigDecimal> gapList = BreakStockDataCenter.getSupportGaps();
        LocalDate date = startDate;
        for (BigDecimal gap : gapList) {
            Map<String, List<BreakStock>> map = new HashMap<>();
            while (date.isBefore(endDate) || date.isEqual(endDate)) {
                if (!DataCenter.isTradeDate(date)) {
                    date = date.plusDays(1);
                    continue;
                }
                LocalDate preTradeDate = DataCenter.getPrevTradeDate(date);
                List<BreakStock> breakStock = getBreakStock(preTradeDate, date, gap);
                map.put(date.toString(), breakStock);
                date = DataCenter.getNextTradeDate(date);
            }
            String redisKey = BREAK_STOCK_REDIS_KEY_PREFIX + gap;
//            RedisUtil.setMapList(redisKey, map);
            date = startDate;
            log.info("gap {} 加载结束", gap);
        }
    }


    private static List<BreakStock> getBreakStock(LocalDate yesterday, LocalDate today, BigDecimal gap) {
        String url = "https://np-tjxg-g.eastmoney.com/api/smart-tag/stock/v3/pw/search-code";
        String keyword = StockKeywordBuilder.buildKeyword(yesterday, today, gap);
        StockQueryRequest request = new StockQueryRequest();
        request.setKeyWord(keyword);
        System.out.println(keyword);
        String post = HttpUtil.post(url, JSON.toJSONString(request));
        StockQueryResponse stockQueryResponse = JSON.parseObject(post, StockQueryResponse.class);
        StockDataProcessor processor = new StockDataProcessor();
        return processor.processResponse(stockQueryResponse, yesterday, today);
    }

}
