package kr.co.esjee.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class SJComponentApplication {
	public static void main(String[] args) throws Exception {
		//외부 프로퍼티 이용시 (win/*nix)
		//1. java -Dspring.config.location=file:///E:/component/config/ -jar component-transcoder.jar
		//2. java -Dspring.config.location=file:///component/config/ -jar component-transcoder.jar
		//기본 실행 java -Dspring.config.location=classpath: -jar sjComponent.jar
		
		//실행
		//java -jar component-transcoder.jar
		SpringApplication.run("classpath:/spring/context-rabbit.xml", args);
		
		//TODO 추후 properties 파일을 YAML로 변경해서 멀티 컴포넌트로 실행할 수 있도록 변경
	}
}
