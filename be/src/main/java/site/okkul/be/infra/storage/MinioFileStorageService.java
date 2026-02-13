package site.okkul.be.infra.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Override
    public String upload(MultipartFile file, String domain) {
        try {
            // 1. 파일 이름 생성 (UUID를 사용하여 고유성 보장)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            String objectName = domain + "/" + uniqueFilename;

            // 2. 파일 업로드
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            inputStream.close();

            // 3. 업로드된 파일의 URL 반환
            // MinIO의 경우 endpoint/bucketName/objectName 형태의 URL을 직접 조합합니다.
            // SSL/TLS 및 포트 설정에 따라 URL 구조가 달라질 수 있습니다.
            return endpoint + "/" + bucketName + "/" + objectName;

        } catch (Exception e) {
            log.error("MinIO file upload error", e);
            // TODO: 도메인에 맞는 커스텀 예외로 전환하는 것을 고려해 보세요.
            throw new RuntimeException("파일 업로드에 실패했습니다.");
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            // 1. URL에서 객체 이름 파싱
            // 예: http://localhost:9000/my-bucket/profiles/image.jpg -> profiles/image.jpg
            String objectName = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);

            // 2. 파일 삭제
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO file delete error", e);
            // TODO: 도메인에 맞는 커스텀 예외로 전환하는 것을 고려해 보세요.
            throw new RuntimeException("파일 삭제에 실패했습니다.");
        }
    }
}
