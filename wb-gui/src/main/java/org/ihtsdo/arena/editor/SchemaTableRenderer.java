package org.ihtsdo.arena.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.EnumMap;
import org.ihtsdo.arena.editor.ArenaEditor.CELL_ATTRIBUTES;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellHandler;
import com.mxgraph.swing.handler.mxGraphTransferHandler;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.view.mxGraph;

/**
 * @author Administrator
 * 
 */
public class SchemaTableRenderer extends JComponent
{

    /**
     * 
     */
    private static final long serialVersionUID = 2106746763664760745L;

    /**
     * 
     */
    public static final String IMAGE_PATH = "/com/mxgraph/examples/swing/images/";

    /**
     * 
     */
    protected static SchemaTableRenderer dragSource = null;

    /**
     * 
     */
    protected static int sourceRow = 0;

    /**
     * 
     */
    protected mxCell cell;

    /**
     * 
     */
    protected mxGraphComponent graphContainer;

    /**
     * 
     */
    protected mxGraph graph;

    /**
     * 
     */
    public JTable table;

	private JLabel resizeLabel;
	
	private EnumMap dataMap;

	private I_HostConceptPlugins host;
	private I_GetConceptData concept;

	private JLabel label;
	
    /**
     * 
     */
    @SuppressWarnings("serial")
    public SchemaTableRenderer(Object cellObj,
            final mxGraphComponent graphContainer)
    {
        this.cell = (mxCell) cellObj;
        this.graphContainer = graphContainer;
        this.graph = graphContainer.getGraph();
        this.dataMap = (EnumMap) this.cell.getValue();
        if (this.dataMap != null && dataMap.get(CELL_ATTRIBUTES.LINKED_TAB) != null) {
        	Integer linkedTab = (Integer) dataMap.get(CELL_ATTRIBUTES.LINKED_TAB);
            try {
            	host = Terms.get().getActiveAceFrameConfig().getConceptViewer(linkedTab);
            	concept = (I_GetConceptData) host.getTermComponent();
            	host.addPropertyChangeListener("termComponent", new HostListener());
			} catch (TerminologyException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			} catch (IOException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}
        }
        setLayout(new BorderLayout());

        JPanel title = new JPanel();
        title.setBackground(new Color(149, 173, 239));
        title.setOpaque(true);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));
        title.setLayout(new BorderLayout());

        JButton goToLinkButton = new JButton(new AbstractAction("", new ImageIcon(SchemaTableRenderer.class
                .getResource("/16x16/plain/pin_green.png"))) {

					@Override
					public void actionPerformed(ActionEvent e) {
						Integer linkedTab = -1;
						if (dataMap != null ) {
							linkedTab = (Integer) dataMap.get(CELL_ATTRIBUTES.LINKED_TAB);
						}
						if (linkedTab != null && linkedTab != -1) {
							try {
								Terms.get().getActiveAceFrameConfig().selectConceptViewer(linkedTab);
							} catch (Exception e1) {
								AceLog.getAppLog().alertAndLogException(e1);
							} 
						}
					}
        	
        });
        goToLinkButton.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 1));
        title.add(goToLinkButton, BorderLayout.WEST);

        label = new JLabel(String.valueOf(graph.getLabel(cell)));
        label.setForeground(Color.WHITE);
        label.setFont(title.getFont().deriveFont(Font.BOLD, 11));
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        title.add(label, BorderLayout.CENTER);

        JPanel toolBar2 = new JPanel();
        toolBar2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
        toolBar2.setOpaque(false);
        JButton button = new JButton(new AbstractAction("", new ImageIcon(
                SchemaTableRenderer.class.getResource(IMAGE_PATH + "minimize.gif")))
        {

        	boolean collapsed = false;
        	
            public void actionPerformed(ActionEvent e)
            {
            	resizeLabel.setVisible(collapsed);
            	collapsed = !collapsed;
            	
                graph.foldCells(collapsed, false, new Object[] { cell });
                ((JButton) e.getSource())
                        .setIcon(new ImageIcon(
                                SchemaTableRenderer.class
                                        .getResource(IMAGE_PATH
                                                + ((graph.isCellCollapsed(cell)) ? "maximize.gif"
                                                        : "minimize.gif"))));
            }
        });
        button.setPreferredSize(new Dimension(16, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("Collapse/Expand");
        button.setOpaque(false);
        toolBar2.add(button);

        title.add(toolBar2, BorderLayout.EAST);
        add(title, BorderLayout.NORTH);

        // CellStyle style =
        // graph.getStylesheet().getCellStyle(graph.getModel(),
        // cell);
        // if (style.getStyleClass() == null) {
        table = new MyTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        if (graph.getModel().getChildCount(cell) == 0)
        {
            scrollPane.getViewport().setBackground(Color.WHITE);
            setOpaque(true);
            add(scrollPane, BorderLayout.CENTER);
        }

        scrollPane.getVerticalScrollBar().addAdjustmentListener(
                new AdjustmentListener() {

                    public void adjustmentValueChanged(AdjustmentEvent e) {
                        graphContainer.refresh();
                    }

                });

        resizeLabel = new JLabel(new ImageIcon(SchemaTableRenderer.class
                .getResource(IMAGE_PATH + "resize.gif")));
        resizeLabel.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(resizeLabel, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);

        ResizeHandler resizeHandler = new ResizeHandler();
        resizeLabel.addMouseListener(resizeHandler);
        resizeLabel.addMouseMotionListener(resizeHandler);

        setMinimumSize(new Dimension(40, 20));
        updateLabel();
    }
    
    private void updateLabel() {
    	if (concept == null) {
    		label.setText("null");
    	} else {
    		label.setText(concept.toString());
    	}
    }

    /**
     * Implements an event redirector for the specified handle index, where 0
     * is the top right, and 1-7 are the top center, rop right, middle left,
     * middle right, bottom left, bottom center and bottom right, respectively.
     * Default index is 7 (bottom right).
     */
    public class ResizeHandler implements MouseListener, MouseMotionListener
    {

        protected int index;

        public ResizeHandler()
        {
            this(7);
        }

        public ResizeHandler(int index)
        {
            this.index = index;
        }

        public void mouseClicked(MouseEvent e)
        {
            // ignore
        }

        public void mouseEntered(MouseEvent e)
        {
            // ignore
        }

        public void mouseExited(MouseEvent e)
        {
            // ignore
        }

        public void mousePressed(MouseEvent e)
        {
            // Selects to create a handler for resizing
            if (!graph.isCellSelected(cell))
            {
                graphContainer.selectCellForEvent(cell, e);
            }

            // Initiates a resize event in the handler
            mxCellHandler handler = graphContainer.getSubHandler().getHandler(cell);

            if (handler != null)
            {
                // Starts the resize at index 7 (bottom right)
                handler.start(SwingUtilities.convertMouseEvent((Component) e
                        .getSource(), e, graphContainer.getGraphControl()),
                        index);
                e.consume();
            }
        }

        public void mouseReleased(MouseEvent e)
        {
            graphContainer.getGraphControl().dispatchEvent(
                    SwingUtilities.convertMouseEvent((Component) e.getSource(),
                            e, graphContainer.getGraphControl()));
        }

        public void mouseDragged(MouseEvent e)
        {
            graphContainer.getGraphControl().dispatchEvent(
                    SwingUtilities.convertMouseEvent((Component) e.getSource(),
                            e, graphContainer.getGraphControl()));
        }

        public void mouseMoved(MouseEvent e)
        {
            // ignore
        }
    }

    /**
     * 
     */
    public class MyTable extends JTable implements DropTargetListener
    {

        /**
         * 
         */
        private static final long serialVersionUID = 5841175227984561071L;

        Object[][] data;

        String[] colNames = new String[] { "A", "B", "C", "D", "E" };

        @SuppressWarnings("serial")
        public MyTable()
        {
            data = new Object[30][5];
            for (int i = 0; i < 30; i++)
            {
                data[i][0] = new Boolean(false);
                data[i][1] = "Column " + i;
                data[i][2] = (Math.random() > 0.5) ? new ImageIcon(
                        SchemaTableRenderer.class.getResource(IMAGE_PATH
                                + "preferences.gif")) : null;
                data[i][3] = (Math.random() > 0.5) ? new ImageIcon(
                        SchemaTableRenderer.class.getResource(IMAGE_PATH
                                + "preferences.gif")) : null;
                data[i][4] = (Math.random() > 0.5) ? new ImageIcon(
                        SchemaTableRenderer.class.getResource(IMAGE_PATH
                                + "preferences.gif")) : null;
            }
            setModel(createModel());
            setTableHeader(null);
            setAutoscrolls(true);
            setGridColor(Color.WHITE);
            TableColumn column = getColumnModel().getColumn(0);
            column.setMaxWidth(20);
            column = getColumnModel().getColumn(2);
            column.setMaxWidth(12);
            column = getColumnModel().getColumn(3);
            column.setMaxWidth(12);
            column = getColumnModel().getColumn(4);
            column.setMaxWidth(12);

            setTransferHandler(new TransferHandler()
            {

                /* (non-Javadoc)
                 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
                 */
                @Override
                public int getSourceActions(JComponent c)
                {
                    return COPY_OR_MOVE;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
                 */
                protected Transferable createTransferable(JComponent c)
                {
                    sourceRow = getSelectedRow();
                    dragSource = SchemaTableRenderer.this;
                    //mxRectangle bounds = new mxRectangle(0, 0, MyTable.this
                    //      .getWidth(), 20);
                    return new mxGraphTransferable(null, null, null);
                }

            });

            setDragEnabled(true);
            setDropTarget(new DropTarget(this, // component
                    DnDConstants.ACTION_COPY_OR_MOVE, // actions
                    this));
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        /**
         * 
         */
        public DropTarget getDropTarget()
        {
            if (!((mxGraphTransferHandler) graphContainer.getTransferHandler())
                    .isLocalDrag())
            {
                return super.getDropTarget();
            }

            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
         */
        public void dragEnter(DropTargetDragEvent e)
        {

        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
         */
        public void dragOver(DropTargetDragEvent e)
        {
            if (!((mxGraphTransferHandler) graphContainer.getTransferHandler())
                    .isLocalDrag()
                    && SchemaTableRenderer.this != dragSource)
            {
                Point p = e.getLocation();
                int row = rowAtPoint(p);
                getSelectionModel().setSelectionInterval(row, row);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
         */
        public void dropActionChanged(DropTargetDragEvent dtde)
        {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
         */
        public void drop(DropTargetDropEvent e)
        {
            if (dragSource != null)
            {
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                Point p = e.getLocation();
                int targetRow = rowAtPoint(p);

                Object edge = graph.insertEdge(null, null, null,
                        dragSource.cell, SchemaTableRenderer.this.cell, "sourceRow="
                                + sourceRow + ";targetRow=" + targetRow);
                graph.setSelectionCell(edge);

                // System.out.println("clearing drag source");
                dragSource = null;
                e.dropComplete(true);
            }
            else
            {
                e.rejectDrop();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
         */
        public void dragExit(DropTargetEvent dte)
        {
            // TODO Auto-generated method stub

        }

        /**
         * 
         * @return the created table model
         */
        public TableModel createModel()
        {
            return new AbstractTableModel()
            {

                /**
                 * 
                 */
                private static final long serialVersionUID = -3642207266816170738L;

                public int getColumnCount()
                {
                    return colNames.length;
                }

                public int getRowCount()
                {
                    return data.length;
                }

                public String getColumnName(int col)
                {
                    return colNames[col];
                }

                public Object getValueAt(int row, int col)
                {
                    return data[row][col];
                }

                @SuppressWarnings("unchecked")
                public Class getColumnClass(int c)
                {
                    Object value = getValueAt(0, c);
                    return (value != null) ? value.getClass() : ImageIcon.class;
                }

                /*
                 * Don't need to implement this method unless your table's
                 * editable.
                 */
                public boolean isCellEditable(int row, int col)
                {
                    return col == 0;
                }

                /*
                 * Don't need to implement this method unless your table's data
                 * can change.
                 */
                public void setValueAt(Object value, int row, int col)
                {
                    data[row][col] = value;
                    fireTableCellUpdated(row, col);
                }
            };

        }

    }

    /**
     * 
     */
    public static SchemaTableRenderer getVertex(Component component)
    {
        while (component != null)
        {
            if (component instanceof SchemaTableRenderer)
            {
                return (SchemaTableRenderer) component;
            }
            component = component.getParent();
        }

        return null;
    }

    private class HostListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			concept = (I_GetConceptData) host.getTermComponent();
			updateLabel();
		}
    	
    }
 }
