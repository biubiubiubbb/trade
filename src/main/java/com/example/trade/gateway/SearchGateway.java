package com.example.trade.gateway;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.example.trade.gateway.search.SearchKeywordBuilder;
import com.example.trade.gateway.search.SearchRequest;
import com.example.trade.gateway.search.SearchResponse;
import com.example.trade.gateway.search.SearchResultParser;
import com.example.trade.simulate.breakstock.model.BreakStock;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author xi.qin
 * @classname SearchGateway
 * @description 东方财富条件选股 <a href="https://xuangu.eastmoney.com/">...</a>
 * @date 2025/6/26 15:57
 */
@Slf4j
public class SearchGateway {

    private final String BASE_URL = "https://np-tjxg-g.eastmoney.com/api/smart-tag/stock/v3/pw/search-code";


    public List<BreakStock> searchBreakStock(LocalDate yesterday, LocalDate today, BigDecimal gap) {
        String keyword = SearchKeywordBuilder.buildKeyword(yesterday, today, gap);
        SearchRequest request = new SearchRequest();
        request.setKeyWord(keyword);
        String result = HttpUtil.post(BASE_URL, JSON.toJSONString(request));
        SearchResponse searchResponse = JSON.parseObject(result, SearchResponse.class);
        SearchResultParser processor = new SearchResultParser();
        return processor.processResponse(searchResponse, yesterday, today);
    }


}
