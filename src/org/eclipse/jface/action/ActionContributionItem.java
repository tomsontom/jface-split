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
package org.eclipse.jface.action;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A contribution item which delegates to an action.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class ActionContributionItem extends ContributionItem {

	private static boolean USE_COLOR_ICONS = true;
	
	private static ImageCache globalImageCache;
	
	/**
	 * The action.
	 */
	private IAction action;

	/**
	 * The widget created for this item; <code>null</code>
	 * before creation and after disposal.
	 */
	private Widget widget = null;

	/**
	 * Remembers the parent widget.
	 */
	private Widget parentWidget = null;
	
	/**
	 * Listener for action property change notifications.
	 */
	private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			actionPropertyChange(event);
		}
	};

	/**
	 * Listener for SWT button widget events.
	 */
	private Listener buttonListener;
	
	/**
	 * Listener for SWT tool item widget events.
	 */
	private Listener toolItemListener;
	
	/**
	 * Listener for SWT menu item widget events.
	 */
	private Listener menuItemListener;
	
	private static class ImageCache {
		/** Map from ImageDescriptor to Entry */
		private Map entries = new HashMap(11);
		private Image missingImage;
		
		private class Entry {
			Image image;
			Image grayImage;

			void dispose() {
				if (image != null) {
					image.dispose();
					image = null;
				}
				if (grayImage != null) {
					grayImage.dispose();
					grayImage = null;
				}
			}
		}

		Entry getEntry(ImageDescriptor desc) {
			Entry entry = (Entry) entries.get(desc);
			if (entry == null) {
				entry = new Entry();
				entries.put(desc, entry);
			}
			return entry;
		}

		Image getImage(ImageDescriptor desc) {
			if (desc == null) {
				return null;
			}
			Entry entry = getEntry(desc);
			if (entry.image == null) {
				entry.image = desc.createImage();
			}
			return entry.image;
		}

		Image getGrayImage(ImageDescriptor desc) {
			if (desc == null) {
				return null;
			}
			Entry entry = getEntry(desc);
			if (entry.grayImage == null) {
				Image image = getImage(desc);
				if (image != null) {
					entry.grayImage = new Image(null, image, SWT.IMAGE_GRAY);
				}
			}
			return entry.grayImage;
		}

		Image getMissingImage() {
			if (missingImage == null) {
				missingImage = getImage(ImageDescriptor.getMissingImageDescriptor());
			}
			return missingImage;
		}
		
		void dispose() {
			for (Iterator i = entries.values().iterator(); i.hasNext();) {
				Entry entry = (Entry) i.next();
				entry.dispose();
			}
			entries.clear();
		}
	}

public boolean isVisible() {
	IAction action = getAction();
	
	if (action != null) {
		String commandId = action.getActionDefinitionId();
		CommandResolver.ICallback callback = CommandResolver.getInstance().getCommandResolver();
		
		if (callback != null)
			return callback.isActive(commandId);
	}
	
	return super.isVisible();	
}

/**
 * Returns whether color icons should be used in toolbars.
 * 
 * @return <code>true</code> if color icons should be used in toolbars, 
 *   <code>false</code> otherwise
 */
public static boolean getUseColorIconsInToolbars() {
	return USE_COLOR_ICONS;
}

/**
 * Sets whether color icons should be used in toolbars.
 * 
 * @param useColorIcons <code>true</code> if color icons should be used in toolbars, 
 *   <code>false</code> otherwise
 */
public static void setUseColorIconsInToolbars(boolean useColorIcons) {
	USE_COLOR_ICONS = useColorIcons;
}

/**
 * Creates a new contribution item from the given action.
 * The id of the action is used as the id of the item.
 *
 * @param action the action
 */
public ActionContributionItem(IAction action) {
	super(action.getId());
	this.action = action;
}
/**
 * Handles a property change event on the action (forwarded by nested listener).
 */
private void actionPropertyChange(final PropertyChangeEvent e) {
	// This code should be removed. Avoid using free asyncExec

	if (isVisible() && widget != null) {
		Display display = widget.getDisplay();
		if (display.getThread() == Thread.currentThread()) {
			update(e.getProperty());
		}
		else {
			display.asyncExec(new Runnable() {
				public void run() {
					update(e.getProperty());
				}
			});
		}	

	}	
}
/**
 * Checks whether the given menu item belongs to a context menu
 * (the one that pops up if the user presses the right mouse button).
 */
