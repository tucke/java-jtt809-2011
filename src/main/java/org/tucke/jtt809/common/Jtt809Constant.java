package org.tucke.jtt809.common;

import io.netty.util.AttributeKey;

/**
 * @author tucke
 */
public class Jtt809Constant {

    public static final String SERVER_NAME = "JT/T 809";

    public static final byte PACKET_HEAD_FLAG = 0x5B;
    public static final byte PACKET_END_FLAG = 0x5D;

    public static final class NettyAttribute {
        public static final AttributeKey<String> GNSS_CENTER_ID = AttributeKey.valueOf("GNSS_CENTER_ID");
    }

    /**
     * 业务数据类型标识
     * <p>
     * a) 上级平台向下级平台发送的请求消息，一般以 DOWN_ 开头，以后缀 _REQ 结尾；而下级平台向上级平台发送的请求消息一般以 UP_ 开头，以后缀 _REQ 结尾；
     * b) 当上下级平台之间有应答消息情况下，应答消息可继续沿用对应的请求消息开头标识符，而通过后缀 RSP 来标识结尾。
     */
    @SuppressWarnings({"SpellCheckingInspection", "unused"})
    public static class DataType {

        /**
         * 主链路登录请求消息
         */
        public final static int UP_CONNECT_REQ = 0x1001;
        /**
         * 主链路登录应答消息
         */
        public final static int UP_CONNECT_RSP = 0x1002;
        /**
         * 主链路注销请求消息
         */
        public final static int UP_DICONNECE_REQ = 0x1003;
        /**
         * 主链路注销应答消息
         */
        public final static int UP_DISCONNECT_RSP = 0x1004;
        /**
         * 主链路连接保持请求消息
         */
        public final static int UP_LINKTEST_REQ = 0x1005;
        /**
         * 主链路连接保持应答消息
         */
        public final static int UP_LINKTEST_RSP = 0x1006;
        /**
         * 主链路断开通知
         */
        public final static int UP_DISCONNECT_INFORM = 0x1007;
        /**
         * 下级平台主动关闭从链路通知
         */
        public final static int UP_CLOSELINK_INFORM = 0x1008;
        /**
         * 主链路登录请求消息
         */
        public final static int DOWN_CONNECT_REQ = 0x9001;
        /**
         * 从链路登录应答消息
         */
        public final static int DOWN_CONNECT_RSP = 0x9002;
        /**
         * 从链路注销请求消息
         */
        public final static int DOWN_DISCONNECT_REQ = 0x9003;
        /**
         * 从链路注销应答消息
         */
        public final static int DOWN_DISCONNECT_RSP = 0x9004;
        /**
         * 从链路连接保持请求消息
         */
        public final static int DOWN_LINKTEST_REQ = 0x9005;
        /**
         * 从链路连接保持应答消息
         */
        public final static int DOWN_LINKTEST_RSP = 0x9006;
        /**
         * 从链路链路断开通知
         */
        public final static int DOWN_DISCONNECT_INFORM = 0x9007;
        /**
         * 上级平台主动关闭从链路通知
         */
        public final static int DOWN_CLOSELINK_INFORM = 0x9008;


        /**
         * 接受定位信息数量通知
         */
        public final static int DOWN_TOTAL_RECV_BACK_MSG = 0x9101;


        /**
         * 主链路动态信息交换消息
         */
        public final static int UP_EXG_MSG = 0x1200;
        /**
         * 从链路动态信息交换
         */
        public final static int DOWN_EXG_MSG = 0x9200;


        /**
         * 主链路平台间信息交互
         */
        public final static int UP_PLATFORM_MSG = 0x1300;
        /**
         * 从链路平台间信息交互
         */
        public final static int DOWN_PLATFORM_MSG = 0x9300;


        /**
         * 主链路报警信息交互
         */
        public final static int UP_WARN_MSG = 0x1400;
        /**
         * 从链路报警信息交互
         */
        public final static int DOWN_WARN_MSG = 0x9400;


        /**
         * 主链路车辆监管消息
         */
        public final static int UP_CTRL_MSG = 0x1500;
        /**
         * 从链路车辆监管消息
         */
        public final static int DOWN_CTRL_MSG = 0x9500;


        /**
         * 主链路静态信息交换
         */
        public final static int UP_BASE_MSG = 0x1600;
        /**
         * 从链路静态信息交换
         */
        public final static int DOWN_BASE_MSG = 0x9600;

    }

    /**
     * 子业务类型标识
     * <p>
     * a) 对应于业务数据类型下的子业务标识头继续遵循原有归属业务数据类型的标识头，例如业务数据类型 UP_EXG_MSG 下的子业务类型标识头均以 UP_EXG_MSG 开始；
     * b) 子业务类型名称标识的主从链路方向遵循原有归属业务数据类型的主从链路方向。
     */
    @SuppressWarnings({"SpellCheckingInspection", "unused"})
    public static class SubDataType {

