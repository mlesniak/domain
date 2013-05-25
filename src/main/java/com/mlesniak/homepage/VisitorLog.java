package com.mlesniak.homepage;

import org.apache.openjpa.persistence.jdbc.Unique;

import javax.persistence.*;
import java.util.Date;

/**
 * DO for logging visitor frequency.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
@Entity
public class VisitorLog {
    @Id
    @GeneratedValue
    private int pk;
    @Unique
    private String id;
    private String sessionId;
    private String ip;
    private int counter;
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "VisitorLog{" +
                       "pk=" + pk +
                       ", id='" + id + '\'' +
                       ", sessionId='" + sessionId + '\'' +
                       ", ip='" + ip + '\'' +
                       ", counter=" + counter +
                       ", timestamp=" + timestamp +
                       '}';
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
