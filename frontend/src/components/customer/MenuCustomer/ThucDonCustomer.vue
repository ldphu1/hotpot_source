<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import khach1 from "@/assets/images/sontung.jpg";
import khach2 from "@/assets/images/huanhoahong.jpg";
import khach3 from "@/assets/images/leminho.jpg";
import icongioHang from "@/assets/icons/shopping-cart.png";
import { useUtils } from "@/utils/helper";
import { getAllSetLau } from "@/api/customer/thucdon/thucDonCustomerApi";
import { getAllThucDon } from "@/api/customer/thucdon/thucDonCustomerApi";
import { getAllLoaiMonAn } from "@/api/customer/thucdon/thucDonCustomerApi";
import { getAllMonTheoDanhMucDotGiamGia } from "@/api/customer/thucdon/thucDonCustomerApi";
import { getAllSetLauDotGiamGia } from "@/api/customer/thucdon/thucDonCustomerApi";
import { getAllLoaiLau } from "@/api/customer/thucdon/thucDonCustomerApi";
import {
  themVaoGioHang,
  xoaMonGioHang,
} from "@/api/customer/themvaogiohang/themVaoGioHangApi";
import { getGioHang } from "@/api/customer/themvaogiohang/themVaoGioHangApi";
import { searchMonAn } from "@/api/customer/thucdon/thucDonCustomerApi";
import { filterSetLau } from "@/api/customer/thucdon/thucDonCustomerApi";
import router from "@/router";
import { getAllThamSoCustomer } from "@/api/customer/datbanonline/datBanOnlineApi";
import { connectWebSocket, disconnectWebSocket } from "@/utils/websocket";
const { notify, nprogress } = useUtils();

////////////////////////// LOAD THAM SO DONG //////////////////
const danhSachThamSo = ref([]);

const loadThamSoDong = async () => {
  try {
    const res = await getAllThamSoCustomer();
    danhSachThamSo.value = res.data;
    console.log(res.data);
  } catch (error) {
    console.log("Lỗi tham số", error);
  }
};

// Get tham số
const getThamSo = (key) => {
  return danhSachThamSo.value.find((x) => x.maThamSo === key)?.giaTri;
};

/////////////////////////////////// LỌC SET LẨU /////////////////////////
const nuocLauDuocChon = ref(null);

const handleFilterNuocLau = async (item) => {
  try {
    nuocLauDuocChon.value = item.id;
    currentPage.value = 1;

    let res;

    if (!item.id) {
      res = await getAllSetLau();
    } else {
      res = await filterSetLau(item.id);
    }

    const data = res.data.data || res.data;
    lstMonAn.value = data;
  } catch (err) {
    console.error("Lỗi filter set lẩu", err);
  }
};

//////////////////////////// BỘ LỌC /////////////////////////////////////
const keyword = ref("");
const sortType = ref("");
let timeout = null;

const isDanhMucLau = computed(() => {
  return danhMucDangChon.value?.id === 1;
});

const handleFilter = async () => {
  try {
    currentPage.value = 1;

    const params = {
      keyword: keyword.value || null,
      sortGia: sortType.value || null,
    };

    const res = await searchMonAn(params);

    lstMonAn.value = res.data.data || res.data;
  } catch (err) {
    console.error("Filter lỗi", err);
  }
};

/////////////////////////////////////////////////////////////////////////
const feedbacks = [
  {
    name: "Sơn Tùng M-TP",
    role: "Food Review",
    img: khach1,
    avatar: khach1,
    comment:
      "Không gian view Hồng Kông xinh đẹp, rộng rãi. Đồ ăn ngon, menu đa dạng, healthy.",
  },
  {
    name: "Huấn Hoa Hồng",
    role: "Tiktoker",
    img: khach2,
    avatar: khach2,
    comment:
      "Đồ hấp giữ được chất dinh dưỡng nên rất healthy, nhân viên phục vụ từ A-Z.",
  },
  {
    name: "Lee Min Ho",
    role: "Youtuber",
    img: khach3,
    avatar: khach3,
    comment: "Món ăn ngon, không sử dụng dầu mỡ nên rất tốt cho sức khỏe.",
  },
];

