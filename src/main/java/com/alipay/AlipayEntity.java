package com.alipay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AlipayEntity {
    public static final String CHARSET = "utf-8";
    private String product;
    private String detial;
    private String orderId;
    private float price;

    public AlipayEntity(String orderId, String product, String detial, float price) {
        try {
            this.orderId = URLEncoder.encode(orderId, "utf-8");
            this.product = URLEncoder.encode(product, "utf-8");
            this.detial = URLEncoder.encode(detial, "utf-8");
            this.price = price;
        } catch (UnsupportedEncodingException var6) {
            var6.printStackTrace();
        }

    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getDetial() {
        return this.detial;
    }

    public void setDetial(String detial) {
        this.detial = detial;
    }

    public float getPrice() {
        return this.price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
