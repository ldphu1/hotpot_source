class CacheService {
  /**
   * Lưu dữ liệu vào localStorage với thời gian hết hạn
   * @param {string} key Tên key cache
   * @param {any} value Giá trị cần lưu (Object/Array/String...)
   * @param {number} ttl Thời gian sống (phút) - Mặc định 10 phút
   */
  static set(key, value, ttl = 10) {
    const now = new Date();
    const item = {
      value: value,
      expiry: now.getTime() + ttl * 60000, // ttl tính theo phút
    };
    localStorage.setItem(key, JSON.stringify(item));
  }
  /**
   * Lấy dữ liệu từ cache
   * @param {string} key Tên key cache
   * @returns {any|null} Dữ liệu nếu còn hạn, null nếu hết hạn hoặc không tồn tại
   */
  static get(key) {
    const itemStr = localStorage.getItem(key);
    
    // Nếu không có
    if (!itemStr) return null;
    try {
      const item = JSON.parse(itemStr);
      const now = new Date();
      // Kiểm tra hết hạn chưa
      if (now.getTime() > item.expiry) {
        // Hết hạn thì xóa luôn
        localStorage.removeItem(key);
        return null;
      }
      return item.value;
    } catch (error) {
      // Bắt lỗi JSON parse
      console.error("Lỗi parse cache", error);
      return null;
    }
  }
  /**
   * Xóa một key cụ thể
   */
  static remove(key) {
    localStorage.removeItem(key);
  }
  /**
   * Xóa toàn bộ cache
   */
  static clear() {
    localStorage.clear();
  }
}
export default CacheService;
