package com.changgou.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 转换工具类
 */
public class ConvertUtils {

    /**
     * 输入流转换为xml字符串
     * @param inputStream
     * @return
     */
    public static String convertToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {   //  Wire data to buffer from inputStream by read .
            outSteam.write(buffer, 0, len);     // Wire data to outStream from buffer
        }
        outSteam.close();
        inputStream.close();
        String result  = new String(outSteam.toByteArray(), "utf-8");  //  1. outStream data change to Byte  2. To change to String with utf-8
        return result;
    }
}
