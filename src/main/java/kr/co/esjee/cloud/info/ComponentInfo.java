package kr.co.esjee.cloud.info;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;

import kr.co.esjee.cloud.constant.BaseConstant;
import kr.co.esjee.cloud.util.JSONUtil;

public class ComponentInfo {
	@Value("#{common['component.type']}")
	private String componentType;

	@Value("#{common['component.ip']}")
	private String componentIp;

	@Value("#{common['component.port']}")
	private String componentPort;

	// 작업 가져올 큐 목록
	private Object[] queueList;

	// 최대 작업 프로세스 수
	@Value("#{common['component.max.process.cnt']}")
	private int maxProcCnt;

	// 작업 프로세스 수
	private int procCnt;

	// MQ에 등록 실패 메세지
	private HashMap<String, HashMap<String, String>> messageMap = new HashMap<String, HashMap<String, String>>();

	@Value("#{common['component.home.path']}")
	private String componentHomePath;

	@Value("#{common['component.transcoder.path']}")
	private String transcoderPath;

	@Value("#{common['component.cataloger.path']}")
	private String catalogerPath;

	@Value("#{common['component.audio.qc.path']}")
	private String audioQcPath;

	@Value("#{common['component.video.qc.path']}")
	private String videoQcPath;

	@Value("#{common['component.video.marker.path']}")
	private String markerPath;
	
	public Message jobMessage = null;
	
	public String getComponentType() {
		return componentType;
	}

	public String getComponentIp() {
		return componentIp;
	}

	public String getComponentPort() {
		return componentPort;
	}

	public boolean isEmptyForQueueList() {
		return this.queueList.length == 0 ? true : false;
	}
	
	public Object[] getQueueList() {
		return queueList;
	}

	public void setQueueList(Object[] queueList) {
		this.queueList = queueList;
	}

	public int getMaxProcCnt() {
		return maxProcCnt;
	}

	public int getProcCnt() {
		return procCnt;
	}

	public void setProcCnt(int procCnt) {
		this.procCnt = procCnt;
	}

	/**
	 * @Description 작업 프로세스 사용
	 * @author Cho Oh-jung (2016. 6. 21.)
	 */
	public synchronized void useProcess() {
		if (this.getMaxProcCnt() > this.getProcCnt())
			this.setProcCnt(this.getProcCnt() + 1);
	}

	/**
	 * @Description 작업 프로세스 반환
	 * @author Cho Oh-jung (2016. 6. 21.)
	 */
	public synchronized void restProcess() {
		if (this.getProcCnt() > 0)
			this.setProcCnt(this.getProcCnt() - 1);
	}

	/**
	 * @Description 남은 프로세스 수
	 * @author Cho Oh-jung (2016. 6. 21.)
	 * @return int
	 */
	public synchronized int getRestProcCnt() {
		return this.getMaxProcCnt() - this.getProcCnt();
	}

	/**
	 * @Description 맵 추가
	 * @author Cho Oh-jung (2016. 6. 29.)
	 * @param key
	 * @param message
	 */
	public synchronized void putMessageMap(String key, HashMap<String, String> message) {
		this.messageMap.put(key, message);
	}

	/**
	 * @Description 맵 조회
	 * @author Cho Oh-jung (2016. 6. 29.)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, HashMap<String, String>> getMessageMap() {
		return (HashMap<String, HashMap<String, String>>) this.messageMap.clone();
	}

	/**
	 * @Description 맵의 빈 상태 여부
	 * @author Cho Oh-jung (2016. 6. 29.)
	 * @return
	 */
	public boolean isEmptyForMessageMap() {
		return this.messageMap.isEmpty();
	}

	/**
	 * @Description 맵 삭제
	 * @author Cho Oh-jung (2016. 6. 29.)
	 * @param key
	 */
	public void removeMessageMap(String key) {
		this.messageMap.remove(key);
	}

	@SuppressWarnings("unchecked")
	public String getProc() {
		JSONObject result = this.getComponentInfo();

		result.put("procCnt", this.getProcCnt());
		result.put("maxProcCnt", this.getMaxProcCnt());
		
		if (jobMessage != null) {
			try {
				Map<String, Object> param = JSONUtil.getMapForString(new String(jobMessage.getBody(), "UTF-8"));
				result.put("contentId", param.get("contentId"));
				result.put("jobSeq", param.get("jobSeq"));
			} catch (UnsupportedEncodingException e) {
			} catch (ParseException e) {
			}
		}
		
		return result.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public JSONObject getComponentInfo() {
		JSONObject result = new JSONObject();
		result.put("ip", this.componentIp);
		result.put("port", this.componentPort);
		result.put("type", this.componentType);

		return result;
	}

	public String getRunPath() {
		switch (this.componentType) {
			case BaseConstant.COMPONENT_TYPE_1001:
			case BaseConstant.COMPONENT_TYPE_1002:
			case BaseConstant.COMPONENT_TYPE_1012:
				return this.componentHomePath + this.transcoderPath;
			case BaseConstant.COMPONENT_TYPE_1003:
				return this.componentHomePath + this.catalogerPath;
			case BaseConstant.COMPONENT_TYPE_1011:
				return this.componentHomePath + this.videoQcPath;
			case BaseConstant.COMPONENT_TYPE_1013:
			case BaseConstant.COMPONENT_TYPE_1014:
				return this.componentHomePath + this.audioQcPath;
			case BaseConstant.COMPONENT_TYPE_1015:
				return this.componentHomePath + this.markerPath;
		}

		return null;
	}
}
