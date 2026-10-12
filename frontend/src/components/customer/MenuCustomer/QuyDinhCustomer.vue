<script setup>
import { getAllThamSoDong } from "@/api/admin/thamsodong/thamSoDongApi";
import { getAllThamSoCustomer } from "@/api/customer/datbanonline/datBanOnlineApi";
import { ref, computed } from "vue";

import { onMounted } from "vue";

const formatCurrency = (val) => {
  if (!val) return "0";
  return Number(val).toLocaleString("vi-VN");
};

const getThamSo = (key) => {
  return danhSachThamSo.value.find((x) => x.maThamSo === key)?.giaTri || "";
};

const rules = computed(() => {
  if (danhSachThamSo.value.length === 0) return [];

  return [
    {
      id: "booking",
      title: "Đặt Bàn & Giờ Phục Vụ",
      icon: "bi bi-clock-history",
      color: "#e65a5a",
      items: [
        {
          subtitle: "Giờ hoạt động",
          content: `Nhà hàng mở cửa đón khách từ <strong>${getThamSo(
            "THOI_GIAN_MO_CUA",
          )} đến ${getThamSo(
            "THOI_GIAN_DONG_CUA",
          )}</strong> tất cả các ngày trong tuần (kể cả Lễ, Tết).`,
        },
        {
          subtitle: "Thời gian đặt trước",
          content: `Quý khách vui lòng đặt bàn trước ít nhất <strong>${getThamSo(
            "DAT_TRUOC_TOI_THIEU",
          )} phút</strong> và tối đa <strong>${getThamSo(
            "DAT_TRUOC_TOI_DA_NGAY",
          )} ngày</strong> qua hệ thống trực tuyến hoặc hotline.`,
        },
        {
          subtitle: "Thời gian sử dụng bàn",
          content: `Mỗi lượt đặt bàn có thời gian sử dụng tiêu chuẩn là <strong>${getThamSo(
            "THOI_GIAN_SU_DUNG_BAN_MAC_DINH",
          )} phút</strong>.`,
        },
        {
          subtitle: "Phụ thu số lượng ghế",
          content: `Số ghế tối đa được xếp dư so với yêu cầu đặt bàn là <strong>${getThamSo(
            "SO_GHE_DU_TOI_DA",
          )} ghế</strong>.`,
        },
        {
          subtitle: "Hỗ trợ khách hàng",
          content: `Mọi thay đổi thông tin vui lòng liên hệ hotline: <strong>${getThamSo(
            "HOTLINE",
          )}</strong>.`,
        },
      ],
    },
    {
      id: "deposit",
      title: "Chính Sách Đặt Cọc",
      icon: "bi bi-credit-card-2-front",
      color: "#c5a059",
      items: [
        {
          subtitle: "Yêu cầu đặt cọc món trước",
          content: `Nếu Quý khách chọn đặt món ăn trước (Pre-order), vui lòng thanh toán đặt cọc <strong>${getThamSo(
            "PHAN_TRAM_COC",
          )}% giá trị đơn hàng</strong> tạm tính.`,
        },
        {
          subtitle: "Thời hạn hoàn tất đặt cọc",
          content: `Giao dịch cọc cần được hoàn tất trong vòng <strong>${getThamSo(
            "THOI_GIAN_CHO_THANH_TOAN_COC",
          )} phút</strong> kể từ khi tạo đơn. Quá thời gian này, hệ thống sẽ tự động hủy đơn đặt món và giải phóng bàn.`,
        },
        {
          subtitle: "Giới hạn gọi món đặt trước",
          content: `Để đảm bảo chất lượng, mỗi đơn đặt trước chỉ được gọi tối đa <strong>${getThamSo(
            "MAX_LAU",
          )} lẩu, ${getThamSo("MAX_DO_NHUNG_LAU")} đồ nhúng lẩu, ${getThamSo(
            "MAX_DO_AN_KEM",
          )} đồ ăn kèm, ${getThamSo("MAX_DO_UONG")} đồ uống, và ${getThamSo(
            "MAX_DO_TRANG_MIENG",
          )} đồ tráng miệng</strong>.`,
        },
      ],
    },
    {
      id: "cancellation",
      title: "Trễ Hẹn & Hủy Bàn",
      icon: "bi bi-calendar-x",
      color: "#e65a5a",
      items: [
        {
          subtitle: "Thời gian giữ bàn (Grace Period)",
          content: `Nhà hàng sẽ giữ bàn cho Quý khách tối đa <strong>${getThamSo(
            "THOI_GIAN_GIU_BAN",
          )} phút</strong> so với giờ hẹn trên phiếu. Quá ${getThamSo(
            "THOI_GIAN_GIU_BAN",
          )} phút trễ, hệ thống sẽ tự động hủy bàn.`,
        },
        {
          subtitle: "Hoàn cọc khi hủy bàn",
          content: `Hủy bàn trước giờ hẹn <strong>${getThamSo(
            "THOI_GIAN_HUY_HOAN_COC",
          )} phút</strong>: hoàn trả 100% tiền cọc. Hủy trễ hơn: tiền cọc không được hoàn lại.`,
        },
      ],
    },
    {
      id: "member",
      title: "Thành Viên & Điểm Thưởng",
      icon: "bi bi-star",
      color: "#c5a059",
      items: [
        {
          subtitle: "Tích lũy điểm thưởng",
          content: `Tỉ lệ tích điểm cho mỗi hóa đơn thanh toán là <strong>${getThamSo(
            "TI_LE_TICH_DIEM",
          )}%</strong>.`,
        },
        {
          subtitle: "Xếp hạng thành viên",
          content: `Ngưỡng nâng hạng: Hạng Bạc (<strong>${formatCurrency(
            getThamSo("NGUONG_HANG_BAC"),
          )}đ</strong>), Hạng Vàng (<strong>${formatCurrency(
            getThamSo("NGUONG_HANG_VANG"),
          )}đ</strong>), Hạng Kim Cương (<strong>${formatCurrency(
            getThamSo("NGUONG_HANG_KIM_CUONG"),
          )}đ</strong>).`,
        },
      ],
    },
  ];
});

