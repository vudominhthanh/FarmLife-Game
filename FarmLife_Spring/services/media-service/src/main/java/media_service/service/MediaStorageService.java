package media_service.service;

import java.time.Duration;

public interface MediaStorageService {
    String generatePresignedPutUrl(String bucket, String objectKey, Duration duration, String contentType);
    String generatePresignedGetUrl(String bucket, String objectKey, Duration duration);
    byte[] getObjectBytes(String bucket, String objectKey);
    void putObject(String bucket, String objectKey, byte[] data, String contentType);
    void deleteObject(String bucket, String objectKey);
}
