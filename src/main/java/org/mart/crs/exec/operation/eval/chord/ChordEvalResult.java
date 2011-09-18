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

package org.mart.crs.exec.operation.eval.chord;

/**
 * User: hut
 * Date: Jul 29, 2008
 * Time: 12:06:14 AM
 * Structure, representing evaluation results
 */
public class ChordEvalResult implements Comparable {

    private String song;
    private double totalTime;
    private double totalChordsTime;
    private double totalKnownChordsTime;
    private double correctTime;
    private double fragmentation;
    private double logLiklihood;


    /**
     * Constructor
     *
     * @param song
     * @param totalTime
     * @param totalChordsTime
     * @param totalKnownChordsTime
     * @param correctTime
     */
    public ChordEvalResult(String song, double totalTime, double totalChordsTime, double totalKnownChordsTime, double correctTime, double fragmentation, double logLiklihood) {
        this.song = song;
        this.totalTime = totalTime;
        this.totalChordsTime = totalChordsTime;
        this.totalKnownChordsTime = totalKnownChordsTime;
        this.correctTime = correctTime;
        this.fragmentation = fragmentation;
        this.logLiklihood = logLiklihood;
    }


    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public double getTotalChordsTime() {
        return totalChordsTime;
    }

    public void setTotalChordsTime(double totalChordsTime) {
        this.totalChordsTime = totalChordsTime;
    }

    public double getTotalKnownChordsTime() {
        return totalKnownChordsTime;
    }

    public void setTotalKnownChordsTime(double totalKnownChordsTime) {
        this.totalKnownChordsTime = totalKnownChordsTime;
    }

    public double getCorrectTime() {
        return correctTime;
    }

    public void setCorrectTime(double correctTime) {
        this.correctTime = correctTime;
    }

    public double getFragmentation() {
        return fragmentation;
    }

    public void setFragmentation(double fragmentation) {
        this.fragmentation = fragmentation;
    }

    public double getLogLiklihood() {
        return logLiklihood;
    }

    public void setLogLiklihood(double logLiklihood) {
        this.logLiklihood = logLiklihood;
    }

    public double getChordrecognitionRate(){
        return correctTime / totalKnownChordsTime;
    }

    public int compareTo(Object o) {
        ChordEvalResult compared = (ChordEvalResult) o;
        return this.getSong().compareTo(compared.getSong());
    }

}
