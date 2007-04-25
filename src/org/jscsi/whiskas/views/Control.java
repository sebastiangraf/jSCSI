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

import org.jscsi.whiskas.preferences.*;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.jscsi.whiskas.Activator;
import org.jscsi.whiskas.preferences.PreferenceConstants;

import org.apache.log4j.spi.LoggingEvent;

import viscsi.*;

public class Control extends ViewPart implements SelectionListener, Runnable,
	DisposeListener
{
	ColorMap cm;
	Display disp;
	Shell topshell;
	int time_resolution = 10000;
	Daten d;
	static int number_of_bins = 17;
	private Button bt1, bt2, bt3,bt4,bt5;
	private Text log_file_name, type_file_name;
	boolean running;
	public int zeilen =1;
	public int spalten = 1;
	public Composite parent;
	private Scale sc, sc2;
	public int add_value = 1;
	public int lose_value = 1;
	private int counter = 0;
	public Control()
	{
		running = false;
	}
	public void run()
	{
		while(disp!=null&&!disp.isDisposed())
		{
			if (running)
			{
				if (d!=null&&d.nr!=null&&!disp.isDisposed())
				{
					d.getDaten();
					disp.syncExec(new Runnable() {
						public void run()
						{
							ListIterator<Idefix_Fetch_Stick> iter = Activator.getDefault().list_of_visualizer.listIterator();
							while (iter.hasNext())
							{
								Idefix_Fetch_Stick ids = iter.next();
								if (!ids.getComposite().isDisposed())
								{
									ids.getNewValues();
									ids.setActive(true);
								}
								else
									iter.remove();
							}
						}
					});
				}
			}
			else
			{
				ListIterator<Idefix_Fetch_Stick> iter = Activator.getDefault().list_of_visualizer.listIterator();
				while (iter.hasNext())
				{
					Idefix_Fetch_Stick ids = iter.next();
					ids.setActive(false);
				}
			}
			try
			{
				Thread.sleep(200);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	public void widgetDefaultSelected(SelectionEvent e) {}
	public void widgetSelected(SelectionEvent e) 
	{
		time_resolution = Integer.parseInt(Activator.getDefault().
				getPluginPreferences().getString(PreferenceConstants.P_INT_HITS_PER_PAINT));
		if (e.widget.toString().contains("Histogram"))
		{
			try 
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("viscsi.views.Histogram", Integer.toString(counter++), IWorkbenchPage.VIEW_ACTIVATE);
			} 
			catch (PartInitException ex) 
			{
				MessageDialog.openError(topshell, "Error", "Error opening view:" + ex.getMessage());
			}
		}
		if (e.widget.toString().contains("Reset"))
		{
			d.reset_Accumulation();
		}
		if (e.widget.toString().contains("Stop"))
		{
			running= false;
		}
		if (e.widget.toString().contains("Pattern"))
		{
			try 
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("viscsi.views.Pattern", Integer.toString(counter++), IWorkbenchPage.VIEW_ACTIVATE);
			} 
			catch (PartInitException ex) 
			{
				MessageDialog.openError(topshell, "Error", "Error opening view:" + ex.getMessage());
			}
		}
		if (e.widget.toString().contains("Apply"))
		{
			running = false;
			try
			{
				Thread.sleep(2000);
				if (d!=null && d.s !=null)
				{	
					d.nr.stopNetworkReader();
					d.s.close();
				}
			}
			catch (Exception ex)
			{ex.printStackTrace();}
			/*ListIterator<Idefix_Fetch_Stick> iter = list_of_visualizer.listIterator();
			while (iter.hasNext())
			{
				Idefix_Fetch_Stick ids = iter.next();
				ids.getShell().dispose();
				iter.remove();
			}*/
			zeilen = 1;
			spalten = 1;
			String log = log_file_name.getText();
			String types = type_file_name.getText();
			if (!log.equals(""))
			{
				d = new Daten(log,types,time_resolution,this);
				d.startConnection();
				running = true;
			}
		}
	}
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) 
	{
		this.parent = parent;
		Activator.getDefault().c = this;
		disp = parent.getDisplay();
		Composite c = new Composite(parent, SWT.NONE);
		cm = new ColorMap(disp);
		topshell = parent.getShell();
		c.setLayout(new RowLayout());
		bt1 = new Button(c,SWT.PUSH);
		bt1.setText("New Histogram");
		bt1.addSelectionListener(this);
		bt1.addDisposeListener(this);
		bt2 = new Button(c,SWT.PUSH);
		bt2.setText("New Pattern");
		bt2.addSelectionListener(this);
		bt3 = new Button(c,SWT.PUSH);
		bt3.setText("Reset Accumulation");
		bt3.addSelectionListener(this);
		Label lb = new Label(c,SWT.NONE);
		lb.setText("jSCSI Server: ");
		log_file_name = new Text(c,SWT.SINGLE|SWT.BORDER);
		log_file_name.setLayoutData(new RowData(200,-1));
		log_file_name.setText(Activator.getDefault().getPluginPreferences().
				getString(PreferenceConstants.P_STRING_DEFAULT_LOG_FILE));
		Label lb2 = new Label(c,SWT.NONE);
		lb2.setText("Type-File: ");
		type_file_name = new Text(c,SWT.SINGLE|SWT.BORDER);
		type_file_name.setLayoutData(new RowData(200,-1));
		type_file_name.setText(Activator.getDefault().getPluginPreferences().
				getString(PreferenceConstants.P_STRING_DEFAULT_TYPE_FILE));
		Label lb3 = new Label(c,SWT.NONE);
		lb3.setText("Value added at hit: ");
		sc = new Scale(c,SWT.HORIZONTAL);
		sc.setMaximum(128);
		sc.setMinimum(1);
		sc.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				add_value = sc.getSelection();
			}
		});
		Label lb4 = new Label(c,SWT.NONE);
		lb4.setText("Value subtracted by time: ");
		sc2 = new Scale(c,SWT.HORIZONTAL);
		sc2.setMaximum(30);
		sc2.setMinimum(1);
		sc2.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				lose_value = sc2.getSelection();
			}
		});
		bt4 = new Button(c,SWT.PUSH);
		bt4.addSelectionListener(this);
		bt4.setText("Apply");
		bt5 = new Button(c,SWT.PUSH);
		bt5.addSelectionListener(this);
		bt5.setText("Stop");
		Thread th = new Thread(this);
		th.start();
	}
	public void setFocus() 
	{
		bt1.setFocus();
	}
	public void widgetDisposed(DisposeEvent e) 
	{
		/*ListIterator<Idefix_Fetch_Stick> iter = Activator.getDefault().list_of_visualizer.listIterator();
		while (iter.hasNext())
		{
			Idefix_Fetch_Stick ids = iter.next();
			ids.getComposite().dispose();
			iter.remove();
		}*/
		running = false;
	}
}

