package kr.co.esjee.cloud.constant;

import com.fpdigital.diva.api.ApiMessagingConstants;
import com.fpdigital.diva.api.DivaErrorType;
import com.fpdigital.diva.api.QOS;
import com.fpdigital.diva.api.RequestStatus;

public interface ArchiveConstant {

	public static final String REQUEST_ARCHIVE = "01";  
	public static final String REQUEST_RESTORE = "10";
	public static final String REQUEST_PARTIAL_RESTORE = "11";
	
	public enum REQUEST_TYPE {
		ARCHIVE(REQUEST_ARCHIVE, "아카이브")
		, RESTORE(REQUEST_RESTORE, "리스토어")
		, PARTIAL_RESTORE(REQUEST_PARTIAL_RESTORE, "부분 리스토어");

		private final String code;
		private final String text;

		private REQUEST_TYPE(String code, String text) {
			this.code = code;
			this.text = text;
		}
		
		public String getCode() {
			return code;
		}

		public String getText() {
			return text;
		}
	}
	
	public enum SUBMIT_RESULT {
		Success(0,"")
		, ConnectionFailed(1,"Diva connection 실패!");
		
		private int code;
		private String text;
		
		private SUBMIT_RESULT(int code, String text) {
			this.code = code;
			this.text = text;
		}

		public int getCode() {
			return code;
		}

		public String getText() {
			return text;
		}
	}

	public enum STATUS {
		
//		  public static final int REQ_STATUS_UNKNOWN = -1;
//		  public static final int REQ_STATUS_NOT_SUBMITTED = 0;
//		  public static final int REQ_STATUS_SUBMITTED = 1;
//		  public static final int REQ_STATUS_RUNNING = 2;
//		  public static final int REQ_STATUS_REJECTED = 3;
//		  public static final int REQ_STATUS_CANCELLED = 4;
//		  public static final int REQ_STATUS_ABORTED = 5;
//		  public static final int REQ_STATUS_PARTIALLY_ABORTED = 6;
//		  public static final int REQ_STATUS_COMPLETED = 7;
		
		  Failed(null, 										"99", "")
		, ExcetionFailed(null, 								"98", "Excetion")
		, ConnectionFailed(null, 							"97", "Diva 커넥션 실패")
		, Unknown(RequestStatus.Unknown, 					"89", "[Unknown] 알수 없는 요청")
		, NotSubmitted(RequestStatus.NotSubmitted, 			"88", "[NotSubmitted] 요청 실패")
		, Rejected(RequestStatus.Rejected,  				"87", "[Rejected] 거부")
		, Cancelled(RequestStatus.Cancelled,  				"86", "[Cancelled] 취소")
		, Aborted(RequestStatus.Aborted,  					"85", "[Aborted] 중단")
		, PartiallyAborted(RequestStatus.PartiallyAborted,  "84", "[PartiallyAborted] 부분 중단")
		, Submitted(RequestStatus.Submitted,  				"02", "[Submitted] 요청")
		, Running(RequestStatus.Running,  					"03", "[Running] 실행중")
		, Completed(RequestStatus.Completed,  				"10", "[Completed] 완료");

		private final RequestStatus status;
		private final String code;
		private final String text;

		private STATUS(RequestStatus status, String code, String text) {
			this.status = status;
			this.code = code;
			this.text = text;
		}

		public RequestStatus getStatus() {
			return status;
		}
		
		public String getCode() {
			return code;
		}

		public String getText() {
			return text;
		}
		
		public static String getCode(RequestStatus state) {
			for(STATUS res : STATUS.values())
			{
				if(state.equals(res.getStatus()))
				{
					return res.getCode();
				}
			}
			return "99";
		}
	}
	
