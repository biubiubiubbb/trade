package com.example.trade.search;

import com.example.trade.simulate.breakstock.model.BreakStock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class StockDataProcessor {

    public List<BreakStock> processResponse(StockQueryResponse response, LocalDate yesterday, LocalDate today) {
        return response.getData().getResult().getDataList().stream()
                .map(item->convertToStockItem(item, yesterday, today))
                .toList();
    }

    private BreakStock convertToStockItem(Map<String, Object> rawData, LocalDate yesterday, LocalDate today) {
        BreakStock item = new BreakStock();
        item.setCode((String) rawData.get("SECURITY_CODE"));
        item.setName((String) rawData.get("SECURITY_SHORT_NAME"));
        item.setCirculationMarketValue((String) rawData.get("CIRCULATION_MARKET_VALUE"));
        // 集合竞价涨跌幅
        item.setCallAuctionChg(parseBigDecimal((rawData.get(String.format("JHJJZDF{%s}", today)))).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        // 今日竞价金额
        item.setCallAuctionTradeAmount((String) rawData.get(String.format("CALL_AUCTION_VOLUMES{%s}", today)));
        // 昨日竞价金额
        item.setCallAuctionTradeAmountPrev((String) rawData.get(String.format("CALL_AUCTION_VOLUMES{%s}", yesterday)));
        item.setTradeDate(today);
        item.setTradeDatePrev(yesterday);
        return item;
    }

    // Improved parsing methods with BigDecimal support
    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;

        try {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            if (value instanceof String) {
                String strValue = ((String) value)
                        .replace("亿", "")
                        .replace("万", "")
                        .replace("%", "")
                        .replace("元", "")
                        .trim();
                return new BigDecimal(strValue);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
}
