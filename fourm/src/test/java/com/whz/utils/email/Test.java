package com.whz.utils.email;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        String password = "y**********d";
        String host = "smtp.qq.com";
        String from = "2******1@qq.com";
        String to = "1********7@qq.com";
        String subject = "输入邮件主题";

        StringBuffer content = new StringBuffer();
        content.append("<!DOCTYPE>" +
                "<div style='border:1px solid #d9f4ee;font-size:14px;line-height:22px;color:#005aa0;padding-left:1px;padding-top:5px;padding-bottom:5px;'>" +
                "<span style='font-weight:bold;'>温馨提示：</span>" +
                "<div style='width:950px;font-family:arial;'>" +
                "欢迎使用NET微活动，您的注册码为：<br/>" +
                "<h2 style='color:green'>" + "测试..." + "</h2><br/>" +
                "本邮件由系统自动发出，请勿回复。<br/>感谢您的使用。<br/>" +
                "</div>" +
                "</div>");
        try {
            List<File> files = new ArrayList();
            files.add(new File("/Users/wanghongzhan/Documents/whz/test/test1.xls"));
            files.add(new File("/Users/wanghongzhan/Documents/whz/test/test2.png"));
            files.add(new File("/Users/wanghongzhan/Documents/whz/test/test3.pdf"));
            MailUtil.sendMail(password, host, from, to, subject, content.toString(), files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
