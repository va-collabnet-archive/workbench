package org.dwfa.mojo.relformat.mojo.db;

public interface DerbyFileRunner {

    void connect(String url);

    void run(String sql);

    void disconnect();
}