	public enum ERROR_CODE {
		API_ERR_INTERNAL                              (DivaErrorType.API_ERR_INTERNAL                                ,"99", "API에서 내부 오류가 발생했습니다."),
		API_ERR_TERMINATED                            (DivaErrorType.API_ERR_TERMINATED                              ,"98", "요청 또는 명령이 취소되었습니다."),
		DIVA_AR_ACTOR                                 (DivaErrorType.DIVA_AR_ACTOR                                   ,"97", "액터의 문제로 인해 요청이 중단되었습니다."),
		DIVA_AR_DISK                                  (DivaErrorType.DIVA_AR_DISK                                    ,"96", "요청을 디스크에 액세스하는 문제로 인해 중단되었습니다."),
		DIVA_AR_DISK_FULL                             (DivaErrorType.DIVA_AR_DISK_FULL                               ,"95", "디스크가 꽉 찼기 때문에 요청이 중단되었습니다."),
		DIVA_AR_DRIVE                                 (DivaErrorType.DIVA_AR_DRIVE                                   ,"94", "요청이 테이프 드라이브에 액세스하는 중 문제로 인해 중단되었습니다."),
		DIVA_AR_INTERNAL                              (DivaErrorType.DIVA_AR_INTERNAL                                ,"93", "요청이 Manager의 내부 오류로 인해 중단되었습니다."),
		DIVA_AR_LIBRARY                               (DivaErrorType.DIVA_AR_LIBRARY                                 ,"92", "요청이 라이브러리의 문제로 인해 중단되었습니다."),
		DIVA_AR_NONE                                  (DivaErrorType.DIVA_AR_NONE                                    ,"91", "성공적으로 완료되었습니다."),
		DIVA_AR_PARAMETERS                            (DivaErrorType.DIVA_AR_PARAMETERS                              ,"89", "잘못된 매개 변수로 인해 요청이 중단되었습니다."),
		DIVA_AR_RESOURCES                             (DivaErrorType.DIVA_AR_RESOURCES                               ,"88", "리소스 할당 문제로 인해 요청이 중단되었습니다."),
		DIVA_AR_SOURCE_DEST                           (DivaErrorType.DIVA_AR_SOURCE_DEST                             ,"87", "요청은 소스 / 대상에 액세스하는 문제로 인해 중단되었습니다."),
		DIVA_AR_TAPE                                  (DivaErrorType.DIVA_AR_TAPE                                    ,"86", "테이프 액세스 문제로 인해 요청이 중단되었습니다."),
		DIVA_AR_UNKNOWN                               (DivaErrorType.DIVA_AR_UNKNOWN                                 ,"85", "오류 코드는 Arbortion 코드가 아닙니다."),
		DIVA_ERR_CANNOT_ACCEPT_MORE_REQUESTS          (DivaErrorType.DIVA_ERR_CANNOT_ACCEPT_MORE_REQUESTS            ,"84", "Manager에서 최대 동시 요청 수에 도달했습니다. 이 값은 manger.conf에 설정됩니다. 기본값은 300입니다."),
		DIVA_ERR_DATABASE_NOT_AVAILABLE               (DivaErrorType.DIVA_ERR_DATABASE_NOT_AVAILABLE                 ,"83", "Manager가 데이터베이스에 액세스 할 수 없습니다."),
		DIVA_ERR_GROUP_ALREADY_EXISTS                 (DivaErrorType.DIVA_ERR_GROUP_ALREADY_EXISTS                   ,"82", "새 그룹을 추가하려고하면 이름이 이미 있습니다."),
		DIVA_ERR_GROUP_IN_USE                         (DivaErrorType.DIVA_ERR_GROUP_IN_USE                           ,"81", "하나 이상의 오브젝트 인스턴스를 포함하는 테이프 그룹을 제거하려고 시도하십시오."),
		DIVA_ERR_INSTANCE_DOESNT_EXIST                (DivaErrorType.DIVA_ERR_INSTANCE_DOESNT_EXIST                  ,"80", "지정된 오브젝트 인스턴스가 데이터베이스에 존재하지 않습니다."),
		DIVA_ERR_INSTANCE_MUST_BE_ON_TAPE             (DivaErrorType.DIVA_ERR_INSTANCE_MUST_BE_ON_TAPE               ,"79", "지정된 오브젝트 인스턴스가 테이프에 없습니다."),
		DIVA_ERR_INSTANCE_OFFLINE                     (DivaErrorType.DIVA_ERR_INSTANCE_OFFLINE                       ,"78", "지정된 오브젝트 인스턴스가 외부 테이프에 있거나 OFFLINE 인 디스크에 있습니다."),                                                            
		DIVA_ERR_INTERNAL                             (DivaErrorType.DIVA_ERR_INTERNAL                               ,"77", "관리자 내부 오류가 발생했습니다."),
		DIVA_ERR_INVALID_INSTANCE_TYPE                (DivaErrorType.DIVA_ERR_INVALID_INSTANCE_TYPE                  ,"76", "유효하지 않은 인스턴스 유형으로 부분 복원이 지정되었습니다."),
		DIVA_ERR_INVALID_PARAMETER                    (DivaErrorType.DIVA_ERR_INVALID_PARAMETER                      ,"75", "매개 변수 값이 범위를 벗어 났거나 Manager에서 이해할 수 없습니다."),
		DIVA_ERR_LAST_INSTANCE                        (DivaErrorType.DIVA_ERR_LAST_INSTANCE                          ,"74", "DeleteInstanceRequest를 사용하여 개체의 마지막 인스턴스를 삭제하려고합니다. DeleteRequest를 사용하여 객체의 모든 인스턴스를 제거합니다."),
		DIVA_ERR_LICENSE_DOES_NOT_SUPPORT_THIS_FEATURE(DivaErrorType.DIVA_ERR_LICENSE_DOES_NOT_SUPPORT_THIS_FEATURE  ,"73", "시스템 라이센스에서 지원하지 않는 기능에 액세스하려고했습니다."),
		DIVA_ERR_MEDIA_DOESNT_EXIST                   (DivaErrorType.DIVA_ERR_MEDIA_DOESNT_EXIST                     ,"72", "지정된 테이프 그룹 또는 디스크 어레이가 없습니다."),
		DIVA_ERR_NO_INSTANCE_TAPE_EXIST               (DivaErrorType.DIVA_ERR_NO_INSTANCE_TAPE_EXIST                 ,"71", "지정된 오브젝트에는 테이프 인스턴스가 없습니다."),
		DIVA_ERR_NO_SUCH_REQUEST                      (DivaErrorType.DIVA_ERR_NO_SUCH_REQUEST                        ,"70", "요청 ID가 없습니다."),
		DIVA_ERR_NOT_CANCELABLE                       (DivaErrorType.DIVA_ERR_NOT_CANCELABLE                         ,"69", "요청을 취소 할 수 없습니다."),
        DIVA_ERR_OBJECT_ALREADY_EXISTS                (DivaErrorType.DIVA_ERR_OBJECT_ALREADY_EXISTS                  ,"68", "지정된 이름 및 범주를 가진 개체가 이미 아카이브에 있습니다."),
        DIVA_ERR_OBJECT_DOESNT_EXIST                  (DivaErrorType.DIVA_ERR_OBJECT_DOESNT_EXIST                    ,"67", "지정된 오브젝트가 아카이브에 없습니다."),
        DIVA_ERR_OBJECT_IN_USE                        (DivaErrorType.DIVA_ERR_OBJECT_IN_USE                          ,"66", "개체가 현재 다른 요청에 사용 중입니다."),
        DIVA_ERR_OBJECT_IS_LOCKED                     (DivaErrorType.DIVA_ERR_OBJECT_IS_LOCKED                       ,"65", "잠긴 개체에 대해 잘못된 연산을 시도했습니다. 잠긴 개체는 새 개체로 복원하거나 복사 할 수 없습니다."),
        DIVA_ERR_OBJECT_OFFLINE                       (DivaErrorType.DIVA_ERR_OBJECT_OFFLINE                         ,"64", "삽입 된 테이프 또는 온라인 디스크에 지정된 오브젝트의 인스턴스가 들어 있지 않습니다."),
        DIVA_ERR_OBJECT_PARTIALLY_DELETED             (DivaErrorType.DIVA_ERR_OBJECT_PARTIALLY_DELETED               ,"63", "지정된 개체에 부분적으로 삭제 된 인스턴스가 포함되어 있습니다."),
        DIVA_ERR_SEVERAL_OBJECTS                      (DivaErrorType.DIVA_ERR_SEVERAL_OBJECTS                        ,"62", "범주가 지정되지 않았으며 둘 이상의 개체가 지정된 이름으로 존재합니다."),
        DIVA_ERR_SOURCE_OR_DESTINATION_DOESNT_EXIST   (DivaErrorType.DIVA_ERR_SOURCE_OR_DESTINATION_DOESNT_EXIST     ,"61", "DIVArchive는 지정된 소스 / 대상을 알 수 없습니다."),
        DIVA_ERR_SYSTEM_IDLE                          (DivaErrorType.DIVA_ERR_SYSTEM_IDLE                            ,"60", "DIVArchive 시스템은 더 이상 연결 및 쿼리를 허용 할 수 없습니다."),
        DIVA_ERR_TAPE_DOESNT_EXIST                    (DivaErrorType.DIVA_ERR_TAPE_DOESNT_EXIST                      ,"59", "getTapeInfo 명령에 존재하지 않는 바코드가 포함되어 있습니다."),
        DIVA_ERR_TIMEOUT                              (DivaErrorType.DIVA_ERR_TIMEOUT                                ,"58", "Manager와의 통신이 완료되기 전에 제한 시간 한도에 도달했습니다. 제한 시간은 SessionPolicy에서 설정됩니다."),
        DIVA_ERR_UNKNOWN                              (DivaErrorType.DIVA_ERR_UNKNOWN                                ,"57", "관리자가 알 수없는 상태를 수신했습니다."),
        DIVA_ERR_WRONG_VERSION                        (DivaErrorType.DIVA_ERR_WRONG_VERSION                          ,"56", "API 버전은 DIVArchive Manager와 호환되지 않습니다."),
        DIVA_OK                                       (DivaErrorType.DIVA_OK                                         ,"55", "성공적으로 완료되었습니다."),
        DIVA_WARN_NO_MORE_OBJECTS                     (DivaErrorType.DIVA_WARN_NO_MORE_OBJECTS                       ,"54", "목록의 끝에 도달했습니다."),
        NO_SUCH_REQUEST                               (DivaErrorType.NO_SUCH_REQUEST                                 ,"53", "지정된 요청 ID가 없습니다."),
		
		
		DIVA_CONNECTION_FAILED			              (-1 ,"00", "Diva 커넥션 실패");                                                                                                       

