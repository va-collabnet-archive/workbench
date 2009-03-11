package org.dwfa.maven;

import java.io.File;


public class MvnUtil {
	
	private static String localRepository;
	
    public static String path(String groupId, String artifactId, String version) {
        StringBuffer buff = new StringBuffer();
        buff.append(localRepository);
        buff.append(File.separatorChar); 
        buff.append(groupId.replace('.', File.separatorChar));
        buff.append(File.separatorChar); 
        buff.append(artifactId);
        buff.append(File.separatorChar); 
        buff.append(version);
        buff.append(File.separatorChar); 
        buff.append(artifactId);
        buff.append("-"); 
        buff.append(version);
        buff.append(".jar"); 
        return buff.toString();
    }

	public static String getLocalRepository() {
		return localRepository;
	}

	public static void setLocalRepository(String localRepository) {
		MvnUtil.localRepository = localRepository;
	}

}
