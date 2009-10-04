package org.dwfa.ace.api;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public interface I_ShowActivity {
    public JButton getStopButton();

    public void setStopButton(JButton stopButton);

    public JPanel getViewPanel();

    public void setProgressInfoUpper(String text);

    public void setProgressInfoLower(String text);

    public String getProgressInfoUpper();

    public String getProgressInfoLower();

    public void setIndeterminate(boolean newValue);

    public boolean isIndeterminate();

    public void setMaximum(int n);

    public int getMaximum();

    public void setValue(int n);

    public int getValue();

    public void addActionListener(ActionListener l);

    public void removeActionListener(ActionListener l);

    public void complete();

    public boolean isComplete();

    public long getStartTime();

    public void setStartTime(long time);

    public I_ShowActivity getSecondaryPanel();

    public void setSecondaryPanel(I_ShowActivity panel);

    public void addShowActivityListener(I_ShowActivity listener);

    public void removeShowActivityListener(I_ShowActivity listener);

    public boolean isStringPainted();
    public void setStringPainted(boolean stringPainted); // displays a string on
                                                         // the progress bar
    public I_ConfigAceFrame getAceFrameConfig();
    
    public void syncWith(I_ShowActivity another);
}
