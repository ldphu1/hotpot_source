<template>
  <div class="notfound-wrapper">
    <div class="notfound-content">

      <!-- LEFT: Robot section -->
      <div class="robot-section">
        <!-- Orbit rings -->
        <div class="orbit-ring orbit-1">
          <div class="orbit-dot dot-r1"></div>
        </div>
        <div class="orbit-ring orbit-2">
          <div class="orbit-dot dot-r2"></div>
        </div>

        <!-- 404 number -->
        <div class="error-num-wrapper">
          <div class="error-num">404</div>
        </div>

        <!-- Astronaut Robot SVG -->
        <div class="robot-wrap">
          <svg class="robot" viewBox="0 0 200 260" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="suitGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color: #1a2a4a" />
                <stop offset="100%" style="stop-color: #0c1828" />
              </linearGradient>
              <linearGradient id="helmetGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color: #1e3460" />
                <stop offset="100%" style="stop-color: #0a1530" />
              </linearGradient>
              <linearGradient id="visorGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color: #4fc3f7; stop-opacity: 0.35" />
                <stop offset="100%" style="stop-color: #0288d1; stop-opacity: 0.2" />
              </linearGradient>
              <filter id="glow">
                <feGaussianBlur stdDeviation="2.5" result="coloredBlur" />
                <feMerge>
                  <feMergeNode in="coloredBlur" />
                  <feMergeNode in="SourceGraphic" />
                </feMerge>
              </filter>
            </defs>

            <!-- Signal waves -->
            <circle cx="100" cy="75" r="55" fill="none" stroke="rgba(79,195,247,0.12)" stroke-width="1.5" />
            <circle cx="100" cy="75" r="70" fill="none" stroke="rgba(79,195,247,0.07)" stroke-width="1" />
            <circle cx="100" cy="75" r="85" fill="none" stroke="rgba(79,195,247,0.04)" stroke-width="1" />

            <!-- HELMET -->
            <circle cx="100" cy="68" r="46" fill="url(#helmetGrad)" stroke="#263d6b" stroke-width="2" />
            <ellipse cx="88" cy="50" rx="14" ry="8" fill="rgba(79,195,247,0.08)" transform="rotate(-20,88,50)" />

            <!-- Visor -->
            <ellipse cx="100" cy="68" rx="32" ry="30" fill="url(#visorGrad)" stroke="rgba(79,195,247,0.4)" stroke-width="1.5" />
            <ellipse cx="100" cy="68" rx="28" ry="26" fill="none" stroke="rgba(79,195,247,0.12)" stroke-width="1" />

            <!-- Question mark face -->
            <text x="100" y="62" text-anchor="middle" fill="rgba(79,195,247,0.6)" font-size="10" font-family="monospace" font-weight="bold">???</text>
            <!-- Eyes -->
            <circle cx="88"  cy="68" r="5" fill="#4fc3f7" opacity="0.8" filter="url(#glow)" />
            <circle cx="112" cy="68" r="5" fill="#4fc3f7" opacity="0.8" filter="url(#glow)" />
            <circle cx="86"  cy="66" r="2.5" fill="#020817" />
            <circle cx="110" cy="66" r="2.5" fill="#020817" />

            <!-- Worried mouth -->
            <path d="M90 80 Q100 75 110 80" stroke="rgba(79,195,247,0.6)" stroke-width="2" fill="none" stroke-linecap="round" />

            <!-- Antenna -->
            <line x1="100" y1="22" x2="100" y2="14" stroke="#263d6b" stroke-width="3" stroke-linecap="round" />
            <line x1="100" y1="14" x2="88"  y2="6"  stroke="#4fc3f7" stroke-width="2" stroke-linecap="round" />
            <line x1="84"  y1="4"  x2="88"  y2="6"  stroke="#ffbe0b" stroke-width="2" stroke-linecap="round" />
            <circle cx="84" cy="4" r="3" fill="none" stroke="#ffbe0b" stroke-width="1.5" />

            <!-- Helmet bolts -->
            <circle cx="58"  cy="60" r="4"   fill="#0c1828" stroke="#263d6b" stroke-width="1.5" />
            <circle cx="142" cy="60" r="4"   fill="#0c1828" stroke="#263d6b" stroke-width="1.5" />
            <circle cx="62"  cy="82" r="3.5" fill="#0c1828" stroke="#263d6b" stroke-width="1.5" />
            <circle cx="138" cy="82" r="3.5" fill="#0c1828" stroke="#263d6b" stroke-width="1.5" />

            <!-- NECK -->
            <rect x="88" y="114" width="24" height="12" rx="5" fill="#0c1828" stroke="#263d6b" stroke-width="1.5" />

            <!-- BODY -->
            <rect x="40" y="126" width="120" height="84" rx="14" fill="url(#suitGrad)" stroke="#263d6b" stroke-width="2" />
            <rect x="44" y="127" width="112" height="5" rx="3" fill="rgba(79,195,247,0.06)" />

            <!-- Chest control panel -->
            <rect x="58" y="136" width="84" height="54" rx="8" fill="#07102a" stroke="#1a3055" stroke-width="1.5" />

            <!-- WiFi bars -->
            <path d="M85 168 Q100 152 115 168" stroke="rgba(79,195,247,0.2)"  stroke-width="2.5" fill="none" stroke-linecap="round" />
            <path d="M90 162 Q100 153 110 162" stroke="rgba(79,195,247,0.35)" stroke-width="2.5" fill="none" stroke-linecap="round" />
            <path d="M95 156 Q100 151 105 156" stroke="rgba(79,195,247,0.5)"  stroke-width="2.5" fill="none" stroke-linecap="round" />
            <circle cx="100" cy="170" r="3.5" fill="rgba(79,195,247,0.6)" />

            <!-- NO SIGNAL text -->
            <text x="100" y="183" text-anchor="middle" fill="rgba(79,195,247,0.4)" font-size="5.5" font-family="monospace" letter-spacing="1.5">NO SIGNAL</text>

            <!-- Status LEDs -->
            <circle cx="66"  cy="142" r="3.5" fill="#ffbe0b" filter="url(#glow)" />
            <circle cx="76"  cy="142" r="3.5" fill="rgba(79,195,247,0.25)" />
            <circle cx="124" cy="142" r="3.5" fill="rgba(79,195,247,0.25)" />
            <circle cx="134" cy="142" r="3.5" fill="#4fc3f7" filter="url(#glow)" />

            <!-- ARMS -->
            <rect x="10" y="108" width="28" height="58" rx="10" fill="url(#suitGrad)" stroke="#263d6b" stroke-width="1.5" />
            <circle cx="24" cy="168" r="10" fill="#07102a" stroke="#263d6b" stroke-width="1.5" />
            <rect x="162" y="116" width="28" height="58" rx="10" fill="url(#suitGrad)" stroke="#263d6b" stroke-width="1.5" />
            <circle cx="176" cy="176" r="10" fill="#07102a" stroke="#263d6b" stroke-width="1.5" />

            <!-- Suit detail lines -->
            <line x1="24"  y1="118" x2="24"  y2="138" stroke="#4fc3f7" stroke-width="1" opacity="0.2" stroke-dasharray="2,3" />
            <line x1="176" y1="126" x2="176" y2="146" stroke="#4fc3f7" stroke-width="1" opacity="0.2" stroke-dasharray="2,3" />

            <!-- LEGS -->
            <rect x="58"  y="210" width="32" height="40" rx="10" fill="url(#suitGrad)" stroke="#263d6b" stroke-width="1.5" />
            <rect x="110" y="210" width="32" height="40" rx="10" fill="url(#suitGrad)" stroke="#263d6b" stroke-width="1.5" />

            <!-- Boots -->
            <rect x="52"  y="244" width="44" height="14" rx="7" fill="#07102a" stroke="#263d6b" stroke-width="1.5" />
            <rect x="104" y="244" width="44" height="14" rx="7" fill="#07102a" stroke="#263d6b" stroke-width="1.5" />

            <!-- Foot shadows -->
            <ellipse cx="74"  cy="260" rx="24" ry="3" fill="rgba(79,195,247,0.12)" />
            <ellipse cx="126" cy="260" rx="24" ry="3" fill="rgba(79,195,247,0.12)" />

            <!-- Debris -->
            <rect   x="28" y="100" width="6" height="6" rx="1" fill="rgba(79,195,247,0.2)"  transform="rotate(20,31,103)" />
            <circle cx="170" cy="105" r="3" fill="rgba(255,190,11,0.25)" />
            <rect   x="150" y="200" width="4" height="4" rx="1" fill="rgba(79,195,247,0.15)" transform="rotate(45,152,202)" />
            <circle cx="38"  cy="190" r="2" fill="rgba(255,190,11,0.18)" />
          </svg>

          <!-- Badges -->
          <div class="badge badge-signal">📡 NO SIGNAL</div>
          <div class="badge badge-lost">🔍 NOT FOUND</div>
          <div class="badge badge-code">HTTP · 404</div>
        </div>
      </div>

      <!-- RIGHT: Info section -->
      <div class="info-section">
        <!-- Status bar -->
        <div class="status-bar">
          <span class="status-dot"></span>
          <span class="status-label">LOST IN SPACE</span>
          <span class="status-divider">|</span>
          <span class="status-code">0x80070002</span>
        </div>

        <!-- Title -->
        <h1 class="page-title">NOT FOUND!</h1>

        <!-- Subtitle -->
        <div class="subtitle-line">
          <span class="sub-icon">🛸</span>
          <span class="sub-text">The resource you are looking for does not exist or has been removed.</span>
        </div>

        <!-- Terminal -->
        <div class="terminal">
          <div class="terminal-header">
            <div class="term-dots">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
            </div>
            <span class="term-title">/ system / network.log</span>
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
              <span class="term-msg">Scanning endpoint...</span>
              <span class="term-cursor">█</span>
            </div>
          </div>
        </div>

        <!-- Ping -->
        <div class="ping-section">
          <div class="ping-label">
            <span>ENDPOINT PING</span>
            <span class="ping-val">TIMEOUT</span>
          </div>
          <div class="ping-bars">
            <div v-for="n in 12" :key="n" class="ping-bar" :class="{ lost: n > 4 }"></div>
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

