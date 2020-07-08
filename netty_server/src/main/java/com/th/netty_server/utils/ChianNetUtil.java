package com.th.netty_server.utils;

import com.alibaba.fastjson.JSONObject;
import com.ctg.ag.sdk.biz.AepDeviceCommandClient;
import com.ctg.ag.sdk.biz.aep_device_command.CreateCommandRequest;
import com.jnthyb.protocol.encode.ProtocolEncodeMain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChianNetUtil {
    public static final Map<String, String> derviceTypeMap = new HashMap<String, String>() {{
        put("民用表", "2");
        put("集中器", "1");
        put("控制器", "0");
    }};

    public static void sendToChinaNet(String res,String deviceId){
        AepDeviceCommandClient client = AepDeviceCommandClient.newClient()
                .appKey("8emzK1K55Wa").appSecret("ypeqGKiH9G")
                .build();
        CreateCommandRequest request = new CreateCommandRequest();
// set your request params here
        request.setParamMasterKey("d285266a368d48c08eb1c1b086d0090f");	// single value
        String productId="10070350";
        String bodyString = "{\"content\": " +
                "{\"dataType\": 2," +
                " \"payload\": \"" + res + "\"" +
                "}," +
                "  \"deviceId\": \"" + deviceId + "\"," +
                "  \"operator\": \"admin\"," +
                "  \"productId\": " + productId + "," +
                "  \"ttl\": 0," +
                "  \"level\": 1" +
                "}";
        // request.addParamMasterKey(MasterKey);	// or multi values
        request.setBody(bodyString.getBytes());
        try {
            System.out.println(client.CreateCommand(request));
        } catch (Exception e) {
            e.printStackTrace();
        }
// more requests
        client.shutdown();
    }
    public static void timing(JSONObject object, String deviceId){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String format = sdf.format(now);
        Map<String, Object> params = new HashMap<String, Object>();
        // 数据类型（9：时间校准）
        params.put("dataType", 9);
        // 包序号（字符串/数字都行）
        params.put("pktSer", 1);
        // 设备类型（字符串/数字都行）(0:集中器，1:集中器，2:民用表)
        params.put("deviceType", derviceTypeMap.get(object.get("deviceType")));
        // 公司代码（字符串/数字都行）
        params.put("companyNum", object.get("companyNum"));
        // 设备地址（字符串/数字都行）
        params.put("deviceAddr", object.get("deviceAddr"));
        // 用户地址（字符串/数字都行）
        params.put("userId", 0);
        // 保留字段
        params.put("yearNc", 0);
        // 时间（字符串）
        params.put("replyTime", format);
        // 编码，时间校准不加密，这里传递哪个密钥都不起作用
        String res = ProtocolEncodeMain.encode(params, 0);
        AepDeviceCommandClient client = AepDeviceCommandClient.newClient()
                .appKey("8emzK1K55Wa").appSecret("ypeqGKiH9G")
                .build();
        CreateCommandRequest request = new CreateCommandRequest();
// set your request params here
        request.setParamMasterKey("d285266a368d48c08eb1c1b086d0090f");	// single value
        String productId="10070350";
        String bodyString = "{\"content\": " +
                "{\"dataType\": 2," +
                " \"payload\": \"" + res + "\"" +
                "}," +
                "  \"deviceId\": \"" + deviceId + "\"," +
                "  \"operator\": \"admin\"," +
                "  \"productId\": " + productId + "," +
                "  \"ttl\": 0," +
                "  \"level\": 1" +
                "}";
        // request.addParamMasterKey(MasterKey);	// or multi values
        request.setBody(bodyString.getBytes());
        try {
            System.out.println(client.CreateCommand(request));
        } catch (Exception e) {
            e.printStackTrace();
        }
// more requests
        client.shutdown();
    }
     public static void clear(Integer pktSer,String telNum,String deviceId,int deviceType){
         Map<String, Object> params = new HashMap<String, Object>();
        // 数据类型（0：充值, 1：调价, 2：开关阀, 3：开户, 4：清表）（剩余的待定。。）
         params.put("dataType", 4);
        // 包序号（字符串/数字都行）
         params.put("pktSer", pktSer);
        // 设备类型（字符串/数字都行）(0:集中器，1:集中器，2:民用表)
         params.put("deviceType", deviceType);
        // 公司代码（字符串/数字都行）
         params.put("companyNum", 0);
        // 设备地址（字符串/数字都行）
         params.put("deviceAddr",telNum);
        // 用户地址（字符串/数字都行）
         params.put("userId", 0);
        // 编码
        // 使用新密钥(即:开户密钥)
         String res = ProtocolEncodeMain.encode(params, 0);
         AepDeviceCommandClient client = AepDeviceCommandClient.newClient()
                 .appKey("8emzK1K55Wa").appSecret("ypeqGKiH9G")
                 .build();
         CreateCommandRequest request = new CreateCommandRequest();
// set your request params here
         request.setParamMasterKey("7aa8433b3e234d25867aa7f22a8d60c2");	// single value
         String productId="10055571";
         String bodyString = "{\"content\": " +
                 "{\"dataType\": 2," +
                 " \"payload\": \"" + res + "\"" +
                 "}," +
                 "  \"deviceId\": \"" + deviceId + "\"," +
                 "  \"operator\": \"admin\"," +
                 "  \"productId\": " + productId + "," +
                 "  \"ttl\": 0," +
                 "  \"level\": 1" +
                 "}";
         // request.addParamMasterKey(MasterKey);	// or multi values
         request.setBody(bodyString.getBytes());
         try {
             System.out.println(client.CreateCommand(request));
         } catch (Exception e) {
             e.printStackTrace();
         }

// more requests

         client.shutdown();
     }
     public static void clearEX(Integer pktSer,String telNum,String deviceId,int deviceType){
        Map<String, Object> params = new HashMap<String, Object>();
        // 数据类型（0：充值, 1：调价, 2：开关阀, 3：开户, 4：清表）（剩余的待定。。）
        params.put("dataType", 6);
        // 包序号（字符串/数字都行）
        params.put("pktSer", pktSer);
        // 设备类型（字符串/数字都行）(0:集中器，1:集中器，2:民用表)
        params.put("deviceType", deviceType);
        // 公司代码（字符串/数字都行）
        params.put("companyNum", 0);
        // 设备地址（字符串/数字都行）
        params.put("deviceAddr",telNum);
        // 用户地址（字符串/数字都行）
        params.put("userId", 0);
        // 编码
        // 使用新密钥(即:开户密钥)
        String res = ProtocolEncodeMain.encode(params, 0);
        AepDeviceCommandClient client = AepDeviceCommandClient.newClient()
                .appKey("8emzK1K55Wa").appSecret("ypeqGKiH9G")
                .build();
        CreateCommandRequest request = new CreateCommandRequest();
// set your request params here
        request.setParamMasterKey("7aa8433b3e234d25867aa7f22a8d60c2");	// single value
        String productId="10055571";
        String bodyString = "{\"content\": " +
                "{\"dataType\": 2," +
                " \"payload\": \"" + res + "\"" +
                "}," +
                "  \"deviceId\": \"" + deviceId + "\"," +
                "  \"operator\": \"admin\"," +
                "  \"productId\": " + productId + "," +
                "  \"ttl\": 0," +
                "  \"level\": 1" +
                "}";
        // request.addParamMasterKey(MasterKey);	// or multi values
        request.setBody(bodyString.getBytes());
        try {
            System.out.println(client.CreateCommand(request));
        } catch (Exception e) {
            e.printStackTrace();
        }

// more requests

        client.shutdown();
    }
    public static void product(Integer pktSer,String telNum,String deviceId,Integer deviceType,Integer valveType,String alarmVol,String reserveVol,String rechargeLimit,String pulse,
                               String allowVol,String openValveTime,String closeValveTime,String signalSrc,String payType,String upTime,String flowMeterCompany){
        System.err.println(upTime+"------------------------------------------------------------------------------------");
        Map<String, Object> params = new HashMap<String, Object>();
        // 数据类型（10：写配置信息）
                params.put("dataType", 10);
        // 包序号（字符串/数字都行）
                params.put("pktSer", pktSer);
        // 设备类型（字符串/数字都行）(0:控制器，1:集中器，2:民用表)
                params.put("deviceType", deviceType);
        // 公司代码（字符串/数字都行）
                params.put("companyNum", 0);
        // 设备地址（字符串/数字都行）
                params.put("deviceAddr", telNum);
        // 用户地址（字符串/数字都行）
                params.put("userId", 0);
        // 阀门类型（字符串/数字都行）(0:普通阀门，1:工业球阀)
                params.put("valveType", valveType);
        // 报警方数（字符串/数字都行）
                params.put("alarmVol", alarmVol);
        // 过流时间系数（字符串/数字都行）
                params.put("overFlow", 0);
        // 预留气量（字符串/数字都行）,民用表，0.01m³/脉冲，扩大10倍后传值
                params.put("reserveVol", reserveVol);
        // 充值上限（字符串/数字都行）
                params.put("rechargeLimit", rechargeLimit);
        // 脉冲当量（字符串/数字都行）
        // 普通阀门(0.01m³:0x20, 0.1m³:0x00, 1m³:0x01, 10m³:10, 100m³:0x11)
        // 工业球阀(1m³:0x01, 10m³:10, 100m³:0x11)
                params.put("pulse", pulse);
        // 预留气量保留位（字符串/数字都行）
        // 00:保留剩余累计，01:清除剩余累计，10:清剩余留累计
                params.put("reserveVolFlag", 1);
        // 允许透支量（字符串/数字都行）
                params.put("allowVol", allowVol);
        // 开阀时间（字符串/数字都行），开阀时间扩大10倍后传值
                params.put("openValveTime", openValveTime);
        // 关阀时间（字符串/数字都行），关阀时间扩大10倍后传值
                params.put("closeValveTime", closeValveTime);
        // 信号来源（字符串/数字都行）
        // 0:干簧管取样，1:流量计信号(低有效)，2:流量计信号(高有效)
                params.put("signalSrc", signalSrc);
        // 计费方式（字符串/数字都行）(0:气量，1:金额)
                params.put("payType", payType);
        // 计量方式（字符串/数字都行）(0:单一，1:阶梯)
                params.put("volType", 0);
        // 阶梯1单价（字符串/数字都行）
                params.put("setPrice1", 1.00);
        // 阶梯1气量（字符串/数字都行）
                params.put("setVol1", 800);
        // 阶梯2单价（字符串/数字都行）
                params.put("setPrice2", 2.85);
        // 阶梯2气量（字符串/数字都行）
                params.put("setVol2", 2100);
        // 阶梯3单价（字符串/数字都行）
                params.put("setPrice3", 3.25);
        // 结算日期（字符串/数字都行）
                params.put("payDate", 25);
        // IP地址（字符串）
                params.put("gateIp", "221.229.214.202");
        // 端口号（字符串/数字都行）
                params.put("gatePort",5683);
        // APN长度（字符串/数字都行）
                params.put("apnLen", 0);
        // 域名长度（字符串/数字都行）
                params.put("gateLen", 0);
        // 上线周期（字符串/数字都行）
                params.put("upCyc", 1440);
        // 上线时间（字符串）
                params.put("upTime", upTime);
        // 抄表信道1（字符串/数字都行）
                params.put("chan1", 0);
        // 抄表信道2（字符串/数字都行）
                params.put("chan2", 0);
        // 抄表功率1（字符串/数字都行）
                params.put("power1", 0);
        // 抄表功率2（字符串/数字都行）
                params.put("power2", 0);
        // 阶梯3气量
                params.put("setVol3", 2800);
        // 阶梯4单价
                params.put("setPrice4", 3.55);
        // 阶梯4气量
                params.put("setVol4", 3500);
        // 阶梯5单价
                params.put("setPrice5", 3.85);
        // 阶梯5气量
                params.put("setVol5", 2147483647);
        // 阶梯6单价
                params.put("setPrice6", 4.25);
        // 微漏时间(分)（字符串/数字都行）
                params.put("microLeakage", 0);
        // 失联天数(天)（字符串/数字都行）
                params.put("lostContact", 0);
        // 不用气天数(天)（字符串/数字都行）
                params.put("noUseGas", 0);
        //结算周期单位，0:年，1:月，2:日
        params.put("payCycleUnit",0);
        // 结算周期时长(天)（字符串/数字都行）
                params.put("payCycleLen", 1);
        // 流量计厂家（字符串/数字都行）
        // 0:天和，1:爱知，2:天信，3:苍南，4:新科，5:华立
                params.put("flowMeterCompany", flowMeterCompany);
        // 通信协议版本（字符串/数字都行）
                params.put("comVersion", 1);
        // 通信波特率（字符串/数字都行）
                params.put("comBaud", 9600);
        // 通信数据位（字符串/数字都行）
                params.put("comDataBit", 8);
        // 通信停止位（字符串/数字都行）
                params.put("comStopBit", 1);
        // 通信奇偶校验（字符串/数字都行）
        // 0:无，1:奇，2:偶
                params.put("comParityBit", 0);
        // 上报每天24H数据（字符串/数字都行）
        // 0:禁用，1:启用
        params.put("up24hData", 1);
        // 编码，这里密钥不起作用，写配置信息没有进行加密处理
        String res = ProtocolEncodeMain.encode(params, 0);

        AepDeviceCommandClient client = AepDeviceCommandClient.newClient()
                .appKey("8emzK1K55Wa").appSecret("ypeqGKiH9G")
                .build();
        CreateCommandRequest request = new CreateCommandRequest();
// set your request params here
        request.setParamMasterKey("7aa8433b3e234d25867aa7f22a8d60c2");	// single value
        String productId="10055571";
        String bodyString = "{\"content\": " +
                "{\"dataType\": 2," +
                " \"payload\": \"" + res + "\"" +
                "}," +
                "  \"deviceId\": \"" + deviceId + "\"," +
                "  \"operator\": \"admin\"," +
                "  \"productId\": " + productId + "," +
                "  \"ttl\": 0," +
                "  \"level\": 1" +
                "}";
        // request.addParamMasterKey(MasterKey);	// or multi values
        request.setBody(bodyString.getBytes());
        try {
            System.out.println(client.CreateCommand(request));
        } catch (Exception e) {
            e.printStackTrace();
        }

// more requests

        client.shutdown();
    }
}