////////////////////////// LOẠI LẨU /////////////////////////
const danhSachNuocLau = ref([]);
const loadNuocLau = async () => {
  try {
    const res = await getAllLoaiLau();
    danhSachNuocLau.value = res.data;
  } catch (error) {
    console.log(error);
  }
};

////////////////////// THÊM MÓN VÀO GIỎ HÀNG ////////////////////////
const showGioHang = ref(false);
const gioHang = ref(null);
const showGioHangChiTiet = ref(false);

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
///////////////////////////////////////////

const lstDanhMuc = ref([]);
const danhMucDangChon = ref(null);
const lstMonAn = ref([]);

const danhMucHienThi = computed(() => {
  return lstDanhMuc.value.filter((dm) => dm.tenDanhMuc !== "Nước lẩu");
});

const loadDanhMuc = async () => {
  try {
    const res = await getAllLoaiMonAn();

    lstDanhMuc.value = res.data;

    // chọn danh mục LẨU (id = 1)
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

const getImgUrl = (path) => {
  return path ? `http://localhost:8080/uploads/${path}` : "";
};

const goToDatBan = () => {
  localStorage.removeItem("thong-tin-khach-hang");
  localStorage.setItem("cart_dat_ban", JSON.stringify(gioHang.value));
  router.push("/dat-ban");
};

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

    if (keyword.value || sortType.value) {
      handleFilter();
    } else if (isDanhMucLau.value && nuocLauDuocChon.value !== null) {
      handleFilterNuocLau({ id: nuocLauDuocChon.value });
    } else {
      loadMonAnTheoDanhMuc(danhMucDangChon.value);
    }
  } else if (data && data.tenDanhMuc !== undefined) {
    refreshDanhMucList();
  }
};
const coMonNgungKinhDoanh = computed(() => {
  return gioHang.value?.items?.some((i) => i.trangThaiXoa) || false;
});

onMounted(() => {
  loadThamSoDong();
  loadNuocLau();
  loadDanhMuc();

  connectWebSocket(null, handleWsMessage);

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
  
  // Save handleCartUpdated to a local variable for removal
  onUnmounted(() => {
    window.removeEventListener("cart-updated", handleCartUpdated);
    disconnectWebSocket(handleWsMessage, null);
  });
});

// Phân trang
const currentPage = ref(1);
const itemsPerPage = 6;

const totalPages = computed(() => {
  return Math.ceil(lstMonAn.value.length / itemsPerPage);
});

const paginatedMonAn = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return lstMonAn.value.slice(start, end);
});
</script>

