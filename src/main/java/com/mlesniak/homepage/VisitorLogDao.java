package com.mlesniak.homepage;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.*;
import java.util.Date;
import java.util.List;

/**
 * DAO for VisitorLog and additional methods for cookie handling.
 *
 * @author Michael Lesniak (mail@mlesniak.com)
 */
@Stateless
@TransactionAttribute
public class VisitorLogDao {
    @PersistenceContext
    EntityManager em;

    public void updateOrCreateVisitorLog(HttpServletRequest request, HttpServletResponse response) throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        String cookie = getCookie(request, getCookieName());
        VisitorLog visitorLog = getVisitorLogByCookie(cookie);

        if (visitorLog == null) {
            // No cookie found.
            visitorLog = getVisitorLogByIp(request.getRemoteHost());
            if (visitorLog == null) {
                // No IP found. Create new entry.
                createVisistorLog(request, response);
            } else {
                // IP found. Update cookie.
                updateVisitorLog(request, response, visitorLog, true);
            }
        } else {
            // Cookie found. Update counter.
            updateVisitorLog(request, response, visitorLog, false);
        }
    }

    private VisitorLog getVisitorLogByIp(String ip) {
        if (ip == null) {
            return null;
        }

        Query query = em.createQuery("SELECT c FROM VisitorLog c WHERE c.ip = :ip");
        query.setParameter("ip", ip);

        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }

        // Multiple result can not happen since the id is unique.
        return (VisitorLog) results.get(0);
    }

    private void createVisistorLog(HttpServletRequest request, HttpServletResponse response) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        String cookieId = getRandomCookieValue();

        VisitorLog visitorLog = new VisitorLog();
        visitorLog.setCounter(1);
        visitorLog.setIp(request.getRemoteHost());
        visitorLog.setId(cookieId);
        visitorLog.setTimestamp(new Date());

        em.persist(visitorLog);
        response.addCookie(new Cookie(getCookieName(), cookieId));
    }

    @TransactionAttribute
    private void updateVisitorLog(HttpServletRequest request, HttpServletResponse response, VisitorLog visitorLog, boolean updateCookie) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        visitorLog.setCounter(visitorLog.getCounter() + 1);
        visitorLog.setIp(request.getRemoteHost());
        visitorLog.setTimestamp(new Date());

        if (updateCookie) {
            String cookieId = getRandomCookieValue();
            visitorLog.setId(cookieId);
            response.addCookie(new Cookie(getCookieName(), cookieId));
        }

        em.persist(visitorLog);
    }

    private String getRandomCookieValue() {
        return RandomStringUtils.random(40, true, true);
    }

    private String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (StringUtils.equals(cookie.getName(), name)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private VisitorLog getVisitorLogByCookie(String id) {
        if (id == null) {
            return null;
        }

        Query query = em.createQuery("SELECT c FROM VisitorLog c WHERE c.id = :id");
        query.setParameter("id", id);

        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }

        // Multiple result can not happen since the id is unique.
        return (VisitorLog) results.get(0);
    }

    public List getVisitorLogs(Date start, Date end) {
        Query query = em.createQuery("SELECT c FROM VisitorLog c WHERE c.timestamp BETWEEN :start AND :end order by c.timestamp asc");
        query.setParameter("start", start);
        query.setParameter("end", end);
        return query.getResultList();
    }

    public List getVisitorLogs() {
        Query query = em.createQuery("SELECT c from VisitorLog c order by c.timestamp asc");
        return query.getResultList();
    }

    protected String getCookieName() {
        return VisitorLog.class.getPackage().getName();
    }
}
