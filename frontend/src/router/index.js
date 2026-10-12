import { createRouter, createWebHistory } from "vue-router";
import { ref } from "vue";
export const transitionName = ref("slide-left");
import { useUtils } from "@/utils/helper";
const { notify, nprogress } = useUtils();

// Layout
import MainLayout from "@/layout/admin/main/MainLayout.vue";

// Public view

// Admin views
import ThongKe from "@/views/admin/ThongKe.vue";
import DatBan from "@/views/admin/DatBan.vue";
import DonHang from "@/views/admin/DonHang.vue";
import CheckIn from "@/views/admin/CheckIn.vue";
import QuanLyBan from "@/views/admin/QuanLyBan.vue";
import NhanVien from "@/views/admin/NhanVien.vue";
import LichLamViec from "@/views/admin/LichLamViec.vue";
import KhachHang from "@/views/admin/KhachHang.vue";
import DanhMuc from "@/views/admin/DanhMuc.vue";
import KhuyenMai from "@/views/admin/KhuyenMai.vue";
import NhanTin from "@/views/admin/NhanTin.vue";
import ThamSoDong from "@/views/admin/ThamSoDong.vue";
import OAuth2Success from "@/views/admin/OAuth2Success.vue";
import DotGiamGia from "@/components/admin/DotGiamGia/DotGiamGia.vue";

// Thực đơn chi tiết
import Login from "@/components/admin/Login/Login.vue";
import dangky from "@/components/customer/DangKy/dangky.vue";
import HoaDonChiTiet from "@/components/admin/DonHang/HoaDonChiTiet.vue";
import NuocLau from "@/components/admin/NuocLau/NuocLau.vue";
import ThemNuocLau from "@/components/admin/NuocLau/ThemNuocLau.vue";
import ThemSetLau from "@/components/admin/Lau/ThemSetLau.vue";
import ThemNhanVien from "@/components/admin/NhanVien/ThemNhanVien.vue";
import ThemKhachHang from "@/components/admin/KhachHang/ThemKhachHang.vue";
import SuaKhachHang from "@/components/admin/KhachHang/SuaKhachHang.vue";
import SuaNhanVien from "@/components/admin/NhanVien/SuaNhanVien.vue";
import SanPham from "@/components/admin/DanhMuc/SanPham.vue";
import SetLau from "@/components/admin/Lau/SetLau.vue";
import ChiTietSetLau from "@/components/admin/Lau/ChiTietSetLau.vue";
import ThemSanPham from "@/components/admin/DanhMuc/ThemSanPham.vue";
import SuaSanPham from "@/components/admin/DanhMuc/SuaSanPham.vue";
import SuaSetLau from "@/components/admin/Lau/SuaSetLau.vue";
import QuanLyDinhLuong from "@/components/admin/ThuocTinh/QuanLyDinhLuong.vue";
import SuaNuocLau from "@/components/admin/NuocLau/SuaNuocLau.vue";
import ThemPhieuDatBan from "@/components/admin/DatBan/ThemPhieuDatBan.vue";
import OderView from "@/components/admin/Order/OderView.vue";
import AddDotGiamGia from "@/components/admin/DotGiamGia/AddDotGiamGia.vue";
import UpdateDotGiamGia from "@/components/admin/DotGiamGia/UpdateDotGiamGia.vue";
import ChiTietDotGiamGia from "@/components/admin/DotGiamGia/ChiTietDotGiamGia.vue";
import DatBanChiTiet from "@/components/admin/DatBan/DatBanChiTiet.vue";
import ThemPhieuGiamGia from "@/components/admin/PhieuGiamGia/ThemPhieuGiamGia.vue";
import SuaPhieuGiamGia from "@/components/admin/PhieuGiamGia/SuaPhieuGiamGia.vue";
import ThanhToanHoaDon from "@/components/admin/CheckIn/ThanhToan.vue";
import ThanhToanKetQua from "@/components/admin/CheckIn/ThanhToanKetQua.vue";
import QRChonMonAn from "@/components/admin/CheckIn/QRChonMonAn.vue";
import CheckInChiTiet from "@/components/admin/CheckIn/CheckInChiTiet.vue";
import XemDonHangCheckIn from "@/components/admin/CheckIn/XemDonHangCheckIn.vue";
import ChonBanCheckIn from "@/components/admin/CheckIn/ChonBanCheckIn.vue";

