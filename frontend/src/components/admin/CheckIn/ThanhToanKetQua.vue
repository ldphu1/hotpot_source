<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const txnRef = computed(() => route.query.vnp_TxnRef || "");
const responseCode = computed(() => route.query.vnp_ResponseCode || "");

const thanhCong = computed(() => responseCode.value === "00");

function veCheckIn() {
  router.push("/admin/check-in");
}

function veQuanLyHoaDon() {
  router.push("/admin/quan-ly-hoa-don");
}
</script>

<template>
  <div class="ket-qua-page">
    <div class="ket-qua-card">
      <div class="icon" :class="{ success: thanhCong, fail: !thanhCong }">
        {{ thanhCong ? "✓" : "✕" }}
      </div>

      <h1>
        {{ thanhCong ? "Thanh toán thành công" : "Thanh toán thất bại" }}
      </h1>

      <p v-if="txnRef">
        Mã giao dịch: <strong>{{ txnRef }}</strong>
      </p>

      <p v-if="thanhCong">Hệ thống đã ghi nhận kết quả thanh toán VNPAY.</p>
      <p v-else>Giao dịch chưa thành công hoặc đã bị hủy.</p>

      <div class="actions">
        <button class="btn-primary" @click="veCheckIn">Về Check-in</button>
        <button class="btn-secondary" @click="veQuanLyHoaDon">
          Về Quản lý hóa đơn
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ket-qua-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  padding: 24px;
}

.ket-qua-card {
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 800;
}

.icon.success {
  background: #eafaf0;
  color: #16a34a;
}

.icon.fail {
  background: #fdecec;
  color: #dc2626;
}

h1 {
  font-size: 28px;
  margin-bottom: 12px;
  color: #0f172a;
}

p {
  color: #475569;
  margin-bottom: 10px;
  font-size: 15px;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary {
  border: none;
  padding: 12px 18px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.btn-primary {
  background: #dc2626;
  color: white;
}

.btn-secondary {
  background: #e2e8f0;
  color: #0f172a;
}
</style>
