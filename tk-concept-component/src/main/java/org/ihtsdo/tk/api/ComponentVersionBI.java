package org.ihtsdo.tk.api;


public interface ComponentVersionBI extends ComponentBI {

    public int getStatusNid();
    public int getAuthorNid();
    public int getPathNid();
    public long getTime();

}
