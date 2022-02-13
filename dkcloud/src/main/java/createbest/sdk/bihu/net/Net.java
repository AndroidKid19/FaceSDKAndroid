package createbest.sdk.bihu.net;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Net {
    /**
     * 设置以太网
     *
     * @param context   Context
     * @param staticIp  是否用静态IP
     * @param ip        静态IP
     * @param netmask   掩码
     * @param gateway   网关
     * @param dns       DNS
     * @param proxy     代理
     * @param exclusion 代理排除
     * @param pac
     */
    public static void setEthernetConfig(Context context, boolean staticIp, String ip, String netmask, String gateway, String[] dns, String proxy, String exclusion, String pac) {
        Intent intent = new Intent("com.cbest.eth.config");
        intent.putExtra("mode", staticIp ? "static" : "dhcp");
        intent.putExtra("ip", ip);
        intent.putExtra("netmask", netmask);
        intent.putExtra("gateway", gateway);
        if (dns != null) {
            for (int idx = 0; idx < dns.length; idx++) {
                intent.putExtra(String.format("dns%d", idx + 1), dns[idx]);
            }
        }
        if (!TextUtils.isEmpty(proxy)) {
            intent.putExtra("proxy", proxy);
            if (!TextUtils.isEmpty(exclusion)) {
                intent.putExtra("exclusion", exclusion);
            }
        } else if (!TextUtils.isEmpty(pac)) {
            intent.putExtra("pac", pac);
        }
        context.sendBroadcast(intent);
    }

    /**
     * 获取以太网MAC
     *
     * @return
     */
    public static String getEthernetMac() {
        try {
            Method method = Build.class.getDeclaredMethod("getString", new Class[]{String.class});
            method.setAccessible(true);
            String mac = (String) method.invoke(null, "net.eth0.mac");
            return mac;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取以太网IP
     *
     * @return
     */
    public static String getEthernetIp() {
        try {
            Method method = Build.class.getDeclaredMethod("getString", new Class[]{String.class});
            method.setAccessible(true);
            String ip = (String) method.invoke(null, "net.eth0.ip");
            return ip;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取以太网Dns1
     *
     * @return
     */
    public static String getEthernetDns1() {
        try {
            Method method = Build.class.getDeclaredMethod("getString", new Class[]{String.class});
            method.setAccessible(true);
            String dns1 = (String) method.invoke(null, "dhcp.eth0.dns1");
            return dns1;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取以太网Dns2
     *
     * @return
     */
    public static String getEthernetDns2() {
        try {
            Method method = Build.class.getDeclaredMethod("getString", new Class[]{String.class});
            method.setAccessible(true);
            String dns2 = (String) method.invoke(null, "dhcp.eth0.dns2");
            return dns2;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取以太网网关
     *
     * @return
     */
    public static String getEthernetGateway() {
        try {
            Method method = Build.class.getDeclaredMethod("getString", new Class[]{String.class});
            method.setAccessible(true);
            String gateway = (String) method.invoke(null, "dhcp.eth0.gateway");
            return gateway;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
