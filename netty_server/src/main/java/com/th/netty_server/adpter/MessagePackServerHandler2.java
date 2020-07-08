package com.th.netty_server.adpter;

import com.alibaba.fastjson.JSONObject;

import com.jnthyb.protocol.encode.ProtocolEncodeMain;
import com.th.netty_server.config.WebSocketServer;
import com.th.netty_server.utils.RedisUtils;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * <p>
 * create by 叶云轩 at 2018/4/12-下午4:12
 * contact by tdg_yyx@foxmail.com
 *
 * @author 叶云轩 contact by tdg_yyx@foxmail.com
 * @date 2018/8/15 - 12:32
 */
public class MessagePackServerHandler2 extends ChannelHandlerAdapter {

    private RedisUtils redisUtils;

    public MessagePackServerHandler2( RedisUtils redisUtils
                                     ) {
        this.redisUtils = redisUtils;
    }

    public final Map<String, String> derviceTypeMap = new HashMap<String, String>() {{
        put("民用表", "2");
        put("集中器", "1");
        put("控制器", "0");
    }};
    /**
     * MessagePackServerHandler 日志控制器
     * Create by 叶云轩 at 2018/4/12 下午4:20
     * Concat at tdg_yyx@foxmail.com
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePackServerHandler2.class);

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.info("--- [发生异常] 释放资源: {}", cause.getMessage());
        // todo
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String clientIP = socketAddress.getAddress().getHostAddress();
        String clientPort = String.valueOf(socketAddress.getPort());
        ctx.writeAndFlush("Server connect success");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
//            LOGGER.info("MSG-------------------------MSG------------{}", msg.toString());
            JSONObject jsonObject = JSONObject.parseObject(msg.toString());
            LOGGER.info("\n\t⌜⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓\n" +
                    "\t├ [接收 转换]: {}\n" +
                    "\t⌞⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓", jsonObject.toJSONString());
            //将9C 转成 10进制
            WebSocketServer.sendInfo(jsonObject.toJSONString(),"123456");
            String resTypeString = jsonObject.getString("resType");
            Integer resType;
            if (resTypeString.equals("9C")) {
                resType = Integer.parseInt(resTypeString, 16);
            } else {
                resType = Integer.parseInt(resTypeString);
            }
            jsonObject.put("resType", resType);
            String deviceAddr = jsonObject.getString("deviceAddr");
            redisUtils.set("netty","netty");
            sendTimingComm(ctx,jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 发送校时指令
     *
     * @param ctx
     * @param jsonObject
     */
    public void sendTimingComm(ChannelHandlerContext ctx, JSONObject jsonObject) {
        String timingComm = getTimingComm(jsonObject);
        LOGGER.info("\n\t⌜⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓\n" +
                "\t├ [校时 ]: {}\n" +
                "\t⌞⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓", timingComm);
        ctx.writeAndFlush(timingComm);
    }


    private String getTimingComm(JSONObject toolInfo) {
        Map<String, Object> params = new HashMap<String, Object>();
        // 数据类型（9：时间校准）
        params.put("dataType", 9);
        // 包序号（字符串/数字都行）
        params.put("pktSer", 1);
        // 设备类型（字符串/数字都行）(0:集中器，1:集中器，2:民用表)
        params.put("deviceType", derviceTypeMap.get(toolInfo.getString("deviceType")));
        // 公司代码（字符串/数字都行）
        params.put("companyNum", toolInfo.getString("companyNum"));
        // 设备地址（字符串/数字都行）
        params.put("deviceAddr", toolInfo.getString("deviceAddr"));
        // 用户地址（字符串/数字都行）
        params.put("userId", 0);
        // 保留字段
        params.put("yearNc", 0);
        // 时间（字符串）
        params.put("replyTime", simpleDateFormat.format(new Date()));

        // 编码，时间校准不加密，这里传递哪个密钥都不起作用
        String res = ProtocolEncodeMain.encode(params, 0);
        return res.trim();
    }





}
