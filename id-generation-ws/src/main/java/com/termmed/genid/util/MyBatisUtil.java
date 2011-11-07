package com.termmed.genid.util;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisUtil {
	
	private static final SqlSessionFactory sessionFactory;
	 
    static {
    	Reader reader = null;
        try {
			// Create the SessionFactory
        	String resource = "com/termmed/genid/data/MyIbatisConfig.xml";
        	reader = Resources.getResourceAsReader(resource);
            sessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
 
    public static SqlSessionFactory getSessionFactory() {
        return sessionFactory;
    }
	
}
