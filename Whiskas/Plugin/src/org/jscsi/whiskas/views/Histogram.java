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
 * $Id: Histogram.java 33 2007-05-29 10:13:09Z ross_roy $
 * 
 */

package org.jscsi.whiskas.views;

import java.awt.Button;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.SWT;
import org.eclipse.ui.part.ViewPart;
import org.jscsi.whiskas.Activator;

/**
 * This class creates an histogram for Whiskas.
 * @author Florian Mansmann, Halldór Janetzko
 */

public class Histogram extends ViewPart implements PaintListener,
ControlListener, VisualListener, DisposeListener {
    /**Number of bins.*/
    private int noOfBins = 24;
    /**Colormap for coloring bins according to height.*/
    private ColorMap cm;
    /**Space between bins.*/
    private final int relSpace = 3;
    /**Size of bins.*/
    private final int binSize = 50;
    /**Array of bin values.*/
    private float[] value = new float[0];
    /**Maximum of values.*/
    private float maxValue;
    /**Labels for display of class division.*/
    private String[] label = {"<-200", "-200/-101", "-100/-41", "-40/-16",
            "-15/-6", "-5/-3", "-2", "-1", "0", "1", "2", "3/5", "6/15",
            "16/40", "41/100", "101/200", ">200"};
    /**Labels to paint on bins.*/
    private String[] binLabels;
    /**Title of histogram.*/
    private String title;
    /**Average of values.*/
    private float average = 0f;
    /**Linear scaling of values.*/
    public static int LINEAR = 0;
    /**Logarithmic scaling of values.*/
    public static int LOGARITHMIC = 1;
    /**Type of scaling values for histogram.*/
    private int type = 0;
    /**Control object to pull values from.*/
    private Control ctr;
    /**Parent shell of histogram view.*/
    private Composite topshell;
    /**Display of histogram.*/
    private Display disp;
    /**Canvas to paint histogram on.*/
    private Canvas c;
    /**Bufferimage for double buffering.*/
    private Image buffer;
    /**Composite to aggregate setting widgets.*/
    private Composite preference;
    /**Lists for settings of device, scaling, etc.*/
    private List typ, cumulative, visualizing, device;
    /**Labels for GUI.*/
    private Label lbType, lbAggregation, lbAccessType, lbDevice;
    /**
     * The Constructor of Histogram registers itself at the Activator.
     */
    public Histogram() {
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
        disp = parent.getDisplay();
        cm = new ColorMap(disp);
        RowLayout rl = new RowLayout();
        rl.type = SWT.VERTICAL;
        rl.wrap = false;
        rl.justify = true;
        topshell.setLayout(rl);
        topshell.addDisposeListener(this);
        preference = new Composite(topshell, SWT.BORDER);
        preference.setLayoutData(
                new RowData(topshell.getClientArea().width, 50));
        preference.setLayout(new RowLayout());
        lbDevice = new Label(preference, SWT.READ_ONLY);
        lbDevice.setText("Device: ");
        RowData rowd = new RowData();
        rowd.height = 45;
        rowd.width = 130;
        device = new List(preference, SWT.READ_ONLY | SWT.V_SCROLL);
        device.setLayoutData(rowd);
        lbType = new Label(preference, SWT.SHADOW_IN);
        lbType.setText("Type: ");
        typ = new List(preference, SWT.READ_ONLY);
        typ.setItems(new String[] {"Linear", "Logarithmic"});
        typ.setSelection(0);
        lbAggregation = new Label(preference, SWT.SHADOW_IN);
        lbAggregation.setText("Values are :");
        cumulative = new List(preference, SWT.READ_ONLY);
        cumulative.setItems(new String[] {"accumulated", "not accumulated"});
        cumulative.setSelection(1);
        lbAccessType = new Label(preference, SWT.READ_ONLY);
        lbAccessType.setText("Visualizing: ");
        visualizing = new List(preference, SWT.READ_ONLY);
        visualizing.setItems(new String[] {"Read" , "Write"});
        visualizing.setSelection(0);
        c = new Canvas(topshell, SWT.DOUBLE_BUFFERED | SWT.BORDER);
        c.setLayoutData(new RowData(topshell.getClientArea().width,
                topshell.getClientArea().height - 75));
        c.addPaintListener(this);
        value = new float[noOfBins];
        for (int i = 0; i < noOfBins; i++) {
            value[i] = 0;
        }
        maxValue = 0;
        binLabels = new String[noOfBins];
        for (int i = 0; i < noOfBins; i++) {
            binLabels[i] = Float.toString(value[i]);
        }
    }
    /**
     * Set scaling of values for the histogram.
     * @param newType - new scaling type
     */
    public final void setType(final int newType) {
        this.type = newType;
    }
    /**
     * Returns scaling method for histogram.
     * @return scaling value of Histogram
     */
    public final int getType() {
        return this.type;
    }
    /**
     * Method to set values of the bins.
     * @param values - of the bins
     */
    public final void setValues(final float[] values) {
        if (values == null) {
            return;
        }
        value = values;
        noOfBins = values.length;
        maxValue = 0;
        average = 0;
        for (int i = 0; i < value.length; i++) {
            binLabels[i] = "Area: " + label[i]
                    + "   Number: " + Integer.toString((int) value[i]);
            average += value[i];
            if (maxValue < value[i]) {
                maxValue = value[i];
            }
        }
        average = average / (float) value.length;
        c.redraw();
    }
    /**
     * This method is called whenever repaint is called,
     * then the Pattern will be painted.
     * @param e PaintEvent called by Eclipse
     */
    public final void paintControl(final PaintEvent e) {
        repaintBuffer();
        e.gc.drawImage(buffer, 0, 0);
    }
    /**
     * In this method the histogram is painted on the
     * grafic context.
     * @param g - the grafic context to paint on
     */
    public final void paint(final GC g) {
        g.setBackground(disp.getSystemColor(SWT.COLOR_BLACK));
        g.fillRectangle(0, 0, c.getBounds().width, c.getBounds().height);
        float[] valuesToPaint = this.value;
        float averageToPaint = this.average;
        float maxValueToPaint = this.maxValue;
        int overallSize = noOfBins * binSize;
        if (noOfBins <= 0.5 * c.getBounds().width) {
            overallSize += (noOfBins - 1) * relSpace;
        }
        if (valuesToPaint.length > c.getBounds().width) {
            g.drawString("Aggregated values", 10, 10);
            valuesToPaint = new float[c.getBounds().width];
            averageToPaint = 0f;
            for (int i = 0; i < this.value.length; i++) {
                valuesToPaint[i * (c.getBounds().width - 1)
                              / (this.value.length - 1)] += this.value[i];
                averageToPaint += this.value[i];
            }
            averageToPaint = averageToPaint / valuesToPaint.length;
            overallSize = c.getBounds().width;
        }
        maxValueToPaint = 0;
        for (int i = 0; i < valuesToPaint.length; i++) {
            if (valuesToPaint[i] > maxValueToPaint) {
                maxValueToPaint = valuesToPaint[i];
            }
        }
        if (type == LOGARITHMIC) {
            float min, max;
            float [] newValues = new float[valuesToPaint.length];
            min = 0; max = valuesToPaint[0];
            for (int i = 0; i < valuesToPaint.length; i++) {
                if (valuesToPaint[i] < min) {
                    min = valuesToPaint[i];
                }
                if (valuesToPaint[i] > max) {
                    max = valuesToPaint[i];
                }
            }
            for (int i = 0; i < valuesToPaint.length; i++) {
                newValues[i] = (float) (Math.log(valuesToPaint[i] - min + 1.0)
                        / Math.log(max - min + 1.00000001));
            }
            averageToPaint = (float) (Math.log(averageToPaint - min + 1.0)
                    / Math.log(max - min + 1.00000001));
            valuesToPaint = newValues;
            maxValueToPaint = (float) (Math.log(max - min + 1.0)
                    / Math.log(max - min + 1.00000001));
        }
        float xFactor = (float) c.getBounds().width / (float) overallSize;
        float yFactor = (float) c.getBounds().height / (float) maxValueToPaint;
        int maxLbl = label.length;
        for (int i = 0; i < valuesToPaint.length; i++) {
            int x1;
            if (noOfBins <= 0.5 * c.getBounds().width) {
                x1 = (int) (i * xFactor * (binSize + relSpace));
            } else {
                x1 = (int) (i * xFactor * binSize);
            }
            int width = (int) (binSize * xFactor);
            if (valuesToPaint.length == c.getBounds().width) {
                x1 = i + 1;
                width = 1;
            }
            int height = (int) (valuesToPaint[i] * yFactor);
            int colorIndex = 0;
            try {
                colorIndex = (int) (valuesToPaint[i] / maxValueToPaint
                        * (cm.getColors(8).length - 1));
                g.setBackground(cm.getColors(8)[colorIndex]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            g.fillRectangle(x1, c.getBounds().height
                    - height, width, c.getBounds().height);
            g.setBackground(disp.getSystemColor(SWT.COLOR_WHITE));
            Font f = new Font(disp, "Arial", 14, SWT.NORMAL);
            g.setFont(f);
            if (i < maxLbl) {
                if (g.getFontMetrics().getHeight() < width
                        && c.getBounds().height
                           > g.getFontMetrics().getAverageCharWidth()
                                * binLabels[i].length()) {
                    Transform tr = new Transform(disp);
                    tr.rotate(-90.0f);
                    g.setTransform(tr);
                    Path p = new Path(disp);
                    p.addString(binLabels[i], -c.getBounds().height + 5,
                            x1 + (int) (0.5 * width) - 10, f);
                    g.fillPath(p);
                    tr.rotate(90.0f);
                    g.setTransform(tr);
                    tr.dispose();
                    p.dispose();
                }
                f.dispose();
            }
        }
        g.setForeground(disp.getSystemColor(SWT.COLOR_WHITE));
        if (title != null) {
            g.drawString(title, 2, 20);
        }
        g.setForeground(disp.getSystemColor(SWT.COLOR_MAGENTA));
        g.drawLine(0, c.getBounds().height - (int) (averageToPaint * yFactor),
                c.getBounds().width, c.getBounds().height
                   - (int) (averageToPaint * yFactor));

        g.setForeground(disp.getSystemColor(SWT.COLOR_WHITE));
        g.drawLine(0, 0, c.getBounds().width, 0);
        g.drawLine(0, 0, 0, c.getBounds().height);
    }
    /**
     * Method which is not used by Histogram.
     * @param e ControlEvent
     */
    public void controlMoved(final ControlEvent e) { }
    /**
     * Method which orders repainting of the histogram.
     * @param e ControlEvent
     */
    public final void controlResized(final ControlEvent e) {
        preference.setLayoutData(
               new RowData(topshell.getClientArea().width - 10, 60));
        c.setLayoutData(new RowData(topshell.getClientArea().width - 10,
                topshell.getClientArea().height - 80));
        c.redraw();
    }
    /**
     * This method is called whenever the buffer has
     * to be repainted.
     */
    public final void repaintBuffer() {
        if (buffer != null) {
            buffer.dispose();
        }
        buffer = new Image(disp, c.getBounds().width, c.getBounds().height);
        GC bufferGC = new GC(buffer);
        paint(bufferGC);
    }
    /**
     * The Control object has every values to draw, here they are
     * pulled from the matching hashtable of Control.
     */
    public final void getNewValues() {
        this.ctr = Activator.getDefault().c;
        if (ctr.getDaten() != null) {
            String newVisualizing =
                visualizing.getItem(visualizing.getSelectionIndex());
            String newCumulative =
                cumulative.getItem(cumulative.getSelectionIndex());
            String newDevice = "";
            if (device.getItemCount() > 0 && device.getSelectionIndex() != -1) {
                newDevice = device.getItem(device.getSelectionIndex());
                float[] val = null;
                if (newCumulative.equals("accumulated")) {
                    if (newVisualizing.equals("Read")) {
                        val = ctr.getDaten().
                             getValuesHistAccumulatedR().get(newDevice);
                    }
                    if (newVisualizing.equals("Write")) {
                        val = ctr.getDaten().
                             getValuesHistAccumulatedW().get(newDevice);
                    }
                } else {
                    if (newVisualizing.equals("Read")) {
                        val = ctr.getDaten().
                             getValuesHistNotAccumulatedR().get(newDevice);
                    }
                    if (newVisualizing.equals("Write")) {
                        val = ctr.getDaten().
                             getValuesHistNotAccumulatedW().get(newDevice);
                    }
                }
                if (val == null) {
                    return;
                } else {
                    setValues(val);
                }
            }
            newVisualizing = typ.getItem(typ.getSelectionIndex());
            if (newVisualizing.equals("Linear")) {
                type = Histogram.LINEAR;
            } else {
                type = Histogram.LOGARITHMIC;
            }
            String[] keys = ctr.getDaten().
                      getValuesPatW().keySet().toArray(new String[0]);
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
    }
    /**
     * Implementation of Interface VisualListener.
     * @return Composite: parent shell of pattern view
     */
    public final Composite getComposite() {
        return topshell;
    }
    /**
     * When the histogram view is closed the Activator object
     * has to be informed and the histogram object will be
     * removed from Activator's list of visualizer.
     * @param e - DisposeEvent from Eclipse
     */
    public final void widgetDisposed(final DisposeEvent e) {
        c.dispose();
        buffer.dispose();
        lbType.dispose();
        lbAggregation.dispose();
        lbAccessType.dispose();
        cumulative.dispose();
        typ.dispose();
        visualizing.dispose();
        preference.dispose();
        Activator.getDefault().list_of_visualizer.remove(this);
    }
    /**
     * Implements setFocus from Workbench (not used).
     */
    public void setFocus() { }
}