/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.preference;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
/**
 * A preference dialog is a hierarchical presentation of preference
 * pages.  Each page is represented by a node in the tree shown
 * on the left hand side of the dialog; when a node is selected, the
 * corresponding page is shown on the right hand side.
 */
public class PreferenceDialog
	extends Dialog
	implements IPreferencePageContainer {
	/**
	 * Title area fields
	 */
	public static final String PREF_DLG_TITLE_IMG = "preference_dialog_title_image"; //$NON-NLS-1$
	public static final String PREF_DLG_IMG_TITLE_ERROR = DLG_IMG_MESSAGE_ERROR; //$NON-NLS-1$

	//The id of the last page that was selected
	private static String lastPreferenceId = null;

	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(PREF_DLG_TITLE_IMG, ImageDescriptor.createFromFile(PreferenceDialog.class, "images/pref_dialog_title.gif")); //$NON-NLS-1$
	}

	private Composite titleArea;
	private CLabel messageLabel;
	private Label titleImage;
	private Color titleAreaColor;

	private String message = ""; //$NON-NLS-1$
	private String errorMessage;
	private Color normalMsgAreaBackground;
	private Color errorMsgAreaBackground;
	private Image messageImage;
	private Image errorMsgImage;
	private boolean showingError = false;
	private Point lastShellSize;

	/**
	 * Preference store, initially <code>null</code> meaning none.
	 *
	 * @see #setPreferenceStore
	 */
	private IPreferenceStore preferenceStore;

	/**
	 * The current preference page, or <code>null</code> if
	 * there is none.
	 */
	private IPreferencePage currentPage;

	/**
	 * The preference manager.
	 */
	private PreferenceManager preferenceManager;

	/**
	 * The Composite in which a page is shown.
	 */
	private Composite pageContainer;

	/**
	 * The minimum page size; 400 by 400 by default.
	 *
	 * @see #setMinimumPageSize
	 */
	private Point minimumPageSize = new Point(400, 400);

	/**
	 * The OK button.
	 */
	private Button okButton;

	/**
	 * The Cancel button.
	 */
	private Button cancelButton;

	/**
	 * The Help button; <code>null</code> if none.
	 */
	private Button helpButton = null;

	/**
	 * Indicates whether help is available; <code>false</code> by default.'
	 *
	 * @see #setHelpAvailable
	 */
	private boolean isHelpAvailable = false;

	/**
	 * The tree control.
	 */
	private Tree tree;

	/**
	 * The current tree item.
	 */
	private TreeItem currentTreeItem;

	/**
	 * Layout for the page container.
	 *
	 */
	private class PageLayout extends Layout {
		public void layout(Composite composite, boolean force) {
			Rectangle rect = composite.getClientArea();
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				children[i].setSize(rect.width, rect.height);
			}
		}
		public Point computeSize(
			Composite composite,
			int wHint,
			int hHint,
			boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
			int x = minimumPageSize.x;
			int y = minimumPageSize.y;

			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				Point size =
					children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
				x = Math.max(x, size.x);
				y = Math.max(y, size.y);
			}
			if (wHint != SWT.DEFAULT)
				x = wHint;
			if (hHint != SWT.DEFAULT)
				y = hHint;
			return new Point(x, y);
		}
	}

	/**
	 * Creates a new preference dialog under the control of the given preference 
	 * manager.
	 *
	 * @param shell the parent shell
	 * @param manager the preference manager
	 */
	public PreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		preferenceManager = manager;
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
			case IDialogConstants.OK_ID :
				{
					okPressed();
					return;
				}
			case IDialogConstants.CANCEL_ID :
				{
					cancelPressed();
					return;
				}
			case IDialogConstants.HELP_ID :
				{
					helpPressed();
					return;
				}
		}
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void cancelPressed() {
		// Inform all pages that we are cancelling
		Iterator nodes =
			preferenceManager
				.getElements(PreferenceManager.PRE_ORDER)
				.iterator();
		while (nodes.hasNext()) {
			final IPreferenceNode node = (IPreferenceNode) nodes.next();
			if (node.getPage() != null) {
				Platform.run(new SafeRunnable() {
					public void run() {
						if (!node.getPage().performCancel())
							return;
					}
				});

			}
		}
		setReturnCode(CANCEL);
		close();
	}
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	public boolean close() {

		List nodes = preferenceManager.getElements(PreferenceManager.PRE_ORDER);
		for (int i = 0; i < nodes.size(); i++) {
			IPreferenceNode node = (IPreferenceNode) nodes.get(i);
			node.disposeResources();
		}
		return super.close();
	}
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(JFaceResources.getString("PreferenceDialog.title")); //$NON-NLS-1$

		newShell.addShellListener(new ShellAdapter() {
			public void shellActivated(ShellEvent e) {
				if (lastShellSize == null)
					lastShellSize = getShell().getSize();
			}
		});
	}
	/*(non-Javadoc)
	 * Method declared on Window.
	 */
	protected void constrainShellSize() {
		super.constrainShellSize();
		// record opening shell size
		if (lastShellSize == null)
			lastShellSize = getShell().getSize();
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton =
			createButton(
				parent,
				IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL,
				true);
		getShell().setDefaultButton(okButton);
		cancelButton =
			createButton(
				parent,
				IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL,
				false);
		if (isHelpAvailable) {
			helpButton =
				createButton(
					parent,
					IDialogConstants.HELP_ID,
					IDialogConstants.HELP_LABEL,
					false);
		}
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createContents(final Composite parent) {
		final Control[] control = new Control[1];
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				control[0] = PreferenceDialog.super.createContents(parent);

				// Add the first page
				selectSavedItem();
			}
		});
		return control[0];
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		GridData gd;
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 2;
		((GridLayout) composite.getLayout()).horizontalSpacing = 10;

		// Build the tree an put it into the composite.
		createTree(composite);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 150;
		gd.verticalSpan = 2;
		tree.setLayoutData(gd);

		// Build the title area and separator line
		Composite titleComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		titleComposite.setLayout(layout);
		titleComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createTitleArea(titleComposite);

		// Build the Page container
		pageContainer = createPageContainer(composite);
		pageContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Build the separator line
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);

		return composite;
	}
	/**
	 * Creates the inner page container.
	 */
	private Composite createPageContainer(Composite parent) {
		Composite result = new Composite(parent, SWT.NULL);
		result.setLayout(new PageLayout());
		return result;
	}
	/**
	 * Creates the wizard's title area.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created title area composite
	 */
	private Composite createTitleArea(Composite parent) {
		// Create the title area which will contain
		// a title, message, and image.
		titleArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.numColumns = 2;

		// Get the background color for the title area
		Display display = parent.getDisplay();
		Color background = JFaceColors.getBannerBackground(display);
		final Color foreground = JFaceColors.getBannerForeground(display);

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		titleArea.setLayout(layout);
		titleArea.setLayoutData(layoutData);
		titleArea.setBackground(background);

		final Color borderColor =
			new Color(titleArea.getDisplay(), ViewForm.borderOutsideRGB);

		titleArea.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.setForeground(borderColor);
				Rectangle bounds = titleArea.getClientArea();
				bounds.height = bounds.height - 2;
				bounds.width = bounds.width - 1;
				e.gc.drawRectangle(bounds);
			}
		});

		// Add a dispose listener
		titleArea.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (titleAreaColor != null)
					titleAreaColor.dispose();
				if (errorMsgAreaBackground != null)
					errorMsgAreaBackground.dispose();
				borderColor.dispose();
			}
		});

		// Message label
		messageLabel = new CLabel(titleArea, SWT.LEFT);
		JFaceColors.setColors(messageLabel, foreground, background);
		messageLabel.setText(" "); //$NON-NLS-1$
		messageLabel.setFont(JFaceResources.getBannerFont());

		final IPropertyChangeListener fontListener =
			new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JFaceResources.BANNER_FONT.equals(event.getProperty()))
					updateMessage();
				if (JFaceResources.DIALOG_FONT.equals(event.getProperty())) {
					updateMessage();
					Font dialogFont = JFaceResources.getDialogFont();
					tree.setFont(dialogFont);
					Control[] children = ((Composite) buttonBar).getChildren();
					for (int i = 0; i < children.length; i++)
						children[i].setFont(dialogFont);
				}
			}
		};

		messageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				JFaceResources.getFontRegistry().removeListener(fontListener);
			}
		});

		JFaceResources.getFontRegistry().addListener(fontListener);

		GridData gd = new GridData(GridData.FILL_BOTH);
		messageLabel.setLayoutData(gd);

		// Title image
		titleImage = new Label(titleArea, SWT.LEFT);
		titleImage.setBackground(background);
		titleImage.setImage(JFaceResources.getImage(PREF_DLG_TITLE_IMG));
		gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		titleImage.setLayoutData(gd);

		return titleArea;
	}

	/**
	 * Creates a Tree/TreeItem structure that reflects the page hierarchy.
	 */
	private void createTree(Composite parent) {
		if (tree != null)
			tree.dispose();

		tree = new Tree(parent, SWT.BORDER);
		OpenStrategy eventAdapter = new OpenStrategy(tree);
		eventAdapter.addPostSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				BusyIndicator.showWhile(tree.getDisplay(), new Runnable() {
					public void run() {
						Object data = event.item.getData();
						if (data instanceof IPreferenceNode) {
							if (!isCurrentPageValid()) {
								handleError();
							} else if (!showPage((IPreferenceNode) data)) {
								// Page flipping wasn't successful
								handleError();
							} else {
								// Everything went well
								currentTreeItem = (TreeItem) event.item;
							}

							// Keep focus in tree.  See bugs 2692, 2621, and 6775.
							tree.setFocus();
						}
					}

					private void handleError() {
						showPageFlippingAbortDialog();
						selectCurrentPageAgain();
						clearSelectedNode();
					}
				});
			}
		});

		eventAdapter.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(final SelectionEvent event) {
				TreeItem[] selection = tree.getSelection();
				if (selection.length > 0)
					selection[0].setExpanded(!selection[0].getExpanded());
			}
		});

		//Register help listener on the tree to use context sensitive help
		tree.addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent event) {
				// call perform help on the current page
				if (currentPage != null) {
					currentPage.performHelp();
				}
			}
		});

		IPreferenceNode node = preferenceManager.getRoot();
		IPreferenceNode[] subnodes = node.getSubNodes();
		tree.setFont(parent.getFont());
		for (int i = 0; i < subnodes.length; i++) {
			createTreeItemFor(tree, subnodes[i]);
		}
	}
	/**
	 * Creates a TreeItem structure that reflects to the page hierarchy.
	 */
	protected void createTreeItemFor(Widget parent, IPreferenceNode node) {
		TreeItem item = null;
		if (parent instanceof Tree)
			item = new TreeItem((Tree) parent, SWT.DEFAULT);
		else
			item = new TreeItem((TreeItem) parent, SWT.DEFAULT);

		item.setData(node);
		item.setText(node.getLabelText());
		Image image = node.getLabelImage();
		if (image != null) {
			item.setImage(image);
		}

		IPreferenceNode[] subnodes = node.getSubNodes();
		for (int i = 0; i < subnodes.length; i++) {
			createTreeItemFor(item, subnodes[i]);
		}
	}
	/**
	 * Returns the preference mananger used by this preference dialog.
	 *
	 * @return the preference mananger
	 */
	public PreferenceManager getPreferenceManager() {
		return preferenceManager;
	}
	/* (non-Javadoc)
	 * Method declared on IPreferencePageDialog.
	 */
	public IPreferenceStore getPreferenceStore() {
		return preferenceStore;
	}
	/**
	 * Save the values specified in the pages.
	 * <p>
	 * The default implementation of this framework method saves all
	 * pages of type <code>PreferencePage</code> (if their store needs saving
	 * and is a <code>PreferenceStore</code>).
	 * </p>
	 * <p>
	 * Subclasses may override.
	 * </p>
	 */
	protected void handleSave() {
		Iterator nodes =
			preferenceManager
				.getElements(PreferenceManager.PRE_ORDER)
				.iterator();
		while (nodes.hasNext()) {
			IPreferenceNode node = (IPreferenceNode) nodes.next();
			IPreferencePage page = node.getPage();
			if (page instanceof PreferencePage) {
				// Save now in case tbe workbench does not shutdown cleanly
				IPreferenceStore store =
					((PreferencePage) page).getPreferenceStore();
				if (store != null
					&& store.needsSaving()
					&& store instanceof IPersistentPreferenceStore) {
					try {
						((IPersistentPreferenceStore) store).save();
					} catch (IOException e) {
						MessageDialog.openError(getShell(), JFaceResources.getString("PreferenceDialog.saveErrorTitle"), //$NON-NLS-1$
						JFaceResources.format("PreferenceDialog.saveErrorMessage", new Object[] { page.getTitle(), e.getMessage()})); //$NON-NLS-1$
					}
				}
			}
		}
	}
	/**
	 * Notifies that the window's close button was pressed, 
	 * the close menu was selected, or the ESCAPE key pressed.
	 * <p>
	 * The default implementation of this framework method
	 * sets the window's return code to <code>CANCEL</code>
	 * and closes the window using <code>close</code>.
	 * Subclasses may extend or reimplement.
	 * </p>
	 */
	protected void handleShellCloseEvent() {
		// handle the same as pressing cancel
		cancelPressed();
	}
	/**
	 * Notifies of the pressing of the Help button.
	 * <p>
	 * The default implementation of this framework method
	 * calls <code>performHelp</code> on the currently active page.
	 * </p>
	 */
	protected void helpPressed() {
		if (currentPage != null) {
			currentPage.performHelp();
		}
	}
	/**
	 * Returns whether the current page is valid.
	 *
	 * @return <code>false</code> if the current page is not valid, or
	 *  or <code>true</code> if the current page is valid or there is
	 *  no current page
	*/
	protected boolean isCurrentPageValid() {
		if (currentPage == null)
			return true;
		else
			return currentPage.isValid();
	}
	/**
	 * The preference dialog implementation of this <code>Dialog</code>
	 * framework method sends <code>performOk</code> to all pages of the 
	 * preference dialog, then calls <code>handleSave</code> on this dialog
	 * to save any state, and then calls <code>close</code> to close
	 * this dialog.
	 */
	protected void okPressed() {

		// Notify all the pages and give them a chance to abort
		Iterator nodes =
			preferenceManager
				.getElements(PreferenceManager.PRE_ORDER)
				.iterator();
		while (nodes.hasNext()) {
			IPreferenceNode node = (IPreferenceNode) nodes.next();
			IPreferencePage page = node.getPage();
			if (page != null) {
				if (!page.performOk())
					return;
			}
		}

		// Give subclasses the choice to save the state of the
		// preference pages.
		handleSave();

		close();
	}
	/**
	 * Selects the page determined by <code>currentTreeItem</code> in
	 * the page hierarchy.
	 */
	void selectCurrentPageAgain() {
		tree.setSelection(new TreeItem[] { currentTreeItem });
		currentPage.setVisible(true);
	}
	/**
	 * Selects the saved item in the tree of preference pages.
	 * If it cannot do this it saves the first one.
	 */
	protected void selectSavedItem() {
		if (tree != null) {
			int count = tree.getItemCount();
			if (count > 0) {
				TreeItem selectedItem = getLastSelectedNode(tree.getItems());
				Object data = selectedItem.getData();
				if (data instanceof IPreferenceNode) {
					tree.setSelection(new TreeItem[] { selectedItem });
					currentTreeItem = selectedItem;
					showPage((IPreferenceNode) data);

					// Keep focus in tree.  See bugs 2692, 2621, and 6775.
					tree.setFocus();
				}
			}
		}
	}

	/**
	 * Get the name of the selected item preference
	 */
	protected String getSelectedNodePreference() {
		return lastPreferenceId;
	}

	/**
	 * Get the name of the selected item preference
	 */
	protected void setSelectedNodePreference(String pageId) {
		lastPreferenceId = pageId;
	}

	/**
	 * Get the node that was last selected in the dialog store.
	 * If there is no match then return the first one,
	 */
	private TreeItem getLastSelectedNode(TreeItem[] items) {
		String lastSelectedNode = getSelectedNodePreference();

		if (lastSelectedNode == null)
			return items[0];

		TreeItem selectedItem = findNodeMatching(items, lastSelectedNode);
		if (selectedItem == null)
			return items[0];
		else
			return selectedItem;
	}

	/**
	 * Find the TreeItem that has data the same id as the nodeId.
	 * Search the children recursively.
	 * @return TreeItem or null if not found.
	 */
	private TreeItem findNodeMatching(TreeItem[] items, String nodeId) {

		for (int i = 0; i < items.length; i++) {
			Object data = items[i].getData();
			if (data instanceof IPreferenceNode) {
				if (((IPreferenceNode) data).getId().equals(nodeId))
					return items[i];
				else {
					TreeItem selectedChild =
						findNodeMatching(items[i].getItems(), nodeId);
					if (selectedChild != null)
						return selectedChild;
				}
			}
		}
		return null;
	}

	/**
	 * Clear the last selected node. This is so that we not chache
	 * the last selection in case of an error.
	 */
	void clearSelectedNode() {
		setSelectedNodePreference(null);
	}

	/**
	 * Save the currently selected node. 
	 */
	private void setSelectedNode() {

		String storeValue = null;

		if (tree.getSelectionCount() == 1) {
			TreeItem currentSelection = tree.getSelection()[0];
			Object data = currentSelection.getData();
			if (currentSelection.getData() instanceof IPreferenceNode)
				storeValue = ((IPreferenceNode) data).getId();
		}

		setSelectedNodePreference(storeValue);
	}

	/**
	 * Display the given error message. The currently displayed message
	 * is saved and will be redisplayed when the error message is set
	 * to <code>null</code>.
	 *
	 * @param errorMessage the errorMessage to display or <code>null</code>
	 */
	public void setErrorMessage(String newErrorMessage) {
		// Any change?
		if (errorMessage == null
			? newErrorMessage == null
			: errorMessage.equals(newErrorMessage))
			return;

		errorMessage = newErrorMessage;
		if (errorMessage == null) {
			if (showingError) {
				// we were previously showing an error
				showingError = false;
				messageLabel.setBackground(normalMsgAreaBackground);
				messageLabel.setImage(null);
				titleImage.setImage(
					JFaceResources.getImage(PREF_DLG_TITLE_IMG));
			}

			// avoid calling setMessage in case it is overridden to call setErrorMessage, 
			// which would result in a recursive infinite loop
			if (message == null)
				//this should probably never happen since setMessage does this conversion....
				message = ""; //$NON-NLS-1$
			messageLabel.setText(message);
			messageLabel.setImage(messageImage);
			messageLabel.setToolTipText(message);
		} else {
			messageLabel.setText(errorMessage);
			messageLabel.setToolTipText(errorMessage);
			if (!showingError) {
				// we were not previously showing an error
				showingError = true;

				// lazy initialize the error background color and image
				if (errorMsgAreaBackground == null) {
					errorMsgAreaBackground =
						JFaceColors.getErrorBackground(
							messageLabel.getDisplay());
					errorMsgImage =
						JFaceResources.getImage(PREF_DLG_IMG_TITLE_ERROR);
				}

				// show the error	
				normalMsgAreaBackground = messageLabel.getBackground();
				messageLabel.setBackground(errorMsgAreaBackground);
				messageLabel.setImage(errorMsgImage);
				titleImage.setImage(null);
			}
		}
		titleArea.layout(true);
	}
	/**
	 * Sets whether a Help button is available for this dialog.
	 * <p>
	 * Clients must call this framework method before the dialog's control
	 * has been created.
	 * <p>
	 *
	 * @param b <code>true</code> to include a Help button, 
	 *  and <code>false</code> to not include one (the default)
	 */
	public void setHelpAvailable(boolean b) {
		isHelpAvailable = b;
	}
	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 * <p>
	 * Shortcut for <code>setMessage(newMessage, NONE)</code>
	 * </p> 
	 * 
	 * @param newMessage the message, or <code>null</code> to clear
	 *   the message
	 */
	public void setMessage(String newMessage) {
		setMessage(newMessage, IMessageProvider.NONE);
	}
	/**
	 * Sets the message for this dialog with an indication of what type
	 * of message it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>, 
	 * <code>INFORMATION</code>, <code>WARNING</code>, or <code>ERROR</code>.
	 * </p>
	 * <p>
	 * Note that for backward compatibility, a message of type <code>ERROR</code> 
	 * is different than an error message (set using <code>setErrorMessage</code>). 
	 * An error message overrides the current message until the error message is 
	 * cleared. This method replaces the current message and does not affect the 
	 * error message.
	 * </p>
	 *
	 * @param newMessage the message, or <code>null</code> to clear
	 *   the message
	 * @param newType the message type
	 * @since 2.0
	 */
	public void setMessage(String newMessage, int newType) {
		Image newImage = null;

		if (newMessage != null) {
			switch (newType) {
				case IMessageProvider.NONE :
					break;
				case IMessageProvider.INFORMATION :
					newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_INFO);
					break;
				case IMessageProvider.WARNING :
					newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_WARNING);
					break;
				case IMessageProvider.ERROR :
					newImage = JFaceResources.getImage(DLG_IMG_MESSAGE_ERROR);
					break;
			}
		}

		showMessage(newMessage, newImage);
	}
	/**
	 * Sets the minimum page size.
	 *
	 * @param minWidth the minimum page width
	 * @param minHeight the minimum page height
	 * @see #setMinimumPageSize(Point)
	 */
	public void setMinimumPageSize(int minWidth, int minHeight) {
		minimumPageSize.x = minWidth;
		minimumPageSize.y = minHeight;
	}
	/**
	 * Sets the minimum page size.
	 *
	 * @param size the page size encoded as
	 *   <code>new Point(width,height)</code>
	 * @see #setMinimumPageSize(int,int)
	 */
	public void setMinimumPageSize(Point size) {
		minimumPageSize.x = size.x;
		minimumPageSize.y = size.y;
	}
	/**
	 * Sets the preference store for this preference dialog.
	 *
	 * @param store the preference store
	 * @see #getPreferenceStore
	 */
	public void setPreferenceStore(IPreferenceStore store) {
		Assert.isNotNull(store);
		preferenceStore = store;
	}
	/**
	 * Changes the shell size to the given size, ensuring that
	 * it is no larger than the display bounds.
	 * 
	 * @param width the shell width
	 * @param height the shell height
	 */
	private void setShellSize(int width, int height) {
		getShell().setSize(width, height);
		constrainShellSize();
	}
	/**
	 * Show the new message
	 */
	private void showMessage(String newMessage, Image newImage) {
		// Any change?
		if (message.equals(newMessage) && messageImage == newImage)
			return;

		message = newMessage;
		if (message == null)
			message = ""; //$NON-NLS-1$
		messageImage = newImage;

		if (!showingError) {
			// we are not showing an error
			messageLabel.setText(message);
			messageLabel.setImage(messageImage);
			messageLabel.setToolTipText(message);
		}
	}
	/**
	 * Shows the preference page corresponding to the given preference node.
	 * Does nothing if that page is already current.
	 *
	 * @param node the preference node, or <code>null</code> if none
	 * @return <code>true</code> if the page flip was successful, and
	 * <code>false</code> is unsuccessful
	 */
	protected boolean showPage(IPreferenceNode node) {
		if (node == null)
			return false;

		// Create the page if nessessary
		if (node.getPage() == null)
			node.createPage();

		if (node.getPage() == null)
			return false;

		IPreferencePage newPage = node.getPage();
		if (newPage == currentPage)
			return true;

		if (currentPage != null) {
			if (!currentPage.okToLeave())
				return false;
		}

		IPreferencePage oldPage = currentPage;
		currentPage = newPage;

		// Set the new page's container
		currentPage.setContainer(this);

		// Ensure that the page control has been created
		// (this allows lazy page control creation)
		if (currentPage.getControl() == null) {
			final boolean[] failed = { false };
			Platform.run(new ISafeRunnable() {
				public void run() {
					currentPage.createControl(pageContainer);
				}
				public void handleException(Throwable e) {
					failed[0] = true;
				}
			});
			if (failed[0])
				return false;

			// the page is responsible for ensuring the created control is accessable
			// via getControl.
			Assert.isNotNull(currentPage.getControl());
		}

		// Force calculation of the page's description label because
		// label can be wrapped.
		final Point[] size = new Point[1];
		final Point failed = new Point(-1, -1);

		Platform.run(new ISafeRunnable() {
			public void run() {
				size[0] = currentPage.computeSize();
			}
			public void handleException(Throwable e) {
				size[0] = failed;
			}
		});
		if (size[0].equals(failed))
			return false;

		Point contentSize = size[0];
		// Do we need resizing. Computation not needed if the
		// first page is inserted since computing the dialog's
		// size is done by calling dialog.open().
		// Also prevent auto resize if the user has manually resized
		Shell shell = getShell();
		Point shellSize = shell.getSize();
		if (oldPage != null) {
			Rectangle rect = pageContainer.getClientArea();
			Point containerSize = new Point(rect.width, rect.height);
			int hdiff = contentSize.x - containerSize.x;
			int vdiff = contentSize.y - containerSize.y;

			if (hdiff > 0 || vdiff > 0) {
				if (shellSize.equals(lastShellSize)) {
					hdiff = Math.max(0, hdiff);
					vdiff = Math.max(0, vdiff);
					setShellSize(shellSize.x + hdiff, shellSize.y + vdiff);
					lastShellSize = shell.getSize();
				} else {
					currentPage.setSize(containerSize);
				}
			} else if (hdiff < 0 || vdiff < 0) {
				currentPage.setSize(containerSize);
			}

		}

		// Ensure that all other pages are invisible 
		// (including ones that triggered an exception during
		// their creation).
		Control[] children = pageContainer.getChildren();
		Control currentControl = currentPage.getControl();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != currentControl)
				children[i].setVisible(false);
		}

		// Make the new page visible
		currentPage.setVisible(true);
		if (oldPage != null)
			oldPage.setVisible(false);

		// update the dialog controls
		update();

		return true;
	}
	/**
	 * Shows the "Page Flipping abort" dialog.
	 */
	void showPageFlippingAbortDialog() {
		MessageDialog.openError(getShell(), JFaceResources.getString("AbortPageFlippingDialog.title"), //$NON-NLS-1$
		JFaceResources.getString("AbortPageFlippingDialog.message")); //$NON-NLS-1$
	}
	/**
	 * Updates this dialog's controls to reflect the current page.
	 */
	protected void update() {
		// Update the title bar
		updateTitle();

		// Update the message line
		updateMessage();

		// Update the buttons
		updateButtons();

		//Saved the selected node in the preferences
		setSelectedNode();
	}
	/* (non-Javadoc)
	 * Method declared on IPreferenceContainer
	 */
	public void updateButtons() {
		okButton.setEnabled(isCurrentPageValid());
	}
	/* (non-Javadoc)
	 * Method declared on IPreferencePageContainer.
	 */
	public void updateMessage() {
		String pageMessage = currentPage.getMessage();
		int pageMessageType = IMessageProvider.NONE;
		if (pageMessage != null && currentPage instanceof IMessageProvider)
			pageMessageType = ((IMessageProvider) currentPage).getMessageType();

		String pageErrorMessage = currentPage.getErrorMessage();

		// Adjust the font
		if (pageMessage == null && pageErrorMessage == null)
			messageLabel.setFont(JFaceResources.getBannerFont());
		else
			messageLabel.setFont(JFaceResources.getDialogFont());

		// Set the message and error message	
		if (pageMessage == null) {
			setMessage(currentPage.getTitle());
		} else {
			setMessage(pageMessage, pageMessageType);
		}
		setErrorMessage(pageErrorMessage);
	}
	/* (non-Javadoc)
	 * Method declared on IPreferencePageContainer.
	 */
	public void updateTitle() {
		updateMessage();
	}
}
