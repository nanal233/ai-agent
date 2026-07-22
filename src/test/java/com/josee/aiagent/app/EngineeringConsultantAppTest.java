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

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("我的项目已经上线一年了，现在打算要重构这个项目，给我推荐一些解决方案？");

        // 测试网页抓取：案例分析
        testMessage("我的项目已经上线一年了，现在打算要重构这个项目，看看该网站（www.baidu.com）的其他案例是怎么解决的？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张项目架构图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的项目档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份项目重构方案PDF，包含整体思路，大致流程");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = engineeringConsultantApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        // 测试图片搜索 MCP
        String message = "Please help me searching some project structure pictures";
        String answer =  engineeringConsultantApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }

}
