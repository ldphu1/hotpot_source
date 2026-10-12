<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import khuyenmai1 from "@/assets/images/khuyenmai1.png";
import khuyenmai2 from "@/assets/images/khuyenmai2.png";
import banner1 from "@/assets/images/banner-lau.png";
import banner2 from "@/assets/images/banner1.png";
import banner3 from "@/assets/images/banner2.png";
import banner4 from "@/assets/images/banner3.png";
import icongioHang from "@/assets/icons/shopping-cart.png";
import { useUtils } from "@/utils/helper";
const { notify, nprogess } = useUtils();
const promoImages = [khuyenmai1, khuyenmai2];
import { useRouter } from "vue-router";
import {
  getAllLoaiMonAn,
  getAllMonTheoDanhMucDotGiamGia,
  getAllSetLauDotGiamGia,
} from "@/api/customer/thucdon/thucDonCustomerApi";
import {
  themVaoGioHang,
  getGioHang,
  xoaMonGioHang,
} from "@/api/customer/themvaogiohang/themVaoGioHangApi";
import { getAllThamSoCustomer } from "@/api/customer/datbanonline/datBanOnlineApi";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
const router = useRouter();
const currentPage = ref(1);
////////////////// LOAD THAM SỐ ĐỘNG /////////////
const danhSachThamSo = ref([]);

const loadThamSo = async () => {
  try {
    const res = await getAllThamSoCustomer();
    danhSachThamSo.value = res.data;
  } catch (error) {
    console.log("LỖI THAM SỐ", error);
  }
};

const getThamSo = (key) => {
  return danhSachThamSo.value.find((x) => x.maThamSo === key)?.giaTri;
};

////////////////////// THÊM MÓN VÀO GIỎ HÀNG ////////////////////////
const showGioHang = ref(false);
const gioHang = ref(null);
const showGioHangChiTiet = ref(false);
const coMonNgungKinhDoanh = computed(() => {
  return gioHang.value?.items?.some((i) => i.trangThaiXoa) || false;
});
const handleThemVaoGio = async (item) => {
  const token = localStorage.getItem("token");

  const payload = {
    idSanPham: item.id,
    soLuong: 1,
  };

  const danhMuc = item.tenDanhMuc?.trim().toLowerCase();

  // HÀM TÍNH TỔNG
  const getTongTheoDanhMuc = (items, listDanhMuc) => {
    return items
      .filter((i) =>
        listDanhMuc.includes((i.tenDanhMuc || "").trim().toLowerCase()),
      )
      .reduce((sum, i) => sum + i.soLuong, 0);
  };

  // ĐÃ LOGIN
  // =========================
  if (!token) {
    let cart = JSON.parse(localStorage.getItem("cart") || "[]");

    const tongLau = getTongTheoDanhMuc(cart, ["lẩu"]);
    const tongDoNhungLau = getTongTheoDanhMuc(cart, ["đồ nhúng lẩu"]);
    const tongDoUong = getTongTheoDanhMuc(cart, ["đồ uống"]);
    const tongDoTrangMieng = getTongTheoDanhMuc(cart, ["đồ tráng miệng"]);
    const toDongAnKem = getTongTheoDanhMuc(cart, ["đồ ăn kèm"]);

    const MAX_LAU = getThamSo("MAX_LAU");
    const MAX_DO_NHUNG_LAU = getThamSo("MAX_DO_NHUNG_LAU");
    const MAX_DO_UONG = getThamSo("MAX_DO_UONG");
    const MAX_DO_AN_KEM = getThamSo("MAX_DO_AN_KEM");
    const MAX_DO_TRANG_MIENG = getThamSo("MAX_DO_TRANG_MIENG");

    if (danhMuc === "lẩu" && tongLau + 1 > MAX_LAU) {
      notify.error("Chỉ được tối đa 3 lẩu");
      return;
    }
    if (
      ["đồ nhúng lẩu"].includes(danhMuc) &&
      tongDoNhungLau + 1 > MAX_DO_NHUNG_LAU
    ) {
      notify.error("Chỉ được tối đa 10 đồ nhúng lẩu");
      return;
    }
    if (["đồ uống"].includes(danhMuc) && tongDoUong + 1 > MAX_DO_UONG) {
      notify.error("Chỉ được tối đa 20 đồ uống");
      return;
    }
    if (["đồ ăn kèm"].includes(danhMuc) && toDongAnKem + 1 > MAX_DO_AN_KEM) {
      notify.error("Chỉ được tối đa 10 đồ ăn kèm");
      return;
    }
    if (
      ["đồ tráng miệng"].includes(danhMuc) &&
      tongDoTrangMieng + 1 > MAX_DO_TRANG_MIENG
    ) {
      notify.error("Chỉ được tối đa 20 đồ tráng miệng");
      return;
    }

    const index = cart.findIndex((i) => i.id === item.id);

    if (index !== -1) {
      cart[index].soLuong += 1;
    } else {
      cart.push({ ...item, soLuong: 1 });
    }

    localStorage.setItem("cart", JSON.stringify(cart));

    gioHang.value = {
      items: cart,
      tongTien: cart.reduce(
        (sum, i) => sum + (i.giaSauGiam || i.gia) * i.soLuong,
        0,
      ),
    };
    showGioHangChiTiet.value = true;
    return;
  }

  //// CHƯA LOGIN
  // =========================
  try {
    const items = gioHang.value?.items || [];

    const tongLau = getTongTheoDanhMuc(items, ["lẩu"]);
    const tongDoNhungLau = getTongTheoDanhMuc(items, ["đồ nhúng lẩu"]);
    const tongDoUong = getTongTheoDanhMuc(items, ["đồ uống"]);
    const tongDoTrangMieng = getTongTheoDanhMuc(items, ["đồ tráng miệng"]);
    const toDongAnKem = getTongTheoDanhMuc(items, ["đồ ăn kèm"]);

    const MAX_LAU = getThamSo("MAX_LAU");
    const MAX_DO_NHUNG_LAU = getThamSo("MAX_DO_NHUNG_LAU");
    const MAX_DO_UONG = getThamSo("MAX_DO_UONG");
    const MAX_DO_AN_KEM = getThamSo("MAX_DO_AN_KEM");
    const MAX_DO_TRANG_MIENG = getThamSo("MAX_DO_TRANG_MIENG");

    if (danhMuc === "lẩu" && tongLau + 1 > MAX_LAU) {
      notify.error("Chỉ được tối đa 3 lẩu");
      return;
    }
    if (
      ["đồ nhúng lẩu"].includes(danhMuc) &&
      tongDoNhungLau + 1 > MAX_DO_NHUNG_LAU
    ) {
      notify.error("Chỉ được tối đa 10 đồ nhúng lẩu");
      return;
    }
    if (["đồ uống"].includes(danhMuc) && tongDoUong + 1 > MAX_DO_UONG) {
      notify.error("Chỉ được tối đa 20 đồ uống");
      return;
    }
    if (["đồ ăn kèm"].includes(danhMuc) && toDongAnKem + 1 > MAX_DO_AN_KEM) {
      notify.error("Chỉ được tối đa 10 đồ ăn kèm");
      return;
    }
    if (
      ["đồ tráng miệng"].includes(danhMuc) &&
      tongDoTrangMieng + 1 > MAX_DO_TRANG_MIENG
    ) {
      notify.error("Chỉ được tối đa 20 đồ tráng miệng");
      return;
    }

    const res = await themVaoGioHang(payload);
    gioHang.value = res.data;
    showGioHang.value = true;
  } catch (err) {
    const message =
      err?.response?.data?.message || "Không thể thêm vào giỏ hàng";

    notify.error(message);

    const res = await getGioHang();
    gioHang.value = res.data;
  }
};

const capNhatLocalCart = () => {
  localStorage.setItem("cart", JSON.stringify(gioHang.value.items));

  gioHang.value.tongTien = gioHang.value.items.reduce(
    (sum, i) => sum + (i.giaSauGiam || i.gia) * i.soLuong,
    0,
  );
};

