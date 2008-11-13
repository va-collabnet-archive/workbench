package org.dwfa.maven.derby;

public interface DerbyClient {

    void openConnection();
    void executeScript(String fileName);
    void executeScript(String fileName, boolean verbose);
    void closeConnection();

}
