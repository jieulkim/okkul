package site.okkul.be.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "스크립트 내 교정 문장 정보")
public class ScriptCorrection {

    @Schema(description = "문장 교정 내용")
    private List<SentenceCorrection> sentenceCorrections;

}

