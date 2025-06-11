package org.axolotlj.RemoteHealth.validations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FilterValidationTest {

	@Test
	public void testValidateFrequency() {
		assertTrue(FilterValidation.validateFrequency("10.5").isValid());
		assertFalse(FilterValidation.validateFrequency("-2.5").isValid());
		assertFalse(FilterValidation.validateFrequency("abc").isValid());
		assertFalse(FilterValidation.validateFrequency(null).isValid());
	}

	@Test
	public void testValidateOrder() {
		assertTrue(FilterValidation.validateOrder("2").isValid());
		assertFalse(FilterValidation.validateOrder("-1").isValid());
		assertFalse(FilterValidation.validateOrder("0").isValid());
		assertFalse(FilterValidation.validateOrder("xyz").isValid());
		assertFalse(FilterValidation.validateOrder(null).isValid());
	}

	@Test
	public void testValidateInferiorLessThanSuperior() {
		assertTrue(FilterValidation.validateInferiorLessThanSuperior("1.0", "5.0").isValid());

		ValidationResult resultEqual = FilterValidation.validateInferiorLessThanSuperior("5.0", "5.0");
		assertFalse(resultEqual.isValid());
		assertEquals(ValidationErrorType.INVALID_FORMAT, resultEqual.getErrorType());

		ValidationResult resultGreater = FilterValidation.validateInferiorLessThanSuperior("10", "5");
		assertFalse(resultGreater.isValid());
		assertEquals(ValidationErrorType.INVALID_FORMAT, resultEqual.getErrorType());

		ValidationResult resultInvalidInput = FilterValidation.validateInferiorLessThanSuperior("abc", "5");
		assertFalse(resultInvalidInput.isValid());
		assertEquals(ValidationErrorType.INVALID_FORMAT, resultInvalidInput.getErrorType());

		assertFalse(FilterValidation.validateInferiorLessThanSuperior(null, null).isValid());
	}
}
