package org.jscsi.target.settings;

import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public class NumericValueTest {

    @Test
    public void testParseNumericValueString() {

        // setup test strings
        final String a = "adf";
        final String b = "123k98";
        final String c = "3443.0";
        final String d = "1701";
        final String e = "-9816902";
        final String f = "16~10";
        final String g = "10~16";

        // test conversion
        final NumericalValue na = NumericalValue.parseNumericalValue(a);
        if (na != null)
            fail(a);

        final NumericalValue nb = NumericalValue.parseNumericalValue(b);
        if (nb != null)
            fail(b);

        final NumericalValue nc = NumericalValue.parseNumericalValue(c);
        if (nc != null)
            fail(c);

        final NumericalValue nd = NumericalValue.parseNumericalValue(d);
        if (!(nd instanceof SingleNumericalValue))
            fail(d + " should have been a SingleNumericValue");

        final NumericalValue ne = NumericalValue.parseNumericalValue(e);
        if (!(ne instanceof SingleNumericalValue))
            fail(e + " should have been a SingleNumericValue");

        final NumericalValue nf = NumericalValue.parseNumericalValue(f);
        if (nf != null)
            fail(f);

        final NumericalValue ng = NumericalValue.parseNumericalValue(g);
        if (!(ng instanceof NumericalValueRange))
            fail(g + " should have been a NumericValueRange");
    }

}
