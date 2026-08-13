package com.easy.ai.utils;

import com.easy.ai.config.ExpressConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kuaidi100.sdk.api.QueryTrack;
import com.kuaidi100.sdk.core.IBaseClient;
import com.kuaidi100.sdk.pojo.HttpResult;
import com.kuaidi100.sdk.request.QueryTrackParam;
import com.kuaidi100.sdk.request.QueryTrackReq;
import com.kuaidi100.sdk.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class ExpressService {

    @Autowired
    private ExpressConfig expressConfig;

    private final Gson gson = new Gson();

    private static final Map<String, double[]> CITY_COORDS = new LinkedHashMap<>();
    static {
        CITY_COORDS.put("北京", new double[]{116.4074, 39.9042});
        CITY_COORDS.put("上海", new double[]{121.4737, 31.2304});
        CITY_COORDS.put("广州", new double[]{113.2644, 23.1292});
        CITY_COORDS.put("深圳", new double[]{114.0579, 22.5431});
        CITY_COORDS.put("杭州", new double[]{120.1551, 30.2741});
        CITY_COORDS.put("武汉", new double[]{114.3054, 30.5931});
        CITY_COORDS.put("成都", new double[]{104.0665, 30.5728});
        CITY_COORDS.put("南京", new double[]{118.7969, 32.0603});
        CITY_COORDS.put("重庆", new double[]{106.5516, 29.5630});
        CITY_COORDS.put("长沙", new double[]{112.9388, 28.2278});
        CITY_COORDS.put("郑州", new double[]{113.6254, 34.7466});
        CITY_COORDS.put("石家庄", new double[]{114.5149, 38.0428});
        CITY_COORDS.put("东莞", new double[]{113.7518, 23.0205});
        CITY_COORDS.put("天津", new double[]{117.1902, 39.1252});
        CITY_COORDS.put("济南", new double[]{117.1205, 36.6510});
        CITY_COORDS.put("西安", new double[]{108.9402, 34.3416});
        CITY_COORDS.put("昆明", new double[]{102.8329, 24.8801});
        CITY_COORDS.put("沈阳", new double[]{123.4315, 41.8057});
        CITY_COORDS.put("合肥", new double[]{117.2272, 31.8206});
        CITY_COORDS.put("福州", new double[]{119.2965, 26.0745});
        CITY_COORDS.put("厦门", new double[]{118.0894, 24.4798});
        CITY_COORDS.put("苏州", new double[]{120.5853, 31.2989});
        CITY_COORDS.put("南昌", new double[]{115.8579, 28.6820});
        CITY_COORDS.put("贵阳", new double[]{106.6302, 26.6477});
        CITY_COORDS.put("南宁", new double[]{108.3661, 22.8170});
        CITY_COORDS.put("海口", new double[]{110.1999, 20.0440});
        CITY_COORDS.put("佛山", new double[]{113.1219, 23.0218});
        CITY_COORDS.put("太原", new double[]{112.5489, 37.8706});
        CITY_COORDS.put("长春", new double[]{125.3235, 43.8171});
        CITY_COORDS.put("哈尔滨", new double[]{126.5350, 45.8023});
        CITY_COORDS.put("兰州", new double[]{103.8343, 36.0611});
        CITY_COORDS.put("乌鲁木齐", new double[]{87.6168, 43.8256});
    }

    public Map<String, Object> queryLogistics(String expressNo, String expressCompany, String phone) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("expressNo", expressNo);

        if (expressCompany == null || expressCompany.isEmpty()) {
            expressCompany = "zhongtong";
        }
        result.put("company", expressCompany);
        result.put("companyName", companyCodeToName(expressCompany));

        try {
            String customer = expressConfig.getCustomer();
            String key = expressConfig.getKey();

            QueryTrackParam trackParam = new QueryTrackParam();
            trackParam.setCom(expressCompany.toLowerCase());
            trackParam.setNum(expressNo);
            trackParam.setResultv2("4");

            String lowerCompany = expressCompany.toLowerCase();
            if ("zhongtong".equals(lowerCompany) || "zto".equals(lowerCompany)
                    || "shunfeng".equals(lowerCompany) || "sf".equals(lowerCompany)) {
                String queryPhone = (phone != null && !phone.isEmpty()) ? phone : "18382274361";
                trackParam.setPhone(queryPhone);
            }

            String param = gson.toJson(trackParam);

            QueryTrackReq queryTrackReq = new QueryTrackReq();
            queryTrackReq.setParam(param);
            queryTrackReq.setCustomer(customer);
            queryTrackReq.setSign(SignUtils.querySign(param, key, customer));

            log.info("快递100请求 param={}", param);

            IBaseClient baseClient = new QueryTrack();
            HttpResult httpResult = baseClient.execute(queryTrackReq);

            log.info("快递100响应 status={}, body={}", httpResult.getStatus(), httpResult.getBody());

            if (httpResult.getStatus() == 200 && httpResult.getBody() != null) {
                JsonObject root = JsonParser.parseString(httpResult.getBody()).getAsJsonObject();

                boolean apiFailed = root.has("result") && !root.get("result").getAsBoolean();

                if (apiFailed) {
                    String returnCode = root.has("returnCode") ? root.get("returnCode").getAsString() : "";
                    String message = root.has("message") ? root.get("message").getAsString() : "";
                    log.warn("快递100返回失败: returnCode={}, message={}, 降级演示数据", returnCode, message);
                    return buildDemoResult(expressNo, expressCompany);
                }

                if (!root.has("data") || root.get("data").isJsonNull()) {
                    log.warn("快递100返回无物流数据, 降级演示数据");
                    return buildDemoResult(expressNo, expressCompany);
                }

                String state = root.has("state") ? root.get("state").getAsString() : "0";
                result.put("state", state);
                result.put("statusText", stateToText(state));

                List<Map<String, Object>> traces = new ArrayList<>();
                if (root.has("data") && root.get("data").isJsonArray()) {
                    JsonArray data = root.get("data").getAsJsonArray();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject node = data.get(i).getAsJsonObject();
                        Map<String, Object> trace = new LinkedHashMap<>();
                        trace.put("time", jsonStr(node, "time"));
                        trace.put("context", jsonStr(node, "context"));
                        trace.put("location", jsonStr(node, "location"));

                        String location = jsonStr(node, "location");
                        double[] coord = geocodeCity(location);
                        if (coord != null) {
                            trace.put("lng", coord[0]);
                            trace.put("lat", coord[1]);
                        }
                        traces.add(trace);
                    }
                }
                result.put("traces", traces);

                if (!traces.isEmpty()) {
                    int lastIdx = traces.size() - 1;
                    result.put("fromCity", traces.get(lastIdx).get("location"));
                    result.put("toCity", traces.get(0).get("location"));
                    double[] fc = geocodeCity((String) traces.get(lastIdx).get("location"));
                    double[] tc = geocodeCity((String) traces.get(0).get("location"));
                    if (fc != null) result.put("fromCoord", fc);
                    if (tc != null) result.put("toCoord", tc);
                }
                return result;
            } else {
                log.warn("快递100 HTTP失败: status={}, 降级演示数据", httpResult.getStatus());
                return buildDemoResult(expressNo, expressCompany);
            }
        } catch (Exception e) {
            log.warn("快递100异常: {}, 降级演示数据", e.getMessage());
            return buildDemoResult(expressNo, expressCompany);
        }
    }

    private Map<String, Object> buildDemoResult(String expressNo, String expressCompany) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("expressNo", expressNo);
        result.put("company", expressCompany);
        result.put("companyName", companyCodeToName(expressCompany));
        result.put("state", "0");
        result.put("statusText", "演示数据");

        List<Map<String, Object>> traces = buildTraces();
        result.put("traces", traces);

        if (!traces.isEmpty()) {
            result.put("fromCity", traces.get(0).get("location"));
            result.put("toCity", traces.get(traces.size() - 1).get("location"));
            result.put("fromCoord", new double[]{(Double) traces.get(0).get("lng"), (Double) traces.get(0).get("lat")});
            result.put("toCoord", new double[]{(Double) traces.get(traces.size() - 1).get("lng"), (Double) traces.get(traces.size() - 1).get("lat")});
        }
        return result;
    }

    private List<Map<String, Object>> buildTraces() {
        List<Map<String, Object>> traces = new ArrayList<>();
        long baseTime = System.currentTimeMillis();

        String[][] data = {
            {"深圳", "【深圳市】快件已揽收，快递员已取件", "深圳市南山区科技园集散中心"},
            {"东莞", "【东莞市】快件已到达东莞中转部", "东莞市虎门转运中心"},
            {"广州", "【广州市】快件已到达广州转运中心", "广州市白云区转运中心"},
            {"长沙", "【长沙市】快件已到达长沙转运中心", "长沙市星沙转运中心"},
            {"武汉", "【武汉市】快件已到达武汉转运中心", "武汉市东西湖转运中心"},
            {"郑州", "【郑州市】快件已到达郑州转运中心", "郑州市经开区转运中心"},
            {"石家庄", "【石家庄市】快件已到达石家庄转运中心", "石家庄市长安区转运中心"},
            {"北京", "【北京市】快件已到达朝阳区派送网点，等待派送", "北京市朝阳区望京派送网点"},
        };

        for (int i = 0; i < data.length; i++) {
            Map<String, Object> trace = new LinkedHashMap<>();
            trace.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date(baseTime - (data.length - i) * 3600000L * 6)));
            trace.put("context", data[i][1]);
            trace.put("location", data[i][2]);
            double[] coord = getCoordByName(data[i][0]);
            trace.put("lng", coord[0]);
            trace.put("lat", coord[1]);
            trace.put("status", i == data.length - 1 ? "派送中" : "运输中");
            traces.add(trace);
        }
        return traces;
    }

    private double[] getCoordByName(String cityName) {
        double[] coord = geocodeCity(cityName);
        return coord != null ? coord : new double[]{116.4074, 39.9042};
    }

    private double[] geocodeCity(String location) {
        if (location == null || location.isEmpty()) return null;
        for (Map.Entry<String, double[]> entry : CITY_COORDS.entrySet()) {
            if (location.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String jsonStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private String stateToText(String state) {
        switch (state) {
            case "0": return "在途";
            case "1": return "揽收";
            case "2": return "疑难";
            case "3": return "签收";
            case "4": return "退签";
            case "5": return "派件";
            default: return "未知";
        }
    }

    private String companyCodeToName(String code) {
        if (code == null) return "中通快递";
        switch (code.toLowerCase()) {
            case "zhongtong": case "zto": return "中通快递";
            case "yuantong": case "yto": return "圆通快递";
            case "shentong": case "sto": return "申通快递";
            case "yunda": return "韵达快递";
            case "shunfeng": case "sf": return "顺丰速运";
            case "ems": return "EMS";
            default: return "中通快递";
        }
    }
}
