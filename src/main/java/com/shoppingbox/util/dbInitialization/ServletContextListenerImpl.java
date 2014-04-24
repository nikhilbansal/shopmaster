package com.shoppingbox.util.dbInitialization;

import com.shoppingbox.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by nikhil.bansal on 18/04/14.
 */
@WebListener
public class ServletContextListenerImpl implements ServletContextListener {
    ServletContext context;
    final static Logger logger = LoggerFactory.getLogger(ServletContextListenerImpl.class);
    final static String APP_CONTEXT = "APP_CONTEXT";
    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        logger.info("Creating Context");
        context = contextEvent.getServletContext();
        // set variable to servlet context
//        context.setAttribute("TEST", "TEST_VALUE");
        Global global = new Global();
        global.beforeStart();
        global.onLoadConfig();
        global.onStart();
        context.setAttribute(APP_CONTEXT, global);
        logger.info("Created Context");
    }
    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        context = contextEvent.getServletContext();
        logger.info("Destroying Context");
//        Global global = (Global) context.getAttribute(APP_CONTEXT);
//        global.onStop();
        logger.info("Destroyed Context");
    }
}
