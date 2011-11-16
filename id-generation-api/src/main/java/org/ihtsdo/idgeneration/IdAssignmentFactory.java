package org.ihtsdo.idgeneration;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class IdAssignmentFactory {

	private static IdAssignmentFactory instance = null;
	public static final String DERBY_DB_IMPL = "DerbyDatabaseImplementation";
	public static final String WEB_SERVICE_IMPL = "WebServiceImplementation";
	
	private IdAssignmentFactory(){}
	static{
		try {
			Class.forName(IdAssignmentDerbyImpl.class.getName());
			Class.forName(IdAssignmentImpl.class.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static IdAssignmentFactory getInstance(){
		if(instance == null){
			instance = new IdAssignmentFactory();
		}
		return instance;
	}
	
	private HashMap<String, Class<? extends IdAssignmentBI>> m_RegisteredProducts = new HashMap<String, Class<? extends IdAssignmentBI>>();

	public void registerProduct(String productID, Class<? extends IdAssignmentBI> productClass) {
		m_RegisteredProducts.put(productID, productClass);
	}

	public IdAssignmentBI createProduct(String productID) throws InstantiationException, IllegalAccessException {
		Class<? extends IdAssignmentBI> productClass = (Class<? extends IdAssignmentBI>) m_RegisteredProducts.get(productID);
		return (IdAssignmentBI) productClass.newInstance();
	}

	/**
	 * Only for Web Service Implementation
	 * 
	 * @param productID
	 * @param propertiesFile
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public IdAssignmentBI createProduct(String productID, File propertiesFile) throws Exception {
		Class<? extends IdAssignmentBI> productClass = (Class<? extends IdAssignmentBI>) m_RegisteredProducts.get(productID);
		Class<?>[] parameterTypes = new Class<?>[] {File.class};
		Constructor<? extends IdAssignmentBI> constructor = productClass.getDeclaredConstructor(parameterTypes );
		return (IdAssignmentBI) constructor.newInstance(propertiesFile);
	}

	/**
	 * Only for Web Service Implementation
	 * 
	 * @param productID
	 * @param targetEndpoint
	 *            : Web Service target endpoint
	 * @param user
	 *            : security username
	 * @param password
	 *            : security password
	 * @return Id Assignment implementation using the specified endpoint web
	 *         service
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public IdAssignmentBI createProduct(String productID, String targetEndpoint, String user, String password) throws Exception {
		Class<? extends IdAssignmentBI> productClass = (Class<? extends IdAssignmentBI>) m_RegisteredProducts.get(productID);
		Class<?>[] parameterTypes = new Class<?>[] {String.class,String.class,String.class};
		Constructor<? extends IdAssignmentBI> constructor = productClass.getDeclaredConstructor(parameterTypes );
		return (IdAssignmentBI) constructor.newInstance(targetEndpoint, user, password);
	}

}
