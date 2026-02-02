package site.okkul.be.domain.practice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.okkul.be.domain.practice.dto.response.PracticeQuestionInfo;
import site.okkul.be.domain.practice.dto.response.PracticeQuestionResponse;
import site.okkul.be.domain.practice.entity.Practice;
import site.okkul.be.domain.practice.mapper.PracticeMapper;
import site.okkul.be.domain.practice.repository.PracticeJpaRepository;
import site.okkul.be.domain.question.entity.Question;
import site.okkul.be.domain.question.entity.QuestionSet;
import site.okkul.be.domain.question.repository.QuestionRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("test")
@ExtendWith(MockitoExtension.class)
class PracticeServiceUnitTest {

    @InjectMocks
    private PracticeService practiceService;

    @Mock
    private PracticeJpaRepository practiceJpaRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private PracticeMapper practiceMapper;

    @Test
    @DisplayName("유형별 연습 문제 조회 성공")
    void getPractice_Success() {
        // given: 테스트 데이터 설정
        long practiceId = 1L;
        long userId = 1L;
        long questionSetId = 10L;

        // Mock 객체 생성
        Practice mockPractice = mock(Practice.class);
        QuestionSet mockQuestionSet = mock(QuestionSet.class);
        List<Question> mockQuestions = Arrays.asList(mock(Question.class), mock(Question.class));
        List<PracticeQuestionInfo> mockPracticeQuestionInfos = Arrays.asList(new PracticeQuestionInfo(), new PracticeQuestionInfo());
        Set<Long> questionIds = mockQuestions.stream().map(Question::getId).collect(Collectors.toSet());

        // Repository 및 Mapper의 동작 정의
        when(practiceJpaRepository.findByPracticeIdAndUserId(practiceId, userId)).thenReturn(Optional.of(mockPractice));
        when(mockPractice.getQuestionIds()).thenReturn(questionIds);
        when(mockPractice.getQuestionSet()).thenReturn(mockQuestionSet);
        when(mockQuestionSet.getId()).thenReturn(questionSetId);
        when(questionRepository.findAllById(questionIds)).thenReturn(mockQuestions);

        // Mapper가 각 Question 객체를 PracticeQuestionInfo DTO로 변환하도록 설정
        for (int i = 0; i < mockQuestions.size(); i++) {
            when(practiceMapper.toPracticeQuestionInfo(mockQuestions.get(i))).thenReturn(mockPracticeQuestionInfos.get(i));
        }

        // when: 테스트할 메서드 실행
        PracticeQuestionResponse response = practiceService.getPractice(practiceId, userId);

        // then: 결과 검증
        assertThat(response).isNotNull();
        assertThat(response.getSetId()).isEqualTo(questionSetId);
        assertThat(response.getQuestions()).hasSize(mockQuestions.size());
        assertThat(response.getQuestions()).containsAll(mockPracticeQuestionInfos);

        // Mock 객체가 예상대로 호출되었는지 검증
        verify(practiceJpaRepository).findByPracticeIdAndUserId(practiceId, userId);
        verify(questionRepository).findAllById(questionIds);
        verify(practiceMapper, times(mockQuestions.size())).toPracticeQuestionInfo(any(Question.class));
    }

    @Test
    @DisplayName("유형별 연습 문제 조회 실패 - 존재하지 않는 연습 또는 접근 권한 없음")
    void getPractice_Fail_PracticeNotFound() {
        // given
        long practiceId = 99L;
        long userId = 99L;
        when(practiceJpaRepository.findByPracticeIdAndUserId(practiceId, userId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            practiceService.getPractice(practiceId, userId);
        });

        assertThat(exception.getMessage()).isEqualTo("Pratice not found or access denied");

        verify(questionRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("유형별 연습 문제 조회 성공 - 포함된 문제가 없는 경우")
    void getPractice_Success_NoQuestions() {
        // given
        long practiceId = 2L;
        long userId = 1L;

        Practice mockPractice = mock(Practice.class);
        when(practiceJpaRepository.findByPracticeIdAndUserId(practiceId, userId)).thenReturn(Optional.of(mockPractice));
        when(mockPractice.getQuestionIds()).thenReturn(Collections.emptySet()); // 문제가 없는 경우
        when(mockPractice.getQuestionSet()).thenReturn(null);


        // when
        PracticeQuestionResponse response = practiceService.getPractice(practiceId, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getSetId()).isNull();
        assertThat(response.getQuestions()).isNotNull();
        assertThat(response.getQuestions()).isEmpty();

        // questionRepository는 호출되지 않아야 함
        verify(questionRepository, never()).findAllById(any());
    }
}