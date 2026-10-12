<template>
  <div class="forbidden-wrapper">
    <div class="forbidden-content">

      <!-- LEFT: Robot section -->
      <div class="robot-section">
        <!-- Holo rings (static) -->
        <div class="holo-ring ring-outer"></div>
        <div class="holo-ring ring-middle"></div>
        <div class="holo-ring ring-inner"></div>

        <!-- 403 number -->
        <div class="error-num-wrapper">
          <div class="error-num">403</div>
        </div>

        <!-- Robot SVG -->
        <div class="robot-wrap">
          <svg class="robot" viewBox="0 0 200 245" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="bodyGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color: #1a2744" />
                <stop offset="100%" style="stop-color: #0b1120" />
              </linearGradient>
              <linearGradient id="bodyGrad2" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color: #141f38" />
                <stop offset="100%" style="stop-color: #080e1c" />
              </linearGradient>
              <linearGradient id="screenGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color: #ff3a3a; stop-opacity: 0.9" />
                <stop offset="100%" style="stop-color: #8b0000; stop-opacity: 0.5" />
              </linearGradient>
              <filter id="glow" x="-30%" y="-30%" width="160%" height="160%">
                <feGaussianBlur stdDeviation="2.5" result="coloredBlur" />
                <feMerge>
                  <feMergeNode in="coloredBlur" />
                  <feMergeNode in="SourceGraphic" />
                </feMerge>
              </filter>
              <filter id="strongGlow" x="-50%" y="-50%" width="200%" height="200%">
                <feGaussianBlur stdDeviation="4" result="coloredBlur" />
                <feMerge>
                  <feMergeNode in="coloredBlur" />
                  <feMergeNode in="SourceGraphic" />
                </feMerge>
              </filter>
            </defs>

            <!-- Antenna -->
            <rect x="97" y="18" width="6" height="14" rx="3" fill="#1a2744" stroke="#4fc3f7" stroke-width="1" />
            <line x1="100" y1="22" x2="118" y2="11" stroke="#4fc3f7" stroke-width="2.5" stroke-linecap="round" />
            <circle cx="120" cy="10" r="4" fill="#ff4757" filter="url(#strongGlow)" />

            <!-- HEAD -->
            <rect x="52" y="32" width="96" height="72" rx="14" fill="url(#bodyGrad)" stroke="#263d6b" stroke-width="2" />
            <rect x="56" y="33" width="88" height="4" rx="2" fill="rgba(79,195,247,0.08)" />

            <!-- Screen face -->
            <rect x="62" y="40" width="76" height="50" rx="7" fill="#05091a" stroke="#1a3055" stroke-width="1.5" />
            <rect x="62" y="40" width="76" height="50" rx="7" fill="url(#screenGrad)" opacity="0.12" />

            <!-- Left eye - X -->
            <g filter="url(#glow)">
              <line x1="78" y1="51" x2="90" y2="63" stroke="#ff4757" stroke-width="3.5" stroke-linecap="round" />
              <line x1="90" y1="51" x2="78" y2="63" stroke="#ff4757" stroke-width="3.5" stroke-linecap="round" />
            </g>
            <!-- Right eye - X -->
            <g filter="url(#glow)">
              <line x1="110" y1="51" x2="122" y2="63" stroke="#ff4757" stroke-width="3.5" stroke-linecap="round" />
              <line x1="122" y1="51" x2="110" y2="63" stroke="#ff4757" stroke-width="3.5" stroke-linecap="round" />
            </g>

            <!-- Sad mouth -->
            <path d="M78 76 Q100 68 122 76" stroke="#ff4757" stroke-width="2.5" fill="none" stroke-linecap="round" />

            <!-- Access denied text -->
            <text x="100" y="87" text-anchor="middle" fill="#ff4757" font-size="5.5" font-family="monospace" opacity="0.8" letter-spacing="1">ACCESS DENIED</text>

            <!-- Bolts -->
            <circle cx="58"  cy="68" r="3.5" fill="#1a2744" stroke="#4fc3f7" stroke-width="1" />
            <circle cx="142" cy="68" r="3.5" fill="#1a2744" stroke="#4fc3f7" stroke-width="1" />

            <!-- NECK -->
            <rect x="87" y="104" width="26" height="12" rx="4" fill="url(#bodyGrad2)" stroke="#263d6b" stroke-width="1.5" />
            <rect x="91" y="107" width="4" height="6" rx="2" fill="#4fc3f7" opacity="0.5" />
            <rect x="105" y="107" width="4" height="6" rx="2" fill="#4fc3f7" opacity="0.5" />

            <!-- BODY -->
            <rect x="42" y="116" width="116" height="82" rx="12" fill="url(#bodyGrad)" stroke="#263d6b" stroke-width="2" />
            <rect x="46" y="117" width="108" height="5" rx="3" fill="rgba(79,195,247,0.06)" />

            <!-- Chest panel -->
            <rect x="58" y="124" width="84" height="54" rx="7" fill="#05091a" stroke="#1a3055" stroke-width="1.5" />

            <!-- Warning triangle -->
            <polygon points="100,131 118,155 82,155" fill="none" stroke="#ffbe0b" stroke-width="2" />
            <text x="100" y="150" text-anchor="middle" fill="#ffbe0b" font-size="14" font-weight="bold">!</text>

            <!-- Status LEDs -->
            <circle cx="66"  cy="130" r="3.5" fill="#ff4757" filter="url(#glow)" />
            <circle cx="76"  cy="130" r="3.5" fill="#ffbe0b" filter="url(#glow)" />
            <circle cx="134" cy="130" r="3.5" fill="#4fc3f7" filter="url(#glow)" />
            <circle cx="124" cy="130" r="3.5" fill="#ff4757" filter="url(#glow)" />

            <!-- Circuit lines -->
            <line x1="66"  y1="133" x2="66"  y2="155" stroke="#ff4757" stroke-width="1" stroke-dasharray="3,3" opacity="0.4" />
            <line x1="134" y1="133" x2="134" y2="155" stroke="#4fc3f7" stroke-width="1" stroke-dasharray="3,3" opacity="0.4" />

            <!-- ARMS -->
            <rect x="15" y="118" width="25" height="62" rx="9" fill="url(#bodyGrad)" stroke="#263d6b" stroke-width="1.5" />
            <circle cx="27" cy="183" r="9" fill="#0b1120" stroke="#263d6b" stroke-width="1.5" />
            <line x1="27" y1="128" x2="27" y2="148" stroke="#4fc3f7" stroke-width="1" opacity="0.3" stroke-dasharray="2,3" />

            <rect x="160" y="120" width="25" height="58" rx="9" fill="url(#bodyGrad)" stroke="#ff4757" stroke-width="1.5" />
            <circle cx="172" cy="181" r="9" fill="#0b1120" stroke="#ff4757" stroke-width="1.5" />

            <!-- Sparks -->
            <circle cx="160" cy="148" r="2.5" fill="#ffbe0b" filter="url(#strongGlow)" />
            <circle cx="157" cy="154" r="2"   fill="#ff4757" filter="url(#strongGlow)" />
            <circle cx="163" cy="143" r="1.5" fill="#ffbe0b" filter="url(#strongGlow)" />
            <circle cx="156" cy="143" r="1"   fill="#ff4757" filter="url(#glow)" />

            <!-- Crack -->
            <path d="M100 116 L96 130 L102 138 L98 155" stroke="#ff4757" stroke-width="1" fill="none" opacity="0.3" stroke-dasharray="2,2" />

            <!-- LEGS -->
            <rect x="62"  y="198" width="30" height="38" rx="9" fill="url(#bodyGrad)" stroke="#263d6b" stroke-width="1.5" />
            <rect x="108" y="198" width="30" height="38" rx="9" fill="url(#bodyGrad)" stroke="#263d6b" stroke-width="1.5" />

            <!-- FEET -->
            <rect x="57"  y="230" width="40" height="13" rx="6" fill="#0b1120" stroke="#263d6b" stroke-width="1.5" />
            <rect x="103" y="230" width="40" height="13" rx="6" fill="#0b1120" stroke="#263d6b" stroke-width="1.5" />

            <!-- Shadows -->
            <ellipse cx="77"  cy="245" rx="22" ry="3" fill="rgba(255,71,87,0.18)" />
            <ellipse cx="123" cy="245" rx="22" ry="3" fill="rgba(255,71,87,0.18)" />
          </svg>

          <!-- Badges -->
          <div class="badge badge-err">⚠ ERR_FORBIDDEN</div>
          <div class="badge badge-lock">🔒 LOCKED</div>
          <div class="badge badge-code">HTTP · 403</div>
        </div>
      </div>

      <!-- RIGHT: Info section -->
      <div class="info-section">
        <!-- Status bar -->
        <div class="status-bar">
          <span class="status-dot"></span>
          <span class="status-label">SECURITY ALERT</span>
          <span class="status-divider">|</span>
          <span class="status-code">0xC000006D</span>
        </div>

        <!-- Title -->
        <h1 class="page-title">ACCESS DENIED!</h1>

        <!-- Subtitle -->
        <div class="subtitle-line">
          <span class="sub-icon">⛔</span>
          <span class="sub-text">You do not have permission to access this resource.</span>
        </div>

        <!-- Terminal -->
        <div class="terminal">
          <div class="terminal-header">
            <div class="term-dots">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
            </div>
            <span class="term-title">/ system / security.log</span>
            <span class="term-live"><span class="live-dot"></span>LIVE</span>
          </div>
          <div class="terminal-body">
            <div v-for="(line, i) in termLines" :key="i" class="term-line">
              <span class="term-time">{{ currentTime }}</span>
              <span :class="'term-tag term-' + line.type">[{{ line.tag }}]</span>
              <span class="term-msg">{{ line.msg }}</span>
            </div>
            <div class="term-line">
              <span class="term-time">{{ currentTime }}</span>
              <span class="term-tag term-sys">[SYS]</span>
              <span class="term-msg">Awaiting authorization...</span>
              <span class="term-cursor">█</span>
            </div>
          </div>
        </div>

        <!-- Progress bar (blocked) -->
        <div class="progress-section">
          <div class="progress-label">
            <span>ACCESS ROLE</span>
            <span class="progress-val">DENIED</span>
          </div>
          <div class="progress-track">
            <div class="progress-fill"></div>
            <div class="progress-blocked"></div>
          </div>
        </div>

        <!-- Buttons -->
        <div class="action-buttons">
          <button class="btn btn-back" @click="goBack">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M19 12H5M12 19l-7-7 7-7" />
            </svg>
            Quay lại
          </button>
          <button class="btn btn-home" @click="goHome">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
            Trang chủ
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const currentTime = ref("");

