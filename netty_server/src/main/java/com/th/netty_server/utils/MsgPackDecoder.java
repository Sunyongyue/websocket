package com.th.netty_server.utils;

import com.jnthyb.protocol.decode.ProtocolDecodeMain;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MsgPack解码器
 * <p>
 * create by 叶云轩 at 2018/4/12-下午7:31
 * contact by tdg_yyx@foxmail.com
 */
public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuf> {


    /**
     * MsgPackDecoder 日志控制器
     * Create by 叶云轩 at 2018/5/3 下午3:15
     * Concat at tdg_yyx@foxmail.com
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MsgPackDecoder.class);

    /**
     * 解码 byte[] -> Object
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext
            , ByteBuf msg, List<Object> out) throws Exception {

        final int length = msg.readableBytes();
        final byte[] array = new byte[length];

        msg.getBytes(msg.readerIndex(), array, 0, length);
//        MessagePack messagePack = new MessagePack();
        String s = new String(array);
        String string = bytesToHexString(array);
        LOGGER.info("\n\t⌜⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓\n" +
                "\t├ [解码]: {}\n" +
                "\t⌞⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓", string);
        String decode = ProtocolDecodeMain.decode(string);

        out.add(decode);
    }

    public  String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static String toHexString1(byte[] b) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < b.length; ++i) {
            buffer.append(toHexString1(b[i]));
        }
        return buffer.toString();
    }

    public static String toHexString1(byte b) {
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            return "0" + s;
        } else {
            return s;
        }
    }
}