const danhSachThamSo = ref([]);

const loadThamSo = async () => {
  try {
    const res = await getAllThamSoCustomer();
    danhSachThamSo.value = res.data;
  } catch (error) {
    console.log("LỖI THAM SỐ", error);
  }
};

onMounted(() => {
  loadThamSo();
});

const activeTab = ref("all");
const searchQuery = ref("");

const filteredRules = computed(() => {
  let result = rules.value;

  if (activeTab.value !== "all") {
    result = result.filter((r) => r.id === activeTab.value);
  }

  if (searchQuery.value.trim() !== "") {
    const q = searchQuery.value.toLowerCase().trim();
    result = result
      .map((category) => {
        const matchedItems = category.items.filter(
          (item) =>
            item.subtitle.toLowerCase().includes(q) ||
            item.content.toLowerCase().includes(q),
        );
        if (
          matchedItems.length > 0 ||
          category.title.toLowerCase().includes(q)
        ) {
          return {
            ...category,
            items: matchedItems.length > 0 ? matchedItems : category.items,
          };
        }
        return null;
      })
      .filter((category) => category !== null);
  }

  return result;
});

// Interactive Calculator State
const billAmount = ref(1000000);

const calculatedDeposit = computed(() => {
  const ptCoc = Number(getThamSo("PHAN_TRAM_COC")) || 20;
  return Math.round(billAmount.value * (ptCoc / 100));
});

const openChatBot = () => {
  window.dispatchEvent(new Event("open-chatbot"));
};
</script>

