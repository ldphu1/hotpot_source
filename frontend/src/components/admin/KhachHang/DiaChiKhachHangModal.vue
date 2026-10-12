<script setup>
import { ref, watch, computed } from "vue";
import axios from "axios";

import {
  getDiaChiByKhachHang,
  addDiaChi,
  setMacDinh,
  updateDiaChi,
} from "@/api/admin/diachikhachhang/diachikhachhangapi";

import { useUtils } from "@/utils/helper";

const { notify, nprogress } = useUtils();

/* ================= PROPS ================= */

const props = defineProps({
  khId: Number,
  tenKhachHang: String,
  isOpen: Boolean,
});

const emit = defineEmits(["close"]);

/* ================= STATE ================= */

const danhSachDiaChi = ref([]);
const editingId = ref(null);

const form = ref({
  hoTen: "",
  sdt: "",
  tinhThanh: "",
  tinhName: "",
  xaPhuong: "",
  diaChiChiTiet: "",
  macDinh: false,
});
const errors = ref({
  hoTen: "",
  sdt: "",
  tinhThanh: "",
  xaPhuong: "",
  diaChiChiTiet: "",
});
/* ================= LOCATION ================= */

const listTinh = ref([]);
const listXa = ref([]);

const openTinh = ref(false);
const openXa = ref(false);

const searchTinh = ref("");
const searchXa = ref("");

/* ================= FILTER ================= */

const filteredTinh = computed(() => {
  if (!searchTinh.value) return listTinh.value;
  return listTinh.value.filter((t) =>
    t.name.toLowerCase().includes(searchTinh.value.toLowerCase()),
  );
});

const filteredXa = computed(() => {
  if (!searchXa.value) return listXa.value;
  return listXa.value.filter((x) =>
    x.name.toLowerCase().includes(searchXa.value.toLowerCase()),
  );
});

/* ================= LOAD DATA ================= */

const loadDiaChi = async () => {
  if (!props.khId) return;
  const res = await getDiaChiByKhachHang(props.khId);
  danhSachDiaChi.value = res.data || [];
};

const loadTinh = async () => {
  const res = await fetch("https://34tinhthanh.com/api/provinces");
  const data = await res.json();

  listTinh.value = data.map((t) => ({
    code: t.province_code,
    name: t.name,
  }));
};

const loadXa = async (tinhCode) => {
  if (!tinhCode) return;

  try {
    const res = await axios.get(
      `https://provinces.open-api.vn/api/p/${tinhCode}?depth=3`,
    );

    const districts = res.data?.districts || [];

    listXa.value = districts.flatMap((d) =>
      (d.wards || []).map((w) => ({
        code: w.code,
        name: w.name,
      })),
    );

    console.log("XA LIST:", listXa.value);
  } catch (e) {
    console.log("LOAD XA FAIL:", e);
    listXa.value = [];
  }
};

/* ================= SELECT ================= */

const selectTinh = async (t) => {
  form.value.tinhThanh = t.code;
  form.value.tinhName = t.name;

  searchTinh.value = t.name;
  openTinh.value = false;

  form.value.xaPhuong = "";
  searchXa.value = "";

  listXa.value = []; // 🔥 thêm dòng này

  await loadXa(t.code);
};

const selectXa = (x) => {
  form.value.xaPhuong = x.name;
  searchXa.value = x.name;
  openXa.value = false;
};

/* ================= RESET ================= */
const validateForm = () => {
  errors.value = {
    hoTen: "",
    sdt: "",
    tinhThanh: "",
    xaPhuong: "",
    diaChiChiTiet: "",
  };

  let valid = true;

  if (!form.value.hoTen.trim()) {
    errors.value.hoTen = "Vui lòng nhập họ tên người nhận";
    valid = false;
  } else if (form.value.hoTen.trim().length < 2) {
    errors.value.hoTen = "Họ tên phải từ 2 ký tự";
    valid = false;
  } else if (form.value.hoTen.length > 50) {
    errors.value.hoTen = "Họ tên không quá 50 ký tự";
    valid = false;
  } else if (!/^[A-Za-zÀ-ỹ\s]+$/.test(form.value.hoTen)) {
    errors.value.hoTen = "Họ tên không được chứa số hoặc ký tự đặc biệt";
    valid = false;
  }

  if (!form.value.sdt.trim()) {
    errors.value.sdt = "Vui lòng nhập số điện thoại";
    valid = false;
  } else if (!/^(0[3|5|7|8|9])[0-9]{8}$/.test(form.value.sdt)) {
    errors.value.sdt = "Số điện thoại không hợp lệ";
    valid = false;
  }

  if (!form.value.tinhName) {
    errors.value.tinhThanh = "Vui lòng chọn tỉnh/thành";
    valid = false;
  }

  if (!form.value.xaPhuong) {
    errors.value.xaPhuong = "Vui lòng chọn phường/xã";
    valid = false;
  }

  if (!form.value.diaChiChiTiet.trim()) {
    errors.value.diaChiChiTiet = "Vui lòng nhập địa chỉ chi tiết";
    valid = false;
  }

  return valid;
};
const resetForm = () => {
  editingId.value = null;

  form.value = {
    hoTen: "",
    sdt: "",
    tinhThanh: "",
    tinhName: "",
    xaPhuong: "",
    diaChiChiTiet: "",
    macDinh: false,
  };

  searchTinh.value = "";
  searchXa.value = "";
};

