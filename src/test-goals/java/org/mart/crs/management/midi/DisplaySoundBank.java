package org.mart.crs.management.midi;/*
 *	DisplaySoundbank.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 2003 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;


/**	<titleabbrev>DisplaySoundbank</titleabbrev>
	<title>Displaying instruments in Soundbanks</title>

	<formalpara><title>Purpose</title>
	<para>All instruments in a soundbank are listed with their patch
	numbers and names. Optionally, a custom soundbank is
	loaded.</para> </formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis><command>java DisplaySoundbank</command>
	<arg choice="opt"><replaceable class="parameter">soundbank</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><replaceable class="parameter">soundbank</replaceable></term>
	<listitem><para>the filename of a custom soundbank to be loaded. If not given, the default soundbank is used.</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>Using a custom soundbank even if no default soundbank is
	available only works with JDK 1.5.0 and later.</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="DisplaySoundbank.java.html">DisplaySoundbank.java</ulink>
	</para>
	</formalpara>

*/
public class DisplaySoundBank
{
    private static final boolean DEBUG = true;


	public static void main(String[] args)
	    throws MidiUnavailableException, InvalidMidiDataException,
	    IOException
	{
		Soundbank soundbank = null;
		if (args.length == 1)
		{
			File file = new File(args[0]);
			soundbank = MidiSystem.getSoundbank(file);
		}
		else if (args.length > 1)
		{
			printUsageAndExit();
		}
		else
		{
			Synthesizer	synth = null;
			synth = MidiSystem.getSynthesizer();
			if (DEBUG) out("Synthesizer: " + synth);

			synth.open();
			soundbank = synth.getDefaultSoundbank();
			synth.close();
		}

		if (soundbank == null)
		{
			out("no soundbank");
			System.exit(1);
		}

		/* Now display the information about the soundbank.
		 */
		out("------------------------------------");
		out("Soundbank: " + soundbank);
		out("Name: " + soundbank.getName());
		out("Description: " + soundbank.getDescription());
		out("Vendor: " + soundbank.getVendor());
		out("Version: " + soundbank.getVersion());
		out("------------------------------------");

		out("Instruments (instr#:[bank#, patch#] name):");
		Instrument[] aInstruments = soundbank.getInstruments();
		for (int i = 0; i < aInstruments.length; i++)
		{
			out("" + i + ":[" + aInstruments[i].getPatch().getBank() + ", " +
				aInstruments[i].getPatch().getProgram() + "] " +
				aInstruments[i].getName());
		}
	}


    private static void printUsageAndExit()
    {
		out("DisplaySoundbank: usage:");
		out("java DisplaySoundbank [<soundbankfilename>]");
		System.exit(1);
    }


	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** DisplaySoundbank.java ***/

