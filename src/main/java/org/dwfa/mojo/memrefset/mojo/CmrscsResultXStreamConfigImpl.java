package org.dwfa.mojo.memrefset.mojo;

import com.thoughtworks.xstream.XStream;

public final class CmrscsResultXStreamConfigImpl implements CmrscsResultXStreamConfig {

    public XStream configure() {
        XStream xStream = new XStream();
        xStream.alias("cmrscsResult", CmrscsResultImpl.class);
        xStream.alias("changeSet", ChangeSet.class);
        xStream.alias("refSet", RefSet.class);
        return xStream;        
    }
}
