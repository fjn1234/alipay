package com.alipay;


import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;


public class AlipayHelper {
    public String Partner = "";
    public String Seller = "";
    public String RSA_Private = "";
    public String RSA_Public = "";
    public String NotifyUrl = "";
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_CHECK_FLAG = 2;
    private PaySucessCallback paySucessCallback;
    private PayFailCallback payFailCallback;
    private PayNotFinishCallback payNotFinishCallback;
    private PayCheckCallback payCheckCallback;
    private Activity activity;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Result resultObj = new Result((String) msg.obj);
                    String resultStatus = resultObj.resultStatus;
                    if (TextUtils.equals(resultStatus, "9000")) {
                        if (AlipayHelper.this.paySucessCallback != null) {
                            AlipayHelper.this.paySucessCallback.onSucces(resultObj);
                        }
                    } else if (TextUtils.equals(resultStatus, "8000")) {
                        if (AlipayHelper.this.payNotFinishCallback != null) {
                            AlipayHelper.this.payNotFinishCallback.notFinish(resultObj);
                        }
                    } else if (AlipayHelper.this.payFailCallback != null) {
                        AlipayHelper.this.payFailCallback.onFail(resultObj);
                    }
                    break;
                case 2:
                    if (AlipayHelper.this.payCheckCallback != null) {
                        AlipayHelper.this.payCheckCallback.onCheck(msg.obj);
                    }
            }

        }
    };

    private AlipayHelper() {
    }

    public AlipayHelper(Activity activity, String parter, String seller, String rsa_private, String rsa_public, String notifyUrl) {
        this.activity = activity;
        this.Partner = parter;
        this.Seller = seller;
        this.RSA_Private = rsa_private;
        this.RSA_Public = rsa_public;
        this.NotifyUrl = notifyUrl;
    }

    public void pay(AlipayEntity entity, PaySucessCallback paySucessCallback, PayFailCallback payFailCallback, PayNotFinishCallback payNotFinishCallback) {
        this.paySucessCallback = paySucessCallback;
        this.payFailCallback = payFailCallback;
        this.payNotFinishCallback = payNotFinishCallback;
        BigDecimal bg = new BigDecimal((double) entity.getPrice());
        entity.setPrice(bg.setScale(2, 4).floatValue());
        String orderInfo = this.getOrderInfo(entity.getOrderId(), entity.getProduct(), entity.getDetial(), String.valueOf(entity.getPrice()));
        String sign = this.sign(orderInfo);

        try {
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException var11) {
            var11.printStackTrace();
        }

        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + this.getSignType();
        Runnable payRunnable = new Runnable() {
            public void run() {
                PayTask alipay = new PayTask(AlipayHelper.this.activity);
                String result = alipay.pay(payInfo);
                Message msg = new Message();
                msg.what = 1;
                msg.obj = result;
                AlipayHelper.this.mHandler.sendMessage(msg);
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    public void check(PayCheckCallback payCheckCallback) {
        this.payCheckCallback = payCheckCallback;
        Runnable checkRunnable = new Runnable() {
            public void run() {
                PayTask payTask = new PayTask(AlipayHelper.this.activity);
                boolean isExist = payTask.checkAccountIfExist();
                Message msg = new Message();
                msg.what = 2;
                msg.obj = isExist;
                AlipayHelper.this.mHandler.sendMessage(msg);
            }
        };
        Thread checkThread = new Thread(checkRunnable);
        checkThread.start();
    }

    public void getSDKVersion() {
        PayTask payTask = new PayTask(this.activity);
        String version = payTask.getVersion();
    }

    public String getOrderInfo(String order, String subject, String body, String price) {
        String orderInfo = "partner=\"" + this.Partner + "\"";
        orderInfo = orderInfo + "&seller_id=\"" + this.Seller + "\"";
        orderInfo = orderInfo + "&out_trade_no=\"" + order + "\"";
        orderInfo = orderInfo + "&subject=\"" + subject + "\"";
        orderInfo = orderInfo + "&body=\"" + body + "\"";
        orderInfo = orderInfo + "&total_fee=\"" + price + "\"";
        orderInfo = orderInfo + "&notify_url=\"" + this.NotifyUrl + "\"";
        orderInfo = orderInfo + "&service=\"mobile.securitypay.pay\"";
        orderInfo = orderInfo + "&payment_type=\"1\"";
        orderInfo = orderInfo + "&_input_charset=\"utf-8\"";
        orderInfo = orderInfo + "&it_b_pay=\"30m\"";
        orderInfo = orderInfo + "&return_url=\"m.alipay.com\"";
        return orderInfo;
    }

    public String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);
        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    public String sign(String content) {
        return SignUtils.sign(content, this.RSA_Private);
    }

    public String getSignType() {
        return "sign_type=\"RSA\"";
    }

    public interface PayCheckCallback {
        void onCheck(Object var1);
    }

    public interface PayFailCallback {
        void onFail(Result var1);
    }

    public interface PayNotFinishCallback {
        void notFinish(Result var1);
    }

    public interface PaySucessCallback {
        void onSucces(Result var1);
    }

    public class Result {
        String resultStatus;
        String result;
        String memo;

        public Result(String rawResult) {
            try {
                String[] resultParams = rawResult.split(";");
                for (String resultParam : resultParams) {
                    if (resultParam.startsWith("resultStatus")) {
                        resultStatus = gatValue(resultParam, "resultStatus");
                    }
                    if (resultParam.startsWith("result")) {
                        result = gatValue(resultParam, "result");
                    }
                    if (resultParam.startsWith("memo")) {
                        memo = gatValue(resultParam, "memo");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return "resultStatus={" + resultStatus + "};memo={" + memo
                    + "};result={" + result + "}";
        }

        private String gatValue(String content, String key) {
            String prefix = key + "={";
            return content.substring(content.indexOf(prefix) + prefix.length(),
                    content.lastIndexOf("}"));
        }

        public String getMemo() {
            return memo;
        }

        public String getResult() {
            return result;
        }

        public String getResultStatus() {
            return resultStatus;
        }
    }
}
