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

import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.util.List;  // disambiguate from SWT List

/**
 * A menu manager is a contribution manager which realizes itself and its items
 * in a menu control; either as a menu bar, a sub-menu, or a context menu.
 * <p>
 * This class may be instantiated; it may also be subclassed.
 * </p>
 */
public class MenuManager extends ContributionManager implements IMenuManager {

	/**
	 * The menu control; <code>null</code> before
	 * creation and after disposal.
	 */
	private Menu menu = null;
	
	/**
	 * List of registered menu listeners (element type: <code>IMenuListener</code>).
	 */
	private ListenerList listeners = new ListenerList(1);

	/**
	 * The menu id.
	 */
	private String id;

	/**
	 * The menu item widget; <code>null</code> before
	 * creation and after disposal. This field is used
	 * when this menu manager is a sub-menu.
	 */
	private MenuItem menuItem;

	/**
	 * The text for a sub-menu.
	 */
	private String menuText;

	/**
	 * Indicates whether <code>removeAll</code> should be
	 * called just before the menu is displayed.
	 */
	private boolean removeAllWhenShown = false;
	
	/**
	 * The parent contribution manager.
	 */
	private IContributionManager parent;

	/**
	 * Indicates this item is visible in its manager; <code>true</code> 
	 * by default.
	 */
	private boolean visible = true;

