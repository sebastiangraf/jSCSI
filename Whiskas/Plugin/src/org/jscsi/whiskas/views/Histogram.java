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
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.part.ViewPart;
import org.jscsi.whiskas.Activator;


public class Histogram extends ViewPart implements PaintListener, ControlListener, Idefix_Fetch_Stick,
	DisposeListener
{
	private int noOfBins = 24;
	ColorMap cm;
	private int relSpace = 3;
	private int binSize = 50;
	private float[] value = new float[0];
	private float maxValue;
	private String[] label = {"<-200","-200/-101","-100/-41","-40/-16","-15/-6","-5/-3","-2","-1","0",
		"1","2","3/5","6/15","16/40","41/100","101/200",">200"};
	private String[] label_anzeige;
	private String title;
	private float average = 0f;
	public static int LINEAR = 0;
	public static int LOGARITHMIC = 1;
	private int type =0;
	private Control ctr;
	Composite topshell;
	Display disp;
	Canvas c;
	Image buffer;
	Composite preference;
	List typ, cumulative, visualizing,device;
	Label lb1, lb2, lb3, lb4;
	Button bt;
	public Histogram()
	{
		Activator.getDefault().list_of_visualizer.add(this);
	}
	public void createPartControl(Composite parent) 
	{
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
		preference = new Composite(topshell,SWT.BORDER);
		preference.setLayoutData(new RowData(topshell.getClientArea().width,50));
		preference.setLayout (new RowLayout());
    lb4 = new Label(preference,SWT.READ_ONLY);
    lb4.setText("Device: ");
    RowData rowd = new RowData();
    rowd.height=45;
    rowd.width=130;
    device = new List(preference, SWT.READ_ONLY|SWT.V_SCROLL);
    device.setLayoutData(rowd);
		lb1 = new Label(preference, SWT.SHADOW_IN);
		lb1.setText("Type: ");
		GridData data = new GridData ();
		//lb1.setLayoutData(data);
		data.horizontalAlignment = GridData.END;
		data.grabExcessHorizontalSpace = true;
		typ = new List(preference, SWT.READ_ONLY);
		typ.setItems(new String[] {"Linear","Logarithmic"});
		typ.setSelection(0);
		//typ.setLayoutData(data);
		lb2 = new Label(preference, SWT.SHADOW_IN);
		lb2.setText("Values are :");
		//lb2.setLayoutData(data);
		cumulative = new List(preference, SWT.READ_ONLY);
		cumulative.setItems(new String[] {"accumulated","not accumulated"});
		cumulative.setSelection(1);
		data.minimumWidth=100;
		//cumulative.setLayoutData(data);
		lb3 = new Label(preference, SWT.READ_ONLY);
		lb3.setText("Visualizing: ");
		//lb3.setLayoutData(data);
		visualizing = new List(preference, SWT.READ_ONLY);
		visualizing.setItems(new String[] {"Read" , "Write"});
		visualizing.setSelection(0);
		//visualizing.setLayoutData(data);
		//preference.pack();
		c = new Canvas(topshell,SWT.DOUBLE_BUFFERED|SWT.BORDER);
		c.setLayoutData(new RowData(topshell.getClientArea().width, topshell.getClientArea().height-75));
		c.addPaintListener(this);
		value = new float[noOfBins];
		for (int i=0; i<noOfBins; i++) {
			value[i] = 0;
		}
		maxValue = 0;
		label_anzeige = new String[noOfBins];
		for (int i=0; i<noOfBins; i++) {
			label_anzeige[i] = Float.toString(value[i]);
		}
		//topshell.addDisposeListener(this);
	}
	
	public void setType(int type) {
		this.type = type;
	}

	public int getType()
	{
		return this.type;
	}

	public void setValues(float[] values)
	{
		if (values==null)
			return;
		value = values;
		noOfBins = values.length;
		maxValue = 0;
		average = 0;
		for (int i=0;i<value.length;i++) {
			//if (value[i]!=0)
				//value[i]++;
			label_anzeige[i] = "Area: "+label[i]+"   Number: "+Integer.toString((int)value[i]);
			average += value[i];
			if (maxValue < value[i])
				maxValue = value[i];
		}
		average = average / (float) value.length;
		c.redraw();
	}
	public void setValues(int[] values)
	{
		float [] value1 = new float[values.length];
		maxValue = 0;
		for (int i=0; i<values.length; i++) {
			value1[i] = values[i];
		}
		setValues(value1);
	}
	public void paintControl(PaintEvent e) 
	{
		repaint_buffer();
		e.gc.drawImage(buffer, 0, 0);
	}
	public void paint(GC g){
		g.setBackground(disp.getSystemColor(SWT.COLOR_BLACK));
		g.fillRectangle(0,0,c.getBounds().width,c.getBounds().height);
		float [] value = this.value;
		float average = this.average;
		float maxValue = this.maxValue;
		int overallSize = noOfBins * binSize + 
				(noOfBins<=0.5*c.getBounds().width?(noOfBins-1)* relSpace:0);
		if (value.length > c.getBounds().width) 
		{
			g.drawString ("Aggregated values",10,10);
			value = new float[c.getBounds().width];
			average = 0f;
			for (int i=0; i<this.value.length;i++) 
			{
				value[i*(c.getBounds().width-1)/(this.value.length-1)] += this.value[i];
				average += this.value[i]; 
			}
			average = average / value.length;
			overallSize = c.getBounds().width;
		}
		maxValue = 0;
		for (int i=0; i<value.length; i++)
			maxValue = (value[i]>maxValue?value[i]:maxValue);

		// scale values
		if (type == LOGARITHMIC) {
			float min, max;
			float [] newValues = new float[value.length];
			min = 0; max = value[0];
			for (int i=0; i<value.length; i++) {
				min = (value[i]<min?value[i]:min);
				max = (value[i]>max?value[i]:max);
			}
			for (int i=0; i<value.length; i++) 
				newValues[i] = (float) (Math.log(value[i] - min +1.0) / Math.log(max - min +1.00000001));
			average = (float) (Math.log(average - min +1.0) / Math.log(max - min +1.00000001));

			value = newValues;
			maxValue = (float) (Math.log(max - min +1.0) / Math.log(max - min +1.00000001));;
		}

		float xFactor = (float) c.getBounds().width /(float) overallSize;
		float yFactor = (float) c.getBounds().height / (float) maxValue;
		int maxLbl = label.length;

		for (int i=0; i < value.length; i++) 
		{
			int x1 = (int) (i*xFactor*(binSize+(noOfBins<=0.5*c.getBounds().width?relSpace:0)));
			int width = (int) (binSize*xFactor);
			if (value.length == c.getBounds().width) {
				x1 = i+1;
				width = 1;
			}
			int height = (int) (value[i]*yFactor);
			int colorIndex=0;
			try
			{
				colorIndex = (int) (value[i] / maxValue * (cm.getColors(8).length - 1));
				g.setBackground(cm.getColors(8)[colorIndex]);
			}
			catch (Exception e)
			{
				System.out.println("Fehler bei Farbindex:\n"+value[i]+"  "+maxValue+"  "+colorIndex+"  "+i);
				e.printStackTrace();
			}
			g.fillRectangle(x1,c.getBounds().height-height,width,c.getBounds().height);
			g.setBackground(disp.getSystemColor(SWT.COLOR_WHITE));
			Font f = new Font(disp,"Arial", 14, SWT.NORMAL);
			g.setFont(f);
			if (i < maxLbl) 
			{
				if (g.getFontMetrics().getHeight() < width && c.getBounds().height>g.getFontMetrics().getAverageCharWidth()*label_anzeige[i].length()) 
				{
					Transform tr = new Transform(disp);
					tr.rotate(-90.0f);
					g.setTransform(tr);
					Path p = new Path(disp);
					p.addString(label_anzeige[i], -c.getBounds().height+5, 
							x1+(int)(0.5*width) -10, f);
					g.fillPath(p);
					//g.drawString(label_anzeige[i], -c.getBounds().height+2,x1+(int)(0.5*width) -2);
					tr.rotate(90.0f);
					g.setTransform(tr);
					tr.dispose();
					p.dispose();
				}
				f.dispose();
			}
		}
		// title
		g.setForeground(disp.getSystemColor(SWT.COLOR_WHITE));
		if(title != null)
			g.drawString(title,2,20);

		// print average
		g.setForeground(disp.getSystemColor(SWT.COLOR_MAGENTA));
		g.drawLine(0,c.getBounds().height-(int) (average*yFactor),
				c.getBounds().width,c.getBounds().height-(int) (average*yFactor));

		g.setForeground(disp.getSystemColor(SWT.COLOR_WHITE));
		g.drawLine(0,0,c.getBounds().width,0);
		g.drawLine(0,0,0,c.getBounds().height);
	}
	public void controlMoved(ControlEvent e) {}
	public void controlResized(ControlEvent e) 
	{
		preference.setLayoutData(new RowData(topshell.getClientArea().width-10,60));
		c.setLayoutData(new RowData(topshell.getClientArea().width-10, topshell.getClientArea().height-80));
		c.redraw();
	}
	public void repaint_buffer()
	{
		if (buffer != null)
		{
			buffer.dispose();
		}
		buffer = new Image(disp, c.getBounds().width, c.getBounds().height);
		GC bufferGC = new GC(buffer);
		paint(bufferGC);
		//bufferGC.dispose();
	}
	public void getNewValues()
	{
		this.ctr = Activator.getDefault().c;
		if (ctr.d!=null)
		{
			String s = visualizing.getItem(visualizing.getSelectionIndex());
			String c = cumulative.getItem(cumulative.getSelectionIndex());
			String d= "";
			if (device.getItemCount()>0&&device.getSelectionIndex()!=-1)
			{
				d = device.getItem(device.getSelectionIndex());
				float[] val = null;
				if (c.equals("accumulated"))
				{
					if (s.equals("Read"))
						val = ctr.d.values_hist_cumulative_r.get(d);
					if (s.equals("Write"))
						val = ctr.d.values_hist_cumulative_w.get(d);
				}
				else
			    {
					if (s.equals("Read"))
						val = ctr.d.values_hist_notcumulative_r.get(d);
					if (s.equals("Write"))
						val = ctr.d.values_hist_notcumulative_w.get(d);
			    }
				if (val == null)
					return;
				else
					setValues(val);
			}
			s = typ.getItem(typ.getSelectionIndex());
			if (s.equals("Linear"))
				type = Histogram.LINEAR;
			else
				type = Histogram.LOGARITHMIC;
			String[] keys = ctr.d.pat_data_r.keySet().toArray(new String[0]);
      boolean equal = keys.length==device.getItemCount();
			for (int i=0; i<device.getItemCount();i++)
			{
				equal = equal && device.getItem(i).equals(keys[i]);
			}
			if (!equal||device.getItemCount()==0)
			{
				device.setItems(keys);
				if (!d.equals(""))
				{
					for (int i=0; i<device.getItemCount();i++)
					{
						if (device.getItem(i).equals(d))
							device.setSelection(i);
					}
				}		
			}
		}
	}
	public Composite getComposite()
	{
		return topshell;
	}
	public void widgetDisposed(DisposeEvent e) 
	{
		c.dispose();
		buffer.dispose();
		lb1.dispose();
		lb2.dispose();
		lb3.dispose();
		cumulative.dispose();
		typ.dispose();
		visualizing.dispose();
		preference.dispose();
		Activator.getDefault().list_of_visualizer.remove(this);
	}
	public void setActive(boolean value) 
	{
		
	}
	public void setFocus() {}

}