const tangSoLuong = (item) => {
  const danhMuc = item.tenDanhMuc?.trim().toLowerCase();
  const items = gioHang.value.items || [];

  const getTong = (listDanhMuc) => {
    return items
      .filter((i) =>
        listDanhMuc.includes((i.tenDanhMuc || "").trim().toLowerCase()),
      )
      .reduce((sum, i) => sum + i.soLuong, 0);
  };

  const tongLau = getTong(["lẩu"]);
  const tongDoNhungLau = getTong(["đồ nhúng lẩu"]);
  const tongDoUong = getTong(["đồ uống"]);
  const tongDoTrangMieng = getTong(["đồ tráng miệng"]);
  const toDongAnKem = getTong(["đồ ăn kèm"]);

  const MAX_LAU = getThamSo("MAX_LAU");
  const MAX_DO_NHUNG_LAU = getThamSo("MAX_DO_NHUNG_LAU");
  const MAX_DO_UONG = getThamSo("MAX_DO_UONG");
  const MAX_DO_AN_KEM = getThamSo("MAX_DO_AN_KEM");
  const MAX_DO_TRANG_MIENG = getThamSo("MAX_DO_TRANG_MIENG");

  if (danhMuc === "lẩu" && tongLau + 1 > MAX_LAU) {
    notify.error("Chỉ được tối đa 3 lẩu");
    return;
  }
  if (
    ["đồ nhúng lẩu"].includes(danhMuc) &&
    tongDoNhungLau + 1 > MAX_DO_NHUNG_LAU
  ) {
    notify.error("Chỉ được tối đa 10 đồ nhúng lẩu");
    return;
  }
  if (["đồ uống"].includes(danhMuc) && tongDoUong + 1 > MAX_DO_UONG) {
    notify.error("Chỉ được tối đa 20 đồ uống");
    return;
  }
  if (["đồ ăn kèm"].includes(danhMuc) && toDongAnKem + 1 > MAX_DO_AN_KEM) {
    notify.error("Chỉ được tối đa 10 đồ ăn kèm");
    return;
  }
  if (
    ["đồ tráng miệng"].includes(danhMuc) &&
    tongDoTrangMieng + 1 > MAX_DO_TRANG_MIENG
  ) {
    notify.error("Chỉ được tối đa 20 đồ tráng miệng");
    return;
  }

  //  pass thì mới tăng
  item.soLuong++;
  capNhatLocalCart();
};

const giamSoLuong = (item) => {
  if (item.soLuong > 1) {
    item.soLuong--;
  } else {
    xoaMon(item);
  }
  capNhatLocalCart();
};

const xoaMon = async (item) => {
  const token = localStorage.getItem("token");
  if (!token) {
    gioHang.value.items = gioHang.value.items.filter((i) => i.id !== item.id);
    capNhatLocalCart();
    return;
  }
  try {
    const res = await xoaMonGioHang(item.id);
    gioHang.value = res.data;
  } catch (err) {
    notify.error("Không thể xóa món");
  }
};
const goToDatBan = () => {
  localStorage.setItem("cart_dat_ban", JSON.stringify(gioHang.value));
  router.push("/dat-ban");
};

///////////////////////////////////////////

const goThucDon = () => {
  router.push("/thuc-don");
};

const goSanpham = () => router.push("/dat-ban");

const scrollBooking = () => {
  router.push(`/thong-tin-dat-ban`);
};
const features = [
  { icon: '<i class="bi bi-droplet-half"></i>', text: "Không dầu mỡ" },
  { icon: '<i class="bi bi-building"></i>', text: "Công nghệ Thái Lan" },
  { icon: '<i class="bi bi-stars"></i>', text: "Không gian hiện đại" },
  { icon: '<i class="bi bi-people"></i>', text: "Phục vụ tận tâm" },
];
const lstDanhMuc = ref([]);
const danhMucDangChon = ref(null);
const lstMonAn = ref([]);

const itemsPerPage = 6;

const totalPages = computed(() => {
  return Math.ceil(lstMonAn.value.length / itemsPerPage);
});

const paginatedMonAn = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return lstMonAn.value.slice(start, end);
});

const danhMucHienThi = computed(() => {
  return lstDanhMuc.value.filter((dm) => dm.tenDanhMuc !== "Nước lẩu");
});

const getImgUrl = (path) => {
  return path ? `http://localhost:8080/uploads/${path}` : "";
};

const banners = [banner1, banner2, banner3, banner4];
const currentIndex = ref(0);

let interval;

const reloadGioHangIfOpen = async () => {
  if (!gioHang.value?.items?.length) return;
  const token = localStorage.getItem("token");

  if (token) {
    try {
      const res = await getGioHang();
      gioHang.value = res.data;
    } catch (err) {
      console.error("Lỗi reload giỏ hàng", err);
    }
    return;
  }

  try {
    const coMonLau = gioHang.value.items.some(
      (i) => (i.tenDanhMuc || "").trim().toLowerCase() === "lẩu",
    );

    const danhMucIds = [
      ...new Set(
        gioHang.value.items
          .filter((i) => (i.tenDanhMuc || "").trim().toLowerCase() !== "lẩu")
          .map((i) => i.idDanhMuc)
          .filter((id) => id !== undefined && id !== null),
      ),
    ];

    const requests = [
      ...(coMonLau ? [getAllSetLauDotGiamGia()] : []),
      ...danhMucIds.map((id) => getAllMonTheoDanhMucDotGiamGia(id)),
    ];

    const results = await Promise.all(requests);

    const activeProducts = new Map(
      results
        .flatMap((res) => res.data?.data || res.data || [])
        .map((p) => [p.id, p]),
    );

    gioHang.value.items.forEach((item) => {
      const product = activeProducts.get(item.id);
      item.trangThaiXoa = !product;
      if (product) {
        item.gia = product.gia;
        item.giaSauGiam = product.giaSauGiam;
      }
    });

    capNhatLocalCart();
  } catch (err) {
    console.error("Lỗi đồng bộ trạng thái giỏ hàng (khách)", err);
  }
};

const loadDanhMuc = async () => {
  try {
    const res = await getAllLoaiMonAn();

    lstDanhMuc.value = res.data;

    // tự chọn LẨU (id = 1)
    const danhMucLau = lstDanhMuc.value.find((dm) => dm.id === 1);

    if (danhMucLau) {
      await loadMonAnTheoDanhMuc(danhMucLau);
    } else if (lstDanhMuc.value.length > 0) {
      await loadMonAnTheoDanhMuc(lstDanhMuc.value[0]);
    }
  } catch (err) {
    console.error("Lỗi load danh mục", err);
  }
};

const loadMonAnTheoDanhMuc = async (dm) => {
  try {
    danhMucDangChon.value = dm;
    currentPage.value = 1;
    let res;
    if (dm.id === 1) {
      res = await getAllSetLauDotGiamGia();
    } else {
      res = await getAllMonTheoDanhMucDotGiamGia(dm.id);
    }
    const data = res.data?.data || res.data || [];

    if (dm.id !== 1) {
      const filtered = data.filter((item) => item.idDanhMuc === dm.id);
      lstMonAn.value = filtered;
    } else {
      const filtered = data.filter((item) => item.laSet === true);
      lstMonAn.value = filtered;
    }
  } catch (err) {
    console.error(" Lỗi load món ăn", err);
  }
};
const refreshDanhMucList = async () => {
  try {
    const res = await getAllLoaiMonAn();
    const newList = res.data;
    lstDanhMuc.value = newList;

    const stillExists = newList.find(
      (dm) => dm.id === danhMucDangChon.value?.id,
    );
    if (!stillExists && newList.length > 0) {
      await loadMonAnTheoDanhMuc(newList[0]);
    }
  } catch (err) {
    console.error("Lỗi refresh danh mục", err);
  }
};

const handleWsMessage = (data) => {
  if (data?.type === "SAN_PHAM_UPDATE") {
    if (!danhMucDangChon.value) {
      loadDanhMuc();
      return;
    }
    reloadGioHangIfOpen();
    loadMonAnTheoDanhMuc(danhMucDangChon.value);
  } else if (data && data.tenDanhMuc !== undefined) {
    refreshDanhMucList();
  }
};

const handleCartUpdated = async () => {
  const token = localStorage.getItem("token");
  if (token) {
    try {
      const res = await getGioHang();
      gioHang.value = res.data;
      showGioHangChiTiet.value = true;
    } catch (err) {
      console.error("Lỗi đồng bộ giỏ hàng:", err);
    }
  } else {
    const cart = JSON.parse(localStorage.getItem("cart") || "[]");
    if (cart.length) {
      gioHang.value = {
        items: cart,
        tongTien: cart.reduce(
          (sum, i) => sum + (i.giaSauGiam || i.gia) * i.soLuong,
          0,
        ),
      };
      showGioHangChiTiet.value = true;
    }
  }
};

