package com.breech.extremity.service.impl;

import com.breech.extremity.core.constant.NotificationConstant;
import com.breech.extremity.core.service.redis.RedisService;
import com.breech.extremity.dto.NotificationDTO;
import com.breech.extremity.model.User;
import com.breech.extremity.service.JavaMailService;
import com.breech.extremity.service.UserService;
import com.breech.extremity.util.Utils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class JavaMailServiceImpl implements JavaMailService {
    /**
     * Java邮件发送器
     */
    @Resource
    private JavaMailSenderImpl mailSender;

    @Resource
    private RedisService redisService;

    @Resource
    private UserService userService;

    @Resource
    private TemplateEngine templateEngine;

    @Value("${spring.mail.host}")
    private String SERVER_HOST;
    @Value("${spring.mail.port}")
    private String SERVER_PORT;
    @Value("${spring.mail.username}")
    private String USERNAME;
    @Value("${spring.mail.password}")
    private String PASSWORD;
    @Value("${resource.domain}")
    private String BASE_URL;

    @Override
    public Integer sendEmailCode(String email) throws MessagingException {
        return sendCode(email, 0);
    }

    @Override
    public Integer sendPassword(String email, String password) throws MessagingException{
        return sendPassword2(email, password);
    }

    @Override
    public Integer sendForgetPasswordEmail(String email) throws MessagingException {
        return sendCode(email, 1);
    }

    @Override
    public Integer sendNotification(NotificationDTO notification) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.ssl.enable", true);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", SERVER_PORT);
        props.put("mail.user", USERNAME);
        props.put("mail.password", PASSWORD);
        mailSender.setJavaMailProperties(props);
        User user = userService.findById(String.valueOf(notification.getIdUser()));
        if (NotificationConstant.Comment.equals(notification.getDataType())) {
            String url = notification.getDataUrl();
            String thymeleafTemplatePath = "mail/commentNotification";
            Map<String, Object> thymeleafTemplateVariable = new HashMap<String, Object>(4);
            thymeleafTemplateVariable.put("user", notification.getAuthor().getUserNickname());
            thymeleafTemplateVariable.put("articleTitle", notification.getDataTitle());
            thymeleafTemplateVariable.put("content", notification.getDataSummary());
            thymeleafTemplateVariable.put("url", url);

            sendTemplateEmail(USERNAME,
                    new String[]{user.getEmail()},
                    new String[]{},
                    "Extremity 消息通知",
                    thymeleafTemplatePath,
                    thymeleafTemplateVariable);
            return 1;
        }
        return 0;
    }

    private Integer sendCode(String to, Integer type) throws MessagingException {
        Properties props = new Properties();
        // 表示SMTP发送邮件，需要进行身份验证
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.ssl.enable", true);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", SERVER_PORT);
        props.put("mail.user", USERNAME);
        props.put("mail.password", PASSWORD);
        mailSender.setJavaMailProperties(props);
        Map<String, Object> thymeleafTemplateVariable = new HashMap<String, Object>(1);

        if (type == 0) {
            Integer code = Utils.genCode();
            redisService.set(to, code, 5 * 60);
            String thymeleafTemplatePath = "mail/registerCodeTemplate.html";
            thymeleafTemplateVariable.put("code", code);
            sendTemplateEmail(USERNAME,new String[]{to},new String[]{},
                    "Extremity 验证码",thymeleafTemplatePath,thymeleafTemplateVariable);
            return 1;
        } else if (type == 1) {
            String code = Utils.entryptPassword(to);
            String url = BASE_URL+ "/" + code;
            redisService.set(code, to, 15 * 60);
            String thymeleafTemplatePath = "mail/forgetPasswordTemplate";

            thymeleafTemplateVariable.put("url", url);

            sendTemplateEmail(USERNAME,
                    new String[]{to},
                    new String[]{},
                    "Extremity 找回密码",
                    thymeleafTemplatePath,
                    thymeleafTemplateVariable);
            return 1;
        }
        return 0;
    }

    private Integer sendPassword2(String to, String password) throws MessagingException {
        Properties props = new Properties();
        // 表示SMTP发送邮件，需要进行身份验证
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.ssl.enable", true);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", SERVER_PORT);
        props.put("mail.user", USERNAME);
        props.put("mail.password", PASSWORD);
        mailSender.setJavaMailProperties(props);
        Map<String, Object> thymeleafTemplateVariable = new HashMap<String, Object>(1);
        thymeleafTemplateVariable.put("password", password);
        String thymeleafTemplatePath = "mail/allocateAccountPassword";
        sendTemplateEmail(USERNAME,
                new String[]{to},
                new String[]{},
                "Extremity 账号密码",
                thymeleafTemplatePath,
                thymeleafTemplateVariable);
        return 1;
    }

    /**
     * 发送thymeleaf模板邮件
     *
     * @param deliver                   发送人邮箱名 如： javalsj@163.com
     * @param receivers                 收件人，可多个收件人 如：11111@qq.com,2222@163.com
     * @param carbonCopys               抄送人，可多个抄送人 如：33333@sohu.com
     * @param subject                   邮件主题 如：您收到一封高大上的邮件，请查收。
     * @param thymeleafTemplatePath     邮件模板 如：mail\mailTemplate.html。
     * @param thymeleafTemplateVariable 邮件模板变量集
     */
    public void sendTemplateEmail(String deliver, String[] receivers, String[] carbonCopys, String subject, String thymeleafTemplatePath,
                                  Map<String, Object> thymeleafTemplateVariable) throws MessagingException {
        String text = null;
        if (thymeleafTemplateVariable != null && !thymeleafTemplateVariable.isEmpty()) {
            Context context = new Context();
            thymeleafTemplateVariable.forEach(context::setVariable);
            text = templateEngine.process(thymeleafTemplatePath, context);
        }
        sendMimeMail(deliver, receivers, carbonCopys, subject, text, true, null);
    }

    /**
     * 发送的邮件(支持带附件/html类型的邮件)
     *
     * @param deliver             发送人邮箱名 如： javalsj@163.com
     * @param receivers           收件人，可多个收件人 如：11111@qq.com,2222@163.com
     * @param carbonCopys         抄送人，可多个抄送人 如：3333@sohu.com
     * @param subject             邮件主题 如：您收到一封高大上的邮件，请查收。
     * @param text                邮件内容 如：测试邮件逗你玩的。 <html><body><img
     *                            src=\"cid:attchmentFileName\"></body></html>
     * @param attachmentFilePaths 附件文件路径 如：
     *                            需要注意的是addInline函数中资源名称attchmentFileName需要与正文中cid:attchmentFileName对应起来
     * @throws MessagingException 邮件发送过程中的异常信息
     */
    private void sendMimeMail(String deliver, String[] receivers, String[] carbonCopys, String subject, String text,
                              boolean isHtml, String[] attachmentFilePaths) throws MessagingException {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(deliver);
        helper.setTo(receivers);
        helper.setCc(carbonCopys);
        helper.setSubject(subject);
        helper.setText(text, isHtml);
        // 添加邮件附件
        if (attachmentFilePaths != null) {
            for (String attachmentFilePath : attachmentFilePaths) {
                File file = new File(attachmentFilePath);
                if (file.exists()) {
                    String attachmentFile = attachmentFilePath
                            .substring(attachmentFilePath.lastIndexOf(File.separator));
                    long size = file.length();
                    if (size > 1024 * 1024) {
                        String msg = String.format("邮件单个附件大小不允许超过1MB，[%s]文件大小[%s]。", attachmentFilePath,
                                file.length());
                        throw new RuntimeException(msg);
                    } else {
                        FileSystemResource fileSystemResource = new FileSystemResource(file);
                        helper.addInline(attachmentFile, fileSystemResource);
                    }
                }
            }
        }
        mailSender.send(mimeMessage);
        stopWatch.stop();

    }

}
