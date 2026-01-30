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
  GetPracticeFeedbackData,
  GetPracticeProblemData,
  SavePracticeSessionData,
  SavePracticeSessionPayload,
  StartPracticeData,
} from "./data-contracts";
import { ContentType, HttpClient, RequestParams } from "./http-client";

export class Practices<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 사용자가 유형별 연습 모드를 새로 시작할 때마다 해당 API를 요청합니다.사용자가 선택한 소재ID, 설문조사ID를 바탕으로 랜덤으로 문제 세트를 가져오고,유형별 연습 모드 데이터를 생성해 ID를 반환합니다.
   *
   * @tags Practice
   * @name StartPractice
   * @summary 유형별 연습 모드 생성 API
   * @request POST:/practices
   * @secure
   */
  startPractice = (
    query: {
      /**
       * 설문조사 ID
       * @format int64
       * @example 1
       */
      surveyId: number;
      /**
       * 소재 ID
       * @format int64
       * @example 101
       */
      topicId: number;
      /**
       * 문제 유형 ID
       * @format int64
       * @example 3
       */
      typeId: number;
    },
    params: RequestParams = {},
  ) =>
    this.request<StartPracticeData, any>({
      path: `/practices`,
      method: "POST",
      query: query,
      secure: true,
      ...params,
    });
  /**
   * @description 유형 연습 모드에서 특정 문제에 대해서사용자의 답변(한/영 스크립트, 녹음 파일)을 받아AI서버를 통해 AI 피드백을 받아옵니다.해당 데이터를 DB에 저장합니다.
   *
   * @tags Practice
   * @name SavePracticeSession
   * @summary 유형별 연습 피드백 생성 요청 API
   * @request POST:/practices/{practiceId}/feedback
   * @secure
   */
  savePracticeSession = (
    practiceId: number,
    data: SavePracticeSessionPayload,
    params: RequestParams = {},
  ) =>
    this.request<SavePracticeSessionData, any>({
      path: `/practices/${practiceId}/feedback`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.FormData,
      ...params,
    });
  /**
   * @description 해당 유형별 연습 모드에서 할당된 세트와 하위 문제의 상세 정보를 반환합니다.
   *
   * @tags Practice
   * @name GetPracticeProblem
   * @summary 유형별 연습 모드 문제 조회 API
   * @request GET:/practices/{practiceId}
   * @secure
   */
  getPracticeProblem = (practiceId: number, params: RequestParams = {}) =>
    this.request<GetPracticeProblemData, any>({
      path: `/practices/${practiceId}`,
      method: "GET",
      secure: true,
      ...params,
    });
  /**
   * @description 피드백 결과를 조회합니다.AI 피드백이 아직 처리중이라면 '처리중'을 의미하는 PROCESSINGAI 피드백이 생성완료된 상태라면 '완료'를 의미하는 COMPLETEDAI 피드백 생성이 실패되었다면 '실패'를 의미하는 FAILED가 응답됩니다.프론트엔드는 PROCESSING 중일 경우, 로딩 표시를 화면에 나타내고,COMPLETED를 응답받을 경우 피드백 결과를 화면에 표시하고,FAILED를 응답받을 경우 '피드백 생성이 실패했습니다'라고 사용자에게 표시합니다.이처럼 피드백 저장 상태에 따라서 분기처리하여 화면에 표시합니다.
   *
   * @tags Practice
   * @name GetPracticeFeedback
   * @summary 유형별 연습 피드백 결과 조회 API
   * @request GET:/practices/feedback/{practiceAnswerId}
   * @secure
   */
  getPracticeFeedback = (
    practiceAnswerId: number,
    params: RequestParams = {},
  ) =>
    this.request<GetPracticeFeedbackData, any>({
      path: `/practices/feedback/${practiceAnswerId}`,
      method: "GET",
      secure: true,
      ...params,
    });
}
