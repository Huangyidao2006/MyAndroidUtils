package com.hj.android.utils;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;

/**
 * Created by hj at 2022/6/6 17:51
 */
public class NetworkUtil {
    public static String getIP(String ifaceName, boolean isBroadcast) {
        try {
            NetworkInterface iface = NetworkInterface.getByName(ifaceName);
            if (iface != null) {
                List<InterfaceAddress> addressList = iface.getInterfaceAddresses();
                for (InterfaceAddress addr : addressList) {
                    if (addr.getAddress() instanceof Inet4Address) {
                        if (isBroadcast) {
                            return addr.getBroadcast().getHostAddress();
                        }
                    } else {
                        return addr.getAddress().getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getWirelessIP(boolean isBroadcast) {
        String ip = getIP("wlan0", isBroadcast);
        if (ip == null || "".equals(ip)) {
            ip = getIP("wlo1", isBroadcast);
        }

        return ip;
    }
}
