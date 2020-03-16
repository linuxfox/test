package kr.co.esjee.cloud.job.diva;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fpdigital.diva.api.ApiMessagingConstants;
import com.fpdigital.diva.api.ApiPartialRestoreFileSummary;
import com.fpdigital.diva.api.ApiPartialRestoreOffsetSummary;
import com.fpdigital.diva.api.DivaApi;
import com.fpdigital.diva.api.DivaInstance;
import com.fpdigital.diva.api.Option;
import com.fpdigital.diva.api.QOS;
import com.fpdigital.diva.api.Request;
import com.fpdigital.diva.api.Session;
import com.fpdigital.diva.api.SessionParameters;
import com.fpdigital.diva.api.requests.ArchiveRequest;
import com.fpdigital.diva.api.requests.PartialRestoreRequest;
import com.fpdigital.diva.api.requests.RestoreRequest;

import kr.co.esjee.cloud.constant.ArchiveConstant;
import kr.co.esjee.cloud.manager.ComponentManager;

@Component
public class ArchiveManager implements ArchiveConstant {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("#{common['diva.hostname']}")
	private String hostname;

	@Value("#{common['diva.port']}")
	private String port;

	@Value("#{common['diva.username']}")
	private String username;

	@Value("#{common['diva.password']}")
	private String password;

	@Value("#{common['diva.userinfo']}")
	private String userinfo;

	@Value("#{common['diva.sitename']}")
	private String sitename;

	@Value("#{common['diva.response.timeout']}")
	private String sResponseTimeOut;

	private static ArchiveManager instance;

	public static ArchiveManager Instance() throws Exception {
		if (instance == null) {
			instance = new ArchiveManager();
		}
		return instance;
	}

