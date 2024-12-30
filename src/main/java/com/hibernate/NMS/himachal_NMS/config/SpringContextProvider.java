package com.hibernate.NMS.himachal_NMS.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringContextProvider {

    private static ApplicationContext context;

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        SpringContextProvider.context = context;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }
}
