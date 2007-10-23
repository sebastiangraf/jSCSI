package org.jscsi.target.conf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.datasegment.IResultFunction;
import org.jscsi.parser.datasegment.ResultFunctionFactory;
import org.jscsi.target.util.Singleton;

/**
 * This class defines standard iSCSI operational text values.
 * 
 * @author Marcus Specht
 * 
 */
public class OperationalTextValue {

	/** The Log interface. */
	private static final Log LOGGER = LogFactory
			.getLog(OperationalTextValue.class);
	
	private static final OperationalTextConfiguration globalConfig = OperationalTextConfiguration.getGlobalConfig();

	/** The ResultFunctionFactory */
	private static final ResultFunctionFactory rfFactory = new ResultFunctionFactory();

	/**
	 * The value of a result function to choose the and-value of the two
	 * parameters.
	 */
	public static final String RESULT_AND = "And";

	/** The value of a result function to choose the first two common values. */
	public static final String RESULT_CHOOSE = "Choose";

	/**
	 * The value of a result function to choose the maximum value of the two
	 * parameter values.
	 */
	public static final String RESULT_MAX = "Max";

	/**
	 * The value of a result function to choose the minimum value of the two
	 * parameter values.
	 */
	public static final String RESULT_MIN = "Min";

	/**
	 * The value of a result function to choose none of the two parameter
	 * values.
	 */
	public static final String RESULT_NONE = "None";

	/**
	 * The value of a result function to choose the or-value of the two
	 * parameter values.
	 */
	public static final String RESULT_OR = "Or";

	/** value's boolean true representation. */
	public static final String BOOLEAN_YES = "Yes";

	/** value's boolean false representation. */
	public static final String BOOLEAN_NO = "No";

	/** delimiter used between values in a list of values. */
	public static final String VALUE_VALUE_DELIMITER = ",";

	/** the delimiter used for numerical ranges. */
	public static final String NUMERIC_RANGE_DELIMITER = "~";

	/** String representation of the value */
	private String value;

	private IResultFunction resultFunction;

	private String resultType;

	/**
	 * Creates an iSCSI operational text value with a given result functionType.
	 * 
	 * @param value
	 *            operational text value
	 * @param resultType
	 *            value's result function type
	 * @throws OperationalTextException
	 */
	private OperationalTextValue(String value, String resultType)
			throws OperationalTextException {
		update(value, resultType);
	}
	
	private OperationalTextValue(String key) throws OperationalTextException{
		String value = globalConfig.getKey(key).getValue().getString();
		String resultType = globalConfig.getKey(key).getValue().getResultType();
		update(value, resultType);
	}
	
	private void update(String value, String resultType)
			throws OperationalTextException {
		if (!checkValue(value)) {
			throw new OperationalTextException("Not a valid value: " + value);
		}
		if (!checkResultType(resultType)) {
			throw new OperationalTextException("Not a valid result type: "
					+ resultType);
		}
		this.value = value;
		this.resultType = resultType;
		this.resultFunction = rfFactory.create(resultType);
	}

	public void update(String value)
			throws OperationalTextException {
		update(value, resultType);
	}

	public String getResultType() {
		return resultType;
	}

	/**
	 * Returns the value
	 * 
	 * @return value as String
	 */
	public String getString() {
		return value;
	}

	public int getInteger() throws OperationalTextException {
		if (isInteger(this)) {
			return Integer.parseInt(value);
		}
		throw new OperationalTextException("Value is not Integer: " + value);
	}

	/**
	 * Returns the maximum and minimum number of an iSCSI numerical range. // *
	 * 
	 * @return int[0] = minValue, int[1] = maxValue
	 * @throws Exception
	 *             If the value is no valid numerical range
	 */
	public int[] getRange() throws OperationalTextException {
		if (isRange(this)) {
			int[] result = new int[2];
			result[0] = Integer
					.parseInt(value.split(NUMERIC_RANGE_DELIMITER)[0]);
			result[1] = Integer
					.parseInt(value.split(NUMERIC_RANGE_DELIMITER)[1]);
			return result;
		}
		throw new OperationalTextException("No valid range value: " + value);
	}

