package site.okkul.be.domain.practice.mapper;

import org.springframework.stereotype.Component;
import site.okkul.be.domain.practice.dto.request.PracticeFeedbackRequest;
import site.okkul.be.domain.practice.entity.Practice;
import site.okkul.be.domain.practice.entity.PracticeAnswer;
import site.okkul.be.domain.question.entity.Question;

@Component
public class PracticeAnswerMapper {

    /**
     * PracticeFeedbackRequest, audioUrl (Dto) & Practice, Question (Entity) -> PracticeAnswer (Entity)
     * @param request
     * @param audioUrl
     * @param practice
     * @param question
     * @return
     */
    public PracticeAnswer toEntity(PracticeFeedbackRequest request, String audioUrl, Practice practice, Question question) {
        return PracticeAnswer.builder()
                .koreanScript(request.getKoreanScript())
                .englishScript(request.getEnglishScript())
                .englishRecordUrl(audioUrl)
                .practice(practice)
                .question(question)
                .questionSet(question.getQuestionSet())
                .build();
    }

}
