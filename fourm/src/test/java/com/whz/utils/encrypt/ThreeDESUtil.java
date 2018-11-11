package com.whz.utils.encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;

/**
 * 3DES-CBC/Nopadding 加解密工具类
 *
 * @Author: wanghz
 * @Date: 2018/10/11 上午11:18
 */
public class ThreeDESUtil {

    private static final String UTF_8 = "utf-8";

    private static final String THREE_DES = "desede";

    private static final String DES_CBC_NO_PADDING = "desede/CBC/NoPadding";

    private static final String DES_CBC_PKCS5_PADDING = "desede/CBC/PKCS5Padding";

    private static final byte[] DEFAULT_IV = HexUtil.toBytes("0000000000000000");

    /**
     * 使用3DES-CBC/Nopadding加密
     *
     * @param data  要加密的数据，使用NO_PADDING方式，必须为8字节的整数倍
     * @param key   加密秘钥，DESedeKeySpec会帮你生成24位秘钥，key可以是任意长度
     * @param keyIV 初始向量，必须为8字节，例如：{@link #DEFAULT_IV}
     * @return 返回加密后的字节数组
     * @throws Exception
     */
    public static byte[] des3CBCNoPaddingEncrypt(byte[] data, byte[] key, byte[] keyIV) throws Exception {
        DESedeKeySpec spec = new DESedeKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(THREE_DES);
        Key desKey = keyFactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance(DES_CBC_NO_PADDING);
        IvParameterSpec ips = new IvParameterSpec(keyIV);
        cipher.init(Cipher.ENCRYPT_MODE, desKey, ips);

        return cipher.doFinal(data);
    }

    /**
     * 使用3DES-CBC/Nopadding解密
     *
     * @param data  要解密的数据，使用NO_PADDING方式，必须为8字节的整数倍
     * @param key   加密秘钥，DESedeKeySpec会帮你生成24位秘钥，key可以是任意长度
     * @param keyIV 初始向量，必须为8字节，例如：{@link #DEFAULT_IV}
     * @return 返回解密后的字节数组
     * @throws Exception
     */
    public static byte[] des3CBCNoPaddingDecrypt(byte[] data, byte[] key, byte[] keyIV) throws Exception {
        DESedeKeySpec spec = new DESedeKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(THREE_DES);
        Key desKey = keyFactory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance(DES_CBC_NO_PADDING);
        IvParameterSpec ips = new IvParameterSpec(keyIV);
        cipher.init(Cipher.DECRYPT_MODE, desKey, ips);

        return cipher.doFinal(data);
    }