	private ArchiveManager() {
		try {
			if (ArchiveManager.instance == null) {
				ArchiveManager.instance = this;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Session connect() {
		Session session = null;
		try {
			int _port = Integer.parseInt(port);
			logger.info("Diva connecting : hostname[" + hostname + "] port[" + _port + "]");
			DivaInstance diva = DivaApi.getDivaInstance(hostname, _port);
			int responseTimeOut = Integer.parseInt(sResponseTimeOut);
			diva.getDefaultPolicy().setResponseTimeoutMs(responseTimeOut);
			diva.getDefaultPolicy().setRequestPollingIntervalMs(1000);
			SessionParameters sp = new SessionParameters(hostname, _port, username, password, "JInitiator",
					"JInitiator session name", userinfo, sitename, "log");
			session = diva.createSession(sp, null);
			logger.info("Diva connected : hostname[" + hostname + "] port[" + _port + "]");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Diva connect Failed! - " + e.getMessage());
			session = null;
		}
		return session;
	}

	public SUBMIT_RESULT submit(Session session, Request request) throws Exception {
		if (session == null) {
			return SUBMIT_RESULT.ConnectionFailed;
		}
		session.submit(request);
		return SUBMIT_RESULT.Success;
	}

	@SuppressWarnings("unchecked")
	public void run(String jobParam, ComponentManager componentManager) {
		Request request = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> message = mapper.readValue(jobParam, Map.class);
			String archiveSeq = String.valueOf(message.get("archiveSeq"));
			String requestType = String.valueOf(message.get("requestType"));
			Map<String, Object> requestData = (Map<String, Object>) message.get("requestData");

			switch (requestType) {
			case ArchiveConstant.REQUEST_ARCHIVE:
				request = archive(requestData);
				break;
			case ArchiveConstant.REQUEST_RESTORE:
				request = restore(requestData);
				break;
			case ArchiveConstant.REQUEST_PARTIAL_RESTORE:
				request = partialRestore(requestData);
				break;
			default:
				break;
			}
			
			Session session = connect();
			SUBMIT_RESULT submitResult = this.submit(session, request);
			new ArchiveResponse(session, archiveSeq, requestType, requestData, componentManager, request, submitResult, ArchiveManager.Instance());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ArchiveManager run Exception! - " + e.getMessage());
			componentManager.restProcess();
		}
	}

	@SuppressWarnings("unchecked")
	private ArchiveRequest archive(Map<String, Object> message) throws Exception {
		try {
			String objectName = String.valueOf(message.get("objectName"));
			String objectCategory = String.valueOf(message.get("objectCategory"));
			String source = String.valueOf(message.get("source"));
			String mediaName = String.valueOf(message.get("mediaName"));
			String filesPathRoot = String.valueOf(message.get("filesPathRoot"));
			Vector<String> filenamesListV = new Vector<String>();
			List<String> filenamesList = (List<String>) message.get("filenamesList");
			for (String filename : filenamesList) {
				filenamesListV.add(filename);
			}
			QOS qos = OUALITY_OF_SERVICE.getValue(String.valueOf(message.get("qualityOfService")));
			String comment = String.valueOf(message.get("comment"));
			String archiveOptions = ((message.get("archiveOptions") != null)
					? String.valueOf(message.get("archiveOptions")) : "");
			int priorityLevel = ((message.get("priorityLevel") != null)
					? Integer.parseInt(String.valueOf(message.get("priorityLevel"))) : -1);

			ArchiveRequest request = new ArchiveRequest(objectName, objectCategory, source, mediaName, filesPathRoot,
					filenamesListV, qos, priorityLevel, comment, archiveOptions);

			boolean deleteSource = (boolean) message.get("deleteSource");
			if (deleteSource) {
				request.addOption(Option.DeleteOnSource);
			}
			return request;

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Archive Exception : " + e.getMessage());
			throw e;
		}
	}

	private RestoreRequest restore(Map<String, Object> message) throws Exception {
		try {
			String objectName = String.valueOf(message.get("objectName"));
			String objectCategory = String.valueOf(message.get("objectCategory"));
			String destination = String.valueOf(message.get("destination"));
			String filesPathRoot = String.valueOf(message.get("filesPathRoot"));
			QOS qualityOfService = OUALITY_OF_SERVICE.getValue(String.valueOf(message.get("qualityOfService")));
			int additionalServices = Integer.parseInt(String.valueOf(message.get("additionalServices")));
			String restoreOptions = String.valueOf(message.get("restoreOptions"));
			int priorityLevel = Integer.parseInt(String.valueOf(message.get("priorityLevel")));
			RestoreRequest request = new RestoreRequest(objectName, objectCategory, destination, filesPathRoot,
					qualityOfService, additionalServices, restoreOptions, priorityLevel);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Restore Exception : " + e.getMessage());
			throw e;
		}
	}

	private PartialRestoreRequest partialRestore(Map<String, Object> message) throws Exception {
		try {
			String objectName = String.valueOf(message.get("objectName"));
			String objectCategory = String.valueOf(message.get("objectCategory"));
			int instanceId = ((message.get("instanceId") != null)
					? Integer.parseInt(String.valueOf(message.get("instanceId"))) : -1);
			int format = ((message.get("format") != null) ? Integer.parseInt(String.valueOf(message.get("format")))
					: ApiMessagingConstants.FORMAT_VIDEO_MXF);
			Vector<?> filenamesListV = getPartialFileList(message);
			String destination = String.valueOf(message.get("destination"));
			String filesPathRoot = String.valueOf(message.get("filesPathRoot"));
			QOS qualityOfService = OUALITY_OF_SERVICE.getValue(String.valueOf(message.get("qualityOfService")));
			String restoreOptions = ((message.get("restoreOptions") != null)
					? String.valueOf(message.get("restoreOptions")) : "");
			int priorityLevel = ((message.get("priorityLevel") != null)
					? Integer.parseInt(String.valueOf(message.get("priorityLevel"))) : -1);

			PartialRestoreRequest request = new PartialRestoreRequest(objectName, objectCategory, instanceId, format,
					filenamesListV, destination, filesPathRoot, qualityOfService, restoreOptions, priorityLevel);
			return request;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("PartialRestore Exception : " + e.getMessage());
			throw e;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Vector<?> getPartialFileList(Map<String, Object> message) throws Exception {
		try {
			Vector fileInfoList = new Vector();
			int format = ((message.get("format") != null) ? Integer.parseInt(String.valueOf(message.get("format")))
					: ApiMessagingConstants.FORMAT_VIDEO_MXF);
			switch (format) {
			case ApiMessagingConstants.FORMAT_VIDEO_MXF:
				List<Map<String, Object>> partialFileList = (List<Map<String, Object>>) message.get("partialFileList");
				for (Map<String, Object> partialFile : partialFileList) {
					String archiveFile = String.valueOf(partialFile.get("archiveFile"));
					String destFile = String.valueOf(partialFile.get("destFile"));
					Vector offsetFileList = new Vector();
					List<Map<String, Object>> offsetList = (List<Map<String, Object>>) partialFile.get("offsetList");
					for (Map<String, Object> offset : offsetList) {
						String startTimeCode = String.valueOf(offset.get("startTimeCode"));
						String endTimeCode = String.valueOf(offset.get("endTimeCode"));
						offsetFileList.add(new ApiPartialRestoreOffsetSummary(startTimeCode, endTimeCode));
					}
					fileInfoList.add(new ApiPartialRestoreFileSummary(archiveFile, destFile, offsetFileList));
				}
				break;

			case ApiMessagingConstants.FORMAT_FOLDER_BASED:
				break;
			}
			return fileInfoList;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("getPartialFileList Exception : " + e.getMessage());
			throw e;
		}
	}

	
}
