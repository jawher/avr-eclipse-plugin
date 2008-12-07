/*******************************************************************************
 * 
 * Copyright (c) 2008 Thomas Holland (thomas@innot.de) and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *     
 * $Id: StringColumnLabelProvider.java 562 2008-07-23 19:53:19Z innot $
 *     
 *******************************************************************************/
package de.innot.avreclipse.ui.views.supportedmcu;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import de.innot.avreclipse.core.IMCUProvider;

/**
 * A very simple ColumnLabelProvider that shows the MCU info from the
 * IMCUProvider for the given element (which is a String with a MCU id value).
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public class StringColumnLabelProvider extends ColumnLabelProvider {

	/** The IMCUProvider associated with this ColumnLabelProvider */
	private IMCUProvider fProvider = null;

	/**
	 * Creates a new ColumnLabelProvider for the given IMCUProvider.
	 * 
	 * @param provider <code>IMCUProvider<code> source
	 */
	public StringColumnLabelProvider(IMCUProvider provider) {
		fProvider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {

		// get the info for the given mcu id and return it
		String mcuid = (String) element;
		String info = fProvider.getMCUInfo(mcuid);
		return info != null ? info : "n/a";
	}
}