/*
 * Copyright 2007 University of Konstanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: Pattern.java 33 2007-05-29 10:13:09Z ross_roy $
 * 
 */

package org.jscsi.whiskas.views;


import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.SWT;
import org.eclipse.ui.part.ViewPart;
import org.jscsi.whiskas.Activator;

/**
 * This clas provides a view of block access patterns on a jSCSI device. It is
 * part of the Whiskas Plugin and is therefore instantiated by the
 * Whiskas Control Center.
 * @author Halldór Janetzko
 */
public class Pattern extends ViewPart implements ControlListener, PaintListener,
VisualListener, DisposeListener, SelectionListener, MouseMoveListener {
    /**Reference of supervising Control center.*/
    private Control ctr;
    /** Map of color for rendering.*/
    private ColorMap cm;
    /**reference object of the top-composite.*/
    private Composite topshell;
    /**canvas to paint Pattern on.*/
    private Canvas c;
    /**image buffer for double buffering.*/
    private Image buffer;
    /**Composites for settings an detail view.*/
    private Composite preference, details;
    /**List of access (read/write) for pattern visualisation.*/
    private List visualizing;
    /**List of devices for pattern visualisation.*/
    private List device;
    /**Label for GUI.*/
    private Label lbDevice, lbDetails;
    /**Button for resetting Touch History.*/
    private Button btResetHistory;
    /**internal memory of number of rows.*/
    private int lines;
    /**internal memory of number of colums.*/
    private int colums;
    /**Counter for devision of Canvas.*/
    private int counter;
    /**Boolean to remember if it is the first time of painting,
     * if it is the canvas has to be devided.*/
    private boolean first = true;
    /**Arrays to hold values for each rectangle.*/
    private int[] xPos, yPos, widths, heights;
    /**Arrays for blocktype and hits.*/
    private byte[] types, values;
    /**Array to save if block has never been touched.*/
    private boolean[] neverTouched;
    /**internal values for difference of overall block width(height)
     *  and screen width(height).*/
    private int xDiff, yDiff;
    /**Strings to detect changes of settings.*/
    private String lastVis, lastDev = "";
    /**Spinners for minimum and maximum block to display.*/
    private Spinner min, max;
    /**Thickness of border in pixel.*/
    private int border = 5;
    /**Variables to store last max and min value of spinner
     * to notice changes.*/
    private int lastMinNo = 0, lastMaxNo = 0;
    /**
     * The Constructor of Pattern initializes values for drawing and
     * it registers itself at the Activator.
     */
    public Pattern() {
        lines = 1;
        colums = 1;
        Activator.getDefault().list_of_visualizer.add(this);
    }
    /**
     * Method which is always executed by the Activator class
     * and builds the GUI.
     * @param parent the parent of the Pattern view.
     */
    public final void createPartControl(final Composite parent) {
        topshell = parent;
        topshell.addControlListener(this);
        cm = new ColorMap(topshell.getDisplay());
        RowLayout rl = new RowLayout();
        rl.type = SWT.VERTICAL;
        rl.wrap = false;
        rl.justify = true;
        topshell.setLayout(rl);
        preference = new Composite(topshell, SWT.BORDER);
        preference.setLayoutData(new RowData(
                topshell.getClientArea().width, 50));
        RowLayout rl2 = new RowLayout();
        rl2.spacing = 10;
        preference.setLayout(rl2);
        lbDevice = new Label(preference, SWT.READ_ONLY);
        lbDevice.setText("Device: ");
        RowData rowd = new RowData();
        rowd.height = 45;
        rowd.width = 130;
        device = new List(preference, SWT.SINGLE | SWT.V_SCROLL);
        device.setLayoutData(rowd);
        visualizing = new List(preference, SWT.SINGLE);
        visualizing.setItems(new String[] {"Read", "Write"});
        visualizing.setSelection(0);
        lastVis = "Read";
        Label lbmin = new Label(preference,SWT.NONE);
        lbmin.setText("Min Blockno.");
        min = new Spinner(preference,SWT.NONE);
        min.setMinimum(0);
        min.setMaximum(1000000);
        Label lbmax = new Label(preference,SWT.NONE);
        lbmax.setText("Max Blockno.");
        max = new Spinner(preference,SWT.NONE);
        max.setMinimum(0);
        max.setMaximum(1000000);
        btResetHistory = new Button(preference, SWT.PUSH);
        btResetHistory.setText("Reset Touch History");
        btResetHistory.addSelectionListener(this);
        details = new Composite(preference, SWT.BORDER);
        details.setLayout(new FillLayout());
        lbDetails = new Label(details, SWT.NONE);
        lbDetails.setText("Details: N/A");
        preference.pack();
        c = new Canvas(topshell, SWT.DOUBLE_BUFFERED | SWT.BORDER);
        c.setLayoutData(new RowData(topshell.getClientArea().width,
                topshell.getClientArea().height - 150));
        c.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        c.addPaintListener(this);
        c.addMouseMoveListener(this);
        xPos = new int[lines * colums];
        yPos = new int[lines * colums];
        widths = new int[lines * colums];
        heights = new int[lines * colums];
        types = new byte[lines * colums];
        values = new byte[lines * colums];
        neverTouched = new boolean[lines * colums];
        for (int i = 0; i < lines * colums; i++) {
            types[i] = 5;
            values[i] = -128;
            neverTouched[i] = true;
        }
        topshell.addDisposeListener(this);
    }
    /**
     * Method to calculate position and measures of each block
     * representing rectangle.
     */
    public final void calcGrid() {
        counter = 0;
        int widthFactor = (c.getBounds().width - border) / colums;
        int heightFactor = (c.getBounds().height - border) / lines;
        int width = ((c.getBounds().width - border) / colums) * colums;
        int height = ((c.getBounds().height - border) / lines) * lines;
        xDiff = (c.getBounds().width - border) - width;
        yDiff = (c.getBounds().height - border) - height;
        int remainingYDiff = this.yDiff;
        for (int x = 0; x < lines; x++) {
            if (x % 2 == 0) {
                if (remainingYDiff == 0) {
                    divide(0, widthFactor, colums,
                            x * heightFactor + this.yDiff, heightFactor);
                } else {
                    divide(0, widthFactor, colums,
                            x * heightFactor + this.yDiff - remainingYDiff,
                            heightFactor + 1);
                    remainingYDiff = remainingYDiff - 1;
                }
            }
            if (x % 2 == 1) {
                if (remainingYDiff == 0) {
                    divide((c.getBounds().width - border), widthFactor,
                            colums, x * heightFactor + this.yDiff,
                            heightFactor);
                } else {
                    divide((c.getBounds().width - border), widthFactor,
                            colums, x * heightFactor + this.yDiff
                            - remainingYDiff,
                            heightFactor + 1);
                    remainingYDiff = remainingYDiff - 1;
                }
            }
        }
    }
    /**
     * Method to devide one line into <i>number</i> rectangles.
     * @param xBegin xPos of the begin of the line
     * @param widthFactor factor calculated by width devided by
     *  number of rectangles in one line
     * @param number of rectangles to place in one line
     * @param y yPos of the line
     * @param height of the line to devide
     */
    private void divide(final int xBegin, final int widthFactor,
            final int number, final int y, final int height) {
        int remainingXDiff = this.xDiff;
        if (xBegin != 0) {
            remainingXDiff = 0;
        }
        for (int x = 1; x <= number; x++) {
            if (xBegin > 0) {
                if (x < number - this.xDiff + 1) {
                    xPos[counter] = xBegin - x * widthFactor;
                    yPos[counter] = y;
                    widths[counter] = widthFactor;
                    heights[counter] = height;
                } else {
                    remainingXDiff = remainingXDiff + 1;
                    xPos[counter] = xBegin - x * widthFactor - remainingXDiff;
                    yPos[counter] = y;
                    widths[counter] = widthFactor + 1;
                    heights[counter] = height;
                }
            }
            if (xBegin == 0) {
                if (remainingXDiff == 0) {
                    xPos[counter] = xBegin + (x - 1) * widthFactor
                        + this.xDiff;
                    yPos[counter] = y;
                    widths[counter] = widthFactor;
                    heights[counter] = height;
                } else {
                    xPos[counter] = xBegin + (x - 1) * widthFactor
                        + this.xDiff - remainingXDiff;
                    yPos[counter] = y;
                    widths[counter] = widthFactor + 1;
                    heights[counter] = height;
                    remainingXDiff = remainingXDiff - 1;
                }
            }
            if (counter < xPos.length - 1) {
                counter++;
            }
            if (counter >= xPos.length) {
                System.out.println(counter + "  " + xPos.length);
            }
        }
    }
    /**
     * Method which is not used by Pattern.
     * @param e ControlEvent
     */
    public void controlMoved(final ControlEvent e) { }
    /**
     * Method which orders recalculation of Grid if Frame is resized.
     * @param e ControlEvent
     */
    public final void controlResized(final ControlEvent e) {
        preference.setLayoutData(new RowData(
                topshell.getClientArea().width - 10, 60));
        c.setLayoutData(new RowData(
                topshell.getClientArea().width - 10,
                topshell.getClientArea().height - 80));
        first = true;
        c.redraw();
    }
    /**
     * This method is called whenever repaint is called,
     * then the Pattern will be painted.
     * @param e PaintEvent called by Eclipse
     */
    public final void paintControl(final PaintEvent e) {
        if (first)  {
            calcGrid();
            first = false;
        }
        repaintBuffer();
        e.gc.drawImage(buffer, 0, 0);
    }
    /**
     * Repaints buffer with new Pattern, the buffer will be painted
     * by paintControl.
     */
    public final void repaintBuffer() {
        if (buffer != null) {
            buffer.dispose();
        }
        buffer = new Image(topshell.getDisplay(),
                c.getBounds().width, c.getBounds().height);
        GC bufferGC = new GC(buffer);
        paint(bufferGC);
        bufferGC.dispose();
    }
    /**
     * Here is the painting implemented. Each rectangle will be painted
     * one after the other.
     * @param g is the GraphicContext
     */
    public final void paint(final GC g) {
        for (int i = 0; i < colums * lines; i++) {
            if (neverTouched[i]) {
                try {
                    g.setBackground(cm.getColors(types[i])[0]);
                } catch (Exception e) {
                    System.out.println(types.length);
                }
                g.fillRectangle(xPos[i], yPos[i], widths[i], heights[i]);
                g.setBackground(cm.getColors(types[i])[99]);
                g.fillRectangle((int) (xPos[i] + 0.5 * widths[i] -
                                0.3 * widths[i] / 2.0),
                        (int) (yPos[i] + 0.5 * heights[i] -
                                0.3 * heights[i] / 2.0),
                        (int) (0.3 * widths[i]) + 1,
                        (int) (0.3 * heights[i]) + 1);
            }
            if (!neverTouched[i] && values != null && values[i] > -128) {
                int cIndex;
                if (values != null) {
                    cIndex = (int) ((float) ((values[i]) + 128) / (256) * 99.0);
                } else {
                    cIndex = (int) ((float) (-128 + 128) / (256) * 99.0);
                }
                g.setBackground(cm.getColors(types[i])[cIndex]);
                g.fillRectangle(xPos[i], yPos[i], widths[i], heights[i]);
                g.setBackground(cm.getColors(types[i])[99]);
                int[] points = new int[6];
                points[0] = xPos[i];
                points[2] = xPos[i] + widths[i];
                points[4] = xPos[i] + widths[i];
                points[1] = yPos[i];
                points[3] = yPos[i];
                points[5] = yPos[i] + heights[i];
                g.fillPolygon(points);
            }
            if (!neverTouched[i] && values != null && values[i] == -128) {
                g.setBackground(cm.getColors(types[i])[99]);
                g.fillRectangle(xPos[i], yPos[i], widths[i], heights[i]);
                g.setBackground(cm.getColors(types[i])[0]);
                g.fillRectangle((int) (xPos[i] + 0.5 * widths[i] -
                                0.3 * widths[i] / 2.0),
                        (int) (yPos[i] + 0.5 * heights[i] -
                                0.3 * heights[i] / 2.0),
                        (int) (0.3 * widths[i]) + 1,
                        (int) (0.3 * heights[i]) + 1);
            }
        }
    }
    /**
     * The Control object has every values to draw, here they are
     * pulled from the matching hashtable.
     */
    public final void getNewValues() {
        this.ctr = Activator.getDefault().ctr;
        if (ctr.getDaten() == null) {
            return;
        }
        String newVisualizing = visualizing.getItem(
                visualizing.getSelectionIndex());
        String newDevice = "";
        if (device.getItemCount() > 0 && device.getSelectionIndex() != -1) {
            newDevice = device.getItem(device.getSelectionIndex());
            byte[] newValues = new byte[0];
            if (newVisualizing.equals("Read")) {
                newValues = ctr.getDaten().getValuesPatR().get(newDevice);
            }
            if (newVisualizing.equals("Write")) {
                newValues = ctr.getDaten().getValuesPatW().get(newDevice);
            }
            if (!newVisualizing.equals(lastVis) || !lastDev.equals(newDevice)) {
                lastVis = newVisualizing;
                lastDev = newDevice;
                resetNeverTouched();
            }
            if (newValues == null) {
                return;
            }
            int maxNo = max.getSelection(), minNo = min.getSelection();
            if (maxNo > newValues.length) {
                maxNo = newValues.length;
            }
            if (maxNo >= minNo && maxNo > 0) {
                byte[] processedNewValues = new byte[(int) Math.pow(
                        (int) Math.sqrt(maxNo - minNo) + 1, 2)];
                for (int i = 0; i < processedNewValues.length; i++) {
                    processedNewValues[i] = newValues[i + minNo];
                }
                newValues = processedNewValues;
            }
            if (maxNo != lastMaxNo || minNo != lastMinNo) {
                resetNeverTouched();
            }
            lastMaxNo = maxNo;
            lastMinNo = minNo;
            int newLines = (int) Math.sqrt(newValues.length) + 1;
            int newColums = (int) Math.sqrt(newValues.length) + 1;
            if (newLines != lines || newColums != colums) {
                xPos = new int[newLines * newColums];
                yPos = new int[newLines * newColums];
                widths = new int[newLines * newColums];
                heights = new int[newLines * newColums];
                boolean[] newNeverTouched = new boolean[newLines * newColums];
                types = new byte[newLines * newColums];
                for (int i = 0; i < newLines * newColums; i++) {
                    if (i < lines * colums) {
                        newNeverTouched[i] = neverTouched[i];
                    } else {
                        newNeverTouched[i] = true;
                    }
                    if (ctr.getDaten().getTypes() != null
                            && ctr.getDaten().getTypes().length > i) {
                        types[i] = ctr.getDaten().getTypes()[i];
                    }
                    if (ctr.getDaten().getTypes() == null) {
                        types[i] = 5;
                    }
                    if (ctr.getDaten().getTypes() != null
                            && ctr.getDaten().getTypes().length < i) {
                        types[i] = 9;
                    }
                }
                lines = newLines;
                colums = newColums;
                neverTouched = newNeverTouched;
                calcGrid();
            }
            values = new byte[lines * colums];
            for (int i = 0; i < newValues.length; i++) {
                values[i] = newValues[i];
                if (values != null && values[i] >= -127) {
                    neverTouched[i] = false;
                }
            }
            for (int i = newValues.length; i < lines * colums; i++) {
                values[i] = -128;
            }
            repaintBuffer();
            c.redraw();
        }
        String[] keys = ctr.getDaten().
                  getValuesPatR().keySet().toArray(new String[0]);
        boolean equal = keys.length == device.getItemCount();
        for (int i = 0; i < device.getItemCount(); i++) {
            equal = equal && device.getItem(i).equals(keys[i]);
        }
        if (!equal || device.getItemCount() == 0) {
            device.setItems(keys);
            if (!newDevice.equals("")) {
                for (int i = 0; i < device.getItemCount(); i++) {
                    if (device.getItem(i).equals(newDevice)) {
                        device.setSelection(i);
                    }
                }
            }
        }
    }
    /**
     * Implementation of Interface VisualListener.
     * @return Composite: parent shell of pattern view
     */
    public final Composite getComposite() {
        return topshell;
    }
    /**
     * When the pattern view is closed the Activator object
     * has to be informed and the pattern object will be
     * removed from Activator's list of visualizer.
     * @param e - DisposeEvent from Eclipse
     */
    public final void widgetDisposed(final DisposeEvent e) {
        c.dispose();
        buffer.dispose();
        visualizing.dispose();
        lbDevice.dispose();
        btResetHistory.dispose();
        lbDetails.dispose();
        details.dispose();
        preference.dispose();
        Activator.getDefault().list_of_visualizer.remove(this);
    }
    /**
     * Implementation of SelectionListener (method not used).
     * @param e - SelectionEvent
     */
    public void widgetDefaultSelected(final SelectionEvent e) { }
    /**
     * Implementation of SelectionListener (used for button
     * "Reset Touch History").
     * @param e - SelectionEvent
     */
    public final void widgetSelected(final SelectionEvent e) {
        if (e.widget.toString().contains("Reset Touch History")) {
            resetNeverTouched();
        }
    }
    /**
     * This method is called when touch history is to
     * be resetted (by user or be realignment of blocks).
     */
    public final void resetNeverTouched() {
        for (int i = 0; i < colums * lines; i++) {
            neverTouched[i] = true;
        }
    }
    /**
     * Implements MouseMoveListener (used for determination
     * of block on which mouse points).
     * @param e - MouseEvent
     */
    public final void mouseMove(final MouseEvent e) {
        int x = e.x;
        int y = e.y;
        boolean gefunden = false;
        for (int i = 0; i < colums * lines && !gefunden; i++) {
            if ((x >= xPos[i]) && (x <= xPos[i] + widths[i])
                    && (y >= yPos[i]) && (y <= yPos[i] + heights[i])) {
                gefunden = true;
                String[] s = {"Free Block", "Root Block",
                        "Positional BTree Block", "Keyed Trie Block",
                        "Keyed BTree Block", "Node Block", "Name Block",
                        "Value Block", "Histogram", "Unknown Block"};
                if (ctr.getDaten().getTypes() != null) {
                    lbDetails.setText("Details:\nTouches: " + values[i]
                                        + "\tPos: " + (i + min.getSelection())
                                        + "\n" + s[types[i]]);
                } else {
                    lbDetails.setText("Details:\nTouches: " + values[i]
                                               + "\tPos: " + (i + min.getSelection()));
                }
                details.pack();
            }
        }
        if (!gefunden) {
            lbDetails.setText("Details: N/A");
            details.pack();
        }
    }
    /**
     * Implements setFocus from Workbench (not used).
     */
    public void setFocus() { }
}
