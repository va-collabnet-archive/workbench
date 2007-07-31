package org.dwfa.maven.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
*
** <h1>MojoGraph</h1>
* <br>
* <p>The <code>MojoGraph</code> class creates a graph image and saves it to the file system.</p>
* <p>A maven xdoc xml file is then created, which will be used by maven to generate a site to display the graph.</p>
* 
* 
* <br>
* <br>
* @see <code>org.apache.maven.plugin.AbstractMojo</code>
* @author PeterVawser 
* @goal buildgraph
*/
public class MojoGraph extends AbstractMojo{
	List<double[]> coords = new ArrayList<double[]>();	
	/*
	 * Buffered image to draw graphic to
	 */
	BufferedImage bufferedImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
	private String jpgExportPath = "C:\\_working\\ace\\dwfa-mojo\\dev\\src\\site\\resources\\images";
	private String exportFormat = "jpg";
	private String jpgFileName = "\\graph.jpg";
	private String xdocExportPath = "C:\\_working\\ace\\dwfa-mojo\\dev\\src\\site\\xdoc";
	private String xdocFileName = "\\progressgraph.xml";
	
		
  public void MojoGraph(){
	  
  }

  public void execute() throws MojoExecutionException, MojoFailureException {
      try {
    	  MojoGraph mg = new MojoGraph();
    	  mg.createGraph();
      } catch (Exception e) {
          throw new MojoExecutionException(e.getMessage(), e);
      }
  }
    
  public void createGraph()throws MojoExecutionException{
	  
	  /*
	   * Create stroke to use for drawing
	   */
	  BasicStroke stroke = new BasicStroke(2.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	  
	  /*
	   * Create graphics context on the buffered image
	   */
	  Graphics2D g2d = bufferedImage.createGraphics();
	  g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
  	        RenderingHints.VALUE_ANTIALIAS_ON);
	  g2d.setStroke(stroke);
	  	  
	  createGraphAxis(g2d);	  
	  translateData();
	  plotData(g2d,Color.BLUE, coords);
	  try{
		  exportGraph();
		  createXDoc();
	  }catch(MojoExecutionException mee){
		  throw new MojoExecutionException(mee.getMessage(), mee);
	  }
	  
	  
  }// End method createGraph
  
  /**
   *  Add a small square centered at (x,y) to the specified path 
   *  */
  private void markPoint(GeneralPath path, float x, float y) {
    float radius = 2;
	  
	path.moveTo(x - radius, y - radius); // Begin a new sub-path
    path.lineTo(x + radius, y - radius); // Add a line segment to it
    path.lineTo(x + radius, y + radius); // Add a line segment to it
    path.lineTo(x - radius, y + radius); // Add a line segment to it
    path.closePath(); // Close path to create line back to sub-path begin point
  }//End method markPoint
  
  private void createGraphAxis(Graphics2D g2d){
	  /*
	   * Draw graph outline on image
	   */
	  g2d.setColor(Color.WHITE);
	  g2d.fillRoundRect(0,0, 800, 800, 10, 10);
	  g2d.setColor(Color.BLACK);
	  g2d.drawLine(100,10, 100, 350);
	  g2d.drawLine(100,350, 760, 350);
	  
	  /*
	   * Set axis increment labels font
	   */
	  Font font = new Font(Font.SERIF,Font.PLAIN, 10 );
	  g2d.setFont(font);
	  
	  /*
	   * Draw and label x-axis increments
	   */
	  int xAxisIncrement = 0;
	  for(int i=150;i<750;i+=100){
		  xAxisIncrement += 10;
		  g2d.drawLine(i,345, i, 355);
		  g2d.drawString(new Integer(xAxisIncrement).toString(), i + (font.getSize()/2), 770);
	  }
	    
	  /*
	   * Draw and label y-axis increments
	   */
	  int yAxisIncrement = 0;
	  for(int i=340;i>10;i-=10){
		  yAxisIncrement +=1;
		  g2d.drawLine(95,i, 105, i);
		  g2d.drawString(new Integer(yAxisIncrement).toString(), 75, i + (font.getSize()/2));
	  }
  }//End method createGraphAxis
  
