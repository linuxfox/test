package kr.co.esjee.cloud.manager;

import java.util.HashMap;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kr.co.esjee.cloud.component.ComponentThread;
import kr.co.esjee.cloud.constant.BaseConstant;
import kr.co.esjee.cloud.constant.BaseConstant.JOB_RESULT;
import kr.co.esjee.cloud.constant.BaseConstant.JOB_STATE;
import kr.co.esjee.cloud.constant.BaseConstant.PROCESS_EXE;
import kr.co.esjee.cloud.info.ComponentInfo;

/**
 * @Description 컴포넌트 메니저
 * @author Cho Oh-jung (2016. 6. 21.)
 */
@Component
public class ComponentManager extends ComponentInfo {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RabbitTemplate jobTemplate;

	@Value("#{common['component.job.exchange']}")
	private String jobExchange;

	@Value("#{common['job.status.queue']}")
	private String componentStatus;

	@Value("#{common['qc.result.queue']}")
	private String componentQualityControl;

	@Value("#{common['cataloger.result.queue']}")
	private String componentCataloger;

	@Value("#{common['marker.result.queue']}")
	private String componentMarker;

	@Value("#{common['process.info.exchange']}")
	private String infoExchange;

	@Value("#{common['process.info.queue']}")
	private String componentInfoStatus;

	@Value("#{common['connect.queue']}")
	private String componentConnect;

	@PostConstruct
	public void init() {
		new Thread(new ComponentThread(this)).start();

		super.setQueueList(new Object[0]);
		// super.setMaxProcCnt(0);
		super.setProcCnt(0);
	}

	/**
	 * @Description 작업 폴링
	 * @author Cho Oh-jung (2016. 6. 23.)
	 * @return
	 */
	public synchronized String getJob() {
//		logger.debug("===== ComponentManager getJob Start =====");

		// 0. 작업 가능 ProcCnt가 0이면 null
		if (super.getRestProcCnt() == 0)
			return null;

		// 1. 큐 목록 루핑
		try {
			// 폴링할 큐 리스트가 비어 있으면
			if (this.isEmptyForQueueList()) {
				this.sendConnectInfo();
				return null;
			}

			for (Object queueName : this.getQueueList()) {
				jobTemplate.setExchange(jobExchange);
				jobTemplate.setQueue(queueName.toString());
				jobTemplate.setRoutingKey(queueName.toString());
				
				logger.debug("===== ComponentManager getJob Queue : " + queueName);

				jobMessage = jobTemplate.receive(queueName.toString());

				if (jobMessage != null)
					break;
			}

			// 2. 폴링한 작업이 있을 경우 반환
			if (jobMessage != null) {
				super.useProcess();

				// Map<String, Object> jobData = mapper.readValue(new
				// String(jobMessage.getBody(), "UTF-8"), Map.class);
				// return mapper.readValue(Base64.decodeBase64(((String)
				// jobData.get("jobData"))), Map.class);
				logger.debug("======================== ComponentManager message start========================");
				logger.debug(new String(jobMessage.getBody(), "UTF-8"));
				logger.debug("======================== ComponentManager message end========================");

				return new String(jobMessage.getBody(), "UTF-8");
			}
		} catch (Exception e) {
			logger.error("===== ComponentManager getJob error =====", e);
			super.restProcess();
			return null;
		}

//		logger.debug("===== ComponentManager getJob End =====");
		return null;
	}

	/**
	 * @Description 작업 실행
	 * @author Cho Oh-jung (2016. 6. 23.)
	 * @param jobSeq
	 */
	@SuppressWarnings("unchecked")
	public void jobRun(Object jobSeq, Object contentId) {
		logger.debug("===== Component jobRun Start =====");

		JSONObject jobStatus = this.getComponentInfo();
		jobStatus.put("jobSeq", jobSeq);
		jobStatus.put("jobState", JOB_STATE.RUN.getValue());
		jobStatus.put("jobExeState", PROCESS_EXE.RUN.getValue());
		jobStatus.put("poolType", this.getComponentType());

		this.convertAndSend(this.componentStatus, jobStatus.toJSONString(), false);

		logger.debug("===== Component jobRun End =====");
	}

