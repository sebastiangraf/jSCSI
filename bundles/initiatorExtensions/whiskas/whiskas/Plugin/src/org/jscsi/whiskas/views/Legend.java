/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.whiskas.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/** This class provides a legend for the visualizations.
 * @author Halldór Janetzko
 */
public class Legend extends ViewPart implements ControlListener {
    /**Colormap for Visualization.*/
    private ColorMap cm;
    /**Parent shell of view.*/
    private Composite topshell;
    /**Canvas for painting of labels and legend.*/
    private Canvas c, c2;
    /**
     * Method which is always executed by the Activator class
     * and builds the GUI.
     * @param parent the parent of the Pattern view.
     */
    public final void createPartControl(final Composite parent) {
        cm = new ColorMap(parent.getDisplay());
        this.topshell = parent;
        c = new Canvas(parent, SWT.BORDER | SWT.DOUBLE_BUFFERED);
        c.addPaintListener(new PaintListener()
        {
            public void paintControl(final PaintEvent e) {
                paintLabel(e.gc);
            }
        });
        c2 = new Canvas(parent, SWT.BORDER | SWT.DOUBLE_BUFFERED);
        c2.addPaintListener(new PaintListener()
        {
            public void paintControl(final PaintEvent e) {
                paintPattern(e.gc);
            }
        });
        parent.addControlListener(this);
        RowLayout rl = new RowLayout();
        rl.type = SWT.VERTICAL;
        rl.wrap = false;
        rl.justify = true;
        parent.setLayout(rl);
    }
    /**
     * Implements WorkbenchPart.setFocus (not used).
     */
    public void setFocus() { }
    /**
     * Method to paint labels.
     * @param gc - the grafic context to paint on
     */
    public final void paintLabel(final GC gc) {
        gc.setForeground(topshell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        gc.drawString("untouched", 125, 1);
        gc.drawLine(125, 30, 140, 15);
        gc.drawString("touched", 165, 15);
        gc.drawLine(140, 30, 160, 25);
        gc.drawString("recently touched (few touches)", 205, 1);
        gc.drawString("recently touched (many touches)",
                c.getClientArea().width
                   - gc.getFontMetrics().getAverageCharWidth()
                * "recently touched (many touches)".length(), 8);
    }
    /**
     * Method to paint patterns of different types.
     * @param gc - grafic context to paint on
     */
    public final void paintPattern(final GC gc) {
        int height = c2.getClientArea().height / (cm.color.length - 1);
        String[] s = {"Free Block", "Root Block", "Positional BTree Block",
                "Keyed Trie Block", "Keyed BTree Block",
                "Node Block", "Name Block", "Value Block",
                "Histogram", "Unknown Block"};
        for (int i = 0; i < cm.color.length - 1; i++) {
            int width = (c2.getClientArea().width - 120) / (cm.color[i].length);
            for (int j = 0; j < cm.color[i].length; j++) {
                gc.setBackground(cm.color[i][j]);
                gc.fillRectangle(j * width + 120, i * height, width, height);
                int xPos = j * width + 120;
                int yPos = i * height;
                if (j == 0 && i != 8) {
                    gc.setBackground(cm.color[i][99]);
                    gc.fillRectangle((int) (xPos + 0.5 * width
                                       - 0.3 * width / 2.0),
                            (int) (yPos + 0.5 * height - 0.3 * height / 2.0),
                            (int) (0.3 * width) + 1,
                            (int) (0.3 * height) + 1);
                }
                if (j == 1 && i != 8) {
                    gc.setBackground(cm.color[i][99]);
                    gc.fillRectangle(xPos, yPos, width, height);
                    gc.setBackground(cm.color[i][j]);
                    gc.fillRectangle((int) (xPos + 0.5 * width 
                                        - 0.3 * width / 2.0),
                            (int) (yPos + 0.5 * height - 0.3 * height / 2.0),
                            (int) (0.3 * width) + 1,
                            (int) (0.3 * height) + 1);
                }
                if (j != 0 && j !=1 && i != 8) {
                    gc.setBackground(cm.color[i][99]);
                    int[] points = new int[6];
                    points[0] = xPos;
                    points[2] = xPos + width;
                    points[4] = xPos + width;
                    points[1] = yPos;
                    points[3] = yPos;
                    points[5] = yPos + height;
                    gc.fillPolygon(points);
                }
                gc.setForeground(
                        topshell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
                gc.setBackground(
                        topshell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
                gc.drawString(s[i], 2, yPos);
            }
        }
    }
    /**
     * Method which is not used by Legend.
     * @param e ControlEvent
     */
    public void controlMoved(final ControlEvent e) { }
    /**
     * Method which orders repainting of the legend.
     * @param e ControlEvent
     */
    public final void controlResized(final ControlEvent e) {
        c2.setLayoutData(new RowData(topshell.getClientArea().width - 10,
                topshell.getClientArea().height - 50));
        c2.redraw();
        c.setLayoutData(new RowData(topshell.getClientArea().width - 10, 30));
        c.redraw();
    }
}
