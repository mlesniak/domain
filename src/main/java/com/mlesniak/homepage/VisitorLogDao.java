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
            createVisistorLog(request, response);
        } else {
            updateVisitorLog(request, visitorLog);
        }
    }

    private void createVisistorLog(HttpServletRequest request, HttpServletResponse response) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        String cookieId = RandomStringUtils.random(40, true, true);
        Cookie cookie = new Cookie(getCookieName(), cookieId);

        VisitorLog visitorLog = new VisitorLog();
        visitorLog.setCounter(1);
        visitorLog.setIp(request.getRemoteHost());
        visitorLog.setId(cookieId);
        visitorLog.setSessionId(request.getSession().getId());

        em.persist(visitorLog);
        response.addCookie(cookie);
    }

    @TransactionAttribute
    private void updateVisitorLog(HttpServletRequest request, VisitorLog visitorLog) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {

        visitorLog.setCounter(visitorLog.getCounter() + 1);
        visitorLog.setIp(request.getRemoteHost());
        visitorLog.setSessionId(request.getSession().getId());

        em.persist(visitorLog);
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

    protected String getCookieName() {
        return VisitorLog.class.getPackage().getName();
    }
}
