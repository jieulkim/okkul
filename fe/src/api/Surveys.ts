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
  CreateSurveyData,
  GetSurveyByIdData,
  GetSurveyListData,
  GetSurveyTopicsData,
  SurveyCreateRequest,
} from "./data-contracts";
import { ContentType, HttpClient, RequestParams } from "./http-client";

export class Surveys<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 현재 로그인한 사용자가 저장한 모든 설문조사의 요약 목록을 조회합니다.
   *
   * @tags 설문조사
   * @name GetSurveyList
   * @summary 나의 설문조사 목록 조회
   * @request GET:/surveys
   */
  getSurveyList = (params: RequestParams = {}) =>
    this.request<GetSurveyListData, any>({
      path: `/surveys`,
      method: "GET",
      ...params,
    });
  /**
   * @description 사용자가 응답한 설문조사 결과를 DB에 저장합니다. 사용자가 선택한 항목들은 답변ID 혹은 T/F 혹은 소재ID로 요청바디에 담겨옵니다.
   *
   * @tags 설문조사
   * @name CreateSurvey
   * @summary 설문조사 결과 저장
   * @request POST:/surveys
   */
  createSurvey = (data: SurveyCreateRequest, params: RequestParams = {}) =>
    this.request<CreateSurveyData, any>({
      path: `/surveys`,
      method: "POST",
      body: data,
      type: ContentType.Json,
      ...params,
    });
  /**
   * @description 설문조사 ID를 통해 특정 설문조사의 상세 결과를 조회합니다. 요청된 설문조사가 현재 로그인한 사용자의 것인지 확인합니다.
   *
   * @tags 설문조사
   * @name GetSurveyById
   * @summary 특정 설문조사 결과 조회
   * @request GET:/surveys/{surveyId}
   */
  getSurveyById = (surveyId: number, params: RequestParams = {}) =>
    this.request<GetSurveyByIdData, any>({
      path: `/surveys/${surveyId}`,
      method: "GET",
      ...params,
    });
  /**
   * @description 설문조사 Part 4(취미/여가) 단계에서 선택할 수 있는 모든 소재를 카테고리별로 그룹화하여 반환합니다. 프론트엔드는 이 데이터를 받아 화면을 구성합니다.
   *
   * @tags 설문조사
   * @name GetSurveyTopics
   * @summary 설문용 카테고리별 소재 목록 조회
   * @request GET:/surveys/topics
   */
  getSurveyTopics = (params: RequestParams = {}) =>
    this.request<GetSurveyTopicsData, any>({
      path: `/surveys/topics`,
      method: "GET",
      ...params,
    });
}
