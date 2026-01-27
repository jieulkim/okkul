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
       * @example 1
       */
      topicId: number;
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
   * @description 유형 연습 모드에서 특정 문제에 대해서사용자의 답변(한/영 스크립트, 녹음 파일)을 받아AI서버를 통해 AI 피드백을 받아옵니다.해당 데이터를 DB에 저장하고피드백 결과를 프론트엔드에게 전달합니다.
   *
   * @tags Practice
   * @name SavePracticeSession
   * @summary 유형별 연습 피드백 세션 저장 API
   * @request POST:/practices/{practiceId}/questions/{questionId}/feedback
   * @secure
   */
  savePracticeSession = (
    practiceId: number,
    questionId: number,
    data: SavePracticeSessionPayload,
    params: RequestParams = {},
  ) =>
    this.request<SavePracticeSessionData, any>({
      path: `/practices/${practiceId}/questions/${questionId}/feedback`,
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
}
