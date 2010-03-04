package org.dwfa.ace.task.util;

import java.io.IOException;

/**
 *
 * @author Matthew Edwards
 */
public interface SerializerDeserializer<T> {

    T deserialize() throws IOException, ClassNotFoundException;

    void serialize() throws IOException;
}
