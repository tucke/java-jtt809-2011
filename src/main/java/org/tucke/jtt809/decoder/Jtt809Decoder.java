package org.tucke.jtt809.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.tucke.gnsscenter.GnssCenterService;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.common.Jtt809Util;
import org.tucke.jtt809.packet.common.OuterPacket;

import java.util.List;

/**
 * @author tucke
 */
@Slf4j
public class Jtt809Decoder extends MessageToMessageDecoder<ByteBuf> {

    @SuppressWarnings({"AlibabaUndefineMagicConstant", "SpellCheckingInspection"})
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // 写个判断，线上环境就不需要执行 ByteBufUtil.hexDump
        if (log.isDebugEnabled()) {
            log.debug("收到一条消息：{}5d", ByteBufUtil.hexDump(msg));
        }
        // 判断包头
        if (msg.readByte() != Jtt809Constant.PACKET_HEAD_FLAG) {
            msg.resetReaderIndex();
            log.warn("消息包头错误: {}5d", ByteBufUtil.hexDump(msg));
            return;
        }
        byte[] readableBytes = new byte[msg.readableBytes()];
        msg.readBytes(readableBytes);
        // 反转义处理
        byte[] bytes = Jtt809Util.unescape(readableBytes);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        // crc校验
        if (!Jtt809Util.validate(byteBuf)) {
            return;
        }

        /* 解析外层包 */
        // 长度
        long length = byteBuf.readUnsignedInt();
        // 长度校验, 反转义之后数组加上包头和包尾长度与解析出来的长度对比；
        // 因为数据长度不包含校验码，而此时解析出来的数据不包含头尾标识，刚好都是2个字节，所以两个长度应该相等
        if (length != bytes.length) {
            return;
        }
        // 报文序列号
        long sn = byteBuf.readUnsignedInt();
        // 业务数据类型
        int id = byteBuf.readUnsignedShort();
        // 下级平台接入码
        int gnsscenterId = byteBuf.readInt();
        ctx.channel().attr(Jtt809Constant.NettyAttribute.GNSS_CENTER_ID).set(String.valueOf(gnsscenterId));
        // 协议版本号标识
        String version = "v" + byteBuf.readByte() + "." + byteBuf.readByte() + "." + byteBuf.readByte();
        // 报文加密标识位
        byte encryptFlag = byteBuf.readByte();
        // 数据加密解密的密匙
        long encryptKey = byteBuf.readUnsignedInt();
        // 消息体
        ByteBuf body;
        if (encryptFlag == 1) {
            byte[] encryptedBytes = new byte[byteBuf.readableBytes() - 2];
            byteBuf.readBytes(encryptedBytes);
            // 解密
            int[] param = GnssCenterService.getInstance().getDecryptParam(gnsscenterId);
            Jtt809Util.decrypt(param[0], param[1], param[2], encryptKey, encryptedBytes);
            body = Unpooled.wrappedBuffer(encryptedBytes);
        } else {
            body = byteBuf.readBytes(byteBuf.readableBytes() - 2);
        }
        // 校验码
        int crcCode = byteBuf.readUnsignedShort();
        ReferenceCountUtil.release(byteBuf);
        out.add(new OuterPacket(length, sn, id, gnsscenterId, version, encryptFlag, encryptKey, body, crcCode));
    }

}
