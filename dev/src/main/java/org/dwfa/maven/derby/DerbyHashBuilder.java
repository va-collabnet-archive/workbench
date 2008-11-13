package org.dwfa.maven.derby;

import org.apache.maven.plugin.logging.Log;
import org.dwfa.maven.Sha1HashCodeGenerator;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public final class DerbyHashBuilder {

    private Log logger;

    private File outputDirectory;

    private File sourceDirectory;

    private String dbName;

    private String version;

    private String[] sources;

    private List sourceRoots;

    private String[] sqlLocations;

    public DerbyHashBuilder(final Log logger) {
        this.logger = logger;
    }

    public DerbyHashBuilder withOutputDirectory(final File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public DerbyHashBuilder withSourceDirectory(final File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        return this;
    }

    public DerbyHashBuilder withDatabaseName(final String dbName) {
        this.dbName = dbName;
        return this;
    }

    public DerbyHashBuilder withVersion(final String version) {
        this.version = version;
        return this;
    }

    public DerbyHashBuilder withSources(final String[] sources) {
        this.sources = sources;
        return this;
    }

    public DerbyHashBuilder withSourceRoots(final List sourceRoots) {
        this.sourceRoots = sourceRoots;
        return this;
    }

    public DerbyHashBuilder withSQLLocations(final String[] sqlLocations) {
        this.sqlLocations = sqlLocations;
        return this;
    }

    public String build() {
        Sha1HashCodeGenerator generator;
        String hashCode = "";
        try {
            generator = new Sha1HashCodeGenerator();
            generator.add(outputDirectory);
            generator.add(sourceDirectory);
            generator.add(version);
            generator.add(dbName);

            for (String source : sources) {
                generator.add(source);
            }

            for (Object sourceRoot : sourceRoots) {
                generator.add(sourceRoot);
            }

            for (String sqlLocation : sqlLocations) {
                generator.add(sqlLocation);
            }

            hashCode = generator.getHashCode();
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
        }

        return hashCode;
    }
}
