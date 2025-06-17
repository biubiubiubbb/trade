package com.example.trade.simulate.breakstock;

import com.example.trade.RedisUtil;
import com.example.trade.simulate.breakstock.model.BreakStock;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class BreakStockDataCenter {

    private static final String BREAK_STOCK_REDIS_KEY_PREFIX = "break-stock-gap-";

    /**
     * 支持的高开的点位
     */
    public static List<BigDecimal> getSupportGaps() {
        List<BigDecimal> gapList = new ArrayList<>();
        gapList.add(BigDecimal.valueOf(0.1));
//        gapList.add(BigDecimal.valueOf(0.5));
//        gapList.add(BigDecimal.valueOf(1));
//        gapList.add(BigDecimal.valueOf(2));
//        gapList.add(BigDecimal.valueOf(3));
//        gapList.add(BigDecimal.valueOf(4));
//        gapList.add(BigDecimal.valueOf(5));
        return gapList;
    }

    /**
     * 获取股票数据
     *
     * @param gap 高开的点数
     * @return key 为交易日 yyyy-MM-dd，value 为符合条件的数据
     */
    public static Map<String, List<BreakStock>> getBreakStockList(BigDecimal gap) {
        return RedisUtil.getMapList(buildRedisKey(gap), BreakStock.class);
    }


    private static String buildRedisKey(BigDecimal gap) {
        return BREAK_STOCK_REDIS_KEY_PREFIX + gap;
    }

}
