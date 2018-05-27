package com.whz.utils.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * 发件人账号和授权码
 *
 * @author wanghz
 */
public class MailAuthenticator extends Authenticator {

    public static String USERNAME = "";
    public static String PASSWORD = "";

    public MailAuthenticator() {
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(USERNAME, PASSWORD);
    }

}