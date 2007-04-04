package org.dwfa.ace.tree;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.KeyEvent;
import java.awt.image.FilteredImageSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.dnd.AceTransferAction;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.je.DatabaseException;

public class JTreeWithDragImage extends JTree {

	private class TermLabelDragSourceListener implements DragSourceListener {

		public void dragDropEnd(DragSourceDropEvent dsde) {
			// TODO Auto-generated method stub
		}

		public void dragEnter(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}

		public void dragExit(DragSourceEvent dse) {
			// TODO Auto-generated method stub
		}

		public void dragOver(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}
	}

	private class DragGestureListenerWithImage implements DragGestureListener {

		DragSourceListener dsl;

		public DragGestureListenerWithImage(DragSourceListener dsl) {
			super();
			this.dsl = dsl;
		}

		public void dragGestureRecognized(DragGestureEvent dge) {
			int selRow = getRowForLocation(dge.getDragOrigin().x, dge
					.getDragOrigin().y);
			TreePath path = getPathForLocation(dge.getDragOrigin().x, dge
					.getDragOrigin().y);
			if (selRow != -1) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				try {
					I_GetConceptData obj = (I_GetConceptData) node
							.getUserObject();
					Image dragImage = getDragImage(obj);
					Point imageOffset = new Point(-10, -(dragImage
							.getHeight(JTreeWithDragImage.this) + 1));
					dge.startDrag(DragSource.DefaultCopyDrop, dragImage,
							imageOffset, getTransferable(obj), dsl);
				} catch (InvalidDnDOperationException e) {
					AceLog.info(e.toString());
				} catch (Exception ex) {
					AceLog.alertAndLogException(ex);
				}
			}
		}

		private Transferable getTransferable(I_GetConceptData obj)
				throws DatabaseException {
			return new ConceptTransferable(ConceptBean.get(obj.getConceptId()));
		}

		public Image getDragImage(I_GetConceptData obj)
				throws IOException {

			I_DescriptionTuple desc = obj.getDescTuple(config
					.getTreeDescPreferenceList(), config);
			JLabel dragLabel = TermLabelMaker.newLabel(desc, false, false).getLabel();
			dragLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			Image dragImage = createImage(dragLabel.getWidth(), dragLabel
					.getHeight());
			dragLabel.setVisible(true);
			Graphics og = dragImage.getGraphics();
			og.setClip(dragLabel.getBounds());
			dragLabel.paint(og);
			og.dispose();
			FilteredImageSource fis = new FilteredImageSource(dragImage
					.getSource(), TermLabelMaker.getTransparentFilter());
			dragImage = Toolkit.getDefaultToolkit().createImage(fis);
			return dragImage;
		}
	}
	private class CommitListener implements PropertyChangeListener {

		@SuppressWarnings("unchecked")
		public void propertyChange(PropertyChangeEvent evt) {
			DefaultTreeModel m = (DefaultTreeModel) JTreeWithDragImage.this.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) m.getRoot();
			Enumeration<DefaultMutableTreeNode> childEnum = root.children();
			while (childEnum.hasMoreElements()) {
				m.nodeStructureChanged(childEnum.nextElement());
			}
			AceLog.info("Tree model changed");
		}
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private I_ConfigAceFrame config;

	private List<ChangeListener> workerFinishedListeners = new ArrayList<ChangeListener>();

	public JTreeWithDragImage(I_ConfigAceFrame config) {
		super();
		this.config = config;
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
				this,
				DnDConstants.ACTION_COPY,
				new DragGestureListenerWithImage(
						new TermLabelDragSourceListener()));
		InputMap imap = this.getInputMap();
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()), TransferHandler
				.getCutAction().getValue(Action.NAME));
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()), TransferHandler
				.getCopyAction().getValue(Action.NAME));
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()), TransferHandler
				.getPasteAction().getValue(Action.NAME));

		ActionMap map = this.getActionMap();
		map.put("cut", new AceTransferAction("cut"));
		map.put("copy", new AceTransferAction("copy"));
		map.put("paste", new AceTransferAction("paste"));
		config.addPropertyChangeListener("commit", new CommitListener());
	}

	public I_ConfigAceFrame getConfig() {
		return config;
	}

	public void addWorkerFinishedListener(ChangeListener l) {
		workerFinishedListeners.add(l);
	}

	public void removeWorkerFinishedListener(ChangeListener l) {
		workerFinishedListeners.remove(l);
	}

	public void workerFinished(ExpandNodeSwingWorker worker) {
		ChangeEvent event = new ChangeEvent(worker);
		List<ChangeListener> listeners;
		synchronized (workerFinishedListeners) {
			listeners = new ArrayList<ChangeListener>(workerFinishedListeners);
		}
		for (ChangeListener l : listeners) {
			l.stateChanged(event);
		}
	}

}
