package org.tucke.jtt809.packet.upexg;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

/**
 * @author tucke
 */
@Slf4j
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class UpExgRealLocationPacket extends UpExgPacket {

    private static final BigDecimal LAT_LON_DIVISOR = new BigDecimal(1000000);

    private byte encrypt;
    private long timestamp;
    private double lon;
    private double lat;
    private short vec1;
    private short vec2;
    private int vec3;
    private short direction;
    private short altitude;
    private int state;
    private int alarm;

    public static UpExgRealLocationPacket decode(ByteBuf byteBuf) {
        UpExgRealLocationPacket packet = new UpExgRealLocationPacket();
        packet.setEncrypt(byteBuf.readByte());
        String date = byteBuf.readByte() + "-" + byteBuf.readByte() + "-" + byteBuf.readShort() + " " +
                byteBuf.readByte() + ":" + byteBuf.readByte() + ":" + byteBuf.readByte();
        try {
            long time = DateUtils.parseDate(date, "dd-MM-yyyy HH:mm:ss").getTime();
            packet.setTimestamp(time);
        } catch (ParseException e) {
            log.warn("日期 [{}] 解析错误", date);
        }
        BigDecimal lon = new BigDecimal(String.valueOf(byteBuf.readInt()));
        BigDecimal lat = new BigDecimal(String.valueOf(byteBuf.readInt()));
        try {
            packet.setLon(lon.divide(LAT_LON_DIVISOR, 6, RoundingMode.UNNECESSARY).doubleValue());
            packet.setLat(lat.divide(LAT_LON_DIVISOR, 6, RoundingMode.UNNECESSARY).doubleValue());
        } catch (ArithmeticException e) {
            log.warn("经纬度 [{}, {}] 解析错误", lon, lat);
        }
        packet.setVec1(byteBuf.readShort());
        packet.setVec2(byteBuf.readShort());
        packet.setVec3(byteBuf.readInt());
        packet.setDirection(byteBuf.readShort());
        packet.setAltitude(byteBuf.readShort());
        packet.setState(byteBuf.readInt());
        packet.setAlarm(byteBuf.readInt());
        return packet;
    }

}
