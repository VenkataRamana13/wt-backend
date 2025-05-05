package com.wtplatform.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private S3Client s3Client;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.s3.region}")
    private String region;

    /**
     * This method will be called after properties are set to initialize the S3 client with the correct region
     */
    @PostConstruct
    public void init() {
        Region awsRegion = Region.of(region != null && !region.isEmpty() ? region : "ap-south-1");
        logger.info("Initializing S3 client with region: {}", awsRegion);
        
        // Initialize the S3 client with the configured region
        this.s3Client = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
    
    /**
     * Uploads a file to S3 bucket with a specific folder structure
     * 
     * @param file The file to upload
     * @param folderPath The folder path where the file should be stored
     * @return The S3 key of the uploaded file
     * @throws IOException If there's an issue with the file
     */
    public String uploadFile(MultipartFile file, String folderPath) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalFilename = file.getOriginalFilename();
        
        // Create a key in format: folderPath/timestamp_filename
        String key = String.format("%s/%s_%s", 
                folderPath.endsWith("/") ? folderPath.substring(0, folderPath.length() - 1) : folderPath, 
                timestamp, 
                originalFilename);
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            logger.info("File uploaded successfully to S3: {}", key);
            
            return key;
        } catch (S3Exception e) {
            logger.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }
    
    /**
     * Downloads a file from S3 to the given path
     * 
     * @param key The S3 key of the file
     * @param destinationPath The path to save the file to
     */
    public void downloadFile(String key, Path destinationPath) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.getObject(getObjectRequest, destinationPath);
            logger.info("File downloaded successfully from S3: {}", key);
        } catch (S3Exception e) {
            logger.error("Error downloading file from S3", e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }
    
    /**
     * Deletes a file from S3
     * 
     * @param key The S3 key of the file to delete
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key).build());
            logger.info("File deleted successfully from S3: {}", key);
        } catch (S3Exception e) {
            logger.error("Error deleting file from S3", e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }
    
    /**
     * Lists objects in S3 with the given prefix
     * 
     * @param prefix The prefix to filter objects by
     * @return A list of S3 objects matching the prefix
     */
    public List<S3Object> listObjects(String prefix) {
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();
            
            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
            logger.info("Listed {} objects from S3 with prefix: {}", response.contents().size(), prefix);
            
            return response.contents();
        } catch (S3Exception e) {
            logger.error("Error listing objects from S3", e);
            throw new RuntimeException("Failed to list objects from S3", e);
        }
    }
    
    /**
     * Get the bucket name
     * 
     * @return The S3 bucket name
     */
    public String getBucketName() {
        return bucketName;
    }
} 