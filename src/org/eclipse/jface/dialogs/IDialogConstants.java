package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Various dialog-related constants.
 * <p>
 * Within the dialog framework, all buttons are referred to
 * by a button id. Various common buttons, like "OK", "Cancel",
 * and "Finish", have pre-assigned button ids for convenience.
 * If an application requires other dialog buttons, they should
 * be assigned application-specific button ids counting up from
 * <code>CLIENT_ID</code>.
 * </p>
 * <p>
 * Button label constants are also provided for the common 
 * buttons. JFace automatically localizes these strings to
 * the current locale; that is, <code>YES_LABEL</code> would
 * be bound to the string <code>"Si"</code> in a Spanish
 * locale, but to <code>"Oui"</code> in a French one.
 * </p>
 * <p>
 * All margins, spacings, and sizes are given in
 * "dialog units" (DLUs), where
 * <ul>
 *  <li>1 horizontal DLU = 1/4 average character width</li>
 *  <li>1 vertical DLU = 1/8 average character height</li>
 * </ul>
 * </p>
 */
import org.eclipse.jface.resource.JFaceResources;

public interface IDialogConstants {

	// button ids

	/**
	 * Button id for an "Ok" button (value 0).
	 */
	public int OK_ID = 0;

	/**
	 * Button id for a "Cancel" button (value 1).
	 */
	public int CANCEL_ID = 1;

	/**
	 * Button id for a "Yes" button (value 2).
	 */
	public int YES_ID = 2;

	/**
	 * Button id for a "No" button (value 3).
	 */
	public int NO_ID = 3;

	/**
	 * Button id for a "Yes to All" button (value 4).
	 */
	public int YES_TO_ALL_ID = 4;

	/**
	 * Button id for a "Skip" button (value 5).
	 */
	public int SKIP_ID = 5;

	/**
	 * Button id for a "Stop" button (value 6).
	 */
	public int STOP_ID = 6;

	/**
	 * Button id for an "Abort" button (value 7).
	 */
	public int ABORT_ID = 7;

	/**
	 * Button id for a "Retry" button (value 8).
	 */
	public int RETRY_ID = 8;

	/**
	 * Button id for an "Ignore" button (value 9).
	 */
	public int IGNORE_ID = 9;

	/**
	 * Button id for a "Proceed" button (value 10).
	 */
	public int PROCEED_ID = 10;

	/**
	 * Button id for an "Open" button (value 11).
	 */
	public int OPEN_ID = 11;

	/**
	 * Button id for a "Close" button (value 12).
	 */
	public int CLOSE_ID = 12;

	/**
	 * Button id for a "Details" button (value 13).
	 */
	public int DETAILS_ID = 13;

	/**
	 * Button id for a "Back" button (value 14).
	 */
	public int BACK_ID = 14;

	/**
	 * Button id for a "Next" button (value 15).
	 */
	public int NEXT_ID = 15;

	/**
	 * Button id for a "Finish" button (value 16).
	 */
	public int FINISH_ID = 16;

	/**
	 * Button id for a "Help" button (value 17).
	 */
	public int HELP_ID = 17;

	/**
	 * Button id for a "Select All" button (value 18).
	 */
	public int SELECT_ALL_ID = 18;

	/**
	 * Button id for a "Deselect All" button (value 19).
	 */
	public int DESELECT_ALL_ID = 19;
	
	/**
	 * Button id for a "Select types" button (value 20).
	 */
	public int SELECT_TYPES_ID = 20;
	
	/**
	 * Button id for a "No to All" button (value 21).
	 */
	public int NO_TO_ALL_ID = 21;
	

	/**
	 * Starting button id reserved for internal use by 
	 * JFace (value 256). JFace classes make ids by
	 * adding to this number.
	 */
	public int INTERNAL_ID = 256;

	/**
	 * Starting button id reserved for use by 
	 * clients of JFace (value 1024). Clients of JFace should 
	 * make ids by adding to this number.
	 */
	public int CLIENT_ID = 1024;

	// button labels
	public String OK_LABEL = JFaceResources.getString("ok"); //$NON-NLS-1$
	public String CANCEL_LABEL = JFaceResources.getString("cancel"); //$NON-NLS-1$
	public String YES_LABEL = JFaceResources.getString("yes"); //$NON-NLS-1$
	public String NO_LABEL = JFaceResources.getString("no"); //$NON-NLS-1$
	public String NO_TO_ALL_LABEL = JFaceResources.getString("notoall"); //$NON-NLS-1$
	public String YES_TO_ALL_LABEL = JFaceResources.getString("yestoall"); //$NON-NLS-1$
	public String SKIP_LABEL = JFaceResources.getString("skip"); //$NON-NLS-1$
	public String STOP_LABEL = JFaceResources.getString("stop"); //$NON-NLS-1$
	public String ABORT_LABEL = JFaceResources.getString("abort"); //$NON-NLS-1$
	public String RETRY_LABEL = JFaceResources.getString("retry"); //$NON-NLS-1$
	public String IGNORE_LABEL = JFaceResources.getString("ignore"); //$NON-NLS-1$
	public String PROCEED_LABEL = JFaceResources.getString("proceed"); //$NON-NLS-1$
	public String OPEN_LABEL = JFaceResources.getString("open"); //$NON-NLS-1$
	public String CLOSE_LABEL = JFaceResources.getString("close"); //$NON-NLS-1$
	public String SHOW_DETAILS_LABEL = JFaceResources.getString("showDetails"); //$NON-NLS-1$
	public String HIDE_DETAILS_LABEL = JFaceResources.getString("hideDetails"); //$NON-NLS-1$
	public String BACK_LABEL = JFaceResources.getString("backButton"); //$NON-NLS-1$
	public String NEXT_LABEL = JFaceResources.getString("nextButton"); //$NON-NLS-1$
	public String FINISH_LABEL = JFaceResources.getString("finish"); //$NON-NLS-1$
	public String HELP_LABEL = JFaceResources.getString("help"); //$NON-NLS-1$

	// Margins, spacings, and sizes 
	/**
	 * Vertical margin in dialog units (value 7).
	 */
	public int VERTICAL_MARGIN = 7;

	/**
	 * Vertical spacing in dialog units (value 4).
	 */
	public int VERTICAL_SPACING = 4;

	/**
	 * Horizontal margin in dialog units (value 7).
	 */
	public int HORIZONTAL_MARGIN = 7;

	/**
	 * Horizontal spacing in dialog units (value 4).
	 */
	public int HORIZONTAL_SPACING = 4;

	/**
	 * Height of button bar in dialog units (value 25).
	 */
	public int BUTTON_BAR_HEIGHT = 25;
	
	/**
	 * Left margin in dialog units (value 20).
	 */
	public int LEFT_MARGIN = 20;
	
	/**
	 * Button margin in dialog units (value 4).
	 */
	public int BUTTON_MARGIN = 4;
	
	/**
	 * Button height in dialog units (value 14).
	 */
	public int BUTTON_HEIGHT = 14;
	
	/**
	 * Button width in dialog units (value 61).
	 */
	public int BUTTON_WIDTH = 61; 
	
	/**
	 * Indent in dialog units (value 21).
	 */
	public int INDENT = 21;
	
	/**
	 * Small indent in dialog units (value 7).
	 */
	public int SMALL_INDENT = 7;

	/**
	 * Entry field width in dialog units (value 200).
	 */
	public int ENTRY_FIELD_WIDTH = 200;

	/**
	 * Minimum width of message area in dialog units (value 300).
	 */
	public int MINIMUM_MESSAGE_AREA_WIDTH = 300;
}
