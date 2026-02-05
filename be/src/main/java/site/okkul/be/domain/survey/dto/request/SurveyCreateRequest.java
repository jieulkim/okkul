package site.okkul.be.domain.survey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "설문조사 생성 요청 DTO")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyCreateRequest {

    /** ============== Part 1 of 4 =================**/

    @Schema(description = "직군(현재 귀하는 어느 분야에 종사하고 계십니까?)", example = "1")
    private Integer occupationAnswerId;

    @Schema(description = "직업 유무(귀하는 직업이 있으십니까?)", example = "true")
    private Boolean hasJob;

    @Schema(description = "근무 기간(귀하의 근무 기간은 얼마나 되십니까?)", example = "1")
    private Integer workPeriodAnswerId;

    @Schema(description = "교육 분야(현재 귀하는 어디에서 학생을 가르치십니까?)", example = "null")
    private Integer teachAnswerId;

    @Schema(description = "관리직 여부(귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?)", example = "false")
    private Boolean manager;

    /** ============== Part 2 of 4 =================**/

    @Schema(description = "학생 여부(당신은 학생입니까?)", example = "false")
    private Boolean student;

    @Schema(description = "수강 종류(최근 어떤 강의를 수강했습니까?)", example = "null")
    private Integer classTypeAnswerId;

    /** ============== Part 3 of 4 =================**/

    @Schema(description = "거주지(현재 당신은 어디에 살고 계십니까?)", example = "3")
    private Integer residenceAnswerId;

    /** ============== Part 4 of 4 =================**/

    @Schema(description = "여가 활동 토픽 ID 목록", example = "[101, 102]")
    private List<Long> leisure;

    @Schema(description = "취미나 관심사 토픽 ID 목록", example = "[201, 202, 203]")
    private List<Long> hobby;

    @Schema(description = "운동 토픽 ID 목록", example = "[301, 302]")
    private List<Long> exercise;

    @Schema(description = "휴가나 출장 토픽 ID 목록", example = "[401, 402, 403, 404, 405]")
    private List<Long> holiday;

    @Schema(description = "자가진단 레벨 (1~6)", example = "4")
    private Integer level;
}
