package org.axolotlj.RemoteHealth.validations;

import org.junit.jupiter.api.Test;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class BaseValidationTest {

    @Test
    public void testIsNotNullOrEmpty() {
        assertTrue(BaseValidation.isNotNullOrEmpty("hello"));
        assertFalse(BaseValidation.isNotNullOrEmpty(""));
        assertFalse(BaseValidation.isNotNullOrEmpty("   "));
        assertFalse(BaseValidation.isNotNullOrEmpty(null));
    }

    @Test
    public void testIsPositiveInteger() {
        assertTrue(BaseValidation.isPositiveInteger("123"));
        assertFalse(BaseValidation.isPositiveInteger("0"));
        assertFalse(BaseValidation.isPositiveInteger("-1"));
        assertFalse(BaseValidation.isPositiveInteger("abc"));
        assertFalse(BaseValidation.isPositiveInteger(null));
    }

    @Test
    public void testIsPositiveDecimal() {
        assertTrue(BaseValidation.isPositiveDecimal("3.14"));
        assertFalse(BaseValidation.isPositiveDecimal("0"));
        assertFalse(BaseValidation.isPositiveDecimal("-5.1"));
        assertFalse(BaseValidation.isPositiveDecimal("abc"));
        assertFalse(BaseValidation.isPositiveDecimal(null));
    }

    @Test
    public void testIsWithinRange() {
        assertTrue(BaseValidation.isWithinRange(5, 1, 10));
        assertTrue(BaseValidation.isWithinRange(1, 1, 10));
        assertTrue(BaseValidation.isWithinRange(10, 1, 10));
        assertFalse(BaseValidation.isWithinRange(0, 1, 10));
        assertFalse(BaseValidation.isWithinRange(11, 1, 10));
    }

    @Test
    public void testIsFileValid() {
        File validFile = new File("build.gradle"); // Usa un archivo conocido si estás en entorno local
        File invalidFile = new File("non_existent_file.txt");

        assertEquals(validFile.exists() && validFile.isFile(), BaseValidation.isFileValid(validFile));
        assertFalse(BaseValidation.isFileValid(invalidFile));
        assertFalse(BaseValidation.isFileValid(null));
    }

    @Test
    public void testIsValidName() {
        assertTrue(BaseValidation.isValidName("Juan Pérez"));
        assertTrue(BaseValidation.isValidName("María123"));
        assertFalse(BaseValidation.isValidName("!@#"));
        assertFalse(BaseValidation.isValidName(""));
        assertFalse(BaseValidation.isValidName(null));
    }

    @Test
    public void testIsValidIPv4() {
        assertTrue(BaseValidation.isValidIPv4("192.168.0.1"));
        assertTrue(BaseValidation.isValidIPv4("localhost"));
        assertFalse(BaseValidation.isValidIPv4("999.999.999.999"));
        assertFalse(BaseValidation.isValidIPv4("abc.def.ghi.jkl"));
        assertFalse(BaseValidation.isValidIPv4(null));
    }

    @Test
    public void testIsValidIPv6() {
        assertTrue(BaseValidation.isValidIPv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertTrue(BaseValidation.isValidIPv6("localhost"));
        assertFalse(BaseValidation.isValidIPv6("2001:db8::1::1"));
        assertFalse(BaseValidation.isValidIPv6("invalid_ip"));
        assertFalse(BaseValidation.isValidIPv6(null));
    }

    @Test
    public void testHandleValidation() {
        ValidationResult validResult = new ValidationResult(true, null, null);
        ValidationResult invalidResult = new ValidationResult(false, "Formato incorrecto", ValidationErrorType.INVALID_FORMAT);

        // Asumimos que AlertUtil.showWarningAlert() no lanza excepciones
        assertFalse(BaseValidation.handleValidation(validResult));
        assertTrue(BaseValidation.handleValidation(invalidResult));
    }
}