<template>
  <main class="rules-page py-5">
    <div class="container">
      <!-- Header -->
      <header class="text-center mb-5 reveal active">
        <span class="sub-title">chính sách nhà hàng</span>
        <h2 class="section-title">Quy Định & Hướng Dẫn</h2>
        <div class="luxury-line"></div>
        <p class="section-desc max-w-2xl mx-auto text-muted">
          Để đảm bảo chất lượng dịch vụ thượng hạng và mang lại trải nghiệm ẩm
          thực trọn vẹn nhất cho toàn bộ thực khách, Four Spice Hotpot xin gửi
          đến Quý khách các quy định và chính sách hoạt động của chúng tôi.
        </p>
      </header>

      <!-- Search & Filters -->
      <div class="filters-section mb-5 p-4 rounded-4 shadow-sm bg-white border">
        <div class="row align-items-center g-3">
          <!-- Search Bar -->
          <div class="col-12 col-md-5">
            <div class="search-box-wrapper">
              <i class="bi bi-search search-icon"></i>
              <input
                v-model="searchQuery"
                type="text"
                class="form-control search-input"
                placeholder="Tìm nhanh quy định"
              />
            </div>
          </div>
          <!-- Tab Filters -->
          <div class="col-12 col-md-7">
            <div class="d-flex flex-wrap gap-2 justify-content-md-end">
              <button
                class="tab-btn"
                :class="{ active: activeTab === 'all' }"
                @click="activeTab = 'all'"
              >
                Tất cả
              </button>
              <button
                v-for="cat in rules"
                :key="cat.id"
                class="tab-btn"
                :class="{ active: activeTab === cat.id }"
                @click="activeTab = cat.id"
              >
                {{ cat.title.split(" & ")[0] }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Rules Grid -->
      <div class="row g-4">
        <!-- Rules List Column -->
        <div class="col-lg-8">
          <div
            v-if="filteredRules.length === 0"
            class="no-results p-5 text-center bg-white rounded-4 border"
          >
            <i class="bi bi-search-heart text-gold display-4 mb-3"></i>
            <h5>Không tìm thấy quy định phù hợp</h5>
            <p class="text-muted">
              Vui lòng thử từ khóa khác hoặc xóa bộ lọc tìm kiếm.
            </p>
            <button
              class="btn btn-outline-gold mt-3"
              @click="
                searchQuery = '';
                activeTab = 'all';
              "
            >
              Đặt lại bộ lọc
            </button>
          </div>

          <div v-else class="rules-list d-flex flex-column gap-4">
            <div
              v-for="category in filteredRules"
              :key="category.id"
              class="category-card p-4 p-md-5 bg-white rounded-4 border position-relative overflow-hidden shadow-sm"
            >
              <!-- Background Icon subtle watermark -->
              <div class="card-watermark">
                <i :class="category.icon"></i>
              </div>

              <div class="category-header d-flex align-items-center gap-3 mb-4">
                <div
                  class="icon-box"
                  :style="{
                    backgroundColor: category.color + '15',
                    color: category.color,
                  }"
                >
                  <i :class="category.icon"></i>
                </div>
                <h4 class="category-title mb-0">{{ category.title }}</h4>
              </div>

              <div class="rules-sub-list d-flex flex-column gap-4">
                <div
                  v-for="(item, idx) in category.items"
                  :key="idx"
                  class="rule-sub-item pb-3"
                  :class="{
                    'border-bottom-dashed': idx !== category.items.length - 1,
                  }"
                >
                  <h5 class="rule-subtitle d-flex align-items-center gap-2">
                    <span
                      class="bullet"
                      :style="{ backgroundColor: category.color }"
                    ></span>
                    {{ item.subtitle }}
                  </h5>
                  <p
                    class="rule-text text-muted mb-0"
                    v-html="item.content"
                  ></p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Sticky Sidebar Column (Calculator & Quick Summary) -->
        <div class="col-lg-4">
          <div
            class="sidebar-wrapper d-flex flex-column gap-4 sticky-top"
            style="top: 100px"
          >
            <!-- Deposit & Surcharge Quick Calculator -->
            <div
              class="luxury-card calculator-card p-4 rounded-4 bg-white border shadow-sm"
            >
              <div class="card-header-luxury mb-3">
                <span class="sub-title mb-1">tiện ích thực khách</span>
                <h5 class="form-section-label mb-0" style="font-size: 1.05rem">
                  Công Cụ Tính Nhanh
                </h5>
              </div>

              <!-- Calculator: Deposit Estimate -->
              <div class="calc-section">
                <label
                  class="form-label-gold mb-2 d-flex justify-content-between"
                >
                  <span>Ước tính giá trị đơn ăn</span>
                  <span class="fw-bold text-danger"
                    >{{ billAmount.toLocaleString() }}đ</span
                  >
                </label>
                <input
                  v-model.number="billAmount"
                  type="range"
                  min="200000"
                  max="10000000"
                  step="100000"
                  class="form-range custom-range"
                />
                <div
                  class="calc-result mt-3 p-3 rounded-3 d-flex justify-content-between align-items-center"
                >
                  <span class="small-title">Tiền đặt cọc (20%):</span>
                  <span class="result-price text-danger fw-bold"
                    >{{ calculatedDeposit.toLocaleString() }}đ</span
                  >
                </div>
                <div
                  class="deposit-deadline-hint mt-2 text-muted text-start"
                  style="font-size: 0.8rem"
                >
                  <i class="bi bi-info-circle-fill text-gold me-1"></i>
                  Cọc trong <strong>15 phút</strong> sau khi đặt bàn trực tuyến.
                </div>
              </div>
            </div>

            <!-- Quick Contact Helpline -->
            <div
              class="luxury-card helpline-card p-4 rounded-4 text-center border shadow-sm"
            >
              <h5
                class="mb-2 text-gold"
                style="
                  font-size: 1.1rem;
                  font-weight: 700;
                  font-family: 'Playfair Display', serif;
                "
              >
                Hỗ Trợ Trực Tuyến
              </h5>
              <p class="text-muted small">
                Nếu có thắc mắc hoặc trường hợp khẩn cấp, vui lòng liên hệ ngay:
              </p>
              <div class="d-flex flex-column gap-2 mt-3">
                <a :href="'tel:' + getThamSo('HOTLINE')" class="helpline-link">
                  <i class="bi bi-telephone-outbound-fill"></i>
                  Hotline: {{ getThamSo("HOTLINE") }}
                </a>
                <button class="btn btn-gold-luxury w-100" @click="openChatBot">
                  <i class="bi bi-chat-dots-fill me-2"></i> Trò chuyện trực
                  tuyến
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.rules-page {
  background-color: #fbfbfb;
  font-family: var(--font-family, "Inter", sans-serif);
  color: #1a1a1a;
  min-height: 100vh;
}

