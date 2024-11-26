package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

public class StorageUploader {
    private static final String CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=lincsc311storage;AccountKey=HMSn+TaAXEmc5TMTEo8L6CGL3lNz3QM8H+KcVVHyf99HOy3GL8jSB4npWyJFoeR4ZQgG27McXA6a+AStOaVytg==;EndpointSuffix=core.windows.net";
    private static final String CONTAINER_NAME = "media-files";
    
    private final BlobContainerClient containerClient;

    public StorageUploader() {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString(CONNECTION_STRING)
                .containerName(CONTAINER_NAME)
                .buildClient();
    }

    public void uploadFile(String filePath, String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(filePath);
    }

    public BlobContainerClient getContainerClient() {
        return containerClient;
    }
}
