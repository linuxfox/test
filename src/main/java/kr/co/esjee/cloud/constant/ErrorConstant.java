package kr.co.esjee.cloud.constant;

/**
 * @Description 에러 상수
 * @author Cho Oh-jung (2016. 7. 14.)
 */
public interface ErrorConstant {
	public enum ERROR_CODE {
		S000("성공", "0000")
		, F001("(실패) 입력 파일 없음", "0001")
		, F002("(실패) 입력 파일 시작 시간 초과", "0002")
		, F003("(실패) 입력 파일 종료 시간 초과", "0003")
		, F004("(실패) 용량 부족", "0004")
		, F005("(실패) 패러미터 이상", "0005")
		, F006("(실패) job id 이상", "0006")
		, F007("(실패) 입력 파일 없음", "0007")
		, F008("(실패) merge 실패", "0008")
		, F009("(실패) json 파일 이상", "0009")
		, F010("(실패) 시스템 리소스 부족", "0010")
		, F011("(실패) request 패킷 예상", "0011")
		, F012("(실패) state 패킷 예상", "0012")
		, F013("(실패) invalid input file", "0013")
		, F014("(실패) 오디오 포맷 불일치", "0014")
		, F0421("(실패) 서비스 이용 장애 (FTP 서버의 이용 장애 오류)", "0421")
		, F0425("(실패) 연결 불가능 (FTP 서버에 연결 불가능)", "0425")
		, F0434("(실패) 알 수 없는 서버 이름 (FTP 서버 이름 입력 오류)", "0434")
		, F0530("(실패) 로그인 정보 오류 (FTP 로그인 정보 오류)", "0530")
		, F0556("(실패) 롤백 오류", "0556")
		, F0701("(실패) MQ 커넥션 오류", "0701")
		, F0702("(실패) MQ 커넥션 시간 초과", "0702");

		private final String message;
		private final String code;

		private ERROR_CODE(String message, String code) {
			this.message = message;
			this.code = code;
		}

		public String getMessage() {
			return message;
		}

		/**
		 * @Description 에러 메세지 반환
		 * @author Cho Oh-jung (2016. 7. 14.)
		 * @param code
		 * @return
		 */
		static public String getMessage(String code) {
			for (ERROR_CODE errorCode : ERROR_CODE.values()) {
				if (errorCode.getCode().equals(code))
					return errorCode.getMessage();
			}

			return null;
		}

		public String getCode() {
			return code;
		}

		/**
		 * @Description 성공 여부
		 * @author Cho Oh-jung (2016. 7. 14.)
		 * @param code
		 * @return
		 */
		static public boolean isSuccess(String code) {
			return S000.getCode().equals(code);
		}
	}
}
