import { Exam } from "./Exam";
import { Surveys } from "./Surveys";
import { Users } from "./Users";

const commonConfig = {
  baseURL: "https://api.dev.okkul.site",
  secure: true,
  securityWorker: async () => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      return {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      };
    }
  },
};

export const examApi = new Exam(commonConfig);
export const surveysApi = new Surveys(commonConfig);
export const usersApi = new Users(commonConfig);

// CORS 및 경로 이슈 해결을 위한 인터셉터
 examApi.instance.interceptors.request.use(config => {

  // 1. 경로 중복 수정 (/exam/exam/ -> /exam/)
  if (config.url && config.url.includes('/exam/exam/')) {
    config.url = config.url.replace('/exam/exam/', '/exam/');
  }

  // 2. FormData 전송 시 Content-Type 수동 설정 제거 (브라우저가 boundary를 자동 생성하도록 함)
  if (config.data instanceof FormData) {
    if (config.headers['Content-Type']) {
      delete config.headers['Content-Type'];
    }
  }

  console.log(`[API Request] ${config.method.toUpperCase()} ${config.url}`, config.headers);
  return config;
});


