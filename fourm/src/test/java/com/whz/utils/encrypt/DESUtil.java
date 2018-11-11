package com.whz.utils.encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DESUtil {

    private static final String DES = "DES";

    private static final String DES_CBC_PKCS5Padding = "DES/CBC/PKCS5Padding";

    /** 默认的IV初始向量 */
    private static final byte[] DEFAULT_IV = HexUtil.toBytes("0000000000000000");

    /**
     * 使用DES/CBC/PKCS5Padding加密，并加密后的byte数组
     *
     * @param content   加密的数据
     * @param keyBytes  加密使用的秘钥，必须为8字节
     * @return
     * @throws Exception
     */
    public static byte[] desCbcPKCS5PaddingEncrypt(byte[] content, byte[] keyBytes) throws Exception {

        DESKeySpec keySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey key = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5Padding);
        // 使用key作为初始向量
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(keySpec.getKey()));
        byte[] result = cipher.doFinal(content);
        return result;
    }

    /**
     * 使用DES/CBC/PKCS5Padding解密，并返回解密后的byte数组
     *
     * @param content   解密的数据
     * @param keyBytes  解密使用的秘钥，必须为8字节
     * @return
     * @throws Exception
     */
    public static byte[] desCbcPKCS5PaddingDecrypt(byte[] content, byte[] keyBytes) throws Exception {

        DESKeySpec keySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey key = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5Padding);
        // 使用key作为初始向量
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(keyBytes));
        byte[] result = cipher.doFinal(content);
        return result;
    }

    /**
     * 使用DES/CBC/PKCS5Padding加密，并加密后的byte数组
     *
     * @param content   加密的数据
     * @param keyBytes  加密使用的秘钥，必须为8字节
     * @param iv        加密使用的秘钥，必须为8字节，比如{@link #DEFAULT_IV}
     * @return
     * @throws Exception
     */
    public static byte[] desCbcPKCS5PaddingEncrypt(byte[] content, byte[] keyBytes, byte[] iv) throws Exception {

        DESKeySpec keySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey key = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5Padding);
        // 使用key作为初始向量
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] result = cipher.doFinal(content);
        return result;
    }

    /**
     * 使用DES/CBC/PKCS5Padding解密，并返回解密后的byte数组
     *
     * @param content   解密的数据
     * @param keyBytes  解密使用的秘钥，必须为8字节
     * @param iv        初始向量，必须为8字节，比如{@link #DEFAULT_IV}
     * @return
     * @throws Exception
     */
    public static byte[] desCbcPKCS5PaddingDecrypt(byte[] content, byte[] keyBytes, byte[] iv) throws Exception {

        DESKeySpec keySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey key = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5Padding);
        // 使用key作为初始向量
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] result = cipher.doFinal(content);
        return result;
    }

    public static void main(String[] args) throws Exception {
        String content = "aaaaaaaabbbbbbbbaaaaaaaa";
        String key = "01234567";

        System.out.println("加密前：" + HexUtil.toHex(content.getBytes()));
        byte[] encrypted = desCbcPKCS5PaddingEncrypt(content.getBytes(), key.getBytes(), DEFAULT_IV);
        System.out.println("加密后：" + HexUtil.toHex(encrypted));
        byte[] decrypted = desCbcPKCS5PaddingDecrypt(encrypted, key.getBytes(), DEFAULT_IV);
        System.out.println("解密后：" + HexUtil.toHex(decrypted));
        System.out.println("解密后的字符串：" + new String(decrypted, "utf-8"));
    }

    /**
     * 16进制与字符串转换工具
     *
     * @Author: wanghz
     * @Date: 2018/10/13 下午7:43
     */
    static class HexUtil {

        private static final String HEX_CHARS = "0123456789ABCDEF";

        /**
         * 字符串转换成为16进制(无需Unicode编码)，对应{@link #toString(String)}
         *
         * @param str
         * @return
         */
        public static String toHex(String str) {
            char[] chars = HEX_CHARS.toCharArray();
            StringBuilder sb = new StringBuilder();
            byte[] bs = str.getBytes();
            int bit;
            for (int i = 0; i < bs.length; i++) {
                bit = (bs[i] & 0x0f0) >> 4;
                sb.append(chars[bit]);
                bit = bs[i] & 0x0f;
                sb.append(chars[bit]);
            }
            return sb.toString().trim();
        }

        /**
         * 16进制直接转换成为字符串(无需Unicode解码)，对应{@link #toHex(String)}
         *
         * @param hex
         * @return
         */
        public static String toString(String hex) {
            char[] hexs = hex.toCharArray();
            byte[] bytes = new byte[hex.length() / 2];
            int n;
            for (int i = 0; i < bytes.length; i++) {
                n = HEX_CHARS.indexOf(hexs[2 * i]) * 16;
                n += HEX_CHARS.indexOf(hexs[2 * i + 1]);
                bytes[i] = (byte) (n & 0xff);
            }
            return new String(bytes);
        }

        /**
         * 将byte数组转换为16进制表示的字符串，对应{@link #toBytes(String)}
         *
         * @param array
         * @return 16进制表示的字符串
         */
        public static String toHex(byte[] array) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                String ch = Integer.toHexString(array[i] & 0xFF).toUpperCase();
                if (ch.length() == 2) {
                    sb.append(ch);
                } else {
                    sb.append("0").append(ch);
                }
            }
            return sb.toString();
        }

        /**
         * 将16进制的字符串转换为byte数组，对应{@link #toHex(byte[])}
         *
         * @param hex
         * @return
         */
        public static byte[] toBytes(String hex) {
            if (hex == null || hex.equals("")) {
                return null;
            }
            hex = hex.toUpperCase();
            int length = hex.length() / 2;
            char[] hexChars = hex.toCharArray();
            byte[] d = new byte[length];
            for (int i = 0; i < length; i++) {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }
            return d;
        }

        /**
         * 将字符转为字节
         *
         * @param c
         * @return
         */
        private static byte charToByte(char c) {
            return (byte) HEX_CHARS.indexOf(c);
        }

        public static void main(String[] args) {
            System.out.println("将字符串'z3w='转为16进制字符串：" + toHex("z3w="));
            System.out.println("将16进制字符串'7A33773D'转为字符串：" + toString("7A33773D"));
        }

    }
}