onMounted(() => {
  loadThamSo();
  loadDanhMuc();

  connectWebSocket(null, handleWsMessage);

  const token = localStorage.getItem("token");
  if (token) {
    getGioHang()
      .then((res) => {
        gioHang.value = res.data;
      })
      .catch((err) => {
        console.error("Lỗi tải giỏ hàng:", err);
      });
  } else {
    const cart = JSON.parse(localStorage.getItem("cart") || "[]");
    if (cart.length) {
      gioHang.value = {
        items: cart,
        tongTien: cart.reduce(
          (sum, i) => sum + (i.giaSauGiam || i.gia) * i.soLuong,
          0,
        ),
      };
    }
  }

  window.addEventListener("cart-updated", handleCartUpdated);

  interval = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % banners.length;
  }, 2000);

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) entry.target.classList.add("active");
      });
    },
    { threshold: 0.15 },
  );
  document
    .querySelectorAll(
      ".reveal, .reveal-left, .reveal-right, .reveal-up, .reveal-down",
    )
    .forEach((el) => observer.observe(el));
});

onUnmounted(() => {
  if (interval) clearInterval(interval);
  window.removeEventListener("cart-updated", handleCartUpdated);
  disconnectWebSocket(handleWsMessage, null);
});
</script>

<template>
  <main class="home-body">
    <section class="hero-zen">
      <div class="hero-container">
        <img
          v-for="(banner, index) in banners"
          :key="index"
          :src="banner"
          class="hero-slide"
          :class="{ active: index === currentIndex }"
        />
        <div class="hero-overlay">
          <div class="hero-content reveal-up">
            <span class="eyebrow">Thưởng thức ngay hôm nay</span>
            <h1 class="hero-title">Lẩu Thái chua cay – Ăn là ghiền</h1>
            <button class="btn-zen-primary" @click="goSanpham">
              ĐẶT BÀN NGAY
              <div class="light-sweep"></div>
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="reveal py-5 container">
      <h2 class="section-title text-center mb-5">KHUYẾN MÃI HOT TRONG TUẦN</h2>
      <div class="row g-4">
        <div class="col-md-6" v-for="(img, index) in promoImages" :key="index">
          <div class="promo-item overflow-hidden rounded-4 shadow-sm">
            <img
              :src="img"
              alt="Promo"
              class="img-fluid hover-zoom promo-img"
            />
          </div>
        </div>
      </div>
    </section>

    <section class="reveal py-5 bg-texture">
      <div class="container text-center">
        <h3 class="fw-bold mb-5 text-danger">
          LẨU THÁI NGON CHUẨN VỊ CHUA CAY & KHÔNG SỢ BÉO
        </h3>
        <div class="row g-4">
          <div
            class="col-6 col-md-3 reveal"
            :class="[
              index === 0
                ? 'reveal-left'
                : index === 1
                ? 'reveal-up'
                : index === 2
                ? 'reveal-down'
                : 'reveal-right',
            ]"
            v-for="(feat, index) in features"
            :key="index"
          >
            <div class="feature-card-royal">
              <div class="card-shine"></div>
              <div class="card-shine second"></div>
              <!-- thêm dòng này -->

              <div class="royal-border"></div>

              <div class="card-content">
                <div class="icon-box-royal mb-3" v-html="feat.icon"></div>
                <p class="feature-text">
                  {{ feat.text }}
                </p>
              </div>
            </div>
          </div>
        </div>
        <div class="mt-4 d-flex justify-content-center gap-3">
          <button
            class="btn btn-danger-outline rounded-pill px-4 btn-modern"
            @click="goThucDon"
          >
            XEM THỰC ĐƠN
          </button>

          <button class="floating-booking" @click="scrollBooking">
            <i class="bi bi-calendar-check"></i>
            Đặt bàn
          </button>
        </div>
      </div>
    </section>

    <section class="reveal py-5 container">
      <div
        class="d-flex flex-wrap justify-content-between align-items-center mb-4 border-bottom pb-3"
      >
        <div class="menu-header-wrapper text-center mb-4">
          <div class="menu-header-box">
            <h5 class="menu-header-title">THỰC ĐƠN</h5>
          </div>
        </div>
        <div class="menu-categories-luxury d-flex gap-3 overflow-auto">
          <template v-for="dm in danhMucHienThi" :key="dm.id">
            <span
              class="category-item-royal"
              :class="{ active: danhMucDangChon?.id === dm.id }"
              @click="loadMonAnTheoDanhMuc(dm)"
            >
              <span class="category-text">{{ dm.tenDanhMuc }}</span>

              <div class="category-shine"></div>

              <div class="active-indicator"></div>
            </span>
          </template>
        </div>
      </div>

      <div class="row g-4">
        <div
          class="col-6 col-lg-4"
          v-for="item in paginatedMonAn"
          :key="item.id"
        >
          <div class="product-card p-3 shadow-sm border rounded-4">
            <div class="product-img-box mb-3">
              <img :src="getImgUrl(item.hinhAnh)" :alt="item.tenSanPham" />

              <div class="shine-fast"></div>
              <div class="shine-slow"></div>
            </div>
            <h5 class="product-title text-uppercase">
              {{ item.tenSanPham }}
            </h5>

            <div class="mt-auto product-price">
              <template v-if="item.giaSauGiam && item.giaSauGiam < item.gia">
                <div class="d-flex align-items-center gap-2">
                  <span class="text-muted text-decoration-line-through small">
                    {{ item.gia.toLocaleString() }}đ
                  </span>

                  <span class="text-danger fw-bold">
                    {{ item.giaSauGiam.toLocaleString() }}đ
                  </span>
                </div>
              </template>

              <template v-else>
                <div class="text-danger fw-bold">
                  {{ item.gia.toLocaleString() }}đ
                </div>
              </template>
            </div>
            <div class="d-flex gap-2">
              <button
                class="btn btn-sm flex-grow-1 fw-bold btn-add luxury-order-btn"
                @click="handleThemVaoGio(item)"
              >
                <div
                  class="d-flex align-items-center justify-content-center gap-2"
                >
                  <div class="icon-wrapper">
                    <i class="bi bi-square icon-default"></i>
                    <i class="bi bi-check-square-fill icon-check"></i>
                  </div>

                  <span>ĐẶT MÓN</span>
                </div>
                <div class="btn-light-sweep"></div>
              </button>
              <button class="btn btn-detail luxury-draw-btn">Chi tiết</button>
            </div>
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-center mt-5" v-if="totalPages > 1">
        <nav>
          <ul
            class="pagination-custom d-flex align-items-center gap-2 list-unstyled"
          >
            <li
              class="page-item-custom"
              :class="{ disabled: currentPage === 1 }"
            >
              <button
                class="page-link-custom"
                @click="currentPage--"
                :disabled="currentPage === 1"
              >
                <i class="bi bi-chevron-left"></i>
              </button>
            </li>

            <li
              v-for="page in totalPages"
              :key="page"
              class="page-item-custom"
              :class="{ active: page === currentPage }"
            >
              <button class="page-link-custom" @click="currentPage = page">
                {{ page }}
              </button>
            </li>

            <li
              class="page-item-custom"
              :class="{ disabled: currentPage === totalPages }"
            >
              <button
                class="page-link-custom"
                @click="currentPage++"
                :disabled="currentPage === totalPages"
              >
                <i class="bi bi-chevron-right"></i>
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </section>

    <section class="reveal py-5 bg-light">
      <div class="container row align-items-center mx-auto">
        <div class="col-lg-7 position-relative">
          <div class="gallery-stack">
            <img
              src="https://via.placeholder.com/400x300"
              class="img-1 shadow-lg rounded-4"
              alt=""
            />
            <img
              src="https://via.placeholder.com/300x400"
              class="img-2 shadow-lg rounded-4"
              alt=""
            />
          </div>
        </div>
        <div class="col-lg-5 ps-lg-5">
          <h2 class="text-danger fw-bold">TRẢI NGHIỆM PHONG CÁCH THAILAND</h2>
          <ul class="list-unstyled mt-4">
            <li class="mb-3">✨ Không gian tráng lệ, nhiều góc check-in</li>
            <li class="mb-3">✨ Lẩu hấp tốt cho sức khỏe, tăng đề kháng</li>
            <li>✨ Đội ngũ phục vụ tận tâm, chuyên nghiệp</li>
          </ul>
        </div>
      </div>
    </section>

    <section class="reveal py-5 container text-center">
      <h2 class="section-title mb-5">CẢM NHẬN CỦA KHÁCH HÀNG</h2>
      <div class="row g-4">
        <div class="col-md-4" v-for="i in [1, 2, 3]" :key="i">
          <div class="feedback-card p-4 rounded-4 shadow-sm">
            <img
              src="https://via.placeholder.com/300x200"
              class="rounded-3 mb-3 w-100 hover-zoom"
              alt=""
            />
            <p class="fst-italic small">
              "Đồ ăn ngon, không gian view ThaiLand cực xinh, nhân viên phục vụ
              10 điểm..."
            </p>
            <div class="mt-3">
              <h6 class="mb-0 fw-bold">KHÁCH HÀNG THỨ {{ i }}</h6>
              <span class="text-muted smaller">Tiktoker</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section id="booking-section" class="reveal py-5 booking-parallax">
      <div class="container">
        <div class="booking-form shadow-lg mx-auto p-4 p-md-5">
          <h4 class="text-center fw-bold mb-4">THÔNG TIN KHÁCH HÀNG</h4>
          <div class="booking-grid">
            <div class="form-group full-width">
              <label>Họ tên (*)</label>
              <input type="text" placeholder="Nhập họ tên" />
            </div>
            <div class="form-group">
              <label>Điện thoại (*)</label>
              <input type="tel" placeholder="Số điện thoại" />
            </div>
            <div class="form-group">
              <label>Email</label>
              <input type="email" placeholder="Email" />
            </div>
            <div class="form-group">
              <label>Ngày đặt (*)</label>
              <input type="date" />
            </div>
            <div class="form-group">
              <label>Giờ đặt</label>
              <input type="time" />
            </div>
          </div>
          <button class="btn btn-danger w-100 mt-4 py-3 rounded-pill fw-bold">
            ĐẶT BÀN NGAY
          </button>
        </div>
      </div>
    </section>
  </main>
  <Teleport to="body">
    <div class="mini-cart-luxury">
      <div class="corner-gold tl"></div>
      <div class="corner-gold tr"></div>

      <div class="mini-cart-info d-flex align-items-center gap-3">
        <span class="cart-icon-luxury">
          <img :src="icongioHang" alt="Cart" width="24" height="24" />
        </span>
        <div class="cart-text">
          <span class="cart-count">{{ gioHang?.items?.length || 0 }} món đã chọn</span>
          <span class="cart-total"
            >{{ (gioHang?.tongTien || 0).toLocaleString() }}đ</span
          >
        </div>
      </div>

      <div class="mini-cart-action-luxury" @click="showGioHangChiTiet = true">
        <span>XEM GIỎ HÀNG</span>
        <i class="bi bi-chevron-right arrow-icon"></i>
      </div>
    </div>

    <!-- OVERLAY -->
    <div
      v-if="showGioHangChiTiet"
      class="modal-overlay"
      @click="showGioHangChiTiet = false"
    ></div>

    <!-- MODAL -->
    <div v-if="showGioHangChiTiet" class="cart-modal-luxury">
      <div class="cart-modal-overlay" @click="showGioHangChiTiet = false"></div>

      <div class="cart-modal-content">
        <div class="modal-corner tl"></div>
        <div class="modal-corner tr"></div>
        <div class="modal-corner bl"></div>
        <div class="modal-corner br"></div>

        <div class="cart-modal-header-luxury">
          <h5 class="header-title">KIỂM TRA THỰC ĐƠN</h5>
          <span class="close-btn-luxury" @click="showGioHangChiTiet = false">
            <span class="close-icon">✕</span>
            <div class="close-halo"></div>
          </span>
        </div>

        <div v-if="!gioHang?.items?.length" class="text-center py-5 text-muted">
          <i class="bi bi-cart-x fs-1 d-block mb-3"></i>
          <span>Giỏ hàng của bạn đang trống</span>
        </div>

        <div
          v-else
          v-for="item in gioHang.items"
          :key="item.id"
          class="cart-modal-item-luxury"
          :class="{ 'item-disabled': item.trangThaiXoa }"
        >
          <div class="item-img-wrapper">
            <img :src="getImgUrl(item.hinhAnh)" />
          </div>

          <div class="flex-grow-1 ms-3">
            <div class="item-name-luxury">{{ item.tenSanPham }}</div>

            <div
              v-if="item.trangThaiXoa"
              class="text-danger fw-bold small mt-1"
            >
              Món ăn ngừng kinh doanh
            </div>

            <div v-else class="item-price-small">
              {{ (item.giaSauGiam || item.gia).toLocaleString() }}đ
            </div>

            <div class="quantity-control-luxury mt-2">
              <button
                class="q-btn"
                @click="giamSoLuong(item)"
                :disabled="item.trangThaiXoa"
              >
                -
              </button>
              <span class="q-number">{{ item.soLuong }}</span>
              <button
                class="q-btn"
                @click="tangSoLuong(item)"
                :disabled="item.trangThaiXoa"
              >
                +
              </button>
            </div>
          </div>

          <div
            class="text-end d-flex flex-direction-column justify-content-between"
          >
            <div class="item-subtotal-luxury">
              {{
                ((item.giaSauGiam || item.gia) * item.soLuong).toLocaleString()
              }}đ
            </div>
            <button class="btn-remove-luxury" @click="xoaMon(item)">
              HỦY MÓN
            </button>
          </div>
        </div>

        <div class="cart-modal-footer-luxury">
          <div class="total-section">
            <span class="total-label">TỔNG CỘNG TIÊU DÙNG:</span>
            <span class="total-amount"
              >{{ (gioHang?.tongTien || 0).toLocaleString() }}đ</span
            >
          </div>

          <div class="d-flex gap-3 mt-3">
            <button
              class="btn-luxury-outline"
              @click="showGioHangChiTiet = false"
            >
              TIẾP TỤC CHỌN MÓN
            </button>
            <button
              class="btn-luxury-solid"
              :disabled="coMonNgungKinhDoanh || !gioHang?.items?.length"
              @click="goToDatBan"
            >
              CHUYỂN ĐẶT BÀN
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* /////////////// DOT GIAM GIA ////////// */
.price-inline {
  display: flex;
  align-items: center;
  gap: 10px;
}

