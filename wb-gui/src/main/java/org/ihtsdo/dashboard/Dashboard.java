package org.ihtsdo.dashboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

public class Dashboard extends JPanel implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Dashboard() {
        super(new BorderLayout());
        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        addButtons(toolBar);
         add(toolBar, BorderLayout.EAST);
         JPanel arena = new JPanel();
         arena.setBackground(Color.GRAY);
         arena.setBorder(BorderFactory.createLoweredBevelBorder());
        add(arena, BorderLayout.CENTER);    
    }
    
    protected void addButtons(JToolBar toolBar) {
        toolBar.add(makeButton("potion_green", "ONE",
            "potion_green",
        "potion_green"));
        toolBar.add(makeButton("pill_red", "TWO",
            "pill_red",
        "pill_red"));
        toolBar.add(makeButton("text_formula", "THREE",
            "text_formula",
        "text_formula"));
        toolBar.add(makeButton("view", "FOUR",
            "view",
        "view"));
        toolBar.add(makeButton("graph_edge_directed", "FIVE",
            "graph_edge_directed",
        "graph_edge_directed"));
        toolBar.add(makeButton("flag_yellow", "SIX",
            "flag_yellow",
        "flag_yellow"));
        toolBar.add(makeButton("elements_selection", "SEVEN",
            "elements_selection",
        "elements_selection"));
    }

    protected JButton makeButton(String imageName,
                                           String actionCommand,
                                           String toolTipText,
                                           String altText) {
        //Look for the image.
        String imgLocation = "/24x24/plain/"
                             + imageName
                             + ".png";
        URL imageURL = Dashboard.class.getResource(imgLocation);

        //Create and initialize the button.
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);

        if (imageURL != null) {                      //image found
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {                                     //no image found
            button.setText(altText);
            System.err.println("Resource not found: " + imgLocation);
        }

        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }    
}
