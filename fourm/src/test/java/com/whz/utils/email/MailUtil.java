package com.whz.utils.email;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MailUtil {

    private final static Logger LOGGER = Logger.getLogger(MailUtil.class);

    /**
     * 发送邮件
     * @param password  授权码（注意不是邮箱登录密码）
     * @param host      邮件服务器（例如：smtp.qq.com）
     * @param from      发件人
     * @param to        接收者邮箱
     * @param subject   邮件主题
     * @param content   邮件内容
     * @return 是否发送成功
     */
    public static boolean sendMail(String password, String host, String from, String to, String subject, String content) {
        return sendMail(password, host, from, to, subject, content, null);
    }

    /**
     * 发送邮件
     * @param password  授权码（注意不是邮箱登录密码）
     * @param host      邮件服务器（例如：smtp.qq.com）
     * @param from      发件人
     * @param to        接收者邮箱
     * @param subject   邮件主题
     * @param content   邮件内容
     * @param files     邮件附件
     * @return 是否发送成功
     */
    public static boolean sendMail(String password, String host, String from, String to, String subject, String content, List<File> files) {
        if (StringUtils.isBlank(password)) {
            LOGGER.error("MailUtil.sendMail error, because password is empty");
            return false;
        }
        if (StringUtils.isBlank(host)) {
            LOGGER.error("MailUtil.sendMail error, because host is empty");
            return false;
        }
        if (StringUtils.isBlank(from)) {
            LOGGER.error("MailUtil.sendMail error, because from is empty");
            return false;
        }
        if (StringUtils.isBlank(to)) {
            LOGGER.error("MailUtil.sendMail error, because to is empty");
            return false;
        }


        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");

        Authenticator auth = new MailAuthenticator();
        MailAuthenticator.USERNAME = from;
        MailAuthenticator.PASSWORD = password;

        Session session = Session.getInstance(props, auth);
        session.setDebug(true);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
            // 邮件主题
            message.setSubject(subject);


            Multipart multipart = new MimeMultipart();

            // 正文(html形式)
            MimeBodyPart bodyContent = new MimeBodyPart();
            bodyContent.setContent(content, "text/html;charset=utf-8");
            multipart.addBodyPart(bodyContent);

            // 附件
            if (files != null) {
                for (File file : files) {
                    // 这种方法会乱码的情况出现
                    // MimeBodyPart filePart = new MimeBodyPart();
                    // filePart.attachFile(file);
                    // filePart.setFileName(MimeUtility.encodeText(file.getName()));
                    // multipart.addBodyPart(filePart);

                    BodyPart filePart = new MimeBodyPart();
                    FileDataSource fileds = new FileDataSource(file.getCanonicalFile());
                    filePart.setFileName(MimeUtility.encodeWord(file.getName()));
                    filePart.setDataHandler(new DataHandler(fileds));
                    multipart.addBodyPart(filePart);


                }
            }

            message.setContent(multipart);
            message.setSentDate(new Date());
            message.saveChanges();

            Transport trans = session.getTransport("smtp");
            trans.send(message);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        String password = "ytawopltyjurec";
        String host = "smtp.qq.com";
        String from = "2321***661@qq.com";
        String to = "10733***47@qq.com";
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
            files.add(new File("/Users/wanghongzhan/Documents/whz/temp/test.xls"));
            MailUtil.sendMail(password, host, from, to, subject, content.toString(), files);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
