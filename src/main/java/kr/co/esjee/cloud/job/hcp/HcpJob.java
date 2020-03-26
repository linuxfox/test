package kr.co.esjee.cloud.job.hcp;

import java.io.File;
import java.util.function.Function;

import kr.co.esjee.cloud.constant.ErrorConstant.ERROR_CODE;
import kr.co.esjee.cloud.job.BaseJob;
import kr.co.esjee.cloud.job.BaseJobException;
import kr.co.esjee.components.HcpBridge;
import kr.co.esjee.components.HcpException;

public class HcpJob extends BaseJob {

	private HcpServerInfo serverInfo = null;
	
	private Function<Double, Double> progressCallback = null;

	public void setProgressCallback(Function<Double, Double> progressCallback) {
		this.progressCallback = progressCallback;
	}

	public HcpJob(String jobMsg, HcpServerInfo serverInfo) {
		super(jobMsg);
		this.serverInfo = serverInfo;
	}
	
	private static final String REQUEST_TYPE_UPLOAD = "01";
	private static final String REQUEST_TYPE_DOWNLOAD = "02";
	private static final String REQUEST_TYPE_DELETE = "03";

	@Override
	protected void _doWork() throws BaseJobException {
		String requestType = request.get("requestType").toString();
		switch (requestType) {
		case REQUEST_TYPE_UPLOAD:
			upload();
			break;
		case REQUEST_TYPE_DOWNLOAD:
			download();
			break;
		case REQUEST_TYPE_DELETE:
			delete();
			break;
		default:
			throw new BaseJobException(ERROR_CODE.HCP_UndefinedRequestType);
		}
	}

	private void upload() throws BaseJobException {
		HcpBridge hcp = connect();
		String filePath = getFilePath();
		File file = new File(filePath);
		if(!file.exists() || !file.isFile()) {
			throw new BaseJobException(ERROR_CODE.F001);
		}
		try {
			logger.info("HCP Upload file : " + filePath);
			if(progressCallback != null) {
				hcp.S3UploadWithProgress(getHcpPath(), filePath, progressCallback);
			} else {
				hcp.S3Upload(getHcpPath(), filePath);
			}
		} catch (HcpException e) {
			throw new BaseJobException(e, ERROR_CODE.HCP_FailedUpload);
		}
	}
	
	private void download() throws BaseJobException {
		HcpBridge hcp = connect();
		String filePath = getFilePath();
		try {
			logger.info("HCP Download file : " + filePath);
			if(progressCallback != null) {
				hcp.S3DownloadWithProgress(getHcpPath(), getHcpFileName(), filePath, progressCallback);
			} else {
				hcp.S3Download(getHcpPath(), getHcpFileName(), filePath);
			}
		} catch (HcpException e) {
			throw new BaseJobException(e, ERROR_CODE.HCP_FailedDownload);
		}
	}
	
	private void delete() throws BaseJobException {
		HcpBridge hcp = connect();
		String filePath = getFilePath();
		try {
			logger.info("HCP Delete file : " + filePath);
			hcp.Delete(getHcpPath(), getHcpFileName());
		} catch (HcpException e) {
			throw new BaseJobException(e, ERROR_CODE.HCP_FailedDownload);
		}
	}
	
	private String getFilePath() throws BaseJobException {
		if(request.get("filePath") != null) {
			String filePath = request.get("filePath").toString();
			return filePath.replace("\\","/");
		}
		throw new BaseJobException(ERROR_CODE.F001);
	}

	private String getHcpPath() throws BaseJobException {
		String hcpPath = getFilePath();
		hcpPath = hcpPath.replaceFirst(serverInfo.getRootPath(), "");
		return hcpPath.substring(0, hcpPath.lastIndexOf("/"));
	}
	
	private String getHcpFileName() throws BaseJobException {
		String filePath = getFilePath();
		return filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
	}

	private HcpBridge connect() throws BaseJobException {
		HcpBridge hcp = null;
		try {
			hcp = new HcpBridge(serverInfo.getHost(), serverInfo.getUser(), serverInfo.getPassword());			
		} catch (Exception e) {
			throw new BaseJobException(e, ERROR_CODE.HCP_FailedConnect);
		}
		return hcp;
	}

}