.old-price {
  text-decoration: line-through;
  color: #999;
  font-size: 0.85rem;
}

.new-price {
  color: #e03b41;
  font-weight: 800;
  font-size: 1rem;
}

/* ///////////// NÚT X GIO HANG  */
.close-btn-luxury {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.4s ease;
}

.close-icon {
  font-size: 1.5rem;
  color: rgba(255, 10, 10, 0.6);
  z-index: 2;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.close-halo {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 1px solid rgba(212, 175, 55, 0);
  border-radius: 50%;
  transition: all 0.5s ease;
  z-index: 1;
}

.close-btn-luxury:hover .close-icon {
  color: #f02020;
  transform: rotate(180deg) scale(1); /* Xoay nửa vòng và phóng to */
  text-shadow: 0 0 15px rgba(212, 175, 55, 0.8);
}

.close-btn-luxury:active {
  transform: scale(0.9);
}
/* MODAL GIO HANG CHI TIET */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1000;
}

/* Overlay che phủ màn hình */
.cart-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  z-index: 1001;
}

.cart-modal-content {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 550px;
  max-width: 95%;
  background: #fff;
  z-index: 1002;
  border: 4px double #d4af37; /* Viền kép vàng Gold */
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  max-height: 85vh;
}

/* Họa tiết góc Modal */
.modal-corner {
  position: absolute;
  width: 15px;
  height: 15px;
  border: 3px solid #a31d21;
  z-index: 1003;
}
.modal-corner.tl {
  top: 10px;
  left: 10px;
  border-right: none;
  border-bottom: none;
}
.modal-corner.tr {
  top: 10px;
  right: 10px;
  border-left: none;
  border-bottom: none;
}
.modal-corner.bl {
  bottom: 10px;
  left: 10px;
  border-right: none;
  border-top: none;
}
.modal-corner.br {
  bottom: 10px;
  right: 10px;
  border-left: none;
  border-top: none;
}

/* Header & Footer */
.cart-modal-header-luxury {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fdfdfd;
}

.header-title {
  font-family: "Playfair Display", serif;
  font-weight: 900;
  color: #d42b31;
  letter-spacing: 2px;
  margin: 0;
}

/* Body & Items */
.cart-modal-body-luxury {
  padding: 20px;
  overflow-y: auto;
}

