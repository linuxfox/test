package kr.co.esjee.cloud.listener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import kr.co.esjee.cloud.constant.BaseConstant;
import kr.co.esjee.cloud.manager.ComponentManager;
import kr.co.esjee.cloud.util.JSONUtil;

/**
 * @Description 컴포넌트 정보 리스너
 * @author Cho Oh-jung (2016. 6. 21.)
 */
@Service(value = "info")
public class ServerPoolListener implements MessageListener {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("#{common['component.type']}")
	private String componentType;

	@Value("#{common['component.ip']}")
	private String componentIp;

	@Value("#{common['component.port']}")
	private String componentPort;

	@Autowired
	private ComponentManager componentManager;

	@Override
	public void onMessage(Message message) {
		logger.debug("===== ServerPoolListener Start =====");
		
		try {
			String key = String.format("%s.%s.%s", new Object[] { componentType, componentIp, componentPort });
			String mgs = new String(message.getBody(), BaseConstant.MESSAGE_ENCODING);
			
			logger.debug("===== ServerPoolListener key : " + key);
			logger.debug("===== ServerPoolListener mgs : " + mgs);
			
			JSONObject poolInfoJson = JSONUtil.getJsonForString(mgs);
			JSONObject poolInfo = (JSONObject) poolInfoJson.get(key);

			if (poolInfo != null && !poolInfo.isEmpty()) {
				logger.debug("===== ServerPoolListener poolInfo ===== :" + poolInfo);
				componentManager.setQueueList((Object[]) ((JSONArray) poolInfo.get("queueList")).toArray());
			}
			
			componentManager.sendComponentStatus();
		} catch (Exception e) {
			logger.error("===== ServerPoolListener error =====", e);
		}

		logger.debug("===== ServerPoolListener End =====");
	}
}
