package org.tucke.jtt809.handler.protocol.exg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.common.Jtt809Util;
import org.tucke.jtt809.handler.protocol.Protocol;
import org.tucke.jtt809.packet.common.OuterPacket;
import org.tucke.jtt809.packet.upexg.UpExgHistoryPacket;
import org.tucke.jtt809.packet.upexg.UpExgRealLocationPacket;
import org.tucke.jtt809.packet.upexg.UpExgRegisterPacket;

/**
 * @author tucke
 */
@Slf4j
public class VehicleExgProtocol implements Protocol {

    @Override
    public boolean support(int id) {
        return id == Jtt809Constant.DataType.UP_EXG_MSG;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, OuterPacket outerPacket) {
        ByteBuf subBody;
        if (outerPacket.getBody() == null) {
            subBody = Unpooled.buffer();
        } else {
            subBody = Unpooled.wrappedBuffer(outerPacket.getBody());
        }
        String vehicleNo = Jtt809Util.readGBKString(subBody, 21);
        Byte vehicleColor = subBody.readByte();
        int dataType = subBody.readUnsignedShort();
        int dataLength = subBody.readInt();
        switch (dataType) {
            case Jtt809Constant.SubDataType.UP_EXG_MSG_REGISTER:
                UpExgRegisterPacket uep = UpExgRegisterPacket.decode(subBody);
                uep.complete(vehicleNo, vehicleColor);
                registerHandle(ctx, uep);
                break;
            case Jtt809Constant.SubDataType.UP_EXG_MSG_REAL_LOCATION:
                UpExgRealLocationPacket uerlp = UpExgRealLocationPacket.decode(subBody);
                uerlp.complete(vehicleNo, vehicleColor);
                realLocationHandle(ctx, uerlp);
                break;
            case Jtt809Constant.SubDataType.UP_EXG_MSG_HISTORY_LOCATION:
                UpExgHistoryPacket uehp = UpExgHistoryPacket.decode(subBody);
                uehp.complete(vehicleNo, vehicleColor);
                historyHandle(ctx, uehp);
                break;
            default:
        }
        ReferenceCountUtil.release(subBody);
    }

    private void registerHandle(ChannelHandlerContext ctx, UpExgRegisterPacket packet) {
        log.info("上传车辆注册信息：{}", packet.toString());
    }

    private void realLocationHandle(ChannelHandlerContext ctx, UpExgRealLocationPacket packet) {
        log.info("实时上传车辆定位信息：{}", packet.toString());
    }

    private void historyHandle(ChannelHandlerContext ctx, UpExgHistoryPacket packet) {
        log.info("补报上传车辆定位信息：{}", packet.toString());
    }

}
