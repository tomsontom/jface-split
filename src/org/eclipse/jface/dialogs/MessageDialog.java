package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.resource.*;

/**
 * A dialog for showing messages to the user.
 * <p>
 * This concrete dialog class can be instantiated as is, 
 * or further subclassed as required.
 * </p>
 */
public class MessageDialog extends IconAndMessageDialog {
	

	/**
	 * 	Constant for a dialog with no image (value 0).
	 */
	public final static int NONE = 0;

	/**
	 * 	Constant for a dialog with an error image (value 1).
	 */
	public final static int ERROR = 1;

	/**
	 * 	Constant for a dialog with an info image (value 2).
	 */
	public final static int INFORMATION = 2;

	/**
	 * 	Constant for a dialog with a question image (value 3).
	 */
	public final static int QUESTION = 3;

	/**
	 * 	Constant for a dialog with a warning image (value 4).
	 */
	public final static int WARNING = 4;

	/**
	 * Labels for buttons in the button bar (localized strings).
	 */
	private String[] buttonLabels;

	/**
	 * The buttons. Parallels <code>buttonLabels</code>.
	 */
	private Button[] buttons;

	/**
	 * Index into <code>buttonLabels</code> of the default button.
	 */
	private int defaultButtonIndex;

	/**
	 * Dialog title (a localized string).
	 */
	private String title;

	/**
	 * Dialog title image.
	 */
	private Image titleImage;

	/**
	 * Image, or <code>null</code> if none.
	 */
	private Image image = null;
	
