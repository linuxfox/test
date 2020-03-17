package kr.co.esjee.cloud.component;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.co.esjee.audioqc.SJAudioQC;
import kr.co.esjee.cloud.constant.BaseConstant;
import kr.co.esjee.cloud.constant.ErrorConstant.ERROR_CODE;
import kr.co.esjee.cloud.job.hcp.HcpJob;
import kr.co.esjee.cloud.manager.ComponentManager;
import kr.co.esjee.deletedirectory.DeleteDirectoryModule;
import kr.co.esjee.detectmarker.DetectMarker;
import kr.co.esjee.sjcataloger.SjCataloger;
import kr.co.esjee.sjtranscoder.SjTranscoder;
import kr.co.esjee.tm.cms_tm.manager.SJFileTransferManager;
import kr.co.esjee.videoqc.SJVideoQC;


/**
 * @Description 컴포넌트 실행 스레드
 * @author Cho Oh-jung (2016. 6. 23.)
 */
public class Component implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ComponentManager componentManager;

	private String jobParam;
	private Map<String, Object> jobMap;
	private ObjectMapper mapper = new ObjectMapper();

	public Component(ComponentManager componentManager, String jobParam, Map<String, Object> jobMap) {
		this.componentManager = componentManager;
		this.jobParam = jobParam;
		this.jobMap = jobMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		componentManager.jobRun(this.jobMap.get("jobSeq"), this.jobMap.get("contentId"));

		logger.debug("===== Component run Start =====");
		logger.debug("===== Component jobParam Start =====");
		logger.debug(this.jobParam);
		logger.debug("===== Component jobParam end =====");

		Map<String, Object> resultMessage = null;
		String message = null;

		try {
			// 컴포넌트별 분기 처리
			switch (componentManager.getComponentType()) {
				case BaseConstant.COMPONENT_TYPE_1001:
				case BaseConstant.COMPONENT_TYPE_1002:
				case BaseConstant.COMPONENT_TYPE_1012:
					SjTranscoder transcoder = new SjTranscoder(jobParam, componentManager.getRunPath()); // jobParam에 JSON형식의 Job description
					message = transcoder.RunTranscoder();// 컴포넌트에서 온 작업 내용
					break;
				case BaseConstant.COMPONENT_TYPE_1003:
					SjCataloger cataloger = new SjCataloger(jobParam, componentManager.getRunPath()); // jobParam에 JSON형식의 Job description
					message = cataloger.RunCataloger();
					break;
				case BaseConstant.COMPONENT_TYPE_1011:
					SJVideoQC videoQC = new SJVideoQC(jobParam, componentManager.getRunPath());
					message = videoQC.RunQC();
					break;
				case BaseConstant.COMPONENT_TYPE_1013:
				case BaseConstant.COMPONENT_TYPE_1014:
					SJAudioQC audioQC = new SJAudioQC(jobParam, componentManager.getRunPath());
					message = audioQC.RunQC();
					break;
				case BaseConstant.COMPONENT_TYPE_1015:
					DetectMarker marker = new DetectMarker(jobParam, componentManager.getRunPath());
					message = marker.RunDetection();
					break;
				case BaseConstant.COMPONENT_TYPE_1007:
				case BaseConstant.COMPONENT_TYPE_1031:
					SJFileTransferManager ftm = new SJFileTransferManager(jobParam);
					message = ftm.run();
					break;
				case BaseConstant.COMPONENT_TYPE_1040:
					HcpJob hcpJob = new HcpJob(jobParam, componentManager.getHcpServerInfo());
					message = hcpJob.run();
					break;
				case BaseConstant.COMPONENT_TYPE_1050:
					//삭제 모듈 추가
					DeleteDirectoryModule ddm = new DeleteDirectoryModule(jobParam);
					ddm.RunDelete();
					
					componentManager.restProcess();
					break;
			}

			// 리턴 메시지 Map으로 변환
			if (message != null) {
				logger.debug("===== Component run getMessage Start =====");
				logger.debug(message);
				logger.debug("===== Component run getMessage End =====");
				resultMessage = mapper.readValue(message, Map.class);
			}
		} catch (Exception e) {
			logger.error("===== Component run Error =====", e);
		}

		boolean isSuccess = false;

		if (resultMessage != null) {
			// 컴포넌트별 작업 결과 처리
			switch (componentManager.getComponentType()) {
				case BaseConstant.COMPONENT_TYPE_1003:
					componentManager.catalogerResult(message);
					break;
				case BaseConstant.COMPONENT_TYPE_1011:
				case BaseConstant.COMPONENT_TYPE_1013:
				case BaseConstant.COMPONENT_TYPE_1014:
					componentManager.qualityControlResult(message);
					break;
				case BaseConstant.COMPONENT_TYPE_1015:
					componentManager.markerResult(message);
					break;
				case BaseConstant.COMPONENT_TYPE_1040:
					componentManager.hcpJobStatus(message);
					break;
			}

			isSuccess = ERROR_CODE.isSuccess((String) resultMessage.get("code"));
			
			// 작업 성공 및 실패 처리
			if (isSuccess) {
				componentManager.jobSuccess(this.jobMap.get("jobSeq"), this.jobMap.get("contentId"), resultMessage.get("code"), this.jobMap.get("transferDiv"));
			} else {
				componentManager.jobError(this.jobMap.get("jobSeq"), this.jobMap.get("contentId"), resultMessage != null ? resultMessage.get("code") : "9999");
			}
		}

		logger.debug("===== Component run End =====");
	}
}
