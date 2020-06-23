package kr.co.esjee.cloud.job.quantum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import kr.co.esjee.cloud.constant.ArchiveConstant;
import kr.co.esjee.cloud.constant.ArchiveConstant.OUALITY_OF_SERVICE;
import kr.co.esjee.cloud.constant.ArchiveConstant.SUBMIT_RESULT;
import kr.co.esjee.cloud.job.diva.ArchiveManager;
import kr.co.esjee.cloud.manager.ComponentManager;

@Component
public class QuantumArchiveManager implements ArchiveConstant {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("#{common['archive.hostname']}")
	private String hostName;

	@Value("#{common['archive.port']}")
	private String port;

	@Value("#{common['archive.username']}")
	private String userName;

	@Value("#{common['archive.password']}")
	private String password;	
	
	private boolean useHttps = true;
	private static final String FROMAT = "json";
	
	private static final String COMPLATE_CODE = "FS0000";
	private static final String COMPLATE_STATUS = "completed";
	
	private QuantumArchiveUtil quantumManager = new QuantumArchiveUtil();
	
	private static QuantumArchiveManager instance;

	public static QuantumArchiveManager Instance() throws Exception {
		if (instance == null) {
			instance = new QuantumArchiveManager();
		}
		return instance;
	}

	
	@SuppressWarnings("unchecked")
	public void run(String jobParam, ComponentManager componentManager) {
		String response = null;
		String archiveSeq = null;
		String requestType = null;
		String objectName = null;
		Map<String, Object> requestData = null;
		String stateCode = "";
		String requestId = "";
		String errorCode = "";
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> message = mapper.readValue(jobParam, Map.class);
			objectName = String.valueOf(message.get("objectName"));
			archiveSeq = String.valueOf(message.get("archiveSeq"));
			requestType = String.valueOf(message.get("requestType"));
			requestData = (Map<String, Object>) message.get("requestData");

			stateCode = "10";
			
			boolean isRunning = true;
			
			switch (requestType) {
			case ArchiveConstant.REQUEST_ARCHIVE:
				response = archive(requestData);
				break;
			case ArchiveConstant.REQUEST_RESTORE:
				response = restore(requestData);
				break;
			default:
				break;
			}
			
			if(response != null) {				
				logger.debug("--------------  Quantum  response start ----------------");
				logger.debug(response);
				logger.debug("--------------  Quantum  response end   ----------------");
				
				ObjectMapper responseMapper = new ObjectMapper();
				Map<String, Object> responseMap = responseMapper.readValue(response, Map.class);
				List<Map<String, Object>> responseStatuses = (List<Map<String, Object>>) responseMap.get("statuses");
				
				if(checkComplate(responseStatuses)) { // 결과상태 성공여부 check
					logger.debug("----->>  Quantum  response complate !!! <<-------");
					convertAndSend(archiveSeq, requestType, requestData, componentManager, stateCode, requestId, errorCode, isRunning);
				}
				else {
					logger.debug("----->>  Quantum  response fail !!! <<-------");
					componentManager.jobError(archiveSeq, objectName, ArchiveConstant.ERROR_CODE.API_ERR_INTERNAL);
					convertAndSend(archiveSeq, requestType, requestData, componentManager, "98", requestId, errorCode, isRunning);
				}
			}
			else {
				logger.debug("----->>  Quantum  response NULL !!! <<-------");				
				componentManager.jobError(archiveSeq, objectName, ArchiveConstant.ERROR_CODE.API_ERR_INTERNAL);
				convertAndSend(archiveSeq, requestType, requestData, componentManager, "98", requestId, errorCode, isRunning);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			logger.error("ArchiveManager run Exception! - " + e.getMessage());
			componentManager.jobError(archiveSeq, objectName, ArchiveConstant.ERROR_CODE.API_ERR_INTERNAL);
			convertAndSend(archiveSeq, requestType, requestData, componentManager, "98", requestId, errorCode, true);
			//componentManager.jobError(archiveSeq, objectName, ArchiveConstant.ERROR_CODE.API_ERR_INTERNAL.toString());			
		}
	}
	
