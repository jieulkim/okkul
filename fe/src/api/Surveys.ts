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
  GetSurveyTopicByIdData,
  SurveyCreateRequest,
} from "./data-contracts";
import { ContentType, HttpClient, RequestParams } from "./http-client";

export class Surveys<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 현재 로그인한 사용자가 저장한 모든 설문조사의 요약 목록을 조회합니다.
   *
   * @tags Survey
   * @name GetSurveyList
   * @summary 설문조사 목록 조회 API
   * @request GET:/surveys
   * @secure
   */
  getSurveyList = (params: RequestParams = {}) =>
    this.request<GetSurveyListData, any>({
      path: `/surveys`,
      method: "GET",
      secure: true,
      ...params,
    });
  /**
   * @description 사용자의 질문별 응답항목을 RequestBody로 받아 Survey 테이블에 저장합니다. 사용자가 선택한 항목들은 답변ID 혹은 T/F 혹은 소재ID로 요청바디에 담겨옵니다.
   *
   * @tags Survey
   * @name CreateSurvey
   * @summary 설문조사 생성 API
   * @request POST:/surveys
   * @secure
   */
  createSurvey = (data: SurveyCreateRequest, params: RequestParams = {}) =>
    this.request<CreateSurveyData, any>({
      path: `/surveys`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
  /**
   * @description 설문조사 ID를 통해 특정 설문조사의 상세 결과를 조회합니다. 요청된 설문조사가 현재 로그인한 사용자의 것인지 확인합니다.
   *
   * @tags Survey
   * @name GetSurveyById
   * @summary 특정 설문조사 결과 조회 API
   * @request GET:/surveys/{surveyId}
   * @secure
   */
  getSurveyById = (surveyId: number, params: RequestParams = {}) =>
    this.request<GetSurveyByIdData, any>({
      path: `/surveys/${surveyId}`,
      method: "GET",
      secure: true,
      ...params,
    });
  /**
   * @description 설문조사 ID를 통해 특정 설문조사가 포함하고 있는 토픽 목록을 반환합니다.중분류 카테고리 상관없이 모든 토픽 목록을 가져옵니다(직업정보, 학생여부, 거주형태 등 포함)유형별 연습 모드에서 특정 설문조사에 따라 선택할 수 있는 토픽을 조회하기 위한 API입니다.해당 API는 유저 정보와 설문조사 ID에 따라서 응답이 달라집니다.
   *
   * @tags Survey
   * @name GetSurveyTopicById
   * @summary 설문조사 토픽 조회 API (유형별 연습 모드 토픽선택 화면 구성용)
   * @request GET:/surveys/{surveyId}/topics
   * @secure
   */
  getSurveyTopicById = (surveyId: number, params: RequestParams = {}) =>
    this.request<GetSurveyTopicByIdData, any>({
      path: `/surveys/${surveyId}/topics`,
      method: "GET",
      secure: true,
      ...params,
    });
}
