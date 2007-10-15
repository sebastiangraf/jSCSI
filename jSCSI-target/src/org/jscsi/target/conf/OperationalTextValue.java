package org.jscsi.target.conf;

public class OperationalTextValue {

	public static final String NUMERIC_RANGE_DELIMITER = "~";

	public static final String BOOLEAN_YES = "Yes";

	public static final String BOOLEAN_NO = "No";

	public static final String VALUE_VALUE_DELIMITER = ",";

	private String value;

	public boolean isRange() {
		if (value.split(NUMERIC_RANGE_DELIMITER).length == 2) {
			boolean test = false;
			try {
				Integer.parseInt(value.split(NUMERIC_RANGE_DELIMITER)[0]);
				Integer.parseInt(value.split(NUMERIC_RANGE_DELIMITER)[1]);
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
	
	public int[] getRange() throws Exception{
		if(isRange()){
			int[] result = new int[2];
			result[0] = Integer.parseInt(value.split("~")[0]);
			result[1] = Integer.parseInt(value.split("~")[1]);
			return result;
		}
		throw new Exception("No valid range value: " + value);
	}

	private int numberOfValues() {
		return value.split(",").length;
	}
	
	public boolean isList(){
		return (numberOfValues() > 1 : true , false);
	}
	
	public String[] getValueList() {
		return value.split(",");
	}

}
