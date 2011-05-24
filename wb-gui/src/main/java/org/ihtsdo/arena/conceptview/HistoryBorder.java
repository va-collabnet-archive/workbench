/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import sun.swing.SwingUtilities2;

/**
 *
 * @author kec
 */
public class HistoryBorder extends AbstractBorder {

    protected String historyString;
    protected Border border;
    protected Font titleFont;
    protected Color titleColor;
    private Point textLoc = new Point();
    // Space between the border and the component's edge
    static protected final int EDGE_SPACING = 0;
    // Space between the border and text
    static protected final int TEXT_SPACING = 0;
    // Horizontal inset of text that is left or right justified
    static protected final int TEXT_INSET_H = 25;

    /**
     * Creates a TitledBorder instance with the specified border,
     * title, title-justification, title-position, title-font, and
     * title-color.
     *
     * @param border  the border
     * @param title  the title the border should display
     * @param titleJustification the justification for the title
     * @param titlePosition the position for the title
     * @param titleFont the font of the title
     * @param titleColor the color of the title
     */
    public HistoryBorder(Border border,
            String title,
            Font titleFont,
            Color titleColor) {
        this.historyString = title;
        this.border = border;
        this.titleFont = titleFont;
        this.titleColor = titleColor;
    }

    /**
     * Paints the border for the specified component with the
     * specified position and size.
     * @param c the component for which this border is being painted
     * @param g the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {



        Rectangle grooveRect = new Rectangle(x + EDGE_SPACING, y + EDGE_SPACING,
                width - (EDGE_SPACING * 2),
                height - (EDGE_SPACING * 2));
        Font font = g.getFont();
        Color color = g.getColor();

        g.setFont(titleFont);

        JComponent jc = (c instanceof JComponent) ? (JComponent) c : null;
        FontMetrics fm = SwingUtilities2.getFontMetrics(jc, g);
        int fontHeight = fm.getHeight();
        int descent = fm.getDescent();
        int ascent = fm.getAscent();
        int diff;
        int stringWidth = SwingUtilities2.stringWidth(jc, fm,
                historyString);
        Insets insets;

        if (border != null) {
            insets = border.getBorderInsets(c);
        } else {
            insets = new Insets(0, 0, 0, 0);
        }
        textLoc.y = grooveRect.y + insets.top + TEXT_INSET_H;

        textLoc.x = grooveRect.x + insets.left;


        // If title is positioned in middle of border AND its fontsize
        // is greater than the border's thickness, we'll need to paint
        // the border in sections to leave space for the component's background
        // to show through the title.
        //
        if (border != null) {

            border.paintBorder(c, g, grooveRect.x, grooveRect.y,
                    grooveRect.width, grooveRect.height);

        }

        g.setColor(titleColor);
        //SwingUtilities2.drawString(jc, g, historyString, textLoc.x, textLoc.y);

       //
       Graphics2D g2d = (Graphics2D) g;
       int xtrans = textLoc.x + width - (insets.right + ascent + TEXT_SPACING);
       int ytrans = textLoc.y;
       g2d.translate(xtrans, ytrans);
       g2d.rotate(Math.toRadians(90));
       g2d.drawString(historyString, 0, 0);
       g2d.rotate(-Math.toRadians(90));
       g2d.translate(-xtrans, -ytrans);

               //
        g.setFont(font);
        g.setColor(color);
    }

    /**
     * Returns the insets of the border.
     * @param c the component for which this border insets value applies
     */
    @Override
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(0, 0, 0, 0));
    }

    /**
     * Reinitialize the insets parameter with this Border's current Insets.
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     */
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        FontMetrics fm;
        int descent = 0;
        int ascent = 16;
        int height = 16;

        if (border != null) {
            if (border instanceof AbstractBorder) {
                ((AbstractBorder) border).getBorderInsets(c, insets);
            } else {
                // Can't reuse border insets because the Border interface
                // can't be enhanced.
                Insets i = border.getBorderInsets(c);
                insets.top = i.top;
                insets.right = i.right;
                insets.bottom = i.bottom;
                insets.left = i.left;
            }
        } else {
            insets.left = insets.top = insets.right = insets.bottom = 0;
        }

        insets.left += EDGE_SPACING + TEXT_SPACING;
        insets.right += EDGE_SPACING + TEXT_SPACING;
        insets.top += EDGE_SPACING + TEXT_SPACING;
        insets.bottom += EDGE_SPACING + TEXT_SPACING;

        if (c == null) {
            return insets;
        }

        fm = c.getFontMetrics(titleFont);

        if (fm != null) {
            descent = fm.getDescent();
            ascent = fm.getAscent();
            height = fm.getHeight();
        }

        insets.right += ascent + descent + TEXT_SPACING;
        return insets;
    }

    /**
     * Returns whether or not the border is opaque.
     */
    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    /**
     * Returns the minimum dimensions this border requires
     * in order to fully display the border and title.
     * @param c the component where this border will be drawn
     */
    public Dimension getMinimumSize(Component c) {
        Insets insets = getBorderInsets(c);
        Dimension minSize = new Dimension(insets.right + insets.left,
                insets.top + insets.bottom);
        FontMetrics fm = c.getFontMetrics(titleFont);
        JComponent jc = (c instanceof JComponent) ? (JComponent) c : null;
        minSize.height += SwingUtilities2.stringWidth(jc, fm, historyString);
        return minSize;
    }

    /**
     * Returns the baseline.
     *
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @see javax.swing.JComponent#getBaseline(int, int)
     * @since 1.6
     */
    @Override
    public int getBaseline(Component c, int width, int height) {
        if (c == null) {
            throw new NullPointerException("Must supply non-null component");
        }
        if (width < 0) {
            throw new IllegalArgumentException("Width must be >= 0");
        }
        if (height < 0) {
            throw new IllegalArgumentException("Height must be >= 0");
        }
        Border border2 = border;
        Insets borderInsets;
        if (border2 != null) {
            borderInsets = border2.getBorderInsets(c);
        } else {
            borderInsets = new Insets(0, 0, 0, 0);
        }
        FontMetrics fm = c.getFontMetrics(titleFont);
        int fontHeight = fm.getHeight();
        int descent = fm.getDescent();
        int ascent = fm.getAscent();
        int y = EDGE_SPACING;
        int h = height - EDGE_SPACING * 2;
        int diff;
        return y + borderInsets.top + ascent + TEXT_SPACING;
    }

    /**
     * Returns an enum indicating how the baseline of the border
     * changes as the size changes.
     *
     * @throws NullPointerException {@inheritDoc}
     * @see javax.swing.JComponent#getBaseline(int, int)
     * @since 1.6
     */
    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            Component c) {
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }
}
