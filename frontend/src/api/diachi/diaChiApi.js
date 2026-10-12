import axios from "axios";

// Lấy tất cả tỉnh/thành phố
export const getProvinces = async () => {
  const res = await axios.get("/api-address/api/p/");
  return res.data;
};

// Lấy chi tiết tỉnh/thành phố (bao gồm cả huyện, xã)
export const getProvinceDetail = async (code) => {
  const res = await axios.get(`/api-address/api/p/${code}?depth=3`);
  return res.data;
};

// Lấy danh sách phường/xã phẳng của một tỉnh
export const getWardsByProvince = async (code) => {
  if (!code) return [];
  const res = await axios.get(`/api-address/api/p/${code}?depth=3`);
  const districts = res.data.districts || [];
  const allWards = [];
  for (const dist of districts) {
    if (dist.wards) {
      allWards.push(...dist.wards);
    }
  }
  return allWards;
};

