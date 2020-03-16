package kr.co.esjee.cloud.util;

import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @Description JSON 유틸
 * @author Cho Oh-jung (2016. 6. 21.)
 */
public class JSONUtil {
	/**
	 * @Description 문자열 -> JSONObject
	 * @author Cho Oh-jung (2016. 6. 21.)
	 * @param data
	 * @return
	 * @throws ParseException
	 */
	public static JSONObject getJsonForString(String data) throws ParseException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(data);
	}

	/**
	 * @Description 문자열 -> Map<String, Object>
	 * @author Cho Oh-jung (2016. 6. 21.)
	 * @param data
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getMapForString(String data) throws ParseException {
		JSONParser parser = new JSONParser();
		return (Map<String, Object>) parser.parse(data);
	}
}