class Daten
{
	HashMap<String, float[]> values_hist_notcumulative_r = new HashMap<String, float[]>();
	HashMap<String, float[]> values_hist_notcumulative_w = new HashMap<String, float[]>();
	HashMap<String, float[]> values_hist_cumulative_r = new HashMap<String, float[]>();
	HashMap<String, float[]> values_hist_cumulative_w = new HashMap<String, float[]>();
	HashMap<String,byte[]> values_pat_w = new HashMap<String,byte[]>();
	HashMap<String,byte[]> values_pat_r = new HashMap<String,byte[]>();
	HashMap<String,Pattern_Daten> pat_data_w = new HashMap<String,Pattern_Daten>();
	HashMap<String,Pattern_Daten> pat_data_r = new HashMap<String,Pattern_Daten>();
	HashMap<String, Integer> last_pos_w = new HashMap<String, Integer>();
	HashMap<String, Integer> last_pos_r = new HashMap<String, Integer>();
    HashMap<String, Histogramm_Daten> histo_data_w = new HashMap<String, Histogramm_Daten>();
    HashMap<String, Histogramm_Daten> histo_data_r = new HashMap<String, Histogramm_Daten>();
	public boolean networking;
	public LineNumberReader lnr;
	public int port = 1986;
	private String file;
	private int time_resolution;
	public NetworkReader nr;
	private Control vi;
	private String[] lines;
	byte[] types;
	Socket s;
	private String scsi_server;
	private int num_pos;
	public Daten (String logfilename, String typefilename, int time_resolution, int num_pos, Control vi)
	{
		this.vi = vi;
		networking = false;
		file = logfilename;
		this.time_resolution = time_resolution;
		this.num_pos = num_pos;
		setTypes(typefilename);
		try
		{
			lnr = new LineNumberReader(new FileReader(new File(file)));
		}
		catch (Exception e){}
	}
	public Daten (String scsi_server,String typefilename,int time_resolution, Control vi)
	{
		this.vi = vi;
		this.time_resolution = time_resolution;
		this.num_pos = 1;
		this.scsi_server = scsi_server;
		setTypes(typefilename);
		networking = true;
	}
	public void getDaten()
	{
		Iterator<String> iter3 = histo_data_w.keySet().iterator();
		while (iter3.hasNext())
		{
			histo_data_w.get(iter3.next()).nextTime();
		}
		iter3 = histo_data_r.keySet().iterator();
		while (iter3.hasNext())
		{
			histo_data_r.get(iter3.next()).nextTime();
		}
		Iterator<Pattern_Daten> iter = pat_data_w.values().iterator();
		while (iter.hasNext())
		{
			iter.next().lose_importance();
		}
		iter = pat_data_r.values().iterator();
		while (iter.hasNext())
		{
			iter.next().lose_importance();
		}
		String[] res = new String[time_resolution];
		if (networking)
		{
				long time = System.currentTimeMillis();
				int i=0;
				while (i<time_resolution&&System.currentTimeMillis()-time<200)
				{
					if (!nr.ll.isEmpty())
					{
						try
						{
							String line = (String) nr.ll.get(0);
							nr.ll.remove(0);
							if (line!=null)
							{
								res[i]=line;
								i++;
							}
						}
						catch (Exception ex) {}
					}
				}
				try
				{
					if (nr.ll.isEmpty())
						Thread.sleep(100);
				}
				catch (Exception e){};
				for (int j=i;j<time_resolution;j++)
					res[j]="  ";
		}
		else
		{
			String line;
		    System.gc();
		    int n=0;
		    try
			{
		    	while ((line = lnr.readLine()) != null&&n<time_resolution)
		    	{
		    		if (line.lastIndexOf(',')>=0&&line.length()>=5)
		    		{
		    			res[n]=line;
		    			n++;
		    		}
		    	}
		    	if (line == null&&n<time_resolution)
		    	{
		    		for (int i=n;i<time_resolution;i++)
		    		{
		    			res[i]="  ";
		    		}
		    		vi.running=false;
		    	}
			}
		    catch (Exception e)
		    {
		    	e.printStackTrace();
		    }
		}
		lines = res;
		analyse();
		iter3 = histo_data_w.keySet().iterator();
		while (iter3.hasNext())
		{
			String aktuell = iter3.next();
			values_hist_notcumulative_w.put(aktuell, histo_data_w.get(aktuell).make_float_array_not_cumulative());
			values_hist_cumulative_w.put(aktuell, histo_data_w.get(aktuell).make_float_array_cumulative());
		}
		iter3 = histo_data_r.keySet().iterator();
		while (iter3.hasNext())
		{
			String aktuell = iter3.next();
			values_hist_notcumulative_r.put(aktuell, histo_data_r.get(aktuell).make_float_array_not_cumulative());
			values_hist_cumulative_r.put(aktuell, histo_data_r.get(aktuell).make_float_array_cumulative());
		}
		Iterator<String> iter2 = pat_data_w.keySet().iterator();
		while (iter2.hasNext())
		{
			String aktuell = iter2.next();
			values_pat_w.put(aktuell, pat_data_w.get(aktuell).build_byte_array());
		}
		iter2 = pat_data_r.keySet().iterator();
		while (iter2.hasNext())
		{
			String aktuell = iter2.next();
			values_pat_r.put(aktuell, pat_data_r.get(aktuell).build_byte_array());
		}
	}
	public void analyse()
	{
		String line = null;
		int pos;
		char read_write;
		String device;
		for (int i=0;i<time_resolution;i++)
		{
			line = lines[i];
			if (line.contains("teardown"))
			{
				device = line.substring(line.indexOf(" "));
				pat_data_w.remove(device);
				pat_data_r.remove(device);
				values_pat_w.remove(device);
				values_pat_r.remove(device);
				histo_data_w.remove(device);
				histo_data_r.remove(device);
				last_pos_w.remove(device);
				last_pos_r.remove(device);
			}
			if (line.lastIndexOf(',')>=0 && !line.contains("teardown"))
			{
				pos = Integer.parseInt(line.substring(line.lastIndexOf(',')+1));
				line = line.substring(0,line.lastIndexOf(','));
				read_write = line.charAt(line.length()-1);
				device = line.substring(0,line.lastIndexOf(','));
				//System.out.println(device + "   "+pos+"   "+read_write);
				try
				{
					if (!pat_data_w.containsKey(device))
					{
						pat_data_w.put(device, new Pattern_Daten(vi));
						pat_data_r.put(device, new Pattern_Daten(vi));
						histo_data_w.put(device, new Histogramm_Daten());
						histo_data_r.put(device, new Histogramm_Daten());
						last_pos_r.put(device, new Integer(-5));
						last_pos_w.put(device, new Integer(-5));
					}
					if (read_write == 'w')
					{
						pat_data_w.get(device).add_importance(pos);
						if (last_pos_w.get(device).intValue()!=-5)
							histo_data_w.get(device).addValue(pos-last_pos_w.get(device).intValue());
						last_pos_w.put(device, new Integer(pos));
					}
					if (read_write == 'r')
					{
						pat_data_r.get(device).add_importance(pos);
						if (last_pos_r.get(device).intValue()!=-5)
							histo_data_r.get(device).addValue(pos-last_pos_r.get(device).intValue());
						last_pos_r.put(device, new Integer(pos));
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("Fehler bei: "+line);
				}
				//System.out.println(zaehler+"  "+line+"  "+pos+"  "+read_write+"  "+disk_cache+"   "+(((long) new Date().getTime()-time)));
			}
		}
	}
	public void reset_Accumulation()
	{
		Iterator<String> iter3 = histo_data_w.keySet().iterator();
		while (iter3.hasNext())
		{
			histo_data_w.get(iter3.next()).reset_accumulation();
		}
		iter3 = histo_data_r.keySet().iterator();
		while (iter3.hasNext())
		{
			histo_data_r.get(iter3.next()).reset_accumulation();
		}
	}
	public void setTypes(String typefilename)
	  {
		  try
		  {
			  ArrayList<String> type_list = new ArrayList<String>(); 
			  String line;
			  byte type;
			  int pos;
			  File f = new File(typefilename);
			  FileReader fr = new FileReader(f);
			  
			  LineNumberReader lnr3 = new LineNumberReader(fr);
			  while ((line=lnr3.readLine())!=null)
			  {
				  type_list.add(line);  
			  }
			  lnr3.close();
			  fr.close();
			  types = new byte[type_list.size()+1];
			  for (int i=0; i<type_list.size();i++)
			  {
				  line = type_list.get(i);
				  pos = Integer.parseInt(line.substring(0,line.indexOf(',')));
				  type = Byte.parseByte(line.substring(line.indexOf(',')+1,line.length()));
				  if (type>=0&&type<=9&&pos>=0)
					  types[pos]=type;
			  }
		  }
		  catch (Exception e) 
		  {
			  types = null;
		  }
	  }
	public void startConnection()
	{
		try
		{
			s = new Socket(scsi_server, port);
			nr = new NetworkReader(new ObjectInputStream(s.getInputStream()), vi);
			nr.start();
		}
		catch (Exception e)
		{
			MessageDialog.openError(vi.topshell, "Connection Error", "Can't connect to jSCSI Server!\n"+e.toString());
			vi.running = false;
		};
	}
	public void killAllThreads()
	{
		if (nr != null)
			nr.stopNetworkReader();
		if (s != null)
		{
			try
			{
				s.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}

class Pattern_Daten
{
	HashMap<Integer,Integer> values;
	private int max = 0;
	private Control ctr;
	public Pattern_Daten(Control c)
	{
		values = new HashMap<Integer,Integer>();
		ctr = c;
	}
	/**
	 * Falls dieses Segment häufig angesteuert wird, muss ein Zugriff wenig
	 * zählen, dies bewerkstelligt diese Methode.
	 */
	public void add_importance(int pos)
	{
		if (pos>=0)
		{
			if (pos > max)
				max = pos;
			if (!values.containsKey(new Integer(pos)))
			{
				values.put(new Integer(pos), new Integer(-127));
			}
			else
			{
				int value = values.get(new Integer(pos)).intValue();
				value += ctr.add_value;
				if (value > 127)
					value = 127;
				values.put(new Integer(pos), new Integer(value));
			}
		}
	}
	/**
	 * Falls dieses Segment häufig angesteuert wird, muss ein Zugriff schneller
	 * vergessen werden, dies bewerkstelligt diese Methode.
	 */
	public void lose_importance()
	{
		for (int pos =0; pos < max; pos++)
		{
			if (values.containsKey(new Integer(pos)))
			{
				int value = values.get(new Integer(pos)).intValue();
				value -= ctr.lose_value;
				if (value < -128)
					value = -128;
				values.put(new Integer(pos), new Integer(value));
			}	
		}
	}
	public byte[] build_byte_array()
	{
		byte[] res = new byte[max];
		for (int i=0; i<res.length; i++)
		{
			if (values.containsKey(new Integer(i)))
			{
				res[i] = (byte)(values.get(new Integer(i)).intValue());
			}
			else
			{
				res[i] = (byte)(-128);
			}
		}
		return res;
	}
}

/**
 * Die Klasse Distribution verwaltet die Werte eines Histogramms und
 * ordnet die Sprungweiten den einzelnen Säulen zu.
 * @author H. Janetzko
 */
class Histogramm_Daten
{
	private Bin_data[] count = new Bin_data [Control.number_of_bins];
	/**
	 * Der Konstruktor initialisiert ein Array von Objekten der
	 * Klasse Werte.
	 */
	public Histogramm_Daten()
	{
		for(int i=0;i<count.length;i++)
			count[i]=new Bin_data();
	}
	/**
	 * Hier wird die Sprungweite übergeben und an die passende Säule 
	 * weitergegeben.
	 * @param value - die Sprungweite
	 */
	public void addValue(int value)
	{
		if(value<-200)
			count[0].increaseValue();
		if(value>=-200&&value<=-101)
			count[1].increaseValue();
		if(value>=-100&&value<=-41)
			count[2].increaseValue();
		if(value>=-40&&value<=-16)
			count[3].increaseValue();
		if(value>=-15&&value<=-6)
			count[4].increaseValue();
		if(value>=-5&&value<=-3)
			count[5].increaseValue();
		if(value==-2)
			count[6].increaseValue();
		if(value==-1)
			count[7].increaseValue();
		if(value==0)
			count[8].increaseValue();
		if(value==1)
			count[9].increaseValue();
		if(value==2)
			count[10].increaseValue();
		if(value>=3&&value<=5)
			count[11].increaseValue();
		if(value>=6&&value<=15)
			count[12].increaseValue();
		if(value>=16&&value<=40)
			count[13].increaseValue();
		if(value>=41&&value<=100)
			count[14].increaseValue();
		if(value>=101&&value<=200)
			count[15].increaseValue();
		if(value>200)
			count[16].increaseValue();
	}
	/**
	 * Diese Methode erzeugt ein float_array, das aus den Werten der 
	 * einzelnen Säulen entsteht.
	 * @return - das Float-Array
	 */
	public float[] make_float_array_cumulative()
	{
		float[] result = new float[count.length];
		for (int i=0;i<count.length;i++)
			result[i]=count[i].get_cumulative_Value();
		return result;
	}
	public float[] make_float_array_not_cumulative()
	{
		float[] result = new float[count.length];
		for (int i=0;i<count.length;i++)
			result[i]=count[i].get_not_cumulative_Value();
		return result;
	}
	/**
	 * Um die Zeiteinheit um eins höher zu setzen muss jede Säule auch
	 * wissen, das es jetzt eine Zeiteinheit später ist, dies wird
	 * hier erledigt.
	 */
	public void nextTime()
	{
		for (int i=0;i<count.length;i++)
		{
			count[i].increase_timestamp();
			count[i].calculateValue();
		}
	}
	/**
	 * Hier wird allen Säulen der Wert 0 zugewiesen.
	 */
	public void reset()
	{
		for (int i=0;i<count.length;i++)
			count[i].reset();
	}
	public void reset_accumulation()
	{
		for (int i=0;i<count.length;i++)
			count[i].reset_accumulation();
	}
}

/**
 * Die Klasse Werte stellt eine einzelne Säule dar, die sich immer die letzten
 * 5 Werte merkt und anhand eines Zeitzählers die älteren vergisst, wobei
 * eine exponentielle Funktion zum Vergessen genommen wird.
 * @author H. Janetzko
 */
class Bin_data
{
	private int timestamp=0;
	private float value_total;
	private float cumulative_total=0;
	private int[] value_at_time = new int[5];
	/**
	 * Der Wert zum aktuellen Zeitstempel wird um eins erhöht; 
	 */
	public void increaseValue()
	{
		value_at_time[timestamp]++;
		value_total++;
		cumulative_total++;
	}
	/**
	 * Der Zeitzähler wird um eins hochgezählt.
	 */
	public void increase_timestamp()
	{
		timestamp++;
		if (timestamp>4)
			timestamp=0;
	}
	/**
	 * Hier wird der neue Gesamtwert ausgerechnet.
	 * (neuester Wert*1 + zweitneuester*.5+drittneuester*0.25+
	 * viertneuester Wert*0.125 + fünftneuester Wert*0.0625)
	 */
	public void calculateValue()
	{
		int[] hilfe = new int[5];
		for (int i=0;i<5;i++)
		{
			hilfe[i]=value_at_time[timestamp];
			timestamp++;
			if (timestamp>4)
				timestamp = 0;
		}
		value_total= (float) (hilfe[0]+hilfe[1]*0.5+hilfe[2]*0.25+hilfe[3]*0.125+hilfe[4]*0.0625);
		value_at_time[timestamp]=0;
	}
	/**
	 * Hier wird der Gesamtwert zurückgegeben.
	 * @return - der berechnete Gesamtwert
	 */
	public float get_not_cumulative_Value()
	{
		return value_total;
	}
	public float get_cumulative_Value()
	{
		return cumulative_total;
	}
	/**
	 * alle alten Werte und der berechnete Gesamtwert werden auf Null
	 * gesetzt.
	 */
	public void reset()
	{
		for (int i=0;i<5;i++)
			value_at_time[i]=0;
		value_total=0;
	}
	public void reset_accumulation()
	{
		cumulative_total=0;
	}
}

class NetworkReader extends Thread
{
	private ObjectInputStream ois;
	private Control vi;
	public List ll;
	private boolean run = false;
	public NetworkReader(ObjectInputStream ois, Control vi)
	{
		this.ois = ois;
		this.vi = vi;
		ll = Collections.synchronizedList(new LinkedList());
	}
	public void run()
	{
		run = true;
		while (run)
		{
			try
			{
				LoggingEvent le = (LoggingEvent) ois.readObject();
				String line = (String) le.getMessage();
				if (line.contains("teardown"))
				{
					ll.add(line);
				}
				else
				{
					int length = Integer.parseInt(line.substring(line.lastIndexOf(',')+1));
					line = line.substring(0, line.lastIndexOf(','));
					int first_pos = Integer.parseInt(line.substring(line.lastIndexOf(',')+1));
					line = line.substring(0, line.lastIndexOf(','));
					if (line!=null)
					{
						//System.out.println(line);
						for (int i=0; i<length; i++)
						{
							ll.add(line+","+(first_pos+i));
						}
					}
				}
				while (ll.size()>3*vi.time_resolution)
				{
					ll.remove(0);	
				}
			}
			catch (SocketException se)
			{
				final String exc = se.toString();
				if (vi.running)
				{
					vi.disp.asyncExec(new Runnable() {
						public void run()
						{
							MessageDialog.openError(vi.topshell, "Connection Error", "Connection to jSCSI Server lost!\n"+exc);
						}
					});
				}
				try
				{
					Thread.sleep(1000);
					run = false;
					stopNetworkReader();
				}
				catch (Exception e){;}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	public void stopNetworkReader()
	{
		run = false;
		try
		{
			ois.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}