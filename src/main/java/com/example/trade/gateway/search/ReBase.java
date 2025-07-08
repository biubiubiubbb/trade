package com.example.trade.gateway.search;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.example.trade.DataCenter;
import com.example.trade.RedisUtil;
import com.example.trade.simulate.breakstock.model.BreakStock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xi.qin
 * @classname ReBase
 * @description TODO
 * @date 2025/7/8 21:08
 */
public class ReBase {

    private static final String BREAK_STOCK_REDIS_KEY_PREFIX = "断板反包-stock";


    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2024, 7, 1);
        LocalDate endDate = LocalDate.of(2025, 7, 7);
        LocalDate date = startDate;
        Map<String, List<BreakStock>> map = new HashMap<>();
        while (date.isBefore(endDate) || date.isEqual(endDate)) {
            if (!DataCenter.isTradeDate(date)) {
                date = date.plusDays(1);
                continue;
            }
            LocalDate preTradeDate = DataCenter.getPrevTradeDate(date);
            List<BreakStock> breakStock = getBreakStock(preTradeDate, date);
            map.put(date.toString(), breakStock);
            date = DataCenter.getNextTradeDate(date);
        }
        String redisKey = BREAK_STOCK_REDIS_KEY_PREFIX;
        RedisUtil.deleteKey(redisKey);
        RedisUtil.setMapList(redisKey, map);
    }

    private static List<BreakStock> getBreakStock(LocalDate yesterday, LocalDate today) {
        String url = "https://np-tjxg-g.eastmoney.com/api/smart-tag/stock/v3/pw/search-code";
        String keyword = SearchKeywordBuilder.buildKeywordV3(yesterday, today);
        SearchRequest request = new SearchRequest();
        request.setKeyWord(keyword);
        System.out.println(keyword);
        String post = HttpUtil.post(url, JSON.toJSONString(request));
        SearchResponse searchResponse = JSON.parseObject(post, SearchResponse.class);
        SearchResultParser processor = new SearchResultParser();
        return processor.processResponse(searchResponse, yesterday, today);
    }

}
