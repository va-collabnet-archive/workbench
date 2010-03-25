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
package org.dwfa.maven.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.AttributedString;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * 
 ** <h1>MojoGraph</h1> <br>
 * <p>
 * The <code>MojoGraph</code> class creates a graph image and saves it to the
 * file system.
 * </p>
 * <p>
 * A maven xdoc xml file is then created, which will be used by maven to
 * generate a site to display the graph.
 * </p>
 * 
 * 
 * <br>
 * <br>
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Peter Vawser
 * 
 */
public class MojoGraph {
    private List<float[]> coords = new ArrayList<float[]>();
    private HashMap<String, double[][]> dataSource;

    private Color[] dataColors = new Color[] { new Color(0, 0, 255), // blue
                                              new Color(0, 255, 255), // cyan
                                              new Color(0, 255, 0), // green
                                              new Color(255, 255, 0), // yellow
                                              new Color(255, 0, 0), // red
                                              new Color(255, 0, 255) // magenta
    };

    private HashMap<String, Color> legendMap = new HashMap<String, Color>();

    /*
     * Graph display and axis coordinates
     */
    private int graphDisplayHeight = 600;
    private int graphDisplayWidth = 900;

    private int xOffset = 100;
    private int yOffset = 100;
    private int xAxisLength = 350;
    private int yAxisLength = 660;

    private float xAxisMaxIncrements = 33.0f;
    private float yAxisMaxIncrements = 33.0f;
    private float xAxisIncrementSize = 10.0f;
    private float yAxisIncrementSize = 10.0f;
    private int xAxisIncrementValue = 1;
    private int yAxisIncrementValue = 1;

    private String graphTitle = "Title";
    private String xAxisLabel = "X-axis label";
    private String yAxisLabel = "Y-axis Label";
    private String siteTitle = "Site Title";
    private String siteDesc = "Site Description";

    /*
     * html site variable
     */
    StringBuilder htmlTableHeadings = new StringBuilder("");
    StringBuilder htmlTableData = new StringBuilder("");

    /*
     * Buffered image to draw graphic to
     */
    BufferedImage bufferedImage = new BufferedImage(graphDisplayWidth, graphDisplayHeight, BufferedImage.TYPE_INT_RGB);
    private String outputPath = ".";
    private String fileName = "graphOutput";
    private String jpgExportPath = File.separator + "src" + File.separator + "site" + File.separator + "resources"
        + File.separator + "images" + File.separator;
    private String exportFormat = "jpg";

    private String xdocExportPath = File.separator + "src" + File.separator + "site" + File.separator + "xdoc"
        + File.separator;

    private DataType displayDataType = DataType.VALUEOVERVALUE;

    public enum DataType {
        VALUEOVERVALUE, VALUEOVERTIME
    };

    public MojoGraph() {
    }

    public MojoGraph(HashMap<String, double[][]> dataMap, DataType dataType) {
        dataSource = dataMap;
        displayDataType = dataType;
    }

    public MojoGraph(HashMap<String, double[][]> dataMap, DataType dataType, String title) {
        dataSource = dataMap;
        displayDataType = dataType;
        graphTitle = title;
    }

    public void setSiteTitle(String title) {
        siteTitle = title;
    }

    public void setSiteDesc(String desc) {
        siteDesc = desc;
    }

    public void setAxisLabels(String xLabel, String yLabel) {
        xAxisLabel = xLabel;
        yAxisLabel = yLabel;
    }

    public void setTitle(String title) {
        graphTitle = title;
    }

    public void setDataType(DataType dataType) {
        displayDataType = dataType;
    }

    public void setDataSource(HashMap<String, double[][]> dataMap) {
        dataSource = dataMap;
    }

