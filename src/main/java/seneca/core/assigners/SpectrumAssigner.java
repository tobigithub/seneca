/* SpectrumAssigner.java
 *
 * Copyright (C) 1997, 1998, 1999  Dr. Christoph Steinbeck
 *
 * Contact: steinbeck@ice.mpg.de
 *
 * This software is published and distributed under artistic license.
 * The intent of this license is to state the conditions under which this Package
 * may be copied, such that the Copyright Holder maintains some semblance
 * of artistic control over the development of the package, while giving the
 * users of the package the right to use and distribute the Package in a
 * more-or-less customary fashion, plus the right to make reasonable modifications.
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * The complete text of the license can be found in a file called LICENSE
 * accompanying this package.
 */

package seneca.core.assigners;

import seneca.core.SenecaDataset;

public class SpectrumAssigner {
    SenecaDataset sd = null;

    public SpectrumAssigner(SenecaDataset sd) {
        this.sd = sd;
    }

    public SpectrumAssigner() {

    }

    public void setSenecaDataset(SenecaDataset sd) {
        this.sd = sd;
    }

    public boolean assign() {
        return false;
    }

}
