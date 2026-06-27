package com.escom.banco.service;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.pubsub.v1.TopicName;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

@Service
public class DistribuidoService {

    private final Storage storage;
    private final String bucketName = System.getenv("GCS_BUCKET");
    private final String projectId = System.getenv("GCP_PROJECT_ID");
    private final String topicId = System.getenv("PUBSUB_TOPIC");
    private Publisher publisher;

    public DistribuidoService(Storage storage) {
        this.storage = storage;
        try {
            if (projectId != null && topicId != null) {
                TopicName topicName = TopicName.of(projectId, topicId);
                this.publisher = Publisher.newBuilder(topicName).build();
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar Publisher de PubSub: " + e.getMessage());
        }
    }

    public void guardarTransaccionEnStorage(long secuencia, String jsonTransaccion) {
        try {
            BlobId blobId = BlobId.of(bucketName, secuencia + ".json");
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/json").build();
            storage.create(blobInfo, jsonTransaccion.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error al guardar en Cloud Storage: " + e.getMessage());
        }
    }

    public void publicarTransaccion(String jsonTransaccion) {
        if (publisher == null) return;
        try {
            ByteString data = ByteString.copyFromUtf8(jsonTransaccion);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            publisher.publish(pubsubMessage);
        } catch (Exception e) {
            System.err.println("Error al publicar en PubSub: " + e.getMessage());
        }
    }
}