package org.tucke.jtt809.packet.upexg;

import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @author tucke
 */
@Data
public class UpExgPacket {

    private String vehicleNo;
    private byte vehicleColor;
    private short dataType;
    private int dataLength;
    private ByteBuf data;

    public void complete(String vehicleNo, Byte vehicleColor) {
        this.setVehicleNo(vehicleNo);
        this.setVehicleColor(vehicleColor);
    }

}
