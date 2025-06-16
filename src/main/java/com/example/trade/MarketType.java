package com.example.trade;

public enum MarketType {
    MAIN_BOARD("主板"),       // 主板（沪市主板 + 深市主板）
    CHINEXT("创业板"),        // 创业板
    SCIENCE_INNOVATION("科创板"), // 科创板
    BEIJING_EXCHANGE("北交所"),   // 北京证券交易所
    UNKNOWN("未知");           // 未知市场

    private final String description;

    MarketType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据股票代码判断所属板块
     *
     * @param stockCode 股票代码
     * @return 所属板块的枚举类型
     */
    public static MarketType getMarketType(String stockCode) {
        if (stockCode == null || stockCode.length() < 6) {
            return MarketType.UNKNOWN;
        }

        // 获取股票代码的前几位数字
        String prefix = stockCode.substring(0, 3);

        // 判断所属板块
        if (prefix.startsWith("60") ||
                prefix.startsWith("000") ||
                prefix.startsWith("001") ||
                prefix.startsWith("002") ||
                prefix.startsWith("003")) {
            return MarketType.MAIN_BOARD; // 主板
        } else if (prefix.startsWith("30")) {
            return MarketType.CHINEXT; // 创业板
        } else if (prefix.startsWith("68")) {
            return MarketType.SCIENCE_INNOVATION; // 科创板
        } else {
            return MarketType.BEIJING_EXCHANGE; // 北交所
        }
    }

}