<template>
  <main class="home-body pt-5">
    <header class="text-center mb-5 reveal">
      <span class="sub-title">exclusive dining</span>
      <h2 class="section-title">Thực Đơn Nhà Hàng</h2>
      <div class="luxury-line"></div>
    </header>
    <section class="reveal container mt-5">
      <div class="row">
        <div class="col-lg-6">
          <div class="promo-item overflow-hidden rounded-4 shadow-lg">
            <img
              src="@/assets/images/khuyenmai1.png"
              class="w-100 d-block hover-zoom promo-img"
              alt="Banner Khuyến Mãi"
            />
          </div>
        </div>
      </div>
    </section>

    <section class="py-5 container">
      <div class="row g-4">
        <aside class="col-lg-3 reveal-left">
          <div
            class="sidebar-container p-3 shadow-sm border rounded-4 bg-white sticky-top"
            style="top: 100px; z-index: 10"
          >
            <div class="menu-header-wrapper text-center mb-4">
              <div class="menu-header-box">
                <h5 class="menu-header-title">THỰC ĐƠN</h5>
              </div>
            </div>

            <div class="category-list">
              <div
                v-for="dm in danhMucHienThi"
                :key="dm.id"
                class="category-item-sidebar custom-menu-item"
                :class="{ active: danhMucDangChon?.id === dm.id }"
                @click="loadMonAnTheoDanhMuc(dm)"
              >
                {{ dm.tenDanhMuc }}
              </div>
            </div>
          </div>
        </aside>

        <div class="col-lg-9">
          <div
            class="reveal-right header-category-wrapper mb-4 pb-3 border-bottom"
          >
            <div class="row align-items-center g-3">
              <div class="col-12 col-md-4">
                <h2 class="category-title mb-0 text-uppercase">
                  {{ danhMucDangChon?.tenDanhMuc }}
                </h2>
              </div>

              <div class="col-12 col-md-8">
                <div
                  class="d-flex flex-wrap align-items-center justify-content-md-end gap-3"
                >
                  <div class="search-box-wrapper">
                    <i class="bi bi-search search-icon"></i>
                    <input
                      v-model="keyword"
                      type="text"
                      class="form-control search-input"
                      placeholder="Tìm tên món ăn..."
                      @input="handleFilter"
                    />
                  </div>

                  <div class="d-flex align-items-center gap-2">
                    <span
                      class="small fw-bold text-secondary text-uppercase text-nowrap"
                      >Sắp xếp</span
                    >
                    <select
                      v-model="sortType"
                      class="form-select form-select-sm rounded-pill border-2 shadow-none select-custom"
                      @change="handleFilter"
                    >
                      <option value="">Mặc định</option>
                      <option value="asc">Giá thấp nhất</option>
                      <option value="desc">Giá cao nhất</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div
            v-show="isDanhMucLau"
            class="oriental-filter-container mb-3 reveal-up"
          >
            <div class="oriental-wrapper d-flex align-items-center gap-3">
              <button class="oriental-nav prev">
                <i class="bi bi-chevron-left"></i>
              </button>
              <div class="oriental-list d-flex gap-4">
                <!-- TẤT CẢ -->
                <div
                  class="oriental-item"
                  :class="{ active: nuocLauDuocChon === null }"
                  @click="handleFilterNuocLau({ id: null })"
                >
                  <div class="corner-tl"></div>
                  <div class="corner-tr"></div>
                  <div class="corner-bl"></div>
                  <div class="corner-br"></div>
                  <span class="oriental-text">Tất cả</span>
                </div>

                <!-- API -->
                <div
                  v-for="item in danhSachNuocLau"
                  :key="item.id"
                  class="oriental-item"
                  :class="{ active: nuocLauDuocChon === item.id }"
                  @click="handleFilterNuocLau(item)"
                >
                  <div class="corner-tl"></div>
                  <div class="corner-tr"></div>
                  <div class="corner-bl"></div>
                  <div class="corner-br"></div>
                  <span class="oriental-text">
                    {{ item.tenNuocLau }}
                  </span>
                </div>
              </div>
              <button class="oriental-nav next">
                <i class="bi bi-chevron-right"></i>
              </button>
            </div>
          </div>

          <div class="row g-4">
            <div
              class="col-6 col-md-4"
              v-for="item in paginatedMonAn"
              :key="item.id"
            >
              <div
                class="product-card p-3 shadow-sm border rounded-2 h-100 d-flex flex-column"
              >
                <div
                  class="overflow-hidden rounded-3 mb-3"
                  style="position: relative"
                >
                  <img
                    :src="'http://localhost:8080/uploads/' + item.hinhAnh"
                    style="width: 100%; height: 200px; object-fit: cover"
                    class="main-img"
                  />
                  <div class="light-sweep"></div>
                </div>

                <h5 class="product-title text-uppercase mb-2">
                  {{ item.tenSanPham }}
                </h5>
                <div class="mt-auto product-price">
                  <template
                    v-if="item.giaSauGiam && item.giaSauGiam < item.gia"
                  >
                    <div class="d-flex align-items-center gap-2">
                      <span
                        class="text-muted text-decoration-line-through small"
                      >
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
                  <button class="btn btn-detail luxury-draw-btn">
                    Chi tiết
                  </button>
                </div>
              </div>
            </div>
            <div class="d-flex justify-content-center mt-5">
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
                    <button
                      class="page-link-custom"
                      @click="currentPage = page"
                    >
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
          </div>
        </div>
      </div>
    </section>

    <section class="reveal-up pt-5 pb-3 container text-center">
      <h2 class="section-title mb-5">CẢM NHẬN CỦA KHÁCH HÀNG</h2>

      <div class="row g-4">
        <div class="col-md-4" v-for="fb in feedbacks" :key="fb.name">
          <!-- CARD REVIEW -->
          <div class="review-card border rounded-4 p-3 bg-white shadow-sm">
            <img :src="fb.img" class="review-img mb-3" />

            <p class="review-text">
              {{ fb.comment }}
            </p>
          </div>

          <!-- USER INFO -->
          <div
            class="review-user mt-3 d-flex align-items-center justify-content-center gap-3"
          >
            <img :src="fb.avatar" class="review-avatar" />

            <div class="text-start">
              <div class="fw-bold small">{{ fb.name }}</div>
              <div class="text-muted smaller">{{ fb.role }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <div
      class="review-more d-flex justify-content-center align-items-center gap-4 mt-4"
    >
      <i class="bi bi-chevron-left review-arrow"></i>

      <router-link to="/danh-gia" class="review-all"> XEM TẤT CẢ </router-link>

      <i class="bi bi-chevron-right review-arrow"></i>
    </div>

    <section class="reveal-up py-5 container mb-5">
      <div class="comment-section p-4 border rounded-4 bg-white shadow-lg">
        <h5
          class="section-title mb-4 pb-3 border-bottom text-uppercase"
          style="font-size: 1.3rem"
        >
          Hỏi đáp & Bình luận
        </h5>

        <div
          class="row align-items-center border p-4 rounded-4 mb-4 mx-0 bg-texture"
        >
          <div class="col-md-6 border-end-md border-light-subtle">
            <p class="mb-2 fw-bold small text-uppercase">
              Đánh giá chung về
              <span class="text-danger">{{ danhMucDangChon?.tenDanhMuc }}</span>
            </p>
            <div
              v-for="star in [5, 4, 3, 2, 1]"
              :key="star"
              class="d-flex align-items-center gap-2 mb-1"
            >
              <span class="small text-muted fw-bold" style="width: 25px"
                >{{ star }}★</span
              >
              <div
                class="progress flex-grow-1 bg-white"
                style="height: 8px; border: 1px solid #eee"
              >
                <div class="progress-bar bg-warning" style="width: 0%"></div>
              </div>
              <span class="smaller text-muted">0 lượt</span>
            </div>
          </div>
          <div class="col-md-6 text-center mt-3 mt-md-0">
            <button
              class="btn btn-danger-dark rounded-pill px-4 py-2 fw-bold shadow-sm btn-modern"
            >
              Xác nhận
            </button>
          </div>
        </div>

        <div class="comment-list">
          <div class="d-flex gap-3 p-3 bg-light rounded-4 border mb-4">
            <div
              class="admin-avatar bg-secondary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold"
              style="width: 45px; height: 45px"
            >
              TV
            </div>
            <div>
              <div class="d-flex align-items-center gap-2 mb-1">
                <span class="fw-bold small">QUẢN TRỊ VIÊN</span>
                <span class="badge bg-danger rounded-pill px-2">OFFICIAL</span>
              </div>
              <p class="small mb-1 text-secondary">
                Chào bạn! Nếu bạn có bất kỳ thắc mắc nào về món ăn trong danh
                mục {{ danhMucDangChon?.tenDanhMuc }}, đừng ngần ngại để lại câu
                hỏi tại đây nhé.
              </p>
              <div class="d-flex gap-3 smaller text-muted fw-bold">
                <a href="#" class="text-danger text-decoration-none">Trả lời</a>
                <span>• Vừa xong</span>
              </div>
            </div>
          </div>
        </div>

        <div class="comment-form mt-4">
          <textarea
            class="form-control rounded-4 mb-3 border-2 shadow-none p-3"
            rows="3"
            placeholder="Mời bạn để lại bình luận..."
          ></textarea>
          <div class="text-start">
            <button
              class="btn btn-danger rounded-pill px-5 py-2 fw-bold btn-modern"
            >
              Xác nhận
            </button>
          </div>
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
.item-disabled {
  opacity: 0.5;
  pointer-events: none;
}
.item-disabled .btn-remove-luxury {
  pointer-events: all;
}
.btn-luxury-solid:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
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
  transform: rotate(180deg) scale(1);
  text-shadow: 0 0 15px rgba(212, 175, 55, 0.8);
}

