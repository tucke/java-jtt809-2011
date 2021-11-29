package org.tucke.jtt809.common;

/**
 * @author tucke
 */
@SuppressWarnings("AlibabaClassNamingShouldBeCamel")
public class CRC16CCITT {

    public static int crc16(byte[] bytes){
        int crc = 0xFFFF;
        for (byte aByte : bytes) {
            crc = ((crc >>> 8) | (crc << 8)) & 0xFFFF;
            // byte to int, trunc sign
            crc ^= (aByte & 0xFF);
            crc ^= ((crc & 0xFF) >> 4);
            crc ^= (crc << 12) & 0xFFFF;
            crc ^= ((crc & 0xFF) << 5) & 0xFFFF;
        }
        crc &= 0xFFFF;
        return crc;
    }

}