const router  = useRouter();
const currentTime = ref("");

const termLines = ref([
  { type: "error", tag: "ERROR", msg: "HTTP 404 Not Found — endpoint missing" },
  { type: "warn",  tag: "WARN",  msg: "Resource does not exist on server" },
  { type: "info",  tag: "INFO",  msg: "Route resolved: NULL — no handler found" },
  { type: "sec",   tag: "NET",   msg: "Retry x3 failed — aborting request" },
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
.notfound-wrapper {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Inter", sans-serif;
  padding: 40px 24px;
}

/* ══ LAYOUT ══ */
.notfound-content {
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

/* Orbit rings — static */
.orbit-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px dashed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}
.orbit-1 { width: 320px; height: 320px; border-color: rgba(79,195,247,0.18); }
.orbit-2 { width: 400px; height: 400px; border-color: rgba(79,195,247,0.09); }

.orbit-dot { position: absolute; border-radius: 50%; }
.dot-r1 {
  width: 8px; height: 8px;
  background: #4fc3f7;
  box-shadow: 0 0 8px #4fc3f7;
  top: 50%; left: -4px;
  transform: translateY(-50%);
}
.dot-r2 {
  width: 6px; height: 6px;
  background: rgba(79,195,247,0.6);
  top: 0; left: 50%;
  transform: translateX(-50%) translateY(-3px);
}

/* Error number */
.error-num-wrapper { position: relative; z-index: 2; margin-bottom: 6px; }
.error-num {
  font-family: "Orbitron", monospace;
  font-size: clamp(52px, 7vw, 76px);
  font-weight: 900;
  color: #4fc3f7;
  text-shadow: 0 0 24px rgba(79,195,247,0.35);
  letter-spacing: 8px;
}

/* Robot */
.robot-wrap { position: relative; z-index: 2; }
.robot {
  width: 210px;
  height: auto;
  display: block;
  filter: drop-shadow(0 8px 24px rgba(79,195,247,0.18))
          drop-shadow(0 2px 8px rgba(0,0,0,0.12));
}

/* Badges */
.badge {
  position: absolute;
  background: #fff;
  border: 1.5px solid rgba(79,195,247,0.35);
  border-radius: 20px;
  padding: 4px 12px;
  font-family: "Share Tech Mono", monospace;
  font-size: 11px;
  color: #2b7cb5;
  white-space: nowrap;
  box-shadow: 0 2px 10px rgba(79,195,247,0.12);
}
.badge-signal { top: 14px;   right: -36px; }
.badge-lost   { top: 74px;   left: -44px; }
.badge-code   { bottom: 56px; right: -24px; }

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
  color: #4fc3f7;
}
.status-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #4fc3f7;
  box-shadow: 0 0 6px #4fc3f7;
  flex-shrink: 0;
}
.status-label  { color: #2b7cb5; }
.status-divider { color: #b0d8f0; }
.status-code   { color: #8db8d0; }

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
  background: rgba(79,195,247,0.06);
  border-left: 3px solid #4fc3f7;
  border-radius: 0 8px 8px 0;
}
.sub-icon { font-size: 18px; flex-shrink: 0; margin-top: 1px; }
.sub-text {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.65;
}

/* ── Terminal ── */
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
  color: #4fc3f7;
  letter-spacing: 1px;
}
.live-dot { width: 6px; height: 6px; border-radius: 50%; background: #4fc3f7; }

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
.term-sec    { color: #79c0ff; }
.term-sys    { color: #6e7681; }
.term-msg    { color: #8b949e; }
.term-cursor { color: rgba(79,195,247,0.7); }

/* Ping */
.ping-section { display: flex; flex-direction: column; gap: 7px; }
.ping-label {
  display: flex;
  justify-content: space-between;
  font-family: "Share Tech Mono", monospace;
  font-size: 10.5px;
  letter-spacing: 1px;
  color: #718096;
}
.ping-val { color: #e53e3e; font-weight: bold; }
.ping-bars { display: flex; gap: 3px; align-items: flex-end; height: 26px; }
.ping-bar {
  flex: 1;
  border-radius: 2px;
  background: #4fc3f7;
  height: 100%;
}
.ping-bar:nth-child(1) { height: 30%; }
.ping-bar:nth-child(2) { height: 55%; }
.ping-bar:nth-child(3) { height: 80%; }
.ping-bar:nth-child(4) { height: 100%; }
.ping-bar.lost { background: #fed7d7; }

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
  background: #f0f9ff;
  color: #2b7cb5;
  border: 1.5px solid #b0d8f0;
}
.btn-back:hover {
  background: #e0f2fe;
  border-color: #4fc3f7;
}

.btn-home {
  background: #4fc3f7;
  color: #fff;
}
.btn-home:hover {
  background: #29b6f6;
  box-shadow: 0 2px 12px rgba(79,195,247,0.4);
}

/* ── Responsive ── */
@media (max-width: 800px) {
  .notfound-content { gap: 40px; }
  .orbit-1 { width: 250px; height: 250px; }
  .orbit-2 { width: 310px; height: 310px; }
  .badge   { display: none; }
  .info-section { min-width: 0; max-width: 100%; }
}

@media (max-width: 480px) {
  .robot { width: 170px; }
  .page-title { font-size: 24px; }
}
</style>