const termLines = ref([
  { type: "error", tag: "ERROR", msg: "HTTP 403 Forbidden — request rejected" },
  { type: "warn",  tag: "WARN",  msg: "Insufficient role: UNAUTHORIZED" },
  { type: "info",  tag: "INFO",  msg: "Request blocked by security policy" },
  {
    type: "sec",
    tag: "SEC",
    msg: "Event logged — ID#" + Math.random().toString(36).slice(2, 10).toUpperCase(),
  },
]);

let clockTimer = null;
function updateTime() {
  currentTime.value = new Date().toTimeString().slice(0, 8);
}

function goBack() { router.go(-1); }
function goHome()  {
  const role = localStorage.getItem("role");
  router.push(role === "CUSTOMER" ? "/" : "/admin");
}

onMounted(() => {
  updateTime();
  clockTimer = setInterval(updateTime, 1000);
});
onBeforeUnmount(() => clearInterval(clockTimer));
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Share+Tech+Mono&family=Inter:wght@300;400;600;700&display=swap");

*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

/* ══ WRAPPER ══ */
.forbidden-wrapper {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Inter", sans-serif;
  padding: 40px 24px;
}

/* ══ LAYOUT ══ */
.forbidden-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 64px;
  max-width: 1060px;
  width: 100%;
  flex-wrap: wrap;
}

