package com.example.trade;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class SnapshotCrawler {

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 17);
        Set<String> codes = DataCenter.listStockCode();
        Map<String, List<Snapshot>> existMap = RedisUtil.getMapList("snapshot", Snapshot.class);
        Collection<List<Snapshot>> values = existMap.values();
        Set<String> existCodes = new HashSet<>();
        for (List<Snapshot> value : values) {
            existCodes.addAll(value.stream().map(Snapshot::getCode).collect(Collectors.toSet()));
        }
        codes = new HashSet<>(Sets.difference(codes, existCodes));
        AtomicInteger i = new AtomicInteger(1);
        AtomicInteger errCount = new AtomicInteger();
        System.out.println("股票总数：" + codes.size());
        List<Snapshot> saveList = Exec.runAsync(code -> {
            List<Snapshot> list = new ArrayList<>();
            if (errCount.get() > 100) {
                return list;
            }
            Map<String, Object> params = new HashMap<>();
            params.put("symbol", code);
            params.put("period", "daily");
            params.put("start_date", startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            params.put("end_date", endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            // 默认返回不复权的数据; qfq: 返回前复权后的数据; hfq: 返回后复权后的数据
            params.put("adjust", "qfq");
            System.out.println("code " + code + " i " + i.getAndIncrement());
            String body = "";
            try {
                body = HttpUtil.get("http://127.0.0.1:8067/api/public/stock_zh_a_hist", params, 10 * 1000);
                list = JSON.parseArray(body, Snapshot.class);
            } catch (Exception e) {
                if (errCount.getAndIncrement() > 100) {
                    log.warn("数据加载异常次数超过限制");
                    return list;
                }
                log.warn("code {} body:{} 数据加载异常", code, body, e);
                ThreadUtil.sleep(1000);
            }
            return list;
        }, codes);
        Map<String, List<Snapshot>> collect = saveList.stream().collect(Collectors.groupingBy(o -> o.getDate().toString()));
        System.out.println("加载完成，开始存入redis，数据总数：" + saveList.size());
        for (String date : collect.keySet()) {
            RedisUtil.setMapList("snapshot", date, collect.get(date));
        }
        System.out.println("完成存写，数据总数：" + saveList.size());
    }

}
