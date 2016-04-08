package com.alipay;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;


public class SignUtils {
    private static final String ALGORITHM = "RSA";
    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";
    private static final String DEFAULT_CHARSET = "UTF-8";

    public SignUtils() {
    }

    public static String sign(String content, String privateKey) {
        try {
            PKCS8EncodedKeySpec e = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(e);
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(priKey);
            signature.update(content.getBytes("UTF-8"));
            byte[] signed = signature.sign();
            return Base64.encode(signed);
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }
}
