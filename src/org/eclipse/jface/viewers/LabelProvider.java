package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.graphics.Image;

/**
 * A label provider implementation which, by default, uses an element's 
 * <code>toString</code> value for its text and <code>null</code> for its image.
 * <p>
 * This class may be used as is, or subclassed to provide richer labels. 
 * Subclasses may override any of the following methods:
 * <ul>
 *   <li><code>isLabelProperty</code></li> 
 *   <li><code>getImage</code></li> 
 *   <li><code>getText</code></li> 
 *   <li><code>dispose</code></li> 
 * </ul>
 * </p>
 */
public class LabelProvider implements ILabelProvider {
	private ListenerList listeners = new ListenerList(1);
/**
 * Creates a new label provider.
 */
public LabelProvider() {
}
/* (non-Javadoc)
 * Method declared on IBaseLabelProvider.
 */
public void addListener(ILabelProviderListener listener) {
	listeners.add(listener);
}
/**
 * The <code>LabelProvider</code> implementation of this 
 * <code>IBaseLabelProvider</code> method does nothing. Subclasses may extend.
 */
public void dispose() {
}
/**
 * Fires a label provider changed event to all registered listeners
 * Only listeners registered at the time this method is called are notified.
 *
 * @param event a label provider changed event
 *
 * @see ILabelProviderListener#labelProviderChanged
 */
protected void fireLabelProviderChanged(LabelProviderChangedEvent event) {
	Object[] listeners = this.listeners.getListeners();
	for (int i = 0; i < listeners.length; ++i) {
		((ILabelProviderListener) listeners[i]).labelProviderChanged(event);
	}
}
/**
 * The <code>LabelProvider</code> implementation of this 
 * <code>ILabelProvider</code> method returns <code>null</code>. Subclasses may 
 * override.
 */
public Image getImage(Object element) {
	return null;
}
/**
 * The <code>LabelProvider</code> implementation of this 
 * <code>ILabelProvider</code> method returns the element's <code>toString</code>
 * string. Subclasses may override.
 */
public String getText(Object element) {
	return element == null ? "" : element.toString();//$NON-NLS-1$
}
/**
 * The <code>LabelProvider</code> implementation of this 
 * <code>IBaseLabelProvider</code> method returns <code>true</code>. Subclasses may 
 * override.
 */
public boolean isLabelProperty(Object element, String property) {
	return true;
}
/* (non-Javadoc)
 * Method declared on IBaseLabelProvider.
 */
public void removeListener(ILabelProviderListener listener) {
	listeners.remove(listener);
}
}
