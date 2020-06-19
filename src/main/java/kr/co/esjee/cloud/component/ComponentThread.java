package kr.co.esjee.cloud.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.esjee.cloud.constant.BaseConstant;
import kr.co.esjee.cloud.job.diva.ArchiveManager;
import kr.co.esjee.cloud.job.quantum.QuantumArchiveManager;
import kr.co.esjee.cloud.manager.ComponentManager;

/**
 * @Description 컴포넌트 스레드
 * @author Cho Oh-jung (2016. 6. 23.)
 */
public class ComponentThread implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 대기 시간
	private final int SLEEP_TIME = 1 * 1000;
	private ObjectMapper mapper = new ObjectMapper();

	private ComponentManager componentManager;

	// 스레드풀
	private ExecutorService executorService = Executors.newFixedThreadPool(4);

	public ComponentThread(ComponentManager componentManager) {
		this.componentManager = componentManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		logger.debug("===== ComponentThread run =====");
		Component component;
		String jobParam;

		while (true) {
			try {
				jobParam = componentManager.getJob();
				
				if(componentManager.getComponentType().equals(BaseConstant.COMPONENT_TYPE_1090))
				{					
					if (jobParam != null) {
						QuantumArchiveManager.Instance().run(jobParam, componentManager);
					}
					Thread.sleep(SLEEP_TIME);
					
				} else {
					if (jobParam != null) {
						component = new Component(componentManager, jobParam, (Map<String, Object>) mapper.readValue(jobParam, Map.class));
						// 작업 실행
						executorService.execute(component);
					}
					Thread.sleep(SLEEP_TIME);
				}
				
			} catch (Exception e) {
				componentManager.restProcess();
				logger.error("===== ComponentThread error =====", e);
			} finally {
				if (!componentManager.isEmptyForMessageMap()) {
					HashMap<String, HashMap<String, String>> messageMap = componentManager.getMessageMap();

					String key;

					for (Iterator<String> iterator = messageMap.keySet().iterator(); iterator.hasNext();) {
						key = iterator.next();

						componentManager.convertAndSend(messageMap.get(key).get("queueName"), messageMap.get(key).get("message"),
								"T".equals(messageMap.get(key).get("isRest")) ? true : false);
						componentManager.removeMessageMap(key);
					}
				}
			}
		}
	}

}
