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
 * $Id: Control.java 32 2007-05-24 10:45:00Z ross_roy $
 * 
 */

package org.jscsi.whiskas.views;

//import org.jscsi.whiskas.preferences.*;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;

import org.jscsi.whiskas.Activator;
import org.jscsi.whiskas.preferences.PreferenceConstants;

import org.apache.log4j.spi.LoggingEvent;

/**
 * The main class of Whiskas. It controls every visualizing
 * view of Whiskas and is responsible for the network
 * connection to jSCSI. Therefore it supplies the values
 * to display.
 * @author Halldór Janetzko
 */
public class Control extends ViewPart implements SelectionListener,
Runnable, DisposeListener {
    /**Display of Control.*/
    private Display disp;
    /**Parent shell of Control view.*/
    private Shell topshell;
    /**How many lines are evaluated per painting.*/
    private int timeResolution = 10000;
    /**Representation of the data to visualize.*/
    private Values data;
    /**Number of bins for histogram.*/
    public static int numberOfBins = 17;
    /**Buttons for GUI.*/
    private Button btNewHistogram, btNewPattern, btResetAccumulation,
        btApply, btStop;
    /**Text fields for input of server an type file.*/
    private Text serverName, typeFileName;
    /**Boolean value to remember state of Whiskas.*/
    private boolean running = false;
    //public int zeilen = 1;
    //public int spalten = 1;
    /**Scales to adjust visualizing of Whiskas.*/
    private Scale sc, sc2;
    /**Values to adjust appearing and disappearing time.*/
    private int addValue = 1, loseValue = 1;
    /**Counter for giving IDs to new views.*/
    private int idCounter = 0;
    /**
     * Returns Values object of the Control object.
     * @return values object
     */
    public final Values getDaten() {
        return data;
    }
    /**
     * Returns parent shell of Control view.
     * @return parent shell
     */
    public final Shell getTopshell() {
        return topshell;
    }
    /**
     * Returns number of lines to visualize
     * per painting.
     * @return number of lines
     */
    public final int getTimeResolution() {
        return timeResolution;
    }
    /**
     * Returns Display of Control view.
     * @return Display of view
     */
    public final Display getDisplay() {
        return disp;
    }
    /**
     * Returns wether Animation is running or not.
     * @return running - value
     */
    public final boolean getRunning() {
        return running;
    }
    /**
     * Methods sets the running value of the animation.
     * @param b - new running value
     */
    public final void setRunning(final boolean b) {
        running = b;
    }
    /**
     * Returns value to add whenever block is touched.
     * @return int that is added on visualizing value whenever
     * block is touched
     */
    public final int getAddValue() {
        return addValue;
    }
    /**
     * Returns value that is subtracted whenever a time interval is over.
     * @return int that is subtracted of visualizing value (for fade-out)
     */
    public final int getLoseValue() {
        return loseValue;
    }
    /**
     * Method which is used to read data and to supply it to
     * visualizing views.
     */
    public final void run() {
        try {
            while (disp != null && !disp.isDisposed()) {
                if (running) {
                    if (data != null && data.getNetworkReader() != null && !disp.isDisposed()) {
                        data.getData();
                        disp.syncExec(new Runnable() {
                            public void run() {
                                ListIterator < VisualListener > iter =
                                    Activator.getDefault().list_of_visualizer.
                                         listIterator();
                                while (iter.hasNext()) {
                                    VisualListener ids = iter.next();
                                    if (!ids.getComposite().isDisposed()) {
                                        ids.getNewValues();
                                    } else {
                                        iter.remove();
                                    }
                                }
                            }
                        });
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) { }
    }
    /**
     * Implementation of SelectionListener (method not used).
     * @param e - SelectionEvent
     */
    public void widgetDefaultSelected(final SelectionEvent e) { }
    /**
     * Implementation of SelectionListener (used for buttons
     * of Control view).
     * @param e - SelectionEvent
     */
    public final void widgetSelected(final SelectionEvent e) {
        timeResolution = Integer.parseInt(Activator.getDefault().
                getPluginPreferences().getString(
                        PreferenceConstants.P_INT_HITS_PER_PAINT));
        if (e.widget.toString().contains("Histogram")) {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().
                   getActivePage().showView(
                           "org.jscsi.whiskas.views.Histogram",
                           Integer.toString(idCounter++),
                           IWorkbenchPage.VIEW_ACTIVATE);
            } catch (PartInitException ex) {
                MessageDialog.openError(topshell, "Error",
                        "Error opening view:" + ex.getMessage());
            }
        }
        if (e.widget.toString().contains("Reset")) {
            data.resetAccumulation();
        }
        if (e.widget.toString().contains("Stop")) {
            running = false;
            try {
                Thread.sleep(2000);
                if (data != null && data.getSocket() != null) {
                    data.getNetworkReader().stopNetworkReader();
                    data.getSocket().close();
                }
                data = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (e.widget.toString().contains("Pattern")) {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().
                  getActivePage().showView(
                          "org.jscsi.whiskas.views.Pattern",
                          Integer.toString(idCounter++),
                          IWorkbenchPage.VIEW_ACTIVATE);
            } catch (PartInitException ex) {
                MessageDialog.openError(topshell, "Error",
                        "Error opening view:" + ex.getMessage());
            }
        }
        if (e.widget.toString().contains("Apply")) {
            running = false;
            try {
                Thread.sleep(2000);
                if (data != null && data.getSocket() != null) {
                    data.getNetworkReader().stopNetworkReader();
                    data.getSocket().close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            String log = serverName.getText();
            String types = typeFileName.getText();
            if (!log.equals("")) {
                data = new Values(log, types, this);
                if (data.startConnection()) {
                    running = true;
                }
            }
        }
    }
    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     * @param parent - Composite to create GUI on
     */
    public final void createPartControl(final Composite parent) {
        Activator.getDefault().c = this;
        disp = parent.getDisplay();
        Composite c = new Composite(parent, SWT.NONE);
        topshell = parent.getShell();
        c.setLayout(new RowLayout());
        btNewHistogram = new Button(c, SWT.PUSH);
        btNewHistogram.setText("New Histogram");
        btNewHistogram.addSelectionListener(this);
        btNewHistogram.addDisposeListener(this);
        btNewPattern = new Button(c, SWT.PUSH);
        btNewPattern.setText("New Pattern");
        btNewPattern.addSelectionListener(this);
        btResetAccumulation = new Button(c, SWT.PUSH);
        btResetAccumulation.setText("Reset Accumulation");
        btResetAccumulation.addSelectionListener(this);
        Label lb = new Label(c, SWT.NONE);
        lb.setText("jSCSI Server: ");
        serverName = new Text(c, SWT.SINGLE | SWT.BORDER);
        serverName.setLayoutData(new RowData(200, -1));
        serverName.setText(Activator.getDefault().getPluginPreferences().
                getString(PreferenceConstants.P_STRING_DEFAULT_LOG_FILE));
        Label lb2 = new Label(c, SWT.NONE);
        lb2.setText("Type-File: ");
        typeFileName = new Text(c, SWT.SINGLE | SWT.BORDER);
        typeFileName.setLayoutData(new RowData(200, -1));
        typeFileName.setText(Activator.getDefault().getPluginPreferences().
                getString(PreferenceConstants.P_STRING_DEFAULT_TYPE_FILE));
        Label lb3 = new Label(c, SWT.NONE);
        lb3.setText("Value added at hit: ");
        sc = new Scale(c, SWT.HORIZONTAL);
        sc.setMaximum(128);
        sc.setMinimum(1);
        sc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addValue = sc.getSelection();
            }
        });
        Label lb4 = new Label(c, SWT.NONE);
        lb4.setText("Value subtracted by time: ");
        sc2 = new Scale(c, SWT.HORIZONTAL);
        sc2.setMaximum(30);
        sc2.setMinimum(1);
        sc2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                loseValue = sc2.getSelection();
            }
        });
        btApply = new Button(c, SWT.PUSH);
        btApply.addSelectionListener(this);
        btApply.setText("Apply");
        btStop = new Button(c, SWT.PUSH);
        btStop.addSelectionListener(this);
        btStop.setText("Stop");
        Thread th = new Thread(this);
        th.start();
    }
    /**
     * Implements setFocus from Workbench (sets focus
     * on btNewHistogram).
     */
    public final void setFocus() {
        btNewHistogram.setFocus();
    }
    /**
     * Is called when View is closed, sets running value
     * to false.
     * @param e - DisposeEvent from Eclipse
     */
    public final void widgetDisposed(final DisposeEvent e) {
        running = false;
    }
}

