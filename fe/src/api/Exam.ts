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
  ExamCreateRequest,
  ExamQuestionAnswerRequest,
  GetExamInfoData,
  StartExamData,
  SubmitAnswerData,
  UpdateAdjustedDifficultyData,
} from "./data-contracts";
import { ContentType, HttpClient, RequestParams } from "./http-client";

export class Exam<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 시험 세션을 생성하고 초기 문항(1~7번)을 반환합니다.
   *
   * @tags Exam
   * @name StartExam
   * @summary 모의고사 생성
   * @request POST:/exam
   * @secure
   */
  startExam = (data: ExamCreateRequest, params: RequestParams = {}) =>
    this.request<StartExamData, any>({
      path: `/exam`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
  /**
   * @description 모든 답변 제출을 마치고 시험을 종료합니다. 이때 AI 분석이 시작됩니다.<br>이미 종료된 시험이라면 400 예외를 터트립니다 (AI 중복 리포팅 생성 방지)
   *
   * @tags Exam
   * @name CompleteExam
   * @summary 시험 최종 종료
   * @request POST:/exam/{examId}/complete
   * @secure
   */
  completeExam = (examId: number, params: RequestParams = {}) =>
    this.request<CompleteExamData, any>({
      path: `/exam/${examId}/complete`,
      method: "POST",
      secure: true,
      ...params,
    });
  /**
   * @description 사용자의 음성 녹음 파일(mp3/m4a 등)을 서버로 업로드합니다.
   *
   * @tags Exam
   * @name SubmitAnswer
   * @summary 답변 제출
   * @request POST:/exam/{examId}/answers/{questionOrder}
   * @secure
   */
  submitAnswer = (
    examId: number,
    questionOrder: number,
    data: ExamQuestionAnswerRequest,
    params: RequestParams = {},
  ) =>
    this.request<SubmitAnswerData, any>({
      path: `/exam/${examId}/answers/${questionOrder}`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.FormData,
      ...params,
    });
  /**
   * @description 난이도 재조정 포인트(7번 이후)에서 난이도를 조정하고 나머지 문항 리스트를 가져옵니다.
   *
   * @tags Exam
   * @name UpdateAdjustedDifficulty
   * @summary 난이도 조정 및 나머지 문제 생성
   * @request PATCH:/exam/{examId}/adjust-level
   * @secure
   */
  updateAdjustedDifficulty = (
    examId: number,
    query: {
      /**
       * 시험 난이도 (new)
       * @format int32
       */
      adjustedDifficulty: number;
    },
    params: RequestParams = {},
  ) =>
    this.request<UpdateAdjustedDifficultyData, any>({
      path: `/exam/${examId}/adjust-level`,
      method: "PATCH",
      query: query,
      secure: true,
      ...params,
    });
  /**
   * @description 시험 정보를 조회합니다
   *
   * @tags Exam
   * @name GetExamInfo
   * @summary 시험 정보 조회(시험 상태 + 문제 Set + 문제들)
   * @request GET:/exam/{examId}
   * @secure
   */
  getExamInfo = (examId: number, params: RequestParams = {}) =>
    this.request<GetExamInfoData, any>({
      path: `/exam/${examId}`,
      method: "GET",
      secure: true,
      ...params,
    });
}
