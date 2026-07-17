package com.josee.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;



@SpringBootTest
class EngineeringConsultantAppTest {

    @Resource
    private EngineeringConsultantApp engineeringConsultantApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是Josee";
        String answer = engineeringConsultantApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我的朋友（Kmk）是一个AI架构工程师，我希望你能帮助他进行一次架构决策";
        answer = engineeringConsultantApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的朋友叫什么来着？刚跟你说过，帮我回忆一下";
        answer = engineeringConsultantApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是Josee，我的朋友（KMK）是一个AI架构工程师，我希望能帮助他进行一次架构决策，但我不知道该怎么做";
        EngineeringConsultantApp.EngineeringConsultantReport engineeringConsultantReport = engineeringConsultantApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(engineeringConsultantReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我的项目已经上线一年了，现在打算要重构这个项目，但人数有限，时间可能不太够，该怎么办？";
        String answer = engineeringConsultantApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }
}
