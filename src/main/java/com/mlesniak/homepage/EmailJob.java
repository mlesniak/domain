package com.mlesniak.homepage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.joda.time.DateTime;

import javax.ejb.Stateless;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/** @author Michael Lesniak (mail@mlesniak.com) */
@Stateless
public class EmailJob extends BackgroundThread {
    @Inject
    VisitorLogDao dao;
    @Inject
    Config config;
    private String username;
    private String password;
    private String to;

    @Override
    public void run() {
        try {
            init();
        } catch (IOException e) {
            System.out.println("Error loading email configuration.");
            return;
        }

        sendEmail();
        while (true) {
            try {
                Thread.sleep(1000 * 60 * config.delayBetweenEmailsInMinutes());
            } catch (InterruptedException e) {
                System.out.println("Waiting interrupted. Aborting.");
                return;
            }
            sendEmail();
        }
    }

    private void sendEmail() {
        Email email = new SimpleEmail();
        email.setHostName("email-smtp.us-east-1.amazonaws.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator(username, password));
        email.setSSLOnConnect(true);
        try {
            email.setFrom("mail@mlesniak.com");
            email.setSubject("[mlesniak.com] Statistic (" + new Date() + ")");
            email.setMsg(getMessage());
            email.addTo(to);
            email.send();
            System.out.println(getMessage());
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }

    private String getMessage() {
        DateTime today = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        DateTime tomorrow = today.plusDays(1);
        List logs = dao.getVisitorLogs(today.toDate(), tomorrow.toDate());

        StringBuilder sb = new StringBuilder();
        for (Object log : logs) {
            VisitorLog vlog = (VisitorLog) log;
            sb.append(StringUtils.rightPad(vlog.getTimestamp().toString(), 31));
            sb.append(StringUtils.rightPad(vlog.getIp(), 32));
            sb.append(StringUtils.rightPad(Integer.toString(vlog.getCounter()), 5));
            sb.append('\n');
        }

        return sb.toString();
    }

    private void init() throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(new File(getAttribute("email-config"))));

        username = prop.getProperty("username");
        password = prop.getProperty("password");
        to = prop.getProperty("to");
    }

    @Override
    public void inject(BeanManager beanManager) {
        InjectionTarget<EmailJob> injectionTarget = beanManager.createInjectionTarget(beanManager.createAnnotatedType(EmailJob.class));
        injectionTarget.inject(this, beanManager.<EmailJob>createCreationalContext(null));
    }
}
