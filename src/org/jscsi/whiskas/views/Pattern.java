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

import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.part.ViewPart;
import org.jscsi.whiskas.Activator;


public class Pattern extends ViewPart implements ControlListener, PaintListener,
		Idefix_Fetch_Stick, DisposeListener, SelectionListener, MouseMoveListener
{
	private Control ctr;
	private ColorMap cm;
	private Composite topshell;
	Canvas c;
	Image buffer;
	Composite preference, details;
	List visualizing;
	List device;
	Label lb1;
	Button bt;
	Label txt;
	private int zeilen;
	private int spalten;
	private int counter;
	boolean first = true;
	private int[] x_pos,y_pos,widths,heights;
	private byte[] types, values;
	private boolean[] never_touched;
	private int x_diff, y_diff;
	private String last_vis, last_dev="";
	public Pattern()
	{
		zeilen = 1;
		spalten = 1;
		Activator.getDefault().list_of_visualizer.add(this);
	}
	public void createPartControl(Composite parent) 
	{
		topshell = parent;
		topshell.addControlListener(this);
		cm = new ColorMap(topshell.getDisplay());
		RowLayout rl = new RowLayout();
		rl.type = SWT.VERTICAL;
		rl.wrap = false;
		rl.justify = true;
		topshell.setLayout(rl);
		preference = new Composite(topshell,SWT.BORDER);
		preference.setLayoutData(new RowData(topshell.getClientArea().width,50));
		RowLayout rl2 = new RowLayout();
		rl2.spacing = 10;
		preference.setLayout (rl2);
		lb1 = new Label(preference, SWT.READ_ONLY);
		lb1.setText("Device: ");
    RowData rowd = new RowData();
    rowd.height=45;
    rowd.width=130;
    device = new List(preference, SWT.SINGLE|SWT.V_SCROLL);
    device.setLayoutData(rowd);
		visualizing = new List(preference, SWT.SINGLE);
		visualizing.setItems(new String[] {"Read", "Write"});
		visualizing.setSelection(0);
		last_vis = "Read";
		bt = new Button(preference, SWT.PUSH);
		bt.setText("Reset Touch History");
		bt.addSelectionListener(this);
		details = new Composite(preference, SWT.BORDER);
		details.setLayout(new FillLayout());
		txt = new Label(details,SWT.NONE);
		txt.setText("Details: N/A");
		preference.pack();
		c = new Canvas(topshell,SWT.DOUBLE_BUFFERED|SWT.BORDER);
		c.setLayoutData(new RowData(topshell.getClientArea().width, topshell.getClientArea().height-150));
		c.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		c.addPaintListener(this);
		c.addMouseMoveListener(this);
		x_pos = new int[zeilen*spalten];
		y_pos = new int[zeilen*spalten];
		widths = new int[zeilen*spalten];
		heights = new int[zeilen*spalten];
		types = new byte[zeilen*spalten];
		values = new byte[zeilen*spalten];
		never_touched= new boolean[zeilen*spalten];
		for (int i=0;i<zeilen*spalten;i++)
		{
			types[i]=5;
			values[i] = -128;
			never_touched[i]=true;
		}
		topshell.addDisposeListener(this);
	}
	public void Raster_anlegen()
	{
		counter = 0;
		int breite_faktor = (c.getBounds().width-5)/spalten;
		int hoehe_faktor = (c.getBounds().height-5)/zeilen;
		int breite = ((c.getBounds().width-5)/spalten)*spalten;
		int hoehe = ((c.getBounds().height-5)/zeilen)*zeilen;
		x_diff = (c.getBounds().width-5)-breite;
		y_diff = (c.getBounds().height-5)-hoehe;
		int y_diff = this.y_diff;
		for (int x = 0; x < zeilen; x++)
		{
			if (x%2==0)
			{
				if (y_diff==0)
					aufteilen(0,breite_faktor,spalten,x*hoehe_faktor+this.y_diff,hoehe_faktor);
				else
				{
					aufteilen(0,breite_faktor,spalten,x*hoehe_faktor+this.y_diff-y_diff,hoehe_faktor+1);
					y_diff = y_diff - 1;
				}
			}
			if (x%2==1)
			{
				if (y_diff==0)
					aufteilen((c.getBounds().width-5),breite_faktor,spalten,x*hoehe_faktor+
							this.y_diff,hoehe_faktor);
				else
				{
					aufteilen((c.getBounds().width-5),breite_faktor,spalten,x*hoehe_faktor+
							this.y_diff-y_diff,hoehe_faktor+1);
					y_diff = y_diff - 1;
				}
			}
		}
	}
	public void aufteilen(int x_anfang, int breite_faktor, int anzahl,
			int y, int hoehe)
	{
		int x_diff = this.x_diff;
		if (x_anfang != 0)
			x_diff = 0;
		for (int x=1;x<=anzahl;x++)
		{
			if (x_anfang > 0)
			{
				if ( x < anzahl - this.x_diff + 1)
				{
					x_pos[counter]=x_anfang-x*breite_faktor;
					y_pos[counter]=y;
					widths[counter]=breite_faktor;
					heights[counter]=hoehe;
				}
				else
				{
					x_diff = x_diff + 1;
					x_pos[counter]=x_anfang-x*breite_faktor-x_diff;
					y_pos[counter]=y;
					widths[counter]=breite_faktor+1;
					heights[counter]=hoehe;
				}
			}
			if (x_anfang == 0)
			{
				if (x_diff == 0)
				{
					x_pos[counter]=x_anfang+(x-1)*breite_faktor+this.x_diff;
					y_pos[counter]=y;
					widths[counter]=breite_faktor;
					heights[counter]=hoehe;
				}
				else
				{
					x_pos[counter]=x_anfang+(x-1)*breite_faktor+this.x_diff-x_diff;
					y_pos[counter]=y;
					widths[counter]=breite_faktor+1;
					heights[counter]=hoehe;
					x_diff = x_diff - 1;
				}
			}
			if (counter < x_pos.length-1) 
				counter++;
			if (counter>=x_pos.length) System.out.println(counter+"  "+x_pos.length);
		}
	}
	public void controlMoved(ControlEvent e) {}
	public void controlResized(ControlEvent e) 
	{
		preference.setLayoutData(new RowData(topshell.getClientArea().width-10,60));
		c.setLayoutData(new RowData(topshell.getClientArea().width-10, topshell.getClientArea().height-80));
		//Raster_anlegen();
		first = true;
		c.redraw();
	}
	public void paintControl(PaintEvent e) 
	{
		if (first)
		{
			Raster_anlegen();
			first = false;
		}
		repaint_buffer();
		e.gc.drawImage(buffer, 0, 0);
	}
	public void repaint_buffer()
	{
		if (buffer != null)
		{
			buffer.dispose();
		}
		buffer = new Image(topshell.getDisplay(), c.getBounds().width, c.getBounds().height);
		GC bufferGC = new GC(buffer);
		paint(bufferGC);
		bufferGC.dispose();
	}
	public void paint(GC g)
	{
		for (int i=0; i<spalten*zeilen; i++)
		{
			if (never_touched[i])
			{
				try
				{	g.setBackground(cm.getColors(types[i])[0]);}
				catch (Exception e)
				{
					/*if (types!=null)
						System.out.println("Hallo"+types[i]);*/
					System.out.println(types.length);
				}
				g.fillRectangle(x_pos[i], y_pos[i], widths[i], heights[i]);
				g.setBackground(cm.getColors(types[i])[99]);
				g.fillRectangle((int) (x_pos[i] + 0.5 * widths[i] - 0.3 * widths[i] / 2.0),
						(int) (y_pos[i] + 0.5 * heights[i] - 0.3 * heights[i] / 2.0),
						(int) (0.3 * widths[i])+1,
						(int) (0.3 * heights[i])+1);
			}
			if (!never_touched[i]&&values!=null&&values[i]>-128)
			{
				int cIndex;
				if (values!=null)
					cIndex = (int) ((float) ((values[i])+128) / (256) * 99.0);
				else
					cIndex = (int) ((float) (-128+128) / (256) * 99.0);
				g.setBackground(cm.getColors(types[i])[cIndex]);
				g.fillRectangle(x_pos[i], y_pos[i], widths[i], heights[i]);
				g.setBackground(cm.getColors(types[i])[99]);
				int[] points = new int[6];
				points[0]=x_pos[i];
				points[2]=x_pos[i]+widths[i];
				points[4]=x_pos[i]+widths[i];
				points[1]=y_pos[i];
				points[3]=y_pos[i];
				points[5]=y_pos[i]+heights[i];
				g.fillPolygon(points);
			}
			if (!never_touched[i]&&values!=null&&values[i]==-128)
			{
				g.setBackground(cm.getColors(types[i])[99]);
				g.fillRectangle(x_pos[i], y_pos[i], widths[i], heights[i]);
				g.setBackground(cm.getColors(types[i])[0]);
				g.fillRectangle((int) (x_pos[i] + 0.5 * widths[i] - 0.3 * widths[i] / 2.0),
						(int) (y_pos[i] + 0.5 * heights[i] - 0.3 * heights[i] / 2.0),
						(int) (0.3 * widths[i])+1,
						(int) (0.3 * heights[i])+1);
			}
		}
	}
	public void getNewValues() 
	{
		this.ctr = Activator.getDefault().c;
		if (ctr.d==null)
			return;
		String s = visualizing.getItem(visualizing.getSelectionIndex());
		String d= "";
		if (device.getItemCount()>0&&device.getSelectionIndex()!=-1)
		{
			d = device.getItem(device.getSelectionIndex());
			byte[] values_neu = new byte[0];
			if (s.equals("Read"))
				values_neu = ctr.d.values_pat_r.get(d);
			if (s.equals("Write"))
				values_neu = ctr.d.values_pat_w.get(d);
			if (!s.equals(last_vis)||!last_dev.equals(d))
			{
				last_vis=s;
				last_dev = d;
				reset_never_touched();
			}
			if (values_neu==null)
				return;
			int zeilen_neu = (int)Math.sqrt(values_neu.length)+1;
			int spalten_neu = (int)Math.sqrt(values_neu.length)+1;
			if (zeilen_neu!=zeilen||spalten_neu!=spalten)
			{
				x_pos = new int[zeilen_neu*spalten_neu];
				y_pos = new int[zeilen_neu*spalten_neu];
				widths = new int[zeilen_neu*spalten_neu];
				heights = new int[zeilen_neu*spalten_neu];
				boolean[] never_touched_neu= new boolean[zeilen_neu*spalten_neu];
				types = new byte[zeilen_neu*spalten_neu];
				for (int i=0;i<zeilen_neu*spalten_neu;i++)
				{
					if (i<zeilen*spalten)
            never_touched_neu[i] = never_touched[i];
          else
            never_touched_neu[i]=true;
					if (ctr.d.types!=null&&ctr.d.types.length>i)
						types[i]=ctr.d.types[i];
					if (ctr.d.types==null)
						types[i]=5;
					if (ctr.d.types!=null&&ctr.d.types.length<i)
						types[i]=9;
				}
        zeilen = zeilen_neu;
        spalten = spalten_neu;
        never_touched = never_touched_neu;
				Raster_anlegen();
			}
			values = new byte[zeilen*spalten];
			//System.out.println(zeilen*spalten+"    "+values_neu.length);
			for (int i=0; i<values_neu.length; i++)
			{
				values[i]=values_neu[i];
				if (values!=null&&values[i]>=-127)
					never_touched[i]=false;
			}
			for (int i=values_neu.length; i<zeilen*spalten;i++)
			{
				values[i]=-128;
			}
			repaint_buffer();
			c.redraw();
		}
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
	public Composite getComposite() 
	{
		return topshell;
	}
	public void widgetDisposed(DisposeEvent e) 
	{
		c.dispose();
		buffer.dispose();
		visualizing.dispose();
		lb1.dispose();
		bt.dispose();
		txt.dispose();
		details.dispose();
		preference.dispose();
		Activator.getDefault().list_of_visualizer.remove(this);
	}
	public void widgetDefaultSelected(SelectionEvent e) {}
	public void widgetSelected(SelectionEvent e) 
	{
		if (e.widget.toString().contains("Reset Touch History"))
		{
			reset_never_touched();
		}
	}
	public void reset_never_touched()
	{
		for (int i = 0; i< spalten*zeilen;i++)
		{
			never_touched[i]=true;
		}
	}
	public void mouseMove(MouseEvent e) 
	{
		int x = e.x;
		int y = e.y;
		boolean gefunden = false;
		for (int i=0; i<spalten*zeilen&&!gefunden; i++)
		{
			if ((x>=x_pos[i])&&(x<=x_pos[i]+widths[i])&&
					(y>=y_pos[i])&&(y<=y_pos[i]+heights[i]))
			{
				gefunden = true;
				String[] s = {"Free Block","Root Block","Positional BTree Block",
						"Keyed Trie Block", "Keyed BTree Block", 
						"Node Block", "Name Block", "Value Block", "Histogram", "Unknown Block"};
				if (ctr.d.types!=null)
					txt.setText("Details:\nTouches: "+values[i]+"\tPos: "+i+"\n"+s[types[i]]);
				else
					txt.setText("Details:\nTouches: "+values[i]+"\tPos: "+i);
				details.pack();
			}
		}
		if (!gefunden)
		{
			txt.setText("Details: N/A");
			details.pack();
		}
	}
	public void setActive(boolean value) {}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}
