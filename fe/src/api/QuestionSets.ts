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
  AddQuestionData,
  CreateQuestionSetData,
  DeleteQuestionData,
  DeleteQuestionSetData,
  GetQuestionSetData,
  GetQuestionSetsData,
  QuestionRequest,
  QuestionSetRequest,
  UpdateQuestionData,
  UpdateQuestionSetData,
} from "./data-contracts";
import { ContentType, HttpClient, RequestParams } from "./http-client";

export class QuestionSets<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 전체 문항 세트를 페이징하여 조회합니다.
   *
   * @tags Question
   * @name GetQuestionSets
   * @summary 문항 세트 목록 조회
   * @request GET:/question-sets
   */
  getQuestionSets = (
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
       * @default ["id,ASC"]
       */
      sort?: string[];
    },
    params: RequestParams = {},
  ) =>
    this.request<GetQuestionSetsData, any>({
      path: `/question-sets`,
      method: "GET",
      query: query,
      ...params,
    });
  /**
   * No description
   *
   * @tags Question
   * @name CreateQuestionSet
   * @summary 문항 세트 생성 (어드민)
   * @request POST:/question-sets
   * @secure
   */
  createQuestionSet = (data: QuestionSetRequest, params: RequestParams = {}) =>
    this.request<CreateQuestionSetData, any>({
      path: `/question-sets`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
  /**
   * @description 특정 세트에 새로운 문항을 추가합니다.
   *
   * @tags Question
   * @name AddQuestion
   * @summary 문항 추가 (어드민)
   * @request POST:/question-sets/{setId}/questions
   * @secure
   */
  addQuestion = (
    setId: number,
    data: QuestionRequest,
    params: RequestParams = {},
  ) =>
    this.request<AddQuestionData, any>({
      path: `/question-sets/${setId}/questions`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
  /**
   * No description
   *
   * @tags Question
   * @name GetQuestionSet
   * @summary 문항 세트 단건 조회
   * @request GET:/question-sets/{setId}
   */
  getQuestionSet = (setId: number, params: RequestParams = {}) =>
    this.request<GetQuestionSetData, any>({
      path: `/question-sets/${setId}`,
      method: "GET",
      ...params,
    });
  /**
   * No description
   *
   * @tags Question
   * @name DeleteQuestionSet
   * @summary 문항 세트 삭제 (어드민)
   * @request DELETE:/question-sets/{setId}
   * @secure
   */
  deleteQuestionSet = (setId: number, params: RequestParams = {}) =>
    this.request<DeleteQuestionSetData, any>({
      path: `/question-sets/${setId}`,
      method: "DELETE",
      secure: true,
      ...params,
    });
  /**
   * No description
   *
   * @tags Question
   * @name UpdateQuestionSet
   * @summary 문항 세트 수정 (어드민)
   * @request PATCH:/question-sets/{setId}
   * @secure
   */
  updateQuestionSet = (
    setId: number,
    data: QuestionSetRequest,
    params: RequestParams = {},
  ) =>
    this.request<UpdateQuestionSetData, any>({
      path: `/question-sets/${setId}`,
      method: "PATCH",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
  /**
   * @description 세트 내에서 특정 문항을 삭제합니다.
   *
   * @tags Question
   * @name DeleteQuestion
   * @summary 문항 삭제 (어드민)
   * @request DELETE:/question-sets/{setId}/questions/{questionId}
   * @secure
   */
  deleteQuestion = (
    setId: number,
    questionId: number,
    params: RequestParams = {},
  ) =>
    this.request<DeleteQuestionData, any>({
      path: `/question-sets/${setId}/questions/${questionId}`,
      method: "DELETE",
      secure: true,
      ...params,
    });
  /**
   * @description 특정 문항의 텍스트나 음성 URL을 수정합니다.
   *
   * @tags Question
   * @name UpdateQuestion
   * @summary 문항 수정 (어드민)
   * @request PATCH:/question-sets/{setId}/questions/{questionId}
   * @secure
   */
  updateQuestion = (
    setId: number,
    questionId: number,
    data: QuestionRequest,
    params: RequestParams = {},
  ) =>
    this.request<UpdateQuestionData, any>({
      path: `/question-sets/${setId}/questions/${questionId}`,
      method: "PATCH",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
}
