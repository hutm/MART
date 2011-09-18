package org.mart.crs.management.midi;/*
 *	SynthNote.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2003 by Matthias Pfisterer
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


import org.mart.crs.utils.AudioHelper;
import org.mart.crs.utils.helper.HelperArrays;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * <titleabbrev>SynthNote</titleabbrev>
 * <title>Playing a note on the synthesizer</title>
 * <p/>
 * <formalpara><title>Purpose</title>
 * <para>Plays a single note on the synthesizer.</para>
 * </formalpara>
 * <p/>
 * <formalpara><title>Usage</title>
 * <para>
 * <cmdsynopsis><command>java SynthNote</command>
 * <arg choice="plain"><replaceable class="parameter">keynumber</replaceable></arg>
 * <arg choice="plain"><replaceable class="parameter">velocity</replaceable></arg>
 * <arg choice="plain"><replaceable class="parameter">duration</replaceable></arg>
 * </cmdsynopsis>
 * </para></formalpara>
 * <p/>
 * <formalpara><title>Parameters</title>
 * <variablelist>
 * <varlistentry>
 * <term><replaceable class="parameter">keynumber</replaceable></term>
 * <listitem><para>the MIDI key number</para></listitem>
 * </varlistentry>
 * <varlistentry>
 * <term><replaceable class="parameter">velocity</replaceable></term>
 * <listitem><para>the velocity</para></listitem>
 * </varlistentry>
 * <varlistentry>
 * <term><replaceable class="parameter">duration</replaceable></term>
 * <listitem><para>the duration in milliseconds</para></listitem>
 * </varlistentry>
 * </variablelist>
 * </formalpara>
 * <p/>
 * <formalpara><title>Bugs, limitations</title>
 * <para>The precision of the duration depends on the precision
 * of <function>Thread.sleep()</function>, which in turn depends on
 * the precision of the system time and the latency of th
 * thread scheduling of the Java VM. For many VMs, this
 * means about 20 ms. When playing multiple notes, it is
 * recommended to use a <classname>Sequence</classname> and the
 * <classname>Sequencer</classname>, which is supposed to give better
 * timing.</para>
 * </formalpara>
 * <p/>
 * <formalpara><title>Source code</title>
 * <para>
 * <ulink url="SynthNote.java.html">SynthNote.java</ulink>
 * </para>
 * </formalpara>
 */
public class SynthNote {
    private static boolean DEBUG = true;


