package org.dwfa.ace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.vodb.types.ConceptBean;

public class TermLabelMaker {
	
	public static final int LABEL_WIDTH = 330;
	
	public static I_ImplementActiveLabel newLabel(I_ConceptAttributeTuple conAttribute, boolean showLongForm, boolean showStatus) throws IOException {
		ConceptBean statusBean = null;
		String text = "null characteristic";
		if (conAttribute != null) {
			if (conAttribute.isDefined()) {
				text = "defined";
			} else {
				text = "primitive";
			}
			statusBean = ConceptBean.get(conAttribute.getConceptStatus());
		}
		StringBuffer labelBuff = new StringBuffer();
		StringBuffer toolTipBuff = new StringBuffer();
		if (BasicHTML.isHTMLString(text)) {
			text = text.substring(6);
		} 
		labelBuff.append("<html>");
		labelBuff.append("<font face='Dialog' size='3'>");
		labelBuff.append(text);
		labelBuff.append(" &nbsp;</font>");
      labelBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
      if (statusBean != null) {
         labelBuff.append(statusBean.getInitialText());
      }
      labelBuff.append("</font>&nbsp;");

      toolTipBuff.append("<html>");
		StringBuffer writeBuff = labelBuff;
		if (showLongForm) {
			writeBuff = labelBuff;
		} else {
			writeBuff = toolTipBuff;
		}

		if (showStatus) {
			writeBuff = labelBuff;
			writeBuff.append("<br>");
		} else {
			writeBuff = toolTipBuff;
			if (showLongForm == false) {
				writeBuff.append("<br>");
			}
		}
		
		String labelHtml = labelBuff.toString();
		String toolTipHtml = toolTipBuff.toString();
		LabelForTuple ldt = new LabelForConceptAttributeTuple(conAttribute, showLongForm, showStatus);
		return makeLabel(ldt, labelHtml, toolTipHtml);		
	}
	public static I_ImplementActiveLabel newLabel(I_DescriptionTuple desc, boolean showLongForm, boolean showStatus) throws IOException {
		ConceptBean typeBean = null;
		ConceptBean statusBean = null;
		String text = "null desc";
		if (desc != null) {
			text = desc.getText();
			typeBean = ConceptBean.get(desc.getTypeId());
			statusBean = ConceptBean.get(desc.getStatusId());
		}
		StringBuffer labelBuff = new StringBuffer();
		StringBuffer toolTipBuff = new StringBuffer();
		if (BasicHTML.isHTMLString(text)) {
			text = text.substring(6);
		} 
		labelBuff.append("<html>");
		labelBuff.append("<font face='Dialog' size='3'>");
		labelBuff.append(text);
		labelBuff.append(" &nbsp;</font>");
		toolTipBuff.append("<html>");
		StringBuffer writeBuff = labelBuff;
		if (showLongForm) {
			writeBuff = labelBuff;
			writeBuff.append("<br>");
		} else {
			writeBuff = toolTipBuff;
		}
		writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
		if (typeBean != null) {
			writeBuff.append(typeBean.getInitialText());
		}
		writeBuff.append(" (");
		if (desc != null) {
			writeBuff.append(desc.getLang());
		} else {
			writeBuff.append("null lang");
		}
		writeBuff.append(")</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
		if (desc != null) {
			if (desc.getInitialCaseSignificant()) {
				writeBuff.append("fixed case&nbsp;");
			} else {
				writeBuff.append("changable case&nbsp;");
			}
		}
		writeBuff.append("</font>");
		
		if (showStatus) {
			writeBuff = labelBuff;
			writeBuff.append("<br>");
		} else {
			writeBuff = toolTipBuff;
			if (showLongForm == false) {
				writeBuff.append("<br>");
			}
		}
		writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
		if (statusBean != null) {
			writeBuff.append(statusBean.getInitialText());
		}
		writeBuff.append("</font>&nbsp;");
		
		String labelHtml = labelBuff.toString();
		String toolTipHtml = toolTipBuff.toString();
		if (desc == null) {
			
		}
		LabelForTuple ldt = new LabelForDescriptionTuple(desc, showLongForm, showStatus);
		return makeLabel(ldt, labelHtml, toolTipHtml);		
	}
	public static I_ImplementActiveLabel newLabel(I_RelTuple rel, boolean showLongForm, boolean showStatus) throws IOException {
		ConceptBean typeBean = ConceptBean.get(rel.getRelTypeId());
		ConceptBean destBean = ConceptBean.get(rel.getC2Id());
		ConceptBean refinabilityBean = ConceptBean.get(rel.getRefinabilityId());
		ConceptBean characteristicBean = ConceptBean.get(rel.getCharacteristicId());
		ConceptBean statusBean = ConceptBean.get(rel.getStatusId());

		StringBuffer labelBuff = new StringBuffer();
		StringBuffer toolTipBuff = new StringBuffer();
		labelBuff.append("<html>");
		toolTipBuff.append("<html>");
		labelBuff.append("<font face='Dialog' size='3' color='blue'>");
		labelBuff.append(typeBean.getInitialText());
		labelBuff.append(" &nbsp;</font><br><font face='Dialog' size='3' color='green'>");
		labelBuff.append(destBean.getInitialText());
		labelBuff.append(" &nbsp;</font>");
		StringBuffer writeBuff = labelBuff;
		if (showLongForm) {
			writeBuff = labelBuff;
			writeBuff.append("<br>");
		} else {
			writeBuff = toolTipBuff;
		}
		writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
		writeBuff.append(characteristicBean.getInitialText());
		writeBuff.append("</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
		writeBuff.append(refinabilityBean.getInitialText());
		writeBuff.append("</font>");
		writeBuff.append("</font> &#151 <font face='Dialog' size='2' color='#00008B'>");
		writeBuff.append(rel.getGroup());
		writeBuff.append("&nbsp;</font>");
		if (showStatus) {
			writeBuff = labelBuff;
			writeBuff.append("<br>");
		} else {
			writeBuff = toolTipBuff;
			if (showLongForm == false) {
				writeBuff.append("<br>");
			}
		}
		writeBuff.append("<font face='Dialog' size='2' color='#00008B'>&nbsp;");
		writeBuff.append(statusBean.getInitialText());
		writeBuff.append("&nbsp;</font>");
		
		
		String labelHtml = labelBuff.toString();
		String toolTipHtml = toolTipBuff.toString();
		LabelForRelationshipTuple lrt = new LabelForRelationshipTuple(rel, showLongForm, showStatus);
		return makeLabel(lrt, labelHtml, toolTipHtml);		
	}

	public static I_ImplementActiveLabel makeLabel(I_ImplementActiveLabel activeLabel,
			String labelHtml, String toolTipHtml) {
		setupLabel(activeLabel.getLabel(), labelHtml, toolTipHtml);
		
		return activeLabel;
	}
	private static JLabel setupLabel(JLabel label, String labelHtml, String toolTipHtml) {
		label.setText(labelHtml);
		label.setBackground(Color.white);
		label.setOpaque(true);

		View v = BasicHTML.createHTMLView(label, labelHtml);
		v.setSize(LABEL_WIDTH, 0);
		float prefYSpan = v.getPreferredSpan(View.Y_AXIS);	
		float firstYSpan = prefYSpan;
		float prefXSpan = v.getPreferredSpan(View.X_AXIS);
		Dimension prefSize = new Dimension((int)prefXSpan, (int) prefYSpan);

		v.setSize(prefSize.width - 10, prefSize.height);
		prefYSpan = v.getPreferredSpan(View.Y_AXIS);	
		prefXSpan = v.getPreferredSpan(View.X_AXIS);
		
		while ((firstYSpan == prefYSpan) && (prefSize.width > 40)) {
			prefSize = new Dimension((int)prefXSpan, (int) prefYSpan);
			v.setSize(prefSize.width - 10, prefSize.height);
			prefYSpan = v.getPreferredSpan(View.Y_AXIS);	
			prefXSpan = v.getPreferredSpan(View.X_AXIS);
		}
		
		prefSize = new Dimension(prefSize.width + 10, prefSize.height + 4);
		label.setMaximumSize(prefSize);
		label.setSize(prefSize);
		label.setMinimumSize(prefSize);
		label.setPreferredSize(prefSize);
		label.setToolTipText(toolTipHtml);
		return label;
	}
	public static JLabel makeLabel(String labelHtml) {
		if (BasicHTML.isHTMLString(labelHtml) == false) {
			labelHtml = "<html>" + labelHtml + " &nbsp;";
		} 
		if (labelHtml.endsWith(" &nbsp;") == false) {
			labelHtml = labelHtml + " &nbsp;";
		}
		return setupLabel(new JLabel(), labelHtml, null);
	}
	public static ImageFilter getTransparentFilter() {
		return transparentFilter;
	}
	private static TransparencyFilter transparentFilter = new TransparencyFilter();
	
	   private static class TransparencyFilter extends RGBImageFilter {
	        public TransparencyFilter() {
	            // When this is set to true, the filter will work with images
	            // whose pixels are indices into a color table (IndexColorModel).
	            // In such a case, the color values in the color table are filtered.
	            canFilterIndexColorModel = true;
	        }
	    
	        // This method is called for every pixel in the image
	        public int filterRGB(int x, int y, int rgb) {
	            if (x == -1) {
	                // The pixel value is from the image's color table rather than the image itself
	            }
	            return rgb & 0xAAFFFFFF;
	        }
	    }

}
