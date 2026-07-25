package com.josee.aiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JoseeManusTest {

    @Resource
    private JoseeManus joseeManus;

    @Test
    void run() {
        String userPrompt = """  
                我的项目已经上线一年了，现在打算要重构这个项目，给我推荐一些目前主流的解决方案，
                并结合一些网络图片，制定一份重构计划，
                并以 PDF 格式输出""";
        String answer = joseeManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}
