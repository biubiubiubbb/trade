package com.example.trade;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashBasedTable;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DataCenter {

    public static final List<LocalDate> LOCAL_DATE_LIST;

    public static final HashBasedTable<String, LocalDate, Snapshot> SNAPSHOT_TABLE;

    public static final Map<LocalDate, List<SealStock>> SEAL_STOCK_MAP;

    static {
        LOCAL_DATE_LIST = listTradeDate();
        SNAPSHOT_TABLE = HashBasedTable.create();
        Map<String, List<Snapshot>> snapshotMap = RedisUtil.getMapList("snapshot", Snapshot.class);
        for (String date : snapshotMap.keySet()) {
            List<Snapshot> snapshots = snapshotMap.get(date);
            for (Snapshot snapshot : snapshots) {
                SNAPSHOT_TABLE.put(snapshot.getCode(), snapshot.getDate(), snapshot);
            }
        }
        log.info("加载涨停数据开始");
        List<SealStock> sealStocks = SealStockCrawler.loadSealStock(null, null);
        log.info("加载涨停数据完成。size:{}", sealStocks.size());
        SEAL_STOCK_MAP = sealStocks.stream()
                // 过滤脏数据
                .filter(sealStock -> sealStock.getLatestPrice().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.groupingBy(SealStock::getTradeDate));
    }

    private static List<LocalDate> listTradeDate() {
        String body = HttpUtil.get("http://127.0.0.1:8067/api/public/tool_trade_date_hist_sina");
        List<JSONObject> jsonObjectList = JSON.parseArray(body, JSONObject.class);
        List<LocalDate> tradeDates = new ArrayList<>();
        for (JSONObject jsonObject : jsonObjectList) {
            String tradeDateStr = jsonObject.getString("trade_date");
            LocalDate tradeDate = LocalDate.parse(tradeDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            tradeDates.add(tradeDate);
        }
        tradeDates.sort(LocalDate::compareTo);
        return tradeDates;
    }

    /**
     * 获取快照数据
     *
     * @param code              股票代码
     * @param date              日期
     * @param getNextIfNotExist 如果没有数据，是否尝试获取上/下一个交易日的数据 null 不获取，true 下一个交易日，false 上一个交易日
     */
    public static Snapshot getSnapshot(String code, LocalDate date, Boolean getNextIfNotExist) {
        Snapshot snapshot = SNAPSHOT_TABLE.get(code, date);
        if (snapshot != null) {
            return snapshot;
        }
        if (date.isAfter(LocalDate.now())) {
            return null;
        }
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Map<String, Object> params = new HashMap<>();
        params.put("symbol", code);
        params.put("period", "daily");
        params.put("start_date", dateStr);
        params.put("end_date", dateStr);
        params.put("adjust", "qfq");
        String body = HttpUtil.get("http://127.0.0.1:8067/api/public/stock_zh_a_hist", params);
        if (!JSONUtil.isTypeJSONArray(body)) {
            log.error("code {} date {} body {} 数据加载异常", code, date, body);
            return null;
        }
        List<Snapshot> list = JSON.parseArray(body, Snapshot.class);
        if (list.isEmpty()) {
            if (getNextIfNotExist == null) {
                log.error("code {} date {} body {} 数据不存在", code, date, body);
                return null;
            } else if (getNextIfNotExist) {
                LocalDate nextTradeDate = getNextTradeDate(date);
                log.error("code {} date {} body {} 数据不存在 {} 尝试获取下一个交易日数据", code, date, body, nextTradeDate);
                return getSnapshot(code, nextTradeDate, getNextIfNotExist);
            } else {
                LocalDate prevTradeDate = getPrevTradeDate(date);
                log.error("code {} date {} body {} 数据不存在 {} 尝试获取上一个交易日数据", code, date, body, prevTradeDate);
                return getSnapshot(code, prevTradeDate, getNextIfNotExist);
            }
        }
        snapshot = list.get(0);
        SNAPSHOT_TABLE.put(code, date, snapshot);
        return snapshot;
    }


    public static Set<String> listStockCode() {
        String body = HttpUtil.get("http://127.0.0.1:8067/api/public/stock_info_a_code_name");
        List<JSONObject> jsonObjectList = JSON.parseArray(body, JSONObject.class);
        Set<String> codes = new HashSet<>();
        for (JSONObject jsonObject : jsonObjectList) {
            String code = jsonObject.getString("code");
            codes.add(code);
        }
        return codes;
    }

    public static List<SealStock> listSealStock(LocalDate date) {
        List<SealStock> sealStockList = SEAL_STOCK_MAP.get(date);
        if (sealStockList != null) {
            return sealStockList;
        } else {
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Map<String, Object> params = new HashMap<>();
            params.put("date", dateStr);
            String body = HttpUtil.get("http://127.0.0.1:8067/api/public/stock_zt_pool_em", params);
            List<SealStock> list = JSON.parseArray(body, SealStock.class);
            for (SealStock sealStock : list) {
                sealStock.setTradeDate(date);
            }
            return list;
        }
    }

    public static LocalDate getNextTradeDate(LocalDate tradeDate) {
        int index = LOCAL_DATE_LIST.indexOf(tradeDate);
        return LOCAL_DATE_LIST.get(index + 1);
    }

    public static LocalDate getPrevTradeDate(LocalDate tradeDate) {
        int index = LOCAL_DATE_LIST.indexOf(tradeDate);
        return LOCAL_DATE_LIST.get(index - 1);
    }

    public static LocalDate getPrevTradeDate(LocalDate tradeDate, int pre) {
        int index = LOCAL_DATE_LIST.indexOf(tradeDate);
        return LOCAL_DATE_LIST.get(index - pre);
    }


    public static boolean isTradeDate(LocalDate date) {
        return LOCAL_DATE_LIST.contains(date);
    }

    public static int getHoldDays(LocalDate startDate, LocalDate endDate) {
        return LOCAL_DATE_LIST.indexOf(endDate) - LOCAL_DATE_LIST.indexOf(startDate) + 1;
    }
}
