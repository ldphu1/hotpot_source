import axiosClient from "@/services/axiosClient";

const API_URL = "http://localhost:8080/api/admin/check-in";

export const getDanhSachBan = () => {
  return axiosClient.get(`${API_URL}/hien-thi`);
};

export const getChiTietCheckIn = (idBanAn) => {
  return axiosClient.get(`${API_URL}/chi-tiet/${idBanAn}`);
};

export const doiTrangThaiCheckIn = (data) => {
  return axiosClient.post(`${API_URL}/xac-nhan`, data);
};

// lấy toàn bộ thực đơn
export const getThucDon = () => {
  return axiosClient.get(`${API_URL}/thuc-don`);
};

// món lẻ
export const getMonLe = () => {
  return axiosClient.get(`${API_URL}/thuc-don/mon-le`);
};

// set lẩu
export const getSetLau = () => {
  return axiosClient.get(`${API_URL}/thuc-don/set-lau`);
};

// search món lẻ
export const searchMonLe = (params) => {
  return axiosClient.get(`${API_URL}/thuc-don/search-mon-le`, {
    params,
  });
};

// search set lẩu
export const searchSetLauCheckIn = (params) => {
  return axiosClient.get(`${API_URL}/thuc-don/search-set-lau`, {
    params,
  });
};

// thêm món vào hóa đơn
export const themMonVaoHoaDon = (data) => {
  return axiosClient.post(`${API_URL}/them-mon`, data);
};

export const themNhieuMonVaoHoaDon = (dataList) => {
  return axiosClient.post(`${API_URL}/them-nhieu-mon`, dataList);
};

export const getMonDaChonTheoHoaDon = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/hoa-don/${idHoaDon}/mon-da-chon`);
};

// lịch sử hóa đơn
export const getLichSuHoaDon = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/lich-su-hoa-don/${idHoaDon}`);
};

// ================== ĐÃ LÊN 1 MÓN ==================
export const daLenMon = (idHoaDonChiTiet, tenBan = "") => {
  return axiosClient.put(`${API_URL}/mon/${idHoaDonChiTiet}/da-len`, null, {
    params: { tenBan },
  });
};

// ================== ĐÃ LÊN TẤT CẢ ==================
export const daLenTatCa = (idHoaDon, tenBan = "") => {
  return axiosClient.put(`${API_URL}/hoa-don/${idHoaDon}/da-len-tat-ca`, null, {
    params: { tenBan },
  });
};

// ================== XÓA MÓN ==================
export const xoaMonKhoiHoaDon = (idHoaDonChiTiet, tenBan = "") => {
  return axiosClient.delete(`${API_URL}/mon/${idHoaDonChiTiet}`, {
    params: { tenBan },
  });
};

// ================== GIẢM GIÁ ==================

// danh sách phiếu hợp lệ chung theo hóa đơn
export const getDanhSachPhieuGiamGiaHopLe = (idHoaDon) => {
  return axiosClient.get(
    `${API_URL}/hoa-don/${idHoaDon}/phieu-giam-gia-hop-le`,
  );
};

// danh sách phiếu giảm giá theo khách hàng của hóa đơn
export const getDanhSachPhieuGiamGiaTheoKhachHang = (idHoaDon) => {
  return axiosClient.get(
    `${API_URL}/hoa-don/${idHoaDon}/phieu-giam-gia-khach-hang`,
  );
};

// lấy phiếu giảm giá tốt nhất
export const getPhieuGiamGiaTotNhat = (idHoaDon) => {
  return axiosClient.get(
    `${API_URL}/hoa-don/${idHoaDon}/phieu-giam-gia-tot-nhat`,
  );
};

// áp dụng mã giảm giá
export const apDungMaGiamGia = (idHoaDon, data) => {
  return axiosClient.post(
    `${API_URL}/hoa-don/${idHoaDon}/ap-dung-ma-giam-gia`,
    data,
  );
};

