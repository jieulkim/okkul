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
  GetExamAnswerDetailData,
  GetExamHistoriesData,
  GetExamHistoryDetailData,
  GetPracticeHistoriesData,
  GetPracticeHistoryDetailData,
} from "./data-contracts";
import { HttpClient, RequestParams } from "./http-client";

export class History<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 사용자가 유형별로 연습한 기록 목록을 최신순으로 조회한다
   *
   * @tags History
   * @name GetPracticeHistories
   * @summary 유형별 연습 히스토리 목록 조회
   * @request GET:/history/practices
   * @secure
   */
  getPracticeHistories = (
    query?: {
      /**
       * Zero-based page index (0..N)
       * @min 0
       * @default 0
       */
      page?: number;
      /**
       * The size of the page to be returned
       * @min 1
       * @default 10
       */
      size?: number;
      /**
       * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
       * @default ["startedAt,DESC"]
       */
      sort?: string[];
    },
    params: RequestParams = {},
  ) =>
    this.request<GetPracticeHistoriesData, any>({
      path: `/history/practices`,
      method: "GET",
      query: query,
      secure: true,
      ...params,
    });
  /**
   * @description 특정 유형별 연습 세션(practiceId)의 모든 문단 사이클 데이터를 리스트로 반환한다.
   *
   * @tags History
   * @name GetPracticeHistoryDetail
   * @summary 유형별 연습 히스토리 상세 조회
   * @request GET:/history/practices/{practiceId}
   * @secure
   */
  getPracticeHistoryDetail = (practiceId: number, params: RequestParams = {}) =>
    this.request<GetPracticeHistoryDetailData, any>({
      path: `/history/practices/${practiceId}`,
      method: "GET",
      secure: true,
      ...params,
    });
  /**
   * @description 사용자가 응시한 모의고사 기록 목록을 최신순으로 조회한다
   *
   * @tags History
   * @name GetExamHistories
   * @summary 모의고사 히스토리 목록 조회
   * @request GET:/history/exams
   * @secure
   */
  getExamHistories = (
    query?: {
      /**
       * Zero-based page index (0..N)
       * @min 0
       * @default 0
       */
      page?: number;
      /**
       * The size of the page to be returned
       * @min 1
       * @default 10
       */
      size?: number;
      /**
       * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
       * @default ["createdAt,DESC"]
       */
      sort?: string[];
    },
    params: RequestParams = {},
  ) =>
    this.request<GetExamHistoriesData, any>({
      path: `/history/exams`,
      method: "GET",
      query: query,
      secure: true,
      ...params,
    });
  /**
   * @description 특정 모의고사(examId)에 대한 상세 정보를 조회한다
   *
   * @tags History
   * @name GetExamHistoryDetail
   * @summary 모의고사 히스토리 상세 조회
   * @request GET:/history/exams/{examId}
   * @secure
   */
  getExamHistoryDetail = (examId: number, params: RequestParams = {}) =>
    this.request<GetExamHistoryDetailData, any>({
      path: `/history/exams/${examId}`,
      method: "GET",
      secure: true,
      ...params,
    });
  /**
   * @description 특정 모의고사(examId)의 특정 문항 순서(question_order)에 대한 상세 피드백을 조회한다
   *
   * @tags History
   * @name GetExamAnswerDetail
   * @summary 모의고사 문항(답변) 상세 조회
   * @request GET:/history/exams/{examId}/answers/{questionOrder}
   * @secure
   */
  getExamAnswerDetail = (
    examId: number,
    questionOrder: number,
    params: RequestParams = {},
  ) =>
    this.request<GetExamAnswerDetailData, any>({
      path: `/history/exams/${examId}/answers/${questionOrder}`,
      method: "GET",
      secure: true,
      ...params,
    });
}