/* ══ ROBOT SECTION ══ */
.robot-section {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

/* Holo rings — static */
.holo-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}
.ring-outer  { width: 360px; height: 360px; border-color: rgba(255,71,87,0.1); }
.ring-middle { width: 280px; height: 280px; border-color: rgba(79,195,247,0.12); }
.ring-inner  { width: 200px; height: 200px; border-color: rgba(255,190,11,0.08); }

/* Error number */
.error-num-wrapper { position: relative; z-index: 2; margin-bottom: 6px; }
.error-num {
  font-family: "Orbitron", monospace;
  font-size: clamp(52px, 7vw, 76px);
  font-weight: 900;
  color: #ff4757;
  text-shadow: 0 0 24px rgba(255,71,87,0.3);
  letter-spacing: 8px;
}

/* Robot */
.robot-wrap { position: relative; z-index: 2; }
.robot {
  width: 210px;
  height: auto;
  display: block;
  filter: drop-shadow(0 8px 24px rgba(255,71,87,0.15))
          drop-shadow(0 2px 8px rgba(0,0,0,0.1));
}

/* Badges */
.badge {
  position: absolute;
  background: #fff;
  border-radius: 20px;
  padding: 4px 12px;
  font-family: "Share Tech Mono", monospace;
  font-size: 11px;
  white-space: nowrap;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
}
.badge-err  {
  top: 14px; right: -40px;
  border: 1.5px solid rgba(255,71,87,0.35);
  color: #c53030;
}
.badge-lock {
  top: 74px; left: -48px;
  border: 1.5px solid rgba(255,190,11,0.4);
  color: #b7791f;
}
.badge-code {
  bottom: 56px; right: -28px;
  border: 1.5px solid rgba(255,71,87,0.25);
  color: #c53030;
}

