<template>
  <ChatRoom
    title="AI 工程师顾问"
    api-path="/ai/ec_app/chat/sse2"
    :with-chat-id="true"
  />
</template>

<script setup>
import ChatRoom from '../components/ChatRoom.vue'
</script>
