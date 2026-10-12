<script setup>
import { onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

onMounted(() => {
  const token = route.query.token;
  const name = route.query.name;

  if (token) {
    localStorage.setItem("token", token);
    localStorage.setItem("name", name);
    localStorage.setItem("loginTime", Date.now());
  }

  router.replace("/");
});
</script>

<template>
  <div>Đang đăng nhập...</div>
</template>
