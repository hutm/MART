package org.mart.crs.core.spectrum;

public class Complex implements Cloneable {

    protected float real;
    protected float imag;

    public Complex(float real, float imag) {
        this.real = real;
        this.imag = imag;
    }

    public float getReal() {
        return real;
    }

    public float getImag() {
        return imag;
    }

    public Complex divide(Complex divisor) {
        float a = real;
        float b = imag;
        float c = divisor.getReal();
        float d = divisor.getImag();
        float real = (a * c + b * d) / (c * c + d * d);
        float imag = (b * c - a * d) / (c * c + d * d);
        return new Complex(real, imag);
    }

    public Complex multiply(Complex complex) {
        float real_ = real * complex.real - imag * complex.imag;
        float imag_ = real * complex.imag + imag * complex.real;
        return new Complex(real_, imag_);
    }

    public Object clone() {
        return new Complex(real, imag);
    }
}

