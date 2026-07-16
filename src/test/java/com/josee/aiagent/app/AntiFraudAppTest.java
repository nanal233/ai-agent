package com.josee.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;



@SpringBootTest
class AntiFraudAppTest {

    @Resource
    private AntiFraudApp antiFraudApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是Josee";
        String answer = antiFraudApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我的朋友（Kmk）前段时间被卷入了一场网络诈骗事件，我希望你能够提高他的反诈能力";
        answer = antiFraudApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的朋友叫什么来着？刚跟你说过，帮我回忆一下";
        answer = antiFraudApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是Josee，我的朋友（KMK）前段时间被卷入了一场网络诈骗时间，我希望能帮他提高反诈能力，但我不知道该怎么做";
        AntiFraudApp.AntiFraudReport antiFraudReport = antiFraudApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(antiFraudReport);
    }
}
