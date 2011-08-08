package org.ihtsdo.arena.editor;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.arena.editor.EditorActions.HistoryAction;
import org.ihtsdo.arena.editor.EditorActions.NewAction;
import org.ihtsdo.arena.editor.EditorActions.OpenAction;
import org.ihtsdo.arena.editor.EditorActions.PrintAction;
import org.ihtsdo.arena.editor.EditorActions.SaveAction;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraphView;

public class ArenaEditorToolBar extends JToolBar
{

    /**
     * 
     */
    private static final long serialVersionUID = -3979320704834605323L;
    /**
     * 
     * @param frame
     * @param orientation
     */
    private boolean ignoreZoomChange = false;

    /**
     * 
     */
    public ArenaEditorToolBar(final BasicGraphEditor editor, int orientation)
    {
        super(orientation);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(3, 3, 3, 3), getBorder()));
        setFloatable(false);

        add(editor.bind("New", new NewAction(),
                "/com/mxgraph/examples/swing/images/new.gif"));
        add(editor.bind("Open", new OpenAction(),
                "/com/mxgraph/examples/swing/images/open.gif"));
        add(editor.bind("Save", new SaveAction(false),
                "/com/mxgraph/examples/swing/images/save.gif"));

        addSeparator();

        add(editor.bind("Print", new PrintAction(),
                "/com/mxgraph/examples/swing/images/print.gif"));

        addSeparator();

        add(editor.bind("Cut", TransferHandler.getCutAction(),
                "/com/mxgraph/examples/swing/images/cut.gif"));
        add(editor.bind("Copy", TransferHandler.getCopyAction(),
                "/com/mxgraph/examples/swing/images/copy.gif"));
        add(editor.bind("Paste", TransferHandler.getPasteAction(),
                "/com/mxgraph/examples/swing/images/paste.gif"));

        addSeparator();

        add(editor.bind("Delete", mxGraphActions.getDeleteAction(),
                "/com/mxgraph/examples/swing/images/delete.gif"));

        addSeparator();

        add(editor.bind("Undo", new HistoryAction(true),
                "/com/mxgraph/examples/swing/images/undo.gif"));
        add(editor.bind("Redo", new HistoryAction(false),
                "/com/mxgraph/examples/swing/images/redo.gif"));

        addSeparator();

        final mxGraphView view = editor.getGraphComponent().getGraph()
                .getView();
        final JComboBox zoomCombo = new JComboBox(new Object[] { "400%",
                "200%", "150%", "100%", "75%", "50%", mxResources.get("page"),
                mxResources.get("width"), mxResources.get("actualSize") });
        zoomCombo.setEditable(true);
        zoomCombo.setMinimumSize(new Dimension(75, 0));
        zoomCombo.setPreferredSize(new Dimension(75, 0));
        zoomCombo.setMaximumSize(new Dimension(75, 100));
        zoomCombo.setMaximumRowCount(9);
        add(zoomCombo);

        addSeparator();
        
        final JToggleButton autoApproval = new JToggleButton("Auto Approval");
        autoApproval.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				boolean aa;
				
				if (autoApproval.isSelected())
					aa = true;
				else
					aa = false;
				
				try {
					Terms.get().getActiveAceFrameConfig().setAutoApprove(aa);
				} catch (Exception e1) {
		        	AceLog.getAppLog().log(Level.WARNING, "Error in setting Auto Approval", e1);
				}
				
			}
        
    	});
        
        
        
		try {

	        AceFrameConfig config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
	        AceFrame ace = config.getAceFrame();
	        JTabbedPane tp = ace.getCdePanel().getConceptTabs();
	        int index = tp.getSelectedIndex();
			
			if (isAutoApprovalAvailable(index, config.getViewCoordinate()))
		        add(autoApproval);
			
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in setting Auto Approval", e);
		}

		
        // Sets the zoom in the zoom combo the current value
        mxIEventListener scaleTracker = new mxIEventListener()
        {
            /**
             * 
             */
            public void invoke(Object sender, mxEventObject evt)
            {
                ignoreZoomChange = true;

                try
                {
                    zoomCombo.setSelectedItem((int) Math.round(100 * view
                            .getScale())
                            + "%");
                }
                finally
                {
                    ignoreZoomChange = false;
                }
            }
        };

        // Installs the scale tracker to update the value in the combo box
        // if the zoom is changed from outside the combo box
        view.getGraph().getView().addListener(mxEvent.SCALE,
                scaleTracker);
        view.getGraph().getView().addListener(
                mxEvent.SCALE_AND_TRANSLATE, scaleTracker);

        // Invokes once to sync with the actual zoom value
        scaleTracker.invoke(null, null);

        zoomCombo.addActionListener(new ActionListener()
        {
            /**
             * 
             */
            public void actionPerformed(ActionEvent e)
            {
                mxGraphComponent graphComponent = editor.getGraphComponent();

                // Zoomcombo is changed when the scale is changed in the diagram
                // but the change is ignored here
                if (!ignoreZoomChange)
                {
                    String zoom = zoomCombo.getSelectedItem().toString();

                    if (zoom.equals(mxResources.get("page")))
                    {
                        graphComponent.setPageVisible(true);
                        graphComponent
                                .setZoomPolicy(mxGraphComponent.ZOOM_POLICY_PAGE);
                    }
                    else if (zoom.equals(mxResources.get("width")))
                    {
                        graphComponent.setPageVisible(true);
                        graphComponent
                                .setZoomPolicy(mxGraphComponent.ZOOM_POLICY_WIDTH);
                    }
                    else if (zoom.equals(mxResources.get("actualSize")))
                    {
                        graphComponent.zoomActual();
                    }
                    else
                    {
                        try
                        {
                            zoom = zoom.replace("%", "");
                            graphComponent.zoomTo(
                                    Double.parseDouble(zoom) / 100,
                                    graphComponent.isCenterZoom());
                        }
                        catch (Exception ex)
                        {
                            JOptionPane.showMessageDialog(editor, ex
                                    .getMessage());
                        }
                    }
                }
            }
        });
    }

	private boolean isAutoApprovalAvailable(int index, ViewCoordinate viewCoord) throws Exception {
		EditorCategoryRefsetSearcher categegorySearcher = new EditorCategoryRefsetSearcher();
		
		return categegorySearcher.isAutomaticApprovalAvailable(WorkflowHelper.getCurrentModeler(), viewCoord);
	}
}
