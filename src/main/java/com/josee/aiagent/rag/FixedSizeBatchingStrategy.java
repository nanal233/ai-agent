package com.josee.aiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;

import java.util.ArrayList;
import java.util.List;

public class FixedSizeBatchingStrategy implements BatchingStrategy {
    private static final int MAX_BATCH_SIZE = 10;

    @Override
    public List<List<Document>> batch(List<Document> documents) {
        List<List<Document>> batches = new ArrayList<>();
        for (int i = 0; i < documents.size(); i += MAX_BATCH_SIZE) {
            batches.add(documents.subList(i, Math.min(i + MAX_BATCH_SIZE, documents.size())));
        }
        return batches;
    }
}