package com.whz.utils.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密基本原理是将明文切分成若干相同的小段，然后对每一段进行加密和解密，最后组合就是最终的结果。
 *
 * AES算法有AES-128、AES-192、AES-256三种，分别对应的秘钥是 16、24、32字节长度，同样对应的加密解密分组数据长度BlockSize也是16、24、32字节长度。
 * 该类默认使用16字节长度的秘钥
 *
 * Created by wanghz on 2018/10/15.
 */
public class AES128Util {

    private static final String AES = "AES";

    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";

    private static final String AES_CBC_NO_PADDING = "AES/CBC/NoPadding";

    /** 默认的IV初始向量，16字节，这里是32个16进制字符串表示的IV */
    private static final byte[] DEFAULT_AES_VI = HexUtil.toBytes("00000000000000000000000000000000");

    /**
     * AES加密：
     * 填充方式为NoPadding时，最后一个块的填充内容由程序员确定，通常为0.
     * AES/CBC/NoPadding加密的明文长度必须是16的整数倍，明文长度不满足16时，程序员要扩充到16的整数倍，另外如果程序扩充的话，解密完后要把扩充的字符干掉
     *
     * @param data      要加密的数据
     * @param key       秘钥key必须为16字节
     * @param aesIV     初始向量，必须为16字节，如果不知道填什么，请使用{@link #DEFAULT_AES_VI}
     * @return
     */
    @Deprecated
    public static byte[] aesCbcNoPaddingEncrypt(byte[] data, byte[] key, byte[] aesIV) throws Exception {
        //加密的数据长度不是16的整数倍时，原始数据后面补0，直到长度满足16的整数倍
        int len = data.length;
        //计算补0后的长度
        while (len % 16 != 0) {
            len++;
        }
        byte[] result = new byte[len];
        //在最后补0
        for (int i = 0; i < len; ++i) {
            if (i < data.length) {
                result[i] = data[i];
            } else {
                //填充字符'a'
                //result[i] = 'a';
                result[i] = 0;
            }
        }
        SecretKeySpec skeySpec = new SecretKeySpec(key, AES);
        //使用CBC模式，需要一个初始向量iv，可增加加密算法的强度
        IvParameterSpec iv = new IvParameterSpec(aesIV);
        Cipher cipher = null;
        try {
            //算法/模式/补码方式
            cipher = Cipher.getInstance(AES_CBC_NO_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return cipher.doFinal(result);
    }

    /**
     * AES解密：TODO whz 该方法不能和{@link #aesCbcNoPaddingEncrypt(byte[], byte[], byte[])}配套使用，解密后可能出现乱码
     *
     * @param data      要解密的数据
     * @param key       秘钥key必须为16字节
     * @param aesIV     初始向量，必须为16字节，如果不知道填什么，请使用{@link #DEFAULT_AES_VI}
     * @return
     */
    @Deprecated
    public static byte[] aesCbcNoPaddingDecrypt(byte[] data, byte[] key, byte[] aesIV) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, AES);
        IvParameterSpec iv = new IvParameterSpec(aesIV);

        Cipher cipher = Cipher.getInstance(AES_CBC_NO_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        return cipher.doFinal(data);
    }

    /**
     * 数据加密
     *
     * @param data      要加密的数据
     * @param key       秘钥key必须为16字节
     * @param aesIV     初始向量，必须为16字节，如果不知道填什么，请使用{@link #DEFAULT_AES_VI}
     * @return
     */
    public static byte[] aesCbcPKCS5PaddingEncrypt(byte[] data, byte[] key, byte[] aesIV) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(aesIV));
        return cipher.doFinal(data);
    }

    /**
     * 数据解密
     *
     * @param data      要加密的数据
     * @param key       秘钥key必须为16字节
     * @param aesIV     初始向量，必须为16字节，如果不知道填什么，请使用{@link #DEFAULT_AES_VI}
     * @return
     */
    public static byte[] aesCbcPKCS5PaddingDecrypt(byte[] data,byte[] key,byte[] aesIV) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, AES);
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(aesIV));
        return cipher.doFinal(data);
    }

    /**
     * AES-192_CBC
     * 解密举例:
     *  解密临时密钥:6F8261CDCBBE1C873081A9DD0E4984F1F45CBFFB7F522209
     *  IV: 00000000000000000000000000000000
     *  输入数据:beb4e9275d5e68c6083622e06bd02132
     *      【授权数据】beb4e9275d5e68c6083622e06bd02132
     *
     *  解密结果: C17101EA598E70083A5A0E1122410E54
     * @param args
     */
    public static void main(String[] args) throws Exception {

        //秘钥
        byte[] key = HexUtil.toBytes("F86D1D6CBBABC4C14975691CFA55DDD3C36CC9B9F2791522");
        //初始向量
        byte[] keyiv = HexUtil.toBytes("00000000000000000000000000000000");

        // -------------------------------
        // 1、测试：AES/CBC/NoPadding
        // -------------------------------

        //要解密的数据
        byte[] data = ":5041734568572d000c3f:7FS4ExhUdArT9Y2fbzAxj6G8a1go52hL".getBytes();


        //加密后的byte[]
        byte[] encodeByte_CBC = aesCbcNoPaddingEncrypt(data, key, keyiv);
        String t = HexUtil.toHex(encodeByte_CBC);
        System.out.println("加密后的数据是:" + t);

        byte[] decodeByte_CBC = aesCbcNoPaddingDecrypt(HexUtil.toBytes(t), key, keyiv);
        // 解密可能会得到部分乱码，这是因为输入的字符串不是16个字节的整数倍，会用0补位
        System.out.println("解密后的数据是:" + HexUtil.toString(HexUtil.toHex(decodeByte_CBC)));


        // -------------------------------
        // 2、测试：AES/CBC/PKCS5Padding
        // -------------------------------

        //加密后的byte[]
        byte[] encodeByte_CBC1 = aesCbcPKCS5PaddingEncrypt(data, key, keyiv);
        String t1 = HexUtil.toHex(encodeByte_CBC1);
        System.out.println("加密后的数据是:" + t1);

        byte[] decodeByte_CBC1 = aesCbcPKCS5PaddingDecrypt(HexUtil.toBytes(t1), key, keyiv);
        System.out.println("解密后的数据是:" + HexUtil.toString(HexUtil.toHex(decodeByte_CBC1)));

    }

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