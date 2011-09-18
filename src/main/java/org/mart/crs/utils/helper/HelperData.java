/*
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.utils.helper;

import org.mart.crs.logging.CRSException;

import java.io.*;

/**
 * @version 1.0 Feb 7, 2010 1:02:15 AM
 * @author: Maksim Khadkevich
 */
public class HelperData {


    //-------------------------------------------Writers and reader of primitives-------------------------------------

    public static void writeInt(int v, BufferedOutputStream out) throws IOException {
        out.write((v >>> 0) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 24) & 0xFF);
    }

    public static void writeShort(short v, BufferedOutputStream out) throws IOException {
        out.write((v >>> 0) & 0xFF);
        out.write((v >>> 8) & 0xFF);
    }


    public static int readInt(BufferedInputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        try {
            if ((ch1 | ch2 | ch3 | ch4) < 0)
                throw new CRSException("error");
        } catch (CRSException e) {
            e.printStackTrace();
        }
        return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    public static short readShort(BufferedInputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        try {
            if ((ch1 | ch2) < 0)
                throw new CRSException("error");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (short) ((ch1 << 0) + (ch2 << 8));
    }

    public static float readFloat(BufferedInputStream in) throws IOException {
        return Float.intBitsToFloat(readInt(in));
    }

    public static void writeFloat(float v, BufferedOutputStream out) throws IOException {
        writeInt(Float.floatToIntBits(v), out);
    }


    public static void main(String args[]) {
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("testReadWrite"));
            writeFloat(0.003f, out);
            out.close();

            BufferedInputStream in = new BufferedInputStream(new FileInputStream("testReadWrite"));
            System.out.println(readFloat(in));
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
