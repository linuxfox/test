package kr.co.esjee.cloud.job.social;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.co.esjee.cloud.constant.ErrorConstant.ERROR_CODE;
import kr.co.esjee.cloud.job.BaseJob;
import kr.co.esjee.cloud.job.BaseJobException;
import kr.co.esjee.transfer.social.SocialTransferator;

public class SocialTransferJob extends BaseJob {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private String requestMessage;
	
	public SocialTransferJob(String jobMsg) {		
		super(jobMsg);
		logger.debug(jobMsg);	
		this.requestMessage = jobMsg;
		
	}

	@Override
	protected void _doWork() throws BaseJobException {		
		logger.debug(requestMessage);
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> message = mapper.readValue(requestMessage, Map.class);
			
			List<Map<String, Object>> inputs = (List<Map<String, Object>>) message.get("inputs");
			
			for(Map<String, Object> input : inputs) {
				String uri = (String) input.get("uri");
				String shareType = (String) input.get("shareType");
				String token = (String) input.get("token");
				String clientId = (String) input.get("clientId");
				String clientSecret = (String) input.get("clientSecret");
				String title = (String) input.get("title");
				String description = (String) input.get("description");
				
				Map<String, Object> transferMapper = new HashMap<String, Object>();
				transferMapper.put("shareType", shareType);
				transferMapper.put("token", token);
				transferMapper.put("clientId", clientId);
				transferMapper.put("clientSecret", clientSecret);
				transferMapper.put("title", title);
				transferMapper.put("description", description);
				transferMapper.put("videoFilePath", uri);
				String messageJson = getSocialTransferJson(transferMapper);
				SocialTransferator socialTransferator = new SocialTransferator();			
				socialTransferator.transfer(messageJson);
			}
						

		}
		catch(Exception e) {
			e.printStackTrace();
			throw new BaseJobException(e, ERROR_CODE.F9999);
		}
	}

	private String getSocialTransferJson(Map<String, Object> params) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		String message = mapper.writeValueAsString(params);
		return message;
	}
}
