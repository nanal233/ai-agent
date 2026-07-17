package com.josee.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ECAppDocumentLoaderTest {

    @Resource
    private ECAppDocumentLoader ecAppDocumentLoader;

    @Test
    void loadMarkdowns() {
        ecAppDocumentLoader.loadMarkdowns();
    }
}