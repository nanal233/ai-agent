package com.josee.aiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class HttpAiInvoke {

    public static void main(String[] args) {
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        JSONObject message1 = new JSONObject();
        message1.set("role", "system");
        message1.set("content", "You are a helpful assistant.");

        JSONObject message2 = new JSONObject();
        message2.set("role", "user");
        message2.set("content", "你是谁");

        JSONObject input = new JSONObject();
        input.set("messages", JSONUtil.createArray().set(message1).set(message2));

        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");

        JSONObject body = new JSONObject();
        body.set("model", "qwen-plus");
        body.set("input", input);
        body.set("parameters", parameters);

        // 关键改动：直接用 testApiKey.API_KEY，而不是 System.getenv()
        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + testApiKey.API_KEY)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(30000)
                .execute();

        if (response.isOk()) {
            System.out.println("响应内容: " + response.body());
        } else {
            System.err.println("请求失败，状态码: " + response.getStatus());
            System.err.println("响应内容: " + response.body());
        }
    }
}