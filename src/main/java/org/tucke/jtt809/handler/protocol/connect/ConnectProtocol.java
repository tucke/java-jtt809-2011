package org.tucke.jtt809.handler.protocol.connect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.tucke.gnsscenter.GnssCenterService;
import org.tucke.jtt809.Jtt809Client;
import org.tucke.jtt809.common.Jtt809Constant;
import org.tucke.jtt809.handler.protocol.Protocol;
import org.tucke.jtt809.packet.UpConnectPacket;
import org.tucke.jtt809.packet.UpDisConnectPacket;
import org.tucke.jtt809.packet.common.OuterPacket;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author tucke
 */
@SuppressWarnings("AlibabaUndefineMagicConstant")
@Slf4j
public class ConnectProtocol implements Protocol {

    private static final Set<Integer> DATA_TYPE = Set.of(
            Jtt809Constant.DataType.UP_CONNECT_REQ,
            Jtt809Constant.DataType.UP_DICONNECE_REQ,
            Jtt809Constant.DataType.UP_LINKTEST_REQ,
            Jtt809Constant.DataType.UP_DISCONNECT_INFORM,
            Jtt809Constant.DataType.UP_CLOSELINK_INFORM,
            Jtt809Constant.DataType.DOWN_CONNECT_RSP,
            Jtt809Constant.DataType.DOWN_DISCONNECT_RSP,
            Jtt809Constant.DataType.DOWN_LINKTEST_RSP
    );

    @Override
    public boolean support(int id) {
        return DATA_TYPE.contains(id);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, OuterPacket packet) {
        switch (packet.getId()) {
            case Jtt809Constant.DataType.UP_CONNECT_REQ:
                login(ctx, packet);
                break;
            case Jtt809Constant.DataType.UP_DICONNECE_REQ:
                logout(ctx, packet);
                break;
            case Jtt809Constant.DataType.UP_LINKTEST_REQ:
                keepLink(ctx, packet);
                break;
            case Jtt809Constant.DataType.UP_DISCONNECT_INFORM:
                disConnectInform(ctx, packet);
                break;
            case Jtt809Constant.DataType.UP_CLOSELINK_INFORM:
                closeLinkInform(ctx, packet);
                break;
            case Jtt809Constant.DataType.DOWN_CONNECT_RSP:
                downConnectRsp(ctx, packet);
                break;
            case Jtt809Constant.DataType.DOWN_DISCONNECT_RSP:
                downDisConnectRsp(ctx, packet);
                break;
            case Jtt809Constant.DataType.DOWN_LINKTEST_RSP:
                downLinkTestRsp(ctx, packet);
                break;
            default:
        }
    }

    /**
     * 处理下级平台登录请求
     * 链路类型：主链路
     */
    private void login(ChannelHandlerContext ctx, OuterPacket packet) {
        UpConnectPacket.Request request = UpConnectPacket.decode(packet.getBody());
        byte result = GnssCenterService.getInstance().validateLogin(packet.getGnsscenterId(), request);
        log.info("接入码：{}，用户：{}，密码：{}，结果：{}。", packet.getGnsscenterId(), request.getUserId(), request.getPassword(), result);
        // 随机一个校验码
        int verifyCode = ThreadLocalRandom.current().nextInt();
        ByteBuf body = UpConnectPacket.encode(new UpConnectPacket.Response(result, verifyCode));
        // 应答
        OuterPacket out = new OuterPacket(Jtt809Constant.DataType.UP_CONNECT_RSP, body);
        ctx.writeAndFlush(out);
        // 接入成功就建立从链接，否则关闭链接
        if (result == 0x00) {
            Jtt809Client.createClient(packet.getGnsscenterId(), request, new OuterPacket(Jtt809Constant.DataType.DOWN_CONNECT_REQ, body));
        } else {
            Jtt809Client.close(packet.getGnsscenterId());
            ctx.close();
        }
    }

    /**
     * 处理下级平台注销请求
     * 链路类型：主链路
     */
    private void logout(ChannelHandlerContext ctx, OuterPacket packet) {
        UpDisConnectPacket.Request request = UpDisConnectPacket.decode(packet.getBody());
        log.warn("用户：{} 请求注销！", request.getUserId());
        // 应答
        OuterPacket out = new OuterPacket(Jtt809Constant.DataType.UP_DISCONNECT_RSP, null);
        ctx.writeAndFlush(out);
        Jtt809Client.close(packet.getGnsscenterId());
        ctx.close();
    }

    /**
     * 保持连接
     * 链路类型：主链路
     */
    private void keepLink(ChannelHandlerContext ctx, OuterPacket packet) {
        log.info("下级平台 {} 的保持连接消息", packet.getGnsscenterId());
        // 应答
        OuterPacket out = new OuterPacket(Jtt809Constant.DataType.UP_LINKTEST_RSP, null);
        ctx.writeAndFlush(out);
    }

    /**
     * 下级平台往上级平台发送的中断通知
     * 链路类型：从链路
     */
    private void disConnectInform(ChannelHandlerContext ctx, OuterPacket packet) {
        byte errorCode = packet.getBody().readByte();
        // 0x00：主链路断开，0x01：其他原因
        // 无需应答
        log.warn("主链路断开通知消息，通知发送方：{}，原因是：{}", packet.getGnsscenterId(), errorCode == 0 ? "主链路断开" : "其他原因");
    }

    /**
     * 下级平台主动关闭主从链路通知消息
     * 链路类型：从链路
     */
    private void closeLinkInform(ChannelHandlerContext ctx, OuterPacket packet) {
        byte errorCode = packet.getBody().readByte();
        // 0x00：网关重启，0x01：其他原因
        // 无需应答
        log.warn("下级平台 {} 即将关闭主从链路，原因是：{}", packet.getGnsscenterId(), errorCode == 0 ? "网关重启" : "其他原因");
    }

    /**
     * 从链路连接应答消息
     * 链路类型：从链路
     */
    private void downConnectRsp(ChannelHandlerContext ctx, OuterPacket packet) {
        byte code = packet.getBody().readByte();
        String result = "未知";
        if (code == 0x00) {
            result = "成功";
        } else if (code == 0x01) {
            result = "校验码错误";
        } else if (code == 0x02) {
            result = "资源紧张，稍后再连接(已经占用)";
        } else if (code == 0x03) {
            result = "其他";
        }
        log.info("下级平台 {} 连接结果 {}", packet.getGnsscenterId(), result);
    }

    /**
     * 从链路注销应答消息
     * 链路类型：从链路
     */
    private void downDisConnectRsp(ChannelHandlerContext ctx, OuterPacket packet) {
        // 这是一条空消息
        log.warn("下级平台 {} 响应了从链路的注销请求消息", packet.getGnsscenterId());
    }

    /**
     * 从链路连接保持应答消息
     * 链路类型：从链路
     */
    private void downLinkTestRsp(ChannelHandlerContext ctx, OuterPacket packet) {
        // 这是一条空消息
        log.warn("下级平台 {} 响应了从链路的连接保持请求消息", packet.getGnsscenterId());
    }

}