private static boolean belongsToContextMenu(MenuItem item) {
	Menu menu = item.getParent();
	if (menu == null)
		return false;
	while (menu.getParentMenu() != null)
		menu = menu.getParentMenu();
	return (menu.getStyle() & SWT.BAR) == 0;
}
/**
 * Compares this action contribution item with another object.
 * Two action contribution items are equal if they refer to the identical Action.
 */
public boolean equals(Object o) {
	if (!(o instanceof ActionContributionItem)) {
		return false;
	}
	return action.equals(((ActionContributionItem) o).action);
}
/**
 * The <code>ActionContributionItem</code> implementation of this
 * <code>IContributionItem</code> method creates an SWT <code>Button</code> for
 * the action using the action's style. If the action's checked property has
 * been set, the button is created and primed to the value of the checked
 * property.
 */
public void fill(Composite parent) {
	if (widget == null && parent != null) {
		int flags = SWT.PUSH;
		if (action != null) {
			if (action.getStyle() == IAction.AS_CHECK_BOX)
				flags = SWT.TOGGLE;
			if (action.getStyle() == IAction.AS_RADIO_BUTTON)
				flags = SWT.RADIO;
		}
		
		Button b = new Button(parent, flags);
		b.setData(this);
		b.addListener(SWT.Dispose, getButtonListener());
		// Don't hook a dispose listener on the parent
		b.addListener(SWT.Selection, getButtonListener());
		if (action.getHelpListener() != null)
			b.addHelpListener(action.getHelpListener());
		widget = b;
		parentWidget = parent;
		
		update(null);
		
		action.addPropertyChangeListener(propertyListener);
	}
}
/**
 * The <code>ActionContributionItem</code> implementation of this
 * <code>IContributionItem</code> method creates an SWT <code>MenuItem</code>
 * for the action using the action's style. If the action's checked property has
 * been set, a button is created and primed to the value of the checked
 * property. If the action's menu creator property has been set, a cascading
 * submenu is created.
 */
public void fill(Menu parent, int index) {
	if (widget == null && parent != null) {
		Menu subMenu= null;
		int flags = SWT.PUSH;
		if (action != null) {
			int style = action.getStyle();
			if (style == IAction.AS_CHECK_BOX)
				flags= SWT.CHECK;
			else if (style == IAction.AS_RADIO_BUTTON)
				flags = SWT.RADIO;
			else if (style == IAction.AS_DROP_DOWN_MENU) {
				IMenuCreator mc = action.getMenuCreator();
				if (mc != null) {
					subMenu = mc.getMenu(parent);
					flags = SWT.CASCADE;
				}
			}
		}
		
		MenuItem mi = null;
		if (index >= 0)
			mi = new MenuItem(parent, flags, index);
		else
			mi = new MenuItem(parent, flags);
		widget = mi;
		parentWidget = parent;

		mi.setData(this);
		mi.addListener(SWT.Dispose, getMenuItemListener());
		mi.addListener(SWT.Selection, getMenuItemListener());
		if (action.getHelpListener() != null)
			mi.addHelpListener(action.getHelpListener());
		
		if (subMenu != null)
			mi.setMenu(subMenu);
					
		update(null);
		
		action.addPropertyChangeListener(propertyListener);
	}
}
/**
 * The <code>ActionContributionItem</code> implementation of this ,
 * <code>IContributionItem</code> method creates an SWT <code>ToolItem</code>
 * for the action using the action's style. If the action's checked property has
 * been set, a button is created and primed to the value of the checked
 * property. If the action's menu creator property has been set, a drop-down
 * tool item is created.
 */
public void fill(ToolBar parent, int index) {
	if (widget == null && parent != null) {
		int flags = SWT.PUSH;
		if (action != null) {
			int style = action.getStyle();
			if (style == IAction.AS_CHECK_BOX)
				flags = SWT.CHECK;
			else if (style == IAction.AS_RADIO_BUTTON)
				flags = SWT.RADIO;
			else if (style == IAction.AS_DROP_DOWN_MENU)
				flags = SWT.DROP_DOWN;					
		}
		
		ToolItem ti = null;
		if (index >= 0)
			ti = new ToolItem(parent, flags, index);
		else
			ti = new ToolItem(parent, flags);
		ti.setData(this);
		ti.addListener(SWT.Selection, getToolItemListener());
		ti.addListener(SWT.Dispose, getToolItemListener());

		widget = ti;
		parentWidget = parent;
		
		update(null);
		
		action.addPropertyChangeListener(propertyListener);
	}
}
/**
 * Returns the action associated with this contribution item.
 *
 * @return the action
 */