/* Typography & Headers */
.sub-title {
  text-transform: uppercase;
  letter-spacing: 6px;
  font-size: 0.7rem;
  color: #c5a059;
  font-weight: 700;
  display: block;
}

.section-title {
  font-family: "Playfair Display", serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #e65a5a;
}

.luxury-line {
  width: 40px;
  height: 2px;
  background: #c5a059;
  margin: 20px auto;
}

.section-desc {
  max-width: 650px;
  font-size: 0.95rem;
  line-height: 1.6;
}

/* Search & Filter Section */
.filters-section {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #f0f0f0;
}

.search-box-wrapper {
  position: relative;
  width: 100%;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  font-size: 1rem;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 42px;
  font-size: 0.9rem;
  border: 1px solid #e5e7eb;
  border-radius: 100px;
  background: #f9fafb;
  transition: all 0.25s ease;
}

.search-input:focus {
  outline: none;
  background: #ffffff;
  border-color: #c5a059;
  box-shadow: 0 0 0 3px rgba(197, 160, 89, 0.1);
}

.tab-btn {
  background: transparent;
  border: 1px solid #e5e7eb;
  padding: 8px 18px;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: 100px;
  color: #4b5563;
  transition: all 0.25s ease;
  cursor: pointer;
}

.tab-btn:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.tab-btn.active {
  background: #e65a5a;
  border-color: #e65a5a;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(230, 90, 90, 0.15);
}

