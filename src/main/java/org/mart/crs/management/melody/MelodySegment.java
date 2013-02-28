package org.mart.crs.management.melody;

/**
 * Time segment of a melody
 * @version 1.0 2/21/13 11:25 AM
 * @author: Hut
 */
public class MelodySegment {

    protected float startTime;
    protected float endTime;
    protected float pitch;
    protected boolean voiced;

    public MelodySegment(float startTime, float endTime, float pitch) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.pitch = Math.abs(pitch);
        this.voiced = pitch > 0;
    }


    public float getStartTime() {
        return startTime;
    }

    public float getEndTime() {
        return endTime;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isVoiced() {
        return voiced;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(float endTime) {
        this.endTime = endTime;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setVoiced(boolean voiced) {
        this.voiced = voiced;
    }
}
