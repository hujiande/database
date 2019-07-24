package net.easipay.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.easipay.common.util.sm.SM3Digest;
import net.easipay.common.util.sm.SM4;
import net.easipay.common.util.sm.SM4_Context;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Hex;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class DesBase64 {

    private static Logger logger = Logger.getLogger(DesBase64.class);

    private static final String Algorithm = "DESede";

    public static String encrypt(String encKey, String src) {
        try {
            byte[] keybyte = md5Hex(encKey);

            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");

            Cipher c1 = Cipher.getInstance("DESede");
            c1.init(1, deskey);

            return new String(zipBytesToBytes(c1.doFinal(src.getBytes("UTF-8"))), "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }


    public static String decrypt(String encKey, String src) {
        try {
            byte[] keybyte = md5Hex(encKey);

            SecretKey deskey = new SecretKeySpec(keybyte, "DESede");

            Cipher c1 = Cipher.getInstance("DESede");
            c1.init(2, deskey);
            return new String(c1.doFinal(unzipBytesFromBytes(src.getBytes("UTF-8"))), "UTF-8");
        } catch (Exception ex) {
            String errmsg = "Decrypt fail! Maybe KeyName[" + encKey + "]'s keyValue is not matched!\nException:" + ex.getMessage();

            logger.error("src=" + src + "\n " + errmsg, ex);
            throw new RuntimeException(errmsg, ex);
        }
    }

    public static String base64En(String src) {
        try {
            if (null == src) {
                return null;
            }
            return new String(zipBytesToBytes(src.getBytes("UTF-8")), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("base64 ERROR!", e);
        }
    }

    public static String base64De(String src) {
        try {
            if (null == src) {
                return null;
            }
            return new String(unzipBytesFromBytes(src.getBytes("UTF-8")), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("base64 ERROR!", e);
        }
    }

    private static byte[] md5Hex(String outKey) {
        String f = DigestUtils.md5Hex(outKey);
        byte[] bkeys = (new String(f)).getBytes();
        byte[] enk = new byte[24];
        for (int i = 0; i < 24; i++) {
            enk[i] = bkeys[i];
        }
        return enk;
    }

    private static byte[] zipBytesToBytes(byte[] input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream bufos = new BufferedOutputStream(new GZIPOutputStream(bos));
        bufos.write(input);
        bufos.close();
        byte[] retval = bos.toByteArray();
        bos.close();

        return Base64.encodeBase64(retval);
    }

    private static byte[] unzipBytesFromBytes(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(bytes));
        BufferedInputStream bufis = new BufferedInputStream(new GZIPInputStream(bis));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = bufis.read(buf)) > 0) {
            bos.write(buf, 0, len);
        }
        bis.close();
        bufis.close();
        bos.close();
        return bos.toByteArray();
    }


    public static String encrypt_sm4(String encKey, String src) {
        try {
            SM4_Context ctx = new SM4_Context();
            ctx.isPadding = true;
            ctx.mode = 1;


            byte[] keyBytes = encKey.getBytes();
            SM4 sm4 = new SM4();
            sm4.sm4_setkey_enc(ctx, keyBytes);
            byte[] encrypted = sm4.sm4_crypt_ecb(ctx, src.getBytes("GBK"));
            String cipherText = (new BASE64Encoder()).encode(encrypted);
            if (cipherText != null && cipherText.trim().length() > 0) {
                Pattern p = Pattern.compile("\\s*|\t|\r|\n");
                Matcher m = p.matcher(cipherText);
                cipherText = m.replaceAll("");
            }
            return cipherText;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String decrypt_sm4(String encKey, String src) {
        try {
            SM4_Context ctx = new SM4_Context();
            ctx.isPadding = true;
            ctx.mode = 0;

            byte[] keyBytes = encKey.getBytes();

            SM4 sm4 = new SM4();
            sm4.sm4_setkey_dec(ctx, keyBytes);
            byte[] decrypted = sm4.sm4_crypt_ecb(ctx, (new BASE64Decoder()).decodeBuffer(src));
            return new String(decrypted, "GBK");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String digest_sm3(String src) {
        byte[] md = new byte[32];
        byte[] msg1 = src.getBytes();
        SM3Digest sm3 = new SM3Digest();
        sm3.update(msg1, 0, msg1.length);
        sm3.doFinal(md, 0);
        return new String(Hex.encode(md));
    }


    public static void test1() {
        String encKey = "1233451234124312";
        String oriData = "1234567 Chinese======original data,This is a test string!";
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;


        System.out.println("" + oriData.length() + "," + oriData);

        String encoded = encrypt(encKey, oriData);
        System.out.println("After Encryption :" + encoded.length() + "," + encoded);
        String encoded2 = encrypt(encKey, encoded);
        System.out.println("After Encryption(2):" + encoded2.length() + "," + encoded2);

        String decoded2 = decrypt(encKey, encoded2);
        System.out.println("After Decryption :(2):" + decoded2.length() + "," + decoded2);
        String decoded = decrypt(encKey, decoded2);
        System.out.println("After Decryption :" + decoded.length() + "," + decoded);

        String encoded3 = encrypt_sm4(encKey, oriData);
        System.out.println("Original Data" + oriData.length() + "," + oriData);

        System.out.println("SM4 After Encryption:" + encoded3.length() + "," + encoded3);
        String encoded4 = decrypt_sm4(encKey, encoded3);
        System.out.println("SM4 After Decryption:" + encoded4.length() + "," + encoded4);
    }


    public static void test2() {
    }


    public static void main(String[] args) throws Exception {
        String oriData = "1234567 Chinese======Original,This is a test string!";
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        oriData = oriData + oriData;
        System.out.println("base64 Before Encryption:" + oriData.length() + "," + oriData);

        String encoded = base64En(oriData);
        System.out.println("base64 After Encryption:" + encoded.length() + "," + encoded);
        String decoded = base64De(encoded);
        System.out.println("base64 After Decryption:" + decoded.length() + "," + decoded);
        test1();
    }
}

