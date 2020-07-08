package com.th.netty_server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;

public class MessageDecoder extends LengthFieldBasedFrameDecoder{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
                          int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception{
        return super.decode(ctx, in);
    }
    @Override
    protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order){
        long frameLength;
        switch (length) {
            case 1:
                frameLength = buf.getUnsignedByte(offset);
                break;
            case 2:
                frameLength = buf.getUnsignedShort(offset);
                break;
            case 3:
                frameLength = buf.getUnsignedMedium(offset);
                break;
            case 4:
                //frameLength = buf.getUnsignedInt(offset);
                frameLength=buf.readableBytes();
                logger.info("==="+frameLength);
                byte[] cc=new byte[buf.readableBytes()];
                ByteBuf tmp=buf.copy();
                tmp.readBytes(cc);
                try {
                    String ss=new String(cc,"GBK");
                    frameLength=Integer.parseInt(ss.substring(offset,offset+length));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case 8:
                frameLength = buf.getLong(offset);
                break;
            default:
                throw new DecoderException(
                        "unsupported lengthFieldLength: "
                                + length + " (expected: 1, 2, 3, 4, or 8)");
        }
        return frameLength;
    }

}
