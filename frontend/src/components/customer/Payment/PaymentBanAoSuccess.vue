<template>
  <div class="page">
    <div class="card">
      <!-- Icon -->
      <div class="icon-wrap">
        <svg viewBox="0 0 24 24" class="check-icon" aria-hidden="true">
          <polyline points="20 6 9 17 4 12" />
        </svg>
      </div>

      <!-- Text -->
      <h1 class="title">Thanh toán nợ thành công</h1>
      <p class="subtitle">
        Hóa đơn treo của bạn đã được thanh toán đầy đủ. Thông tin chi tiết đã
        được gửi đến email của bạn.
      </p>

      <!-- Divider -->
      <hr class="divider" />

      <!-- Summary rows -->
      <div class="summary">
        <div class="summary-row">
          <span class="summary-label">Trạng thái</span>
          <span class="summary-value success-tag">Đã thanh toán</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">Thông báo</span>
          <span class="summary-value">Gửi qua Email</span>
        </div>
      </div>

      <hr class="divider" />

      <!-- Actions -->
      <div class="actions">
        <router-link to="/" class="btn btn-primary"> Về trang chủ </router-link>
      </div>

      <!-- Support -->
      <p class="support">
        Cần hỗ trợ? Gọi
        <a href="tel:0332654198" class="support-link">0332 654 198</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap");

.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  font-family: "Inter", -apple-system, BlinkMacSystemFont, sans-serif;
  padding: 24px;
}

.card {
  width: 100%;
  max-width: 440px;
  background: #ffffff;
  border-radius: 16px;
  padding: 48px 40px 40px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08), 0 8px 24px rgba(0, 0, 0, 0.06);
  text-align: center;
}

/* ---- Icon ---- */
.icon-wrap {
  width: 64px;
  height: 64px;
  background: #f0faf4;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 28px;
  border: 1.5px solid #d1f0dd;
}

.check-icon {
  width: 28px;
  height: 28px;
  stroke: #1a9e52;
  stroke-width: 2.5;
  stroke-linecap: round;
  stroke-linejoin: round;
  fill: none;
}

/* ---- Typography ---- */
.title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 12px;
  letter-spacing: -0.3px;
}

.subtitle {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.65;
  margin: 0;
}

/* ---- Divider ---- */
.divider {
  border: none;
  border-top: 1px solid #f0f0f0;
  margin: 28px 0;
}

/* ---- Summary ---- */
.summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
  text-align: left;
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-label {
  font-size: 13.5px;
  color: #9ca3af;
  font-weight: 500;
}

.summary-value {
  font-size: 13.5px;
  color: #374151;
  font-weight: 600;
}

.success-tag {
  color: #1a9e52;
  background: #f0faf4;
  border: 1px solid #d1f0dd;
  border-radius: 6px;
  padding: 2px 10px;
  font-size: 12.5px;
}

/* ---- Buttons ---- */
.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn {
  display: block;
  padding: 13px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}

.btn-primary {
  background: #f23232;
  color: #ffffff;
}

.btn-primary:hover {
  background: #d62929;
}

.btn-secondary {
  background: #ffffff;
  color: #374151;
  border: 1.5px solid #e5e7eb;
}

.btn-secondary:hover {
  background: #f9fafb;
  border-color: #dbd1d1;
  color: red;
}

/* ---- Support ---- */
.support {
  margin: 24px 0 0;
  font-size: 13px;
  color: #9ca3af;
}

.support-link {
  color: #374151;
  font-weight: 600;
  text-decoration: none;
}

.support-link:hover {
  text-decoration: underline;
}
</style>