	/**
	 * The custom dialog area.	 */
	private Control customArea;
/**
 * Create a message dialog.
 * Note that the dialog will have no visual representation (no widgets)
 * until it is told to open.
 * <p>
 * The labels of the buttons to appear in the button bar are supplied in this
 * constructor as an array. The <code>open</code> method will return the index
 * of the label in this array corresponding to the button that was pressed to 
 * close the dialog. If the dialog was dismissed without pressing a button (ESC, etc.)
 * then -1 is returned. Note that the <code>open</code> method blocks.
 * </p>
 *
 * @param parentShell the parent shell
 * @param dialogTitle the dialog title, or <code>null</code> if none
 * @param dialogTitleImage the dialog title image, or <code>null</code> if none
 * @param dialogMessage the dialog message
 * @param dialogImageType one of the following values:
 * <ul>
 *	<li> <code>MessageDialog.NONE</code> for a dialog with no image </li>
 *	<li> <code>MessageDialog.ERROR</code> for a dialog with an error image </li>
 *	<li> <code>MessageDialog.INFORMATION</code> for a dialog with an information image </li>
 * 	<li> <code>MessageDialog.QUESTION </code> for a dialog with a question image </li>
 *	<li> <code>MessageDialog.WARNING</code> for a dialog with a warning image </li>
 * </ul>
 * @param dialogButtonLabels an array of labels for the buttons in the button bar
 * @param defaultIndex the index in the button label array of the default button
 */
public MessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
	super(parentShell);
	this.title = dialogTitle;
	this.titleImage = dialogTitleImage;
	this.message = dialogMessage;
	switch (dialogImageType) {
		case ERROR : {
			this.image = getImage(DLG_IMG_ERROR);
			break;
		} 
		case INFORMATION : {
			this.image = getImage(DLG_IMG_INFO);
			break;
		} 
		case QUESTION : {
			this.image = getImage(DLG_IMG_QUESTION);
			break;
		} 
		case WARNING : {
			this.image = getImage(DLG_IMG_WARNING);
			break;
		} 
	}
	this.buttonLabels = dialogButtonLabels;
	this.defaultButtonIndex = defaultIndex;
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void buttonPressed(int buttonId) {
	setReturnCode(buttonId);
	close();
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	if (title != null)
		shell.setText(title);
	if (titleImage != null)
		shell.setImage(titleImage);
}

/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void createButtonsForButtonBar(Composite parent) {
	buttons = new Button[buttonLabels.length];
	for (int i = 0; i < buttonLabels.length; i++) {
		String label = buttonLabels[i];
		Button button = createButton(parent, i, label, defaultButtonIndex == i);
		buttons[i] = button;
	}
}
/**
 * Creates and returns the contents of an area
 * of the dialog which appears below the message and
 * above the button bar.
 * <p>
 * The default implementation of this framework method
 * returns <code>null</code>. Subclasses may override.
 * </p>
 *
 * @param the parent composite to contain the custom area
 * @return the custom area control, or <code>null</code>
 */
protected Control createCustomArea(Composite parent) {
	return null;
}
/**
 * This implementation of the <code>Dialog</code> framework 
 * method creates and lays out a composite and calls 
 * <code>createMessageArea</code> and <code>createCustomArea</code>
 * to populate it. Subclasses should override <code>createCustomArea</code>
 * to add contents below the message.
 */
protected Control createDialogArea(Composite parent) {
	
	// create message area
	createMessageArea(parent);
	
	// create the top level composite for the dialog area
	Composite composite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	composite.setLayout(layout);
	
	GridData data = new GridData(GridData.FILL_BOTH);
	data.horizontalSpan = 2;
	
	composite.setLayoutData(data);
	composite.setFont(parent.getFont());

	// allow subclasses to add custom controls
	customArea = createCustomArea(composite);
	
	//If it is null create a dummy label for spacing purposes
	if(customArea == null)
		customArea = new Label(composite,SWT.NULL);

	return composite;
}
/**
 * Gets a button in this dialog's button bar.
 *
 * @param index the index of the button in the dialog's button bar
 * @return a button in the dialog's button bar
 */
protected Button getButton(int index) {
	return buttons[index];
}
/**
 * Returns the minimum message area width in pixels
 * This determines the minimum width of the dialog.
 * <p>
 * Subclasses may override.
 * </p>
 *
 * @return the minimum message area width (in pixels)
 */
protected int getMinimumMessageWidth() {
	return convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
}
/* (non-Javadoc)
 * Method declared on Dialog.
 * Sets a return code of -1 since none of the dialog buttons were pressed to close the dialog.
 */
protected void handleShellCloseEvent() {
	super.handleShellCloseEvent();
	setReturnCode(-1);
}
/** 
 * Convenience method to open a simple confirm (OK/Cancel) dialog.
 *
 * @param parent the parent shell of the dialog, or <code>null</code> if none
 * @param title the dialog's title, or <code>null</code> if none
 * @param message the message
 * @return <code>true</code> if the user presses the OK button,
 *    <code>false</code> otherwise
 */
public static boolean openConfirm(Shell parent, String title, String message) {
	MessageDialog dialog = new MessageDialog(
		parent, 
		title, 
		null,	// accept the default window icon
		message, 
		QUESTION, 
		new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
		0); 	// OK is the default
	return dialog.open() == 0;
}
/** 
 * Convenience method to open a standard error dialog.
 *
 * @param parent the parent shell of the dialog, or <code>null</code> if none
 * @param title the dialog's title, or <code>null</code> if none
 * @param message the message
 */
public static void openError(Shell parent, String title, String message) {
	MessageDialog dialog = new MessageDialog(
		parent,
		title, 
		null,	// accept the default window icon
		message, 
		ERROR, 
		new String[] {IDialogConstants.OK_LABEL}, 
		0); 	// ok is the default
	dialog.open();
	return;
}
/** 
 * Convenience method to open a standard information dialog.
 *
 * @param parent the parent shell of the dialog, or <code>null</code> if none
 * @param title the dialog's title, or <code>null</code> if none
 * @param message the message
 */
public static void openInformation(
	Shell parent,
	String title,
	String message) {
		MessageDialog dialog =
			new MessageDialog(parent, title, null, // accept the default window icon
	message, INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0);
	// ok is the default
	dialog.open();
	return;
}
/** 
 * Convenience method to open a simple Yes/No question dialog.
 *
 * @param parent the parent shell of the dialog, or <code>null</code> if none
 * @param title the dialog's title, or <code>null</code> if none
 * @param message the message
 * @return <code>true</code> if the user presses the OK button,
 *    <code>false</code> otherwise
 */
public static boolean openQuestion(Shell parent, String title, String message) {
	MessageDialog dialog = new MessageDialog(
		parent,
		title, 
		null,	// accept the default window icon
		message, 
		QUESTION, 
		new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, 
		0); 	// yes is the default
	return dialog.open() == 0;
}
/** 
 * Convenience method to open a standard warning dialog.
 *
 * @param parent the parent shell of the dialog, or <code>null</code> if none
 * @param title the dialog's title, or <code>null</code> if none
 * @param message the message
 */
public static void openWarning(Shell parent, String title, String message) {
	MessageDialog dialog = new MessageDialog(
		parent,
		title, 
		null,	// accept the default window icon
		message, 
		WARNING, 
		new String[] {IDialogConstants.OK_LABEL}, 
		0); 	// ok is the default
	dialog.open();
	return;
}
/**
 * @see org.eclipse.jface.dialogs.Dialog#createButton(org.eclipse.swt.widgets.Composite, int, java.lang.String, boolean)
 */
protected Button createButton(Composite parent,int id,String label,boolean defaultButton) {
	
	Button button = super.createButton(parent, id, label, defaultButton);
	//Be sure to set the focus if the custom area cannot so as not
	//to lose the defaultButton.
	if(defaultButton && !customShouldTakeFocus())
		button.setFocus();
	return button;
}
/**
 * Return whether or not we should apply the workaround where
 * we take focus for the default button or if that should be 
 * determined by the dialog.
 * By default only return true if the custom area is a label
 * or CLabel that cannot take focus. * @return boolean */
protected boolean customShouldTakeFocus() {
	if(customArea instanceof Label) 
		return false;
	
	if(customArea instanceof CLabel)
		return (customArea.getStyle() & SWT.NO_FOCUS) > 0;
	
	return true;	
}

	/*
	 * @see IconAndMessageDialog#getImage()
	 */
	public Image getImage() {
		return image;
	}
}
