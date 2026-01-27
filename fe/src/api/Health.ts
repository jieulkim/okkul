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

import { HealthCheckData, HealthCheckError } from "./data-contracts";
import { HttpClient, RequestParams } from "./http-client";

export class Health<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 서버가 정상적으로 동작하는지 확인합니다.
   *
   * @tags 헬스체크
   * @name HealthCheck
   * @summary 서버 헬스 체크
   * @request GET:/health
   */
  healthCheck = (params: RequestParams = {}) =>
    this.request<HealthCheckData, HealthCheckError>({
      path: `/health`,
      method: "GET",
      ...params,
    });
}
