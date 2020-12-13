package com.melon.android.tool;

import android.accounts.NetworkErrorException;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cretin on 2017/3/22.
 * HttpURLConnection 网络请求工具类
 * <p>
 * 数据的请求都是基于HttpURLConnection的 请求成功与失败的回调都是在主线程
 * 可以直接更新UI
 */
public class HttpUtil {
    static ExecutorService threadPool = Executors.newCachedThreadPool();

    public interface HttpCallbackStringListener {
        // 网络请求成功
        void onFinish(String response);

        // 网络请求失败
        void onError(Exception e);
    }

    /**
     * GET方法 返回数据会解析成字符串String
     *
     * @param context   上下文
     * @param urlString 请求的url
     * @param listener  回调监听
     */
    public static void doGet(final Context context, final String urlString, final HttpCallbackStringListener listener) {
        doPost(context, urlString, null, listener);
    }


    /**
     * GET方法 返回数据会解析成字符串 String
     *
     * @param context   上下文
     * @param urlString 请求的路径
     * @param listener  回调监听
     * @param params    参数列表
     */
    public static void doPost(final Context context, final String urlString, final Map<String, Object> params, final HttpCallbackStringListener listener) {
        StringBuilder paramContent = new StringBuilder();
        if (params != null) {
            // 组织请求参数
            for (String key : params.keySet()) {
                if (paramContent.length() != 0) {
                    paramContent.append("&");
                }
                paramContent.append(key).append("=").append(params.get(key));
            }
        }

        // 因为网络请求是耗时操作，所以需要另外开启一个线程来执行该任务。
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                URL url;
                HttpURLConnection httpURLConnection = null;
                try {
                    url = new URL(urlString);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    if (params != null) {
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setConnectTimeout(5000);
                        httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                        httpURLConnection.setRequestProperty("Content-Length", paramContent.toString().getBytes().length+"");
                        // 设置运行输出
                        httpURLConnection.setDoOutput(true);

                        // 发送请求参数
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        outputStream.write(paramContent.toString().getBytes());
                    } else {
                        httpURLConnection.setRequestMethod("GET");
                    }

                    if (httpURLConnection.getResponseCode() == 200) {
                        // 获取网络的输入流
                        InputStream is = httpURLConnection.getInputStream();
                        BufferedReader bf = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        //最好在将字节流转换为字符流的时候 进行转码
                        StringBuilder buffer = new StringBuilder();
                        String line = "";
                        while ((line = bf.readLine()) != null) {
                            buffer.append(line);
                        }
                        bf.close();
                        is.close();
                        listener.onFinish(buffer.toString());
                    } else {
                        listener.onError(new NetworkErrorException("response err code:" + httpURLConnection.getResponseCode()));
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        // 回调onError()方法
                        listener.onError(e);
                    }
                } finally {
                    if (httpURLConnection != null) {
                        // 最后记得关闭连接
                        httpURLConnection.disconnect();
                    }
                }
            }
        });
    }
}