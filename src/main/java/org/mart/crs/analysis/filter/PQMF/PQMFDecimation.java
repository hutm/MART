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

package org.mart.crs.analysis.filter.PQMF;



/**
 * @version 1.0 Feb 2, 2010 12:53:13 PM
 * @author: Maksim Khadkevich
 */
public class PQMFDecimation extends PQMF{

    //TODO
    public PQMFDecimation(int channelNumber, float samplingRate) {
        super(channelNumber, samplingRate);
    }


    public static float[][] filter_(float[] samples) {
        //The output of the 32 filterBanks
        float[][] outSamples = new float[CHANNELS_NUMBER][samples.length / CHANNELS_NUMBER + 1];


        float[] buffer = new float[ORDER];

        int currentPosition = 0;
        for (int i = buffer.length - 1; i >= 0; i--) {
            buffer[i] = samples[currentPosition++];
        }

        float firstSample = sumProduct(h_PQMF, buffer);

        float[] window_C = calculateWindowFunctionForH_n(h_PQMF);
        float[][] matrix_M = calculateM(CHANNELS_NUMBER, 64);

        float[] channelsOutput;
        while (currentPosition + CHANNELS_NUMBER < samples.length) {
            //Make shift in buffer
            for (int i = ORDER - CHANNELS_NUMBER - 1; i >= 0; i--) {
                buffer[i + CHANNELS_NUMBER] = buffer[i];
            }
            //Now add new samples
            for (int i = CHANNELS_NUMBER - 1; i >= 0; i--) {
                buffer[i] = samples[currentPosition++];
            }

            float[] Z = product(buffer, window_C);

            //Partial sum of Z
            //TODO replace 64 with a constant
            float[] S = new float[64];
            for (int r = 0; r < 63; r++) {
                for (int j = 0; j < 8; j++) {
                    S[r] += Z[r + 64 * j];
                }
            }

            channelsOutput = matrixVectorMultiplication(matrix_M, S);

            //Write Output data
            for (int i = 0; i < CHANNELS_NUMBER; i++) {
                outSamples[i][(currentPosition - ORDER) / CHANNELS_NUMBER] = channelsOutput[i];
            }
        }

        return outSamples;

    }


    //Alternative

    public static float[][] filter(float[] samples) {
        //The output of the 32 filterBanks
        float[][] outSamples = new float[CHANNELS_NUMBER][samples.length / CHANNELS_NUMBER + 1];


        float[] buffer = new float[ORDER];

        int currentPosition = 0;
        for (int i = buffer.length - 1; i >= 0; i--) {
            buffer[i] = samples[currentPosition++];
        }

        float[] window_C = calculateWindowFunctionForH_n(h_PQMF);

        while (currentPosition + CHANNELS_NUMBER < samples.length) {
            //Make shift in buffer
            for (int i = ORDER - CHANNELS_NUMBER - 1; i >= 0; i--) {
                buffer[i + CHANNELS_NUMBER] = buffer[i];
            }
            //Now add new samples
            for (int i = CHANNELS_NUMBER - 1; i >= 0; i--) {
                buffer[i] = samples[currentPosition++];
            }

            float[] vector = new float[64];
            for (int r = 0; r < 64; r++) {
                for (int p = 0; p < 8; p++) {
                    vector[r] += window_C[r + 64 * p] * buffer[r + 64 * p];
                }
            }
            for (int k = 0; k < CHANNELS_NUMBER; k++) {
                for (int r = 0; r < 64; r++) {
                    outSamples[k][(currentPosition - ORDER) / CHANNELS_NUMBER] += (float) Math.cos((k + 0.5f) * (r - 16) * Math.PI / 32) * vector[r];
                }
            }
        }

        return outSamples;

    }


    public static float[] reconstruct(float[][] channelsData) {
        float[] outSamples = new float[channelsData[0].length * channelsData.length];

        float[] buffer = new float[1024];

        int currentPosition = 0;

        float[] window_C = calculateWindowFunctionForH_n(h_PQMF);
        float[][] matrix_N = calculateN(64, CHANNELS_NUMBER);


        while (currentPosition < channelsData[0].length) {
            //Generate sample data from all channels
            float[] inpuData = new float[32];
            for (int i = 0; i < channelsData.length; i++) {
                inpuData[i] = channelsData[i][currentPosition];
            }

            //Make shift in buffer
            for (int i = 991; i >= 0; i--) {
                buffer[i + 32] = buffer[i];
            }

            float[] newValues = matrixVectorMultiplication(matrix_N, inpuData);
            //Add new values
            for (int r = 0; r < 64; r++) {
                buffer[r] = newValues[r];
            }

            //Build U[i]
            float[] U = new float[512];
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 32; j++) {
                    U[64 * i + j] = buffer[128 * i + j];
                    U[64 * i + 32 + j] = buffer[128 * i + 96 + j];
                }
            }

            float[] W = new float[512];
            //apply windowing
            for (int i = 0; i < 512; i++) {
                W[i] = U[i] * window_C[i] * 32;
            }

            //Generate 32 output samples
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 16; j++) {
                    outSamples[currentPosition * 32 + i] += W[i + 32 * j];
                }
            }

            currentPosition++;

        }

        return outSamples;
    }




    private static float sumProduct(float[] a, float[] b) {
        float output = 0;
        for (int i = 0; i < a.length; i++) {
            output += a[i] * b[i];
        }
        return output;
    }

    private static float[] product(float[] a, float[] b) {
        float[] output = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            output[i] = a[i] * b[i];
        }
        return output;
    }

//    private static float[] product_(float[] a, float[] b){
//        float[] output = new float[a.length];
//        for(int i = 0; i < a.length; i++){
//            output[i] = a[i] * b[a.length - 1 - i];
//        }
//        return output;
//    }

    private static float[] calculateWindowFunctionForH_n(float[] h) {
        float[] out = new float[h.length];
        for (int i = 0; i < h.length; i++) {
            out[i] = (float) Math.pow(-1f, i / 64) * h[i];
        }
        return out;
    }

    private static float[][] calculateM(int numberOfChannels, int numberOfColumns) {
        float[][] output = new float[numberOfChannels][numberOfColumns];
        for (int i = 0; i < numberOfChannels; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                output[i][j] = (float) Math.cos((i + 0.5f) * (j - 16) * Math.PI / 32);
            }
        }

        return output;
    }

    private static float[][] calculateN(int numberOfChannels, int numberOfColumns) {
        float[][] output = new float[numberOfChannels][numberOfColumns];
        for (int i = 0; i < numberOfChannels; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                output[i][j] = (float) Math.cos((i + 0.5f) * (j + 16) * Math.PI / 32);
            }
        }

        return output;
    }


    private static float[] matrixVectorMultiplication(float[][] matrix, float[] vector) {
        if (matrix[0].length != vector.length) {
            throw new IllegalArgumentException("Incorrect matrix multiplication");
        }
        float[] out = new float[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                out[i] += matrix[i][j] * vector[j];
            }
        }
        return out;
    }
}