	private String archive(Map<String, Object> message) throws Exception {
		try {
			String objectName = String.valueOf(message.get("objectName"));
			String objectCategory = String.valueOf(message.get("objectCategory"));
			String source = String.valueOf(message.get("source"));
			String mediaName = String.valueOf(message.get("mediaName"));
			String filesPathRoot = String.valueOf(message.get("filesPathRoot"));
			List<String> filenamesListV = new ArrayList<String>();
			boolean deleteSource = (boolean) message.get("deleteSource");
			
			List<String> filenamesList = (List<String>) message.get("filenamesList");			
			for (String filename : filenamesList) {
				filenamesListV.add(filename);
			}
			
			String jsonString = quantumManager.getFsStore(this.useHttps, this.userName, this.password, this.hostName, this.port, this.FROMAT, 
					filesPathRoot, filenamesListV, 1);
			
			/*
			QOS qos = OUALITY_OF_SERVICE.getValue(String.valueOf(message.get("qualityOfService")));
			String comment = String.valueOf(message.get("comment"));
			String archiveOptions = ((message.get("archiveOptions") != null)
					? String.valueOf(message.get("archiveOptions")) : "");
			int priorityLevel = ((message.get("priorityLevel") != null)
					? Integer.parseInt(String.valueOf(message.get("priorityLevel"))) : -1);

			ArchiveRequest request = new ArchiveRequest(objectName, objectCategory, source, mediaName, filesPathRoot,
					filenamesListV, qos, priorityLevel, comment, archiveOptions);

			if (deleteSource) {
				request.addOption(Option.DeleteOnSource);
			}
			*/
			
			//return request;
			logger.debug("----------------------- quantum output start -------------------------");
			logger.debug(jsonString);
			logger.debug("-----------------------  quantum output end  -------------------------");
			return jsonString;

		} 
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Archive Exception : " + e.getMessage());
			throw e;
		}		
	}
	
	/**
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	private String restore(Map<String, Object> message) throws Exception {
		String filesPathRoot = String.valueOf(message.get("filesPathRoot"));
		String filesName = String.valueOf(message.get("filesName"));
		
		String objectName = String.valueOf(message.get("objectName"));
		String objectCategory = String.valueOf(message.get("objectCategory"));
		String destination = String.valueOf(message.get("destination"));		
		int additionalServices = Integer.parseInt(String.valueOf(message.get("additionalServices")));
		String restoreOptions = String.valueOf(message.get("restoreOptions"));
		int priorityLevel = Integer.parseInt(String.valueOf(message.get("priorityLevel")));
		
		String filePath = "";
		
		filePath = filesPathRoot + filesName; // 파일명 연동필요
		
		String response = quantumManager.getFsRetrieve(this.useHttps, this.userName, this.password, this.hostName, this.port, this.FROMAT, filePath);
		
		return response;
	}
	
	private void convertAndSend(String archiveSeq, String requestType, Map<String, Object> requestData, ComponentManager componentManager, 
			String stateCode, Object requestId, String errorCode, boolean isRunning) {
		try {
			JSONObject jobResult = new JSONObject();
			jobResult.put("archiveSeq", archiveSeq);
			jobResult.put("requestType", requestType);
			jobResult.put("requestData", requestData);
			jobResult.put("poolType", componentManager.getComponentType());

			jobResult.put("requestState", stateCode);
			jobResult.put("requestId", requestId);
			jobResult.put("errorCode", errorCode);

			//상태 코드 값이 성공일 경우 용량 데이터 
			if(ArchiveConstant.STATUS.Completed.getCode().equals(stateCode)) {
				/*
				jobResult.put("sizeBytes", 
						getSizeBytes(String.valueOf(requestData.get("objectName")), 
								String.valueOf(requestData.get("objectCategory")))
						);
				*/
			}
			
			componentManager.convertAndSend(jobResult.toJSONString(), !isRunning);

			logger.info("Quantum response : " + jobResult.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	private long getSizeBytes(String objectName, String category) {
		try {
			return 0;
		} 
		catch(Exception e) {
			
		}
		
		return 0;
	}	
	
	private boolean checkComplate(List<Map<String, Object>> responseStatuses) {
		boolean check = false;
		
		for(Map<String, Object> responseStatus : responseStatuses) {
			String statusCode = (String) responseStatus.get("statusCode");
			String commandStatus = (String) responseStatus.get("commandStatus");
			
			if(statusCode.equals(COMPLATE_CODE) && commandStatus.equals(COMPLATE_STATUS)) { // 결과값이 성공인 case
				check = true;
			}
		}
		return check;
	}
}
