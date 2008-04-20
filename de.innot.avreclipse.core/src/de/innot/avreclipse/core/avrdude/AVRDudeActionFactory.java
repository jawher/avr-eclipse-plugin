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
 * $Id$
 *     
 *******************************************************************************/
package de.innot.avreclipse.core.avrdude;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.PluginIDs;
import de.innot.avreclipse.core.avrdude.AVRDudeAction.Action;
import de.innot.avreclipse.core.avrdude.AVRDudeAction.FileType;
import de.innot.avreclipse.core.avrdude.AVRDudeAction.MemType;
import de.innot.avreclipse.core.toolinfo.fuses.Fuses;

/**
 * This class provides some static methods to get {@link AVRDudeAction} objects
 * for common scenarios.
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class AVRDudeActionFactory {

	/** The name of the fuses for 1, 2 or 3 fusebytes */
	private final static MemType[][] FUSENAMEMAP = { {}, { MemType.fuse },
	        { MemType.lfuse, MemType.hfuse }, { MemType.lfuse, MemType.hfuse, MemType.efuse } };

	/**
	 * Get a list of actions to read all readable elements of an MCU and write
	 * them to the given folder.
	 * <p>
	 * This method needs the mcu id to determine what memories are readable from
	 * the device.
	 * </p>
	 * <p>
	 * It will call avrdude to get a list of all memory types and safe them with
	 * the following filenames and formats <table>
	 * <tr>
	 * <th>Memory</th>
	 * <th>Filename</th>
	 * <th>Type</th>
	 * </tr>
	 * <tr>
	 * <td>flash</td>
	 * <td>flash.hex</td>
	 * <td>ihex</td>
	 * </tr>
	 * <tr>
	 * <td>eeprom</td>
	 * <td>eeprom.hex</td>
	 * <td>ihex</td>
	 * </tr>
	 * <tr>
	 * <td>fuses</td>
	 * <td>mcuid.fuses</td>
	 * <td>fuses format</td>
	 * </tr>
	 * <tr>
	 * <td>lock</td>
	 * <td>mcuid.lock</td>
	 * <td>lock format</td>
	 * </tr>
	 * <tr>
	 * <td>calibration</td>
	 * <td>calibration</td>
	 * <td>hex values</td>
	 * </tr>
	 * <tr>
	 * <td>signature</td>
	 * <td>signature</td>
	 * <td>hex values</td>
	 * </tr>
	 * </table>
	 * 
	 * 
	 * @param mcuid
	 *            The mcu id value.
	 * @param backupfolderpath
	 *            Path to an existing folder
	 * @return <code>List&lt;String&gt;</code> with all actions required to
	 *         backup the mcu
	 */
	public static List<AVRDudeAction> backupActions(String mcuid, String backupfolderpath) {

		List<AVRDudeAction> actions = new ArrayList<AVRDudeAction>();

		// TODO load the list of memories from avrdude

		// Get the number of fuses for the given mcu
		// int fusecount = Fuses.getMCUInfo(mcuid);
		int fusecount = 3;

		MemType[] fusenames = FUSENAMEMAP[fusecount];

		IPath destpath = new Path(backupfolderpath);

		String flashfile = destpath.append("flash.hex").toOSString();
		String eepromfile = destpath.append("eeprom.eep").toOSString();
		String signaturefile = destpath.append("signature").toOSString();

		actions.add(new AVRDudeAction(MemType.signature, Action.read, signaturefile, FileType.hex));

		actions.add(new AVRDudeAction(MemType.flash, Action.read, flashfile, FileType.iHex));

		actions.add(new AVRDudeAction(MemType.eeprom, Action.read, eepromfile, FileType.iHex));

		for (MemType type : fusenames) {
			// TODO change this to fuses format once fuse files are implemented.
			actions.add(new AVRDudeAction(type, Action.read, destpath.append(type.toString())
			        .toOSString(), FileType.hex));
		}

		// TODO lock and calibration are still missing

		return actions;
	}

	/**
	 * Create an {@link AVRDudeAction} to write the flash image file defined in
	 * the given build configuration to the MCU.
	 * <p>
	 * If plugin.xml has not been modified, the filename will be
	 * <code>${BuildArtifactBaseFileName}.hex</code>. The variable is not
	 * resolved. It is up to the caller to resolve any variables in the
	 * generated arguments of the returned avrdude action.
	 * </p>
	 * <p>
	 * The generated action uses {@link AVRDudeAction.FileType#auto} to let
	 * avrdude determine the file type.
	 * </p>
	 * 
	 * @param buildcfg
	 *            <code>IConfiguration</code> from which to extract the flash
	 *            image file name.
	 * @return <code>AVRDudeAction</code> to write the flash.
	 */
	public static AVRDudeAction writeFlashAction(IConfiguration buildcfg) {

		AVRDudeAction action = null;

		ITool[] tools = buildcfg.getToolsBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_TOOL_FLASH);
		// Test if there is a Generate Flash Image tool in the toolchain of the
		// configuration.
		if (tools.length != 0) {
			// Tool does exist, extract the filename from the output option
			// We cannot get the name directly from the output element, because
			// the reference from the output element to the name-declaring
			// output option is not resolved when we call
			// outputelement.getValue().
			ITool objcopy = tools[0];
			IOption outputoption = objcopy
			        .getOptionBySuperClassId("de.innot.avreclipse.objcopy.flash.option.output");
			String filename = (String) outputoption.getValue();

			action = writeFlashAction(filename);
		}
		return action;
	}

	/**
	 * Create an {@link AVRDudeAction} to write the given flash image file to
	 * the MCU.
	 * <p>
	 * The generated action uses {@link AVRDudeAction.FileType#auto} to let
	 * avrdude determine the file type.
	 * </p>
	 * <p>
	 * Any macros in the filename are not resolved. It is up to the caller to
	 * resolve any macros as required.
	 * </p>
	 * 
	 * @param filename
	 *            <code>String</code> with the flash image file name.
	 * @return <code>AVRDudeAction</code>
	 */
	public static AVRDudeAction writeFlashAction(String filename) {

		return new AVRDudeAction(MemType.flash, Action.write, filename, FileType.auto);
	}

	/**
	 * Create an {@link AVRDudeAction} to write the eeprom image file defined in
	 * the given build configuration to the MCU.
	 * <p>
	 * If plugin.xml has not been modified, the filename will be
	 * <code>${BuildArtifactBaseFileName}.eep</code>. The variable is not
	 * resolved. It is up to the caller to resolve any variables in the
	 * generated arguments of the returned avrdude action.
	 * </p>
	 * <p>
	 * The generated action uses {@link AVRDudeAction.FileType#auto} to let
	 * avrdude determine the file type.
	 * </p>
	 * 
	 * @param buildcfg
	 *            <code>IConfiguration</code> from which to extract the eeprom
	 *            image file name.
	 * @return <code>AVRDudeAction</code> to write the eeprom.
	 */
	public static AVRDudeAction writeEEPROMAction(IConfiguration buildcfg) {

		AVRDudeAction action = null;

		ITool[] tools = buildcfg.getToolsBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_TOOL_EEPROM);
		// Test if there is a Generate EEPROM Image tool in the toolchain of the
		// configuration.
		if (tools.length != 0) {
			// Tool does exist, extract the filename from the output option
			// We cannot get the name directly from the output element, because
			// the reference from the output element to the name-declaring
			// output option is not resolved when we call
			// outputelement.getValue().
			ITool objcopy = tools[0];
			IOption outputoption = objcopy
			        .getOptionBySuperClassId("de.innot.avreclipse.objcopy.eeprom.option.output");
			String filename = (String) outputoption.getValue();

			action = writeEEPROMAction(filename);
		}
		return action;
	}

	/**
	 * Create an {@link AVRDudeAction} to write the given eeprom image file to
	 * the MCU.
	 * <p>
	 * The generated action uses {@link AVRDudeAction.FileType#auto} to let
	 * avrdude determine the file type.
	 * </p>
	 * <p>
	 * Any macros in the filename are not resolved. It is up to the caller to
	 * resolve any macros as required.
	 * </p>
	 * 
	 * @param filename
	 *            <code>String</code> with the flash image file name.
	 * @return <code>AVRDudeAction</code>
	 */
	public static AVRDudeAction writeEEPROMAction(String filename) {
		return new AVRDudeAction(MemType.eeprom, Action.write, filename, FileType.auto);
	}

	/**
	 * Create a List of {@link AVRDudeAction} objects to write the all given
	 * fuse byte values.
	 * <p>
	 * If a fuse byte value is <code>-1</code>, no action is created for it.
	 * </p>
	 * 
	 * @param mcuid
	 *            <code>String</code> with a valid MCU id value.
	 * @param fusevalues
	 *            Array of <code>int</code> with the byte values (0-255). All
	 *            other values are ignored (no action created).
	 * @return <code>List&lt;AVRDudeAction&gt;</code> with all actions. List
	 *         may be empty if the MCU has no fuses or no valid fuse byte value
	 *         was given.
	 */
	public static List<AVRDudeAction> writeFuseBytes(String mcuid, int fusevalues[]) {

		List<AVRDudeAction> fuseactions = new ArrayList<AVRDudeAction>();

		// Test if this is a 1 Fuse or 2-3 Fuse MCU
		int fusecount;
		try {
			fusecount = Fuses.getDefault().getFuseByteCount(mcuid);
		} catch (IOException e) {
			// Can't access the FuseDescription Objects?
			// Log the Exception and return an empty list.
			IStatus status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID,
			        "Can't access the FuseDescription file for " + mcuid, e);
			AVRPlugin.getDefault().log(status);
			return fuseactions;
		}

		// Do some checks.
		if (fusecount <= 0) {
			// given MCU has no fuses.
			// return an empty List
			return fuseactions;
		}

		// Get the name mapping
		MemType[] fusenames = FUSENAMEMAP[fusecount];

		// iterate over all fusenames until we run out of names or out of values
		for (int i = 0; i < fusenames.length && i < fusevalues.length; i++) {
			int value = fusevalues[i];
			if (0 <= value && value <= 255) {
				fuseactions.add(new AVRDudeAction(fusenames[i], Action.write, fusevalues[i]));
			}
		}

		return fuseactions;
	}

}