/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
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
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.FilteredImageSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import sun.awt.dnd.SunDragSourceContextPeer;

public class TermComponentLabel extends JLabel implements FocusListener, I_ContainTermComponent, ClipboardOwner {

    public class CommitListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (termComponent != null && 
					I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
				try {
					I_GetConceptData igcd = (I_GetConceptData) termComponent;
					if (igcd.isCanceled()) {
						setTermComponent(null);
					}
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
 		}

	}

	private I_AmTermComponent termComponent;

    private I_ConfigAceFrame config;
    
    private boolean lineWrapEnabled = false;
    private int fixedWidth = 150;

    public boolean isLineWrapEnabled() {
		return lineWrapEnabled;
	}

	public void setLineWrapEnabled(boolean lineWrapEnabled) {
		this.lineWrapEnabled = lineWrapEnabled;
	}

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

    private class DeleteAction extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            setTermComponent(null);
        }
    }

    private class CopyXML extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringBuffer buff = new StringBuffer();
                buff.append("<concept>\n");
                if (getTermComponent() == null) {

                } else {
                    I_TermFactory tf = Terms.get();
                    I_GetConceptData concept = (I_GetConceptData) getTermComponent();
                    buff.append("  <concept-ids>\n");
                    for (I_IdVersion idt : concept.getIdentifier().getIdVersions()) {
                        writeIdentifiersToBuff("    ", buff, idt);
                    }
                    buff.append("  </concept-ids>\n");
                    buff.append("  <descriptions>\n");
                    for (I_DescriptionTuple dt : concept.getDescriptionTuples(config.getAllowedStatus(), null,
                        config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy())) {
                        buff.append("    <description type='");
                        I_GetConceptData type = tf.getConcept(dt.getTypeNid());

                        I_DescriptionTuple typeDesc = type.getDescTuple(config.getLongLabelDescPreferenceList(),
                            config.getLanguagePreferenceList(), config.getAllowedStatus(), config.getViewPositionSetReadOnly(),
                            config.getLanguageSortPref(), config.getPrecedence(), config.getConflictResolutionStrategy());
                        buff.append(typeDesc.getText());
                        buff.append("'\n                 text='");
                        buff.append(dt.getText());
                        buff.append("'>\n");
                        for (I_IdVersion idt : tf.getId(dt.getDescId()).getIdVersions()) {
                            writeIdentifiersToBuff("      ", buff, idt);
                        }
                        buff.append("    </description>\n");
                    }
                    buff.append("  </descriptions>\n");
                }
                buff.append("</concept>\n");
                StringSelection transferable = new StringSelection(buff.toString());

                clipboard.setContents(transferable, TermComponentLabel.this);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

        private void writeIdentifiersToBuff(String indent, StringBuffer buff, I_IdVersion idt) throws IOException {
            buff.append(indent);
            buff.append("<id source='");
            I_GetConceptData source;
            try {
                source = Terms.get().getConcept(idt.getAuthorityNid());
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
            I_DescriptionTuple sourceDesc = source.getDescTuple(config.getLongLabelDescPreferenceList(),
                config.getLanguagePreferenceList(), config.getAllowedStatus(), config.getViewPositionSetReadOnly(),
                config.getLanguageSortPref(), config.getPrecedence(), config.getConflictResolutionStrategy());
            buff.append(sourceDesc.getText());
            buff.append("' value='");
            buff.append(idt.getDenotation().toString());
            buff.append("'/>\n");
        }
    }

    private class CopyTDT extends AbstractAction {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringBuffer buff = new StringBuffer();
                if (getTermComponent() == null) {

                } else {
                    I_GetConceptData concept = (I_GetConceptData) getTermComponent();
                    buff.append("cid source\tcid\tdesc\n");
                    I_DescriptionTuple dt = concept.getDescTuple(config.getLongLabelDescPreferenceList(), config);
                    for (I_IdVersion idt : concept.getIdentifier().getIdVersions()) {
                        writeIdentifiersToBuff(buff, idt, dt.getText());
                    }
                }
                StringSelection transferable = new StringSelection(buff.toString());

                clipboard.setContents(transferable, TermComponentLabel.this);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

        private void writeIdentifiersToBuff(StringBuffer buff, I_IdVersion idt, String text) throws IOException {
            I_GetConceptData source;
            try {
                source = Terms.get().getConcept(idt.getAuthorityNid());
            } catch (TerminologyException e) {
                throw new IOException(e);
            }
            I_DescriptionTuple sourceDesc = source.getDescTuple(config.getLongLabelDescPreferenceList(),
                config.getLanguagePreferenceList(), config.getAllowedStatus(), config.getViewPositionSetReadOnly(),
                config.getLanguageSortPref(), config.getPrecedence(), config.getConflictResolutionStrategy());
            buff.append(sourceDesc.getText());
            buff.append("\t");
            buff.append(idt.getDenotation().toString());
            buff.append("\t");
            buff.append(text);
            buff.append("\n");
        }
    }

    private class DragGestureListenerWithImage implements DragGestureListener {

        DragSourceListener dsl;

        public DragGestureListenerWithImage(DragSourceListener dsl) {
            super();
            this.dsl = dsl;
        }

        public void dragGestureRecognized(DragGestureEvent dge) {
        	if (termComponent == null) {
        		return;
        	}
            Image dragImage = getDragImage();
            Point imageOffset = new Point(0, 0);
            try {
				dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, new ConceptTransferable(
				    (I_GetConceptData) termComponent), dsl);
			} catch (InvalidDnDOperationException e) {
                AceLog.getAppLog().log(Level.WARNING, e.getMessage(), e);
                AceLog.getAppLog().log(Level.INFO, "Resetting SunDragSourceContextPeer [1]");
                SunDragSourceContextPeer.setDragDropInProgress(false);
			}
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TermComponentLabel() throws TerminologyException, IOException {
        this(Terms.get().getActiveAceFrameConfig());
    }

    public TermComponentLabel(I_ConfigAceFrame config) {
        super("<html><font color=red>Empty");
        this.config = config;
        addFocusListener(this);
        this.config.addPropertyChangeListener("commit", new CommitListener());
        setTransferHandler(Terms.get().makeTerminologyTransferHandler(this));
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY,
            new DragGestureListenerWithImage(new TermLabelDragSourceListener()));

        setFocusable(true);
        setEnabled(true);
        
        

        this.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }

            public void mouseReleased(MouseEvent e) {
                requestFocusInWindow();
            }

        });

        ActionMap map = this.getActionMap();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteTask");
        map.put("deleteTask", new DeleteAction());
        map.put("Copy TDT", new CopyTDT());
        map.put("Copy XML", new CopyXML());
        setBorder(noFocusBorder);
    }

    private static final Border hasFocusBorder = UIManager.getBorder("List.focusCellHighlightBorder");

    private static final Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

    public void focusGained(FocusEvent e) {
        setBorder(hasFocusBorder);
    }

    public void focusLost(FocusEvent e) {
        setBorder(noFocusBorder);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.I_ContainTermComponent#getTermComponent()
     */
    public I_AmTermComponent getTermComponent() {
        return termComponent;
    }

    private boolean frozen = false;

	private Dimension wrapSize = new Dimension(fixedWidth, 16);

    public int getFixedWidth() {
		return fixedWidth;
	}

	public void setFixedWidth(int fixedWidth) {
		this.fixedWidth = fixedWidth;
	}

	/*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.I_ContainTermComponent#setTermComponent(org.dwfa.vodb.types
     * .I_AmTermComponent)
     */
    public void setTermComponent(I_AmTermComponent termComponent) {
        if (isFrozen()) {
            return;
        }
        if (termComponent != null && 
        		I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
            try {
				if (((I_GetConceptData) termComponent).isCanceled()) {
					termComponent = null;
				}
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
        }
        Object old = this.termComponent;
        this.termComponent = termComponent;
        if (termComponent != null) {
            if (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
                I_GetConceptData cb = (I_GetConceptData) termComponent;
                try {
                    I_DescriptionTuple desc = cb.getDescTuple(config.getLongLabelDescPreferenceList(), config);
                    if (desc != null) {
                        this.setText(desc.getText());
                    } else {
                        this.setText(cb.getInitialText());
                    }
                } catch (IOException e) {
                    this.setText(e.getMessage());
                    AceLog.getAppLog().alertAndLogException(e);
                }
            } else {
                this.setText(this.termComponent.toString());
            }
        } else {
            this.setText("<html><font color=red>Empty");
        }
        if (old == null) {
            firePropertyChange("termComponent", old, termComponent);
        } else if (old.equals(termComponent)) {
            firePropertyChange("termComponent", null, termComponent);
        } else {
            firePropertyChange("termComponent", old, termComponent);
        }
    }

	@Override
	public void setText(String text) {
        setBorder(noFocusBorder);
		if (lineWrapEnabled) {
			if (!BasicHTML.isHTMLString(text)) {
				text = "<html>" + text;
			}
	    	super.setText(text);
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
		} else {
			super.setText(text);
		}
	}

    @Override
	public void setSize(Dimension d) {
		if (lineWrapEnabled) {
			super.setSize(wrapSize);
		} else {
			super.setSize(d);
		}
	}

	@Override
	public void setSize(int width, int height) {
		if (lineWrapEnabled) {
			super.setSize(wrapSize.width, wrapSize.height);
		} else {
			super.setSize(width, height);
		}
	}

	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (lineWrapEnabled) {
			super.setBounds(x, y, wrapSize.width, wrapSize.height);
		} else {
			super.setBounds(x, y, width, height);
		}
	}

	@Override
	public void setBounds(Rectangle r) {
		setBounds(r.x, r.y, r.width, r.height);
	}

	@Override
	public Dimension getMaximumSize() {
		if (lineWrapEnabled)
			return wrapSize;
		return super.getMaximumSize();
	}

	@Override
	public Dimension getMinimumSize() {
		if (lineWrapEnabled)
			return wrapSize;
		return super.getMinimumSize();
	}

	@Override
	public Dimension getPreferredSize() {
		if (lineWrapEnabled)
			return wrapSize;
		return super.getPreferredSize();
	}

	
	
	

	public void addTermChangeListener(PropertyChangeListener l) {
        addPropertyChangeListener("termComponent", l);
    }

    public void removeTermChangeListener(PropertyChangeListener l) {
        removePropertyChangeListener("termComponent", l);
    }

    public I_ConfigAceFrame getConfig() {
        throw new UnsupportedOperationException();
    }

    public Image getDragImage() {
        JLabel dragLabel = TermLabelMaker.makeLabel(getText());
        dragLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        Image dragImage = createImage(dragLabel.getWidth(), dragLabel.getHeight());
        dragLabel.setVisible(true);
        Graphics og = dragImage.getGraphics();
        og.setClip(dragLabel.getBounds());
        dragLabel.paint(og);
        og.dispose();
        FilteredImageSource fis = new FilteredImageSource(dragImage.getSource(), TermLabelMaker.getTransparentFilter());
        dragImage = Toolkit.getDefaultToolkit().createImage(fis);
        return dragImage;
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // Nothing to do...
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        getDropTarget().setActive(!frozen);
    }

	@Override
	public void unlink() {
		// nothing to do...
	}

}
