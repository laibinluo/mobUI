package com.example.admin.myapplication;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class test {
    public static void main(String[] args){
        System.out.println(" passwd : " + getCustomPasswd());
    }

    private   static String getCustomPasswd() {
        String pwdPath = "D:\\123.txt";
        BufferedReader br = null;
        try {
            File file = new File(pwdPath);
            if (!file.exists()) {
                System.out.println(" file is not exist ， pwdPath : " + pwdPath);
                return null;
            }
            br = new BufferedReader(new FileReader(file));
            String s = null;
            s = br.readLine();
            if (s == null) {
                return null;
            }
            System.out.println("加密后的字符串为:" + s);
            String passwd = com.example.admin.myapplication.Base64.decode(s);
            System.out.println("解密后的字符串为" + passwd);
            return passwd;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (br != null){
                    br.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }
}