.close-btn-luxury:active {
  transform: scale(0.9);
}
/* 
////////////////////// THẺ MÓN AN ????????????? */
.overflow-hidden {
  position: relative;
}

.light-sweep {
  position: absolute;
  top: 0;
  left: -150%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.3) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  transform: skewX(-25deg);
  transition: 0s;
}

.product-card:hover .light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

.product-card:hover .main-img {
  transform: scale(1.1);
  transition: 0.6s ease;
}

.luxury-order-btn {
  --btn-gold: #d4af37;
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

.luxury-order-btn:hover {
  background-color: #7b1417;
  border-color: #7b1417;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(163, 29, 33, 0.2);
}

.luxury-order-btn:hover .icon-default {
  opacity: 0;
  transform: scale(0.5) rotate(10deg);
}

.luxury-order-btn:hover .icon-check {
  opacity: 1;
  transform: scale(1.1) rotate(0deg);
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
  background-color: #d4af37;
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

/* /////////////////// LỌC NƯỚC LẨU ?????????????????? */
.oriental-filter-container {
  background: transparent;
}
.oriental-text {
  white-space: nowrap;
}
.oriental-list {
  overflow-x: auto;
  scrollbar-width: none;
  padding: 15px 5px;
}
.oriental-list::-webkit-scrollbar {
  display: none;
}
.oriental-item {
  white-space: nowrap;
  width: max-content;
  min-width: auto;
  flex-shrink: 0;
}
.oriental-item {
  position: relative;
  padding: 8px 20px;
  background: #fff;
  border: 1px solid #d4af37;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: fit-content;
  width: max-content;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.oriental-item [class^="corner-"] {
  position: absolute;
  width: 8px;
  height: 8px;
  border: 2px solid #d4af37;
  opacity: 0;
  transition: 0.3s;
}
.corner-tl {
  top: 4px;
  left: 4px;
  border-right: none !important;
  border-bottom: none !important;
}
.corner-tr {
  top: 4px;
  right: 4px;
  border-left: none !important;
  border-bottom: none !important;
}
.corner-bl {
  bottom: 4px;
  left: 4px;
  border-right: none !important;
  border-top: none !important;
}
.corner-br {
  bottom: 4px;
  right: 4px;
  border-left: none !important;
  border-top: none !important;
}

.oriental-text {
  font-family: "Playfair Display", serif;
  font-weight: 800;
  font-size: 13px;
  text-uppercase: uppercase;
  color: #333;
  letter-spacing: 1px;
  line-height: 1.8;
  z-index: 2;
}

.oriental-item.active {
  background: #df3a3a;
  border: 3px double #d4af37;
  box-shadow: 0 10px 25px rgba(163, 29, 33, 0.3);
  transform: translateY(-5px);
}

.oriental-item.active .oriental-text {
  color: #fff;
}

.oriental-item.active [class^="corner-"],
.oriental-item:hover [class^="corner-"] {
  opacity: 1;
}

.oriental-nav {
  width: 20px;
  height: 45px;
  background: #fff;

  border: 1px solid #a31d21;
  color: #a31d21;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: 0.3s;
}

.oriental-nav:hover {
  background: #a31d21;
  color: #fff;
}
/* -------------------------------------- */
.category-title {
  font-size: 2rem;
  font-weight: 700;
  font-family: "Playfair Display", serif;
  font-weight: 800;
  background: linear-gradient(45deg, #a31d21, #d32f2f);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  display: inline-block;
  line-height: 1.5;
  padding-top: 15px;
  margin-bottom: 0;
}
.section-title {
  font-family: "Playfair Display", serif;
  font-weight: 800;
  background: linear-gradient(45deg, #a31d21, #d32f2f);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  display: inline-block;
  line-height: 1.5;
  padding-top: 15px;
  margin-bottom: 0;
}

/* Style cho thanh tìm kiếm đẳng cấp */
.search-box-wrapper {
  position: relative;
  min-width: 250px;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #a31d21;
  font-size: 0.9rem;
}

.search-input {
  padding-left: 40px;
  padding-right: 20px;
  height: 40px;
  border-radius: 50px;
  border: 2px solid #eee;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.3s ease;
  background: #fff;
}

.search-input:focus {
  border-color: #a31d21;
  box-shadow: 0 5px 15px rgba(163, 29, 33, 0.1);
  width: 280px; /* Hiệu ứng dãn nhẹ khi focus */
}

/* Đồng bộ Select Box */
.select-custom {
  height: 40px;
  padding-left: 15px;
  padding-right: 35px;
  border-color: #eee;
  font-weight: 600;
  color: #555;
  cursor: pointer;
  transition: all 0.3s;
}

.select-custom:focus {
  border-color: #a31d21;
}

@media (max-width: 768px) {
  .search-box-wrapper {
    width: 100%;
  }
  .search-input:focus {
    width: 100%;
  }
}
/* Khung tiêu đề đỏ */
.menu-header-box {
  background-color: #e63a3f;
  border: 3px double #ffffff;
  outline: 2px solid #a31d21;
  padding: 8px 25px;
  display: inline-block;
  position: relative;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* Thêm họa tiết nhỏ ở 4 góc (tùy chọn) */
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
  font-size: 1.1rem;
}

/* Kiểu dáng các mục menu phía dưới */
.custom-menu-item {
  cursor: pointer;
  padding: 12px 15px;
  margin-bottom: 5px;
  border-bottom: 1px solid #f0f0f0;
  color: #333;
  font-weight: 700;
  text-transform: uppercase;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.custom-menu-item:hover {
  background-color: #fff5f5;
  color: #a31d21;
}

/* Hiệu ứng khi đang chọn (Active) */
.custom-menu-item.active {
  color: #a31d21;
  background-color: #fff0f0;
  border-left: 4px solid #a31d21;
}
/* --- PHONG CÁCH MENU TÂN CỔ ĐIỂN --- */

.custom-menu-item {
  display: block;
  text-align: center;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 1px;
  font-size: 0.95rem;
  padding: 10px 15px;
  margin: 5px auto;
  max-width: 250px; /* Điều chỉnh độ rộng phù hợp */
  cursor: pointer;
  transition: all 0.2s ease;

  border: 2px double #ffffff;
  outline: 1px solid #ddd;
  border-radius: 2px;
}

.custom-menu-item {
  background-color: #fcfcfc;
  color: #333;
}

.custom-menu-item:hover {
  background-color: #f1f1f1;
  outline-color: #bbb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.custom-menu-item.active {
  background-color: #a31d21;
  color: #ffffff;
  outline-color: #a31d21;

  box-shadow: 0 4px 6px rgba(163, 29, 33, 0.2);
  border: 3px double #ffffff;
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
/* =========================
   STYLE ĐỒNG BỘ TRANG CHỦ
========================= */
.luxury-line {
  width: 40px;
  height: 2px;
  background: #c5a059;
  margin: 20px auto;
}
.section-title {
  margin-top: 5px;
  font-family: "Playfair Display", serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a1a1a;
  display: inline-block;
  line-height: 1.4;
  padding-top: 10px;

  vertical-align: middle;
}

.sub-title {
  text-transform: uppercase;
  letter-spacing: 6px;
  font-size: 0.7rem;
  color: #c5a059;
  font-weight: 700;
  display: block;
  margin-bottom: 10px;
}

.reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: 0.8s cubic-bezier(0.2, 1, 0.3, 1);
}
.reveal.active {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 768px) {
  .booking-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .section-title {
    font-size: 1.8rem;
  }
}

.home-body {
  background: linear-gradient(180deg, #fff, #fff6f6);
}

.section-title {
  font-weight: 800;
  background: linear-gradient(45deg, #b80000, #ff3c3c);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.bg-texture {
  background-image: radial-gradient(#ff000005 1px, transparent 1px);
  background-size: 20px 20px;
}

/* Sidebar */
.category-item-sidebar {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #666;
  font-size: 0.9rem;
}
.category-item-sidebar:hover {
  background: #fff5f5;
  color: #dc3545;
  transform: translateX(8px);
}
.category-item-sidebar.active {
  background: linear-gradient(45deg, #b80000, #ff3c3c);
  color: white;
  box-shadow: 0 8px 20px rgba(184, 0, 0, 0.2);
}

/* Product Card */
.product-card {
  background: white;
  transition: 0.35s;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
}
.product-card:hover {
  transform: translateY(-12px) scale(1.02);
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.18);
}

/* Feedback Card (Đồng bộ trang chủ) */
.feedback-card {
  background: white;
  transition: 0.35s;
  border: 1px solid #eee;
}
.feedback-card:hover {
  transform: translateY(-10px) rotate(1deg);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.15);
  border-color: #ffc107;
}
.feedback-img {
  width: 100%;
  height: 220px; /* chiều cao cố định */
  object-fit: cover; /* cắt ảnh cho vừa khung */
  border-radius: 12px;
}
.feedback-card:hover .feedback-img {
  transform: scale(1.1);
}

/* Buttons & UI */
.btn-modern {
  transition: 0.35s;
}
.btn-modern:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.2);
}
.btn-warning-gold {
  background: linear-gradient(135deg, #ffc107, #ffdd55);
  border: none;
  color: #000;
}
.btn-danger-dark {
  background-color: #8b1515;
  color: white;
  border: none;
}

.select-custom {
  border-color: #eee;
  font-weight: 600;
  color: #555;
}
.page-link {
  width: 42px;
  height: 42px;
  border-radius: 50% !important;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  transition: 0.3s;
}
.page-item.active .page-link {
  background: linear-gradient(45deg, #b80000, #ff3c3c);
  border: none;
  color: white;
}

/* Reveal Animation */
.reveal,
.reveal-left,
.reveal-right,
.reveal-up {
  opacity: 0;
  transition: transform 0.9s cubic-bezier(0.2, 0.6, 0.3, 1), opacity 0.8s ease;
}
.reveal {
  transform: translateY(70px) scale(0.95);
}
.reveal-left {
  transform: translateX(-80px);
}
.reveal-right {
  transform: translateX(80px);
}
.reveal-up {
  transform: translateY(80px);
}

.active {
  opacity: 1 !important;
  transform: translate(0) scale(1) !important;
}

.hover-zoom {
  transition: transform 0.8s cubic-bezier(0.2, 0.6, 0.3, 1);
}

@media (max-width: 991px) {
  .category-list {
    display: flex;
    overflow-x: auto;
    gap: 8px;
    scrollbar-width: none;
  }
  .category-list::-webkit-scrollbar {
    display: none;
  }
  .category-item-sidebar {
    white-space: nowrap;
    padding: 8px 18px !important;
  }
  .border-end-md {
    border-end: none !important;
    border-bottom: 1px solid #dee2e6;
    padding-bottom: 1.5rem;
  }
}

.review-card {
  transition: 0.3s;
}

.review-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
}

.review-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 12px;
}

.review-text {
  font-size: 14px;
  color: #666;
  font-style: italic;
  position: relative;
  padding: 10px 5px;
}

.review-text::before {
  content: "“";
  font-size: 40px;
  color: #ddd;
  margin-right: 6px;
}

.review-text::after {
  content: "”";
  font-size: 40px;
  color: #ddd;
  float: right;
}

.review-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.review-more {
  margin-top: 10px;
  margin-bottom: 10px;
}
.review-all {
  font-weight: 600;
  color: #1a2a44;
  text-decoration: none;
  border-bottom: 2px solid #1a2a44;
  padding-bottom: 2px;
}

.review-all:hover {
  color: #b30000;
  border-color: #b30000;
}

.review-arrow {
  font-size: 22px;
  color: #999;
  cursor: pointer;
  transition: 0.2s;
}

.review-arrow:hover {
  color: #333;
  transform: scale(1.2);
}

/* Nút đặt món */
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

@media (max-width: 991px) {
  /* Biến Sidebar thành thanh trượt ngang */
  .sidebar-container {
    position: sticky !important;
    top: 60px !important; /* Khớp với chiều cao navbar của bạn */
    z-index: 999 !important;
    margin: -20px -15px 20px -15px !important;
    border-radius: 0 !important;
    border-left: none !important;
    border-right: none !important;
    background: rgba(255, 255, 255, 0.9) !important;
    backdrop-filter: blur(10px);
  }

  .sidebar-header {
    display: none;
  }

  .category-list {
    display: flex !important;
    overflow-x: auto !important;
    padding: 10px !important;
    gap: 10px !important;
    scroll-behavior: smooth;
    -webkit-overflow-scrolling: touch;
  }

  .category-list::-webkit-scrollbar {
    display: none; /* Ẩn thanh cuộn */
  }

  .category-item-sidebar {
    white-space: nowrap;
    padding: 8px 20px !important;
    margin-bottom: 0 !important;
    border-radius: 50px !important; /* Bo tròn kiểu viên thuốc */
    font-size: 0.85rem !important;
    background: #f8f9fa;
    border: 1px solid #eee;
  }

  .category-item-sidebar.active {
    transform: none !important;
    background: linear-gradient(45deg, #b80000, #ff3c3c) !important;
  }
}

@media (max-width: 576px) {
  /* Tiêu đề mục lớn */
  .section-title {
    font-size: 1.6rem !important;
  }

  /* Card sản phẩm */
  .product-card {
    padding: 10px !important;
    border-radius: 15px !important;
  }

  .product-title {
    font-size: 0.85rem !important;
    min-height: 38px; /* Đảm bảo các card bằng chiều cao nhau */
    line-height: 1.3;
  }

  .product-card .text-danger.fw-bold {
    font-size: 0.95rem !important;
  }

  /* Thu nhỏ các nút bấm */
  .btn-modern {
    padding: 8px 5px !important;
    font-size: 0.75rem !important;
  }

  .page-link-custom {
    width: 35px;
    height: 35px;
    font-size: 0.8rem;
  }
}

@media (max-width: 768px) {
  .review-img {
    height: 160px;
  }

  .review-text {
    font-size: 13px !important;
  }

  .bg-texture {
    padding: 1.5rem !important;
    margin-bottom: 20px !important;
  }

  .border-end-md {
    border-right: none !important;
    border-bottom: 1px solid #eee;
    padding-bottom: 20px;
    margin-bottom: 20px;
  }

  .admin-avatar {
    width: 35px !important;
    height: 35px !important;
    font-size: 12px;
  }
}

/* CUSTOM ADDITIONAL RESPONSIVE & ZOOM OVERRIDES */
@media (max-width: 991px) {
  .category-item-sidebar {
    flex-shrink: 0 !important;
  }
}
</style>

<style>
/* ===== MINI CART TELEPORT (global - scoped không apply được sau Teleport) ===== */
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
  background: rgba(0, 0, 0, 0.6);
  z-index: 10000;
  backdrop-filter: blur(4px);
}
.cart-modal-luxury {
  position: fixed;
  top: 0;
  right: 0;
  height: 100vh;
  width: 420px;
  z-index: 10001;
  display: flex;
  flex-direction: column;
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

  .cart-modal-luxury {
    width: 100% !important;
    max-width: 100vw !important;
  }

  .cart-modal-luxury .cart-modal-content {
    width: 100% !important;
    height: 100vh !important;
    max-height: 100vh !important;
    border: none !important;
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
</style>
