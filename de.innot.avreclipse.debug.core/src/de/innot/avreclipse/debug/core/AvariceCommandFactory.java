/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/

package de.innot.avreclipse.debug.core;

import org.eclipse.cdt.debug.mi.core.command.factories.StandardCommandFactory;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class AvariceCommandFactory extends StandardCommandFactory {

	/**
	 * 
	 */
	public AvariceCommandFactory() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param miVersion
	 */
	public AvariceCommandFactory(String miVersion) {
		super(miVersion);
		// TODO Auto-generated constructor stub
	}

}