public IAction getAction() {
	return action;
}
/**
 * Returns the image cache.
 * The cache is global, and is shared by all action contribution items.
 * This has the disadvantage that once an image is allocated, it is never freed until the display
 * is disposed.  However, it has the advantage that the same image in different contribution managers
 * is only ever created once.
 */
private ImageCache getImageCache() {
	ImageCache cache = globalImageCache;
	if (cache == null) {
		globalImageCache = cache = new ImageCache();
		Display display = Display.getDefault();
		if (display != null) {
			display.disposeExec(new Runnable() {
				public void run() {
					if (globalImageCache != null) {
						globalImageCache.dispose();
						globalImageCache = null;
					}
				}	
			});
		}
	}
	return cache;
}
/**
 * Returns the listener for SWT button widget events.
 * 
 * @return a listener for button events
 */
private Listener getButtonListener() {
	if (buttonListener == null) {
		buttonListener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						Widget ew = event.widget;
						if (ew != null) {
							handleWidgetSelection(event, ((Button)ew).getSelection());
						}
						break;
				}
			}
		};
	}
	return buttonListener;
}
/**
 * Returns the listener for SWT tool item widget events.
 * 
 * @return a listener for tool item events
 */
private Listener getToolItemListener() {
	if (toolItemListener == null) {
		toolItemListener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						Widget ew = event.widget;
						if (ew != null) {
							handleWidgetSelection(event, ((ToolItem)ew).getSelection());
						}
						break;
				}
			}
		};
	}
	return toolItemListener;
}
/**
 * Returns the listener for SWT menu item widget events.
 * 
 * @return a listener for menu item events
 */
private Listener getMenuItemListener() {
	if (menuItemListener == null) {
		menuItemListener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						Widget ew = event.widget;
						if (ew != null) {
							handleWidgetSelection(event, ((MenuItem)ew).getSelection());
						}
						break;
				}
			}
		};
	}
	return menuItemListener;
}
/**
 * Handles a widget dispose event for the widget corresponding to this item.
 */
private void handleWidgetDispose(Event e) {
	if (e.widget == widget) {
		// the item is being disposed
		if (action.getStyle() == IAction.AS_DROP_DOWN_MENU)  {
			IMenuCreator mc = action.getMenuCreator();
			if (mc != null) { 
				mc.dispose(); 
			}
		}
		action.removePropertyChangeListener(propertyListener);
		widget = null;
	}
}
/**
 * Handles a widget selection event.
 */
private void handleWidgetSelection(Event e, boolean selection) {
	  
	Widget item= e.widget;
	if (item != null) {
		int style = item.getStyle();
					
		if ((style & (SWT.TOGGLE | SWT.CHECK)) != 0) {
			if (action.getStyle() == IAction.AS_CHECK_BOX) {
				action.setChecked(selection);
			}
		} else if ((style & SWT.RADIO) != 0) {
			if (action.getStyle() == IAction.AS_RADIO_BUTTON) {
				action.setChecked(selection);
			}
		} else if ((style & SWT.DROP_DOWN) != 0) {
			if (e.detail == 4) {	// on drop-down button
				if (action.getStyle() == IAction.AS_DROP_DOWN_MENU) {
					IMenuCreator mc = action.getMenuCreator();
					ToolItem ti = (ToolItem) item;
					// we create the menu as a sub-menu of "dummy" so that we can use
					// it in a cascading menu too.
					// If created on a SWT control we would get an SWT error...
					//Menu dummy= new Menu(ti.getParent());
					//Menu m= mc.getMenu(dummy);
					//dummy.dispose();
					if (mc != null) {
						Menu m= mc.getMenu(ti.getParent());
						if (m != null) {
							// position the menu below the drop down item
							Rectangle b = ti.getBounds();
							Point p = ti.getParent().toDisplay(new Point(b.x, b.y+b.height));
							m.setLocation(p.x, p.y);	// waiting for SWT 0.42
							m.setVisible(true);
							return;	// we don't fire the action
						}
					}
				}
			}
		}

		// Ensure action is enabled first.
		// See 1GAN3M6: ITPUI:WINNT - Any IAction in the workbench can be executed while disabled.
		if (action.isEnabled()) {
			boolean trace = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface/trace/actions")); //$NON-NLS-1$ //$NON-NLS-2$
			long ms = System.currentTimeMillis();
			if(trace)
				System.out.println("Running action: " + action.getText()); //$NON-NLS-1$

			action.runWithEvent(e);
			
			if(trace)
				System.out.println((System.currentTimeMillis() - ms) + " ms to run action: "  + action.getText()); //$NON-NLS-1$
		}
	}
}
/* (non-Javadoc)
 * Method declared on Object.
 */
