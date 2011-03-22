package org.ihtsdo.arena.conceptview;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

public class FixedWidthJEditorPane extends JEditorPane {

    public void resetSize() {
        View v = BasicHTML.createHTMLView(this, getText());
        v.setSize(fixedWidth, 0);
        float prefYSpan = v.getPreferredSpan(View.Y_AXIS);
        if (prefYSpan > 16) {
            wrapSize = new Dimension(fixedWidth, (int) (prefYSpan + 4));
            setSize(wrapSize);
        } else {
            wrapSize = new Dimension(fixedWidth, (int) prefYSpan);
            setSize(wrapSize);
        }
    }
    
    private class DCL implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent de) {
            resetSize();
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            resetSize();
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            resetSize();
        }  
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    int fixedWidth = 150;
    Dimension wrapSize = new Dimension();

    public String getHtmlPrefix() {
        StringBuilder buff = new StringBuilder();
        buff.append("<html><head><style type=\"text/css\">BODY {font:");
        buff.append(getFont().getSize());
        buff.append("pt ");
        buff.append(getFont().getFamily());
        buff.append("}</style></head><body>");

        return buff.toString();
    }

    public String getHtmlSuffix() {
        return "</body>";
    }
    boolean isHtmlStr = true;

    public FixedWidthJEditorPane() {
        super("text/html", "<html>");
        getDocument().addDocumentListener(new DCL());
    }
    

    @Override
    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(fixedWidth, height);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, wrapSize.width, wrapSize.height);
    }

    @Override
    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }

    @Override
    public Dimension getMaximumSize() {
        return wrapSize;
    }

    @Override
    public Dimension getMinimumSize() {
        return wrapSize;
    }

    @Override
    public Dimension getPreferredSize() {
        return wrapSize;
    }

    @Override
    public void setText(String text) {
        if (!BasicHTML.isHTMLString(text)) {
            text = getHtmlPrefix() + text + getHtmlSuffix();
            isHtmlStr = false;
        }
        super.setText(text);
        resetSize();
    }

    public String extractText() {
        if (isHtmlStr) {
            return getText();
        }
        String text = getText();
        text = text.substring(text.indexOf("<body>") + "<body>".length(), text.length());
        text = text.substring(0, text.indexOf("</body>"));
        text = text.trim();
        return text;
    }
}
