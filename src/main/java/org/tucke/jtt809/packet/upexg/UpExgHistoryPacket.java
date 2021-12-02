package org.tucke.jtt809.packet.upexg;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tucke
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpExgHistoryPacket extends UpExgPacket {

    private byte cnt;
    private List<UpExgRealLocationPacket> locations;

    public static UpExgHistoryPacket decode(ByteBuf byteBuf) {
        UpExgHistoryPacket packet = new UpExgHistoryPacket();
        byte cnt = byteBuf.readByte();
        packet.setCnt(byteBuf.readByte());
        List<UpExgRealLocationPacket> locations = new ArrayList<>();
        for (int i = cnt; i > 0; i--) {
            locations.add(UpExgRealLocationPacket.decode(byteBuf));
        }
        packet.setLocations(locations);
        return packet;
    }

}