		private final int divaApiErrorCode;
		private final String code;
		private final String message;
		
		private ERROR_CODE(int divaApiErrorCode, String code, String message) {
			this.divaApiErrorCode = divaApiErrorCode;
			this.code = code;
			this.message = message;
		}

		public int getDivaApiErrorCode() {
			return divaApiErrorCode;
		}

		public String getCode() {
			return code;
		}

		public static String getCode(int divaErrorTypeCode) {
			for (ERROR_CODE e : ERROR_CODE.values()) {
				if (e.getDivaApiErrorCode() == divaErrorTypeCode) {
					return e.getCode();
				}
			}
			return "";
		}

		public String getMessage() {
			return message;
		}
		
		public static String getMessage(String code) {
			for (ERROR_CODE e : ERROR_CODE.values()) {
				if (e.getCode().equals(code)) {
					return e.getMessage();
				}
			}
			return "";
		}
	}
	
	public enum OUALITY_OF_SERVICE {
		Default("1", QOS.Default)
		, CacheOnly("2", QOS.CacheOnly)
		, DirectOnly("3", QOS.DirectOnly)
		, DirectAndCache("4", QOS.DirectAndCache)
		, CacheAndDirect("5", QOS.CacheAndDirect)
		, NearlineOnly("6", QOS.NearlineOnly)
		, NearlineAndDirect("7", QOS.NearlineAndDirect);

