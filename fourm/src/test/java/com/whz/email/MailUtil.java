package com.whz.email;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MailUtil {

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
                    MimeBodyPart filePart = new MimeBodyPart();
                    filePart.attachFile(file);
                    filePart.setFileName(MimeUtility.encodeText(file.getName()));
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

}
