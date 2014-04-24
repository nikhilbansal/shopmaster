package com.shoppingbox.util.dbInitialization;

/**
 * Created by nikhil.bansal on 21/04/14.
 */

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.shoppingbox.BBConfiguration;
import com.shoppingbox.Global;
import com.shoppingbox.db.DbHelper;
import com.shoppingbox.exception.InvalidAppCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

public class OrientDBFilter implements Filter {

    final static Logger logger = LoggerFactory.getLogger(OrientDBFilter.class);
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("init OrientDBFilter");
        Global global = new Global();
        global.beforeStart();
        global.onLoadConfig();
        global.onStart();
        return;
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            DbHelper.open(BBConfiguration.configuration.getString(BBConfiguration.ADMIN_USERNAME), BBConfiguration.configuration.getString(BBConfiguration.ADMIN_PASSWORD));
        } catch (InvalidAppCodeException e) {
            e.printStackTrace();
        }
        logger.info("doFilter return the execution to the servlet");
        try{
            chain.doFilter(request, response);
        } finally {
            logger.info("doFilter return connection to pool");
            DbHelper.close(DbHelper.getConnection());
        }
    }

    public void destroy() {
        logger.info("destroy OrientDBFilter connection pool");
        Global global = new Global();
        global.onStop();
        ODatabaseDocumentPool.global().close();
    }
}
