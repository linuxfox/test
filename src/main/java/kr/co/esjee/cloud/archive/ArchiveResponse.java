package kr.co.esjee.cloud.archive;

import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fpdigital.diva.api.DivaError;
import com.fpdigital.diva.api.DivaErrorType;
import com.fpdigital.diva.api.DivaObject;
import com.fpdigital.diva.api.DivaObjectInfo;
import com.fpdigital.diva.api.Request;
import com.fpdigital.diva.api.RequestEventType;
import com.fpdigital.diva.api.RequestInfo;
import com.fpdigital.diva.api.RequestListener;
import com.fpdigital.diva.api.RequestStatus;
import com.fpdigital.diva.api.Session;
import com.fpdigital.diva.api.exceptions.DivaException;

import kr.co.esjee.cloud.constant.ArchiveConstant;
import kr.co.esjee.cloud.manager.ComponentManager;

public class ArchiveResponse implements ArchiveConstant {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private Session session;
	private String archiveSeq;
	private String requestType;
	private Map<String, Object> requestData = null;
	private ComponentManager componentManager = null;

	private ArchiveManager archiveManager = null;
	
	public ArchiveResponse(Session session, String archiveSeq, String requestType, Map<String, Object> requestData,
			ComponentManager componentManager, Request request, SUBMIT_RESULT submitResult, ArchiveManager archiveManager) {
		this.session = session;
		this.archiveSeq = archiveSeq;
		this.requestType = requestType;
		this.requestData = requestData;
		this.componentManager = componentManager;
		
		this.archiveManager = archiveManager;
		
		if (SUBMIT_RESULT.Success == submitResult) {
			this.response(request);
		} else if (SUBMIT_RESULT.ConnectionFailed == submitResult) {
			this.convertAndSend(STATUS.ConnectionFailed.getCode(), null, ERROR_CODE.DIVA_CONNECTION_FAILED.getCode(),
					false);
		}
	}
	
//	public ArchiveResponse(String archiveSeq, String requestType, Map<String, Object> requestData,
//			ComponentManager componentManager, Request request, SUBMIT_RESULT submitResult, ArchiveManager archiveManager) {
//		this.archiveSeq = archiveSeq;
//		this.requestType = requestType;
//		this.requestData = requestData;
//		this.componentManager = componentManager;
//		
//		this.archiveManager = archiveManager;
//		
//		if (SUBMIT_RESULT.Success == submitResult) {
//			this.response(request);
//		} else if (SUBMIT_RESULT.ConnectionFailed == submitResult) {
//			this.convertAndSend(STATUS.ConnectionFailed.getCode(), null, ERROR_CODE.DIVA_CONNECTION_FAILED.getCode(),
//					false);
//		}
//	}
	
	private void response(Request request) {
		try {
			RequestStatus requestStatus = request.getStatus();
			boolean isRunning = RequestStatus.Running.equals(requestStatus);

			RequestInfo info = request.getInfo();

			String errorCode = "";
			DivaError error = info.getError();
			if (error != null) {
				DivaErrorType errorType = error.getType();
				errorCode = ERROR_CODE.getCode(errorType.getAPIAbortionCode());
				logger.error("Diva Error : " + error.toString());
				logger.error(ERROR_CODE.getMessage(errorCode));
			}

			this.convertAndSend(STATUS.getCode(requestStatus), info.getRequestId(), errorCode, isRunning);

			if (isRunning) {
				logger.info("Diva Running Listener start!!");
				request.addListener(new RequestListener() {
					public void onRequestStateChanged(Request request, RequestEventType event) {
						response(request);
					}
				}, RequestEventType.FinalState);
			}
			else
			{
				close();
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Diva response Error : " + e.getMessage());
		}
	}
	
	private void close() {
		try {
			if (session != null) {
				session.close();
			}
			session = null;
		} catch (Exception e) {
			logger.error("Diva close Failed! - " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void convertAndSend(String stateCode, Object requestId, String errorCode, boolean isRunning) {

		try {
			JSONObject jobResult = new JSONObject();
			jobResult.put("archiveSeq", archiveSeq);
			jobResult.put("requestType", requestType);
			jobResult.put("requestData", requestData);
			jobResult.put("poolType", this.componentManager.getComponentType());

			jobResult.put("requestState", stateCode);
			jobResult.put("requestId", requestId);
			jobResult.put("errorCode", errorCode);

			//상태 코드 값이 성공일 경우 용량 데이터 
			if(ArchiveConstant.STATUS.Completed.getCode().equals(stateCode)) {
				jobResult.put("sizeBytes", getSizeBytes(String.valueOf(requestData.get("objectName")), String.valueOf(requestData.get("objectCategory"))));
			}
			
			this.componentManager.convertAndSend(jobResult.toJSONString(), !isRunning);

			logger.info("Diva response : " + jobResult.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private DivaObjectInfo getObjectInfo(String objectName, String category) throws DivaException {
		if (session == null || session.isClosed()) {
			close();
			session = archiveManager.connect();
		}
		if (session != null)
		{	
			DivaObject divaObject = new DivaObject(objectName, category);
			return this.session.getObjectInfo(divaObject);
		}
		return null;
	}
	
	public long getSizeBytes(String objectName, String category) {
		try {
			return this.getObjectInfo(objectName, category).getSizeBytes();
		} catch(DivaException e) {
			logger.error("getSizeBytes Exception", e);
		} finally {
			this.close();
		}
		
		return 0;
	}
	
	public long getSizeKb(String objectName, String category) {
		try {
			return this.getObjectInfo(objectName, category).getSizeKb();
		} catch(DivaException e) { 
			logger.error("getSizeKb Exception", e);
		} finally {
			this.close();
		}
		
		return 0;
	}
}
