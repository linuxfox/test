package kr.co.esjee.cloud.job;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.co.esjee.cloud.constant.BaseConstant.JOB_RESULT;
import kr.co.esjee.cloud.constant.BaseConstant.JOB_STATE;
import kr.co.esjee.cloud.constant.ErrorConstant.ERROR_CODE;
import kr.co.esjee.cloud.util.JSONUtil;

@SuppressWarnings("unchecked")
public abstract class BaseJob {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected JSONObject request = null;
	public JSONObject getRequest() {
		return request;
	}
	
	protected BaseJob(String jobMsg) {
		super();
		try {
			this.request = JSONUtil.getJsonForString(jobMsg);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public String run() {
		String name = this.getClass().getName();
		String jobSeq = request.get("jobSeq").toString();
		try {
			try {
				logger.info("[jobSeq : "+jobSeq+"]" + name + " start ========");
				request.put("jobState", JOB_STATE.RUN.getValue());
				_doWork();
				logger.info("[jobSeq : "+jobSeq+"]" + name + " End ========");
				request.put("jobState", JOB_STATE.COMPLETE.getValue());
				request.put("jobResult", JOB_RESULT.COMPLETE.getValue());
				return success();
			} catch (BaseJobException e) {
				request.put("jobState", JOB_STATE.COMPLETE.getValue());
				request.put("jobResult", JOB_RESULT.ERROR.getValue());
				return failed(e);
			}
		} catch (Exception e) {
			logger.info("[jobSeq : "+jobSeq+"]" + name + " Exception ========", e);
			String json = "{\"jobSeq\":\""+jobSeq+"\""
					+ ", \"jobState\":\"" + JOB_STATE.COMPLETE.getValue() + "\""
					+ ", \"jobResult\":\"" + JOB_RESULT.ERROR.getValue() + "\""
					+ ", \"code\":\"" + ERROR_CODE.F9999.getCode() + "\""
					+ ", \"errorMsg\":\"" + e.getMessage() + "\""
							+ "}"; 
			return json ;
		} 
	}
	
	protected String success() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		request.put("code", ERROR_CODE.S000.getCode());
		request.put("errorMsg", ERROR_CODE.S000.getMessage());
		return mapper.writeValueAsString(request);
	}
	
	protected String failed(BaseJobException e) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ERROR_CODE code = e.getErrorCode();
		request.put("code", code.getCode());
		request.put("errorMsg", e.getErrorMessage());
		return mapper.writeValueAsString(request);
	}
	
	protected abstract void _doWork() throws BaseJobException;

}
