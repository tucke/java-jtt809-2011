package org.tucke.jtt809.packet.upexg;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.tucke.jtt809.common.Jtt809Util;

/**
 * @author tucke
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class UpExgRegisterPacket extends UpExgPacket {

    private String platformId;
    private String producerId;
    private String terminalModelType;
    private String terminalId;
    private String terminalSimCode;

    public static UpExgRegisterPacket decode(ByteBuf byteBuf) {
        UpExgRegisterPacket packet = new UpExgRegisterPacket();
        packet.setPlatformId(Jtt809Util.readGBKString(byteBuf, 11));
        packet.setProducerId(Jtt809Util.readGBKString(byteBuf, 11));
        packet.setTerminalModelType(Jtt809Util.readGBKString(byteBuf, 8));
        packet.setTerminalId(Jtt809Util.readGBKString(byteBuf, 7));
        packet.setTerminalSimCode(Jtt809Util.readGBKString(byteBuf, 12));
        return packet;
    }

}
