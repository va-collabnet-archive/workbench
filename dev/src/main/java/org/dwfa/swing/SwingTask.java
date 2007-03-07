package org.dwfa.swing;

import java.awt.EventQueue;

public abstract class SwingTask extends java.util.TimerTask {
    public abstract void doRun();
    public void run() {
	if (!EventQueue.isDispatchThread()) {
	    EventQueue.invokeLater(this);
	} else {
	    doRun();
	}
    }
}
