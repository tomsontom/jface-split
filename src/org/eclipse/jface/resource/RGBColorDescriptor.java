/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;

/**
 * Describes a color by its RGB values.
 * 
 * @since 3.1
 */
class RGBColorDescriptor extends ColorDescriptor {

    private RGB color;
    private Color originalColor = null;
    private Device originalDevice = null;
    
    /**
     * Creates a new RGBColorDescriptor given some RGB values
     * 
     * @param color RGB values (not null)
     */
    public RGBColorDescriptor(RGB color) {
        this.color = color;
    }
    
    public RGBColorDescriptor(Color originalColor, Device originalDevice) {
        this(originalColor.getRGB());
        this.originalColor = originalColor;
        this.originalDevice = originalDevice;
    }
    
    public RGBColorDescriptor(Color originalColor) {
        this(originalColor.getRGB());
        this.originalColor = originalColor;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof RGBColorDescriptor) {
            RGBColorDescriptor other = (RGBColorDescriptor) obj;
            
            return other.color.equals(color) && other.originalColor == originalColor;
        }
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return color.hashCode();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.interfaces.ColorDescriptor#createColor()
     */
    public Color createColor(Device device) {
        // If this descriptor is wrapping an existing color, then we can return the original color
        // if this is the same device.
        if (originalColor != null) { 

            // If we don't know what device the original was allocated on, we can't tell if we
            // can reuse the original until we try to create a new one.
            if (originalDevice == null) {
                Color result = new Color(device, color);
                       
                // If this new color was equal to the original, then it must have been allocated
                // on the same device. 
                if (result.equals(originalColor)) {
                    // We now know the original device. We can reuse the original image,
                    // discard the temporary Image, and remember the device for the next time
                    // this descriptor is used.
                    result.dispose();
                    originalDevice = device;
                    return originalColor;
                }
                // The newly created color ended up being different from the original, so
                // it may have been allocated on a different device. Return the new version.
                return result;
            }
         
            // If we're allocating on the same device as the original color, return the original.
            if (originalDevice == device) {
                return originalColor;
            }            
        }
        
        return new Color(device, color);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.ColorDescriptor#destroyColor(org.eclipse.swt.graphics.Color)
     */
    public void destroyColor(Color toDestroy) {
        if (toDestroy == originalColor) {
            return;
        }
        
        toDestroy.dispose();
    }
}
