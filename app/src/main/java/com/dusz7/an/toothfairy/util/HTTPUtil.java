package com.dusz7.an.toothfairy.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dusz2 on 2016/12/28 0028.
 */

public class HTTPUtil {

    private String urlStr;

    private HttpURLConnection conn;
    private URL url;

    public HTTPUtil(String urlStr){
        this.urlStr = urlStr;
    }

    public String getMethod(){
        String gettingData = "";
        try {
            //封装访问服务器的地址
            url=new URL(urlStr);
            try {
                //打开对服务器的连接
                conn=(HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(5000);           //设置连接超时时间
                conn.setDoInput(true);                  //打开输入流，以便从服务器获取数据
                conn.setRequestMethod("GET");
                try {

                    int response = conn.getResponseCode();            //获得服务器的响应码

                    if(response == HttpURLConnection.HTTP_OK) {

                        Log.d("response","get:"+response);//处理服务器的响应结果

                        /**读入服务器数据的过程**/
                        //得到输入流
                        InputStream is=conn.getInputStream();
                        //创建包装流
                        BufferedReader br=new BufferedReader(new InputStreamReader(is));
                        //定义String类型用于储存单行数据
                        String line=null;
                        //创建StringBuffer对象用于存储所有数据
                        StringBuffer sb=new StringBuffer();
                        while((line=br.readLine())!=null){
                            sb.append(line);
                        }

                        gettingData = sb.toString();
                    }

                }finally {
                    conn.disconnect();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return gettingData;
    }



    public String postMethod(String user){

        String responseState = "";

        try {
            try {
                JSONObject jsonTemp = new JSONObject(user);

                //封装访问服务器的地址
                url=new URL(urlStr);
                try {
                    //打开对服务器的连接
                    conn=(HttpURLConnection) url.openConnection();

                    conn.setConnectTimeout(3000);           //设置连接超时时间
                    conn.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
                    conn.setRequestMethod("POST");
                    conn.setUseCaches(false);

                    byte[] data = jsonTemp.toString().getBytes();

                    //设置请求体的类型是文本类型
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Charset", "UTF-8");
                    //设置请求体的长度
                    conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                    //获得输出流，向服务器写入数据
                    OutputStream outputStream = conn.getOutputStream();

                    DataOutputStream dos = new DataOutputStream(outputStream);
                    dos.write(data);

                    dos.flush();
                    dos.close();

                    int response = conn.getResponseCode();            //获得服务器的响应码
                    Log.d("response","post:"+response);//处理服务器的响应结果

                    if(response == HttpURLConnection.HTTP_OK) {

                        responseState = "OK";

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }catch (JSONException e){
                e.printStackTrace();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return responseState;
    }
}
