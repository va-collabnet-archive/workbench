package org.dwfa.mojo;

import org.apache.maven.plugin.logging.Log;

import java.io.File;

/**
 * Wraps <code>MojoUtil</code> so it can be mocked out.
 */
public interface MojoUtilWrapper {

    boolean alreadyRun(Log logger,  String absolutePath, Class<?> clazz, File outputDirectory) throws Exception;
}