.cart-modal-item-luxury {
  display: flex;
  padding: 15px 0;
  border-bottom: 1px dashed #d4af37; /* Đường xé sớ */
}

.item-img-wrapper img {
  width: 85px;
  height: 85px;
  object-fit: cover;
  border: 1px solid #d4af37;
  padding: 3px;
}

.item-name-luxury {
  font-family: "Playfair Display", serif;
  font-weight: 800;
  font-size: 1.1rem;
  color: #333;
}

/* Quantity Control */
.quantity-control-luxury {
  display: flex;
  align-items: center;
  border: 1px solid #e46468;
  width: fit-content;
}
.q-btn {
  background-color: #e63a3f;
  color: white;
  border: none;
  width: 25px;
  height: 25px;
  font-weight: bold;
}
.q-number {
  padding: 0 15px;
  font-weight: 800;
  color: #dd3e43;
}

.item-subtotal-luxury {
  font-weight: 700;
  color: #e6464c;
  font-size: 1.1rem;
}

.item-disabled {
  opacity: 0.5;
  pointer-events: none;
}
.item-disabled .btn-remove-luxury {
  pointer-events: all;
}
.btn-remove-luxury {
  border: none;
  background: none;
  color: #999;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 1px;
}

/* Footer & Buttons */
.cart-modal-footer-luxury {
  padding: 25px;
  background: #f9f9f9;
  border-top: 1px solid #eee;
}

.total-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 2px solid #ed5e5e;
  padding-bottom: 10px;
}

.total-label {
  font-weight: 800;
  color: #666;
  font-size: 0.8rem;
}
.total-amount {
  font-size: 2rem;
  font-weight: 700;
  color: #e03b41;
}

.btn-luxury-solid {
  flex: 1;
  background: #e42e34;
  color: white;
  border: none;
  padding: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: 0.3s;
}
.btn-luxury-solid:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}
.btn-luxury-outline {
  flex: 1;
  background: transparent;
  color: #e14248;
  border: 2px solid #e63f45;
  padding: 15px;
  font-weight: 600;
}

.btn-luxury-solid:hover {
  background: #ff0008;
  transform: translateY(-3px);
}
.cart-modal-content {
  animation: modalSwoosh 0.5s cubic-bezier(0.165, 0.84, 0.44, 1);
}

@keyframes modalSwoosh {
  0% {
    opacity: 0;
    transform: translate(-50%, -45%) scale(0.9) rotateX(-10deg);
  }
  100% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1) rotateX(0deg);
  }
}
.cart-modal-body-luxury::-webkit-scrollbar {
  width: 5px;
}

.cart-modal-body-luxury::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.cart-modal-body-luxury::-webkit-scrollbar-thumb {
  background: #d4af37; /* Màu vàng Gold */
  border-radius: 10px;
}
.cart-modal-item-luxury {
  transition: background 0.3s ease;
  padding: 15px 10px;
  margin: 0 -10px;
}

.cart-modal-item-luxury:hover {
  background: rgba(212, 175, 55, 0.05);
}
/* CSS GIO HANG */
.mini-cart-luxury {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;

  background: linear-gradient(135deg, #e53238 0%, #a02226 100%);
  padding: 12px 12px 12px 30px;
  border-radius: 4px;

  /* Viền kép Gold */
  border: 3px double #d4af37;

  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 50px;
  min-width: 420px;

  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5), 0 0 15px rgba(212, 175, 55, 0.2);
  animation: slideUpOriental 0.6s cubic-bezier(0.23, 1, 0.32, 1);
  transition: all 0.4s ease;
}

.corner-gold {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid #d4af37;
  opacity: 0.6;
}
.corner-gold.tl {
  top: 5px;
  left: 5px;
  border-right: none;
  border-bottom: none;
}
.corner-gold.tr {
  top: 5px;
  right: 5px;
  border-left: none;
  border-bottom: none;
}

.cart-icon-luxury {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
  display: flex;
  align-items: center;
}
.mini-cart-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cart-count {
  font-size: 12px;
  color: #d43737;
  font-weight: 700;
}

.cart-total {
  font-size: 18px;
  font-weight: 800;
  color: #f11212;
  line-height: 1.2;
}

.cart-count {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #f0a6a6;
  font-weight: 700;
}
.cart-text {
  display: flex;
  flex-direction: column; /* QUAN TRỌNG */
  line-height: 1.3;
}

.cart-total {
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  font-family: "Playfair Display" serif;
  line-height: 1.2;
}

/* Nút hành động kiểu Gold Button */
.mini-cart-action-luxury {
  background: #f37777;
  color: #ffffff;
  padding: 14px 28px;
  border-radius: 2px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1.5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.mini-cart-action-luxury:hover {
  background: #fff;
  color: #e7353a;
  transform: scale(1.05);
  box-shadow: 0 0 20px rgba(212, 175, 55, 0.4);
}

/* Animation trượt lên kiểu cung đình */
@keyframes slideUpOriental {
  from {
    transform: translate(-50%, 100px);
    opacity: 0;
  }
  to {
    transform: translate(-50%, 0);
    opacity: 1;
  }
}

.mini-cart-luxury:hover {
  bottom: 35px;
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.6);
}
/* /////////////// NÚT ĐẶT MÓN VÀ CHI TIẾT /////////////// */

.btn-add {
  border: 1px solid #dc3545;
  color: #ffffff;
  background-color: rgb(240, 74, 74);
}

.btn-add:hover {
  background: #dc3545;
  color: white;
  border-color: #dc3545;
}
.luxury-order-btn span {
  color: white;
}
.luxury-order-btn .d-flex {
  position: relative;
  z-index: 2;
}
.luxury-order-btn {
  --btn-gold: #ef5757;
  background-color: var(--btn-gold);
  color: #fff;
  border: 1px solid var(--btn-gold);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}
.icon-wrapper {
  position: relative;
  width: 18px; /* Độ rộng icon */
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper i {
  position: absolute;
  transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
  font-size: 1.1rem;
}
.icon-default {
  opacity: 0.6;
  color: #fff !important;
}
.icon-check {
  opacity: 0;
  transform: scale(0);
  color: #fff !important;
}
.luxury-order-btn {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  z-index: 1;
}

.btn-light-sweep {
  position: absolute;
  top: 0;
  left: -150%;
  width: 40%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.4) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: 0s;
  z-index: 2;
  pointer-events: none;
}

.luxury-order-btn:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

.luxury-order-btn:hover .icon-default {
  opacity: 0;
  transform: scale(0.5);
}

.luxury-order-btn:hover .icon-check {
  opacity: 1;
  transform: scale(1);
}
.luxury-order-btn {
  position: relative;
  background-color: #f53d3d;
  border: none;
  overflow: hidden;
  z-index: 1;
  transition: all 0.3s ease;
}

.luxury-order-btn::before {
  content: "";
  position: absolute;
  top: 4px;
  left: 4px;
  right: 4px;
  bottom: 4px;
  border: 1px solid rgba(255, 255, 255, 0.5);

  z-index: 0;
  pointer-events: none;
  transition: all 0.3s ease;
}

.luxury-order-btn:hover::before {
  border-color: rgba(255, 255, 255, 1);
  top: 3px;
  left: 3px;
  right: 3px;
  bottom: 3px;
}

.luxury-order-btn .d-flex {
  position: relative;
  z-index: 2;
}

.luxury-order-btn:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

.luxury-draw-btn {
  position: relative;
  background: #fff;
  color: #e4484d;
  border: 0.5px solid #a31d21;
  padding: 8px 25px;
  overflow: hidden;
  transition: all 0.3s ease;
  z-index: 1;
}

.luxury-draw-btn:hover {
  background: #ee3137;
  color: #fff;
}

.luxury-draw-btn::before,
.luxury-draw-btn::after {
  content: "";
  position: absolute;
  top: 4px;
  left: 4px;
  right: 4px;
  bottom: 4px;
  z-index: -1;
}

.luxury-draw-btn:hover::before {
  border-top: 1px solid #fff;
  border-bottom: 1px solid #fff;
  animation: draw-horizontal 0.4s forwards ease-in-out;
}

.luxury-draw-btn:hover::after {
  border-left: 1px solid #fff;
  border-right: 1px solid #fff;

  animation: draw-vertical 0.4s forwards ease-in-out;
}

@keyframes draw-horizontal {
  0% {
    left: 50%;
    right: 50%;
    opacity: 0;
  }
  100% {
    left: 4px;
    right: 4px;
    opacity: 1;
  }
}