    /**
     * des3CBCPKCS5PaddingEncrypt
     *
     * @param data  需要加密的数据，使用PKCS5Padding方式，不足8位会自动补齐
     * @param key   秘钥，DESedeKeySpec会帮你生成24位秘钥，key可以是任意长度
     * @param keyIV 初始向量
     * @return
     * @throws Exception
     */
    public static byte[] des3CBCPKCS5PaddingEncrypt(byte[] data, byte[] key, byte[] keyIV) throws Exception {
        DESedeKeySpec spec = new DESedeKeySpec(key);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(THREE_DES);
        Key desKey = factory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5_PADDING);
        IvParameterSpec ips = new IvParameterSpec(keyIV);
        cipher.init(Cipher.ENCRYPT_MODE, desKey, ips);
        return cipher.doFinal(data);
    }

    /**
     * des3CBCPKCS5PaddingDecrypt
     *
     * @param data  需要加密的数据，，使用PKCS5Padding方式，不足8位会自动补齐
     * @param key   秘钥，DESedeKeySpec会帮你生成24位秘钥，key可以是任意长度
     * @param keyIV 初始向量
     * @return
     * @throws Exception
     */
    public static byte[] des3CBCPKCS5PaddingDecrypt(byte[] data, byte[] key, byte[] keyIV) throws Exception {
        //DESedeKeySpec会帮你生成24位秘钥，key可以是任意长度
        DESedeKeySpec spec = new DESedeKeySpec(key);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(THREE_DES);
        Key desKey = factory.generateSecret(spec);

        Cipher cipher = Cipher.getInstance(DES_CBC_PKCS5_PADDING);
        IvParameterSpec ips = new IvParameterSpec(keyIV);
        cipher.init(Cipher.DECRYPT_MODE, desKey, ips);
        return cipher.doFinal(data);
    }


    /**
     * 生成24字节的3DES密钥。（不够24字节，则补0；超过24字节，则取前24字节。）
     * 示例：DESedeKeySpec spec = new DESedeKeySpec(get3DesKey(key));
     *
     * @param key 密钥字符串
     * @return
     */
    private static byte[] get3DesKey(byte[] key) {
        byte[] keyBytes = new byte[24];
        if (key.length > 24) {
            for (int i = 0; i < 24; i++) {
                keyBytes[i] = key[i];
            }
        } else {
            for (int i = 0; i < 24; i++) {
                if (i < key.length) {
                    keyBytes[i] = key[i];
                } else {
                    keyBytes[i] = 0x00;
                }
            }
        }
        return keyBytes;
    }

    /**
     * 3DES-CBC计算MAC举例如下：
     * 密钥：A0A1A2A3A4A5A6A7B0B1B2B3B4B5B6B7C0C1C2C3C4C5C6C7
     * IV:	0000000000000000
     * 输入数据：fc9ed8c43101ff1606503253573131ffff12588a80000000
     * 【随机数】	    fc9ed8c4
     * 【安全芯片ID】	3101ff1606503253573131ffff12588a
     * 【Padding】	    80000000
     * 加密结果：0F6C47D2DDA9E91AFC49D58156C1AFF7B6A452EF0875E829
     * MAC值：B6A452EF0875E829【加密结果最后8字节】
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        //要加密的数据
        byte[] data = "abcdefgh".getBytes();
        //秘钥
        byte[] key = HexUtil.toBytes("A0A1A2A3A4A5A6A7B0B1B2B3B4B5B6B7C0C1C2C3C4C5C6C7");
        //初始向量
        byte[] keyIV = HexUtil.toBytes("0000000000000000");

        // -----------------
        // 测试：noPadding
        // -----------------

        //加密后的byte[]
        byte[] encodeByte_CBC = des3CBCNoPaddingEncrypt(data, key, keyIV);
        System.out.println("加密后的数据是:" + HexUtil.toHex(encodeByte_CBC));

        //解密后的byte[]
        byte[] decodeByte_CBC = des3CBCNoPaddingDecrypt(encodeByte_CBC, key, keyIV);
        System.out.println("解密后的数据是:" + HexUtil.toHex(decodeByte_CBC));
        System.out.println("解密后的字符串是:" + new String(decodeByte_CBC, UTF_8));


        // -----------------
        // 测试：PKCS5Padding
        // -----------------

        //要加密的数据
        byte[] data1 = "abcdefgh1".getBytes();
        //秘钥
        byte[] key1 = HexUtil.toBytes("A0A1A2A3A4A5A6A7B0B1B2B3B4B5B6B7C0C1C2C3C4C5C6C7");
        //初始向量
        byte[] keyIV1 = HexUtil.toBytes("0000000000000000");

        //加密后的byte[]
        byte[] encodeByte_CBC1 = des3CBCPKCS5PaddingEncrypt(data1, key1, keyIV1);
        System.out.println("加密后的数据是:" + HexUtil.toHex(encodeByte_CBC1));

        //解密后的byte[]
        byte[] decodeByte_CBC1 = des3CBCPKCS5PaddingDecrypt(encodeByte_CBC1, key1, keyIV1);
        System.out.println("解密后的数据是:" + HexUtil.toHex(decodeByte_CBC1));
        System.out.println("解密后的字符串是:" + new String(decodeByte_CBC1, UTF_8));
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