/* Main Cards */
.category-card {
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.category-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.04) !important;
  border-color: #c5a059;
}

.icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
}

.category-title {
  font-family: "Playfair Display", serif;
  font-weight: 700;
  font-size: 1.3rem;
  color: #111;
  letter-spacing: 0.5px;
}

.rule-subtitle {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.bullet {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}

.rule-text {
  font-size: 0.9rem;
  line-height: 1.6;
  padding-left: 14px;
}

.border-bottom-dashed {
  border-bottom: 1px dashed #e5e7eb;
}

/* Card Watermark */
.card-watermark {
  position: absolute;
  right: -20px;
  bottom: -20px;
  font-size: 10rem;
  opacity: 0.02;
  color: #000;
  pointer-events: none;
  line-height: 1;
}

/* Calculator & Sidebar */
.luxury-card {
  background: #ffffff;
  border: 1px solid #f0f0f0;
}

.form-section-label {
  font-family: "Playfair Display", serif;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.form-label-gold {
  font-size: 0.85rem;
  font-weight: 600;
  color: #4b5563;
}

.custom-range {
  height: 6px;
  border-radius: 5px;
  background: #e5e7eb;
  outline: none;
}

.custom-range::-webkit-slider-thumb {
  background: #c5a059;
}

.custom-range::-moz-range-thumb {
  background: #c5a059;
}

.calc-result {
  background: #fcf8f2;
  border: 1px solid #f5ebd6;
}

.small-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: #78350f;
}

.result-price {
  font-size: 1.15rem;
  font-weight: 800;
}

.deposit-deadline-hint {
  font-size: 0.75rem;
}

.calc-subtitle {
  font-size: 0.9rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.qty-input {
  width: 50px;
  font-weight: 700;
  border-radius: 8px;
  border-color: #e5e7eb;
}

.btn-qty {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  transition: all 0.2s ease;
}

.btn-qty:hover:not(:disabled) {
  border-color: #c5a059;
  color: #c5a059;
}

.btn-qty:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.bg-success-soft {
  background-color: #f0fdf4;
  color: #15803d;
  font-size: 0.75rem;
  padding: 6px 12px;
}

.bg-warning-soft {
  background-color: #fffbeb;
  color: #b45309;
  font-size: 0.75rem;
  padding: 6px 12px;
}

.bg-danger-soft {
  background-color: #fef2f2;
  color: #b91c1c;
  font-size: 0.75rem;
  padding: 6px 12px;
}

.helpline-card {
  background: linear-gradient(to bottom, #ffffff, #fafafa);
}

.helpline-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: #fff5f5;
  color: #b91c1c;
  font-weight: 700;
  border-radius: 12px;
  text-decoration: none;
  font-size: 0.95rem;
  border: 1px solid #fecaca;
  transition: all 0.25s ease;
}

.helpline-link:hover {
  background: #fee2e2;
  transform: translateY(-1px);
}

.btn-gold-luxury {
  background: rgb(242, 38, 38);
  color: #ffffff;
  border: none;
  padding: 12px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.25s ease;
}

.btn-gold-luxury:hover {
  background: rgb(236, 56, 56);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(197, 160, 89, 0.2);
}

.text-gold {
  color: #c5a059;
}

.btn-outline-gold {
  border: 1.5px solid #c5a059;
  color: #c5a059;
  background: transparent;
  padding: 8px 20px;
  border-radius: 100px;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.25s ease;
}

.btn-outline-gold:hover {
  background: #c5a059;
  color: #ffffff;
}

/* Animations */
.reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: 0.8s cubic-bezier(0.2, 1, 0.3, 1);
}

.reveal.active {
  opacity: 1;
  transform: translateY(0);
}

/* Responsive */
@media (max-width: 768px) {
  .section-title {
    font-size: 1.8rem;
  }
  .category-card {
    padding: 20px !important;
  }
}
</style>
