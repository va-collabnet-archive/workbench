package org.dwfa.mojo;

import org.apache.maven.plugin.logging.Log;
import org.dwfa.maven.MojoUtil;

import java.io.File;

/**
 * Default implementation of <code>MojoUtilWrapper</code> which degelates to @{link org.dwfa.maven.MojoUtil}.
 */
public final class MojoUtilWrapperImpl implements MojoUtilWrapper {

    public boolean alreadyRun(final Log logger, final String absolutePath, final Class<?> clazz,
                              final File outputDirectory) throws Exception {
        return MojoUtil.alreadyRun(logger, absolutePath, clazz, outputDirectory);
    }
}