  private void plotData(Graphics2D g2d, Color color, List<double[]> coordsList ){
	  /*
	   * Create line graph data 
	   */
	  g2d.setColor(Color.BLUE);
	  GeneralPath gp = new GeneralPath();
	  gp.moveTo(coordsList.get(0)[0], coordsList.get(0)[1]); // Begin a new path
	  for (int i =1; i < coordsList.size(); i++){
		  gp.lineTo(coordsList.get(i)[0], coordsList.get(i)[1]); // Add a line segment to it
	  }
	  gp.moveTo(coordsList.get(0)[0], coordsList.get(0)[1]); // Move to start of path to prevent closePath from drawing line back to point of origin
	  gp.closePath(); // Close the path
	  
	  /*
	   * Iterate over path and draw plot points at line segment ends
	   */
	  PathIterator pi = gp.createTransformedShape(null).getPathIterator(null);
	  while(!pi.isDone()){
		  float[] coords = new float[6];
		  int type = pi.currentSegment(coords);
		  switch(type){
		  	case PathIterator.SEG_CUBICTO:
		  		markPoint(gp, coords[4], coords[5]); // falls through
		    case PathIterator.SEG_QUADTO:
		        markPoint(gp, coords[2], coords[3]); // falls through
		    case PathIterator.SEG_MOVETO:
		    case PathIterator.SEG_LINETO:
		       markPoint(gp, coords[0], coords[1]); // falls through
		    case PathIterator.SEG_CLOSE:
		       break;
		  }
		  pi.next();
	  }
	  
	  /*
	   * Create shape from GeneralPath and draw shape to graphics object
	   */
	  Shape shape = gp.createTransformedShape(null);
	  g2d.draw(shape);
  }//End method plotData
  
  private void labelAxis(Graphics2D g2d){
	  
	  /*
	   * Label X and Y axis titles
	   */
	  Font font = new Font(Font.SERIF,Font.BOLD, 20 );
	  String xAxisLabel = "X-axis label" ;
	  String yAxislabel = "Y-axis Label";
	  
	  
	  g2d.setFont(font);
	  g2d.drawString(xAxisLabel, 350 - (xAxisLabel.length()/2), 420);
	  
	  // clockwise 90 degrees
	  AffineTransform at = new AffineTransform();
	  at.setToRotation(-Math.PI/2.0, 25, 175 - (yAxislabel.length()/2));
	  g2d.setTransform(at);
	  g2d.drawString(yAxislabel, 25, 175 - (yAxislabel.length()/2));
	  
  }//End method labelAxis
  
  private void translateData(){
	  coords.add(new double[]{150, 345});
	  coords.add(new double[]{250, 255});
	  coords.add(new double[]{350, 280});
	  coords.add(new double[]{450, 312});
	  coords.add(new double[]{550, 250});
  }
  
  
  public void exportGraph() throws MojoExecutionException{
	  try{
		  ImageIO.write(bufferedImage, exportFormat, new File(jpgExportPath + jpgFileName));
	  }
	  catch(IOException e){
		  throw new MojoExecutionException(e.getMessage(), e);
	  }
  }//End method exportGraph
  public void createXDoc(){
	 StringBuilder xdocXML = new StringBuilder();
	 
	 xdocXML.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
	 xdocXML.append("<document>");
	 xdocXML.append("<properties>");
	 xdocXML.append("<author email=\"nospam.AT.foo.DOT.com\">Jon S. Stevens</author>");
	 xdocXML.append("<title></title>");
	 xdocXML.append("</properties>");
	 xdocXML.append("<meta name=\"keyword\" content=\"jakarta, java\"/>");
	 xdocXML.append("<body>");
	 xdocXML.append("<section name=\"Section 1\">");
	 xdocXML.append("<p> This is section 1. </p>");
	 xdocXML.append("<table> <tr>  <td>This is a table</td> </tr> </table>");
	 xdocXML.append("</section>");
	 xdocXML.append("<section name=\"Section 2\">");
	 xdocXML.append("<p> This is section 2. </p>");
	 xdocXML.append("</section>");
	 xdocXML.append("<source>");
	 xdocXML.append("<img src=\"images/graph.jpg\" alt=\"GRAPH\"/>");
	 xdocXML.append("</source>");
	 xdocXML.append("</body>");
	 xdocXML.append("</document>");
	 
	 try{
		 File file = new File(xdocExportPath + xdocFileName);                      
		 FileWriter fileWriter = new FileWriter(file);
		 fileWriter.write(xdocXML.toString());
		 fileWriter.close();
	 }
	 catch(IOException e){}
	 
  }//End method createXDoc
}//End class MojoGraph
