package org.tucke.gnsscenter;

import org.tucke.jtt809.packet.connect.UpConnectPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 下级平台配置服务
 *
 * @author tucke
 */
@SuppressWarnings("SpellCheckingInspection")
public class GnssCenterService {

    private final Map<Integer, UpConnectPacket.Request> DOWN_REQUEST = new ConcurrentHashMap<>();
    private volatile static GnssCenterService instance;

    private GnssCenterService() {
    }

    public static GnssCenterService getInstance() {
        if (instance == null) {
            synchronized (GnssCenterService.class) {
                if (instance == null) {
                    instance = new GnssCenterService();
                }
            }
        }
        return instance;
    }

    public void start() throws Exception {
        // TODO 加载下级平台配置信息
    }

    /**
     * 获取加密的参数
     *
     * @param gnsscenterId 下级平台接入码
     * @return {M1, IA1, IC1}
     */
    public int[] getEncryptParam(int gnsscenterId) {
        // TODO
        return new int[]{1, 2, 3};
    }

    /**
     * 获取解密的参数
     *
     * @param gnsscenterId 下级平台接入码
     * @return {M1, IA1, IC1}
     */
    public int[] getDecryptParam(int gnsscenterId) {
        return getEncryptParam(gnsscenterId);
    }

    /**
     * 验证下级平台登录
     */
    public byte validateLogin(int gnsscenterId, UpConnectPacket.Request request) {
        // TODO
        // 首先验证 IP 地址
        // 其次验证接入码、用户名以及密码
        byte result = 0x00;
        if (result == 0x00) {
            DOWN_REQUEST.put(gnsscenterId, request);
        }
        return result;
    }

    public UpConnectPacket.Request getDownRequest(int gnsscenterId) {
        return DOWN_REQUEST.get(gnsscenterId);
    }

    public void stop() {

    }

}
