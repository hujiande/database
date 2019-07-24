package net.easipay.common.util.sm;

public class SM3 {
    public static final byte[] iv = {
            115, Byte.MIN_VALUE, 22, 111, 73, 20, -78, -71, 23, 36, 66, -41, -38, -118, 6, 0, -87, 111, 48, -68, 22, 49, 56, -86, -29, -115, -18, 77, -80, -5, 14, 78};


    public static int[] Tj = new int[64];

    static {
        for (int i = 0; i < 16; i++) {
            Tj[i] = 2043430169;
        }

        for (int i = 16; i < 64; i++) {
            Tj[i] = 2055708042;
        }
    }


    public static byte[] CF(byte[] V, byte[] B) {
        int[] v = convert(V);
        int[] b = convert(B);
        return convert(CF(v, b));
    }


    private static int[] convert(byte[] arr) {
        int[] out = new int[arr.length / 4];
        byte[] tmp = new byte[4];
        for (int i = 0; i < arr.length; i += 4) {

            System.arraycopy(arr, i, tmp, 0, 4);
            out[i / 4] = bigEndianByteToInt(tmp);
        }
        return out;
    }


    private static byte[] convert(int[] arr) {
        byte[] out = new byte[arr.length * 4];
        byte[] tmp = null;
        for (int i = 0; i < arr.length; i++) {

            tmp = bigEndianIntToByte(arr[i]);
            System.arraycopy(tmp, 0, out, i * 4, 4);
        }
        return out;
    }


    public static int[] CF(int[] V, int[] B) {
        int a = V[0];
        int b = V[1];
        int c = V[2];
        int d = V[3];
        int e = V[4];
        int f = V[5];
        int g = V[6];
        int h = V[7];

        int[][] arr = expand(B);
        int[] w = arr[0];
        int[] w1 = arr[1];

        for (int j = 0; j < 64; j++) {

            int ss1 = bitCycleLeft(a, 12) + e + bitCycleLeft(Tj[j], j);
            ss1 = bitCycleLeft(ss1, 7);
            int ss2 = ss1 ^ bitCycleLeft(a, 12);
            int tt1 = FFj(a, b, c, j) + d + ss2 + w1[j];
            int tt2 = GGj(e, f, g, j) + h + ss1 + w[j];
            d = c;
            c = bitCycleLeft(b, 9);
            b = a;
            a = tt1;
            h = g;
            g = bitCycleLeft(f, 19);
            f = e;
            e = P0(tt2);
        }


        int[] out = new int[8];
        out[0] = a ^ V[0];
        out[1] = b ^ V[1];
        out[2] = c ^ V[2];
        out[3] = d ^ V[3];
        out[4] = e ^ V[4];
        out[5] = f ^ V[5];
        out[6] = g ^ V[6];
        out[7] = h ^ V[7];

        return out;
    }


    private static int[][] expand(int[] B) {
        int[] W = new int[68];
        int[] W1 = new int[64];
        for (int i = 0; i < B.length; i++) {
            W[i] = B[i];
        }

        for (int i = 16; i < 68; i++) {
            W[i] = P1(W[i - 16] ^ W[i - 9] ^ bitCycleLeft(W[i - 3], 15)) ^ bitCycleLeft(W[i - 13], 7) ^ W[i - 6];
        }


        for (int i = 0; i < 64; i++) {
            W1[i] = W[i] ^ W[i + 4];
        }

        return new int[][]{W, W1};
    }


    private static byte[] bigEndianIntToByte(int num) {
        return back(SMUtil.intToBytes(num));
    }


    private static int bigEndianByteToInt(byte[] bytes) {
        return SMUtil.byteToInt(back(bytes));
    }


    private static int FFj(int X, int Y, int Z, int j) {
        if (j >= 0 && j <= 15) {
            return FF1j(X, Y, Z);
        }


        return FF2j(X, Y, Z);
    }


    private static int GGj(int X, int Y, int Z, int j) {
        if (j >= 0 && j <= 15) {
            return GG1j(X, Y, Z);
        }


        return GG2j(X, Y, Z);
    }


    private static int FF1j(int X, int Y, int Z) {
        return X ^ Y ^ Z;
    }


    private static int FF2j(int X, int Y, int Z) {
        return X & Y | X & Z | Y & Z;
    }


    private static int GG1j(int X, int Y, int Z) {
        return X ^ Y ^ Z;
    }


    private static int GG2j(int X, int Y, int Z) {
        return X & Y | (X ^ 0xFFFFFFFF) & Z;
    }


    private static int P0(int X) {
        int y = rotateLeft(X, 9);
        y = bitCycleLeft(X, 9);
        int z = rotateLeft(X, 17);
        z = bitCycleLeft(X, 17);
        return X ^ y ^ z;
    }


    private static int P1(int X) {
        return X ^ bitCycleLeft(X, 15) ^ bitCycleLeft(X, 23);
    }


    public static byte[] padding(byte[] in, int bLen) {
        int k = 448 - (8 * in.length + 1) % 512;
        if (k < 0) {
            k = 960 - (8 * in.length + 1) % 512;
        }
        k++;
        byte[] padd = new byte[k / 8];
        padd[0] = Byte.MIN_VALUE;
        long n = (in.length * 8 + bLen * 512);
        byte[] out = new byte[in.length + k / 8 + 8];
        int pos = 0;
        System.arraycopy(in, 0, out, 0, in.length);
        pos += in.length;
        System.arraycopy(padd, 0, out, pos, padd.length);
        pos += padd.length;
        byte[] tmp = back(SMUtil.longToBytes(n));
        System.arraycopy(tmp, 0, out, pos, tmp.length);
        return out;
    }


    private static byte[] back(byte[] in) {
        byte[] out = new byte[in.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = in[out.length - i - 1];
        }

        return out;
    }


    public static int rotateLeft(int x, int n) {
        return x << n | x >> 32 - n;
    }


    private static int bitCycleLeft(int n, int bitLen) {
        bitLen %= 32;
        byte[] tmp = bigEndianIntToByte(n);
        int byteLen = bitLen / 8;
        int len = bitLen % 8;
        if (byteLen > 0) {
            tmp = byteCycleLeft(tmp, byteLen);
        }

        if (len > 0) {
            tmp = bitSmall8CycleLeft(tmp, len);
        }

        return bigEndianByteToInt(tmp);
    }


    private static byte[] bitSmall8CycleLeft(byte[] in, int len) {
        byte[] tmp = new byte[in.length];

        for (int i = 0; i < tmp.length; i++) {

            int t1 = (byte) ((in[i] & 0xFF) << len);
            int t2 = (byte) ((in[(i + 1) % tmp.length] & 0xFF) >> 8 - len);
            int t3 = (byte) (t1 | t2);
            tmp[i] = (byte) t3;
        }

        return tmp;
    }


    private static byte[] byteCycleLeft(byte[] in, int byteLen) {
        byte[] tmp = new byte[in.length];
        System.arraycopy(in, byteLen, tmp, 0, in.length - byteLen);
        System.arraycopy(in, 0, tmp, in.length - byteLen, byteLen);
        return tmp;
    }
}