/////////////////// CUSTOMER ////////////////////////
import TrangChu from "@/components/customer/GioiThieu/TrangChu.vue";
import GioiThieuCustomer from "@/components/customer/MenuCustomer/GioiThieuCustomer.vue";
import KhuyenMaiCustomer from "@/components/customer/MenuCustomer/KhuyenMaiCustomer.vue";
import ThucDonCustomer from "@/components/customer/MenuCustomer/ThucDonCustomer.vue";
import TinTucCustomer from "@/components/customer/MenuCustomer/TinTucCustomer.vue";
import DatBanCustomer from "@/components/customer/MenuCustomer/DatBanCustomer.vue";
import ThongTinDatBanCustomer from "@/components/customer/MenuCustomer/ThongTinDatBanCustomer.vue";
import LichSuChiTiet from "@/components/customer/lichsuchitiet/LichSuChiTiet.vue";
import LoginCustomer from "@/components/customer/login/LoginCustomer.vue";
import CustomerAccount from "@/components/customer/account/CustomerAccount.vue";
import CustomerLayout from "@/layout/customer/CustomerLayout.vue";
import PaymentSuccess from "@/components/customer/Payment/PaymentSuccess.vue";
import PaymentFail from "@/components/customer/Payment/PaymentFail.vue";
import DanhGia from "@/components/admin/DanhGia/DanhGia.vue";
import ThongTinCaNhan from "@/components/admin/NhanVien/ThongTinCaNhan.vue";
import KhachHangChiTiet from "@/components/admin/KhachHang/KhachHangChiTiet.vue";
import QuyDinhCustomer from "@/components/customer/MenuCustomer/QuyDinhCustomer.vue";
import TraCuuDonHangCustomer from "@/components/customer/MenuCustomer/TraCuuDonHangCustomer.vue";
import Forbidden from "@/components/error/Forbidden.vue";
import NotFound from "@/components/error/NotFound.vue";
import HoaDonTreo from "@/components/admin/BanAo/HoaDonTreo.vue";
import ThanhToanHoaDonTreo from "@/components/admin/BanAo/ThanhToanHoaDonTreo.vue";
import SuaPhieuDatBan from "@/components/admin/DatBan/SuaPhieuDatBan.vue";
import PaymentBanAoSuccess from "@/components/customer/Payment/PaymentBanAoSuccess.vue";
import PaymentBanAoFail from "@/components/customer/Payment/PaymentBanAoFail.vue";
const routes = [
  // ==== CUSTOMER ===
  {
    path: "/",
    component: CustomerLayout,
    children: [
      {
        path: "/",
        component: TrangChu,
      },
      {
        path: "gioi-thieu",
        component: GioiThieuCustomer,
      },
      {
        path: "khuyen-mai",
        component: KhuyenMaiCustomer,
      },
      {
        path: "thuc-don",
        component: ThucDonCustomer,
      },
      {
        path: "tin-tuc",
        component: TinTucCustomer,
      },
      {
        path: "thong-tin-dat-ban",
        component: ThongTinDatBanCustomer,
      },
      {
        path: "quy-dinh",
        component: QuyDinhCustomer,
      },
      {
        path: "tra-cuu",
        component: TraCuuDonHangCustomer,
      },
      {
        path: "dat-ban",
        component: DatBanCustomer,
      },
      {
        path: "tai-khoan",
        component: CustomerAccount,
      },
      {
        path: "lich-su/detail/:id",
        component: LichSuChiTiet,
      },
    ],
  },
  {
    path: "/oauth2/success",
    component: OAuth2Success,
  },

  // ===== AUTH =====
  {
    path: "/dang-ky",
    name: "DangKy",
    component: dangky,
  },
  {
    path: "/dang-nhap",
    name: "login-customer",
    component: LoginCustomer,
  },
  {
    path: "/admin/login",
    name: "login-admin",
    component: Login,
  },

  {
    path: "/qr-chon-mon",
    name: "QRChonMonAn",
    component: QRChonMonAn,
  },

  // ===== ADMIN =====
  {
    path: "/admin",
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: "/admin/tong-quan", component: ThongKe },
      // ĐẶt bàn
      { path: "/admin/dat-ban", component: DatBan, meta: { menu: "dat-ban" } },
      {
        path: "/admin/dat-ban/detail/:id",
        component: DatBanChiTiet,
        meta: { menu: "dat-ban" },
      },
      {
        path: "/admin/dat-ban/edit/:id",
        component: SuaPhieuDatBan,
        meta: { menu: "dat-ban" },
      },
      {
        path: "/admin/dat-ban/save",
        component: ThemPhieuDatBan,
        meta: { menu: "dat-ban" },
      },
      // Hóa đơn
      {
        path: "/admin/hoa-don",
        component: DonHang,
        meta: { menu: "hoa-don" },
      },
      {
        path: "/admin/hoa-don-treo",
        component: HoaDonTreo,
        meta: { menu: "hoa-don-treo" },
      },
      {
        path: "/admin/hoa-don-treo/thanh-toan/:idBanAo",
        component: ThanhToanHoaDonTreo,
        meta: { menu: "hoa-don-treo" },
      },
      {
        path: "/admin/hoa-don/detail/:id",
        name: "HoaDonChiTiet",
        component: HoaDonChiTiet,
        meta: { menu: "hoa-don" },
      },
      // Check in
      { path: "/admin/check-in", component: CheckIn },
      {
        path: "/admin/thanh-toan/:idHoaDon",
        name: "ThanhToanHoaDon",
        component: ThanhToanHoaDon,
      },
      {
        path: "/admin/check-in/chi-tiet/:id",
        name: "CheckInChiTiet",
        component: CheckInChiTiet,
      },
      {
        path: "/admin/check-in/xem-don-hang/:id",
        name: "XemDonHangCheckIn",
        component: XemDonHangCheckIn,
      },
      {
        path: "/admin/check-in/chon-ban/:id",
        name: "ChonBanCheckIn",
        component: ChonBanCheckIn,
      },

      { path: "/admin/order/:idHoaDon", component: OderView },
      // QL bàn
      { path: "/admin/quan-ly-ban", component: QuanLyBan },
      // Nhân viên
      { path: "/admin/nhan-vien", component: NhanVien },
      { path: "/admin/lich-lam-viec", component: LichLamViec },
      {
        path: "/admin/giao-ca",
        component: () =>
          import("@/components/admin/LichLamViec/LichSuHoatDong.vue"),
      },
      {
        path: "/admin/chi-tiet-lich-lam-viec",
        component: () =>
          import("@/components/admin/LichLamViec/ChiTietLichLamViec.vue"),
      },
      { path: "/admin/nhan-vien/save", component: ThemNhanVien },
      { path: "/admin/nhan-vien/me", component: ThongTinCaNhan },
      {
        path: "/admin/nhan-vien/sua/:id",
        component: SuaNhanVien,
      },
      // Khách hàng
      { path: "/admin/khach-hang", component: KhachHang },
      { path: "/admin/khach-hang/save", component: ThemKhachHang },
      {
        path: "/admin/khach-hang/sua/:id",
        component: SuaKhachHang,
      },
      {
        path: "/admin/khach-hang/:id",
        component: KhachHangChiTiet,
      },
      // Danh mục và sản phẩm
      {
        path: "/admin/danh-muc-cha",
        component: DanhMuc,
        meta: { menu: "danh-muc-cha" },
      },
      {
        path: "/admin/san-pham/hien-thi-all",
        component: SanPham,
        meta: { menu: "san-pham" },
      },
      {
        path: "/admin/san-pham/save",
        component: ThemSanPham,
        meta: { menu: "san-pham" },
      },
      {
        path: "/admin/san-pham/update/:id",
        component: SuaSanPham,
        meta: { menu: "san-pham" },
      },

      {
        path: "/admin/san-pham/by-danh-muc/:idDanhMuc",
        component: SanPham,
        meta: { menu: "san-pham" },
      },
      // Set lẩu và nước lẩu
      {
        path: "/admin/set-lau/hien-thi",
        component: SetLau,
        meta: { menu: "set-lau" },
      },
      {
        path: "/admin/set-lau/view-save",
        component: ThemSetLau,
        meta: { menu: "set-lau" },
      },
      {
        path: "/admin/set-lau/detail/:id",
        component: ChiTietSetLau,
        meta: { menu: "set-lau" },
      },
      {
        path: "/admin/set-lau/sua/:id",
        component: SuaSetLau,
        meta: { menu: "set-lau" },
      },
      {
        path: "/admin/thuc-don/nuoc-lau",
        component: NuocLau,
        meta: { menu: "nuoc-lau" },
      },
      {
        path: "/admin/thuc-don/nuoc-lau/sua/:id",
        component: SuaNuocLau,
        meta: { menu: "nuoc-lau" },
      },
      {
        path: "/admin/thuc-don/nuoc-lau/save",
        component: ThemNuocLau,
        meta: { menu: "nuoc-lau" },
      },
      //PHIEU GIAM GIA
      {
        path: "/admin/phieu-giam-gia",
        component: KhuyenMai,
        meta: { menu: "phieu-giam-gia" },
      },
      {
        path: "/admin/phieu-giam-gia/sua/:id",
        component: SuaPhieuGiamGia,
        meta: { menu: "phieu-giam-gia" },
      },
      {
        path: "/admin/phieu-giam-gia/save",
        component: ThemPhieuGiamGia,
        meta: { menu: "phieu-giam-gia" },
      },

      // Đợt giảm giá
      { path: "/admin/dot-giam-gia", component: DotGiamGia },
      {
        path: "/admin/dot-giam-gia/add",
        component: AddDotGiamGia,
      },
      {
        path: "/admin/dot-giam-gia/update/:id",
        name: "UpdateDotGiamGia",
        component: UpdateDotGiamGia,
      },
      {
        path: "/admin/dot-giam-gia/detail/:id",
        name: "ChiTietDotGiamGia",
        component: ChiTietDotGiamGia,
      },

      { path: "/admin/nhan-tin", component: NhanTin },
      { path: "/admin/phan-hoi", component: DanhGia },
      { path: "/admin/tham-so-dong", component: ThamSoDong },

      // submenu

      {
        path: "/admin/dinh-luong/hien-thi",
        component: QuanLyDinhLuong,
      },
    ],
  },
  ////////////////////////// THANH TOAN THANH CONG & THAT BAI ///////////////////
  { path: "/payment-success", component: PaymentSuccess },
  { path: "/payment-banao-success", component: PaymentBanAoSuccess },
  { path: "/payment-banao-fail", component: PaymentBanAoFail },
  { path: "/payment-fail", component: PaymentFail },
  ////////////////////////// ERROR PAGES ///////////////////
  { path: "/403", component: Forbidden },
  { path: "/404", component: NotFound },
  { path: "/:pathMatch(.*)*", redirect: "/404" },
];

routes.push({
  path: "/thanh-toan/ket-qua",
  component: ThanhToanKetQua,
});

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});
router.beforeEach((to, from, next) => {
  const toDepth = to.path.split("/").length;
  const fromDepth = from.path.split("/").length;
  transitionName.value = toDepth < fromDepth ? "slide-right" : "slide-left";

  const token = localStorage.getItem("token");

  if (to.meta.requiresAuth && !token) {
    notify.error("Bạn cần đăng nhập để tiếp tục");
    setTimeout(() => {
      next("/admin/login");
    }, 1500);
  } else {
    next();
  }
});
export default router;
