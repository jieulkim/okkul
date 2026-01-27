/* eslint-disable */
/* tslint:disable */
// @ts-nocheck
/*
 * ---------------------------------------------------------------
 * ## THIS FILE WAS GENERATED VIA SWAGGER-TYPESCRIPT-API        ##
 * ##                                                           ##
 * ## AUTHOR: acacode                                           ##
 * ## SOURCE: https://github.com/acacode/swagger-typescript-api ##
 * ---------------------------------------------------------------
 */

import {
  CompleteExamData,
  ExamStartRequest,
  GetExamResultData,
  GetExamStatusData,
  GetRemainingQuestionsData,
  StartExamData,
  SubmitAnswerData,
  SubmitAnswerPayload,
} from "./data-contracts";
import { ContentType, HttpClient, RequestParams } from "./http-client";

export class Exam<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 난이도 재조정 포인트(7번 이후)에서 나머지 문항 리스트를 한꺼번에 가져옵니다.
   *
   * @tags Exam
   * @name GetRemainingQuestions
   * @summary 나머지 문항 조회
   * @request POST:/exam/{examId}/questions/current
   */
  getRemainingQuestions = (
    examId: number,
    query: {
      /** @format int32 */
      adjustedDifficulty: number;
    },
    params: RequestParams = {},
  ) =>
    this.request<GetRemainingQuestionsData, any>({
      path: `/exam/${examId}/questions/current`,
      method: "POST",
      query: query,
      ...params,
    });
  /**
   * @description 시험 세션을 생성하고 초기 문항(1~7번)을 반환합니다.
   *
   * @tags Exam
   * @name StartExam
   * @summary 모의고사 시작
   * @request POST:/exam/start
   */
  startExam = (data: ExamStartRequest, params: RequestParams = {}) =>
    this.request<StartExamData, any>({
      path: `/exam/start`,
      method: "POST",
      body: data,
      type: ContentType.Json,
      ...params,
    });
  /**
   * @description 사용자의 음성 녹음 파일(mp3/m4a 등)을 서버로 업로드합니다.
   *
   * @tags Exam
   * @name SubmitAnswer
   * @summary 음성 답변 제출
   * @request POST:/exam/exam/{examId}/questions/{answerId}/answer
   */
  submitAnswer = (
    examId: number,
    answerId: number,
    data: SubmitAnswerPayload,
    params: RequestParams = {},
  ) =>
    this.request<SubmitAnswerData, any>({
      path: `/exam/exam/${examId}/questions/${answerId}/answer`,
      method: "POST",
      body: data,
      type: ContentType.FormData,
      ...params,
    });
  /**
   * @description 모든 답변 제출을 마치고 시험을 종료합니다. 이때 AI 분석이 시작됩니다.
   *
   * @tags Exam
   * @name CompleteExam
   * @summary 시험 최종 종료
   * @request POST:/exam/exam/{examId}/complete
   */
  completeExam = (examId: number, params: RequestParams = {}) =>
    this.request<CompleteExamData, any>({
      path: `/exam/exam/${examId}/complete`,
      method: "POST",
      ...params,
    });
  /**
   * @description 결과 페이지 진입 전, AI 분석이 완료되었는지 확인(Polling)합니다.
   *
   * @tags Exam
   * @name GetExamStatus
   * @summary AI 분석 진행 상태 조회
   * @request GET:/exam/exam/{examId}/status
   */
  getExamStatus = (examId: number, params: RequestParams = {}) =>
    this.request<GetExamStatusData, any>({
      path: `/exam/exam/${examId}/status`,
      method: "GET",
      ...params,
    });
  /**
   * @description 전체 문항의 답변, STT 스크립트 및 AI 피드백 결과를 조회합니다.
   *
   * @tags Exam
   * @name GetExamResult
   * @summary 시험 결과 조회
   * @request GET:/exam/exam/{examId}/result
   */
  getExamResult = (examId: number, params: RequestParams = {}) =>
    this.request<GetExamResultData, any>({
      path: `/exam/exam/${examId}/result`,
      method: "GET",
      ...params,
    });
}