	/**
	 * @Description 작업 오류
	 * @author Cho Oh-jung (2016. 6. 23.)
	 * @param jobSeq
	 */
	@SuppressWarnings("unchecked")
	public void jobError(Object jobSeq, Object contentId, Object code) {
		logger.debug("===== Component jobError Start =====");
		// 작업 정보 초기화
		this.jobMessage = null;

		JSONObject jobStatus = new JSONObject();
		jobStatus.put("jobSeq", jobSeq);
		jobStatus.put("jobState", JOB_STATE.COMPLETE.getValue());
		jobStatus.put("jobExeState", PROCESS_EXE.COMPLETE.getValue());
		jobStatus.put("jobResult", JOB_RESULT.ERROR.getValue());
		jobStatus.put("poolType", this.getComponentType());

		JSONObject jobResult = new JSONObject();
		jobResult.put("jobSeq", jobSeq);
		jobResult.put("successCnt", 0);
		jobResult.put("failCnt", 1);
		jobResult.put("resultCode", code);

		jobStatus.put("result", jobResult);

		this.convertAndSend(this.componentStatus, jobStatus.toJSONString(), true);

		logger.debug("===== Component jobError End =====");
	}

	/**
	 * 작업 성공
	 * 
	 * @param jobSeq
	 * @param contentId
	 * @param code
	 * @param transferDiv
	 */
	@SuppressWarnings("unchecked")
	public void jobSuccess(Object jobSeq, Object contentId, Object code, Object transferDiv) {
		logger.debug("===== Component jobSuccess Start =====");
		// 작업 정보 초기화
		this.jobMessage = null;

		JSONObject jobStatus = new JSONObject();
		jobStatus.put("jobSeq", jobSeq);
		jobStatus.put("jobState", JOB_STATE.COMPLETE.getValue());
		jobStatus.put("jobExeState", PROCESS_EXE.COMPLETE.getValue());
		jobStatus.put("jobResult", JOB_RESULT.COMPLETE.getValue());
		jobStatus.put("poolType", this.getComponentType());
		jobStatus.put("contentId", contentId);

		// 전송일 경우에만 아래 추가
		if (BaseConstant.COMPONENT_TYPE_1007.equals(this.getComponentType())) {
			jobStatus.put("transferDiv", transferDiv);
		}

		JSONObject jobResult = new JSONObject();
		jobResult.put("jobSeq", jobSeq);
		jobResult.put("successCnt", 1);
		jobResult.put("failCnt", 0);
		jobResult.put("resultCode", code);

		jobStatus.put("result", jobResult);

		this.convertAndSend(this.componentStatus, jobStatus.toJSONString(), true);

		logger.debug("===== Component jobSuccess End =====");

	}

	/**
	 * @Description MQ 등록
	 * @author Cho Oh-jung (2016. 6. 30.)
	 * @param queueName
	 * @param message
	 */
	public void convertAndSend(String message, boolean isRest) {
		this.convertAndSend(this.componentStatus, message, isRest);
	}
	public void convertAndSend(String queueName, String message, boolean isRest) {
		logger.debug("===== Component convertAndSend : " + message);

		try {
			// 0. MQ가 down 되었거나 Network disconnect 될 경우 고려
			jobTemplate.setExchange(jobExchange);
			jobTemplate.convertAndSend(queueName, message);

			if (isRest)
				this.restProcess();
			
		} catch (AmqpException e) {
			HashMap<String, String> messageMap = new HashMap<String, String>();
			messageMap.put("queueName", queueName);
			messageMap.put("message", message);
			messageMap.put("isRest", isRest ? "T" : "F");

			super.putMessageMap(UUID.randomUUID().toString(), messageMap);
			logger.error("===== Component convertAndSend Error =====", e);
		}
	}

	/**
	 * @Description QC 결과 등록
	 * @author Cho Oh-jung (2016. 6. 30.)
	 * @param message
	 */
	public void qualityControlResult(String message) {
		this.convertAndSend(this.componentQualityControl, message, false);
	}

	/**
	 * @Description 카탈로거 결과 등록
	 * @author Cho Oh-jung (2016. 6. 30.)
	 * @param message
	 */
	public void catalogerResult(String message) {
		this.convertAndSend(this.componentCataloger, message, false);
	}

	/**
	 * @Description 마커 결과 등록
	 * @author Cho Oh-jung (2017. 3. 31.)
	 * @param message
	 */
	public void markerResult(String message) {
		this.convertAndSend(this.componentMarker, message, false);
	}

	/**
	 * @Description 작업 프로세스 전송
	 * @author Cho Oh-jung (2016. 7. 13.)
	 */
	public void sendComponentStatus() {
		jobTemplate.setExchange(infoExchange);
		jobTemplate.setQueue(componentInfoStatus);
		jobTemplate.convertAndSend(componentInfoStatus, this.getProc());
	}

	public void sendConnectInfo() {
		jobTemplate.setQueue(componentConnect);
		jobTemplate.convertAndSend(componentConnect, this.getComponentInfo().toJSONString());
	}
}
