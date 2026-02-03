import { Exam } from "./Exam";
import { Surveys } from "./Surveys";
import { Users } from "./Users";
import { Practices } from "./Practices";
import { History } from "./History";
import { Auth } from './Auth';
import axios from 'axios';

const commonConfig = {
  baseURL: import.meta.env.VITE_API_BASE_URL,
  secure: true,
  securityWorker: async () => {
    const accessToken = localStorage.getItem("accessToken");
    // securityWorker 에서는 refresh token을 헤더에 담지 않습니다.
    // reissue 시에만 사용되어야 합니다.
    
    // 디버깅을 위한 로그 추가
    // console.log('[SecurityWorker] Fetching tokens from localStorage:', { 
    //   accessToken: accessToken ? `${accessToken.substring(0, 10)}...` : 'MISSING', 
    // });

    if (accessToken) {
      const headers = {
        Authorization: `Bearer ${accessToken}`,
      };
      
      return { headers };
    }
    return {};
  },
};

export const examApi = new Exam(commonConfig);
export const surveysApi = new Surveys(commonConfig);
export const usersApi = new Users(commonConfig);
export const practicesApi = new Practices(commonConfig);
export const historyApi = new History(commonConfig);
// reissue 전용 Auth API 인스턴스 (순환 참조 방지)
const authApi = new Auth({ baseURL: import.meta.env.VITE_API_BASE_URL });


// 여러 요청이 동시에 401 오류를 받을 경우 토큰 재발급을 한 번만 실행하기 위한 변수
let isReissuing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};


// 모든 인스턴스에 공통으로 적용할 인터셉터 설정 함수
const setupInterceptors = (apiInstance) => {
  apiInstance.instance.interceptors.request.use(config => {

    //  FormData 전송 시 Content-Type 수동 설정 제거 (브라우저가 boundary를 자동 생성하도록 함)
    if (config.data instanceof FormData) {
      if (config.headers['Content-Type']) {
        delete config.headers['Content-Type'];
      }
    }

      // console.log(`[API Request] ${config.method.toUpperCase()} ${config.url}`, {
      //   url: config.url,
      //   method: config.method,
      //   headers: { ...config.headers } // 헤더 전체를 로그로 확인
      // });
    return config;
  }, error => {
    return Promise.reject(error);
  });

  apiInstance.instance.interceptors.response.use(
    response => response,
    async (error) => {
      const originalRequest = error.config;

      // 401 에러이고, 재시도한 요청이 아닐 경우
      if (error.response.status === 401 && !originalRequest._retry) {
        
        if (isReissuing) {
          // 토큰 재발급이 진행 중인 경우, 큐에 추가하고 대기
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          }).then(accessToken => {
            originalRequest.headers['Authorization'] = 'Bearer ' + accessToken;
            return axios(originalRequest);
          }).catch(err => {
            return Promise.reject(err);
          });
        }

        originalRequest._retry = true;
        isReissuing = true;

        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
          isReissuing = false;
          // 로그아웃 처리
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
          return Promise.reject(error);
        }

        try {
          const reissueResponse = await authApi.reissue({ refreshToken });
          const newAccessToken = reissueResponse.data.accessToken;

          if (!newAccessToken) {
            throw new Error("New accessToken not found");
          }

          localStorage.setItem('accessToken', newAccessToken);
          
          // 새로 발급받은 토큰으로 원래 요청의 헤더 변경
          axios.defaults.headers.common['Authorization'] = 'Bearer ' + newAccessToken;
          originalRequest.headers['Authorization'] = 'Bearer ' + newAccessToken;
          
          processQueue(null, newAccessToken); // 대기 중인 요청들 재개

          // 원래 요청 재시도
          return axios(originalRequest);
        } catch (reissueError) {
          processQueue(reissueError, null); // 대기 중인 요청들 실패 처리
          // 리프레시 토큰이 만료되었거나 문제가 생긴 경우
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
          return Promise.reject(reissueError);
        } finally {
          isReissuing = false;
        }
      }

      return Promise.reject(error);
    }
  );
};

// 각 API 인스턴스에 인터셉터 적용
setupInterceptors(examApi);
setupInterceptors(surveysApi);
setupInterceptors(usersApi);
setupInterceptors(practicesApi);
setupInterceptors(historyApi);
