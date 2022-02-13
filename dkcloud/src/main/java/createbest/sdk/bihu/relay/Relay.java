package createbest.sdk.bihu.relay;

import java.io.FileOutputStream;

/**
 * 继电器控制类
 */
public class Relay {
    /**
     * 开门
     */
    public static void openDoorByRelay() {
        relay1(true);
        relay2(true);
        try {
            Thread.sleep(500);
            relay1(false);
            relay2(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 继电器1
     *
     * @param open
     */
    public static void relay1(boolean open) {
        String status = open ? "1" : "0";
        try {
            FileOutputStream fos = new FileOutputStream("/sys/exgpio/relay1");
            fos.write(status.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 继电器2
     *
     * @param open
     */
    public static void relay2(boolean open) {
        String status = open ? "1" : "0";
        try {
            FileOutputStream fos = new FileOutputStream("/sys/exgpio/relay2");
            fos.write(status.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
