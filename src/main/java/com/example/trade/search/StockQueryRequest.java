package com.example.trade.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StockQueryRequest {
    private String keyWord;
    private int pageSize = 100;
    private int pageNo = 1;
    private String fingerprint = "fbecf580b089771106aa74c9df06592d";
    private List<String> gids = new ArrayList<>();
    private String matchWord;
    private String timestamp = String.valueOf(System.currentTimeMillis());
    private boolean shareToGuba = false;
    private String requestId = "FH4tzDoURPAHELzo0JzUY4NOPK5Fm9Kl1750000445011";
    private boolean needCorrect = true;
    private List<String> removedConditionIdList = new ArrayList<>();
    private String xcId = "xc0ab0277e8693010052";
    private boolean ownSelectAll = false;
    private String extraCondition;
}