package com.mlesniak.homepage;

import com.mlesniak.homepage.config.Config;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

/** @author Michael Lesniak (mail@mlesniak.com) */
@Stateless
public class EmailJob implements Job {
    private static Logger log = LoggerFactory.getLogger(EmailJob.class);
    Config config;

    public void sendEmail() {
        Email email = new SimpleEmail();
        if (config == null) {
            config = Config.getConfig();
        }

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
            if (config.getBoolean("com.mlesniak.homepage.EmailJob.sendEmail")) {
                email.send();
                log.info("Email sent.");
            } else {
                log.info("FAKE Email sent.");
            }
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
        sendEmail();
    }
}
