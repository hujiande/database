package net.easipay.datades;

import java.util.Properties;

/**
 * @Author: jiande.hu
 * @Date: 2019/7/23 19:15
 * @Description:
 */
public class PropertiesUtil {

    private static Properties pps = null;

    public static String getPropertiesVaue(String keyValue) {
        try {
            if (pps == null) {
                pps = Handle.getProperties("pepp-rz-resend.properties");
                return pps.getProperty(keyValue);
            }
            return pps.getProperty(keyValue);
        } catch (Exception e) {
            return null;
        }
    }
}
