/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Event;

/**
 * <p>
 * This class adapts instances of <code>IAction</code> to
 * <code>IHandler</code>.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public final class ActionHandler extends AbstractHandler {

	/**
	 * The wrapped action. This value is never <code>null</code>.
	 */
	private final IAction action;

	/**
	 * The property change listener hooked on to the action. This is initialized
	 * when the first listener is attached to this handler, and is removed when
	 * the handler is disposed or the last listener is removed.
	 */
	private IPropertyChangeListener propertyChangeListener;

	/**
	 * Creates a new instance of this class given an instance of
	 * <code>IAction</code>.
	 * 
	 * @param action
	 *            the action. Must not be <code>null</code>.
	 */
	public ActionHandler(final IAction action) {
		if (action == null)
			throw new NullPointerException();

		this.action = action;
	}

	public void addHandlerListener(IHandlerListener handlerListener) {
		if (!hasListeners()) {
			attachListener();
		}

		super.addHandlerListener(handlerListener);
	}

	/**
	 * When a listener is attached to this handler, then this registers a
	 * listener with the underlying action.
	 * 
	 * @since 3.1
	 */
	private final void attachListener() {
		if (propertyChangeListener == null) {
			propertyChangeListener = new IPropertyChangeListener() {
				public final void propertyChange(
						final PropertyChangeEvent propertyChangeEvent) {
					final String property = propertyChangeEvent.getProperty();
					fireHandlerChanged(new HandlerEvent(ActionHandler.this,
							IAction.ENABLED.equals(property), IAction.HANDLED
									.equals(property)));
				}
			};
		}

		this.action.addPropertyChangeListener(propertyChangeListener);
	}

	/**
	 * When no more listeners are registered, then this is used to removed the
	 * property change listener from the underlying action.
	 */
	private final void detachListener() {
		this.action.removePropertyChangeListener(propertyChangeListener);
		propertyChangeListener = null;
	}

	/**
	 * Removes the property change listener from the action.
	 * 
	 * @see org.eclipse.core.commands.IHandler#dispose()
	 */
	public final void dispose() {
		if (hasListeners()) {
			action.removePropertyChangeListener(propertyChangeListener);
		}
	}

	/**
	 * @see IHandler#execute(ExecutionEvent)
	 */
	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		if ((action.getStyle() == IAction.AS_CHECK_BOX)
				|| (action.getStyle() == IAction.AS_RADIO_BUTTON))
			action.setChecked(!action.isChecked());
		final Object trigger = event.getTrigger();
		try {
			if (trigger instanceof Event) {
				action.runWithEvent((Event) trigger);
			} else {
				action.runWithEvent(new Event());
			}
		} catch (Exception e) {
			throw new ExecutionException(
					"While executing the action, an exception occurred", e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Returns the action associated with this handler
	 * 
	 * @return the action associated with this handler (not null)
	 * @since 3.1
	 */
	public final IAction getAction() {
		return action;
	}

	public final boolean isEnabled() {
		return action.isEnabled();
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		super.removeHandlerListener(handlerListener);

		if (!hasListeners()) {
			detachListener();
		}
	}
}