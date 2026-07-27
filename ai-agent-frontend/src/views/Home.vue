<template>
  <div class="home-page">
    <div class="home-hero">
      <p class="hero-tag">// SYSTEM ONLINE</p>
      <h1 class="home-title">
        AI <span class="glow-text">应用中心</span><span class="cursor">_</span>
      </h1>
      <p class="home-subtitle">&gt; 选择一个应用，开始对话</p>
    </div>

    <div class="app-grid">
      <router-link to="/ec-app" class="app-card">
        <div class="card-titlebar">
          <span class="dot dot-red"></span>
          <span class="dot dot-amber"></span>
          <span class="dot dot-green"></span>
          <span class="card-path">~/apps/ec-app</span>
        </div>
        <div class="card-body">
          <div class="app-icon">🛠️</div>
          <h2>AI 工程师顾问</h2>
          <p>面向工程实践问题的专业顾问应用，支持多轮会话上下文。</p>
          <span class="card-cta">进入会话 <i>›</i></span>
        </div>
      </router-link>

      <router-link to="/manus" class="app-card">
        <div class="card-titlebar">
          <span class="dot dot-red"></span>
          <span class="dot dot-amber"></span>
          <span class="dot dot-green"></span>
          <span class="card-path">~/apps/manus</span>
        </div>
        <div class="card-body">
          <div class="app-icon">🤖</div>
          <h2>AI 超级智能体</h2>
          <p>具备工具调用与自主规划能力的通用智能体应用。</p>
          <span class="card-cta">进入会话 <i>›</i></span>
        </div>
      </router-link>
    </div>
  </div>
</template>

<script setup></script>

<style scoped>
.home-page {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 96px 24px;
}

.home-hero {
  text-align: center;
  margin-bottom: 56px;
}

.hero-tag {
  font-family: var(--font-mono);
  font-size: 13px;
  letter-spacing: 0.2em;
  color: var(--accent-green);
  margin: 0 0 12px;
}

.home-title {
  font-size: clamp(28px, 5vw, 44px);
  font-weight: 700;
  margin: 0 0 12px;
  letter-spacing: 0.02em;
}

.glow-text {
  background: linear-gradient(90deg, var(--accent-cyan), var(--accent-purple));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  filter: drop-shadow(0 0 18px rgba(0, 240, 255, 0.35));
}

.cursor {
  color: var(--accent-cyan);
  animation: blink 1.1s step-end infinite;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.home-subtitle {
  font-family: var(--font-mono);
  color: var(--text-secondary);
  margin: 0;
  font-size: 14px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 320px));
  gap: 28px;
  width: 100%;
  max-width: 700px;
  justify-content: center;
}

.app-card {
  display: flex;
  flex-direction: column;
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.app-card:hover {
  transform: translateY(-6px);
  border-color: var(--border-color-strong);
  box-shadow: var(--shadow-glow-cyan);
}

.card-titlebar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: var(--bg-panel-raised);
  border-bottom: 1px solid var(--border-color);
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  display: inline-block;
}

.dot-red {
  background: #ff5f56;
}

.dot-amber {
  background: #ffbd2e;
}

.dot-green {
  background: #27c93f;
}

.card-path {
  margin-left: 8px;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-dim);
}

.card-body {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 26px 24px 24px;
  flex: 1;
}

.app-icon {
  font-size: 32px;
  margin-bottom: 14px;
}

.app-card h2 {
  font-size: 18px;
  margin: 0 0 10px;
  color: var(--text-primary);
}

.app-card p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 18px;
  line-height: 1.6;
}

.card-cta {
  margin-top: auto;
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--accent-cyan);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.card-cta i {
  font-style: normal;
  transition: transform 0.2s ease;
}

.app-card:hover .card-cta i {
  transform: translateX(4px);
}

@media (max-width: 600px) {
  .home-page {
    padding: 56px 16px;
  }

  .home-hero {
    margin-bottom: 40px;
  }

  .app-grid {
    grid-template-columns: 1fr;
    max-width: 380px;
  }
}
</style>