@keyframes draw-vertical {
  0% {
    top: 50%;
    bottom: 50%;
    opacity: 0;
  }
  100% {
    top: 4px;
    bottom: 4px;
    opacity: 1;
  }
}
/* ////////////////// CARD MÓN ĂN //////////////// */
.product-img-box {
  width: 100%;
  height: 180px;
  overflow: hidden;
  border-radius: 12px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.shine-fast,
.shine-slow {
  position: absolute;
  top: 0;
  left: -150%;
  height: 100%;
  transform: skewX(-20deg);
  pointer-events: none;
}

.shine-fast {
  width: 60px;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.9),
    transparent
  );
  transition: 0.5s ease;
}

.shine-slow {
  width: 120px;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(239, 226, 226, 0.4),
    transparent
  );
  transition: 1s ease;
}

.product-card:hover .shine-fast {
  left: 140%;
}

.product-card:hover .shine-slow {
  left: 170%;
}

/* ===== ZOOM NHẸ ===== */
.product-card:hover img {
  transform: scale(1.08);
}
.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-title {
  min-height: 40px;
}

/* //////////////// THỰC ĐƠN ///////////////// */
.menu-header-box {
  background-color: #e63a3f;
  border: 3px double #ffffff;
  outline: 2px solid #a31d21;
  padding: 8px 25px;
  display: inline-block;
  position: relative;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.menu-header-box::before,
.menu-header-box::after {
  content: "◈";
  color: #fff;
  position: absolute;
  font-size: 8px;
  top: 50%;
  transform: translateY(-50%);
}
.menu-header-box::before {
  left: 5px;
}
.menu-header-box::after {
  right: 5px;
}

.menu-header-title {
  color: #ffffff;
  font-weight: 700;
  margin: 0;
  letter-spacing: 1px;
  font-size: 1.4rem;
}
/* ========================= */
.feature-card-royal {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(247, 36, 36, 0.2);
  padding: 30px 20px;
  border-radius: 4px; /* Bo góc nhẹ cho sang */
  text-align: center;
  overflow: hidden;
  transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1);
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.royal-border {
  position: absolute;
  top: 5px;
  left: 5px;
  right: 5px;
  bottom: 5px;
  border: 1px solid rgba(212, 175, 55, 0.3);
  pointer-events: none;
  z-index: 2;
  transition: all 0.5s ease;
}

.royal-border::after {
  content: "";
  position: absolute;
  top: 3px;
  left: 3px;
  right: 3px;
  bottom: 3px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.icon-box-royal {
  font-size: 2.5rem;
  color: #f32727;
  transition: all 0.5s ease;
  filter: drop-shadow(0 0 5px rgba(212, 175, 55, 0.3));
}

.feature-text {
  font-size: 0.85rem;
  font-weight: 700;
  color: #ee2f2f;
  letter-spacing: 1.5px;
  margin-top: 15px;
  font-size: 16px;
}

.card-shine {
  position: absolute;
  top: 0;
  left: -150%;
  width: 70px;
  height: 100%;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 1) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: 0.6s ease;
  z-index: 3;
}

.card-shine.second {
  width: 120px;
  background: linear-gradient(
    120deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.6) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transition: 1s ease;
}
.feature-card-royal:hover .card-shine {
  left: 150%;
}

.feature-card-royal:hover .card-shine.second {
  left: 170%;
}
.feature-card-royal:hover {
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.4), 0 0 20px rgba(212, 175, 55, 0.3);
}
.feature-card-royal:hover {
  transform: translateY(-10px);
  background: rgba(212, 175, 55, 0.1); /* Nền ngả vàng nhẹ */
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.4);
  border-color: #ef6b6b;
}

.feature-card-royal:hover .card-shine {
  left: 100%;
}

.feature-card-royal:hover .royal-border {
  border-color: #ae6262;
  top: 8px;
  left: 8px;
  right: 8px;
  bottom: 8px;
}

.feature-card-royal:hover .icon-box-royal {
  transform: scale(1.2);
  color: #fff;
  filter: drop-shadow(0 0 15px #f07777);
}

/* /////////////////// BỘ LỘC ////////////// */
.menu-categories-luxury {
  padding: 10px 5px;
  display: flex;
  gap: 25px;
  overflow-x: auto;
  scrollbar-width: none;
}
.menu-categories-luxury::-webkit-scrollbar {
  display: none;
}

/* ===== ITEM ===== */
.category-item-royal {
  position: relative;
  padding: 8px 5px;
  background: transparent;
  border: none;
  cursor: pointer;
  white-space: nowrap;
  color: #ef1a1a;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: 1.5px;
  text-transform: uppercase;

  transition: all 0.3s ease;
}

.category-text {
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
}

.category-shine {
  position: absolute;
  top: 0;
  left: -120%;
  width: 60px;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.35),
    transparent
  );
  transform: skewX(-20deg);
  transition: 0.7s;
  pointer-events: none;
}

.active-indicator {
  position: absolute;
  bottom: -3px;
  left: 50%;
  width: 0;
  height: 2px;
  background: #e97b7b;
  transform: translateX(-50%);
  transition: all 0.35s ease;
  box-shadow: 0 0 8px #d4af37;
}

.category-item-royal:hover {
  color: #ee7777;
}

.category-item-royal:hover .category-text {
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.4);
}

.category-item-royal:hover .category-shine {
  left: 150%;
}

.category-item-royal.active {
  color: #ff0000;
}

.category-item-royal.active .category-text {
  text-shadow: 0 0 10px rgba(212, 175, 55, 0.6);
}

.category-item-royal.active .active-indicator {
  width: 70%;
}
/* ========================= */

.btn-zen-primary {
  position: relative; /* QUAN TRỌNG */
  overflow: hidden;
  background: #e04c4c;
  color: #fff;
  border: none;
  padding: 15px 45px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 2px;
  transition: 0.4s;
}
.btn-zen-primary:hover {
  background: #e53f3f;
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(241, 38, 38, 0.2);
  letter-spacing: 3px;
}
.btn-zen-primary:hover .light-sweep {
  left: 150%;
}
.light-sweep {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

.zen-category-tabs {
  display: flex;
  gap: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
.tab-item {
  font-size: 0.75rem;
  font-weight: 700;
  color: #bbb;
  cursor: pointer;
  text-transform: uppercase;
  position: relative;
  padding: 10px 0;
}
.tab-item.active {
  color: #000;
}
.tab-item.active::after {
  content: "";
  position: absolute;
  bottom: -11px;
  left: 0;
  width: 100%;
  height: 2px;
  background: #000;
}

.hero-zen {
  height: calc(70vh - 70px); /* trừ chiều cao header */
  margin-top: 70px; /* đẩy xuống đúng vị trí */
}
.hero-zen {
  height: 70vh;
  background: #000;
  position: relative;
  overflow: hidden;
}

.hero-zen::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 150px;
  background: linear-gradient(to top, #fff, transparent);
  z-index: 15;
}
.hero-slide {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  filter: brightness(0.7);
  transition: opacity 1.5s cubic-bezier(0.4, 0, 0.2, 1), transform 3s ease;
  transform: scale(1.1);
}
.hero-slide.active {
  opacity: 1;
  transform: scale(1);
}
.hero-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-left: 80px;
  z-index: 10;
}
.hero-title {
  font-family: "Syncopate", sans-serif;
  font-size: clamp(1.8rem, 4vw, 3.5rem);
  color: #fff;
  text-transform: uppercase;
  letter-spacing: -2px;
  line-height: 0.9;
  margin: 20px 0;
}
.eyebrow {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 5px;
  color: rgba(255, 255, 255, 0.7);
}

/* --- SCROLL HINT --- */
.scroll-hint {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  z-index: 20;
}
.scroll-text {
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 4px;
  color: #fff;
  opacity: 0.8;
}
.scroll-bar {
  width: 1px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}
.scroll-bar::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #fff;
  animation: scroll-anim 2s infinite cubic-bezier(0.77, 0, 0.175, 1);
}
@keyframes scroll-anim {
  0% {
    transform: translateY(-100%);
  }
  50% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(100%);
  }
}

/* Hiệu ứng chuyển cảnh mượt mà cho banner */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.8s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.hero-img {
  width: 100%;
  height: auto;
  object-fit: cover;
  /* Bỏ transition opacity cũ nếu có để tránh xung đột với transition group */
}
.hero-img {
  transition: opacity 1s ease;
  opacity: 1;
}

