package org.ihtsdo.tk.api.description;

import java.beans.PropertyVetoException;

import org.ihtsdo.tk.api.AnalogBI;

public interface DescriptionAnalogBI extends AnalogBI, DescriptionVersionBI {

    public void setInitialCaseSignificant(boolean capStatus) throws PropertyVetoException;
    public void setLang(String lang) throws PropertyVetoException;
    public void setText(String text) throws PropertyVetoException;

}