	/**
	 * The overrides for items of this manager
	 */
	private IContributionManagerOverrides overrides;

	
/**
 * Creates a menu manager.  The text and id are <code>null</code>.
 * Typically used for creating a context menu, where it doesn't need to be referred to by id.
 */
public MenuManager() {
	this(null, null);
}
/**
 * Creates a menu manager with the given text. The id of the menu
 * is <code>null</code>.
 * Typically used for creating a sub-menu, where it doesn't need to be referred to by id.
 *
 * @param text the text for the menu, or <code>null</code> if none
 */
public MenuManager(String text) {
	this(text, null);
}
/**
 * Creates a menu manager with the given text and id.
 * Typically used for creating a sub-menu, where it needs to be referred to by id.
 *
 * @param text the text for the menu, or <code>null</code> if none
 * @param id the menu id, or <code>null</code> if it is to have no id
 */
public MenuManager(String text, String id) {
	this.menuText = text;
	this.id = id;
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void addMenuListener(IMenuListener listener) {
	listeners.add(listener);
}
/**
 * Creates and returns an SWT context menu control for this menu,
 * and installs all registered contributions.
 * Does not create a new control if one already exists.
 * <p>
 * Note that the menu is not expected to be dynamic.
 * </p>
 *
 * @param parent the parent control
 * @return the menu control
 */
public Menu createContextMenu(Control parent) {
	if (!menuExist()) {
		menu = new Menu(parent);
		initializeMenu();
	}
	return menu;
}
/**
 * Creates and returns an SWT menu bar control for this menu,
 * for use in the given <code>Decorations</code>, and installs all registered
 * contributions. Does not create a new control if one already exists.
 *
 * @param parent the parent decorations
 * @return the menu control
 * @since 2.1
 */
public Menu createMenuBar(Decorations parent) {
	if (!menuExist()) {
		menu = new Menu(parent, SWT.BAR);
		update(false);
	}
	return menu;
}
/**
 * Creates and returns an SWT menu bar control for this menu, for use in the
 * given <code>Shell</code>, and installs all registered contributions. Does not
 * create a new control if one already exists. This implementation simply calls
 * the <code>createMenuBar(Decorations)</code> method
 *
 * @param parent the parent decorations
 * @return the menu control
 * @deprecated use <code>createMenuBar(Decorations)</code> instead.
 */
public Menu createMenuBar(Shell parent) {
	return createMenuBar((Decorations)parent);
}
/**
 * Disposes of this menu manager and frees all allocated SWT resources.
 * Notifies all contribution items of the dispose. Note that this method does
 * not clean up references between this menu manager and its associated
 * contribution items. Use <code>removeAll</code> for that purpose.
 */
public void dispose() {
	if (menuExist())
		menu.dispose();
	menu = null;
	
	if (menuItem != null) {
		menuItem.dispose();
		menuItem = null;
	}
	
	IContributionItem[] items = getItems();
	for (int i = 0; i < items.length; i++) {
		items[i].dispose();
	}
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void fill(Composite parent) {}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void fill(Menu parent, int index) {
	if (menuItem == null || menuItem.isDisposed()) {
		if (index >= 0)
			menuItem = new MenuItem(parent, SWT.CASCADE, index);
		else
			menuItem = new MenuItem(parent, SWT.CASCADE);

		menuItem.setText(menuText);

		if (!menuExist())
			menu = new Menu(parent);

		menuItem.setMenu(menu);

		initializeMenu();

		// populate the submenu, in order to enable accelerators
		// and to set enabled state on the menuItem properly
		update(true);
	}
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void fill(ToolBar parent, int index) {}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public IMenuManager findMenuUsingPath(String path) {
	IContributionItem item = findUsingPath(path);
	if (item instanceof IMenuManager)
		return (IMenuManager) item;
	return null;
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public IContributionItem findUsingPath(String path) {
	String id = path;
	String rest = null;
	int separator = path.indexOf('/');
	if (separator != -1) {
		id = path.substring(0, separator);
		rest = path.substring(separator + 1);
	} else {
		return super.find(path);
	}

	IContributionItem item = super.find(id);
	if (item instanceof IMenuManager) {
		IMenuManager manager = (IMenuManager) item;
		return manager.findUsingPath(rest);
	}
	return null;
}
/**
 * Notifies any menu listeners that a menu is about to show.
 * Only listeners registered at the time this method is called are notified.
 *
 * @param manager the menu manager
 *
 * @see IMenuListener#menuAboutToShow
 */
private void fireAboutToShow(IMenuManager manager) {
	Object[] listeners = this.listeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((IMenuListener) listeners[i]).menuAboutToShow(manager);
	}
}
/**
 * Returns the menu id.
 * The menu id is used when creating a contribution item 
 * for adding this menu as a sub menu of another.
 *
 * @return the menu id
 */
public String getId() {
	return id;
}
/**
 * Returns the SWT menu control for this menu manager.
 *
 * @return the menu control
 */
public Menu getMenu() {
	return menu;
}
/**
 * Returns the text shown in the menu.
 *
 * @return the menu text
 */
public String getMenuText() {
	return menuText;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionManagerOverrides getOverrides() {
	if (overrides == null) {
		if (parent == null) {
			overrides = new IContributionManagerOverrides() {
				public Boolean getEnabled(IContributionItem item) {
					return null;
				}
				public Integer getAccelerator(IContributionItem item) {
					return null;
				}
				public String getAcceleratorText(IContributionItem item) {
					return null;
				}
				public String getText(IContributionItem item) {
					return null;
				}
			};
		} else {
			overrides = parent.getOverrides();
		}
		super.setOverrides(overrides);
	}
	return overrides;
}

/**
 * Returns the parent contribution manager of this manger.
 * 
 * @return the parent contribution manager
 * @since 2.0
 */
public IContributionManager getParent() {
	return parent;
}

/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public boolean getRemoveAllWhenShown() {
	return removeAllWhenShown;
}
/**
 * Notifies all listeners that this menu is about to appear.
 */
private void handleAboutToShow() {
	if (removeAllWhenShown)
		removeAll();
	fireAboutToShow(this);
	update(false,true);
}
/**
 * Initializes the menu control.
 */
private void initializeMenu() {
	menu.addMenuListener(new MenuAdapter() {
		public void menuShown(MenuEvent e) {
			handleAboutToShow();
		}	
		public void menuHidden(MenuEvent e) {
//			ApplicationWindow.resetDescription(e.widget);
		}	
	});
	markDirty();
	// Don't do an update(true) here, in case menu is never opened.
	// Always do it lazily in handleAboutToShow().
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isDynamic() {
	return false;
}
/**
 * Returns whether this menu should be enabled or not.
 * Used to enable the menu item containing this menu when it is realized as a sub-menu.
 * <p>
 * The default implementation of this framework method
 * returns <code>true</code>. Subclasses may reimplement.
 * </p>
 *
 * @return <code>true</code> if enabled, and
 *   <code>false</code> if disabled
 */
public boolean isEnabled() {
	return true;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isGroupMarker() {
	return false;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isSeparator() {
	return false;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isSubstituteFor(IContributionItem item) {
	return this.equals(item);
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public boolean isVisible() {
	return visible;
}
/**
 * Returns whether the menu control is created
 * and not disposed.
 * 
 * @return <code>true</code> if the control is created
 *	and not disposed, <code>false</code> otherwise
 */
private boolean menuExist() {
	return menu != null && !menu.isDisposed();
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void removeMenuListener(IMenuListener listener) {
	listeners.remove(listener);
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void setRemoveAllWhenShown(boolean removeAll) {
	this.removeAllWhenShown = removeAll;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void setVisible(boolean visible) {
	this.visible = visible;
}
/**
 * Sets the overrides for this contribution manager
 * 
 * @param newOverrides the overrides for the items of this manager
 * @since 2.0
 */
public void setOverrides(IContributionManagerOverrides newOverrides) {
	overrides = newOverrides;
	super.setOverrides(overrides);
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void setParent(IContributionManager manager) {
	parent = manager;
}
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void update() {
	updateMenuItem();
}
/**
 * The <code>MenuManager</code> implementation of this <code>IContributionManager</code>
 * updates this menu, but not any of its submenus.
 *
 * @see #updateAll
 */
public void update(boolean force) {
	update(force, false);
}
public void update(String property) {
	IContributionItem items[] = getItems();
	for (int i = 0; i < items.length; i++) {
		items[i].update(property);		
	}
	if(menu != null && (IAction.TEXT.equals(property))) {
		String text = getOverrides().getText(this);
		if(text == null)
			text = menuText;
		if(menu == null || menu.isDisposed())
			return;
		if((text != null) && (menu.getParentItem() != null))
			menu.getParentItem().setText(text);
	}
}
/**
 * Incrementally builds the menu from the contribution items.
 * This method leaves out double separators and separators in the first 
 * or last position.
 *
 * @param force <code>true</code> means update even if not dirty,
 *   and <code>false</code> for normal incremental updating
 * @param recursive <code>true</code> means recursively update 
 *   all submenus, and <code>false</code> means just this menu
 */
protected void update(boolean force, boolean recursive) {
	if (isDirty() || force) {
		if (menuExist()) {
			// clean contains all active items without double separators
			IContributionItem[] items= getItems();
			List clean= new ArrayList(items.length);
			IContributionItem separator= null;
			for (int i = 0; i < items.length; ++i) {
				IContributionItem ci= items[i];
				if (!ci.isVisible())
					continue;
				if (ci.isSeparator()) {
					// delay creation until necessary 
					// (handles both adjacent separators, and separator at end)
					separator= ci;
				} else {
					if (separator != null) {
						if (clean.size() > 0)	// no separator if first item
							clean.add(separator);
						separator= null;
					}
					clean.add(ci);
				}
			}
			
			// remove obsolete (removed or non active)
			MenuItem[] mi= menu.getItems();
			for (int i= 0; i < mi.length; i++) {
				Object data= mi[i].getData();
				if (data == null || !clean.contains(data)) {
					mi[i].dispose();
				} else if(data instanceof IContributionItem && 
					((IContributionItem)data).isDynamic() && 
					((IContributionItem)data).isDirty()) {
						mi[i].dispose();
				}
			}

			// add new
			mi= menu.getItems();
			int srcIx= 0;
			int destIx= 0;
			for (Iterator e= clean.iterator(); e.hasNext();) { 
				IContributionItem src= (IContributionItem) e.next();
				IContributionItem dest;
					
				// get corresponding item in SWT widget
				if (srcIx < mi.length)
					dest= (IContributionItem) mi[srcIx].getData();
				else
					dest= null;
					
				if (dest != null && src.equals(dest)) {
					srcIx++;
					destIx++;
				} else if (dest != null && dest.isSeparator() && src.isSeparator()) {
					mi[srcIx].setData(src);
					srcIx++;
					destIx++;
				} else {																				
					int start= menu.getItemCount();
					src.fill(menu, destIx);
					int newItems= menu.getItemCount()-start;
					for (int i= 0; i < newItems; i++) {
						MenuItem item = menu.getItem(destIx++);
						item.setData(src);
					}
				}	
				
				// May be we can optimize this call. If the menu has just
				// been created via the call src.fill(fMenuBar, destIx) then
				// the menu has already been updated with update(true) 
				// (see MenuManager). So if force is true we do it again. But
				// we can't set force to false since then information for the
				// sub sub menus is lost.
				if (recursive) {
					IContributionItem item = src;
					if (item instanceof SubContributionItem) 
						item = ((SubContributionItem)item).getInnerItem();
					if (item instanceof IMenuManager) 
						((IMenuManager)item).updateAll(force);
				}

			}

			// remove any old menu items not accounted for
			for (; srcIx < mi.length; srcIx++) {
				mi[srcIx].dispose();
			}
			
			setDirty(false);
		}
	} else {
		// I am not dirty. Check if I must recursivly walk down the hierarchy.
		if (recursive) {
			IContributionItem[] items= getItems();
			for (int i = 0; i < items.length; ++i) {
				IContributionItem ci= items[i];
				if (ci instanceof IMenuManager) {
					IMenuManager mm = (IMenuManager) ci;
					if (mm.isVisible()) {
						mm.updateAll(force);
					}
				}
			}
		}
	}
	updateMenuItem();
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void updateAll(boolean force) {
	update(force, true);
}
/**
 * Updates the menu item for this sub menu.
 * The menu item is disabled if this sub menu is empty.
 * Does nothing if this menu is not a submenu.
 */
private void updateMenuItem() {
/*
 * Commented out until proper solution to enablement of
 * menu item for a sub-menu is found. See bug 30833 for
 * more details.
 *  
	if (menuItem != null && !menuItem.isDisposed() && menuExist()) {
		IContributionItem items[] = getItems();
		boolean enabled = false;
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			enabled = item.isEnabled();
			if(enabled) break;
		}
		// Workaround for 1GDDCN2: SWT:Linux - MenuItem.setEnabled() always causes a redraw
		if (menuItem.getEnabled() != enabled)
			menuItem.setEnabled(enabled);
	}
*/
}
}
