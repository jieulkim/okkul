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

import { ReissueData, TokenReissueRequest } from "./data-contracts";
import { ContentType, HttpClient, RequestParams } from "./http-client";

export class Auth<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.<br>body에 리프래쉬 토큰만 담아주세요
   *
   * @tags Auth
   * @name Reissue
   * @summary 액세스 토큰 재발급
   * @request POST:/auth/reissue
   */
  reissue = (data: TokenReissueRequest, params: RequestParams = {}) =>
    this.request<ReissueData, any>({
      path: `/auth/reissue`,
      method: "POST",
      body: data,
      type: ContentType.Json,
      ...params,
    });
}