/**
 * Internal representation of all data values to visualize.
 * @author Halldór Janetzko
 */
class Values {
    /**HashMaps to put device and values together (for histogram).*/
    private HashMap < String, float[] >
        valuesHistNotAccumulatedR = new HashMap < String, float[] > (),
        valuesHistNotAccumulatedW = new HashMap < String, float[] > (),
        valuesHistAccumulatedR = new HashMap < String, float[] > (),
        valuesHistAccumulatedW = new HashMap < String, float[] > ();
    /**HashMaps to put device and values together (for pattern).*/
    private HashMap < String, byte[] >
        valuesPatW = new HashMap < String, byte[] > (),
        valuesPatR = new HashMap < String, byte[] > ();
    /**HashMaps to put device and internal data representation
     * for pattern together.*/
    private HashMap < String, PatternData >
        patDataW = new HashMap < String, PatternData > (),
        patDataR = new HashMap < String, PatternData > ();
    /**HashMaps to save device and last block touch of
     * this device (for histogram).*/
    private HashMap < String, Integer >
        lastPosW = new HashMap < String, Integer > (),
        lastPosR = new HashMap < String, Integer > ();
    /**HashMaps to put device and internal data representation
     * for histogram together.*/
    private HashMap < String, HistogramData >
        histoDataW = new HashMap < String, HistogramData > (),
        histoDataR = new HashMap < String, HistogramData > ();
    /**Port to communicate with jSCSI server.*/
    private int port = 1986;
    /**Thread to read from network.*/
    private NetworkReader networkReader;
    /**Backlink to Control object for other classes.*/
    private Control control;
    /**Lines of data which are going to be visualized.*/
    private String[] lines;
    /**Array of block types for different coloring.*/
    private byte[] types;
    /**Socket to communicato with jSCSI server.*/
    private Socket socket;
    /**URL of jSCSI server.*/
    private String jscsiServer;
    /**
     * Constructor of Values which needs jSCSI server URL,
     * the type file and the Control object.
     * @param jSCSI - String of URL to jSCSI server
     * @param typeFile - Path and name of type file
     * @param ctr - Control object, which creates this Values object
     */
    public Values(final String jSCSI,
            final String typeFile, final Control ctr) {
        this.control = ctr;
        this.jscsiServer = jSCSI;
        setTypes(typeFile);
    }
    /**
     * Get method which returns the types array.
     * @retrun array of types
     */
    public byte[] getTypes() {
        return types;
    }
    /**
     * Get method which returns a hashtable.
     * @return the hashtable
     */
    public HashMap < String, byte[] > getValuesPatW() {
        return valuesPatW;
    }
    /**
     * Get method which returns a hashtable.
     * @return the hashtable
     */
    public HashMap < String, byte[] > getValuesPatR() {
        return valuesPatR;
    }
    /**
     * Get method which returns a hashtable.
     * @return the hashtable
     */
    public HashMap < String, float[] > getValuesHistNotAccumulatedR() {
        return valuesHistNotAccumulatedR;
    }
    /**
     * Get method which returns a hashtable.
     * @return the hashtable
     */
    public HashMap < String, float[] > getValuesHistNotAccumulatedW() {
        return valuesHistNotAccumulatedW;
    }
    /**
     * Get method which returns a hashtable.
     * @return the hashtable
     */
    public HashMap < String, float[] > getValuesHistAccumulatedR() {
        return valuesHistAccumulatedR;
    }
    /**
     * Get method which returns a hashtable.
     * @return the hashtable
     */
    public HashMap < String, float[] > getValuesHistAccumulatedW() {
        return valuesHistAccumulatedW;
    }
    /**
     * Get method which returns the network reader object.
     * @return NetworkReader object
     */
    public NetworkReader getNetworkReader() {
        return networkReader;
    }
    /**
     * Returns Socket to jSCSI server.
     * @return Socket to jSCSI server.
     */
    public Socket getSocket() {
        return socket;
    }
    /**
     * Method which pulls new data from the network reader
     * and processes them.
     */
    public void getData() {
        Iterator < String > iterHistogram = histoDataW.keySet().iterator();
        while (iterHistogram.hasNext()) {
            histoDataW.get(iterHistogram.next()).nextTime();
        }
        iterHistogram = histoDataR.keySet().iterator();
        while (iterHistogram.hasNext()) {
            histoDataR.get(iterHistogram.next()).nextTime();
        }
        Iterator < PatternData > iterPattern = patDataW.values().iterator();
        while (iterPattern.hasNext()) {
            iterPattern.next().loseValue();
        }
        iterPattern = patDataR.values().iterator();
        while (iterPattern.hasNext()) {
            iterPattern.next().loseValue();
        }
        String[] res = new String[control.getTimeResolution()];
        long time = System.currentTimeMillis();
        int i = 0;
        while (i < control.getTimeResolution()
                && System.currentTimeMillis() - time < 200) {
            if (!networkReader.getList().isEmpty()) {
                try {
                    String line = (String) networkReader.getList().get(0);
                    networkReader.getList().remove(0);
                    if (line != null) {
                        res[i] = line;
                        i++;
                    }
                } catch (Exception ex) { }
            }
        }
        try {
            if (networkReader.getList().isEmpty()) {
                Thread.sleep(100);
            }
        } catch (Exception e) { }
        for (int j = i; j < control.getTimeResolution(); j++) {
            res[j] = "  ";
        }
        lines = res;
        analyse();
        iterHistogram = histoDataW.keySet().iterator();
        while (iterHistogram.hasNext()) {
            String s = iterHistogram.next();
            valuesHistNotAccumulatedW.put(s,
                    histoDataW.get(s).buildFloatArrayNotAccumulated());
            valuesHistAccumulatedW.put(s,
                    histoDataW.get(s).buildFloatArrayAccumulated());
        }
        iterHistogram = histoDataR.keySet().iterator();
        while (iterHistogram.hasNext()) {
            String s = iterHistogram.next();
            valuesHistNotAccumulatedR.put(s,
                    histoDataR.get(s).buildFloatArrayNotAccumulated());
            valuesHistAccumulatedR.put(s,
                    histoDataR.get(s).buildFloatArrayAccumulated());
        }
        Iterator < String > iterPatData = patDataW.keySet().iterator();
        while (iterPatData.hasNext()) {
            String aktuell = iterPatData.next();
            valuesPatW.put(aktuell,
                    patDataW.get(aktuell).buildByteArray());
        }
        iterPatData = patDataR.keySet().iterator();
        while (iterPatData.hasNext()) {
            String aktuell = iterPatData.next();
            valuesPatR.put(aktuell, patDataR.get(aktuell).buildByteArray());
        }
    }
    /**
     * In this method the new data are processed and
     * put in the different objects.
     */
    public void analyse() {
        String line = null;
        int pos;
        char readWrite;
        String device;
        for (int i = 0; i < control.getTimeResolution(); i++) {
            line = lines[i];
            if (line.contains("teardown")) {
                device = line.substring(line.indexOf(" "));
                patDataW.remove(device);
                patDataR.remove(device);
                valuesPatW.remove(device);
                valuesPatR.remove(device);
                histoDataW.remove(device);
                histoDataR.remove(device);
                lastPosW.remove(device);
                lastPosR.remove(device);
            }
            if (line.lastIndexOf(',') >= 0 && !line.contains("teardown")) {
                pos = Integer.parseInt(line.substring(
                        line.lastIndexOf(',') + 1));
                line = line.substring(0, line.lastIndexOf(','));
                readWrite = line.charAt(line.length() - 1);
                device = line.substring(0, line.lastIndexOf(','));
                try {
                    if (!patDataW.containsKey(device)) {
                        patDataW.put(device, new PatternData(control));
                        patDataR.put(device, new PatternData(control));
                        histoDataW.put(device, new HistogramData());
                        histoDataR.put(device, new HistogramData());
                        lastPosR.put(device, new Integer(-5));
                        lastPosW.put(device, new Integer(-5));
                    }
                    if (readWrite == 'w') {
                        patDataW.get(device).addValue(pos);
                        if (lastPosW.get(device).intValue() != -5) {
                            histoDataW.get(device).addValue(pos
                                    - lastPosW.get(device).intValue());
                        }
                        lastPosW.put(device, new Integer(pos));
                    }
                    if (readWrite == 'r') {
                        patDataR.get(device).addValue(pos);
                        if (lastPosR.get(device).intValue() != -5) {
                            histoDataR.get(device).addValue(pos
                                    - lastPosR.get(device).intValue());
                        }
                        lastPosR.put(device, new Integer(pos));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * This method resets the Accumulation of the
     * hsitogram data.
     */
    public void resetAccumulation() {
        Iterator < String > iter = histoDataW.keySet().iterator();
        while (iter.hasNext()) {
            histoDataW.get(iter.next()).resetAccumulation();
        }
        iter = histoDataR.keySet().iterator();
        while (iter.hasNext()) {
            histoDataR.get(iter.next()).resetAccumulation();
        }
    }
    /**
     * This methods reads the block types from
     * the typefile and processes it.
     * @param typefilename - the nam e of the pathfile
     */
    public void setTypes(final String typefilename) {
        try {
            ArrayList < String > typeList = new ArrayList < String > ();
            String line;
            byte type;
            int pos;
            File f = new File(typefilename);
            FileReader fr = new FileReader(f);

            LineNumberReader lnr3 = new LineNumberReader(fr);
            while ((line = lnr3.readLine()) != null) {
                typeList.add(line);
            }
            lnr3.close();
            fr.close();
            types = new byte[typeList.size() + 1];
            for (int i = 0; i < typeList.size(); i++) {
                line = typeList.get(i);
                pos = Integer.parseInt(line.substring(0, line.indexOf(',')));
                type = Byte.parseByte(line.substring(
                        line.indexOf(',') + 1, line.length()));
                if (type >= 0 && type <= 9 && pos >= 0) {
                    types[pos] = type;
                }
            }
        } catch (Exception e) {
            types = null;
        }
    }
    /**
     * This method starts the connection to the
     * jSCSI server.
     * @return everything went OK
     */
    public boolean startConnection() {
        try {
            socket = new Socket(jscsiServer, port);
            networkReader = new NetworkReader(
                    new ObjectInputStream(socket.getInputStream()), control);
            networkReader.start();
            return true;
        } catch (Exception e) {
            MessageDialog.openError(control.getTopshell(),
                    "Connection Error",
                    "Can't connect to jSCSI Server!\n" + e.toString());
            control.setRunning(false);
            return false;
        }
    }
    /**
     * This method stops the network reader thread.
     */
    public void killThread() {
        if (networkReader != null) {
            networkReader.stopNetworkReader();
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * This class is an internal representation of the
 * pattern values to visualize.
 * @author Halldór Janetzko
 */
class PatternData {
    /**HashMap of block, value.*/
    private HashMap < Integer, Integer > values;
    /**block number.*/
    private int max = 0;
    /**Control object to get add and lose value.*/
    private Control ctr;
    /**
     * Constructor of PatternData.
     * @param c - the Control object
     */
    public PatternData(final Control c) {
        values = new HashMap < Integer, Integer > ();
        ctr = c;
    }
    /**
     * Method adds a certain value (given by Control) to
     * the internal block representation.
     * @param pos - the Block on which a value has to be added
     */
    public void addValue(final int pos) {
        if (pos >= 0) {
            if (pos > max) {
                max = pos;
            }
            if (!values.containsKey(new Integer(pos))) {
                values.put(new Integer(pos), new Integer(-127));
            } else {
                int value = values.get(new Integer(pos)).intValue();
                value += ctr.getAddValue();
                if (value > 127) {
                    value = 127;
                }
                values.put(new Integer(pos), new Integer(value));
            }
        }
    }
    /**
     * Every internal block representation has to lose a
     * certain value given by Control.
     */
    public void loseValue() {
        for (int pos = 0; pos < max; pos++) {
            if (values.containsKey(new Integer(pos))) {
                int value = values.get(new Integer(pos)).intValue();
                value -= ctr.getLoseValue();
                if (value < -128) {
                    value = -128;
                }
                values.put(new Integer(pos), new Integer(value));
            }
        }
    }
    /**
     * This methods creates an byte array containing all values
     * for the pattern.
     * @return byte - Array of values
     */
    public byte[] buildByteArray() {
        byte[] res = new byte[max];
        for (int i = 0; i < res.length; i++) {
            if (values.containsKey(new Integer(i))) {
                res[i] = (byte) (values.get(new Integer(i)).intValue());
            } else {
                res[i] = (byte) (-128);
            }
        }
        return res;
    }
}

/**
 * This class manages all values and assign the
 * different jump lengths.
 * @author Halldór Janetzko
 */
class HistogramData {
    /**The bin objects are stored here.*/
    private BinData[] count = new BinData [Control.numberOfBins];
    /**
     * Constructor which initializes the bin objects
     * in the array.
     */
    public HistogramData() {
        for (int i = 0; i < count.length; i++) {
            count[i] = new BinData();
        }
    }
    /**
     * This method adds a hit to the suitable
     * bin according to the jump.
     * @param value of the jumping distance
     */
    public void addValue(final int value) {
        if (value < -200) {
            count[0].increaseValue();
        }
        if (value >= -200 && value <= -101) {
            count[1].increaseValue();
        }
        if (value >= -100 && value <= -41) {
            count[2].increaseValue();
        }
        if (value >= -40 && value <= -16) {
            count[3].increaseValue();
        }
        if (value >= -15 && value <= -6) {
            count[4].increaseValue();
        }
        if (value >= -5 && value <= -3) {
            count[5].increaseValue();
        }
        if (value == -2) {
            count[6].increaseValue();
        }
        if (value == -1) {
            count[7].increaseValue();
        }
        if (value == 0) {
            count[8].increaseValue();
        }
        if (value == 1) {
            count[9].increaseValue();
        }
        if (value == 2) {
            count[10].increaseValue();
        }
        if (value >= 3 && value <= 5) {
            count[11].increaseValue();
        }
        if (value >= 6 && value <= 15) {
            count[12].increaseValue();
        }
        if (value >= 16 && value <= 40) {
            count[13].increaseValue();
        }
        if (value >= 41 && value <= 100) {
            count[14].increaseValue();
        }
        if (value >= 101 && value <= 200) {
            count[15].increaseValue();
        }
        if (value > 200) {
            count[16].increaseValue();
        }
    }
    /**
     * This method returns an float array consisting
     * of the different bin heights, which are
     * accumulated.
     * @return float array for histogram
     */
    public float[] buildFloatArrayAccumulated() {
        float[] array = new float[count.length];
        for (int i = 0; i < count.length; i++) {
            array[i] = count[i].getAccumulatedValue();
        }
        return array;
    }
    /**
     * This method returns an float array consisting
     * of the different bin heights, which are not
     * accumulated.
     * @return float array for histogram
     */
    public float[] buildFloatArrayNotAccumulated() {
        float[] result = new float[count.length];
        for (int i = 0; i < count.length; i++) {
            result[i] = count[i].getNotAccumulatedValue();
        }
        return result;
    }
    /**
     * This method increases the timestamp
     * of every bin, to ensure the right
     * calculation of the bin height.
     */
    public void nextTime() {
        for (int i = 0; i < count.length; i++) {
            count[i].increaseTimestamp();
            count[i].calculateValue();
        }
    }
    /**
     * This method sets the height of every
     * bin to 0.
     */
    public void reset() {
        for (int i = 0; i < count.length; i++) {
            count[i].reset();
        }
    }
    /**
     * This method resets the accumulation
     * of every bin for instance when another
     * device is choosen.
     */
    public void resetAccumulation() {
        for (int i = 0; i < count.length; i++) {
            count[i].resetAccumulation();
        }
    }
}

/**
 * This class represents a single bin of the histogram. The
 * actual height is calculated of the last 5 values by
 * using an exponential function.
 * @author Halldór Janetzko
 */
class BinData {
    /**A value between 0 and 4 (incl.) for calculation of
     *  bin height.
     */
    private int timestamp = 0;
    /**Height calculated with exponential function.*/
    private float calculatedHeight;
    /**All values accumulated.*/
    private float valuesAccumulated = 0;
    /**Array to store the last five values.*/
    private int[] valueAtTimestamp = new int[5];
    /**
     * The value at the current timestamp and the
     * accumulated value is increased by 1.
     */
    public void increaseValue() {
        valueAtTimestamp[timestamp]++;
        calculatedHeight++;
        valuesAccumulated++;
    }
    /**
     * The value of the timestamp is increased by 1 and
     * resetted if it is greater than 4.
     */
    public void increaseTimestamp() {
        timestamp++;
        if (timestamp > 4) {
            timestamp = 0;
        }
    }
    /**
     * In this method is the bin height calculated by
     * using an exponential weighting function.
     */
    public void calculateValue() {
        int[] temp = new int[5];
        for (int i = 0; i < 5; i++) {
            temp[i] = valueAtTimestamp[timestamp];
            timestamp++;
            if (timestamp > 4) {
                timestamp = 0;
            }
        }
        calculatedHeight = (float) (temp[0] + temp[1] * 0.5
                + temp[2] * 0.25 + temp[3] * 0.125 + temp[4] * 0.0625);
        valueAtTimestamp[timestamp] = 0;
    }
    /**
     * This method returns the bin height consisting of
     * the last 5 values.
     * @return calculated bin height
     */
    public float getNotAccumulatedValue() {
        return calculatedHeight;
    }
    /**
     * This method returns the bin height consisting of
     * all last values.
     * @return accumulated bin height
     */
    public float getAccumulatedValue() {
        return valuesAccumulated;
    }
    /**
     * The last 5 values are set to 0.
     */
    public void reset() {
        for (int i = 0; i < 5; i++) {
            valueAtTimestamp[i] = 0;
        }
        calculatedHeight = 0;
    }
    /**
     * The accumulation of all values is
     * set to 0.
     */
    public void resetAccumulation() {
        valuesAccumulated = 0;
    }
}

/**
 * This class reads the incoming data from the
 * jSCSI server and stores them in a
 * synchronized list.
 * @author Halldór Janetzko
 */
class NetworkReader extends Thread {
    /**Stream to read the LoggingEvents from.*/
    private ObjectInputStream ois;
    /**Control object.*/
    private Control control;
    /**List in which the Strings from the network are stored.*/
    private List list;
    /**Boolean to check whether the Thread should be running.*/
    private boolean run = false;
    /**
     * The constructor of NetworkReader which needs a ObjectInputStream
     * and a backlink to Control.
     * @param stream - Stream to read the LoggingEvents from
     * @param ctr - Control object
     */
    public NetworkReader(final ObjectInputStream stream, final Control ctr) {
        this.ois = stream;
        this.control = ctr;
        list = Collections.synchronizedList(new LinkedList());
    }
    /**
     * This method return the list where the
     * events are stored in.
     * @return list of events
     */
    public List getList() {
        return list;
    }
    /**
     * Run-method of thread. Here are the objects read from the
     * network and stored as strings.
     */
    public void run() {
        run = true;
        while (run) {
            try {
                LoggingEvent le = (LoggingEvent) ois.readObject();
                String line = (String) le.getMessage();
                if (line.contains("teardown")) {
                    list.add(line);
                } else {
                    int length = Integer.parseInt(
                            line.substring(line.lastIndexOf(',') + 1));
                    line = line.substring(0, line.lastIndexOf(','));
                    int firstPos = Integer.parseInt(
                            line.substring(line.lastIndexOf(',') + 1));
                    line = line.substring(0, line.lastIndexOf(','));
                    if (line != null) {
                        for (int i = 0; i < length; i++) {
                            list.add(line + "," + (firstPos + i));
                        }
                    }
                }
                while (list.size() > 3 * control.getTimeResolution()) {
                    list.remove(0);
                }
            } catch (SocketException se) {
                final String exc = se.toString();
                if (control.getRunning()) {
                    control.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            MessageDialog.openError(control.getTopshell(),
                                    "Connection Error",
                                    "Connection to jSCSI Server lost!\n" + exc);
                        }
                    });
                }
                try {
                    Thread.sleep(1000);
                    run = false;
                    stopNetworkReader();
                } catch (Exception e) { }
            } catch (Exception e) { }
        }
    }
    /**
     * This method stops the Thread. It is called
     * when the shell of Control is disposed.
     */
    public void stopNetworkReader() {
        run = false;
        try {
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}