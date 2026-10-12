<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const isCancelledAfterPayment = computed(
  () => route.query.status === "cancelled_after_payment"
);
const orderId = computed(() => route.query.orderId);
</script>

<template>
  <div class="page">
    <div class="card">
      <!-- Icon -->
      <div
        class="icon-wrap"
        :class="{ 'icon-wrap--warning': isCancelledAfterPayment }"
      >
        <svg
          v-if="!isCancelledAfterPayment"
          viewBox="0 0 24 24"
          class="check-icon"
          aria-hidden="true"
        >
          <polyline points="20 6 9 17 4 12" />
        </svg>
        <svg
          v-else
          viewBox="0 0 24 24"
          class="warning-icon"
          aria-hidden="true"
        >
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
      </div>

      <!-- Case 1: Bình thường (Thành công & Đã xác nhận) -->
      <template v-if="!isCancelledAfterPayment">
        <h1 class="title">Thanh toán thành công</h1>
        <p class="subtitle">
          Đặt bàn của bạn đã được xác nhận. Thông tin chi tiết đã được gửi đến
          email của bạn.
        </p>

        <hr class="divider" />

        <div class="summary">
          <div class="summary-row">
            <span class="summary-label">Mã đặt bàn</span>
            <span class="summary-value">#{{ orderId || "---" }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Trạng thái</span>
            <span class="summary-value success-tag">Đã xác nhận</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Thông báo</span>
            <span class="summary-value">Gửi qua Email</span>
          </div>
        </div>
      </template>

      <!-- Case 2: Đã trừ tiền cọc nhưng đơn đã bị hủy/quá hạn trước đó -->
      <template v-else>
        <h1 class="title title--warning">Ghi nhận thanh toán cọc</h1>
        <p class="subtitle">
          Giao dịch thanh toán cọc đã hoàn tất. Tuy nhiên, phiếu đặt bàn này đã bị hủy hoặc quá thời gian giữ chỗ trước đó.
        </p>

        <div class="alert-box">
          <div class="alert-title">⚠️ Lưu ý quan trọng:</div>
          <div>
            Đơn của quý khách đã được chuyển sang trạng thái <strong>Chờ hoàn cọc</strong>. Nhà hàng sẽ chủ động liên hệ qua SĐT để hỗ trợ hoàn tiền cọc trong thời gian sớm nhất!
          </div>
        </div>

        <hr class="divider" />

        <div class="summary">
          <div class="summary-row">
            <span class="summary-label">Mã đặt bàn</span>
            <span class="summary-value">#{{ orderId || "---" }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Trạng thái đơn</span>
            <span class="summary-value warning-tag">Chờ hoàn cọc</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Hướng xử lý</span>
            <span class="summary-value">Nhà hàng liên hệ hoàn tiền</span>
          </div>
        </div>
      </template>

      <hr class="divider" />

      <!-- Actions -->
      <div class="actions">
        <router-link to="/" class="btn btn-primary">
          Về trang chủ
        </router-link>
      </div>

      <!-- Support -->
      <p class="support">
        Cần hỗ trợ trực tiếp? Gọi Hotline
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
  max-width: 460px;
  background: #ffffff;
  border-radius: 16px;
  padding: 40px 36px;
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
  margin: 0 auto 24px;
  border: 1.5px solid #d1f0dd;
}

.icon-wrap--warning {
  background: #fffbeb;
  border-color: #fde68a;
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

.warning-icon {
  width: 28px;
  height: 28px;
  stroke: #d97706;
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

.title--warning {
  color: #b45309;
}

.subtitle {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.65;
  margin: 0;
}

.alert-box {
  background: #fffbeb;
  border: 1px solid #fef3c7;
  border-radius: 10px;
  padding: 12px 16px;
  margin-top: 16px;
  font-size: 13px;
  color: #92400e;
  line-height: 1.55;
  text-align: left;
}

.alert-title {
  font-weight: 700;
  margin-bottom: 4px;
  color: #b45309;
}

/* ---- Divider ---- */
.divider {
  border: none;
  border-top: 1px solid #f0f0f0;
  margin: 24px 0;
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

.warning-tag {
  color: #d97706;
  background: #fffbeb;
  border: 1px solid #fde68a;
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

/* ---- Support ---- */
.support {
  margin: 20px 0 0;
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
