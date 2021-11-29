package org.tucke.jtt809.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author tucke
 */
@SuppressWarnings("SpellCheckingInspection")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OuterPacket implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据长度(包括头标识、数据头、数据体和尾标识)
     */
    private long length;
    /**
     * 报文序列号
     * 占用四个字节，为发送信息的序列号，用于接收方检测是否有信息的丢失，上级平台和下级平台接自己发送数据包的个数计数，互不影响。
     * 程序开始运行时等于零，发送第一帧数据时开始计数，到最大数后自动归零
     */
    private long sn;
    /**
     * 业务数据类型
     */
    private int id;
    /**
     * 下级平台接入码，上级平台给下级平台分配唯一标识码
     */
    private int gnsscenterId;
    /**
     * 协议版本号标识，上下级平台之间采用的标准协议版编号
     * 长度为 3 个字节来表示，0x01 0x02 0x0F 表示的版本号是 v1.2.15，以此类推
     */
    private String version;
    /**
     * 报文加密标识位
     * 0 - 报文不加密
     * 1 - 报文加密, 后继相应业务的数据体采用 ENCRYPT_KEY 对应的密钥进行加密处理
     */
    private byte encryptFlag;
    /**
     * 数据加密解密的密匙，长度为 4 个字节
     */
    private long encryptKey;
    /**
     * 消息体
     */
    private ByteBuf body;
    /**
     * 数据 CRC 校验码
     */
    private int crcCode;

    public OuterPacket(int id, ByteBuf body) {
        this.id = id;
        this.body = body;
    }

    public OuterPacket(int id, int gnsscenterId, ByteBuf body) {
        this.id = id;
        this.gnsscenterId = gnsscenterId;
        this.body = body;
    }

}