.hero-img.fade {
  opacity: 0;
}
.promo-img {
  width: 100%;
  height: 260px;
  object-fit: cover;
  transition: transform 0.8s ease;
}
.icon-box {
  font-size: 38px;
  color: #dc3545;
  transition: 0.3s;
}

.feature-card:hover .icon-box {
  transform: scale(1.2) rotate(6deg);
  color: #ff7a45;
}

.home-body {
  background: linear-gradient(180deg, #fff, #fff6f6);
}

/* =========================
   HERO
========================= */

.hero-img {
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18), 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.9s ease, filter 0.6s;
}

.hero-img:hover {
  transform: scale(1.03);
  filter: brightness(1.05);
}

/* =========================
   TITLE
========================= */

.section-title {
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(45deg, #b80000, #ff3c3c);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* =========================
   PROMO CARD
========================= */

.promo-img {
  max-width: 100%;
  height: auto;
  object-fit: contain;
  display: block;
  margin: auto;
}

.promo-item::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.25));
  opacity: 0;
  transition: 0.5s;
}

.promo-item:hover::after {
  opacity: 1;
}

.promo-img {
  transition: transform 0.8s ease;
}

.promo-item:hover .promo-img {
  transform: scale(1.12);
}

/* =========================
   FEATURE CARD
========================= */

.feature-card {
  padding: 26px;
  border-radius: 20px;
  background: white;
  position: relative;
  transition: 0.4s;

  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.05);
}

.feature-card::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 20px;
  padding: 1px;
  background: linear-gradient(45deg, #ff4d4d, #ffc107);
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  opacity: 0;
  transition: 0.4s;
}

.feature-card:hover {
  transform: translateY(-12px) scale(1.04);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
}

.feature-card:hover::before {
  opacity: 1;
}

.icon-box {
  font-size: 32px;
}

/* =========================
   MENU TAB
========================= */

.category-item {
  font-weight: 700;
  color: #777;
  cursor: pointer;
  padding-bottom: 4px;
  transition: 0.25s;
}

.category-item:hover {
  color: #dc3545;
}

.category-item.active {
  color: #dc3545;
  border-bottom: 2px solid #dc3545;
}

/* =========================
   PRODUCT CARD
========================= */

.product-card {
  background: white;
  border-radius: 18px;
  overflow: hidden;

  transition: 0.35s;

  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08), 0 2px 10px rgba(0, 0, 0, 0.04);
}

.product-card:hover {
  transform: translateY(-12px) scale(1.02);

  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.18);
}

.product-title {
  font-size: 0.95rem;
  font-weight: 800;
  letter-spacing: 0.4px;
}

.product-price {
  font-size: 1rem;
}

/* image */

.hover-zoom {
  transition: transform 0.8s cubic-bezier(0.2, 0.6, 0.3, 1);
}

.product-card:hover .hover-zoom {
  transform: scale(1.12);
}

/* =========================
   BUTTON
========================= */

.btn-modern {
  font-weight: 700;
  transition: 0.35s;

  position: relative;
  overflow: hidden;
}
.btn-modern::before {
  content: "";
  position: absolute;
  top: 0;
  left: -120%;

  width: 120%;
  height: 100%;

  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.35),
    transparent
  );

  transform: skewX(-25deg);
  transition: 0.6s;
}

.btn-modern:hover::before {
  left: 120%;
}

.btn-danger-outline {
  border: 2px solid #dc3545;
  color: #dc3545;
}

.btn-warning-gold {
  background: linear-gradient(135deg, #ffc107, #ffdd55);
  border: none;
}

.btn-modern:hover {
  transform: translateY(-3px);

  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.2);
}

/* add button */

.btn-add {
  color: #dc3545;
  border-color: #dc3545;
  transition: 0.3s;
}

.btn-add:hover {
  background: #dc3545;
  color: white;
  border-color: #dc3545;
}
/* =========================
   GALLERY
========================= */

.gallery-stack {
  position: relative;
}

.img-1 {
  width: 65%;
  transition: 0.7s;
}

.img-2 {
  width: 45%;
  position: absolute;
  right: 0;
  bottom: -40px;
  transition: 0.7s;
}

.gallery-stack:hover .img-1 {
  transform: translateY(-10px) rotate(-2deg);
}

.gallery-stack:hover .img-2 {
  transform: translateY(10px) rotate(2deg);
}

/* =========================
   FEEDBACK CARD
========================= */

.feedback-card {
  background: white;
  transition: 0.35s;

  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
}

.feedback-card:hover {
  transform: translateY(-10px);

  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.18);
}

/* =========================
   BOOKING FORM
========================= */
.booking-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

.full-width {
  grid-column: span 2;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-weight: 600;
  font-size: 0.9rem;
  color: #555;
}
.booking-form {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);

  border-radius: 22px;

  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.18);
}

.form-group input {
  border: 1px solid #eee;
  background: #fafafa;
  border-radius: 12px;
  padding: 12px;
  transition: 0.3s;
}

.form-group input:focus {
  border-color: #dc3545;
  outline: none;

  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.15);
}

/* =========================
   REVEAL ANIMATION
========================= */

.reveal {
  opacity: 0;
  transform: translateY(70px) scale(0.95);
  transition: transform 0.9s cubic-bezier(0.2, 0.6, 0.3, 1), opacity 0.8s ease;
}

.reveal.active {
  opacity: 1;
  transform: translateY(0) scale(1);
}

/* =========================
   MOBILE
========================= */
@media (max-width: 768px) {
  .booking-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: span 1;
  }
}
@media (max-width: 768px) {
  .hero-img {
    border-radius: 10px;
  }

  .section-title {
    font-size: 1.6rem;
  }
}

/* reveal directions */

.reveal-left {
  opacity: 0;
  transform: translateX(-80px);
  transition: all 0.9s cubic-bezier(0.2, 0.6, 0.3, 1);
}

.reveal-right {
  opacity: 0;
  transform: translateX(80px);
  transition: all 0.9s cubic-bezier(0.2, 0.6, 0.3, 1);
}

.reveal-up {
  opacity: 0;
  transform: translateY(80px);
  transition: all 0.9s cubic-bezier(0.2, 0.6, 0.3, 1);
}

.reveal-down {
  opacity: 0;
  transform: translateY(-80px);
  transition: all 0.9s cubic-bezier(0.2, 0.6, 0.3, 1);
}

.reveal-left.active,
.reveal-right.active,
.reveal-up.active,
.reveal-down.active {
  opacity: 1;
  transform: translate(0);
}

/* =========================
FLOATING BOOKING BUTTON
========================= */

.floating-booking {
  display: flex;
  align-items: center;
  gap: 8px;

  padding: 14px 20px;

  border-radius: 40px;
  border: none;

  font-weight: 700;

  color: white;

  background: linear-gradient(135deg, #dc3545, #ff6a6a);

  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.25);

  transition: 0.3s;
  position: relative; /* thêm dòng này */
  overflow: hidden;
}

.floating-booking:hover {
  transform: translateY(-4px) scale(1.05);

  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.35);
}

/* pulse animation */

.floating-booking::after {
  content: "";
  position: absolute;
  inset: 0;

  border-radius: 40px;

  background: rgba(220, 53, 69, 0.4);

  animation: pulseBooking 2s infinite;
  pointer-events: none;
}

@keyframes pulseBooking {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }

  70% {
    transform: scale(1.5);
    opacity: 0;
  }

  100% {
    opacity: 0;
  }
}

