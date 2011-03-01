package org.ihtsdo.tk.api;


public interface ComponentVersionBI extends ComponentBI {

    int getStatusNid();
    int getAuthorNid();
    int getPathNid();
    long getTime();
    int getSapNid();
}
