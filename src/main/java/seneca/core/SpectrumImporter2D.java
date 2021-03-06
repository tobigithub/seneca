/*
 *  SpectrumImporter2D.java
 *
 *  Copyright (C) 1999-2004  Dr. Christoph Steinbeck
 *
 *  Contact: steinbeck@ice.mpg.de
 *
 *  This software is published and distributed under artistic license.
 *  The intent of this license is to state the conditions under which this Package
 *  may be copied, such that the Copyright Holder maintains some semblance
 *  of artistic control over the development of the package, while giving the
 *  users of the package the right to use and distribute the Package in a
 *  more-or-less customary fashion, plus the right to make reasonable modifications.
 *
 *  THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES,
 *  INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  The complete text of the license can be found in a file called LICENSE
 *  accompanying this package.
 * 
 */

package seneca.core;

import java.util.StringTokenizer;

import org.openscience.spectra.model.NMRSpectrum;
import org.openscience.spectra.model.NMRSignal;

/**
 * Importer class for 2D NMR data. Reads data from supported file and clipboard
 * formats 31.12.2000 Support for WinNMR clipboard format, Mestrec format
 * 10.09.2004 Support for Simple Lists (shift1;shift2;intensity)
 *
 * @author steinbeck
 * @created 10. September 2004
 */

public class SpectrumImporter2D {
    /**
     * Constructor for the SpectrumImporter2D object
     */
    public SpectrumImporter2D() {
    }

    /**
     * @param s           A string to parse the spectrum from
     * @param nmrSpectrum Description of the Parameter
     * @return Description of the Return Value
     */
    public static NMRSpectrum importWinNMR2D(String s, NMRSpectrum nmrSpectrum) {
        float procInt;
        NMRSignal cnmrsig;
        int phase = 0;
        float shifts[];
        StringTokenizer strTok = new StringTokenizer(s);
        do {
            shifts = new float[2];
            shifts[0] = new Float(strTok.nextToken().trim()).floatValue();
            shifts[1] = new Float(strTok.nextToken().trim()).floatValue();
            procInt = 1;
            phase = NMRSignal.PHASE_POSITIVE;
            cnmrsig = new NMRSignal(nmrSpectrum.nucleus, shifts, procInt, phase);
            nmrSpectrum.addSignal(cnmrsig);
        } while (strTok.hasMoreTokens());
        return nmrSpectrum;
    }

    /**
     * @param s           A string to parse the spectrum from
     * @param nmrSpectrum Description of the Parameter
     * @return Description of the Return Value
     */
    public static NMRSpectrum importSimpleList2D(String s,
                                                 NMRSpectrum nmrSpectrum) {
        System.out.println("This is importSimpleList2D in SpectrumImporter2D");
        float procInt;
        int phase = 0;
        float shifts[];
        StringTokenizer strTok = new StringTokenizer(s, "; \n");
        do {
            shifts = new float[2];
            shifts[0] = new Float(strTok.nextToken().trim()).floatValue();
            shifts[1] = new Float(strTok.nextToken().trim()).floatValue();
            procInt = new Float(strTok.nextToken().trim()).floatValue();
            System.out.println("parsed: " + shifts[0] + "; " + shifts[0] + "; "
                    + procInt);
            phase = NMRSignal.PHASE_POSITIVE;
            nmrSpectrum.addSignal(new NMRSignal(nmrSpectrum.nucleus, shifts,
                    procInt, phase));
        } while (strTok.hasMoreTokens());
        return nmrSpectrum;
    }

    /**
     * Import 1H - 13C copied from clip board.
     *
     * @param s
     * @param nmrSpectrum
     * @return
     */
    public static NMRSpectrum importShiftsHMBC(String s,
                                               NMRSpectrum nmrSpectrum) {
        float h_1;
        float c_13;
        float[] shifts;
        NMRSignal h_c_nmrsig;
        String tokenString = null;
        int phase = 0;
        StringTokenizer strTok = new StringTokenizer(s, "\n");
        // StringTokenizer strTok = new StringTokenizer(s);
        do {
            // chemical shift;
            tokenString = strTok.nextToken();
            System.out.println("Next Token: " + tokenString);
            String[] shift_values = tokenString.split("\\s+");
            shifts = new float[shift_values.length];
            h_1 = Float.parseFloat(shift_values[1]);
            shifts[0] = h_1;
            c_13 = Float.parseFloat(shift_values[0]);
            shifts[1] = c_13;
            phase = NMRSignal.PHASE_NONE;
            h_c_nmrsig = new NMRSignal(nmrSpectrum.nucleus, shifts, 0, phase);
            nmrSpectrum.addSignal(h_c_nmrsig);
        } while (strTok.hasMoreTokens());
        return nmrSpectrum;
    }

}
