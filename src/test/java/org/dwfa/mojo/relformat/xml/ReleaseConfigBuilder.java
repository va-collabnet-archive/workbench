package org.dwfa.mojo.relformat.xml;

public class ReleaseConfigBuilder {

    private static final String START_CONFIG = "<ReleaseConfig>";
    private static final String END_CONFIG = "</ReleaseConfig>";

    private static final String START_RELEASE_FORMAT = "<ReleaseFormat>";
    private static final String END_RELEASE_FORMAT = "</ReleaseFormat>";

    private static final String START_NAME = "<Type>";
    private static final String END_NAME = "</Type>";

    private static final String START_SCHEMA = "<Schema>";
    private static final String END_SCHEMA = "</Schema>";

    private final StringBuilder builder;

    public ReleaseConfigBuilder() {
        builder = new StringBuilder();       
        builder.append(START_CONFIG);
    }

    public ReleaseConfigBuilder createReleaseFormat() {
        builder.append(START_RELEASE_FORMAT);
        return this;
    }

    public ReleaseConfigBuilder addName(final String name) {
        builder.append(START_NAME).append(name).append(END_NAME);
        return this;
    }

    public ReleaseConfigBuilder addSchema(final String schema) {
        builder.append(START_SCHEMA).append(schema).append(END_SCHEMA);
        return this;
    }

    public ReleaseConfigBuilder addReleaseFormat() {
        builder.append(END_RELEASE_FORMAT);
        return this;
    }

    public String build() {
        return builder.append(END_CONFIG).toString();
    }
}
