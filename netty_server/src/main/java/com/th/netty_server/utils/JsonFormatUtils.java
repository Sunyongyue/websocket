package com.th.netty_server.utils;

import com.alibaba.fastjson.JSONObject;
import com.th.decode.DeocdeDeviceAnswer;

import java.util.HashMap;
import java.util.Map;

/**
 * Json格式化
 */
public class JsonFormatUtils {
    public static  Map<String, String> answerMap = new HashMap<String, String>() {{
        put("07", "阀控指令设置");
        put("08", "密钥指令设置");
        put("03", "推送终端消息设置");
        put("47", "设置功能参数设置");
        put("48", "读功能参数设置");
        put("49", "设置上报周期设置");
        put("4A", "读上报周期设置");
        put("4D", "读终端编号设置");
        put("4F", "读终端流量计参数");
        put("18", "设置重发间隔设置");
        put("19", "读重发间隔");
        put("3B", "读小时历史记录");
        put("3C", "读天历史记录");
        put("3D", "读月历史记录");
        put("45", "清除终端异常状态");
        put("46", "读取终端异常状态");
        put("06", "读取时钟");
        put("09", "读取SIM卡ICCID号");
        put("58", "设置余量报警额度设置");
        put("59", "读取余量报警额度");
        put("2B", "设置无上报关阀天数设置");
        put("11", "读无上报关阀天数");
        put("14", "设置无用气关阀设置");
        put("15", "读无用气关阀设置");
        put("42", "设置关阀一次累计气量值");
        put("43", "读关阀一次累计气量值");
        put("39", "读分体机上报信息应答");
        put("38", "上报信息");
        put("37", "上报应答回应");
    }};
    public static  Map<String, String> readAndSet = new HashMap<String, String>() {{
        put("07", "0");
        put("08", "0");
        put("03", "0");
        put("47", "0");
        put("48", "1");
        put("49", "0");
        put("4A", "1");
        put("4D", "1");
        put("4F", "1");
        put("18", "0");
        put("19", "1");
        put("3B", "1");
        put("3C", "1");
        put("3D", "1");
        put("45", "0");
        put("46", "1");
        put("06", "1");
        put("09", "1");
        put("58", "0");
        put("59", "1");
        put("2B", "0");
        put("11", "1");
        put("14", "0");
        put("15", "1");
        put("42", "0");
        put("43", "1");
        put("39", "1");
        put("38", "1");
    }};
    public static String format(String s){
        String decode = DeocdeDeviceAnswer.decode(s);
        JSONObject jsonObject = JSONObject.parseObject(decode);
        String commandCode = s.substring(24,26);
        String version=s.substring(2,4);
        String deviceType=s.substring(4,6);
        String deviceAddr=s.substring(6,22);
        String key=s.substring(22,24);
        StringBuffer res = new StringBuffer();

        if (readAndSet.get(commandCode).equals("0")){
            res.append("接收到数据：" + s);
            res.append("\n版本号：" + version);
            res.append("\n设备类型：" + deviceType);
            res.append("\n设备地址：" + deviceAddr);
            res.append("\n密钥：" + key);
            String setStatus = jsonObject.get("setStatus").toString();
            res.append("\n执行结果："+setStatus);
            return res.toString();
        }else if (readAndSet.get(commandCode).equals("1")){
            res.append("\n\n\n接收到数据：" + s);
            res.append("\n版本号：" + version);
            res.append("\n设备类型：" + deviceType);
            res.append("\n设备地址：" + deviceAddr);
            res.append("\n密钥：" + key);
            switch (commandCode){
            case "48":
                String decrypt = jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt);
                String commandType = jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType);
                String leakageCheck = jsonObject.get("leakageCheck").toString();
                res.append("\n泄漏检测：" + leakageCheck);
                String displayLong = jsonObject.get("displayLong").toString();
                res.append("\n液晶长显：" + displayLong);
                String outFactory = jsonObject.get("outFactory").toString();
                res.append("\n出厂模式：" + outFactory);
                String powerReport = jsonObject.get("powerReport").toString();
                res.append("\n上电上传：" + powerReport);
                String openClose = jsonObject.get("openClose").toString();
                res.append("\n开盖开阀：" + openClose);
                String valveExClose = jsonObject.get("valveExClose").toString();
                res.append("\n阀门异常关阀：" + valveExClose);
                String titleAlarm = jsonObject.get("titleAlarm").toString();
                res.append("\n倾斜报警：" + titleAlarm);
                String highTemAlarm = jsonObject.get("highTemAlarm").toString();
                res.append("\n高温报警：" + highTemAlarm);
                String fourEFTelEx = jsonObject.get("fourEFTelEx").toString();
                res.append("\n485通讯异常关阀：" + fourEFTelEx);
                String hourSave = jsonObject.get("hourSave").toString();
                res.append("\n按小时存储流量计信息：" + hourSave);
                String onLineLong = jsonObject.get("onLineLong").toString();
                res.append("\n长期在线模式：" + onLineLong);
                String inventedCount = jsonObject.get("inventedCount").toString();
                res.append("\n虚拟计费：" + inventedCount);
                return res.toString();
            case "4A":
                String decrypt4A = jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt4A);
                String commandType4A = jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType4A);
                String reportType = jsonObject.get("reportType").toString();
                res.append("\n上报方式：" + reportType);
                if (reportType.equals("定时上报")){
                    String reportTimes = jsonObject.get("reportTimes").toString();
                    res.append("\n定时上报日次数：" + reportTimes);
                    String timingReportOne = jsonObject.get("timingReportOne").toString();
                    res.append("\n定时上报时间1：" + timingReportOne);
                    String timingReportTwo = jsonObject.get("timingReportTwo").toString();
                    res.append("\n定时上报时间2：" + timingReportTwo);
                    String timingReportThree = jsonObject.get("timingReportThree").toString();
                    res.append("\n定时上报时间3：" + timingReportThree);
                    String timingReportFour = jsonObject.get("timingReportFour").toString();
                    res.append("\n定时上报时间4：" + timingReportFour);
                    String timingReportFive = jsonObject.get("timingReportFive").toString();
                    res.append("\n定时上报时间5：" + timingReportFive);
                    String timingReportSix = jsonObject.get("timingReportSix").toString();
                    res.append("\n定时上报时间6：" + timingReportSix);
                    String timingReportSeven = jsonObject.get("timingReportSeven").toString();
                    res.append("\n定时上报时间7：" + timingReportSeven);
                    String timingReportEight = jsonObject.get("timingReportEight").toString();
                    res.append("\n定时上报时间8：" + timingReportEight);
                    String timingReportNine = jsonObject.get("timingReportNine").toString();
                    res.append("\n定时上报时间9：" + timingReportNine);
                    String timingReportTen = jsonObject.get("timingReportTen").toString();
                    res.append("\n定时上报时间10：" + timingReportTen);
                    String timingReportEleven = jsonObject.get("timingReportEleven").toString();
                    res.append("\n定时上报时间11：" + timingReportEleven);
                    String timingReportTwelve = jsonObject.get("timingReportTwelve").toString();
                    res.append("\n定时上报时间12：" + timingReportTwelve);
                }else {
                    String intervalTime = jsonObject.get("intervalTime").toString();
                    res.append("\n上报间隔时间：" + intervalTime);
                    String intervalTimeFirst = jsonObject.get("intervalTimeFirst").toString();
                    res.append("\n首次上报时间：" + intervalTimeFirst);
                }
                return res.toString();
            case "4D":
                  String decrypt4D= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt4D);
                String commandType4D = jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType4D);
                String flowerNumOver = jsonObject.get("flowerNumOver").toString();
                res.append("\n流量计编号：" + flowerNumOver);
                return res.toString();
            case "4F":
                  String decrypt4F= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt4F);
                String commandType4F = jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType4F);
                String location = jsonObject.get("location").toString();
                res.append("\n参数位置：" + location);
                String flowerNumOver4F = jsonObject.get("flowerNumOver").toString();
                res.append("\n流量计编号：" + flowerNumOver4F);
                String sonDevice = jsonObject.get("sonDevice").toString();
                res.append("\n子机号：" + sonDevice);
                return res.toString();
            case "19":
                   String decrypt19= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt19);
                 String commandType19= jsonObject.get("commandType").toString();
                 res.append("\n命令类型：" + commandType19);
                 String reInterval= jsonObject.get("reInterval").toString();
                 res.append("\n重发间隔：" + reInterval);
                 String reIntervalTime= jsonObject.get("reIntervalTime").toString();
                 res.append("\n重发次数：" + reIntervalTime);
                 return  res.toString();
            case "3B":
                   String decrypt3B= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt3B);
                String commandType3B= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType3B);
                String subpackageCount= jsonObject.get("subpackageCount").toString();
                res.append("\n分包计数：" + subpackageCount);
                String flowerNumOver3B= jsonObject.get("flowerNumOver").toString();
                res.append("\n流量计编号：" + flowerNumOver3B);
                String date= jsonObject.get("date").toString();
                res.append("\n终端时间：" + date);
                res.append("\n小时记录1：-------------------");
                String lJBK= jsonObject.get("lJBK").toString();
                res.append("\n累计气量标况：" + lJBK);
                String sSBK= jsonObject.get("sSBK").toString();
                res.append("\n瞬时流量标况：" + sSBK);
                String flowMeterTemp= jsonObject.get("flowMeterTemp").toString();
                res.append("\n流量计温度：" + flowMeterTemp);
                String flowMeterPre= jsonObject.get("flowMeterPre").toString();
                res.append("\n流量计压力：" + flowMeterPre);
                String surplusDouble= jsonObject.get("surplusDouble").toString();
                res.append("\n剩余金额：" + surplusDouble);
                res.append("\n小时记录2：-------------------");
                String lJBKTwo= jsonObject.get("lJBKTwo").toString();
                res.append("\n累计气量标况：" + lJBKTwo);
                String sSBKTwo= jsonObject.get("sSBKTwo").toString();
                res.append("\n瞬时流量标况：" + sSBKTwo);
                String flowMeterTempTwo= jsonObject.get("flowMeterTempTwo").toString();
                res.append("\n流量计温度：" + flowMeterTempTwo);
                String flowMeterPreTwo= jsonObject.get("flowMeterPreTwo").toString();
                res.append("\n流量计压力：" + flowMeterPreTwo);
                String surplusDoubleTwo= jsonObject.get("surplusDoubleTwo").toString();
                res.append("\n剩余金额：" + surplusDoubleTwo);
                res.append("\n小时记录3：-------------------");
                String lJBKThree= jsonObject.get("lJBKThree").toString();
                res.append("\n累计气量标况：" + lJBKThree);
                String sSBKThree= jsonObject.get("sSBKThree").toString();
                res.append("\n瞬时流量标况：" + sSBKThree);
                String flowMeterTempThree= jsonObject.get("flowMeterTempThree").toString();
                res.append("\n流量计温度：" + flowMeterTempThree);
                String flowMeterPreThree= jsonObject.get("flowMeterPreThree").toString();
                res.append("\n流量计压力：" + flowMeterPreThree);
                String surplusDoubleThree= jsonObject.get("surplusDoubleThree").toString();
                res.append("\n剩余金额：" + surplusDoubleThree);
                res.append("\n小时记录4：-------------------");
                String lJBKFour= jsonObject.get("lJBKFour").toString();
                res.append("\n累计气量标况：" + lJBKFour);
                String sSBKFour= jsonObject.get("sSBKFour").toString();
                res.append("\n瞬时流量标况：" + sSBKFour);
                String flowMeterTempFour= jsonObject.get("flowMeterTempFour").toString();
                res.append("\n流量计温度：" + flowMeterTempFour);
                String flowMeterPreFour= jsonObject.get("flowMeterPreFour").toString();
                res.append("\n流量计压力：" + flowMeterPreFour);
                String surplusDoubleFour= jsonObject.get("surplusDoubleFour").toString();
                res.append("\n剩余金额：" + surplusDoubleFour);
                res.append("\n小时记录5：-------------------");
                String lJBKFive= jsonObject.get("lJBKFive").toString();
                res.append("\n累计气量标况：" + lJBKFive);
                String sSBKFive= jsonObject.get("sSBKFive").toString();
                res.append("\n瞬时流量标况：" + sSBKFive);
                String flowMeterTempFive= jsonObject.get("flowMeterTempFive").toString();
                res.append("\n流量计温度：" + flowMeterTempFive);
                String flowMeterPreFive= jsonObject.get("flowMeterPreFive").toString();
                res.append("\n流量计压力：" + flowMeterPreFive);
                String surplusDoubleFive= jsonObject.get("surplusDoubleFive").toString();
                res.append("\n剩余金额：" + surplusDoubleFive);
                res.append("\n小时记录6：-------------------");
                String lJBKSix= jsonObject.get("lJBKSix").toString();
                res.append("\n累计气量标况：" + lJBKSix);
                String sSBKSix= jsonObject.get("sSBKSix").toString();
                res.append("\n瞬时流量标况：" + sSBKSix);
                String flowMeterTempSix= jsonObject.get("flowMeterTempSix").toString();
                res.append("\n流量计温度：" + flowMeterTempSix);
                String flowMeterPreSix= jsonObject.get("flowMeterPreSix").toString();
                res.append("\n流量计压力：" + flowMeterPreSix);
                String surplusDoubleSix= jsonObject.get("surplusDoubleSix").toString();
                res.append("\n剩余金额：" + surplusDoubleSix);
                return  res.toString();
            case "3C":
                   String decrypt3C= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt3C);
                String commandType3C= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType3C);
                String flowerNumOver3C= jsonObject.get("flowerNumOver").toString();
                res.append("\n流量计编号：" + flowerNumOver3C);
                String date3C= jsonObject.get("date").toString();
                res.append("\n终端时间：" + date3C);
                String lJBK3C= jsonObject.get("lJBK").toString();
                res.append("\n累计气量标况：" + lJBK3C);
                String sSBK3C= jsonObject.get("sSBK").toString();
                res.append("\n瞬时流量标况：" + sSBK3C);
                String flowMeterTemp3C= jsonObject.get("flowMeterTemp").toString();
                res.append("\n流量计温度：" + flowMeterTemp3C);
                String flowMeterPre3C= jsonObject.get("flowMeterPre").toString();
                res.append("\n流量计压力：" + flowMeterPre3C);
                String surplusDouble3= jsonObject.get("surplusDouble").toString();
                res.append("\n剩余金额：" + surplusDouble3);
                return  res.toString();
            case "3D":
                  String decrypt3D= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt3D);
                String commandType3D= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType3D);
                String flowerNumOver3D= jsonObject.get("flowerNumOver").toString();
                res.append("\n流量计编号：" + flowerNumOver3D);
                String date3D= jsonObject.get("date").toString();
                res.append("\n终端时间：" + date3D);
                String lJBK3D= jsonObject.get("lJBK").toString();
                res.append("\n累计气量标况：" + lJBK3D);
                String sSBK3D= jsonObject.get("sSBK").toString();
                res.append("\n瞬时流量标况：" + sSBK3D);
                String flowMeterTemp3D= jsonObject.get("flowMeterTemp").toString();
                res.append("\n流量计温度：" + flowMeterTemp3D);
                String flowMeterPre3D= jsonObject.get("flowMeterPre").toString();
                res.append("\n流量计压力：" + flowMeterPre3D);
                String surplusDouble3D= jsonObject.get("surplusDouble").toString();
                res.append("\n剩余金额：" + surplusDouble3D);
                return  res.toString();
            case "46":
                  String decrypt46= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt46);
                String commandType46= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType46);
                res.append("\n表状态1-------------------");
                String balanceStatus= jsonObject.get("balanceStatus").toString();
                res.append("\n表端余额：" + balanceStatus);
                String overdrawStatus= jsonObject.get("overdrawStatus").toString();
                res.append("\n表端透支：" + overdrawStatus);
                String countPreStatus= jsonObject.get("countPreStatus").toString();
                res.append("\n计量电压：" + countPreStatus);
                String farAwayPreStatus= jsonObject.get("farAwayPreStatus").toString();
                res.append("\n远传电压：" + farAwayPreStatus);
                String backupPowerStatus= jsonObject.get("backupPowerStatus").toString();
                res.append("\n时钟备电：" + backupPowerStatus);
                String valveControlPreStatus= jsonObject.get("valveControlPreStatus").toString();
                res.append("\n阀控电压：" + valveControlPreStatus);
                String farAwayWDStatus= jsonObject.get("farAwayWDStatus").toString();
                res.append("\n远传外电：" + farAwayWDStatus);
                String fourEightFiveStatus= jsonObject.get("fourEightFiveStatus").toString();
                res.append("\n485通信：" + fourEightFiveStatus);
                res.append("\n表状态2-------------------");
                String valveCheck= jsonObject.get("valveCheck").toString();
                res.append("\n阀门检测：" + valveCheck);
                String saveCheck= jsonObject.get("saveCheck").toString();
                res.append("\n存储检测：" + saveCheck);
                String leakageCheck46= jsonObject.get("leakageCheck").toString();
                res.append("\n泄漏检测：" + leakageCheck46);
                String tiltCheck= jsonObject.get("tiltCheck").toString();
                res.append("\n倾斜检测：" + tiltCheck);
                String highTempCheck= jsonObject.get("highTempCheck").toString();
                res.append("\n高温检测：" + highTempCheck);
                String noReportDaysCheck= jsonObject.get("noReportDaysCheck").toString();
                res.append("\n无上报天数：" + noReportDaysCheck);
                String closeOneTotalCheck= jsonObject.get("closeOneTotalCheck").toString();
                res.append("\n关阀一次累计值：" + closeOneTotalCheck);
                String openLidCheck= jsonObject.get("openLidCheck").toString();
                res.append("\n开盖检测：" + openLidCheck);
                return  res.toString();
            case "06":
                String decrypt06= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt06);
                String commandType06= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType06);
                String date06= jsonObject.get("date").toString();
                res.append("\n终端时间：" + date06);
                return  res.toString();
            case "09":
                String decrypt09= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt09);
                String commandType09= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType09);
                String ICCIDNum= jsonObject.get("ICCIDNum").toString();
                res.append("\nICCID编号：" + ICCIDNum);
                return  res.toString();
            case "59":
                String decrypt59= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt59);
                String commandType59= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType59);
                String unit= jsonObject.get("unit").toString();
                res.append("\n计量单位：" + unit);
                String surplusOne= jsonObject.get("surplusOne").toString();
                res.append("\n余量报警额度1：" + surplusOne);
                String valveOne= jsonObject.get("valveOne").toString();
                res.append("\n阀控标识1：" + valveOne);
                String surplusTWo= jsonObject.get("surplusTWo").toString();
                res.append("\n余量报警额度2：" + surplusTWo);
                String valveTWo= jsonObject.get("valveTWo").toString();
                res.append("\n阀控标识2：" + valveTWo);
                String surplusThree= jsonObject.get("surplusThree").toString();
                res.append("\n余量报警额度3：" + surplusThree);
                String valveThree= jsonObject.get("valveThree").toString();
                res.append("\n阀控标识3：" + valveThree);
                String surplusFour= jsonObject.get("surplusFour").toString();
                res.append("\n余量报警额度4：" + surplusFour);
                String valveFour= jsonObject.get("valveFour").toString();
                res.append("\n阀控标识4：" + valveFour);
                String surplusFive= jsonObject.get("surplusFive").toString();
                res.append("\n余量报警额度5：" + surplusFive);
                String valveFive= jsonObject.get("valveFive").toString();
                res.append("\n阀控标识5：" + valveFive);
                return  res.toString();
            case "11":
                String decrypt11= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt11);
                String commandType11= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType11);
                String days= jsonObject.get("days").toString();
                res.append("\n无上报关阀天数：" + days);
                return  res.toString();
            case "15":
                String decrypt15= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt15);
                String commandType15= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType15);
                String days15= jsonObject.get("days").toString();
                res.append("\n无用气关阀天数：" + days15);
                return  res.toString();
            case "43":
                String decrypt43= jsonObject.get("decrypt").toString();
                res.append("\n解密后：" + decrypt43);
                String commandType43= jsonObject.get("commandType").toString();
                res.append("\n命令类型：" + commandType43);
                String closeType= jsonObject.get("closeType").toString();
                res.append("\n关阀类型：" + closeType);
                String totalGas= jsonObject.get("totalGas").toString();
                res.append("\n累计气量(标况)：" + totalGas);
                return res.toString();
            case "39":
                return reportInfoToText(s);
            case "38":
                return reportInfoToText(s);
                default:
                return "未知类型";

        }
        }
        return "未知类型";
    }

    public static void main(String[] args) {
        String format = format("BB0114000100000000011801388000F92D103ADD6AD6A5EE70FD62CB6A1F00F58B6B3CEB21C88AB33CC64BD19324C812C759F2AA62146B5FBE3C8ADB3ED7B3B6662C45746E6E72A48CB761DE8BF2522B147091BF40B66D7EB3BF92BD6A52354BD8619A14FBE310BFE6F00E2D04CFD7063397B31BFE2521EB0A6576488A56EB8698765775CDD100E9F3F1634CAD33884DA1EE");
        System.out.println(format);
    }
    public static String reportInfoToText(String s){
        String decode = DeocdeDeviceAnswer.decode(s);
        JSONObject jsonObject = JSONObject.parseObject(decode);
        String commandCode = s.substring(24,26);
        String version=s.substring(2,4);
        String deviceType=s.substring(4,6);
        String deviceAddr=s.substring(6,22);
        String key=s.substring(22,24);
        StringBuffer res = new StringBuffer();
        res.append("\n\n\n接收到数据：" + s);
        res.append("\n版本号：" + version);
        res.append("\n设备类型：" + deviceType);
        res.append("\n设备地址：" + deviceAddr);
        res.append("\n密钥：" + key);
        String decrypt= jsonObject.get("decrypt").toString();
        res.append("\n解密后：" + decrypt);
       // String commandType= jsonObject.get("commandCode").toString();
        res.append("\n命令类型：" + commandCode+"上报信息回复");
        String reportType= jsonObject.get("reportType").toString();
        res.append("\n上报类型：" + reportType);
        String signalIntensity= jsonObject.get("signalIntensity").toString();
        res.append("\n信号强度：" + signalIntensity);
        String supplyVoltage= jsonObject.get("supplyVoltage").toString();
        res.append("\n电源电压：" + supplyVoltage);
        String valveStatus= jsonObject.get("valveStatus").toString();
        res.append("\n阀门状态：" + valveStatus);
        res.append("\n表状态一：----------");
        String balanceStatus= jsonObject.get("balanceStatus").toString();
        res.append("\n表端余额：" + balanceStatus);
        String overdrawStatus= jsonObject.get("overdrawStatus").toString();
        res.append("\n表端透支：" + overdrawStatus);
        String countPreStatus= jsonObject.get("countPreStatus").toString();
        res.append("\n计量电压：" + countPreStatus);
        String farAwayPreStatus= jsonObject.get("farAwayPreStatus").toString();
        res.append("\n远传电压：" + farAwayPreStatus);
        String backupPowerStatus= jsonObject.get("backupPowerStatus").toString();
        res.append("\n时钟备电：" + backupPowerStatus);
        String valveControlPreStatus= jsonObject.get("valveControlPreStatus").toString();
        res.append("\n阀控电压：" + valveControlPreStatus);
        String farAwayWDStatus= jsonObject.get("farAwayWDStatus").toString();
        res.append("\n远传外电：" + farAwayWDStatus);
        String fourEightFiveStatus= jsonObject.get("fourEightFiveStatus").toString();
        res.append("\n485通信：" + fourEightFiveStatus);
        res.append("\n表状态二：----------");
        String valveCheck= jsonObject.get("valveCheck").toString();
        res.append("\n阀门检测：" + valveCheck);
        String saveCheck= jsonObject.get("saveCheck").toString();
        res.append("\n存储检测：" + saveCheck);
        String leakageCheck= jsonObject.get("leakageCheck").toString();
        res.append("\n泄漏检测：" + leakageCheck);
        String tiltCheck= jsonObject.get("tiltCheck").toString();
        res.append("\n倾斜检测：" + tiltCheck);
        String highTempCheck= jsonObject.get("highTempCheck").toString();
        res.append("\n高温检测：" + highTempCheck);
        String noReportDaysCheck= jsonObject.get("noReportDaysCheck").toString();
        res.append("\n无上报天数：" + noReportDaysCheck);
        String closeOneTotalCheck= jsonObject.get("closeOneTotalCheck").toString();
        res.append("\n关阀一次累计值：" + closeOneTotalCheck);
        String openLidCheck= jsonObject.get("openLidCheck").toString();
        res.append("\n开盖检测：" + openLidCheck);
        res.append("\n表状态三：----------");
        String outOfFactory= jsonObject.get("outOfFactory").toString();
        res.append("\n出厂标识：" + outOfFactory);
        String operationKey= jsonObject.get("operationKey").toString();
        res.append("\n运营密钥：" + operationKey);
        String ipOne= jsonObject.get("ipOne").toString();
        res.append("\n域名/IP1：" + ipOne);
        String ipTwo= jsonObject.get("ipTwo").toString();
        res.append("\n域名/IP2：" + ipTwo);
        String date= jsonObject.get("date").toString();
        res.append("\n表端时间：" + date);
        String temperature= jsonObject.get("temperature").toString();
        res.append("\n环境温度：" + temperature);
        String flowerCount= jsonObject.get("flowerCount").toString();
        res.append("\n流量计数：" + flowerCount);
        String flowerNumOver= jsonObject.get("flowerNumOver").toString();
        res.append("\n流量计编号：" + flowerNumOver);
        String subMachineNumber= jsonObject.get("subMachineNumber").toString();
        res.append("\n流量计地址：" + subMachineNumber);
        String flowMeterLJBK= jsonObject.get("flowMeterLJBK").toString();
        res.append("\n累计气量(标况)：" + flowMeterLJBK);
        String flowMeterLJGK= jsonObject.get("flowMeterLJGK").toString();
        res.append("\n累计气量(工况)：" + flowMeterLJGK);
        String flowMeterSSBK= jsonObject.get("flowMeterSSBK").toString();
        res.append("\n瞬时流量(标况)：" + flowMeterSSBK);
        String flowMeterSSGK= jsonObject.get("flowMeterSSGK").toString();
        res.append("\n瞬时流量(工况)：" + flowMeterSSGK);
        String flowMeterTemp= jsonObject.get("flowMeterTemp").toString();
        res.append("\n流量计温度：" + flowMeterTemp);
        String flowMeterPre= jsonObject.get("flowMeterPre").toString();
        res.append("\n流量计压力：" + flowMeterPre);
        res.append("\n流量计状态：----------" );
        String lowPre= jsonObject.get("lowPre").toString();
        res.append("\n压力下限：" + lowPre);
        String highPre= jsonObject.get("highPre").toString();
        res.append("\n压力上限：" + highPre);
        String lowTemp= jsonObject.get("lowTemp").toString();
        res.append("\n温度下限：" + lowTemp);
        String highTemp= jsonObject.get("highTemp").toString();
        res.append("\n温度上限：" + highTemp);
        String lowLLGK= jsonObject.get("lowLLGK").toString();
        res.append("\n流量下限：" + lowLLGK);
        String highLLGK= jsonObject.get("highLLGK").toString();
        res.append("\n流量上限：" + highLLGK);
        String flowMeterTempFellStatus= jsonObject.get("flowMeterTempFellStatus").toString();
        res.append("\n温度传感器：" + flowMeterTempFellStatus);
        String flowMeterPreFellStatus= jsonObject.get("flowMeterPreFellStatus").toString();
        res.append("\n压力传感器：" + flowMeterPreFellStatus);
        String flowMeterMagneticInterference= jsonObject.get("flowMeterMagneticInterference").toString();
        res.append("\n磁干扰检测：" + flowMeterMagneticInterference);
        String errTotalLJBK= jsonObject.get("errTotalLJBK").toString();
        res.append("\n错误累计(标况)：" + errTotalLJBK);
        String errTotalLJGK= jsonObject.get("errTotalLJGK").toString();
        res.append("\n错误累计(工况：" + errTotalLJGK);
        String kFactor= jsonObject.get("kFactor").toString();
        res.append("\nK系数：" + kFactor);
        String flowMeterTime= jsonObject.get("flowMeterTime").toString();
        res.append("\n流量计时间：" + flowMeterTime);
        String unitPriceDouble= jsonObject.get("unitPriceDouble").toString();
        res.append("\n当前气价：" + unitPriceDouble);
        String surplusDouble= jsonObject.get("surplusDouble").toString();
        res.append("\n剩余金额：" + surplusDouble);
        String soundVelDouble= jsonObject.get("soundVelDouble").toString();
        res.append("\n超声声速：" + soundVelDouble);
        return res.toString();
    }
}