/* ================= EDIT ================= */

const editDiaChi = async (dc) => {
  editingId.value = dc.id;

  form.value = {
    hoTen: dc.hoTenNguoiNhan,
    sdt: dc.sdtNguoiNhan,
    tinhThanh: "",
    tinhName: dc.tinhThanh,
    xaPhuong: dc.xaPhuong,
    diaChiChiTiet: dc.diaChiChiTiet,
    macDinh: dc.macDinh,
  };

  searchTinh.value = dc.tinhThanh;
  searchXa.value = dc.xaPhuong;

  const tinh = listTinh.value.find((t) => t.name === dc.tinhThanh);

  if (tinh) {
    form.value.tinhThanh = tinh.code;

    await loadXa(tinh.code);

    searchXa.value = dc.xaPhuong; // 👈 thêm dòng này
  }
};

/* ================= SAVE ================= */

const saveDiaChi = async () => {
  if (!validateForm()) {
    notify.error("Vui lòng kiểm tra lại thông tin");
    return;
  }

  const data = {
    hoTenNguoiNhan: form.value.hoTen,
    sdtNguoiNhan: form.value.sdt,
    tinhThanh: form.value.tinhName,
    xaPhuong: form.value.xaPhuong,
    diaChiChiTiet: form.value.diaChiChiTiet,
    macDinh: form.value.macDinh,
  };

  try {
    nprogress.start();

    console.log("DATA SEND:", data); // 👈 THÊM DÒNG NÀY

    if (editingId.value) {
      await updateDiaChi(editingId.value, data);
      notify.success("Cập nhật địa chỉ thành công");
    } else {
      await addDiaChi(props.khId, data); // 👈 giữ như này trước
      notify.success("Thêm địa chỉ thành công");
    }

    resetForm();
    await loadDiaChi();
  } catch (err) {
    console.log("ERROR:", err.response?.data || err); // 👈 THÊM DÒNG NÀY
    notify.error(err.response?.data?.message || "Lưu địa chỉ thất bại");
  } finally {
    nprogress.done();
  }
};

/* ================= DEFAULT ================= */

const datMacDinh = async (id) => {
  const ok = await notify.confirm("Bạn muốn đặt địa chỉ này làm mặc định?");

  if (!ok) return;

  try {
    await setMacDinh(id);

    notify.success("Đã đặt địa chỉ mặc định");

    await loadDiaChi();
  } catch (e) {
    notify.error("Không thể đặt địa chỉ mặc định");
  }
};

/* ================= RELOAD ================= */

const reloadForm = async () => {
  resetForm();
  await loadTinh();
  listXa.value = [];

  notify.success("Đã làm mới dữ liệu");
};

/* ================= WATCH ================= */

watch(
  () => props.isOpen,
  async (v) => {
    if (v) {
      await loadTinh();
      await loadDiaChi();
    }
  },
);
</script>

