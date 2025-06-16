package com.example.trade.search;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StockQueryResponse {
    private String code;
    private String msg;
    private ResponseData data;
    private String traceId;
    private String quoteTraceId;
    private TraceInfo traceInfo;
    private Boolean enableRefresh;
    private Boolean hiddenDrawLine;
    private CorrectRes correctRes;
    private Object keywordInfo;
    private String extraCondition;
    private String ctraceId;

    @Data
    public static class ResponseData {
        private Object resultType;
        private Object matchable;
        private Result result;
        private List<String> codes;
        private Object ownSelectAble;
        private List<ResponseCondition> responseConditionList;
        private Object dynamicGroupCHG;
        private Object userSelectCount;
        private Object isNotLoggedInForOwnSelect;
        private Object domain;
        private String parserTreeMd5;
        private String searchTreeMd5;
        private String xcId;
        private Object ownSelectAllCodes;
    }

    @Data
    public static class Result {
        private List<Column> columns;
        private List<Map<String, Object>> dataList;
        private List<Object> diverseList;
        private List<Object> linkList;
        private Meta meta;
        private String latestDate;
        private Integer total;
    }

    @Data
    public static class Column {
        private String title;
        private String key;
        private String dateMsg;
        private Boolean sortable;
        private Boolean light;
        private String sortWay;
        private String indexName;
        private Boolean redGreenAble;
        private String unit;
        private Integer userNeed;
        private Boolean mtmKey;
        private String dataType;
        private Boolean resCacheNeed;
        private Boolean quoteJumpNeed;
        private Boolean reportTimeHighLight;
        private Object showType;
        private Object optKlp;
        private Object mplcType;
        private Boolean hiddenNeed;
        private List<Column> children;
        private String linkDisplayWay;
    }

    @Data
    public static class Meta {
        private Map<String, DisplayConfig> columnInfo;
    }

    @Data
    public static class DisplayConfig {
        private Integer widthFlag;
        private Object quoteRelatedField;
    }

    @Data
    public static class ResponseCondition {
        private String describe;
        private Integer stockCount;
        private List<Integer> childrenIdList;
        private Integer resultIndex;
        private Integer conditionId;
        private List<Integer> sameIdList;
        private Boolean isValid;
        private Boolean removable;
        private Object traceInfo;
        private Object drawLineNode;
    }

    @Data
    public static class TraceInfo {
        private Integer conditionId;
        private String showText;
        private Object traceId;
        private Object traceText;
        private Object tmpl;
        private List<ChildrenInfo> childrenInfo;
        private String etext;
        private Object dtext;
    }

    @Data
    public static class ChildrenInfo {
        private Integer conditionId;
        private String showText;
        private Integer traceId;
        private String traceText;
        private Object tmpl;
        private Object childrenInfo;
        private String etext;
        private Object dtext;
    }

    @Data
    public static class CorrectRes {
        private Boolean existError;
        private Object correctedText;
        private Object editDetails;
        private Integer code;
        private Boolean needCorrect;
        private Integer success;
        private Integer fail;
    }
}