    public void setOutputDir(String outputDir) {
        outputPath = outputDir;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void createGraph() throws MojoExecutionException {

        /*
         * Create stroke to use for drawing
         */
        BasicStroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        /*
         * Create graphics context on the buffered image
         */
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(stroke);

        // Set canvas area and background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, graphDisplayWidth, graphDisplayHeight);
        g2d.setColor(Color.BLACK);

        translateData(g2d);
        createGraphAxis(g2d);
        createLegend(g2d);
        labelAxis(g2d);
        try {
            exportGraph();
            createXDoc();
        } catch (MojoExecutionException mee) {
            throw new MojoExecutionException(mee.getMessage(), mee);
        }

    }// End method createGraph

    private void createLegend(Graphics2D g2d) {
        Iterator<String> it = legendMap.keySet().iterator();
        int spacer = yOffset;
        while (it.hasNext()) {
            String key = it.next();
            Color color = legendMap.get(key);
            int lineXPoint = (xOffset + yAxisLength);
            spacer += 20;
            g2d.setColor(color);
            g2d.drawLine(lineXPoint, spacer, lineXPoint + 15, spacer);
            g2d.drawString(key, lineXPoint + 30, spacer);
        }
        g2d.setColor(Color.BLACK);
    }// End method createLegend

    /**
     * Add a small square centered at (x,y) to the specified path
     * */
    private void markPoint(GeneralPath path, float x, float y) {
        float radius = 2;

        path.moveTo(x - radius, y - radius); // Begin a new sub-path
        path.lineTo(x + radius, y - radius); // Add a line segment to it
        path.lineTo(x + radius, y + radius); // Add a line segment to it
        path.lineTo(x - radius, y + radius); // Add a line segment to it
        path.closePath(); // Close path to create line back to sub-path begin
                          // point
    }// End method markPoint

    private void createGraphAxis(Graphics2D g2d) {
        /*
         * Draw graph outline on image
         */
        // g2d.setColor(Color.WHITE);
        // g2d.fillRoundRect(0,0, graphDisplayWidth, graphDisplayHeight, 10,
        // 10);
        g2d.setColor(Color.BLACK);
        g2d.drawLine(xOffset, yOffset, xOffset, yOffset + xAxisLength);
        g2d.drawLine(xOffset, yOffset + xAxisLength, xOffset + yAxisLength, yOffset + xAxisLength);

        /*
         * Set axis increment labels font
         */
        Font font = new Font("Serif", Font.PLAIN, 10);
        g2d.setFont(font);

        /*
         * Draw and label x-axis increments
         */
        int xAxisIncrement = yOffset + xAxisLength;
        for (int i = 1; i < xAxisMaxIncrements; i++) {
            xAxisIncrement -= xAxisIncrementSize;
            g2d.drawLine(xOffset - 5, xAxisIncrement, xOffset, xAxisIncrement);

            String incrementLabel = new Integer(xAxisIncrementValue * i).toString();
            float labelXPosition = (incrementLabel.length() <= 3) ? xOffset * 0.75f : (xOffset * 0.75f)
                - ((font.getSize2D() / 2) * (incrementLabel.length() - 3));
            g2d.drawString(incrementLabel, labelXPosition, (xAxisIncrement + (font.getSize() / 2)));
        }

        /*
         * Draw and label y-axis increments
         */
        int yAxisIncrement = xOffset;
        for (int i = 1; i < yAxisMaxIncrements; i++) {
            yAxisIncrement = Math.round((xOffset + (yAxisIncrementSize * i)));
            g2d.drawLine(yAxisIncrement, yOffset + xAxisLength, yAxisIncrement, yOffset + xAxisLength + 5);

            String incrementLabel = "";
            float labelYPosition = 0.0f;

            switch (displayDataType) {
            case VALUEOVERTIME:
                Iterator<String> it = dataSource.keySet().iterator();
                while (it.hasNext()) {
                    double[][] pointData = dataSource.get(it.next());
                    long val = (long) pointData[(i * yAxisIncrementValue) - 1][1];
                    Date date = new Date(val);

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM");
                    incrementLabel = formatter.format(date);
                }

                labelYPosition = (xOffset + (yAxisIncrementSize * i))
                    - ((font.getSize2D() / 2) * (incrementLabel.length() / 2));
                g2d.drawString(incrementLabel, labelYPosition, (yOffset + xAxisLength + 15));
                break;

            case VALUEOVERVALUE:
            default:
                incrementLabel = new Integer(yAxisIncrementValue * i).toString();

                labelYPosition = (xOffset + (yAxisIncrementSize * i))
                    - ((font.getSize2D() / 2) * incrementLabel.length());
                g2d.drawString(incrementLabel, labelYPosition, (yOffset + xAxisLength + 15));
            }
            // Build html table headings
            htmlTableHeadings.append("<th>" + incrementLabel + "</th>");

        }// End for loop
    }// End method createGraphAxis

    private void plotData(Graphics2D g2d, Color color, List<float[]> coordsList) {
        /*
         * Create line graph data
         */
        g2d.setColor(color);
        GeneralPath gp = new GeneralPath();
        gp.moveTo(coordsList.get(0)[0], coordsList.get(0)[1]); // Begin a new
                                                               // path
        for (int i = 1; i < coordsList.size(); i++) {
            gp.lineTo(coordsList.get(i)[0], coordsList.get(i)[1]); // Add a line
                                                                   // segment to
                                                                   // it
        }
        gp.moveTo(coordsList.get(0)[0], coordsList.get(0)[1]); // Move to start
                                                               // of path to
                                                               // prevent
                                                               // closePath from
                                                               // drawing line
                                                               // back to point
                                                               // of origin
        gp.closePath(); // Close the path

        /*
         * Iterate over path and draw plot points at line segment ends
         */
        PathIterator pi = gp.createTransformedShape(null).getPathIterator(null);
        while (!pi.isDone()) {
            float[] coords = new float[6];
            int type = pi.currentSegment(coords);
            switch (type) {
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
    }// End method plotData

    private void labelAxis(Graphics2D g2d) {

        /*
         * Label X and Y axis titles
         */
        g2d.setColor(Color.BLACK);
        Font font = new Font("Serif", Font.BOLD, 20);
        g2d.setFont(font);

        // Create attributed string to enable underlined title
        AttributedString as = new AttributedString(graphTitle);
        as.addAttribute(TextAttribute.SIZE, 24.0f);
        as.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_GRAY);
        float x = ((yAxisLength) / 2) - ((font.getSize2D() / 2) * (graphTitle.length() / 2)) + xOffset;
        float y = yOffset / 2;

        g2d.drawString(as.getIterator(), x, y);

        // Label x axis
        g2d.drawString(xAxisLabel,
            ((xOffset + yAxisLength) / 2) - ((font.getSize2D() / 2) * (xAxisLabel.length() / 2)), (yOffset
                + xAxisLength + 50));

        // Set transform and rotate clockwise 90 degrees for y axis label
        g2d.translate(((Double) (xOffset * 0.25)).intValue(), (xAxisLength / 2)
            - ((font.getSize2D() / 2) * (yAxisLabel.length() / 2)) + (yOffset));
        g2d.rotate(90.0 * Math.PI / 180.0);
        g2d.drawString(yAxisLabel, 0, 0);

    }// End method labelAxis

    private void translateData(Graphics2D g2d) {
        float maxXPointDataValue = 0.0f;
        float maxYPointDataValue = 0.0f;
        if (dataSource != null) {
            Iterator<String> it = dataSource.keySet().iterator();

            // Determine the max x and y values
            while (it.hasNext()) {
                double[][] pointData = dataSource.get(it.next());

                for (int i = 0; i < pointData.length; i++) {
                    float xPointData = ((Double) pointData[i][0]).floatValue();
                    float yPointData = ((Double) pointData[i][1]).floatValue();

                    switch (displayDataType) {
                    case VALUEOVERTIME:
                        if (xPointData > maxXPointDataValue) {
                            maxXPointDataValue = xPointData;
                        }

                        maxYPointDataValue = pointData.length;

                        break;
                    case VALUEOVERVALUE:
                    default:
                        if (xPointData > maxXPointDataValue) {
                            maxXPointDataValue = xPointData;
                        }

                        if (yPointData > maxYPointDataValue) {
                            maxYPointDataValue = yPointData;
                        }
                    }
                }// End For Loop
            }// End while loop

            /*
             * Set maximum number of increments for x and y axis
             */
            calcAxisIncrements(maxXPointDataValue, maxYPointDataValue);

            /*
             * Transform data values to x,y coordinates
             */

            it = dataSource.keySet().iterator();
            int colorIndex = 0;
            while (it.hasNext()) {
                String key = (String) it.next();
                htmlTableData.append("<tr><td>" + key + "</td>");
                double[][] pointData = dataSource.get(key);

                for (int i = 0; i < pointData.length; i++) {
                    float xPointData = ((Double) pointData[i][0]).floatValue();

                    if (i % yAxisIncrementValue == 0) {
                        htmlTableData.append("<td>" + xPointData + "</td>");
                    }

                    float yPointData = 0.0f;

                    switch (displayDataType) {
                    case VALUEOVERTIME:
                        yPointData = i + 1;
                        break;
                    case VALUEOVERVALUE:
                    default:
                        yPointData = ((Double) pointData[i][1]).floatValue();
                    }

                    coords.add(new float[] {
                                            (yAxisIncrementSize * (yPointData / yAxisIncrementValue)) + xOffset,
                                            ((xAxisLength) - (xAxisIncrementSize * (xPointData / xAxisIncrementValue)))
                                                + yOffset

                    });
                }

                htmlTableData.append("</tr>");
                plotData(g2d, dataColors[colorIndex], coords);
                coords.clear();

                legendMap.put(key, dataColors[colorIndex]);

                colorIndex = (colorIndex < 5) ? ++colorIndex : 0;
            } // End while loop
        }
    }// End method translateData

    private void calcAxisIncrements(float maxXAxisValue, float maxYAxisValue) {
        float xAxisPlus10PC = maxXAxisValue + (maxXAxisValue * 0.10f);
        xAxisIncrementValue = (((xAxisPlus10PC) / (xAxisLength)) * 10 <= 0.0f)
                                                                              ? 1
                                                                              : Math.round(((xAxisPlus10PC) / (xAxisLength)) * 10);
        // Line below works but I don't know why???
        xAxisMaxIncrements = Math.round(xAxisPlus10PC / xAxisIncrementValue) - 2;
        xAxisIncrementSize = (xAxisLength) / xAxisMaxIncrements;

        float yAxisPlus10PC = maxYAxisValue + (maxYAxisValue * 0.10f);
        yAxisIncrementValue = ((yAxisPlus10PC / (yAxisLength)) * 10 <= 0)
                                                                         ? 1
                                                                         : Math.round((yAxisPlus10PC / (yAxisLength)) * 10);

        if (yAxisIncrementValue == 1 && yAxisPlus10PC > (yAxisLength / 2) / 10) {
            yAxisIncrementValue = 5;
        }

        yAxisMaxIncrements = Math.round(yAxisPlus10PC / yAxisIncrementValue);
        yAxisIncrementSize = (yAxisLength) / yAxisMaxIncrements;

    }// End method calcAxisIncrements

    public void exportGraph() throws MojoExecutionException {
        try {
            File f = new File(outputPath + jpgExportPath);
            if (!f.exists()) {
                f.mkdirs();
            }

            ImageIO.write(bufferedImage, exportFormat, new File(outputPath + jpgExportPath + fileName + ".jpg"));
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }// End method exportGraph

    public void createXDoc() {
        StringBuilder xdocXML = new StringBuilder();

        xdocXML.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        xdocXML.append("<document>");
        xdocXML.append("<properties>");
        xdocXML.append("<author email=\"nospam.AT.foo.DOT.com\">Jon S. Stevens</author>");
        xdocXML.append("<title></title>");
        xdocXML.append("</properties>");
        xdocXML.append("<meta name=\"keyword\" content=\"jakarta, java\"/>");
        xdocXML.append("<body>");
        xdocXML.append("<section name=\"" + siteTitle + "\">");
        xdocXML.append("<p>" + siteDesc + "</p>");
        xdocXML.append("<table>");
        xdocXML.append("<tr>");
        xdocXML.append("<th> </th>");
        xdocXML.append(htmlTableHeadings);
        xdocXML.append("</tr>");
        xdocXML.append(htmlTableData);
        xdocXML.append("</table>");
        xdocXML.append("</section>");
        xdocXML.append("<img src=\"images/" + fileName + ".jpg\" alt=\"GRAPH\"/>");
        xdocXML.append("</body>");
        xdocXML.append("</document>");

        try {
            File f = new File(outputPath + xdocExportPath);
            if (!f.exists()) {
                f.mkdirs();
            }

            File file = new File(outputPath + xdocExportPath + fileName + ".xml");
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(xdocXML.toString());
            fileWriter.close();
        } catch (IOException e) {
        }

    }// End method createXDoc
}// End class MojoGraph