<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay">
      <div class="modal-box">
        <!-- HEADER -->
        <div class="modal-header">
          <div class="header-left">
            <img src="@/assets/icons/diachiic.png" class="icon" />

            <div>
              <div class="title">Sổ địa chỉ khách hàng</div>
              <div class="sub">Khách hàng: {{ tenKhachHang }}</div>
            </div>
          </div>

          <div class="header-right">
            <button class="btn-reload" @click="reloadForm">⟳</button>
            <button class="btn-icon" @click="$emit('close')">✕</button>
          </div>
        </div>

        <!-- BODY -->
        <div class="modal-body">
          <!-- LIST -->
          <div class="card left">
            <div class="card-title">📋 Danh sách địa chỉ</div>
            <div class="table-header">
              <div class="col-stt">STT</div>

              <div class="col-diachi">Địa chỉ</div>

              <div class="col-default">Mặc định</div>
            </div>
            <div
              class="diachi-item"
              v-for="(dc, index) in danhSachDiaChi"
              :key="dc.id"
            >
              <div class="col-stt">
                {{ index + 1 }}
              </div>

              <div class="col-diachi">
                <div class="address">
                  {{ dc.diaChiChiTiet }}, {{ dc.xaPhuong }}, {{ dc.tinhThanh }}
                </div>

                <div class="nguoinhan">
                  Người nhận: {{ dc.hoTenNguoiNhan }} • SĐT:
                  {{ dc.sdtNguoiNhan }}
                </div>
              </div>

              <div class="col-default">
                <span v-if="dc.macDinh" class="default"> Mặc định </span>

                <button v-else class="btn-default" @click="datMacDinh(dc.id)">
                  Đặt
                </button>
                <button class="btn-default" @click="editDiaChi(dc)">Sửa</button>
              </div>
            </div>
          </div>

          <!-- FORM -->
          <div class="card right">
            <div class="card-title">➕ Thêm nhanh địa chỉ</div>

            <label>Họ tên người nhận</label>
            <input
              v-model="form.hoTen"
              placeholder="Nhập họ tên người nhận"
              :class="{ error: errors.hoTen }"
            />
            <div class="error-text">{{ errors.hoTen }}</div>

            <label>SĐT</label>
            <input
              v-model="form.sdt"
              placeholder="vd:03..."
              :class="{ error: errors.sdt }"
            />
            <div class="error-text">{{ errors.sdt }}</div>

            <div class="row">
              <div class="col">
                <label>Tỉnh / Thành</label>

                <div class="combo" ref="tinhRef">
                  <input
                    v-model="searchTinh"
                    @focus="openTinh = true"
                    placeholder="Chọn tỉnh"
                    :class="{ error: errors.tinhThanh }"
                  />
                  <div class="error-text">{{ errors.tinhThanh }}</div>

                  <div v-if="openTinh" class="dropdown">
                    <div
                      v-for="t in filteredTinh"
                      :key="t.code"
                      class="item"
                      @mousedown.prevent="selectTinh(t)"
                    >
                      {{ t.name }}
                    </div>
                  </div>
                </div>
              </div>

              <div class="col">
                <label>Phường / Xã</label>

                <div class="combo" ref="xaRef">
                  <input
                    v-model="searchXa"
                    @focus="openXa = true"
                    placeholder="Chọn phường xã"
                    :class="{ error: errors.xaPhuong }"
                  />
                  <div class="error-text">{{ errors.xaPhuong }}</div>

                  <div v-if="openXa" class="dropdown">
                    <div
                      v-for="x in filteredXa"
                      :key="x.code"
                      class="item"
                      @mousedown.prevent="selectXa(x)"
                    >
                      {{ x.name }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <label>Địa chỉ chi tiết</label>
            <input
              v-model="form.diaChiChiTiet"
              placeholder="Nhập số nhà, xóm..."
              :class="{ error: errors.diaChiChiTiet }"
            />
            <div class="error-text">{{ errors.diaChiChiTiet }}</div>

            <label class="checkbox">
              <input type="checkbox" v-model="form.macDinh" />
              Đặt làm mặc định
            </label>

            <button class="btn-add" @click="saveDiaChi">
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-box {
  width: 1300px;
  background: white;
  border-radius: 14px;
  padding: 20px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  gap: 10px;
  align-items: center;
}

.icon {
  width: 22px;
}

.title {
  font-weight: 600;
  font-size: 18px;
}

.sub {
  font-size: 13px;
  color: #777;
}

.btn-icon {
  border: none;
  background: #f3f4f6;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  cursor: pointer;
}

.modal-body {
  display: flex;
  gap: 20px;
}

.card {
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 15px;
}

.left {
  flex: 1.2;
}

.right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.diachi-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #eee;
  transition: 0.2s;
}

.diachi-item:hover {
  background: #f9fafb;
}

.address {
  font-weight: 500;
}

.nguoinhan {
  font-size: 13px;
  color: #777;
}

.default {
  background: #ffe5e5;
  color: #e11d48;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
}

.btn-default {
  background: #e5f0ff;
  color: #2563eb;
  border: none;
  padding: 6px 0;
  border-radius: 8px;
  cursor: pointer;
  width: 60px;
  text-align: center;
  font-size: 13px;
}

input {
  padding: 9px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.combo {
  position: relative;
}

.dropdown {
  position: absolute;
  top: 42px;
  left: 0;
  right: 0;
  max-height: 220px;
  overflow: auto;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  z-index: 20;
}

.item {
  padding: 8px 10px;
  cursor: pointer;
}

.item:hover {
  background: #f3f4f6;
}

.btn-add {
  margin-top: 10px;
  background: #c94c4c;
  color: white;
  border: none;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
}

.checkbox {
  display: flex;
  gap: 6px;
  font-size: 13px;
}
.row {
  display: flex;
  gap: 10px;
}

.col {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.table-header {
  display: flex;
  padding: 8px 10px;
  font-size: 13px;
  font-weight: 600;
  color: #555;
  border-bottom: 1px solid #ddd;
  background: #f9fafb;
  border-radius: 6px;
  margin-bottom: 5px;
}

.col-stt {
  width: 50px;
  text-align: center;
}

.col-diachi {
  flex: 1;
}

.col-default {
  width: 180px;
  display: flex;
  gap: 6px;
  justify-content: center;
  align-items: center;
}
.btn-reload {
  border: none;
  background: #f3f4f6;
  width: 35px;
  height: 35px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-reload:hover {
  background: #e5e7eb;
}
.error {
  border: 1px solid #ef4444 !important;
  background: #fff5f5;
}

.error-text {
  font-size: 12px;
  color: #ef4444;
  margin-top: 2px;
}
</style>
