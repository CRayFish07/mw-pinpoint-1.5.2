package com.navercorp.pinpoint.web.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 邮件发送工具类
 */
public class MailUtil {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Session session;
    private String smtpHost = "";
    private int smtpPort = 25;
    private String senderUserName = "";
    private String senderPassword = "";
    private String senderNickName = "";
    private String receiver = "";
    private String ccReceiver = "";
    private String bccReceiver = "";

    public MailUtil(Properties properties, boolean debug) {
        this.smtpHost = properties.getProperty("mail.smtp.host");
        this.smtpPort = Integer.parseInt(properties.getProperty("mail.smtp.port"));
        this.senderUserName = properties.getProperty("mail.sender.username");
        this.senderPassword = properties.getProperty("mail.sender.password");
        this.senderNickName = properties.getProperty("mail.sender.nickname");

        // if (!StringUtils.isEmpty(senderNickName)) {
        //
        // EncodingUtil parse = new EncodingUtil();
        // logger.debug(parse.getEncoding(senderNickName));
        // try {
        // senderNickName = new String(senderNickName.getBytes("ISO-8859-1"),
        // "gbk");
        // } catch (Exception e) {
        // e.printStackTrace();
        // logger.error("发件人呢称编码转换失败！", e);
        // senderNickName = properties.getProperty("mail.sender.nickname");
        // }
        // }
        this.receiver = properties.getProperty("mail.to");
        this.ccReceiver = properties.getProperty("mail.cc");
        this.bccReceiver = properties.getProperty("mail.bcc");
        session = Session.getDefaultInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderUserName, senderPassword);

            }
        });
        // 开启后有调试信息
        session.setDebug(debug);
    }

    /**
     * 发送Html格式邮件
     *
     * @param subject
     *            邮件标题
     * @param mailBody
     *            邮件内容
     * @param receiveUser
     *            收件人地址
     */
    public void sendHtmlMail(String subject, String mailBody, String receiveUser) {
        sendEmail(subject, mailBody, null, receiveUser, null, null, true);
    }

    /**
     * 发送纯文本邮件
     *
     * @param subject
     *            邮件标题
     * @param mailBody
     *            邮件内容
     * @param receiveUser
     *            收件人地址
     */
    public void sendTextMail(String subject, String mailBody, String receiveUser) {
        sendEmail(subject, mailBody, null, receiveUser, null, null, false);
    }

    /**
     * 发送Html格式邮件
     *
     * @param subject
     *            邮件标题
     * @param mailBody
     *            邮件内容
     */
    public void sendHtmlMail(String subject, String mailBody) {
        sendEmail(subject, mailBody, this.senderNickName, this.receiver, this.ccReceiver, this.bccReceiver, true);
    }

    /**
     * 发送纯文件邮件
     *
     * @param subject
     *            邮件标题
     * @param mailBody
     *            邮件内容
     */
    public void sendTextMail(String subject, String mailBody) {
        sendEmail(subject, mailBody, this.senderNickName, this.receiver, this.ccReceiver, this.bccReceiver, false);
    }

    /**
     * 发送Html格式邮件
     *
     * @param subject
     *            邮件标题
     * @param mailBody
     *            邮件内容
     * @param receiveUser
     *            收件人地址
     * @param ccReceiverUser
     *            抄送地址
     */
    public void sendHtmlMail(String subject, String mailBody, String receiveUser, String ccReceiverUser) {
        sendEmail(subject, mailBody, null, receiveUser, ccReceiverUser, null, true);
    }

    /**
     * 发送纯文本邮件
     *
     * @param subject
     *            邮件标题
     * @param mailBody
     *            邮件内容
     * @param receiveUser
     *            收件人地址
     * @param ccReceiverUser
     *            抄送地址
     */
    public void sendTextMail(String subject, String mailBody, String receiveUser, String ccReceiverUser) {
        sendEmail(subject, mailBody, null, receiveUser, ccReceiverUser, null, false);
    }

    /**
     * 发送邮件
     *
     * @param subject
     *            邮件主题
     * @param mailBody
     *            邮件内容
     * @param senderNickName
     *            发件人NickName
     * @param receiveUser
     *            收件人地址
     * @param ccReceiveUser
     *            抄送地址
     * @param bccReceiveUser
     *            密送地址
     */
    public void sendEmail(final String subject, String mailBody, String senderNickName, String receiveUser,
            String ccReceiveUser, String bccReceiveUser, Boolean isHtmlFormat) {

        Transport transport = null;
        MimeMessage message;
        try {
            message = new MimeMessage(session);
            // 发件人
            InternetAddress from;
            if (StringUtils.isEmpty(senderNickName)) {
                from = new InternetAddress(senderUserName);
            } else {
                from = new InternetAddress(MimeUtility.encodeWord(senderNickName) + " <" + senderUserName + ">");
            }
            message.setFrom(from);

            // 收件人(支持多个，中间用";"分隔)
            String[] arrTo = receiveUser.split(";");
            InternetAddress[] toAddrs = new InternetAddress[arrTo.length];
            for (int i = 0; i < arrTo.length; i++) {
                toAddrs[i] = new InternetAddress(arrTo[i]);
            }
            message.setRecipients(Message.RecipientType.TO, toAddrs);

            // 抄送人
            if (!StringUtils.isEmpty(ccReceiveUser)) {
                String[] arrCC = ccReceiveUser.split(";");
                InternetAddress[] ccAddrs = new InternetAddress[arrCC.length];
                for (int i = 0; i < arrCC.length; i++) {
                    ccAddrs[i] = new InternetAddress(arrCC[i]);
                }
                message.setRecipients(Message.RecipientType.CC, ccAddrs);
            }

            // 密送人
            if (!StringUtils.isEmpty(bccReceiveUser)) {
                String[] arrBCC = bccReceiveUser.split(";");
                InternetAddress[] bccAddrs = new InternetAddress[arrBCC.length];
                for (int i = 0; i < arrBCC.length; i++) {
                    bccAddrs[i] = new InternetAddress(arrBCC[i]);
                }
                message.setRecipients(Message.RecipientType.BCC, bccAddrs);
            }

            String subjectEnCode = MimeUtility.encodeWord(subject, "UTF-8", "Q");// 解决邮件标题乱码问题

            message.setSubject(subjectEnCode);
            String content = mailBody.toString();

            if (isHtmlFormat) {
                message.setContent(content, "text/html;charset=UTF-8");
            } else {
                message.setContent(content, "text/plain;charset=UTF-8");
            }
            message.saveChanges();
            transport = session.getTransport("smtp");
            transport.connect(smtpHost, smtpPort, senderUserName, senderPassword);
            transport.sendMessage(message, message.getAllRecipients());

            logger.info(senderUserName + " 向 " + receiveUser + " 发送邮件成功！");

        } catch (Exception e) {
            logger.error("sendEmail失败！", e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    // e.printStackTrace();
                    logger.error("sendEmail->transport关闭失败！", e);
                }
            }
        }
    }

}
