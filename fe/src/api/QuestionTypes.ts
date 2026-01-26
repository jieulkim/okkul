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
  CreateQuestionTypeData,
  DeleteQuestionTypeData,
  GetQuestionTypeData,
  GetQuestionTypesData,
  QuestionTypeRequest,
  UpdateQuestionTypeData,
} from "./data-contracts";
import { ContentType, HttpClient, RequestParams } from "./http-client";

export class QuestionTypes<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 페이징 가능한 문항 유형 목록을 반환합니다. 지원 파라미터: `page` (0부터 시작), `size`, `sort`. 응답은 HATEOAS `PagedModel<...>` 형태이며, `Pageable`을 통해 페이지를 제어합니다.
   *
   * @tags Question
   * @name GetQuestionTypes
   * @summary 문항 유형 목록 조회
   * @request GET:/question-types
   */
  getQuestionTypes = (
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
    this.request<GetQuestionTypesData, any>({
      path: `/question-types`,
      method: "GET",
      query: query,
      ...params,
    });
  /**
   * @description 새 문항 유형을 생성합니다. 유효성 검사 후 생성된 리소스의 URI를 `Location` 헤더로 반환합니다.
   *
   * @tags Question
   * @name CreateQuestionType
   * @summary 문항 유형 생성 (어드민 전용)
   * @request POST:/question-types
   * @secure
   */
  createQuestionType = (
    data: QuestionTypeRequest,
    params: RequestParams = {},
  ) =>
    this.request<CreateQuestionTypeData, any>({
      path: `/question-types`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
  /**
   * @description 문항 유형 ID로 단건 조회합니다. 존재하지 않으면 404를 반환합니다.
   *
   * @tags Question
   * @name GetQuestionType
   * @summary 문항 유형 단건 조회
   * @request GET:/question-types/{id}
   */
  getQuestionType = (id: number, params: RequestParams = {}) =>
    this.request<GetQuestionTypeData, any>({
      path: `/question-types/${id}`,
      method: "GET",
      ...params,
    });
  /**
   * @description 문항 유형을 삭제합니다. 성공 시 `204 No Content`를 반환합니다.
   *
   * @tags Question
   * @name DeleteQuestionType
   * @summary 문항 유형 삭제 (어드민 전용)
   * @request DELETE:/question-types/{id}
   * @secure
   */
  deleteQuestionType = (id: number, params: RequestParams = {}) =>
    this.request<DeleteQuestionTypeData, any>({
      path: `/question-types/${id}`,
      method: "DELETE",
      secure: true,
      ...params,
    });
  /**
   * @description 기존 문항 유형을 수정합니다. 부분 수정 또는 전체 교체를 설명하고, 성공 시 수정된 리소스를 반환합니다.
   *
   * @tags Question
   * @name UpdateQuestionType
   * @summary 문항 유형 수정 (어드민 전용)
   * @request PATCH:/question-types/{id}
   * @secure
   */
  updateQuestionType = (
    id: number,
    data: QuestionTypeRequest,
    params: RequestParams = {},
  ) =>
    this.request<UpdateQuestionTypeData, any>({
      path: `/question-types/${id}`,
      method: "PATCH",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
}
