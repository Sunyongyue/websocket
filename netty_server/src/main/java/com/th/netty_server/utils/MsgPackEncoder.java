package com.th.netty_server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * MsgPack编码器
 * <p>
 * create by 叶云轩 at 2018/4/12-下午7:29
 * contact by tdg_yyx@foxmail.com
 */
public class MsgPackEncoder extends MessageToByteEncoder {



    /**
     * MsgPackEncoder 日志控制器
     * Create by 叶云轩 at 2018/5/3 下午3:15
     * Concat at tdg_yyx@foxmail.com
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MsgPackEncoder.class);

    private final Charset charset;


    public MsgPackEncoder() {
        this(Charset.defaultCharset());
    }

    public MsgPackEncoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        LOGGER.info("\n\t⌜⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓\n" +
                "\t├ [编码]: {}\n" +
                "\t⌞⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓⎓", msg.toString());
        MessagePack messagePack = new MessagePack();
        //byte[] write = messagePack.write(msg);
        //msg.toString().replace("..2","")
        byte[] write = hexStr2Bytes(msg.toString());
        byteBuf.writeBytes(write);
    }

    public static byte[] hexStr2Bytes(String src){
        /*✔输入的值进行规范化管理*/
        src = src.trim().replace(" ","").toUpperCase(Locale.US);
        //处理值初始化
        int m=0,n=0;
        int iLen = src.length()/2; //计算长度
        byte[] ret = new byte[iLen];//分配存储空间
        for(int i=0;i<iLen;i++){
            m = i*2+1;
            n = m+1;
            ret[i] = (byte)(Integer.decode("0x"+src.substring(i*2,m)+src.substring(m,n)) & 0xFF);

        }
        return ret;
    }



    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }



}