    public static void main(String[] args) throws LineUnavailableException {
        /** The MIDI channel to use for playing the note. */
        int nChannelNumber = 0;
        int nNoteNumber = 0;    // MIDI key number
        int nVelocity = 0;

        /*
           *	Time between note on and note off event in
           *	milliseconds. Note that on most systems, the
           *	best resolution you can expect are 10 ms.
           */
        int nDuration = 0;
        int nNoteNumberArgIndex = 0;
        switch (args.length) {
            case 4:
                nChannelNumber = Integer.parseInt(args[0]) - 1;
                nChannelNumber = Math.min(15, Math.max(0, nChannelNumber));
                nNoteNumberArgIndex = 1;
                // FALL THROUGH

            case 3:
                nNoteNumber = Integer.parseInt(args[nNoteNumberArgIndex]);
                nNoteNumber = Math.min(127, Math.max(0, nNoteNumber));
                nVelocity = Integer.parseInt(args[nNoteNumberArgIndex + 1]);
                nVelocity = Math.min(127, Math.max(0, nVelocity));
                nDuration = Integer.parseInt(args[nNoteNumberArgIndex + 2]);
                nDuration = Math.max(0, nDuration);
                break;

            default:
                printUsageAndExit();
        }

//        SoundRecorder soundRecorder = new SoundRecorder();

        /*
           *	We need a synthesizer to play the note on.
           *	Here, we simply request the default
           *	synthesizer.
           */
        Synthesizer synth = null;
        try {
            synth = MidiSystem.getSynthesizer();
        }
        catch (MidiUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (DEBUG) out("Synthesizer: " + synth);

        /*
           *	Of course, we have to open the synthesizer to
           *	produce any sound for us.
           */
        try {
            synth.open();
        }
        catch (MidiUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
        }


        /*
        *	Turn the note on on MIDI channel 1.
        *	(Index zero means MIDI channel 1)
        */
        MidiChannel[] channels = synth.getChannels();
        MidiChannel channel = channels[nChannelNumber];
        if (DEBUG) out("MidiChannel: " + channel);
        channel.programChange(0, 11);
        channel.noteOn(nNoteNumber, nVelocity);
        channel.noteOn(nNoteNumber + 4, nVelocity);
        channel.noteOn(nNoteNumber + 7, nVelocity);

//        soundRecorder.start();
        /*
        *	Wait for the specified amount of time
        *	(the duration of the note).
        */
        try {
            Thread.sleep(nDuration);
        }
        catch (InterruptedException e) {
        }

        /*
           *	Turn the note off.
           */
        channel.noteOff(nNoteNumber);
        channel.noteOff(nNoteNumber + 4, nVelocity);
        channel.noteOff(nNoteNumber + 7, nVelocity);

        try {
            Thread.sleep(nDuration);
        }
        catch (InterruptedException e) {
        }

        /* Close the synthesizer.
           */
        synth.close();
//        soundRecorder.stop();
//        storeData(soundRecorder.getSamplesAccumulator().getSamples(), soundRecorder.getAf(), "out.wav");


    }

    private static void storeData(byte[] streamData, AudioFormat audioFormat, String outFileName) {
        ByteArrayInputStream bais = new ByteArrayInputStream(streamData);
        AudioInputStream ais = new AudioInputStream(bais, audioFormat, streamData.length);
        try {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(outFileName + ".wav"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printUsageAndExit() {
        out("SynthNote: usage:");
        out("java SynthNote [<channel>] <note_number> <velocity> <duration>");
        System.exit(1);
    }


    private static void out(String strMessage) {
        System.out.println(strMessage);
    }
}


class SoundRecorder implements Runnable {

    private Thread thread;

    private TargetDataLine recLine;
    private AudioFormat af;
    private SamplesAccumulator samplesAccumulator;

    public SamplesAccumulator getSamplesAccumulator() {
        return samplesAccumulator;
    }

    public AudioFormat getAf() {
        return af;
    }

    public SoundRecorder() {
        // set up for simple low-rate mono recording
        float rate = 44100;
        int samplesSizeInBits = 16;
        int channels_ = 1;
        boolean signed = true;
        boolean bigEndian = false;
        af = new AudioFormat(rate,
                samplesSizeInBits,
                channels_,
                signed,
                bigEndian);


        DataLine.Info dli = new DataLine.Info(TargetDataLine.class, af);
        recLine = null;
        try {
            recLine = (TargetDataLine) AudioSystem.getLine(dli);
            recLine.open(af);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        this.samplesAccumulator = new SamplesAccumulator();


    }

    public void start() {
        thread = new Thread(this);
        thread.setName("recorderThread");
        thread.start();
    }

    public void run() {
        recLine.start();
        while (thread != null) {
            while (recLine != null && recLine.isOpen()) {
                try {
                    // then start reading in buffers
                    byte[] buffer = new byte[44100*4]; // say 1 sec worth of audio data
                    int count = recLine.read(buffer, 0, buffer.length);

                    if (count > 0) {
                        float[] samples = AudioHelper.getSamplesMono(buffer, count, false);
                        samplesAccumulator.addSamples(buffer);

                    }
                } catch (NullPointerException ne) {
//                    ne.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        shutDown(null);
    }

    private void shutDown(String message) {

        if (message != null) {
            System.err.println(message);
        }
        if (recLine != null) {
            recLine.stop();
            recLine = null;
        }
        if (thread != null) {
            thread = null;
        }

    }
}


class SamplesAccumulator {
    private List<byte[]> samplesList;

    SamplesAccumulator() {
        this.samplesList = new ArrayList<byte[]>();
    }

    synchronized void addSamples(byte[] samplesPortion) {
        samplesList.add(samplesPortion);
    }

    synchronized byte[] getAllSamples() {
        byte[] result = new byte[0];
        for (byte[] currentPortion : samplesList) {
            result = HelperArrays.concat(result, currentPortion);
        }
        return result;
    }
}


/*** SynthNote.java ***/