		private String code;
		private QOS value;

		private OUALITY_OF_SERVICE(String code, QOS value) {
			this.code = code;
			this.value = value;
		}

		public String getCode() {
			return code;
		}

		public QOS getValue() {
			return value;
		}

		public static String getCode(QOS value) {
			for (OUALITY_OF_SERVICE qos : OUALITY_OF_SERVICE.values()) {
				if (value.equals(qos.getValue())) {
					return qos.getCode();
				}
			}
			return getCode(QOS.Default);
		}

		public static QOS getValue(String code) {
			for (OUALITY_OF_SERVICE qos : OUALITY_OF_SERVICE.values()) {
				if (code.equals(qos.getCode())) {
					return qos.getValue();
				}
			}
			return getValue("1");
		}
	}
	
	public enum RESTORE_SERVICE {
		Default(ApiMessagingConstants.SERVICES_DEFAULT,"Default")
		,DoNotOverwrite(ApiMessagingConstants.SERVICES_DO_NOT_OVERWRITE,"Do Not Overwrite")
		,DoNotCheckExistence(ApiMessagingConstants.SERVICES_DO_NOT_CHECK_EXISTENCE,"Do Not Check Existence")
		,DeleteAndWrite(ApiMessagingConstants.SERVICES_DELETE_AND_WRITE,"Delete And Write");

		private int code;
		private String text;

		private RESTORE_SERVICE(int code, String text) {
			this.code = code;
			this.text = text;
		}

		public int getCode() {
			return code;
		}

		public String getText() {
			return text;
		}
	}
	
	
	
	
	
	
}
