package org.jscsi.target.util;

import static org.junit.Assert.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This Test tests the class {@link SerialArithmeticNumber} and it's
 * functionalities
 * 
 * @author Andreas Rain
 * 
 */
public class SerialArithmeticNumberTest {

    static SerialArithmeticNumber serialNumber;

    @BeforeClass
    public void beforeClass() {
        serialNumber = new SerialArithmeticNumber(1);
        assertEquals(1, serialNumber.getValue());
    }

    @Test
    public void testLessThan() {

        assertEquals(true, serialNumber.lessThan(2));
        assertEquals(false, serialNumber.lessThan(0));

        serialNumber.increment();

        assertEquals(true, serialNumber.equals(2));

    }

    @Test
    public void testGreaterThan() {

        assertEquals(true, serialNumber.greaterThan(0));
        assertEquals(false, serialNumber.greaterThan(2));

    }

}
