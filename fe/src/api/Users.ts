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
  GetMyInfoData,
  UpdateNicknameData,
  UpdateNicknameRequest,
  UpdateProfileImageData,
  UpdateProfileImagePayload,
  UpdateTargetLevelData,
  UpdateTargetLevelRequest,
} from "./data-contracts";
import { ContentType, HttpClient, RequestParams } from "./http-client";

export class Users<
  SecurityDataType = unknown,
> extends HttpClient<SecurityDataType> {
  /**
   * @description 오픽 목표 등급(IM2, AL 등)을 수정합니다.
   *
   * @tags User
   * @name UpdateTargetLevel
   * @summary 목표 등급(Target Level) 수정
   * @request POST:/users/target-level
   * @secure
   */
  updateTargetLevel = (
    data: UpdateTargetLevelRequest,
    params: RequestParams = {},
  ) =>
    this.request<UpdateTargetLevelData, any>({
      path: `/users/target-level`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
  /**
   * @description 프로필 이미지를 업로드하여 변경합니다. (MultipartFile)
   *
   * @tags User
   * @name UpdateProfileImage
   * @summary (미구현) 프로필 이미지 변경
   * @request POST:/users/profile-image
   * @deprecated
   * @secure
   */
  updateProfileImage = (
    data: UpdateProfileImagePayload,
    params: RequestParams = {},
  ) =>
    this.request<UpdateProfileImageData, any>({
      path: `/users/profile-image`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.FormData,
      ...params,
    });
  /**
   * @description 사용자의 닉네임을 변경합니다.
   *
   * @tags User
   * @name UpdateNickname
   * @summary 닉네임 수정
   * @request POST:/users/nickname
   * @secure
   */
  updateNickname = (data: UpdateNicknameRequest, params: RequestParams = {}) =>
    this.request<UpdateNicknameData, any>({
      path: `/users/nickname`,
      method: "POST",
      body: data,
      secure: true,
      type: ContentType.Json,
      ...params,
    });
  /**
   * @description 현재 로그인한 사용자의 상세 정보를 조회합니다.
   *
   * @tags User
   * @name GetMyInfo
   * @summary 내 정보 조회
   * @request GET:/users/me
   * @secure
   */
  getMyInfo = (params: RequestParams = {}) =>
    this.request<GetMyInfoData, any>({
      path: `/users/me`,
      method: "GET",
      secure: true,
      ...params,
    });
}
