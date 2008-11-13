package org.dwfa.mojo.relformat.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("ReleaseConfig")
public class ReleaseConfig {

    @XStreamImplicit
    private final List<ReleaseFormat> releaseFormats;

    public ReleaseConfig(final List<ReleaseFormat> releaseFormats) {
        this.releaseFormats = releaseFormats;
    }

    public List<ReleaseFormat> getReleaseFormats() {
        return releaseFormats;
    }
}
