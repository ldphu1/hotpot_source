<template>
  <div class="page">
    <div class="card">

      <!-- Icon -->
      <div class="icon-wrap">
        <svg viewBox="0 0 24 24" class="x-icon" aria-hidden="true">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </div>

      <!-- Text -->
      <h1 class="title">Thanh toán thất bại</h1>
      <p class="subtitle">
        Giao dịch không thể hoàn tất. Vui lòng kiểm tra lại thông tin hoặc thử lại sau ít phút.
      </p>

      <!-- Divider -->
      <hr class="divider" />

      <!-- Summary rows -->
      <div class="summary">
        <div class="summary-row">
          <span class="summary-label">Trạng thái</span>
          <span class="summary-value error-tag">Thất bại</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">Mã lỗi</span>
          <span class="summary-value">ERR_PAYMENT_001</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">Nguyên nhân</span>
          <span class="summary-value">Kết nối ngân hàng thất bại</span>
        </div>
      </div>

      <hr class="divider" />

      <!-- Actions -->
      <div class="actions">
        <a href="javascript:void(0)" @click="$router.go(-1)" class="btn btn-primary">
          Thử lại giao dịch
        </a>
        <router-link to="/" class="btn btn-secondary">
          Về trang chủ
        </router-link>
      </div>

      <!-- Support -->
      <p class="support">
        Cần hỗ trợ? Gọi <a href="tel:0332654198" class="support-link">0332 654 198</a>
      </p>

    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  padding: 24px;
}

.card {
  width: 100%;
  max-width: 440px;
  background: #ffffff;
  border-radius: 16px;
  padding: 48px 40px 40px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08), 0 8px 24px rgba(0,0,0,0.06);
  text-align: center;
}

/* ---- Icon ---- */
.icon-wrap {
  width: 64px;
  height: 64px;
  background: #fff5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 28px;
  border: 1.5px solid #fecaca;
}

.x-icon {
  width: 26px;
  height: 26px;
  stroke: #dc2626;
  stroke-width: 2.5;
  stroke-linecap: round;
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

.error-tag {
  color: #dc2626;
  background: #fff5f5;
  border: 1px solid #fecaca;
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
  cursor: pointer;
  transition: background 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}

.btn-primary {
  background: #111827;
  color: #ffffff;
  border: none;
}

.btn-primary:hover {
  background: #1f2937;
}

.btn-secondary {
  background: #ffffff;
  color: #374151;
  border: 1.5px solid #e5e7eb;
}

.btn-secondary:hover {
  background: #f9fafb;
  border-color: #d1d5db;
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
