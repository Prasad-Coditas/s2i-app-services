package com.snap2buy.themobilebackend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harshal
 */
public class S2BLogger {

    private Logger logger;

    public S2BLogger(String className){
        this.logger = LoggerFactory.getLogger(className);
    }

    public void info(String message){
        this.logger.info(message);
    }

    public void info(String message, Exception e){
        this.logger.info(message, e);
    }

    public void error(String message){
        this.logger.error(message);
    }

    public void error(String message, Exception e){
        this.logger.error(message, e);
    }

    public void debug(String message){
        this.logger.debug(message);
    }

}