	/**
	 * Returns the value's boolean representation.
	 * 
	 * @return true = "Yes", false = "No"
	 * @throws Exception
	 *             if not boolean
	 */
	public boolean getBoolean() throws OperationalTextException {
		if (value.equals(BOOLEAN_YES)) {
			return true;
		}
		if (value.equals(BOOLEAN_NO)) {
			return false;
		}
		throw new OperationalTextException("Value is not boolean: " + value);
	}

	/**
	 * Returns the containing values.
	 * 
	 * @return ordered values
	 * @throws OperationalTextException 
	 */
	public String[] getList() throws OperationalTextException {
		if (isList(this)) {
			return value.split(",");
		}
		throw new OperationalTextException("Not a list of values:" + value);

	}

	public static OperationalTextValue getResult(OperationalTextValue valueA, OperationalTextValue valueB)
			throws OperationalTextException {
		OperationalTextValue result = null;
		if (valueA.getResultType().equals(valueB.getResultType())) {
			result = new OperationalTextValue(valueA.resultFunction.result(valueA
					.getString(), valueB.getString()), valueA.getResultType());
		}
		return result;
	}

	public static boolean isInteger(OperationalTextValue value) {
		boolean result = false;
		try {
			Integer.parseInt(value.getString());
			result = true;
		} catch (Exception e) {
			// nothing necessary here
		}
		return result;
	}

	public static OperationalTextValue create(String key) throws OperationalTextException{
		return new OperationalTextValue(key);
	}
	
	public static OperationalTextValue create(String value, String resultType) throws OperationalTextException{
		return new OperationalTextValue(value, resultType);
	}
	
	
	
	/**
	 * Checks whether the value is a numerical range or not.
	 * 
	 * @param value
	 *            checked value
	 * @return true if numerical range, false else.
	 */
	public static boolean isRange(OperationalTextValue value) {
		if (value.getString().split(NUMERIC_RANGE_DELIMITER).length == 2) {
			boolean test = false;
			try {
				Integer.parseInt(value.getString().split(
						NUMERIC_RANGE_DELIMITER)[0]);
				Integer.parseInt(value.getString().split(
						NUMERIC_RANGE_DELIMITER)[1]);
				test = true;
			} catch (Exception e) {

			} finally {
				if (test) {
					return true;
				}
			}

		}
		return false;
	}

	/**
	 * If the value contains more than one value(separated by ','), the value is
	 * a list of values.
	 * 
	 * @return true if value is a list, false else.
	 */
	public static boolean isList(OperationalTextValue value) {
		if (value.getString().split(",").length > 1){
			return true;
		}
		return false;
	}

	/**
	 * Checks if the value is boolean, e.g "Yes" or "No"
	 * 
	 * @param value
	 *            checked value
	 * @return true if boolean, false else.
	 */
	public static boolean isBoolean(OperationalTextValue value) {
		if (value.getString().equals(BOOLEAN_YES)
				|| value.getString().equals(BOOLEAN_NO)) {
			return true;
		}
		return false;

	}

	/**
	 * Checks if the value is valid as an iSCSI operational text value
	 * 
	 * @param value
	 * @return true if valid, false else.
	 */
	public static boolean checkValue(String value) {
		return true;
	}

	/**
	 * Checks if the resultType is valid as an iSCSI result function type.
	 * 
	 * @param resultType
	 * @return true if valid, false else.
	 */
	public static boolean checkResultType(String resultType) {
		if (resultType.equals(RESULT_AND) || resultType.equals(RESULT_CHOOSE)
				|| resultType.equals(RESULT_MAX)
				|| resultType.equals(RESULT_MIN)
				|| resultType.equals(RESULT_NONE)
				|| resultType.equals(RESULT_OR)) {
			return true;
		}
		return false;
	}

	private static void throwNotAValidValueException(String value)
			throws OperationalTextException {
		throw new OperationalTextException("Not a valid Value: " + value);
	}

	private static void throwNotAValidResultTypeException(String resultType)
			throws OperationalTextException {
		throw new OperationalTextException("Not a valid resultType: "
				+ resultType);
	}

	/*
	 * private static boolean isValidValue(OperationalTextValue value){ return
	 * true; }
	 */

}
