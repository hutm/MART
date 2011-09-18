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

package org.mart.crs.analysis.filter.QMF;

import javax.swing.*;

/**
 * Quadrature Mirror Filter implementation
 *
 * @version 1.0 14.04.2009 10:00:30
 * @author: Maksim Khadkevich
 */
public class QMF {


    //Filter order
    public static final int M = 64;
    public static float[] h0 = QMFCoeff.h0_64;
    public static float[] h1 = QMFCoeff.h1_64;

    public static int delay = M - 1;


    public static float[][] filter(float[] samples, float[] h, boolean isToDecimate) {
        int i, j, k;
        float[] h_inv;
        float[] x;
        int x2;

        int N = samples.length;
        int bandLength;

        if (isToDecimate) {
            if ((samples.length / 2) == (samples.length / 2.0f)) {
                bandLength = (samples.length / 2);
            } else {
                bandLength = (samples.length / 2) + 1;
            }
        } else {
            bandLength = samples.length;
        }
        float[] bandLow = new float[bandLength];
        float[] bandHigh = new float[bandLength];


        h_inv = new float[M];
        x = new float[N + M - 1];
        x2 = M - 1;
        for (i = 0; i < M; i++)
            h_inv[M - i - 1] = h[i];
        for (i = 0; i < N; i++)
            x[i + M - 1 - delay] = samples[i];      //Removed  + M  - 1
        for (i = 0, k = 0; i < N; k++) {
            bandLow[k] = 0;
            bandHigh[k] = 0;
            for (j = 0; j < M / 2; j++) {
                bandLow[k] += h_inv[j] * (x[i + j] + x[x2 + i - j]);
                bandHigh[k] -= h_inv[j] * (x[i + j] - x[x2 + i - j]);
                j++;
                bandLow[k] += h_inv[j] * (x[i + j] + x[x2 + i - j]);
                bandHigh[k] += h_inv[j] * (x[i + j] - x[x2 + i - j]);
            }

            if (isToDecimate) {
                i += 2;
            } else {
                i++;
            }
        }

        //writing output values
        float[][] out = new float[2][];
        out[0] = bandLow;
        out[1] = bandHigh;

        return out;
    }

    /**
     * performs decimation
     *
     * @param samples samples
     * @return decimated samples
     */
    public static float[] decimate(float[] samples) {
        int length;
        if ((samples.length / 2) == (samples.length / 2.0f)) {
            length = (samples.length / 2);
        } else {
            length = (samples.length / 2) + 1;
        }

        float[] output = new float[length];
        for (int i = 0; i < samples.length; i += 2) {
            output[i / 2] = samples[i];
        }

        return output;
    }


    /**
     * Make reconstruction
     *
     * @param band band data
     * @param h    transfer function
     * @return reconstructed samples
     */
    public static float[] reconstruct(final float[] band,
                                      final float[] h) {
        int i, j;

        int N = band.length * 2;
        float[] out = new float[N];

        float[] xx = new float[2 * N];

        for (i = 0; i < N / 2; i++)
            xx[2 * i] = band[N / 2 - 1 - i];


        for (i = 0; i < N; i += 4) {
            float y0, y1, y2, y3, x0;
            y0 = y1 = y2 = y3 = 0.f;
            if (N - 4 - i < 0 || N + M - i >= xx.length) {
                continue;
            }
            x0 = xx[N - 4 - i];
            for (j = 0; j < M; j += 4) {
                float x1, a0, a1;
                a0 = h[j];
                a1 = h[j + 1];
                x1 = xx[N - 2 + j - i];
                y0 += a0 * x1;
                y1 += a1 * x1;
                y2 += a0 * x0;
                y3 += a1 * x0;
                a0 = h[j + 2];
                a1 = h[j + 3];
                x0 = xx[N + j - i];
                y0 += a0 * x0;
                y1 += a1 * x0;
                y2 += a0 * x1;
                y3 += a1 * x1;
            }
            out[i] = y0;
            out[i + 1] = y1;
            out[i + 2] = y2;
            out[i + 3] = y3;
        }

        return out;
    }


    public static void addChildren(QMFTree tree, QMFTreeNode node, float[] samples) {
        float[] band1 = new float[0];
        float[] band2 = new float[0];
        if (tree.hasLeftChild(node) || tree.hasRightChild(node)) {

            if (samples != null) {
                float[][] data = filter(samples, h0, false);
                band1 = data[0];
                band2 = data[1];
            }
            QMFTreeNode leftNode;
            if (tree.hasLeftChild(node)) {
                if (!tree.isFinalLeftNode(node.getId())) {
                    band1 = decimate(band1);
                }
                leftNode = new QMFTreeNode(node, QMFTree.LEFT_NODE, band1, tree.isFinalLeftNode(node.getId()));
                tree.addNode(leftNode);
                addChildren(tree, leftNode, leftNode.getData());
            }
            QMFTreeNode rightNode;
            if (tree.hasRightChild(node)) {
                if (!tree.isFinalRightNode(node.getId())) {
                    band2 = decimate(band2);
                }
                rightNode = new QMFTreeNode(node, QMFTree.RIGHT_NODE, band2, tree.isFinalRightNode(node.getId()));
                tree.addNode(rightNode);
                addChildren(tree, rightNode, rightNode.getData());
            }
        }
    }


    public static float[] reconstructSamples(QMFTree tree, QMFTreeNode node) {

        float[] y0 = new float[0];
        float[] y1 = new float[0];
        float[] samplesOut;

        if (tree.isFinalNode(node.getId())) {
            node.setData(decimate(node.getData()));
        }


        if (!(tree.hasLeftChild(node) || tree.hasRightChild(node))) {
            if (node.isLeft()) {
                return reconstruct(node.getData(), h0);
            } else {
                return reconstruct(node.getData(), h1);
            }
        }


        if (tree.hasLeftChild(node)) {
            y0 = reconstructSamples(tree, tree.getLeftChild(node));
        }
        if (tree.hasRightChild(node)) {
            y1 = reconstructSamples(tree, tree.getRightChild(node));
        }

        if (y0.length > 0) {
            samplesOut = new float[y0.length];
            if (y1.length > 0) {
                int length = Math.min(y0.length, y1.length);
                for (int i = 0; i < length; i++)
                    samplesOut[i] = 2 * (y0[i] - y1[i]);
            } else {
                for (int i = 0; i < y0.length; i++)
                    samplesOut[i] = 2 * (y0[i]);
            }
        } else {
            if (y1.length > 0) {
                samplesOut = new float[y1.length];
                for (int i = 0; i < y1.length; i++)
                    samplesOut[i] = 2 * (-y1[i]);
            } else {
                return null;
            }
        }

        if (node.isLeft()) {
            return reconstruct(samplesOut, h0);
        }
        if (node.isRight()) {
            return reconstruct(samplesOut, h1);
        } else {
            return samplesOut;
        }
    }


    //<-------------------Methods to launch GUI----------------------->

    public static void createAndShowGUI(final String title, final JPanel panel) {

        JFrame frame = new JFrame(title);
        frame.setContentPane(panel);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

}







