package net.easipay.common.util.sm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class SM4 {
    public static final int SM4_ENCRYPT = 1;
    public static final int SM4_DECRYPT = 0;

    private long GET_ULONG_BE(byte[] b, int i) {
        return (b[i] & 0xFF) << 24 | ((b[i + 1] & 0xFF) << 16) | ((b[i + 2] & 0xFF) << 8) | (b[i + 3] & 0xFF) & 0xFFFFFFFFL;
    }


    private void PUT_ULONG_BE(long n, byte[] b, int i) {
        b[i] = (byte) (int) (0xFFL & n >> 24);
        b[i + 1] = (byte) (int) (0xFFL & n >> 16);
        b[i + 2] = (byte) (int) (0xFFL & n >> 8);
        b[i + 3] = (byte) (int) (0xFFL & n);
    }


    private long SHL(long x, int n) {
        return (x & 0xFFFFFFFFFFFFFFFFL) << n;
    }


    private long ROTL(long x, int n) {
        return SHL(x, n) | x >> 32 - n;
    }


    private void SWAP(long[] sk, int i) {
        long t = sk[i];
        sk[i] = sk[31 - i];
        sk[31 - i] = t;
    }

    public static final byte[] SboxTable = {
            -42, -112, -23, -2, -52, -31, 61, -73, 22, -74, 20, -62, 40, -5, 44, 5, 43, 103, -102, 118, 42, -66, 4, -61, -86, 68, 19, 38, 73, -122, 6, -103, -100, 66, 80, -12, -111, -17, -104, 122, 51, 84, 11, 67, -19, -49, -84, 98, -28, -77, 28, -87, -55, 8, -24, -107, Byte.MIN_VALUE, -33, -108, -6, 117, -113, 63, -90, 71, 7, -89, -4, -13, 115, 23, -70, -125, 89, 60, 25, -26, -123, 79, -88, 104, 107, -127, -78, 113, 100, -38, -117, -8, -21, 15, 75, 112, 86, -99, 53, 30, 36, 14, 94, 99, 88, -47, -94, 37, 34, 124, 59, 1, 33, 120, -121, -44, 0, 70, 87, -97, -45, 39, 82, 76, 54, 2, -25, -96, -60, -56, -98, -22, -65, -118, -46, 64, -57, 56, -75, -93, -9, -14, -50, -7, 97, 21, -95, -32, -82, 93, -92, -101, 52, 26, 85, -83, -109, 50, 48, -11, -116, -79, -29, 29, -10, -30, 46, -126, 102, -54, 96, -64, 41, 35, -85, 13, 83, 78, 111, -43, -37, 55, 69, -34, -3, -114, 47, 3, -1, 106, 114, 109, 108, 91, 81, -115, 27, -81, -110, -69, -35, -68, Byte.MAX_VALUE, 17, -39, 92, 65, 31, 16, 90, -40, 10, -63, 49, -120, -91, -51, 123, -67, 45, 116, -48, 18, -72, -27, -76, -80, -119, 105, -105, 74, 12, -106, 119, 126, 101, -71, -15, 9, -59, 110, -58, -124, 24, -16, 125, -20, 58, -36, 77, 32, 121, -18, 95, 62, -41, -53, 57, 72};


    public static final int[] FK = {-1548633402, 1453994832, 1736282519, -1301273892};
    public static final int[] CK = {
            462357, 472066609, 943670861, 1415275113, 1886879365, -1936483679, -1464879427, -993275175, -521670923, -66909679, 404694573, 876298825, 1347903077, 1819507329, -2003855715, -1532251463, -1060647211, -589042959, -117504499, 337322537, 808926789, 1280531041, 1752135293, -2071227751, -1599623499, -1128019247, -656414995, -184876535, 269950501, 741554753, 1213159005, 1684763257};


    private byte sm4Sbox(byte inch) {
        int i = inch & 0xFF;
        return SboxTable[i];
    }


    private long sm4Lt(long ka) {
        long bb = 0L;
        long c = 0L;
        byte[] a = new byte[4];
        byte[] b = new byte[4];
        PUT_ULONG_BE(ka, a, 0);
        b[0] = sm4Sbox(a[0]);
        b[1] = sm4Sbox(a[1]);
        b[2] = sm4Sbox(a[2]);
        b[3] = sm4Sbox(a[3]);
        bb = GET_ULONG_BE(b, 0);
        return bb ^ ROTL(bb, 2) ^ ROTL(bb, 10) ^ ROTL(bb, 18) ^ ROTL(bb, 24);
    }


    private long sm4F(long x0, long x1, long x2, long x3, long rk) {
        return x0 ^ sm4Lt(x1 ^ x2 ^ x3 ^ rk);
    }


    private long sm4CalciRK(long ka) {
        long bb = 0L;
        long rk = 0L;
        byte[] a = new byte[4];
        byte[] b = new byte[4];
        PUT_ULONG_BE(ka, a, 0);
        b[0] = sm4Sbox(a[0]);
        b[1] = sm4Sbox(a[1]);
        b[2] = sm4Sbox(a[2]);
        b[3] = sm4Sbox(a[3]);
        bb = GET_ULONG_BE(b, 0);
        return bb ^ ROTL(bb, 13) ^ ROTL(bb, 23);
    }


    private void sm4_setkey(long[] SK, byte[] key) {
        long[] MK = new long[4];
        long[] k = new long[36];
        int i = 0;
        MK[0] = GET_ULONG_BE(key, 0);
        MK[1] = GET_ULONG_BE(key, 4);
        MK[2] = GET_ULONG_BE(key, 8);
        MK[3] = GET_ULONG_BE(key, 12);
        k[0] = MK[0] ^ FK[0];
        k[1] = MK[1] ^ FK[1];
        k[2] = MK[2] ^ FK[2];
        k[3] = MK[3] ^ FK[3];
        for (; i < 32; i++) {

            k[i + 4] = k[i] ^ sm4CalciRK(k[i + 1] ^ k[i + 2] ^ k[i + 3] ^ CK[i]);
            SK[i] = k[i + 4];
        }
    }


    private void sm4_one_round(long[] sk, byte[] input, byte[] output) {
        int i = 0;
        long[] ulbuf = new long[36];
        ulbuf[0] = GET_ULONG_BE(input, 0);
        ulbuf[1] = GET_ULONG_BE(input, 4);
        ulbuf[2] = GET_ULONG_BE(input, 8);
        ulbuf[3] = GET_ULONG_BE(input, 12);
        while (i < 32) {

            ulbuf[i + 4] = sm4F(ulbuf[i], ulbuf[i + 1], ulbuf[i + 2], ulbuf[i + 3], sk[i]);
            i++;
        }
        PUT_ULONG_BE(ulbuf[35], output, 0);
        PUT_ULONG_BE(ulbuf[34], output, 4);
        PUT_ULONG_BE(ulbuf[33], output, 8);
        PUT_ULONG_BE(ulbuf[32], output, 12);
    }


    private byte[] padding(byte[] input, int mode) {
        if (input == null) {
            return null;
        }

        byte[] ret = (byte[]) null;
        if (mode == 1) {

            int p = 16 - input.length % 16;
            ret = new byte[input.length + p];
            System.arraycopy(input, 0, ret, 0, input.length);
            for (int i = 0; i < p; i++) {
                ret[input.length + i] = (byte) p;
            }
        } else {

            int p = input[input.length - 1];
            ret = new byte[input.length - p];
            System.arraycopy(input, 0, ret, 0, input.length - p);
        }
        return ret;
    }


    public void sm4_setkey_enc(SM4_Context ctx, byte[] key) throws Exception {
        if (ctx == null) {
            throw new Exception("ctx is null!");
        }

        if (key == null || key.length != 16) {
            throw new Exception("key error!");
        }

        ctx.mode = 1;
        sm4_setkey(ctx.sk, key);
    }


    public void sm4_setkey_dec(SM4_Context ctx, byte[] key) throws Exception {
        if (ctx == null) {
            throw new Exception("ctx is null!");
        }

        if (key == null || key.length != 16) {
            throw new Exception("key error!");
        }

        int i = 0;
        ctx.mode = 0;
        sm4_setkey(ctx.sk, key);
        for (i = 0; i < 16; i++) {
            SWAP(ctx.sk, i);
        }
    }


    public byte[] sm4_crypt_ecb(SM4_Context ctx, byte[] input) throws Exception {
        if (input == null) {
            throw new Exception("input is null!");
        }

        if (ctx.isPadding && ctx.mode == 1) {
            input = padding(input, 1);
        }

        int length = input.length;
        ByteArrayInputStream bins = new ByteArrayInputStream(input);
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        for (; length > 0; length -= 16) {

            byte[] in = new byte[16];
            byte[] out = new byte[16];
            bins.read(in);
            sm4_one_round(ctx.sk, in, out);
            bous.write(out);
        }

        byte[] output = bous.toByteArray();
        if (ctx.isPadding && ctx.mode == 0) {
            output = padding(output, 0);
        }
        bins.close();
        bous.close();
        return output;
    }


    public byte[] sm4_crypt_cbc(SM4_Context ctx, byte[] iv, byte[] input) throws Exception {
        if (iv == null || iv.length != 16) {
            throw new Exception("iv error!");
        }

        if (input == null) {
            throw new Exception("input is null!");
        }

        if (ctx.isPadding && ctx.mode == 1) {
            input = padding(input, 1);
        }

        int i = 0;
        int length = input.length;
        ByteArrayInputStream bins = new ByteArrayInputStream(input);
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        if (ctx.mode == 1) {

            for (; length > 0; length -= 16) {
                byte[] in = new byte[16];
                byte[] out = new byte[16];
                byte[] out1 = new byte[16];

                bins.read(in);
                for (i = 0; i < 16; i++) {
                    out[i] = (byte) (in[i] ^ iv[i]);
                }
                sm4_one_round(ctx.sk, out, out1);
                System.arraycopy(out1, 0, iv, 0, 16);
                bous.write(out1);
            }

        } else {

            byte[] temp = new byte[16];
            for (; length > 0; length -= 16) {

                byte[] in = new byte[16];
                byte[] out = new byte[16];
                byte[] out1 = new byte[16];

                bins.read(in);
                System.arraycopy(in, 0, temp, 0, 16);
                sm4_one_round(ctx.sk, in, out);
                for (i = 0; i < 16; i++) {
                    out1[i] = (byte) (out[i] ^ iv[i]);
                }
                System.arraycopy(temp, 0, iv, 0, 16);
                bous.write(out1);
            }
        }

        byte[] output = bous.toByteArray();
        if (ctx.isPadding && ctx.mode == 0) {
            output = padding(output, 0);
        }
        bins.close();
        bous.close();
        return output;
    }
}
