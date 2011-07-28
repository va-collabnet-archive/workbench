package org.ihtsdo.qadb.helper;

import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

public class MyBatisUtil {

	private static final Logger logger = Logger.getLogger(MyBatisUtil.class);
	private static final SqlSessionFactory sessionFactory;

	static {
		Reader reader = null;
		try {
			logger.debug("Creating Session factory ");
			// Create the SessionFactory
			String resource = "org/ihtsdo/qadb/data/MyIbatisConfig.xml";
			reader = Resources.getResourceAsReader(resource);
			SqlSession session = null; 
			sessionFactory = new SqlSessionFactoryBuilder().build(reader);
			Exception etoThrow = null;
			try {
				session = sessionFactory.openSession();
			} catch (Exception e) {
				//Try again.
			}
			for (int i = 1; i < 4; i++) {
				if (session == null) {
					try{
					session = sessionFactory.openSession();
					}catch (Exception e) {
						logger.debug("try: " + i + " -> Could not open session");
						etoThrow = e;
					}
				} else {
					break;
				}
			}
			if (session != null) {
				session.close();
			}else{
				sessionFactory.openSession();
			}

		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SqlSessionFactory getSessionFactory() {
		logger.debug("Getting session Factory ");
		return sessionFactory;
	}

}
