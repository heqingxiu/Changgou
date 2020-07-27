package com.gc;

import com.gc.Dao.Person;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: QX_He
 * DATA: 2020/7/6-20:49
 * Description:
 **/
public class MainApplication {

    public static void main(String[] args) {
//        Person person = new Person(100,"TenXun");

        String dateFormat = "yyyy-MM-dd HH:mm:ss";// print: 2020-07-18 14:23:11   recommend this method.
//        String dateFormat = "yyyy-MM-dd hh:mm:ss"; // print: 2020-07-18 02:22:44
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date localDate = new Date();
        System.out.println(simpleDateFormat.format(localDate));

        System.out.println("Current time one:" + System.currentTimeMillis());
        System.out.println("Current time two:" + new Date().getTime());
        System.out.println("Current time three:" + System.nanoTime());

    }
}
