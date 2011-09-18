package org.mart.crs.utils;

import org.mart.crs.utils.helper.HelperArrays;

/**
 * @version 1.0 1/21/11 5:21 PM
 * @author: Hut
 */
public class ComplexArray {

    /**
     * Constant required to calculate power values in dBs: log 10
     */
    protected static final float LOG10 = (float) Math.log(10);

    /**
     * Constant required to calculate power values in dBs: 20 / log
     * 10
     */
    protected static final float DBLOG = 20 / LOG10;

    /**
     * Real component
     */
    protected float[] real;

    /**
     * Imaginary component
     */
    protected float[] imag;


    /**
     * Create a new <code>Complex</code> number <code>real</code> +
     * j(<code>imag</code>)
     */
    public ComplexArray(float[] real, float[] imag) {
        if (real.length != imag.length || real.length == 0) {
            throw new IllegalArgumentException("Real and Imaginary parts should be equal and greater than zero");
        }
        this.real = real;
        this.imag = imag;
    }

    public ComplexArray(float[] rawData) {
        checkLengthAndInit(rawData.length);
        for (int i = 0; i < rawData.length; i += 2) {
            real[i / 2] = rawData[i];
            imag[i / 2] = rawData[i + 1];
        }
    }


    public ComplexArray(double[] rawData) {
        checkLengthAndInit(rawData.length);
        for (int i = 0; i < rawData.length; i += 2) {
            real[i / 2] = (float) rawData[i];
            imag[i / 2] = (float) rawData[i + 1];
        }
    }

    protected void checkLengthAndInit(int length) {
        if (length % 2 > 0) {
            throw new IllegalArgumentException("ArrayLength sould have even number of bins");
        }
        this.real = new float[length / 2];
        this.imag = new float[length / 2];
    }

    /**
     * Get real component
     */
    public float[] getReal() {
        return real;
    }

    /**
     * Set real component
     */
    public void setReal(float[] real) {
        this.real = real;
    }

    /**
     * Get imaginary component
     */
    public float[] getImag() {
        return imag;
    }

    /**
     * Set imaginary component
     */
    public void setImag(float[] imag) {
        this.imag = imag;
    }

    /**
     * Add the given <code>Complex</code> number to this
     * <code>Complex</code> number
     */
    public ComplexArray add(ComplexArray complex) {
        return new ComplexArray(HelperArrays.add(real, complex.real), HelperArrays.add(imag, complex.imag));
    }

    /**
     * Subtract the given <code>Complex</code> number from this
     * <code>Complex</code> number
     */
    public ComplexArray subtract(ComplexArray complex) {
        return new ComplexArray(HelperArrays.subtract(real, complex.real), HelperArrays.subtract(imag, complex.imag));
    }

    /**
     * Multiply this <code>Complex</code> number by the given factor
     */
    public ComplexArray multiply(float factor) {
        return new ComplexArray(HelperArrays.emphasizeVector(real, factor), HelperArrays.emphasizeVector(imag, factor));
    }

    /**
     * Divide this <code>Complex</code> number by the given factor
     */
    public ComplexArray divide(float factor) {
        return new ComplexArray(HelperArrays.emphasizeVector(real, 1 / factor), HelperArrays.emphasizeVector(imag, 1 / factor));
    }

    public ComplexArray divide(ComplexArray divisor) {
        int length = real.length;
        float[] a = real;
        float[] b = imag;
        float[] c = divisor.getReal();
        float[] d = divisor.getImag();
        float[] real = new float[length];
        float[] imag = new float[length];

        for (int i = 0; i < length; i++) {
            real[i] = (a[i] * c[i] + b[i] * d[i]) / (c[i] * c[i] + d[i] * d[i]);
            imag[i] = (b[i] * c[i] - a[i] * d[i]) / (c[i] * c[i] + d[i] * d[i]);
        }
        return new ComplexArray(real, imag);
    }


    /**
     * Multiply this <code>Complex</code> number by the given
     * <code>Complex</code> number
     */
    public ComplexArray multiply(ComplexArray complex) {
        int length = real.length;
        float[] nuReal = new float[length];
        float[] nuImag = new float[length];
        for (int i = 0; i < length; i++) {
            nuReal[i] = real[i] * complex.real[i] - imag[i] * complex.imag[i];
            nuImag[i] = real[i] * complex.imag[i] + imag[i] * complex.real[i];
        }
        return new ComplexArray(nuReal, nuImag);
    }

    /**
     * Set this <code>Complex</code> number to be its complex
     * conjugate
     */
    public ComplexArray conjugate() {
        return new ComplexArray(real, HelperArrays.emphasizeVector(imag, -1));
    }


    /**
     * Return the magnitude of the <code>Complex</code> number
     */
    public float[] getMagnitude() {
        return magnitude(real, imag);
    }

    /**
     * Return the phase of the <code>Complex</code> number
     */
    public float[] getPhase() {
        return phase(real, imag);
    }

    /**
     * Return the power of this <code>Complex</code> number in dBs
     */
    public float[] getPower() {
        return power(real, imag);
    }


    /**
     * Return the magnitude of a <code>Complex</code> number
     * <code>real</code> + (<code>imag</code>)j
     */
    public static float[] magnitude(float[] real, float[] imag) {
        int length = real.length;
        float[] out = new float[length];
        for (int i = 0; i < length; i++) {
            out[i] = (float) Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }
        return out;
    }

    /**
     * Return the phase of a <code>Complex</code> number
     * <code>real</code> + (<code>imag</code>)j
     */
    public static float[] phase(float[] real, float[] imag) {
        int length = real.length;
        float[] out = new float[length];
        for (int i = 0; i < length; i++) {
            out[i] = (float) Math.atan2(imag[i], real[i]);
        }
        return out;
    }

    /**
     * Return the power of a <code>Complex</code> number
     * <code>real</code> + (<code>imag</code>)j
     */
    public static float[] power(float[] real, float[] imag) {
        int length = real.length;
        float[] out = new float[length];
        for (int i = 0; i < length; i++) {
            out[i] = DBLOG * (float) Math.log(2 * (float) Math.sqrt(real[i] * real[i] + imag[i] * imag[i]));
        }
        return out;
    }


}

