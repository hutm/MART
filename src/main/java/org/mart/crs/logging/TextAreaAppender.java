/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.logging;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;

/**
 * Simple example of creating a Log4j appender that will
 * write to a JTextArea.
 */
public class TextAreaAppender extends WriterAppender {

    static private JTextArea jTextArea = null;

    /**
     * Set the target JTextArea for the logging information to appear.
     */
    static public void setTextArea(JTextArea jTextArea) {
        TextAreaAppender.jTextArea = jTextArea;
    }

    @Override
    /**
     * Format and then append the loggingEvent to the stored
     * JTextArea.
     */
    public void append(LoggingEvent loggingEvent) {
        final String message = this.layout.format(loggingEvent);

        if (jTextArea != null) {
            // Append formatted message to textarea using the Swing Thread.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    jTextArea.append(message);
                }
            });
        }
    }
}
