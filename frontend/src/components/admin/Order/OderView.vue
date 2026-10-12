<script setup>
import { ref, onMounted } from "vue";
import { getAllSanPham } from "@/api/admin/sanpham/sanPhamApi";

const danhSachSanPham = ref([]);
const hoaDon = ref([]);

onMounted(() => {
  loadSanPham();
});

async function loadSanPham() {
  const res = await getAllSanPham();
  danhSachSanPham.value = res.data;
}

/* thêm món */
function themMon(sp) {
  const item = hoaDon.value.find(i => i.id === sp.id);

  if (item) {
    item.soLuong++;
  } else {
    hoaDon.value.push({
      id: sp.id,
      ten: sp.tenSanPham,
      gia: sp.gia,
      soLuong: 1
    });
  }
}

/* tổng tiền */
function tongTien() {
  return hoaDon.value.reduce(
    (t, i) => t + i.gia * i.soLuong,
    0
  );
}
</script>

<template>
<div class="order-page">

  <!-- DANH SÁCH MÓN -->
  <div class="menu">

    <h3>Menu</h3>

    <div
      v-for="sp in danhSachSanPham"
      :key="sp.id"
      class="mon"
      @click="themMon(sp)"
    >
      <div>{{ sp.tenSanPham }}</div>
      <b>{{ sp.gia }} đ</b>
    </div>

  </div>

  <!-- HÓA ĐƠN -->
  <div class="hoadon">

    <h3>Hóa đơn</h3>

    <div
      v-for="item in hoaDon"
      :key="item.id"
      class="item"
    >
      {{ item.ten }}
      x{{ item.soLuong }}
      - {{ item.gia * item.soLuong }} đ
    </div>

    <div class="tong">
      Tổng: {{ tongTien() }} đ
    </div>

  </div>

</div>
</template>

<style scoped>
.order-page{
display:grid;
grid-template-columns:2fr 1fr;
gap:20px;
padding:20px;
}

.menu{
background:white;
padding:16px;
border-radius:10px;
}

.mon{
border:1px solid #eee;
padding:10px;
margin-bottom:8px;
cursor:pointer;
display:flex;
justify-content:space-between;
}

.mon:hover{
background:#f5f5f5;
}

.hoadon{
background:white;
padding:16px;
border-radius:10px;
}

.item{
margin-bottom:6px;
}

.tong{
margin-top:10px;
font-weight:bold;
font-size:18px;
}
</style>