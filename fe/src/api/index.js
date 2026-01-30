import { Exam } from "./Exam";
import { Surveys } from "./Surveys";
import { Users } from "./Users";
import { Practices } from "./Practices";

const commonConfig = {
  baseURL: import.meta.env.VITE_API_BASE_URL,
  secure: true,
  securityWorker: async () => {
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");
    
    // 디버깅을 위한 로그 추가
    console.log('[SecurityWorker] Fetching tokens from localStorage:', { 
      accessToken: accessToken ? `${accessToken.substring(0, 10)}...` : 'MISSING', 
      refreshToken: refreshToken ? `${refreshToken.substring(0, 10)}...` : 'MISSING' 
    });

    if (accessToken) {
      const headers = {
        Authorization: `Bearer ${accessToken}`,
      };
      
      if (refreshToken) {
        headers["Authorization-Refresh"] = `Bearer ${refreshToken}`;
        headers["Authorization-refresh"] = `Bearer ${refreshToken}`;
        headers["X-Refresh-Token"] = `Bearer ${refreshToken}`;
        headers["Refresh-Token"] = `Bearer ${refreshToken}`;
      }
      
      return { headers };
    }
    return {};
  },
};

export const examApi = new Exam(commonConfig);
export const surveysApi = new Surveys(commonConfig);
export const usersApi = new Users(commonConfig);
export const practicesApi = new Practices(commonConfig);

// 모든 인스턴스에 공통으로 적용할 인터셉터 설정 함수
const setupInterceptors = (apiInstance) => {
  apiInstance.instance.interceptors.request.use(config => {

    //  FormData 전송 시 Content-Type 수동 설정 제거 (브라우저가 boundary를 자동 생성하도록 함)
    if (config.data instanceof FormData) {
      if (config.headers['Content-Type']) {
        delete config.headers['Content-Type'];
      }
    }

      console.log(`[API Request] ${config.method.toUpperCase()} ${config.url}`, {
        url: config.url,
        method: config.method,
        headers: { ...config.headers } // 헤더 전체를 로그로 확인
      });
    return config;
  }, error => {
    return Promise.reject(error);
  });
};

// 각 API 인스턴스에 인터셉터 적용
setupInterceptors(examApi);
setupInterceptors(surveysApi);
setupInterceptors(usersApi);
setupInterceptors(practicesApi);