/* ══ INFO SECTION ══ */
.info-section {
  flex: 1;
  min-width: 300px;
  max-width: 460px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* Status bar */
.status-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: "Share Tech Mono", monospace;
  font-size: 11px;
  letter-spacing: 1.5px;
}
.status-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #ff4757;
  box-shadow: 0 0 6px rgba(255,71,87,0.5);
  flex-shrink: 0;
}
.status-label  { color: #c53030; }
.status-divider { color: #fbb6b6; }
.status-code   { color: #e29090; }

/* Page title */
.page-title {
  font-family: "Orbitron", monospace;
  font-size: clamp(26px, 4vw, 40px);
  font-weight: 900;
  color: #1a2744;
  line-height: 1.2;
  letter-spacing: 2px;
}

/* Subtitle */
.subtitle-line {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(255,71,87,0.05);
  border-left: 3px solid #ff4757;
  border-radius: 0 8px 8px 0;
}
.sub-icon { font-size: 18px; flex-shrink: 0; margin-top: 1px; }
.sub-text {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.65;
}

/* Terminal */
.terminal {
  background: #0d1117;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}
.terminal-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 14px;
  background: #161b22;
  border-bottom: 1px solid #21262d;
}
.term-dots { display: flex; gap: 6px; }
.dot { width: 10px; height: 10px; border-radius: 50%; }
.dot.red    { background: #ff5f56; }
.dot.yellow { background: #ffbd2e; }
.dot.green  { background: #27c93f; }
.term-title {
  flex: 1;
  font-family: "Share Tech Mono", monospace;
  font-size: 11px;
  color: #6e7681;
}
.term-live {
  display: flex;
  align-items: center;
  gap: 5px;
  font-family: "Share Tech Mono", monospace;
  font-size: 10px;
  color: #ff4757;
  letter-spacing: 1px;
}
.live-dot { width: 6px; height: 6px; border-radius: 50%; background: #ff4757; }

.terminal-body { padding: 12px 14px; display: flex; flex-direction: column; gap: 5px; }
.term-line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-family: "Share Tech Mono", monospace;
  font-size: 11.5px;
  line-height: 1.55;
}
.term-time   { color: #444c56; min-width: 66px; flex-shrink: 0; }
.term-tag    { min-width: 58px; font-weight: bold; flex-shrink: 0; }
.term-error  { color: #ff7b72; }
.term-warn   { color: #e3b341; }
.term-info   { color: #58a6ff; }
.term-sec    { color: #ff7b72; }
.term-sys    { color: #6e7681; }
.term-msg    { color: #8b949e; }
.term-cursor { color: rgba(255,71,87,0.7); }

/* Progress bar */
.progress-section { display: flex; flex-direction: column; gap: 7px; }
.progress-label {
  display: flex;
  justify-content: space-between;
  font-family: "Share Tech Mono", monospace;
  font-size: 10.5px;
  letter-spacing: 1px;
  color: #718096;
}
.progress-val { color: #e53e3e; font-weight: bold; }
.progress-track {
  height: 6px;
  background: #f7f7f7;
  border: 1px solid #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
  display: flex;
}
.progress-fill {
  width: 32%;
  height: 100%;
  background: linear-gradient(90deg, #ff4757, #ff6b78);
  border-radius: 3px;
}
.progress-blocked {
  flex: 1;
  height: 100%;
  background: repeating-linear-gradient(
    90deg,
    rgba(255,71,87,0.15) 0px,
    rgba(255,71,87,0.15) 4px,
    transparent 4px,
    transparent 8px
  );
}

/* Buttons */
.action-buttons { display: flex; gap: 10px; flex-wrap: wrap; }
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  font-family: "Inter", sans-serif;
  cursor: pointer;
  border: none;
  transition: background 0.15s, box-shadow 0.15s, transform 0.1s;
}
.btn svg { width: 15px; height: 15px; flex-shrink: 0; }
.btn:active { transform: scale(0.97); }

.btn-back {
  background: #fff5f5;
  color: #c53030;
  border: 1.5px solid #fbb6b6;
}
.btn-back:hover {
  background: #fed7d7;
  border-color: #ff4757;
}

.btn-home {
  background: #ff4757;
  color: #fff;
}
.btn-home:hover {
  background: #e53e4f;
  box-shadow: 0 2px 12px rgba(255,71,87,0.4);
}

/* Responsive */
@media (max-width: 800px) {
  .forbidden-content { gap: 40px; }
  .ring-outer  { width: 260px; height: 260px; }
  .ring-middle { width: 200px; height: 200px; }
  .ring-inner  { width: 140px; height: 140px; }
  .badge       { display: none; }
  .info-section { min-width: 0; max-width: 100%; }
}

@media (max-width: 480px) {
  .robot { width: 170px; }
  .page-title { font-size: 24px; }
}
</style>
