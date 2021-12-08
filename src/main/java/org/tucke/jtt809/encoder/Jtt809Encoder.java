package org.tucke.jtt809.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.tucke.gnsscenter.GnssCenterService;
import org.tucke.jtt809.common.CRC16CCITT;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.common.Jtt809Util;
import org.tucke.jtt809.packet.common.OuterPacket;

/**
 * @author tucke
 */
public class Jtt809Encoder extends MessageToByteEncoder<OuterPacket> {


    @SuppressWarnings("SpellCheckingInspection")
    @Override
    protected void encode(ChannelHandlerContext ctx, OuterPacket packet, ByteBuf out) throws Exception {
        if (packet == null) {
            return;
        }
        int gnsscenterId;
        if (ctx.channel().hasAttr(Jtt809Constant.NettyAttribute.GNSS_CENTER_ID)) {
            gnsscenterId = Integer.parseInt(ctx.channel().attr(Jtt809Constant.NettyAttribute.GNSS_CENTER_ID).get());
        } else {
            gnsscenterId = packet.getGnsscenterId();
        }
        ByteBuf body = packet.getBody();
        if (body == null) {
            body = Unpooled.buffer();
        }
        // 24 = 头标识[1] + 数据头[22 = 长度[4] + 序列号[4] + 数据类型[2] + 接入码[4] + 版本号[3] + 加密标识[1] + 密钥[4]] + 尾标识[1]
        int len = body.readableBytes() + 24;
        out.markReaderIndex();
        // 数据长度
        out.writeInt(len);
        // 序列号
        out.writeInt(GnssCenterService.getInstance().serialNo(gnsscenterId));
        // 业务数据类型
        out.writeShort(packet.getId());
        // 下级平台接入码
        out.writeInt(gnsscenterId);
        // 版本号
        out.writeByte(1);
        out.writeByte(0);
        out.writeByte(0);
        // 报文加密标识位
        out.writeByte(0);
        // 数据加密的密钥
        out.writeInt(0);
        // 数据体
        out.writeBytes(ByteBufUtil.getBytes(body));
        // 校验码
        byte[] crcBytes = new byte[out.readableBytes()];
        out.readBytes(crcBytes);
        out.writeShort(CRC16CCITT.crc16(crcBytes));

        // 转义
        out.resetReaderIndex();
        byte[] escapeBytes = new byte[out.readableBytes()];
        out.readBytes(escapeBytes);

        // 重置下标
        out.setIndex(0, 0);
        // 包头标识
        out.writeByte(Jtt809Constant.PACKET_HEAD_FLAG);
        // 数据内容
        out.writeBytes(Jtt809Util.escape(escapeBytes));
        // 包尾标识
        out.writeByte(Jtt809Constant.PACKET_END_FLAG);
    }

}
