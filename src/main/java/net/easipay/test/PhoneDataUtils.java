package net.easipay.test;

/**
 * @Author: jiande.hu
 * @Date: 2019/7/25 15:55
 * @Description:
 */
public class PhoneDataUtils {

    private static String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");

    /***
     *
     * Project Name: recruit-helper-util
     * <p>随机生成手机号码
     *
     * @author jiande.hu
     * @date 2019年5月23日  下午2:14:17
     * @version v1.0
     * @since
     * @return
     */
    public static String getTelephone() {
        int index = getNum(0, telFirst.length - 1);
        String first = telFirst[index];
        String second = String.valueOf(getNum(1, 888) + 10000).substring(1);
        String thrid = String.valueOf(getNum(1, 9100) + 10000).substring(1);
        return first + second + thrid;
    }

    public static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 500; i++) {
            String telephone = getTelephone();
            System.out.println(telephone);
        }
    }
}
