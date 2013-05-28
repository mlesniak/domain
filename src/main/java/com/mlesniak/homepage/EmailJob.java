package com.mlesniak.homepage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

/** @author Michael Lesniak (mail@mlesniak.com) */
@Stateless
public class EmailJob implements Job {
    Config config;

    private void sendEmail() {
        Email email = new SimpleEmail();

        try {
            email.setHostName("email-smtp.us-east-1.amazonaws.com");
            email.setSmtpPort(465);
            DefaultAuthenticator auth = new DefaultAuthenticator(config.get("username"), config.get("password"));
            email.setAuthenticator(auth);
            email.setSSLOnConnect(true);
            email.setFrom("mail@mlesniak.com");
            email.setSubject("[mlesniak.com] Statistic (" + new Date() + ")");
            email.setMsg(getMessage());
            email.addTo(config.get("to"));
            email.send();
            System.out.println("Email sent.");
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }

    private String getMessage() {
        DateTime today = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        DateTime tomorrow = today.plusDays(1);
        List logs = DaoManager.get().getVisitorLogDao().getVisitorLogs(today.toDate(), tomorrow.toDate());

        StringBuilder sb = new StringBuilder();
        for (Object log : logs) {
            VisitorLog vlog = (VisitorLog) log;
            sb.append(StringUtils.rightPad(vlog.getTimestamp().toString(), 31));
            sb.append(StringUtils.rightPad(vlog.getIp(), 32));
            sb.append(StringUtils.rightPad(Integer.toString(vlog.getCounter()), 5));
            sb.append('\n');
        }

        if (sb.length() == 0) {
            sb.append("No entries found.");
        }

        return sb.toString();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        config = Config.getConfig();
        sendEmail();
    }
}