@media (max-width: 768px) {
  /* Thu hẹp khoảng cách container để nội dung rộng hơn */
  .container {
    padding-left: 15px;
    padding-right: 15px;
  }

  /* Section Title nhỏ lại cho cân đối */
  .section-title {
    font-size: 1.4rem;
    margin-bottom: 30px !important;
  }

  /* Banner Hot Promotion: Chồng lên nhau để ảnh to rõ */
  .promo-img {
    height: 180px; /* Giảm chiều cao để không chiếm hết màn hình */
  }

  /* Thực đơn: Giữ 2 cột nhưng thu nhỏ font và padding */
  .product-card {
    padding: 10px !important;
    border-radius: 12px;
  }

  .product-title {
    font-size: 0.8rem;
    height: 40px; /* Giữ độ cao cố định để card đều nhau */
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .product-price {
    font-size: 0.9rem;
  }

  /* Nút bấm trên Card thực đơn cho gọn */
  .btn-add {
    font-size: 10px !important;
    padding: 5px 2px !important;
  }
  .btn-detail {
    display: none; /* Ẩn bớt nút phụ trên mobile cho đỡ rối */
  }
}
@media (max-width: 991px) {
  /* Gallery Stack: Chuyển về dạng nằm ngang đơn giản */
  .gallery-stack {
    height: 300px;
    margin-bottom: 30px;
  }

  .img-1 {
    width: 80%;
    left: 0;
  }

  .img-2 {
    width: 60%;
    right: 0;
    bottom: 0;
    z-index: 2;
  }

  /* Danh sách tính năng (features) */
  .feature-card {
    padding: 15px;
  }

  .icon-box {
    font-size: 24px;
  }
}
@media (max-width: 576px) {
  .booking-form {
    padding: 25px 15px !important;
    margin: 0 10px;
    border-radius: 15px;
  }

  .booking-grid {
    gap: 12px; /* Thu hẹp khoảng cách giữa các input */
  }

  .form-group label {
    font-size: 0.8rem;
  }

  .form-group input {
    padding: 10px;
    font-size: 14px;
  }

  /* Nút đặt bàn to rõ, dễ bấm bằng ngón cái */
  .btn-danger.w-100 {
    padding: 15px !important;
    font-size: 1.1rem;
    letter-spacing: 1px;
  }
}
.menu-categories {
  display: flex;
  white-space: nowrap;
  padding-bottom: 10px;
  -webkit-overflow-scrolling: touch; /* Cuộn mượt trên iOS */
  gap: 20px;
}

.menu-categories::-webkit-scrollbar {
  display: none; /* Ẩn thanh cuộn cho đẹp */
}

.category-item {
  flex: 0 0 auto; /* Không cho các item bị co lại */
  font-size: 0.9rem;
}

/* CUSTOM ADDITIONAL RESPONSIVE & ZOOM OVERRIDES */
@media (max-width: 768px) {
  .hero-overlay {
    padding-left: 20px !important;
    justify-content: center !important;
    text-align: center !important;
  }
  .hero-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
  }
  .hero-title {
    font-size: 1.8rem !important;
    line-height: 1.2 !important;
    margin: 15px 0 !important;
  }
}

@media (max-width: 576px) {
  .feature-card-royal {
    padding: 15px 10px !important;
    min-height: 120px;
  }
  .icon-box-royal {
    font-size: 1.8rem !important;
  }
  .feature-text {
    font-size: 13px !important;
    margin-top: 10px !important;
  }
  .category-item-royal {
    padding: 6px 10px !important;
    font-size: 0.85rem !important;
  }
  .gallery-stack {
    height: 220px !important;
  }
  .img-1 {
    width: 75% !important;
  }
  .img-2 {
    width: 55% !important;
  }
}
</style>

<style>
/* ===== MINI CART TELEPORT - TrangChu (global) ===== */
.mini-cart-luxury {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  background: linear-gradient(135deg, #e53238 0%, #a02226 100%);
  padding: 12px 12px 12px 30px;
  border-radius: 4px;
  border: 3px double #d4af37;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 50px;
  min-width: 420px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5), 0 0 15px rgba(212, 175, 55, 0.2);
  animation: slideUpOriental 0.6s cubic-bezier(0.23, 1, 0.32, 1);
  transition: all 0.4s ease;
}
.mini-cart-luxury .corner-gold {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid #d4af37;
  opacity: 0.6;
}
.mini-cart-luxury .corner-gold.tl {
  top: 5px;
  left: 5px;
  border-right: none;
  border-bottom: none;
}
.mini-cart-luxury .corner-gold.tr {
  top: 5px;
  right: 5px;
  border-left: none;
  border-bottom: none;
}
.mini-cart-luxury .cart-icon-luxury {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
  display: flex;
  align-items: center;
}
.mini-cart-luxury .mini-cart-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.mini-cart-luxury .cart-count {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #f0a6a6;
  font-weight: 700;
}
.mini-cart-luxury .cart-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.mini-cart-luxury .cart-total {
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  line-height: 1.2;
}
.mini-cart-luxury:hover {
  bottom: 35px;
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.6);
}
.mini-cart-action-luxury {
  background: #f37777;
  color: #ffffff;
  padding: 14px 28px;
  border-radius: 2px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1.5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.3);
}
.mini-cart-action-luxury:hover {
  background: #fff;
  color: #e7353a;
  transform: scale(1.05);
  box-shadow: 0 0 20px rgba(212, 175, 55, 0.4);
}
@keyframes slideUpOriental {
  from {
    transform: translate(-50%, 100px);
    opacity: 0;
  }
  to {
    transform: translate(-50%, 0);
    opacity: 1;
  }
}

/* ===== MODAL OVERLAY & CART MODAL (global) ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 10000;
}
.cart-modal-luxury {
  position: fixed;
  inset: 0;
  z-index: 10001;
  pointer-events: none;
}
.cart-modal-luxury .cart-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  z-index: 10001;
  pointer-events: all;
}
.cart-modal-luxury .cart-modal-content {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 550px;
  max-width: 95%;
  background: #fff;
  z-index: 10002;
  border: 4px double #d4af37;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  max-height: 85vh;
  pointer-events: all;
  animation: modalSwoosh 0.5s cubic-bezier(0.165, 0.84, 0.44, 1);
}
@keyframes modalSwoosh {
  0% {
    opacity: 0;
    transform: translate(-50%, -45%) scale(0.9);
  }
  100% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

/* CUSTOM GLOBAL RESPONSIVE & ZOOM OVERRIDES FOR TELEPORTED ELEMENTS */
@media (max-width: 576px) {
  .mini-cart-luxury {
    min-width: 90% !important;
    width: 90% !important;
    gap: 15px !important;
    padding: 10px 10px 10px 15px !important;
    bottom: 15px !important;
  }
  .mini-cart-luxury:hover {
    bottom: 20px !important;
  }
  .mini-cart-action-luxury {
    padding: 10px 15px !important;
    font-size: 10px !important;
    letter-spacing: 0.5px !important;
    gap: 5px !important;
  }
  .mini-cart-luxury .cart-total {
    font-size: 16px !important;
  }
  .mini-cart-luxury .cart-count {
    font-size: 9px !important;
    letter-spacing: 1px !important;
  }

  .cart-modal-luxury .cart-modal-content {
    width: 95% !important;
    max-height: 90vh !important;
  }

  .cart-modal-item-luxury {
    flex-direction: column !important;
    gap: 10px !important;
    align-items: flex-start !important;
    padding: 10px 0 !important;
  }
  .cart-modal-item-luxury .item-img-wrapper img {
    width: 60px !important;
    height: 60px !important;
  }
  .cart-modal-item-luxury .ms-3 {
    margin-left: 0 !important;
    width: 100% !important;
  }
  .cart-modal-item-luxury .text-end {
    text-align: left !important;
    align-items: flex-start !important;
    width: 100% !important;
    flex-direction: row !important;
    justify-content: space-between !important;
  }
  .cart-modal-footer-luxury {
    padding: 15px !important;
  }
  .cart-modal-footer-luxury .d-flex {
    flex-direction: column !important;
    gap: 10px !important;
  }
  .btn-luxury-outline,
  .btn-luxury-solid {
    padding: 10px !important;
    font-size: 13px !important;
    width: 100% !important;
  }
  .total-amount {
    font-size: 1.5rem !important;
  }
}

/* Phân trang hiện đại */
.pagination-custom {
  padding: 10px 20px;
  background: white;
  border-radius: 50px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

.page-item-custom {
  list-style: none;
}

.page-link-custom {
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #eee;
  background: white;
  color: #555;
  border-radius: 50%;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  outline: none;
}

.page-link-custom:hover:not(:disabled) {
  background: #fff5f5;
  color: #dc3545;
  border-color: #dc3545;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(220, 53, 69, 0.2);
}

.page-item-custom.active .page-link-custom {
  background: linear-gradient(45deg, #b80000, #ff3c3c);
  color: white;
  border: none;
  box-shadow: 0 8px 20px rgba(184, 0, 0, 0.3);
  transform: scale(1.1);
}

.page-item-custom.disabled .page-link-custom {
  opacity: 0.4;
  cursor: not-allowed;
  background: #f8f9fa;
}

/* Icon trong phân trang */
.page-link-custom i {
  font-size: 1.1rem;
  -webkit-text-stroke: 0.5px;
}
</style>
