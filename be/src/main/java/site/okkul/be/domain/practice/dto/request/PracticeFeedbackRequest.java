package site.okkul.be.domain.practice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "유형별 연습 피드백 요청 DTO")
public class PracticeFeedbackRequest {

    @Schema(description = "문제 ID", example = "7")
    private Long questionId;

    @Schema(description = "사용자 한국어 스크립트", example = "제가 가장 자주 가는 단골 바는 집 근처 골목에 숨겨진 '아지트'라는 작은 펍이에요. 이곳은 전체적으로 어두운 조명에 빈티지한 벽돌로 꾸며져 있어서, 들어서자마자 굉장히 아늑하고 편안한 느낌을 줘요. 특히 벽면 가득한 LP판에서 흘러나오는 재즈 음악과 은은한 위스키 향이 어우러져서, 하루의 피로를 풀며 친구들과 진솔한 대화를 나누기에 정말 최고의 장소입니다.")
    private String koreanScript;

    @NotBlank
    @Schema(description = "사용자 영어 스크립트", example = "My favorite place to grab a drink is a cozy, neighborhood bar called \\\"The Hideout\\\" located just a few blocks from my house. The interior is dimly lit with vintage brick walls and comfortable leather booths, creating a very warm and inviting atmosphere. It’s the perfect spot to unwind because the music is always at a perfect volume, allowing for great conversations with friends. Would you like me to adjust the vocabulary level or add more specific details to make it sound more like your personal style?")
    private String englishScript;
}
