
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.tree;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.I_RenderAndFocusOnBean;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.bind.ThinVersionHelper.MAX_VALUE_TYPE;
import org.dwfa.vodb.types.IntSet;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

/**
 *
 * @author kec
 */
public class TaxonomyNodeRenderer extends DefaultTreeCellRenderer
        implements PropertyChangeListener, I_RenderAndFocusOnBean {
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   private boolean          drawsFocusBorderAroundIcon = false;
   private IntSet           viewerImageTypes           = new IntSet();
   private I_ConfigAceFrame aceConfig;
   private boolean          drawDashedFocusIndicator;

   /**
    * Color to draw the focus indicator in, determined from the background.
    * color.
    */
   private Color            focusBGColor;
   private I_GetConceptData focusBean;
   private TaxonomyHelper   helper;
   private Boolean          highlightConflictsInTaxonomyView;
   private I_IntList        refsetsToShow;
   private boolean          showPathInfoInTaxonomy;
   private boolean          showRefsetInfoInTaxonomy;
   private boolean          showViewerImagesInTaxonomy;

   /**
    * Background color of the tree.
    */
   private Color   treeBGColor;
   @SuppressWarnings("unused")
   private boolean variableHeightTaxonomyView;

   //~--- constructors --------------------------------------------------------

   public TaxonomyNodeRenderer(I_ConfigAceFrame aceConfig, TaxonomyHelper helper)
           throws IOException {
      super();
      this.aceConfig                   = aceConfig;
      this.helper                      = helper;
      showViewerImagesInTaxonomy       = this.aceConfig.getShowViewerImagesInTaxonomy();
      variableHeightTaxonomyView       = this.aceConfig.getVariableHeightTaxonomyView();
      showRefsetInfoInTaxonomy         = this.aceConfig.getShowRefsetInfoInTaxonomy();
      showPathInfoInTaxonomy           = this.aceConfig.getShowPathInfoInTaxonomy();
      highlightConflictsInTaxonomyView = this.aceConfig.getHighlightConflictsInTaxonomyView();
      this.aceConfig.addPropertyChangeListener(this);
      refsetsToShow = this.aceConfig.getRefsetsToShowInTaxonomy();
      setLeafIcon(null);
      setClosedIcon(null);
      setOpenIcon(null);

      Object value = UIManager.get("Tree.drawDashedFocusIndicator");

      drawDashedFocusIndicator = ((value != null) && ((Boolean) value).booleanValue());

      ConceptChronicleBI viewerImageType =
         Ts.get().getConcept(ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getUids());

      viewerImageTypes.add(viewerImageType.getConceptNid());
   }

   //~--- enums ---------------------------------------------------------------

   public enum NodeIcon {
      MULTI_PARENT_CLOSED(new ImageIcon(ACE.class.getResource("/16x16/plain/nav_up_green.png"))),
      MULTI_PARENT_OPEN(new ImageIcon(ACE.class.getResource("/16x16/plain/nav_up_right_green.png"))),
      FOCUS_MULTI_PARENT_OPEN(new ImageIcon(ACE.class.getResource("/16x16/plain/nav_up_right_blue.png"))),
      MULTI_PARENT_ROOT(new ImageIcon(ACE.class.getResource("/16x16/plain/pin_green.png")));

      ImageIcon icon;

      //~--- constructors -----------------------------------------------------

      private NodeIcon(ImageIcon icon) {
         this.icon = icon;
      }
   }

   //~--- methods -------------------------------------------------------------

   private void addChildrenToolTipText(ConceptVersionBI cv) throws IOException {
      StringBuilder toolTipText = new StringBuilder();

      toolTipText.append("<html>");

      int originCount = 0;

      for (PositionBI child : Ts.get().getPath(cv.getConceptNid()).getOrigins()) {
         originCount++;
         toolTipText.append("<font color=blue>origin:</font> ");
         toolTipText.append(child.toString());
         toolTipText.append("<br>");
      }

      if (originCount == 0) {
         toolTipText.append("no origins<br><br>");
      } else {
         toolTipText.append("<br>");
      }

      int childCount = 0;

      for (PathBI child : Ts.get().getPathChildren(cv.getConceptNid())) {
         childCount++;
         toolTipText.append("<font color=green>child:</font> ");
         toolTipText.append(child.toString());
         toolTipText.append("<br>");
      }

      if (childCount == 0) {
         toolTipText.append("no children");
      }

      this.setToolTipText(toolTipText.toString());
   }

   /**
    * Paints the value. The background is filled based on selected.
    */
   @Override
   public void paint(Graphics g) {
      Color bColor;

      if (selected) {
         bColor = getBackgroundSelectionColor();
      } else {
         bColor = getBackgroundNonSelectionColor();

         if (bColor == null) {
            bColor = getBackground();
         }
      }

      int imageOffset = -1;

      if (bColor != null) {
         imageOffset = getLabelStart();
         g.setColor(bColor);

         if (getComponentOrientation().isLeftToRight()) {
            g.fillRect(imageOffset, 0, getWidth() - imageOffset, getHeight());
         } else {
            g.fillRect(0, 0, getWidth() - imageOffset, getHeight());
         }
      }

      if (hasFocus) {
         if (drawsFocusBorderAroundIcon) {
            imageOffset = 0;
         } else if (imageOffset == -1) {
            imageOffset = getLabelStart();
         }

         if (getComponentOrientation().isLeftToRight()) {
            paintFocus(g, imageOffset, 0, getWidth() - imageOffset, getHeight());
         } else {
            paintFocus(g, 0, 0, getWidth() - imageOffset, getHeight());
         }
      }

      super.paint(g);
   }

   private void paintFocus(Graphics g, int x, int y, int w, int h) {
      Color bsColor = getBorderSelectionColor();

      if ((bsColor != null) && (selected ||!drawDashedFocusIndicator)) {
         g.setColor(bsColor);
         g.drawRect(x, y, w - 1, h - 1);
      }

      if (drawDashedFocusIndicator) {
         Color color;

         if (selected) {
            color = getBackgroundSelectionColor();
         } else {
            color = getBackgroundNonSelectionColor();

            if (color == null) {
               color = getBackground();
            }
         }

         if (treeBGColor != color) {
            treeBGColor  = color;
            focusBGColor = new Color(~color.getRGB());
         }

         g.setColor(focusBGColor);
         BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("showRefsetInfoInTaxonomy")) {
         showRefsetInfoInTaxonomy = aceConfig.getShowRefsetInfoInTaxonomy();
      } else if (evt.getPropertyName().equals("variableHeightTaxonomyView")) {
         variableHeightTaxonomyView = aceConfig.getVariableHeightTaxonomyView();
      } else if (evt.getPropertyName().equals("highlightConflictsInTaxonomyView")) {
         highlightConflictsInTaxonomyView = aceConfig.getHighlightConflictsInTaxonomyView();
      } else if (evt.getPropertyName().equals("showViewerImagesInTaxonomy")) {
         showViewerImagesInTaxonomy = aceConfig.getShowViewerImagesInTaxonomy();
      } else if (evt.getPropertyName().equals("refsetsToShow")) {
         refsetsToShow = this.aceConfig.getRefsetsToShowInTaxonomy();
      } else if (evt.getPropertyName().equals("showPathInfoInTaxonomy")) {
         showPathInfoInTaxonomy = this.aceConfig.getShowPathInfoInTaxonomy();
      }
   }

   public void setupTaxonomyNode(TaxonomyNode node, ConceptVersionBI cv) throws IOException {
      List<String>     htmlPrefixes = new ArrayList<String>();
      List<String>     htmlSuffixes = new ArrayList<String>();

      if (showViewerImagesInTaxonomy) {
         for (MediaChronicleBI media : node.getMedia()) {
            htmlPrefixes.add("<img src='ace:" + media.getNid() + "$" + media.getConceptNid()
                             + "' align=center>");
         }
      }

      if ((aceConfig.getClassificationRoot() != null)
              && (aceConfig.getClassificationRoot().getConceptNid() == node.getCnid())) {
         htmlSuffixes.add("<font color='#CC3300'>&nbsp;[Classification Root]</font>");
      }

      if ((aceConfig.getClassificationRoleRoot() != null)
              && (aceConfig.getClassificationRoleRoot().getConceptNid() == node.getCnid())) {
         htmlSuffixes.add("<font color='#CC3300'>&nbsp;[Classifier Role Root]</font>");
      }

      if (showPathInfoInTaxonomy) {
         showPathInfoInTaxonomy(cv, htmlSuffixes);
      }

      List<DescriptionChronicleBI> descriptionsWithExtensions = new ArrayList<DescriptionChronicleBI>();

      if (showRefsetInfoInTaxonomy) {
         showRefsetInfoInTaxonomy(cv, descriptionsWithExtensions, htmlPrefixes, htmlSuffixes);
      }

      StringBuilder buff = new StringBuilder();

      if ((htmlPrefixes.size() > 0) || (htmlSuffixes.size() > 0)) {
         buff.append("<html>");

         for (String prefix : htmlPrefixes) {
            buff.append(prefix);
         }

         String text;

         if (descriptionsWithExtensions.size() > 0) {
            try {
               text = descriptionsWithExtensions.get(0).getVersion(cv.getViewCoordinate()).getText();
            } catch (ContraditionException ex) {
               text = descriptionsWithExtensions.get(0).getVersions(cv.getViewCoordinate()).iterator().next().getText();
            }
         } else {
            text = node.getConceptText();
         }

         if (text.toLowerCase().startsWith("<html>")) {
            buff.append(text.substring(5));
         } else {
            buff.append(text);
         }

         // if (htmlSuffixes.size() > 0) {
         // buff.append("<br>");
         // }
         for (String suffix : htmlSuffixes) {
            buff.append(suffix);
         }
      } else {
         buff.append(node.getConceptText());
      }

      node.setConceptText(buff.toString());

      int       sourceRelTupleSize = node.getParentNidSet(cv).size();
      Rectangle iconRect           = getIconRect(node.getParentDepth());

      if (sourceRelTupleSize > 1) {
         if (node.isParentOpened()) {
            node.setIcon(NodeIcon.MULTI_PARENT_OPEN);
         } else {
            node.setIcon(NodeIcon.MULTI_PARENT_CLOSED);
         }

         this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                 BorderFactory.createMatteBorder(0, iconRect.x, 0, 0, getBackgroundNonSelectionColor())));
      } else {
         if (node.isSecondaryParentNode()) {
            this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                    BorderFactory.createMatteBorder(0, iconRect.x, 0, 0, getBackgroundNonSelectionColor())));

            if (sourceRelTupleSize == 0) {
               node.setIcon(NodeIcon.MULTI_PARENT_ROOT);
            } else {
               if (node.isParentOpened()) {
                  if ((focusBean != null) && (node.getCnid() == focusBean.getConceptNid())) {
                     node.setIcon(NodeIcon.FOCUS_MULTI_PARENT_OPEN);
                  } else {
                     node.setIcon(NodeIcon.MULTI_PARENT_OPEN);
                  }
               } else {
                  if ((focusBean != null) && (node.getCnid() == focusBean.getConceptNid())) {
                     node.setIcon(NodeIcon.MULTI_PARENT_OPEN);
                  } else {
                     node.setIcon(NodeIcon.MULTI_PARENT_CLOSED);
                  }
               }
            }
         } else {
            node.setIcon(null);
         }
      }
   }

   private void showPathInfoInTaxonomy(ConceptVersionBI cv, List<String> htmlSuffixes) throws IOException {
      if (Ts.get().hasPath(cv.getConceptNid())) {
         addChildrenToolTipText(cv);

         PositionBI latestInheritedViewPosition = null;

         for (PathBI editPath : aceConfig.getEditingPathSet()) {
            if (editPath.getConceptNid() == cv.getConceptNid()) {
               htmlSuffixes.add("<font color=red>&nbsp;[Editing]</font>");
            }
         }

         for (PathBI promotionPath : aceConfig.getPromotionPathSet()) {
            if (promotionPath.getConceptNid() == cv.getConceptNid()) {
               htmlSuffixes.add("<font color='#669900'>&nbsp;[Promotion]</font>");
            }
         }

         for (PositionBI viewPosition : aceConfig.getViewPositionSet()) {
            if (viewPosition.getPath().getConceptNid() == cv.getConceptNid()) {
               String version = ThinVersionHelper.format(viewPosition.getVersion(), MAX_VALUE_TYPE.LATEST);

               htmlSuffixes.add("<font color='#007FAE'>&nbsp;[Viewing:" + version + "]</font>");
            }

            for (PositionBI origin : viewPosition.getPath().getNormalisedOrigins()) {
               if (origin.getPath().getConceptNid() == cv.getConceptNid()) {
                  if ((latestInheritedViewPosition == null)
                          || (origin.getVersion() > latestInheritedViewPosition.getVersion())) {
                     latestInheritedViewPosition = origin;
                  }
               }
            }
         }

         if (latestInheritedViewPosition != null) {
            String version = ThinVersionHelper.format(latestInheritedViewPosition.getVersion(),
                                MAX_VALUE_TYPE.LATEST);

            htmlSuffixes.add("<font color='#967F49'>&nbsp;[Inherited view:" + version + "]</font>");
         }
      } else {
         setToolTipText(null);
      }
   }

   private void showRefsetInfoInTaxonomy(ConceptVersionBI cv,
           List<DescriptionChronicleBI> descriptionsWithExtensions, List<String> htmlPrefixes,
           List<String> htmlSuffixes)
           throws IOException {
      Collection<RefexChronicleBI> extensions = new ArrayList<RefexChronicleBI>();

      extensions.addAll(cv.getRefexes());

      Collection<? extends DescriptionChronicleBI> descriptions          = cv.getDescs();
      List<RefexChronicleBI>                       descriptionExtensions = new ArrayList<RefexChronicleBI>();

      for (DescriptionChronicleBI desc : descriptions) {
         Collection<? extends RefexChronicleBI<?>> descExts = desc.getRefexes();

         descriptionExtensions.addAll(descExts);

         if (descExts.size() > 0) {
            descriptionsWithExtensions.add(desc);
         }
      }

      extensions.addAll(descriptionExtensions);

      for (int i : refsetsToShow.getListArray()) {
         for (RefexChronicleBI ebr : extensions) {
            if ((ebr != null) && (ebr.getCollectionNid() == i)) {
               List<? extends RefexVersionBI> returnTuples = new ArrayList<RefexVersionBI>();

               if (ebr instanceof RefexBooleanVersionBI) {
                  for (RefexChronicleBI t : ebr.getCurrentRefexes(cv.getViewCoordinate())) {
                     boolean extValue = ((RefexBooleanVersionBI) t).getBoolean1();

                     try {
                        I_GetConceptData booleanImageBean =
                           Terms.get().getConcept(
                               RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.getUids());

                        if (extValue) {
                           booleanImageBean = Terms.get().getConcept(
                              RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.getUids());
                        }

                        for (I_ImageTuple imageTuple :
                                booleanImageBean.getImageTuples(aceConfig.getAllowedStatus(),
                                   viewerImageTypes, aceConfig.getViewPositionSetReadOnly(),
                                   aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy())) {
                           htmlPrefixes.add("<img src='ace:" + imageTuple.getNid() + "$"
                                            + imageTuple.getConceptNid() + "' align=center>");
                        }
                     } catch (TerminologyException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                     }
                  }
               } else if (ebr instanceof RefexCnidVersionBI) {
                  for (RefexChronicleBI t : ebr.getCurrentRefexes(cv.getViewCoordinate())) {
                     ConceptVersionBI ebrCb = Ts.get().getConceptVersion(cv.getViewCoordinate(),
                                                 ((RefexCnidVersionBI) t).getCnid1());

                     try {
                        for (MediaChronicleBI imageTuple : ebrCb.getMediaActive()) {
                           htmlPrefixes.add("<img src='ace:" + imageTuple.getNid() + "$"
                                            + imageTuple.getConceptNid() + "' align=center>");
                        }
                     } catch (ContraditionException ex) {
                        for (MediaChronicleBI imageTuple : ebrCb.getMedia()) {
                           htmlPrefixes.add("<img src='ace:" + imageTuple.getNid() + "$"
                                            + imageTuple.getConceptNid() + "' align=center>");
                        }
                     }
                  }
               } else if (ebr instanceof RefexIntVersionBI) {
                  for (RefexChronicleBI t : ebr.getCurrentRefexes(cv.getViewCoordinate())) {
                     int extValue = ((RefexIntVersionBI) t).getInt1();

                     htmlPrefixes.add("<font color=blue>&nbsp;" + extValue + "&nbsp;</font>");
                  }
               } else if (ebr instanceof RefexStrVersionBI) {
                  for (RefexChronicleBI t : ebr.getCurrentRefexes(cv.getViewCoordinate())) {
                     String strExt = ((RefexStrVersionBI) t).getStr1();

                     htmlSuffixes.add("<code><strong>" + strExt + "'</strong></code>");
                  }
               }
            }
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public I_GetConceptData getFocusBean() {
      return focusBean;
   }

   @Override
   public Rectangle getIconRect(int parentDepth) {
      int indent = (NodeIcon.MULTI_PARENT_OPEN.icon.getIconWidth() * parentDepth);

      if (indent > 0) {
         indent = indent + 4;
      }

      return new Rectangle(indent, 0, NodeIcon.MULTI_PARENT_OPEN.icon.getIconWidth(),
                           NodeIcon.MULTI_PARENT_OPEN.icon.getIconHeight());
   }

   private int getLabelStart() {
      Icon currentI = getIcon();

      if ((currentI != null) && (getText() != null)) {
         if (getBorder() != null) {
            return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1)
                   + getBorder().getBorderInsets(this).left;
         }

         return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
      }

      return 0;
   }

   @Override
   public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
           boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

      TaxonomyNode node = (TaxonomyNode) value;

      this.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));

      try {
         for (Color pathColor :
                 node.getPathColors(aceConfig.getViewCoordinate(), aceConfig.getPathColorMap())) {
            this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                    BorderFactory.createMatteBorder(0, 2, 0, 0, pathColor)));
            this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                    BorderFactory.createEmptyBorder(0, 2, 0, 0)));
         }

         this.setIcon(node.getIcon().icon);
      } catch (IOException e) {
         this.setText(e.toString());
         AceLog.getAppLog().alertAndLogException(e);
      }

      return this;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setFocusBean(I_GetConceptData focusBean) {
      this.focusBean = focusBean;
   }
}
