package com.example.trade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SnapshotCrawler {

    public static final String SNAPSHOT_FILE_NAME = "snapshot.xlsx";

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 16);
        Set<String> codes = DataCenter.listStockCode();
        Map<String, List<Snapshot>> existMap = RedisUtil.getMapList("snapshot", Snapshot.class);
        Set<String> existCodes = existMap.keySet();
        List<Snapshot> saveList = new ArrayList<>(codes.size() * 1000);
        codes = new HashSet<>(Sets.difference(codes, existCodes));
//        codes.removeIf(code -> MarketType.getMarketType(code) == MarketType.BEIJING_EXCHANGE);
        int i = 1;
        int errCount = 0;
        System.out.println("股票总数：" + codes.size());
        for (String code : codes) {
            Map<String, Object> params = new HashMap<>();
            params.put("symbol", code);
            params.put("period", "daily");
            params.put("start_date", startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            params.put("end_date", endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            // 默认返回不复权的数据; qfq: 返回前复权后的数据; hfq: 返回后复权后的数据
            params.put("adjust", "qfq");
            System.out.println("code " + code + " i " + i);
            String body = "";
            try {
                body = HttpUtil.get("http://127.0.0.1:8067/api/public/stock_zh_a_hist", params, 10 * 1000);                List<Snapshot> list = JSON.parseArray(body, Snapshot.class);
                saveList.addAll(list);
            } catch (Exception e) {
                if (errCount++ > 10) {
                    log.warn("数据加载异常次数超过限制");
                    break;
                }
                log.warn("code {} body:{} 数据加载异常", code, body, e);
                ThreadUtil.sleep(1000);
            }
            i++;
            System.out.println(i);
        }
        Map<String, List<Snapshot>> collect = saveList.stream().collect(Collectors.groupingBy(Snapshot::getCode));
        System.out.println("加载完成，开始存入redis，数据总数：" + saveList.size());
        RedisUtil.setMapList("snapshot", collect);
        System.out.println("完成存写，数据总数：" + saveList.size());
    }

    public static List<Snapshot> loadSnapshot(Set<String> codes) {
        List<Snapshot> list = Exec.read(SNAPSHOT_FILE_NAME, Snapshot.class);
        if (CollUtil.isEmpty(codes)) {
            return list;
        }
        return list.stream().
                filter(snapshot -> codes.contains(snapshot.getCode()))
                .toList();
    }

}
