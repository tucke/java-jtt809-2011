package org.tucke.gnsscenter;

import org.tucke.jtt809.packet.connect.UpConnectPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 下级平台配置服务
 *
 * @author tucke
 */
@SuppressWarnings("SpellCheckingInspection")
public class GnssCenterService {

    private final Lock lock = new ReentrantLock();
    private final Map<Integer, AtomicInteger> SERIAL_NUMBER_MAP = new ConcurrentHashMap<>();
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

    }

    /**
     * 获取加密的参数
     *
     * @param gnsscenterId 下级平台接入码
     * @return {M1, IA1, IC1}
     */
    public int[] getEncryptParam(int gnsscenterId) {
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

    /**
     * 获取序列号
     * 1 - 同一个下级平台的序列号保证连续
     * 2 - 不同下级平台的序列号保证互不干扰
     * 3 - 序列号达到最大值后，需要清零
     *
     * @param gnsscenterId 下级平台接入码
     * @return 序列号
     */
    public int serialNo(int gnsscenterId) {
        int serialNumber;
        lock.lock();
        try {
            if (!SERIAL_NUMBER_MAP.containsKey(gnsscenterId)) {
                if (!SERIAL_NUMBER_MAP.containsKey(gnsscenterId)) {
                    SERIAL_NUMBER_MAP.put(gnsscenterId, new AtomicInteger());
                }
            }
            serialNumber = SERIAL_NUMBER_MAP.get(gnsscenterId).getAndIncrement();
            if (serialNumber == Integer.MAX_VALUE) {
                SERIAL_NUMBER_MAP.get(gnsscenterId).set(0);
            }
        } finally {
            lock.unlock();
        }
        return serialNumber;
    }

    public void stop() {

    }

}
