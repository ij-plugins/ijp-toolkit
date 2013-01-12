/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */
package net.sf.ij_plugins.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Text related utilities.
 *
 * @author Jarek Sacha
 * @since July 16, 2002
 */

public class TextUtil {

    /**
     * Constructor for the TextUtil object
     */
    private TextUtil() {
    }


    /**
     * Parse a string value as a floating point number.
     *
     * @param str          Input string.
     * @param defaultValue Value returned if <code>str</code> can not be parsed as a floating point
     *                     number.
     * @return Strung value interpreted as a floating point number.
     */
    public static float parseFloat(final String str, final float defaultValue) {

        float f = defaultValue;
        if (str != null) {
            try {
                f = Float.parseFloat(str);
            } catch (final NumberFormatException ex) {
                f = defaultValue;
            }
        }

        return f;
    }


    /**
     * Parse a string value as an integer.
     *
     * @param str          Input string.
     * @param defaultValue Value returned if <code>str</code> can not be parsed as a integer.
     * @return String value interpreted as an integer.
     */
    public static int parseInt(final String str, final int defaultValue) {
        int i = defaultValue;
        if (str != null) {
            try {
                i = Integer.parseInt(str);
            } catch (final NumberFormatException ex) {
                i = defaultValue;
            }
        }

        return i;
    }


    /**
     * Parse string as an array of integers separated by white space.
     *
     * @param str Input string.
     * @return int array containing parsed numbers
     * @throws IllegalArgumentException In case of parsing error.
     */
    public static int[] parseIntArray(final String str) throws IllegalArgumentException {

        final StringReader reader = new StringReader(str);
        final StreamTokenizer tokenizer = new StreamTokenizer(reader);
        tokenizer.parseNumbers();

        final List<Integer> tokens = new ArrayList<>();
        try {
            int id = tokenizer.nextToken();
            while (id != StreamTokenizer.TT_EOF) {
                if (id != StreamTokenizer.TT_NUMBER) {
                    throw new IllegalArgumentException("Cannot parse string as an array of integers...");
                }
                tokens.add((int) tokenizer.nval);
                id = tokenizer.nextToken();
            }

            if (tokens.size() < 1) {
                throw new IllegalArgumentException("Input string does not contain any numbers.");
            }
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unexpected error extracting tokens: " + ex);
        }

        final int[] a = new int[tokens.size()];
        for (int i = 0; i < a.length; ++i) {
            a[i] = tokens.get(i);
        }

        return a;
    }


    /**
     * Parse string as an array of floats separated by white space.
     *
     * @param str Input string.
     * @return float array containing parsed numbers.
     * @throws IllegalArgumentException In case of parsing error.
     */
    public static float[] parseFloatArray(final String str) throws IllegalArgumentException {

        final StringReader reader = new StringReader(str);
        final StreamTokenizer tokenizer = new StreamTokenizer(reader);
        tokenizer.parseNumbers();

        final List<Float> tokens = new ArrayList<Float>();
        try {
            int id = tokenizer.nextToken();
            while (id != StreamTokenizer.TT_EOF) {
                if (id != StreamTokenizer.TT_NUMBER) {
                    throw new IllegalArgumentException("Cannot parse string as an array of integers...");
                }
                tokens.add(new Float(tokenizer.nval));
                id = tokenizer.nextToken();
            }

            if (tokens.size() < 1) {
                throw new IllegalArgumentException("Input string does not contain any numbers.");
            }
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unexpected error extracting tokens: " + ex);
        }

        final float[] f = new float[tokens.size()];
        for (int i = 0; i < f.length; ++i) {
            f[i] = tokens.get(i);
        }

        return f;
    }


    /**
     * Convert exception stack trace to a string.
     *
     * @param t exception
     * @return string with stack trace.
     */
    public static String toString(final Throwable t) {
        final CharArrayWriter caw = new CharArrayWriter();
        final PrintWriter pw = new PrintWriter(caw);
        t.printStackTrace(pw);
        return caw.toString();
    }
}
