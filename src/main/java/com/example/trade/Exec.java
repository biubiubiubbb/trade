package com.example.trade;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.trade.profit.StatisticProfit;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Exec {

    public static void main(String[] args) {
        List<StatisticProfit> list = new ArrayList<>();
        StatisticProfit statisticProfits = new StatisticProfit();
        statisticProfits.setMonth("2025-05");
        list.add(statisticProfits);
        Exec.write("昨日上影线曾涨停-今日高开.xlsx", "高开1个点以上", list, StatisticProfit.class);
        StatisticProfit statisticProfits2 = new StatisticProfit();
        statisticProfits.setMonth("2025-06");
        list.add(statisticProfits2);
        Exec.write("昨日上影线曾涨停-今日高开.xlsx", "高开2个点以上", list, StatisticProfit.class);
    }

    public static <R> List<R> exec(Function<LocalDate, List<R>> function, LocalDate startDate, LocalDate endDate) {
        List<R> list = new ArrayList<>();
        LocalDate date = startDate;
        while (date.isBefore(endDate) || date.isEqual(endDate)) {
            if (!DataCenter.isTradeDate(date)) {
                date = date.plusDays(1);
            } else {
                List<R> apply = function.apply(date);
                list.addAll(apply);
                date = DataCenter.getNextTradeDate(date);
            }
        }
        return list;
    }

    public static <T> void write(String fileName, String sheetName, List<T> dataList, Class<T> clazz) {
        File file = new File(fileName);
        if(StringUtils.isEmpty(sheetName)) {
            sheetName = "Sheet1";
        }
        if (file.exists()) {
            // 如果文件已存在，追加数据
            try (ExcelWriter excelWriter = EasyExcel.write(fileName, clazz).build()) {
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).head(clazz).build();
                excelWriter.write(dataList, writeSheet);
            }
        } else {
            // 如果文件不存在，直接写入数据
            EasyExcel.write(fileName, clazz)
                    .head(clazz)
                    .sheet(sheetName)
                    .doWrite(dataList);
        }
    }

    public static <T> void write(String fileName, List<String> sheetNameList, List<List<T>> dataList, Class<T> clazz) {
        File file = new File(fileName);
        if (file.exists()) {
            // 如果文件已存在，追加数据
            try (ExcelWriter excelWriter = EasyExcel.write(fileName, clazz).build()) {
                for (int i = 0; i < sheetNameList.size(); i++) {
                    WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetNameList.get(i)).build();
                    excelWriter.write(dataList.get(i), writeSheet);
                }
            }
        } else {
            try (ExcelWriter excelWriter = EasyExcel.write(fileName, clazz).build()) {
                for (int i = 0; i < sheetNameList.size(); i++) {
                    WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetNameList.get(i)).build();
                    excelWriter.write(dataList.get(i), writeSheet);
                }
            }
        }
    }

    public static <T> List<T> read(String fileName, Class<T> clazz) {
        File file = new File(fileName);
        if (file.exists()) {
            List<T> list = new ArrayList<>();
            EasyExcel.read(fileName, clazz, new PageReadListener<T>(list::addAll))
                    .sheet()
                    .doRead();
            return list;
        } else {
            return Collections.emptyList();
        }
    }
}
