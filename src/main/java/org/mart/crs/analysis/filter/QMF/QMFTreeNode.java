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


import org.mart.crs.config.ExecParams;

/**
 * @version 1.0 27.04.2009 15:58:33
 * @author: Maksim Khadkevich
 */
public class QMFTreeNode implements Comparable {

    private String id;
    private float[] data;

    private boolean isFinal;
    private float startFreq;
    private float endFreq;
    private float samplingRate;

    public QMFTreeNode(QMFTreeNode root, String id, float[] data, boolean isFinal) {
        this.isFinal = isFinal;
        this.id = root.getId() + id;
        this.data = data;
        if (this.isFinal) {
            this.samplingRate = root.getSamplingRate();
        } else {
            this.samplingRate = root.getSamplingRate() / 2;
        }
        float[] freqRange = getFrequencyRange(ExecParams._initialExecParameters.samplingRate);
        startFreq = freqRange[0];
        endFreq = freqRange[1];
    }

    private QMFTreeNode() {
        this.id = "";
        this.data = null;
        this.samplingRate = ExecParams._initialExecParameters.samplingRate;
        this.endFreq = ExecParams._initialExecParameters.samplingRate / 2;
    }

    public static QMFTreeNode getRootNode() {
        return new QMFTreeNode();
    }

    public String getId() {
        return id;
    }

    public float[] getData() {
        return data;
    }

    public boolean isLeft() {
        if (id.endsWith(QMFTree.LEFT_NODE)) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isRight() {
        if (id.endsWith(QMFTree.RIGHT_NODE)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRoot() {
        if (id.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    public float[] getFrequencyRange(float samplingFrequency) {
        float startFreq = 0.0f;
        float endFreq = samplingFrequency / 2;
        float temp, middlePoint;
        boolean isLeft, reverse = false;
        for (int i = 0; i < id.length(); i++) {
            isLeft = (id.substring(i, i + 1)).equals(QMFTree.LEFT_NODE);

            if (startFreq < endFreq) {
                middlePoint = startFreq + Math.abs(endFreq - startFreq) / 2.0f;
                if (isLeft) {
                    if (reverse) {
                        startFreq = middlePoint;
                    } else {
                        endFreq = middlePoint;
                    }
                } else {
                    if (reverse) {
                        endFreq = middlePoint;
                    } else {
                        startFreq = middlePoint;
                    }
                    reverse = !reverse;
                    temp = startFreq;
                    startFreq = endFreq;
                    endFreq = temp;
                }
            } else {
                middlePoint = endFreq + Math.abs(endFreq - startFreq) / 2.0f;
                if (isLeft) {
                    if (reverse) {
                        endFreq = middlePoint;
                    } else {
                        startFreq = middlePoint;
                    }
                } else {
                    if (reverse) {
                        startFreq = middlePoint;
                    } else {
                        endFreq = middlePoint;
                    }
                    reverse = !reverse;
                    temp = startFreq;
                    startFreq = endFreq;
                    endFreq = temp;
                }
            }


        }

        float[] out = new float[2];
        out[0] = startFreq;
        out[1] = endFreq;
        return out;

    }


    public int compareTo(Object o) {
        QMFTreeNode node1 = (QMFTreeNode) o;
        if (Math.min(node1.getStartFreq(), node1.getEndFreq()) > Math.min(this.getStartFreq(), this.getEndFreq())) {
            return -1;
        } else {
            return 1;
        }
    }

    public boolean equals(Object obj) {
        QMFTreeNode treeNode;
        if (obj instanceof QMFTreeNode) {
            treeNode = (QMFTreeNode) obj;
            return this.getId() == treeNode.getId();
        }
        return false;
    }


    public String toString() {
        if (startFreq < endFreq) {
            return startFreq + " >> " + endFreq;
        } else {
            return endFreq + " << " + startFreq;
        }
    }


    public void setData(float[] data) {
        this.data = data;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public float getStartFreq() {
        return startFreq;
    }

    public float getEndFreq() {
        return endFreq;
    }

    public float getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(float samplingRate) {
        this.samplingRate = samplingRate;
    }
}
