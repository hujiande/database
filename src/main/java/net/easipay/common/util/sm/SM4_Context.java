package net.easipay.common.util.sm;

/**
 * @Author: jiande.hu
 * @Date: 2019/7/23 19:29
 * @Description:
 */
public class SM4_Context {
    public int mode;
    public long[] sk;
    public boolean isPadding;

    public SM4_Context() {
        this.mode = 1;
        this.isPadding = true;
        this.sk = new long[32];
    }
}
