package site.okkul.be.infra.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * 파일을 스토리지에 업로드하고 접근 가능한 URL을 반환합니다.
     *
     * @param file   업로드할 파일
     * @param domain 파일이 속할 도메인 (예: "profiles", "posts"). 폴더처럼 사용됩니다.
     * @return 접근 가능한 전체 파일 URL
     */
    String upload(MultipartFile file, String domain);

    /**
     * 파일 URL을 기반으로 스토리지에서 파일을 삭제합니다.
     *
     * @param fileUrl 삭제할 파일의 전체 URL
     */
    void delete(String fileUrl);
}
