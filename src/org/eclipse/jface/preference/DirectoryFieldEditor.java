package org.eclipse.jface.preference;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import java.io.File;

/**
 * A field editor for a directory path type preference. A standard directory
 * dialog appears when the user presses the change button.
 */
public class DirectoryFieldEditor extends StringButtonFieldEditor {
/**
 * Creates a new directory field editor 
 */
protected DirectoryFieldEditor() {
}
/**
 * Creates a directory field editor.
 * 
 * @param name the name of the preference this field editor works on
 * @param labelText the label text of the field editor
 * @param parent the parent of the field editor's control
 */
public DirectoryFieldEditor(String name, String labelText, Composite parent) {
	init(name, labelText);
	setErrorMessage(JFaceResources.getString("DirectoryFieldEditor.errorMessage"));//$NON-NLS-1$
	setChangeButtonText(JFaceResources.getString("openBrowse"));//$NON-NLS-1$
	setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
	createControl(parent);
}
/* (non-Javadoc)
 * Method declared on StringButtonFieldEditor.
 * Opens the directory chooser dialog and returns the selected directory.
 */
protected String changePressed() {
	File f = new File(getTextControl().getText());
	if (!f.exists())
		f = null;
	File d = getDirectory(f);
	if (d == null)
		return null;

	return d.getAbsolutePath();
}
/* (non-Javadoc)
 * Method declared on StringFieldEditor.
 * Checks whether the text input field contains a valid directory.
 */
protected boolean doCheckState() {
	String fileName = getTextControl().getText();
	fileName = fileName.trim();
	if (fileName.length() == 0 && isEmptyStringAllowed())
		return true;
	File file = new File(fileName);
	return file.isDirectory();
}
/**
 * Helper that opens the directory chooser dialog.
 */
private File getDirectory(File startingDirectory) {

	DirectoryDialog fileDialog = new DirectoryDialog(getShell(), SWT.OPEN);
	if (startingDirectory != null)
		fileDialog.setFilterPath(startingDirectory.getPath());
	String dir = fileDialog.open();
	if (dir != null) {
		dir = dir.trim();
		if (dir.length() > 0)
			return new File(dir);
	}

	return null;
}
}
