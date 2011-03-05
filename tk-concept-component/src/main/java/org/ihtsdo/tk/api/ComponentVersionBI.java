package org.ihtsdo.tk.api;

import java.io.IOException;


public interface ComponentVersionBI extends ComponentBI {

    int getStatusNid();
    int getAuthorNid();
    int getPathNid();
    long getTime();
    int getSapNid();
    PositionBI getPosition() throws IOException;
}
