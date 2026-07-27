<template>
  <ChatRoom
    title="AI 超级智能体"
    api-path="/ai/manus/chat"
    :with-chat-id="false"
    mode="steps"
  />
</template>

<script setup>
import ChatRoom from '../components/ChatRoom.vue'
</script>
