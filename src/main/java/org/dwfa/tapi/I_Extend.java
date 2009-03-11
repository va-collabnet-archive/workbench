package org.dwfa.tapi;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

public interface I_Extend extends I_Manifest {
	public PropertyDescriptor[] getDataDescriptors() throws IntrospectionException;
}