// áp dụng giảm giá trực tiếp
export const apDungGiamGiaTrucTiep = (idHoaDon, data) => {
  return axiosClient.post(
    `${API_URL}/hoa-don/${idHoaDon}/ap-dung-giam-gia-truc-tiep`,
    data,
  );
};

// áp dụng điểm tích lũy
export const apDungDiemTichLuy = (idHoaDon, data) => {
  return axiosClient.post(
    `${API_URL}/hoa-don/${idHoaDon}/ap-dung-diem-tich-luy`,
    data,
  );
};

// hiển thị bàn trống
export const getBanTrong = () => {
  return axiosClient.get(`${API_URL}/ban-trong`);
};

// đổi bàn
export const doiBan = (data) => {
  return axiosClient.put(`${API_URL}/doi-ban`, data);
};

// ghép bàn trống
export const ghepBan = (data) => {
  return axiosClient.put(`${API_URL}/ghep-ban`, data);
};

// tách bàn
export const tachBan = (data) => {
  return axiosClient.put(`${API_URL}/tach-ban`, data);
};

// ghép 2 bàn đang dùng
export const ghepHaiBanDangDung = (data) => {
  return axiosClient.put(`${API_URL}/ghep-hai-ban-dang-dung`, data);
};

// lấy danh sách hóa đơn đang dùng theo bàn
export const getHoaDonDangDungTheoBan = (idBanAn) => {
  return axiosClient.get(`${API_URL}/ban/${idBanAn}/hoa-don-dang-dung`);
};

// lấy chi tiết check-in theo bàn + hóa đơn
export const getChiTietCheckInTheoHoaDon = (idBanAn, idHoaDon) => {
  return axiosClient.get(`${API_URL}/chi-tiet/${idBanAn}/hoa-don/${idHoaDon}`);
};

// TẠO HÓA ĐƠN MỚI TRÊN BÀN ĐANG DÙNG
export const taoHoaDonMoiTrenBan = (data) => {
  return axiosClient.post(`${API_URL}/tao-hoa-don-moi-tren-ban`, data);
};

// tìm kiếm đặt bàn
export const searchDatBanCheckIn = (keyword) => {
  return axiosClient.get(`${API_URL}/search-dat-ban`, {
    params: { keyword },
  });
};

// ================== XUẤT PDF HÓA ĐƠN ==================
export const exportPdfHoaDon = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/hoa-don/${idHoaDon}/export-pdf`, {
    responseType: "blob",
  });
};

// ================== XUẤT PDF GỌI MÓN ==================
export const exportPdfGoiMon = (idHoaDon, chiTietIds = []) => {
  return axiosClient.get(`${API_URL}/hoa-don/${idHoaDon}/export-pdf-goi-mon`, {
    params: { chiTietIds: chiTietIds.join(",") },
    responseType: "blob",
  });
};

// xác thực mã đặt bàn
export const xacThucMaDatBan = (idDatBan, maDatBan) => {
  return axiosClient.get(`${API_URL}/xac-thuc-ma`, {
    params: { idDatBan, maDatBan },
  });
};

// xác thực mã đặt bàn và check in luôn
export const xacThucVaCheckIn = (data) => {
  return axiosClient.post(`${API_URL}/xac-thuc-va-checkin`, data);
};

export const getDanhSachPhieuGiamGiaGoiY = (idHoaDon) => {
  return axiosClient.get(`${API_URL}/hoa-don/${idHoaDon}/phieu-giam-gia-goi-y`);
};

// ================== HỦY MÓN KHỎI HÓA ĐƠN ==================
export const huyMonKhoiHoaDon = (
  idHoaDon,
  idHoaDonChiTiet,
  tenBan = "",
  idBanAn = null,
) => {
  return axiosClient.delete(
    `${API_URL}/hoa-don/${idHoaDon}/chi-tiet/${idHoaDonChiTiet}`,
    {
      params: {
        tenBan,
        idBanAn,
      },
    },
  );
};
