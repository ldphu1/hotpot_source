<template>
  <Header />

  <div class="main-content">
    <section class="hero"></section>
    <div class="route-content">
      <router-view />
    </div>
  </div>
  <ChatBot></ChatBot>
  <Footer />
</template>

<script setup>
import Header from "@/components/customer/GioiThieu/Header.vue";
import Footer from "@/components/customer/GioiThieu/Footer.vue";
import ChatBot from "@/components/customer/ChatBot/ChatBot.vue";
</script>

<style scoped>
.main-content {
  margin-top: 80px;
}
</style>
