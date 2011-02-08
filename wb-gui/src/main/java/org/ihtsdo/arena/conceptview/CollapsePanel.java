package org.ihtsdo.arena.conceptview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ihtsdo.arena.ArenaComponentSettings;

public class CollapsePanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    boolean collapsed = false;
    boolean refex = false;
    boolean suggestions = false;
    boolean alerts = false;
    boolean showExtras = false;
    EnumSet<ComponentVersionDragPanel.SubPanels> subpanelsToShow = EnumSet.noneOf(ComponentVersionDragPanel.SubPanels.class);
    Set<I_ToggleSubPanels> components = new HashSet<I_ToggleSubPanels>();

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public CollapsePanel(String labelStr, ArenaComponentSettings settings) {
        super();
        setBackground(Color.LIGHT_GRAY);
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
        setLayout(new BorderLayout());

        JPanel toolBar1 = new JPanel();
        toolBar1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
        toolBar1.setOpaque(false);
        toolBar1.add(getRefexButton());
        toolBar1.add(getSuggestionButton());
        toolBar1.add(getAlertsButton());
        toolBar1.add(getShowExtrasButton());
        add(toolBar1, BorderLayout.WEST);

        JLabel label = new JLabel(labelStr, JLabel.CENTER);
        label.setFont(getFont().deriveFont(settings.getFontSize()));
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        add(label, BorderLayout.CENTER);
        JPanel toolBar2 = new JPanel();
        toolBar2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
        toolBar2.setOpaque(false);
        toolBar2.add(getCollapseExpandButton());
        add(toolBar2, BorderLayout.EAST);
    }

    private BufferedImage getBlackAndWhite(Image disImage) {
        BufferedImage image = new BufferedImage(disImage.getWidth(this), disImage.getHeight(this), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graph = image.createGraphics();
        graph.drawImage(disImage, 0, 0, this);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        return op.filter(image, null);
    }

    private JButton getShowExtrasButton() {
        final ImageIcon showExtrasIcon = new ImageIcon(
                ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH
                + "minimize.gif"));
        final ImageIcon hideExtrasIcon = new ImageIcon(
                ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH
                + "maximize.gif"));

        JButton button = new JButton(new AbstractAction("", showExtrasIcon) {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                showExtras = !showExtras;
                for (I_ToggleSubPanels cvdp : components) {
                    cvdp.showSubPanels(subpanelsToShow);
                }
                ((JButton) e.getSource()).setIcon((showExtras ? hideExtrasIcon
                        : showExtrasIcon));
            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("Hide/Show extra info for all group members");
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
        return button;
    }

    private JButton getRefexButton() {
        final ImageIcon showRefexes = new ImageIcon(
                ConceptViewRenderer.class.getResource(
                "/16x16/plain/paperclip.png"));
        final ImageIcon hideRefexes =
                new ImageIcon(getBlackAndWhite(showRefexes.getImage()));

        JButton button = new JButton(new AbstractAction("", showRefexes) {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                refex = !refex;
                showSubpanels(refex, ComponentVersionDragPanel.SubPanels.REFEX);

                for (I_ToggleSubPanels jc : components) {
                    //jc.setVisible(!collapsed);
                }
                ((JButton) e.getSource()).setIcon((refex ? hideRefexes
                        : showRefexes));
            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("Hide/Show refexes");
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
        return button;
    }

    private void showSubpanels(boolean show,
            ComponentVersionDragPanel.SubPanels subpanel) {
        if (show) {
            subpanelsToShow.add(subpanel);
        } else {
            subpanelsToShow.remove(subpanel);
        }
        for (I_ToggleSubPanels cvdp : components) {
            cvdp.showSubPanels(subpanelsToShow);
        }
    }

    private JButton getSuggestionButton() {
        final ImageIcon showSuggestions = new ImageIcon(
                ConceptViewRenderer.class.getResource(
                "/16x16/plain/lightbulb_on.png"));
        final ImageIcon hideSuggestions =
                new ImageIcon(getBlackAndWhite(showSuggestions.getImage()));

        JButton button = new JButton(new AbstractAction("", showSuggestions) {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                suggestions = !suggestions;
                showSubpanels(suggestions, ComponentVersionDragPanel.SubPanels.TEMPLATE);

                ((JButton) e.getSource()).setIcon(suggestions ? hideSuggestions
                        : showSuggestions);
            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("Hide/Show suggestions");
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
        return button;
    }

    private JButton getAlertsButton() {
        final ImageIcon showAlerts = new ImageIcon(
                ConceptViewRenderer.class.getResource(
                "/16x16/plain/warning.png"));
        final ImageIcon hideAlerts =
                new ImageIcon(getBlackAndWhite(showAlerts.getImage()));

        JButton button = new JButton(new AbstractAction("", showAlerts) {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                alerts = !alerts;
                showSubpanels(alerts, ComponentVersionDragPanel.SubPanels.ALERT);
                ((JButton) e.getSource()).setIcon(alerts ? hideAlerts
                        : showAlerts);
            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("Hide/Show warnings & errors");
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
        return button;
    }

    private JButton getCollapseExpandButton() {
        JButton button = new JButton(new AbstractAction("", new ImageIcon(
                ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH + "minimize.gif"))) {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                collapsed = !collapsed;

                for (I_ToggleSubPanels jc : components) {
                    jc.setVisible(!collapsed);
                }
                ((JButton) e.getSource()).setIcon(new ImageIcon(
                        CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH
                        + (collapsed ? "maximize.gif"
                        : "minimize.gif"))));
            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("Collapse/Expand");
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
        return button;
    }

    public void addToggleComponent(I_ToggleSubPanels component) {
        components.add(component);
    }
}