        /**
         * 上传车辆注册信息
         */
        public final static int UP_EXG_MSG_REGISTER = 0x1201;
        /**
         * 实时上传车辆定位信息
         */
        public final static int UP_EXG_MSG_REAL_LOCATION = 0x1202;
        /**
         * 车辆定位信息自动补报
         */
        public final static int UP_EXG_MSG_HISTORY_LOCATION = 0x1203;
        /**
         * 启动车辆定位信息交换应答
         */
        public final static int UP_EXG_MSG_RETURE_STARTUP_ACK = 0x1205;
        /**
         * 结束车辆定位信息交换应答
         */
        public final static int UP_EXG_MSG_RETURE_END_ACK = 0x1206;
        /**
         * 申请交换指定车辆定位信息请求
         */
        public final static int UP_EXG_MSG_APPLE_FOR_MONITOR_STAR_TUP = 0x1207;
        /**
         * 取消交换制定车辆定位信息请求
         */
        public final static int UP_EXG_MSG_APPLE_FOR_MONITOR_END = 0x1208;
        /**
         * 补发车辆定位信息请求
         */
        public final static int UP_EXG_MSG_APPLE_HISGNSSDATA_REQ = 0x1209;
        /**
         * 上报车辆驾驶员身份识别信息应答
         */
        public final static int UP_EXG_MSG_REPORT_DRIVER_INFO_ACK = 0x120A;
        /**
         * 上报车辆电子运单应答
         */
        public final static int UP_EXG_MSG_TAKE_EWAYBILL_ACK = 0x120B;


        /**
         * 车辆定位信息交换
         */
        public final static int DOWN_EXG_MSG_CAR_LOCATION = 0x9202;
        /**
         * 车辆定位信息交换补发
         */
        public final static int DOWN_EXG_MSG_HISTORY_ARCOSSAREL = 0x9203;
        /**
         * 车辆静态信息交换
         */
        public final static int DOWN_EXG_MSG_CAR_INFO = 0x9204;
        /**
         * 启动车辆定位信息交换请求
         */
        public final static int DOWN_EXG_MSG_RETURN_STARTUP = 0x9205;
        /**
         * 关闭车辆定位信息交换请求
         */
        public final static int DOWN_EXG_MSG_RETURN_END = 0x9206;
        /**
         * 申请交换制定车辆定位信息
         */
        public final static int DOWN_EXG_MSG_APPLY_FOR_MONITOR_STARTUP_ACK = 0x9207;
        /**
         * 取消交换制定车辆定位信息
         */
        public final static int DOWN_EXG_MSG_APPLY_FOR_MONITOR_END_ACK = 0x9208;
        /**
         * 补发车辆定位信息应答
         */
        public final static int DOWN_EXG_MSG_APPLY_HISGNSSDATA_ACK = 0x9209;
        /**
         * 上报车辆驾驶员身份识别信息请求
         */
        public final static int DOWN_EXG_MSG_REPORT_DRIVER_INFO = 0x920A;
        /**
         * 上报车辆电子运单请求
         */
        public final static int DOWN_EXG_MSG_TAKE_EWAYBILL_REQ = 0x920B;


        /**
         * 平台查岗应答
         */
        public final static int UP_PLATFORM_MSG_POST_QUERY_ACK = 0x1301;
        /**
         * 下发平台间报文应答
         */
        public final static int UP_PLATFORM_MSG_INFO_ACK = 0x1302;


        /**
         * 平台查岗应答
         */
        public final static int DOWN_PLATFORM_MSG_POST_QUERY_REQ = 0x9301;
        /**
         * 下发平台间报文应答
         */
        public final static int DOWN_PLATFORM_MSG_INFO_REQ = 0x9302;


        /**
         * 报警督办应答
         */
        public final static int UP_WARN_MSG_URGE_TODO_ACK = 0x1401;
        /**
         * 上报报警信息
         */
        public final static int UP_WARN_MSG_ADPT_INFO = 0x1402;


        /**
         * 报警督办请求
         */
        public final static int DOWN_WARN_MSG_URGE_TODO_REQ = 0x9401;
        /**
         * 报警预警
         */
        public final static int DOWN_WARN_MSG_INFORM_TIPS = 0x9402;
        /**
         * 实时交换报警信息
         */
        public final static int DOWN_WARN_MSG_EXG_INFORM = 0x9403;


        /**
         * 车辆单向监听应答
         */
        public final static int UP_CTRL_MSG_MONITOR_VEHICLE_ACK = 0x1501;
        /**
         * 车辆牌照应答
         */
        public final static int UP_CTRL_MSG_TAKE_PHOTO_ACK = 0x1502;
        /**
         * 下发车辆报文应答
         */
        public final static int UP_CTRL_MSG_TEXT_INFO_ACK = 0x1503;
        /**
         * 上报车辆形式记录应答
         */
        public final static int UP_CTRL_MSG_TAKE_TRAVEL_ACK = 0x1504;
        /**
         * 车辆应急接入监管平台应答
         */
        public final static int UP_CTRL_MSG_EMERGENCY_MONITORING_ACK = 0x1505;


        /**
         * 车辆单向监听请求
         */
        public final static int DOWN_CTRL_MSG_MONITOR_VEHICLE_REQ = 0x9501;
        /**
         * 车辆牌照请求
         */
        public final static int DOWN_CTRL_MSG_TAKE_PHOTO_REQ = 0x9502;
        /**
         * 下发车辆报文请求
         */
        public final static int DOWN_CTRL_MSG_TEXT_INFO_REQ = 0x9503;
        /**
         * 上报车辆形式记录请求
         */
        public final static int DOWN_CTRL_MSG_TAKE_TRAVEL_REQ = 0x9504;
        /**
         * 车辆应急接入监管平台请求
         */
        public final static int UP_CTRL_MSG_EMERGENCY_MONITORING_REQ = 0x9505;


        /**
         * 补报车辆静态信息应答
         */
        public final static int UP_BASE_MSG_VEHICLE_ADDED_ACK = 0x1601;
        /**
         * 补报车辆静态信息请求
         */
        public final static int DOWN_BASE_MSG_VEHICLE_ADDED = 0x9601;

    }

}
