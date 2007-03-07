package org.dwfa.maven;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class GenericInvocationHandler implements InvocationHandler {
	Object obj;

	Class objClass;

	public GenericInvocationHandler(Object obj) {
		super();
		this.obj = obj;
		this.objClass = obj.getClass();
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		for (Method m : objClass.getMethods()) {
			if (m.getName().equals(method.getName())) {
				Class returnType = method.getReturnType();
				Object returnValue = m.invoke(obj, args);
				Class componentType = returnType.getComponentType();
				if (returnType.isArray()) {
					Object[] returnValues = (Object[]) returnValue;
					Object[] returnArray = (Object[]) Array.newInstance(
							componentType, returnValues.length);
					for (int i = 0; i < returnValues.length; i++) {
						returnArray[i] = Proxy.newProxyInstance(getClass()
								.getClassLoader(),
								new Class[] { componentType },
								new GenericInvocationHandler(returnValues[i]));
					}
					return returnArray;
				}
				if (returnValue != null) {
					if (returnType.isPrimitive()) {
						return returnValue;
					} else if (String.class.isAssignableFrom(returnValue
							.getClass())) {
						return returnValue;
					} else if (returnType.isEnum()) {
				           Method valueOfMethod = returnType.getMethod("valueOf", new Class[] { String.class});
				           return valueOfMethod.invoke(null, new Object[] { returnValue.toString() });
					}

					return Proxy.newProxyInstance(getClass().getClassLoader(),
							new Class[] { returnType },
							new GenericInvocationHandler(returnValue));

				}
			}
		}
		System.out.println("Method not found: " + method);
		return null;
	}

}