public int hashCode() {
	return action.hashCode();
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isEnabled() {
	return action != null && action.isEnabled();
}
/**
 * The action item implementation of this <code>IContributionItem</code>
 * method returns <code>true</code> for menu items and <code>false</code>
 * for everything else.
 */
public boolean isDynamic() {
	if(widget instanceof MenuItem) {
		//Optimization. Only recreate the item is the check or radio style has changed. 
		boolean itemIsCheck = (widget.getStyle() & SWT.CHECK) != 0;
		boolean actionIsCheck = getAction() != null && getAction().getStyle() == IAction.AS_CHECK_BOX;
		boolean itemIsRadio = (widget.getStyle() & SWT.RADIO) != 0;
		boolean actionIsRadio = getAction() != null && getAction().getStyle() == IAction.AS_RADIO_BUTTON;
		return (itemIsCheck != actionIsCheck) || (itemIsRadio != actionIsRadio);
	}
	return false;
}
/**
 * Returns <code>true</code> if this item is allowed to enable,
 * <code>false</code> otherwise.
 * 
 * @return if this item is allowed to be enabled
 * @since 2.0
 */
protected boolean isEnabledAllowed() {
	if (getParent() == null)
		return true;
	Boolean value = getParent().getOverrides().getEnabled(this);
	return (value == null) ? true : value.booleanValue();
}

/**
 * The action item implementation of this <code>IContributionItem</code>
 * method calls <code>update(null)</code>.
 */
public final void update() {
	update(null);	
}
/**
 * Synchronizes the UI with the given property.
 *
 * @param propertyName the name of the property, or <code>null</code> meaning all applicable
 *   properties 
 */
public void update(String propertyName) {
	if (widget != null) {		
		// determine what to do			
		boolean textChanged = propertyName == null || propertyName.equals(Action.TEXT);
		boolean imageChanged = propertyName == null || propertyName.equals(Action.IMAGE);
		boolean tooltipTextChanged = propertyName == null || propertyName.equals(Action.TOOL_TIP_TEXT);
		boolean enableStateChanged = propertyName == null || propertyName.equals(Action.ENABLED) || 
			propertyName.equals(IContributionManagerOverrides.P_ENABLED);
		boolean checkChanged = 
			(action.getStyle() == IAction.AS_CHECK_BOX || action.getStyle() == IAction.AS_RADIO_BUTTON)
			&& (propertyName == null || propertyName.equals(Action.CHECKED));
					
		if (widget instanceof ToolItem) {
			ToolItem ti = (ToolItem) widget;

			if (imageChanged)
				updateImages(true);
			
			if (tooltipTextChanged)
				ti.setToolTipText(action.getToolTipText());
				
			if (enableStateChanged) {
				boolean shouldBeEnabled = action.isEnabled() && isEnabledAllowed();
				
				if (ti.getEnabled() != shouldBeEnabled)
					ti.setEnabled(shouldBeEnabled);
			}
				
			if (checkChanged) {
				boolean bv = action.isChecked();
				
				if (ti.getSelection() != bv)
					ti.setSelection(bv);
			}
			return;
		}
		
		if (widget instanceof MenuItem) {
			MenuItem mi = (MenuItem) widget;

			if (textChanged) {
				Integer accelerator = null;
				String acceleratorText = null;
				IAction action = getAction();	
				String text = null;
				
				if (action != null) {				
					CommandResolver.ICallback callback = CommandResolver.getInstance().getCommandResolver();
		
					if (callback != null) {
						String commandId = action.getActionDefinitionId();
				
						if (commandId != null) {
							accelerator = callback.getAccelerator(commandId);
							acceleratorText = callback.getAcceleratorText(commandId);
						} 
					} 
				} 
											
				IContributionManagerOverrides overrides = null;
					
				if (getParent() != null)
					overrides = getParent().getOverrides();
					
				if (overrides != null)
					text = getParent().getOverrides().getText(this);
				
				if (accelerator == null)
					accelerator = new Integer(action.getAccelerator());
								
				mi.setAccelerator(accelerator.intValue());

				if (text == null)
					text = action.getText();

				if (text == null)
					text = ""; //$NON-NLS-1$
				else
					text = Action.removeAcceleratorText(text);
					
				if (acceleratorText == null)
					mi.setText(text);
				else
					mi.setText(text + '\t' + acceleratorText);				
			}
			
			if (imageChanged)
				updateImages(false);
			
			if (enableStateChanged) {
				boolean shouldBeEnabled = action.isEnabled() && isEnabledAllowed();
				
				if (mi.getEnabled() != shouldBeEnabled) 
					mi.setEnabled(shouldBeEnabled);
			}
	
			if (checkChanged) {	
				boolean bv = action.isChecked();
				
				if (mi.getSelection() != bv)
					mi.setSelection(bv);
			}
			
			return;
		}

		if (widget instanceof Button) {
			Button button = (Button) widget;
			
			if (imageChanged && updateImages(false))
				textChanged = false; // don't update text if it has an image
			
			if (textChanged) {
				String text = action.getText();
				
				if (text != null)
					button.setText(text);
			}
			
			if (tooltipTextChanged)
				button.setToolTipText(action.getToolTipText());
				
			if (enableStateChanged) {
				boolean shouldBeEnabled = action.isEnabled() && isEnabledAllowed();
				
				if (button.getEnabled() != shouldBeEnabled)
					button.setEnabled(shouldBeEnabled);
			}
				
			if (checkChanged) {
				boolean bv = action.isChecked();
				
				if (button.getSelection() != bv)
					button.setSelection(bv);
			}
			return;
		}
	}
}
/**
 * Updates the images for this action.
 *
 * @param forceImage <code>true</code> if some form of image is compulsory,
 *  and <code>false</code> if it is acceptable for this item to have no image
 * @return <code>true</code> if there are images for this action, <code>false</code> if not
 */
private boolean updateImages(boolean forceImage) {

	ImageCache cache = getImageCache();

	if (widget instanceof ToolItem) {
		if (USE_COLOR_ICONS) {
			Image image = cache.getImage(action.getHoverImageDescriptor());
			if (image == null) {
				image = cache.getImage(action.getImageDescriptor());
			}
			Image disabledImage = cache.getImage(action.getDisabledImageDescriptor());

			// Make sure there is a valid image.
			if (image == null && forceImage) {
				image = cache.getMissingImage();
			}
				
			// performance: more efficient in SWT to set disabled and hot image before regular image
			if (disabledImage != null) {
				// Set the disabled image if we were able to create one.
				// Assumes that SWT.ToolItem will use platform's default
				// behavior to show item when it is disabled and a disabled
				// image has not been set. 
				((ToolItem) widget).setDisabledImage(disabledImage);
			}
			((ToolItem) widget).setImage(image);
			
			return image != null;
		}
		else {
			Image image = cache.getImage(action.getImageDescriptor());
			Image hoverImage = cache.getImage(action.getHoverImageDescriptor());
			Image disabledImage = cache.getImage(action.getDisabledImageDescriptor());
	
			// If there is no regular image, but there is a hover image,
			// convert the hover image to gray and use it as the regular image.
			if (image == null && hoverImage != null) { 
				image = cache.getGrayImage(action.getHoverImageDescriptor());
			}
			else {
				// If there is no hover image, use the regular image as the hover image,
				// and convert the regular image to gray
				if (hoverImage == null && image != null) {
					hoverImage = image;
					image = cache.getGrayImage(action.getImageDescriptor());
				}
			}
	
			// Make sure there is a valid image.
			if (hoverImage == null && image == null && forceImage) {
				image = cache.getMissingImage();
			}
				
			// performance: more efficient in SWT to set disabled and hot image before regular image
			if (disabledImage != null) {
				// Set the disabled image if we were able to create one.
				// Assumes that SWT.ToolItem will use platform's default
				// behavior to show item when it is disabled and a disabled
				// image has not been set. 
				((ToolItem) widget).setDisabledImage(disabledImage);
			}
			((ToolItem) widget).setHotImage(hoverImage);
			((ToolItem) widget).setImage(image);
			
			return image != null;
		}
	}
	else if (widget instanceof Item || widget instanceof Button) {
		// Use hover image if there is one, otherwise use regular image.
		Image image = cache.getImage(action.getHoverImageDescriptor());
		if (image == null) {
			image = cache.getImage(action.getImageDescriptor());
		}
		// Make sure there is a valid image.
		if (image == null && forceImage) {
			image = cache.getMissingImage();
		}
		if (widget instanceof Item) {
			((Item) widget).setImage(image);
		}
		else if (widget instanceof Button) {
			((Button) widget).setImage(image);
		}
		return image != null;
	}
	return false;
}
}
