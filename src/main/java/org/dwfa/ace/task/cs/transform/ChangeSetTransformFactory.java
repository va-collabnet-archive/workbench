package org.dwfa.ace.task.cs.transform;

import java.io.File;
import java.util.HashMap;

/**
 * Creates a change set transformer known to handle a particular type of file. 
 * 
 * The strategy is to match by the file name suffix, each known transformer class defines the
 * file suffix it can handle as an input and then by setting the output suffix in the invoked transformer
 * the type of output may be defined. 
 */
public class ChangeSetTransformFactory {

	private static HashMap<String, Class<? extends ChangeSetTransformer>> registeredTransforms;
	
	static {
		registeredTransforms = new HashMap<String, Class<? extends ChangeSetTransformer>>();
		registerTransform(ChangeSetXmlDecoder.class);
		registerTransform(ChangeSetXmlEncoder.class);
		registerTransform(CmrscsXmlEncoder.class);
	}

	private static void registerTransform(Class<? extends ChangeSetTransformer> transformClass) {
		InputSuffix inputSuffix = transformClass.getAnnotation(InputSuffix.class);
		if (inputSuffix != null) {
			registeredTransforms.put(inputSuffix.value(), transformClass);
		}
	}
	
	public static ChangeSetTransformer getTransformForFile(File file) throws InstantiationException, IllegalAccessException {
		for (String suffix : registeredTransforms.keySet()) {
			if (file != null && file.getName().endsWith(suffix)) {
				return registeredTransforms.get(suffix).newInstance();
			}
		}
		throw new InstantiationException("No change set transformer registered to handle file " + file.getName());
	}
	
}
