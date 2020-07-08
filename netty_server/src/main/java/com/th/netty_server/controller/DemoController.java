package com.th.netty_server.controller;



import com.alibaba.fastjson.JSONObject;
import com.ctg.ag.sdk.biz.AepDeviceManagementClient;
import com.ctg.ag.sdk.biz.aep_device_management.CreateDeviceRequest;
import com.ctg.ag.sdk.biz.aep_device_management.CreateDeviceResponse;
import com.ctg.ag.sdk.biz.aep_device_management.DeleteDeviceRequest;
import com.ctg.ag.sdk.biz.aep_device_management.DeleteDeviceResponse;
import com.th.decode.DeocdeDeviceAnswer;
import com.th.decode.MainDecode;
import com.th.encode.*;
import com.th.entity.ReportingInformation;
import com.th.netty_server.config.WebSocketServer;
import com.th.netty_server.utils.ChianNetUtil;
import com.th.netty_server.utils.JsonFormatUtils;
import com.th.netty_server.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
public class DemoController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    RedisUtils redisUtils;
    @GetMapping("index")
    public ResponseEntity<String> index(){
        return ResponseEntity.ok("请求成功");
    }

    @GetMapping("page")
    public ModelAndView page(){
        return new ModelAndView("websocket");
    }

    @RequestMapping("test")
    public ModelAndView test(){
        return new ModelAndView("test");
    }
    @RequestMapping("creat")
    public String device(String deviceId,String deviceImsi,String flag){
        String msg="";
        AepDeviceManagementClient client = AepDeviceManagementClient.newClient()
                .appKey("8emzK1K55Wa").appSecret("ypeqGKiH9G")
                .build();
        // set your request params here
        String productId="10070350";
        if (flag.equals("注册")){
            CreateDeviceRequest request = new CreateDeviceRequest();
            String bodyString = "{\"deviceName\":\"" + deviceId + "\",\"deviceSn\":\"\",\"imei\":\"" + deviceImsi.trim() + "\",\"operator\":\"admin\",\"other\": {\"autoObserver\":0,\"imsi\":\"\",\"pskValue\":\"\"},\"productId\":\"" + productId + "\"}";
            request.setParamMasterKey("d285266a368d48c08eb1c1b086d0090f");
            request.setBody(bodyString.getBytes());
            try {
                CreateDeviceResponse createDeviceResponse = client.CreateDevice(request);
                LOGGER.info("createDeviceResponse{}",createDeviceResponse);
                byte[] body = createDeviceResponse.getBody();
                String s = new String(body, "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(s);
                JSONObject result = jsonObject.getJSONObject("result");
                String deviceID = (String) result.get("deviceId");
                redisUtils.set(deviceId,deviceID);
                msg= (String) jsonObject.get("msg");
            } catch (Exception e) {
                e.printStackTrace();
            }
// more requests
            client.shutdown();
        }else {
            DeleteDeviceRequest delete = new DeleteDeviceRequest();
            String device = (String) redisUtils.get(deviceId);
            delete.setParamDeviceIds(device);
            delete.setParamMasterKey("d285266a368d48c08eb1c1b086d0090f");
            delete.setParamProductId(productId);
            try {
                DeleteDeviceResponse deleteDeviceResponse = client.DeleteDevice(delete);
                LOGGER.info("deleteDeviceResponse{}",deleteDeviceResponse);
                byte[] body = deleteDeviceResponse.getBody();
                String s = new String(body, "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(s);
                msg= (String) jsonObject.get("msg");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",1);
        jsonObject.put("message",msg);
        return jsonObject.toString();

    }
    @RequestMapping("/ctwSend")
    public String reviceIotDate(@RequestBody JSONObject msg) throws Exception {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        LOGGER.info("接收到的消息:{}",msg);
        JSONObject payload = msg.getJSONObject("payload");
        String deviceId = (String) msg.get("deviceId");
        String apPdata = payload.getString("APPdata");
        byte[] decoded = Base64.getDecoder().decode(apPdata);
        String s = toHex(decoded);
        LOGGER.info("接收到的消息base64解码后:{}",s);
        String commandCode = s.substring(24,26);
        String version=s.substring(2,4);
        String deviceType=s.substring(4,6);
        String deviceAddr=s.substring(6,22);
        String key=s.substring(22,24);
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceAddr);
        //密钥
        params.put("key",key);
        Object o1 = redisUtils.get(deviceAddr);
        if (commandCode.equals("38")){
            String jsonFormat= JsonFormatUtils.format(s);
            String decode = DeocdeDeviceAnswer.decode(s);
            JSONObject jsonObject = JSONObject.parseObject(decode);
            ReportingInformation o = JSONObject.toJavaObject(jsonObject, ReportingInformation.class);
            WebSocketServer.sendInfo(jsonFormat,o.getDeviceAddr());
            //下发上报应答命令
            String encode = EncodeReadReportInfoCommand.reportAnswerCommand(params);
            ChianNetUtil.sendToChinaNet(encode,o1.toString());
            WebSocketServer.sendInfo("\nS=>C上报应答：\n"+encode,o.getDeviceAddr());

        }else if (commandCode.equals("37")){
            WebSocketServer.sendInfo("C=>S上报应答：\n版本号："+version+"\n设备类型："+deviceType+"\n设备编号："+deviceAddr+"\n密钥："+key+"\n系统时间："+format,deviceAddr);
            List<Object> objects = redisUtils.lGet(deviceAddr+"command", 0, 0);
            if (objects.size()>0){
                for (Object ob: objects) {
                    ChianNetUtil.sendToChinaNet(ob.toString(),o1.toString());
                    redisUtils.lRemove(deviceAddr+"command",1,ob);
                    WebSocketServer.sendInfo("\nS=>C指令下发：\n"+ob,deviceAddr);
                }
            }else{
                //断网指令
                String encode = EncodeDisConnectionCommand.encode(params);
                ChianNetUtil.sendToChinaNet(encode,o1.toString());
                WebSocketServer.sendInfo("\nS=>C断网指令：\n"+encode,deviceAddr);
            }
        }else {
            //其他指令设置回复以及读取指令的回复
            //String allDecode=DeocdeDeviceAnswer.decode(s);
            String allDecode = JsonFormatUtils.format(s);
            WebSocketServer.sendInfo("\nC=>S表端回复：\n"+allDecode,deviceAddr);
            List<Object> objects = redisUtils.lGet(deviceAddr+"command", 0, 0);
            if (objects.size()>0){
                for (Object ob: objects) {
                    ChianNetUtil.sendToChinaNet(ob.toString(),o1.toString());
                    redisUtils.lRemove(deviceAddr+"command",1,ob);
                    WebSocketServer.sendInfo("\nS=>C指令下发：\n"+ob,deviceAddr);
                }
            }else {
                //断网指令
                String encode = EncodeDisConnectionCommand.encode(params);
                ChianNetUtil.sendToChinaNet(encode,o1.toString());
                WebSocketServer.sendInfo("\nS=>C断网指令：\n"+encode,deviceAddr);
            }
        }
        return "OK";
    }
    @RequestMapping("/push/{toUserId}")
    public ResponseEntity<String> pushToWeb(String message, @PathVariable String toUserId) throws IOException {
        WebSocketServer.sendInfo(message,toUserId);
        return ResponseEntity.ok("MSG SEND SUCCESS");
    }

    @RequestMapping("/valveControl")
    public String valveControl(String deviceId,String deviceImsi,String flag,String version,String deviceType,String key) throws Exception {
        String valveControl="开阀";
        if (flag.equals("22")){
            valveControl="警告性关阀";
        }else if (flag.equals("33")){
            valveControl="强制性关阀";
        }
        int i=0;
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key);
        //阀控标识
        params.put("flag",flag.trim());
        String s = EncodeValveControl.valveControl(params);
        boolean b = redisUtils.lSet(deviceId+"command", s);
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+valveControl+"指令缓存：\n"+s,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+valveControl+"指令：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();
    }
    @RequestMapping("/pushTerminalMessage")
    public String pushTerminalMessage(String deviceId,String money,String unitPrice,String moneyDisplay,String overdrawDisplay,String deviceType,String key,String version) throws Exception {

        int i=0;
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //终端剩余金额
        params.put("money",money.trim());
        //终端单价
        params.put("unitPrice",unitPrice.trim());
        //终端显示状态 00：都不显示 01：有余额不足显示，无透支显示 02：无余额不足显示，有透支显示 03：有余额不足显示，有透支显示
        int i1 = Integer.parseInt(overdrawDisplay + moneyDisplay, 2);
        params.put("displayStatus","0"+i1);
        String encode = EncodePushTerminalMessage.encode(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"推送终端消息指令缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"指令：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();
    }
    @RequestMapping("/setParameters")
    public String setParameters(String deviceId,String highTemAlarm,String lcdDisplayLong,String tiltAlarm,String leakageAlarm,String abnormalValve,String fourEx,String openAndCloseValve,String hourSave,
                                String resetReport,String onlineLong,String outFactory,String virtualBilling,String deviceType,String key,String version) throws IOException {


        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //功能位1 1字节
        params.put("functionOne",leakageAlarm+lcdDisplayLong+outFactory+resetReport+openAndCloseValve+abnormalValve+tiltAlarm+highTemAlarm);
        //功能位2 1字节
        params.put("functionTwo","0000"+virtualBilling+onlineLong+hourSave+fourEx);
        String encode = null;
        try {
            encode = EncodeSetParameters.encode(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
       // System.out.println(encode);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"设置功能参数指令缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"指令：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();
    }

    @RequestMapping("/readParams")
    public String readParams(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode = EncodeSetParameters.readParams(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读功能参数指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读功能参数指令缓存指令：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/reportingCycleSet")
    public String reportingCycleSet(String deviceId,String deviceType,String key,String version ,String reportMode,String regularReportTimes,String firstReportTime,
            String intervalReportTime,String timeOne,String timeTwo,String timeThree,String timeFour,String timeFive,String timeSix,
                                            String timeSeven,String timeEight,String timeNine,String timeTen,String timeEleven,String timeTwelve) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //上报方式 1字节 01/02
        params.put("reportMethod",reportMode);
        //定时上报日次数 取值1-12 hex码 1字节
        params.put("dayReport",regularReportTimes);
        //定时上报时间 BCD码 24字节
        params.put("reportTime",timeOne+timeTwo+timeThree+timeFour+timeFive+timeSix+timeSeven+timeEight+timeNine+timeTen+timeEleven+timeTwelve);
        //间隔上报的间隔时间 2字节 hex码
        params.put("intervalReportTime",intervalReportTime);
        //间隔上报的首次间上报时间 2字节
        params.put("firstIntervalReportTime",firstReportTime);
        String encode = EncodeReportingCycle.set(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"设置上报周期指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"设置上报周期指令缓存指令：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/reportingCycleRead")
    public String reportingCycleRead(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode = EncodeReportingCycle.read(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读取上报周期指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读取上报周期指令缓存指令：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/readTerminalNumber")
    public String readTerminalNumber(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode = EncodeReadTerminalNumber.read(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读取终端编号指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读取终端编号指令缓存指令：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/flowmeterParamsRead")
    public String flowmeterParamsRead(String deviceId,String deviceType,String key,String version,String flowmeterLocationNumber) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //参数位置号 01-04
        params.put("parameterLocation",flowmeterLocationNumber);
        String encode = EncodeSetFlowmeterParams.read(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读取流量计参数指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读取流量计参数指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/setRetransmissionInterva")
    public String setRetransmissionInterva(String deviceId,String deviceType,String key,String version,String rewireTimes,String rewireInterval) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //重发间隔分钟数 2字节
        params.put("minute",rewireInterval);
        //重发次数 1字节
        params.put("times",rewireTimes);
        String encode = EncodeRetransmissionInterva.set(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"设置重发间隔指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"设置重发间隔指令：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/readHourRecord")
    public String readHourRecord(String deviceId,String deviceType,String key,String version,String recordHour,String flowerNumHour) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //历史记录时段 81-88
        params.put("timeSlot",recordHour);
        //流量计编号转为28位
        params.put("flowerNum",flowerNumHour);
        String encode = EncodeHistoryRecord.readHourRecord(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读小时历史记录指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读小时历史记录指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/readDayRecord")
    public String readDayRecord(String deviceId,String deviceType,String key,String version,String recordDay,String flowerNumDay) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //记录日期 20200630
        params.put("recordDate",recordDay);
        //流量计编号转为28位
        params.put("flowerNum",flowerNumDay);
        String encode = EncodeHistoryRecord.readDayRecord(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读取天历史记录指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读取天历史记录指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/readMonthRecord")
    public String readMonthRecord(String deviceId,String deviceType,String key,String version,String flowerNumMonth,String  recordMonth) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
     //记录日期 202006
     params.put("recordDate",recordMonth);
     //流量计编号转为28位
     params.put("flowerNum",flowerNumMonth);
     String encode = EncodeHistoryRecord.readMonthRecord(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读月历史记录指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读月历史记录指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     * 清除终端异常45H
     * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @param balance
     * @param valveEx
     * @param overdrawStatus
     * @param saveEx
     * @param meterLowPre
     * @param leakageEx
     * @param farAwayLowPre
     * @param tiltEx
     * @param standbyVoltageEx
     * @param highTemEx
     * @param valveControlVolLow
     * @param noReportDays
     * @param farAwayExternalEle
     * @param valveCloseOne
     * @param fourTelEx
     * @param openLidEx
     * @return
     * @throws Exception
     */
    @RequestMapping("/clearEx")
    public String clearEx(String deviceId,String deviceType,String key,String version,String balance,String valveEx,String  overdrawStatus,String saveEx,String meterLowPre,
                          String leakageEx,String farAwayLowPre,String tiltEx,String standbyVoltageEx,String highTemEx,String valveControlVolLow,
                          String noReportDays,String farAwayExternalEle,String valveCloseOne,String fourTelEx,String openLidEx) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //功能位1 1字节 清除0 不清1
        params.put("functionOne",fourTelEx+farAwayExternalEle+valveControlVolLow+standbyVoltageEx+farAwayLowPre+meterLowPre+overdrawStatus+balance);
        //功能位2 1字节 清除0 不清1
        params.put("functionTwo",openLidEx+valveCloseOne+noReportDays+highTemEx+tiltEx+leakageEx+saveEx+valveEx);
        String encode = EncodeClearEx.clearEx(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"清除终端异常命令指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"清除终端异常命令指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     *读终端状态46H
     * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @return
     * @throws Exception
     */
    @RequestMapping("/readDeviceStatus")
    public String readDeviceStatus(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode = EncodeClearEx.read(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n"+"设备编号："+deviceId+"\n"+"读终端状态指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读终端状态指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     *读取系统时钟06H
     * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @return
     * @throws Exception
     */
    @RequestMapping("/systemClock")
    public String systemClock(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode = EncodeSystemClock.encode(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n"+"设备编号："+deviceId+"\n"+"读取系统时钟指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读取系统时钟指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     * 读IC卡ICCID号
     * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @return
     * @throws Exception
     */
    @RequestMapping("/iccIDNum")
    public String iccIDNum(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode = EncodeIccIDNum.read(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n"+"设备编号："+deviceId+"\n"+"读IC卡ICCID号指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n设备编号："+deviceId+"\n"+"读IC卡ICCID号指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     *设置无上报关阀天数2B
     * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @param noReportCloseValveDays
     * @return
     * @throws Exception
     */
    @RequestMapping("/setNoReportCloseDays")
    public String setNoReportCloseDays(String deviceId,String deviceType,String key,String version,String noReportCloseValveDays) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        params.put("day",noReportCloseValveDays.trim());
        String encode =EncodeValveControl.setNoReportCloseDays(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n"+"设备编号："+deviceId+"\n"+"设置无上报关阀天数指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"设置无上报关阀天数指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
   @RequestMapping("/readNoReportCloseDays")
    public String readNoReportCloseDays(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode =EncodeValveControl.readNoReportCloseDays(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n"+"设备编号："+deviceId+"\n"+"读无上报关阀天数指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"读无上报关阀天数指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     *设置无用气关阀天数14H
     * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @param noUseGasCloseValveDays
     * @return
     * @throws Exception
     */
    @RequestMapping("/setNoUseGasCloseDays")
    public String setNoUseGasCloseDays(String deviceId,String deviceType,String key,String version,String noUseGasCloseValveDays) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        params.put("day",noUseGasCloseValveDays.trim());
        String encode =EncodeValveControl.setNoUseGasCloseDays(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n"+"设备编号："+deviceId+"\n"+"设置无用气关阀天数指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"设置无用气关阀天数指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     * 读无用气关阀天数15H
      * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @return
     * @throws Exception
     */
    @RequestMapping("/readNoUseGasCloseDays")
    public String readNoUseGasCloseDays(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());

        String encode =EncodeValveControl.readNoUseGasCloseDays(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n"+"设备编号："+deviceId+"\n"+"读无用气关阀天数指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"读无用气关阀天数指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     *设置关阀一次累计气量值(42H)
     * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @param closeValveType
     * @param totalGas
     * @return
     * @throws Exception
     */
    @RequestMapping("/setCloseOneTimes")
    public String setCloseOneTimes(String deviceId,String deviceType,String key,String version,String closeValveType,String totalGas) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
//累计气量DOUBLE
        params.put("totalGas",totalGas);
        //关阀类型00警告01强制
        params.put("closeType",closeValveType);

        String encode = EncodeValveControl.setCloseOneTimes(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n"+"设备编号："+deviceId+"\n"+"设置关阀一次累计气量值指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"设置关阀一次累计气量值指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     * 读关阀一次累计气量值(43H)
     * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @return
     * @throws Exception
     */
    @RequestMapping("/redCloseOneTimes")
    public String redCloseOneTimes(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode = EncodeValveControl.redCloseOneTimes(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n"+"设备编号："+deviceId+"\n"+"读关阀一次累计气量值指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"读关阀一次累计气量值指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }

    /**
     * 设置余量报警额度(58H)
     * @param deviceId
     * @param deviceType
     * @param key
     * @param version
     * @param meterType
     * @param valveOne
     * @param surplusOne
     * @param surplusTwo
     * @param valveTwo
     * @param surplusThree
     * @param valveThree
     * @param surplusFour
     * @param valveFour
     * @param surplusFive
     * @param valveFive
     * @return
     * @throws Exception
     */
    @RequestMapping("/allowAlarmLimit")
    public String allowAlarmLimit(String deviceId,String deviceType,String key,String version,String meterType,String valveOne,String surplusOne,
                                  String surplusTwo,String valveTwo,String surplusThree,String valveThree,String surplusFour,String valveFour,
                                  String surplusFive,String valveFive) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //00气量模式 01金额模式
        params.put("countType",meterType);
        //剩余量Double
        params.put("surplusOne",surplusOne.trim());
        params.put("surplusTwo",surplusTwo.trim());
        params.put("surplusThree",surplusThree.trim());
        params.put("surplusFour",surplusFour.trim());
        params.put("surplusFive",surplusFive.trim());
        //阀控标识 00无动作 22警告性关阀 33强制关阀
        params.put("valveOne",valveOne);
        params.put("valveTwo",valveTwo);
        params.put("valveThree",valveThree);
        params.put("valveFour",valveFour);
        params.put("valveFive",valveFive);

        String encode = EncodeAllowAlarm.encode(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n"+"设备编号："+deviceId+"\n"+"设置余量报警额度指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"设置余量报警额度指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/readAllowAlarm")
    public String readAllowAlarm(String deviceId,String deviceType,String key,String version) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode = EncodeAllowAlarm.readAllowAlarm(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"读取余量报警额度指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"读取余量报警额度指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/readReportInfoCommand")
    public String readReportInfoCommand(String deviceId,String deviceType,String key,String version,String flowerNum) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //流量计编号转为28位
        params.put("flowerNum",flowerNum);
        String encode = EncodeReadReportInfoCommand.encode(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"读分体机上报信息指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"读分体机上报信息指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    @RequestMapping("/keyIssue")
    public String keyIssue(String deviceId,String deviceType,String key,String version,String flowerNum) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        String encode = EncodeKeyIssue.encode(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"下发密钥指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("\n\n\n设备编号："+deviceId+"\n"+"下发密钥指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
    /*  @RequestMapping("/xml")
    public XML xmlReceive(@RequestBody String xml){


        Document document = XmlUtil.readXML(xml);

        return null;
    }*/
    private static final char[] DIGITS
            = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static final String toHex(byte[] data) {
        final StringBuffer sb = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(DIGITS[(data[i] >>> 4) & 0x0F]);
            sb.append(DIGITS[data[i] & 0x0F]);
        }
        return sb.toString();
    }

/**
 * pub
 */
/*

    @RequestMapping("/xx")
    public String xx(String deviceId,String deviceType,String key,String version,String flowmeterLocationNumber) throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        //版本号
        params.put("version",version);
        //设备类型
        params.put("deviceType",deviceType);
        //设备编号
        params.put("deviceAddr",deviceId);
        //密钥
        params.put("key",key.trim());
        //参数位置号 01-04
        params.put("parameterLocation",flowmeterLocationNumber);
        String encode = EncodeSetFlowmeterParams.read(params);
        boolean b = redisUtils.lSet(deviceId+"command", encode);
        int i=0;
        if (b){
            i=6;
            WebSocketServer.sendInfo("设备编号："+deviceId+"\n"+"读取流量计参数指令已缓存：\n"+encode,deviceId);
        } else {
            WebSocketServer.sendInfo("设备编号："+deviceId+"\n"+"读取流量计参数指令缓存：缓存失败",deviceId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",i);
        return jsonObject.toString();

    }
*/

}
