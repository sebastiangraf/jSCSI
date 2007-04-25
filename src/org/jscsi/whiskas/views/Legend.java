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
 * $Id$
 * 
 */

package org.jscsi.whiskas.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

public class Legend extends ViewPart implements ControlListener
{
	private ColorMap cm;
	private Composite parent;
	private Canvas c, c2;
	public void createPartControl(Composite parent) 
	{
		cm = new ColorMap(parent.getDisplay());
		this.parent = parent;
		c = new Canvas(parent, SWT.BORDER|SWT.DOUBLE_BUFFERED);
		c.addPaintListener(new PaintListener() 
		{
			public void paintControl(PaintEvent e) 
			{
				paint_label(e.gc);
			}
		});
		c2 = new Canvas(parent, SWT.BORDER|SWT.DOUBLE_BUFFERED);
		c2.addPaintListener(new PaintListener() 
		{
			public void paintControl(PaintEvent e) 
			{
				paint_pattern(e.gc);			
			}
		});
		parent.addControlListener(this);
		RowLayout rl = new RowLayout();
		rl.type = SWT.VERTICAL;
		rl.wrap = false;
		rl.justify = true;
		parent.setLayout(rl);
	}
	public void setFocus() {}
	public void paint_label(GC gc)
	{
		gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		gc.drawString("untouched", 125, 1);
		gc.drawLine(125, 30, 140, 15);
		gc.drawString("touched", 165, 15);
		gc.drawLine(140, 30, 160, 25);
		gc.drawString("recently touched (few touches)", 205,1);
		gc.drawString("recently touched (many touches)",c.getClientArea().width-gc.getFontMetrics().getAverageCharWidth()*
				"recently touched (many touches)".length(),8);
	}
	public void paint_pattern(GC gc)
	{
		int height = c2.getClientArea().height/(cm.color.length-1);
		System.out.println(c2.getClientArea());
		String[] s = {"Free Block","Root Block","Positional BTree Block",
				"Keyed Trie Block", "Keyed BTree Block", 
				"Node Block", "Name Block", "Value Block", "Histogram", "Unknown Block"};
		for (int i = 0; i<cm.color.length-1; i++)
		{
			int width = (c2.getClientArea().width-120)/(cm.color[i].length);
			for (int j=0; j<cm.color[i].length;j++)
			{
				gc.setBackground(cm.color[i][j]);
				gc.fillRectangle(j*width+120, i*height, width, height);
				int x_pos = j*width+120;
				int y_pos =i*height;
				if (j==0&&i!=8)
				{
					gc.setBackground(cm.color[i][99]);
					gc.fillRectangle((int) (x_pos + 0.5 * width - 0.3 * width / 2.0),
							(int) (y_pos + 0.5 * height - 0.3 * height / 2.0),
							(int) (0.3 * width)+1,
							(int) (0.3 * height)+1);
				}
				if (j==1&&i!=8)
				{
					gc.setBackground(cm.color[i][99]);
					gc.fillRectangle(x_pos, y_pos, width, height);
					gc.setBackground(cm.color[i][j]);
					gc.fillRectangle((int) (x_pos + 0.5 * width - 0.3 * width / 2.0),
							(int) (y_pos + 0.5 * height - 0.3 * height / 2.0),
							(int) (0.3 * width)+1,
							(int) (0.3 * height)+1);
				}
				if (j!=0&&j!=1&&i!=8)
				{
					gc.setBackground(cm.color[i][99]);
					int[] points = new int[6];
					points[0]=x_pos;
					points[2]=x_pos+width;
					points[4]=x_pos+width;
					points[1]=y_pos;
					points[3]=y_pos;
					points[5]=y_pos+height;
					gc.fillPolygon(points);
				}
				gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				gc.drawString(s[i], 2, y_pos);
			}
		}
	}
	public void controlMoved(ControlEvent e) {}
	public void controlResized(ControlEvent e) 
	{
		c2.setLayoutData(new RowData(parent.getClientArea().width-10, parent.getClientArea().height-50));
		c2.redraw();
		c.setLayoutData(new RowData(parent.getClientArea().width-10, 30));
		c.redraw();
	}
}
