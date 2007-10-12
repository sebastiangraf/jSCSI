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
 * $Id: ViSCSI_Preferences.java 22 2007-04-25 09:17:58Z mkramis $
 * 
 */

package org.jscsi.whiskas.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.jscsi.whiskas.Activator;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class ViSCSI_Preferences
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public ViSCSI_Preferences() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preference page for ViSCSI Plugin. All changes "+
				"will take effect for the next pressing of the \"Apply\"-Button.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		/*addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
				"&Directory preference:", getFieldEditorParent()));
		addField(
			new BooleanFieldEditor(
				PreferenceConstants.P_BOOLEAN,
				"&An example of a boolean preference",
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_CHOICE,
			"An example of a multiple-choice preference",
			1,
			new String[][] { { "&Choice 1", "choice1" }, {
				"C&hoice 2", "choice2" }
		}, getFieldEditorParent()));*/
		IntegerFieldEditor hits_per_paint = new IntegerFieldEditor(
				PreferenceConstants.P_INT_HITS_PER_PAINT, 
				"Number of Hits per painting:",getFieldEditorParent(),5);
		hits_per_paint.setValidRange(500, 30000);
		addField(hits_per_paint);
		StringFieldEditor default_log_file = new StringFieldEditor(
				PreferenceConstants.P_STRING_DEFAULT_LOG_FILE,
				"Default value for box \"iSCSI Server\":",
				getFieldEditorParent());
		addField(default_log_file);
		StringFieldEditor default_type_file = new StringFieldEditor(
				PreferenceConstants.P_STRING_DEFAULT_TYPE_FILE,
				"Default value for box \"Type-File\":",
				getFieldEditorParent());
		addField(default_type_file);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}