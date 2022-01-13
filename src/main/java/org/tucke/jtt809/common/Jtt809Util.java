package org.tucke.jtt809.common;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.TimeZone;

/**
 * @author tucke
 */
@SuppressWarnings({"AlibabaUndefineMagicConstant", "UnusedReturnValue", "AlibabaLowerCamelCaseVariableNaming", "unused", "BooleanMethodIsAlwaysInverted"})
@Slf4j
public class Jtt809Util {

    /**
     * 0x5B, 0x5A, 0x5D, 0x5E 转义处理
     */
    public static byte[] escape(byte[] bytes) {
        // 最极端情况，每个byte都需要转义，所以使用2倍长度
        byte[] result = new byte[bytes.length * 2];
        int i = 0;
        for (byte b : bytes) {
            switch (b) {
                case 0x5B:
                    result[i++] = 0x5A;
                    result[i++] = 0x01;
                    break;
                case 0x5A:
                    result[i++] = 0x5A;
                    result[i++] = 0x02;
                    break;
                case 0x5D:
                    result[i++] = 0x5E;
                    result[i++] = 0x01;
                    break;
                case 0x5E:
                    result[i++] = 0x5E;
                    result[i++] = 0x02;
                    break;
                default:
                    result[i++] = b;
            }
        }
        // 截取转义后的数据并返回
        return Arrays.copyOf(result, i);
    }

    /**
     * 0x5B, 0x5A, 0x5D, 0x5E 反转义处理
     */
    public static byte[] unescape(byte[] bytes) {
        if (bytes == null || bytes.length <= 1) {
            return bytes;
        }
        // 最极端情况，每个byte都不需要反转义，所以使用1倍长度
        byte[] result = new byte[bytes.length];
        int ii = 0;
        for (int i = 0; i < bytes.length; i++) {
            // 当前循环的 byte 数据
            byte curr = bytes[i];
            // 若最后一条 byte 数据还能进入循环，则它必定不满足反转义
            if (i == bytes.length - 1) {
                result[ii++] = curr;
                break;
            }
            // 下一条 byte 数据
            byte next = bytes[i + 1];
            if (curr == 0x5A) {
                // 将 0x5A 0x01 反转义为 0x5B，且下一条数据 0x01 不需要参与循环
                if (next == 0x01) {
                    result[ii++] = 0x5B;
                    i++;
                    continue;
                }
                // 0x5A 0x02 反转义结果就是 0x5A，且下一条数据 0x02 不需要参与循环
                if (next == 0x02) {
                    i++;
                }
            }
            if (curr == 0x5E) {
                // 将 0x5E 0x01 反转义为 0x5D，且下一条数据 0x01 不需要参与循环
                if (next == 0x01) {
                    result[ii++] = 0x5D;
                    i++;
                    continue;
                }
                // 0x5E 0x02 反转义结果就是 0x5E，且下一条数据 0x02 不需要参与循环
                if (next == 0x02) {
                    i++;
                }
            }
            result[ii++] = curr;
        }
        // 截取反转义后的数据并返回
        return Arrays.copyOf(result, ii);
    }

    /**
     * 消息校验
     */
    public static boolean validate(ByteBuf byteBuf) {
        int len = byteBuf.readableBytes();
        byte[] bytes = new byte[len - 2];
        byteBuf.getBytes(0, bytes);
        int calc = CRC16CCITT.crc16(bytes);
        int code = byteBuf.getUnsignedShort(len - 2);
        boolean result = calc == code;
        if (!result) {
            log.warn("CRC校验失败！计算结果为：{}, 传入值为：{}", calc, code);
        }
        return result;
    }

    /**
     * 加密
     */
    public static byte[] encrypt(int m1, int ia1, int ic1, long key, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (key == 0) {
            key = 1;
        }
        for (int i = 0; i < bytes.length; i++) {
            key = ia1 * (key % m1) + ic1;
            bytes[i] ^= ((key >> 20) & 0xFF);
        }
        return bytes;
    }

    /**
     * 解密
     */
    public static byte[] decrypt(int m1, int ia1, int ic1, long key, byte[] bytes) {
        return encrypt(m1, ia1, ic1, key, bytes);
    }

    /**
     * 解析字符串
     *
     * @param complement 是否考虑右边补零的情况
     */
    public static String readString(ByteBuf byteBuf, int length, Charset charset, boolean complement) {
        // 是否考虑右补十六进制 0x00
        if (complement) {
            byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes);
            int len = 0;
            for (int i = bytes.length; i > 0; i--) {
                if (bytes[i - 1] != 0x00) {
                    len = i;
                    break;
                }
            }
            return new String(Arrays.copyOf(bytes, len), charset);
        } else {
            return byteBuf.readBytes(length).toString(charset);
        }
    }

    /**
     * 解析GBK字符串
     *
     * @param complement 是否考虑右边补零的情况
     */
    public static String readGBKString(ByteBuf byteBuf, int length, boolean complement) {
        return readString(byteBuf, length, Charset.forName("GBK"), complement);
    }

    /**
     * 解析GBK字符串
     */
    public static String readGBKString(ByteBuf byteBuf, int length) {
        return readGBKString(byteBuf, length, true);
    }

    /**
     * 解析时间
     */
    public static long parseDateTime(ByteBuf byteBuf) {
        String date = byteBuf.readByte() + "-" + byteBuf.readByte() + "-" + byteBuf.readShort() + " " +
                byteBuf.readByte() + ":" + byteBuf.readByte() + ":" + byteBuf.readByte();
        long time = 0L;
        try {
            FastDateFormat format = FastDateFormat.getInstance("dd-MM-yyyy HH:mm:ss", TimeZone.getTimeZone("GMT+8:00"));
            time = format.parse(date).getTime();
        } catch (ParseException e) {
            log.warn("日期 [{}] 解析错误", date);
        }
        return time;
    }

}
