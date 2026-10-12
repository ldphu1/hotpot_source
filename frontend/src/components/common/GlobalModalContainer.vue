<template>
  <Teleport to="body">
    <!-- Global Modal Dialog Overlay -->
    <transition name="modal-fade">
      <div v-if="modalState.isOpen" class="custom-modal-overlay" @click.self="handleDismiss">
        <transition name="modal-scale" appear>
          <div class="custom-modal-box">
            <!-- Header: Icon + Title + Close Button -->
            <div class="custom-modal-header">
              <div class="custom-modal-title-wrap">
                <div class="custom-modal-icon" :class="modalState.type">
                  <!-- Info/Question circular badge -->
                  <svg v-if="modalState.type === 'confirm'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                    <line x1="12" y1="17" x2="12.01" y2="17"></line>
                  </svg>
                  <svg v-else-if="modalState.type === 'warning'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="12" y1="9" x2="12" y2="13"></line>
                    <line x1="12" y1="17" x2="12.01" y2="17"></line>
                  </svg>
                  <svg v-else-if="modalState.type === 'error'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </div>
                <h3 class="custom-modal-title">{{ modalState.title }}</h3>
              </div>
              <!-- Close (X) button top right -->
              <button type="button" class="custom-modal-close-btn" @click="handleCancel">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>

            <!-- Dialog body -->
            <div class="custom-modal-body">
              <p class="custom-modal-text">{{ modalState.message }}</p>
            </div>

            <!-- Action buttons -->
            <div class="custom-modal-actions">
              <button
                v-if="modalState.cancelButtonText"
                type="button"
                class="btn-custom-cancel"
                @click="handleCancel"
              >
                {{ modalState.cancelButtonText }}
              </button>
              <button
                type="button"
                class="btn-custom-confirm"
                :class="modalState.type"
                @click="handleConfirm"
              >
                <div class="btn-light-sweep"></div>
                {{ modalState.confirmButtonText }}
              </button>
            </div>
          </div>
        </transition>
      </div>
    </transition>

    <!-- Global Success Toast Notification (Top-right) -->
    <transition name="toast-slide-right">
      <div v-if="toastState.isOpen && toastState.type === 'success'" class="custom-toast-box-right">
        <div class="custom-toast-icon success">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </div>
        <span class="custom-toast-text">{{ toastState.message }}</span>
        <div class="custom-toast-progress success"></div>
      </div>
    </transition>

    <!-- Global Error Toast Notification (Centered top) -->
    <div class="custom-toast-wrapper-center">
      <transition name="toast-slide-center">
        <div v-if="toastState.isOpen && toastState.type === 'error'" class="custom-toast-box-center">
          <div class="custom-toast-icon error">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </div>
          <span class="custom-toast-text">{{ toastState.message }}</span>
        </div>
      </transition>
    </div>
  </Teleport>
</template>

<script setup>
import { modalState, toastState, modalStore } from "@/utils/modalStore";

const handleConfirm = () => {
  modalStore.close(true);
};

const handleCancel = () => {
  modalStore.close(false);
};

const handleDismiss = () => {
  if (modalState.type === "confirm") {
    modalStore.close(false);
  }
};
</script>

<style scoped>
/* Overlay backdrop with glassmorphism */
.custom-modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(15, 23, 42, 0.35); /* Slate backdrop */
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999999; /* Higher than regular teleported modals */
}

/* Modal box - matching target design */
.custom-modal-box {
  background: #ffffff;
  border-radius: 5px; /* Tối đa 5px */
  padding: 26px 30px;
  width: 100%;
  max-width: 500px; /* Thon gọn, sang trọng hơn */
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  font-family: "Inter", sans-serif;
  box-sizing: border-box;
}

/* Header style with title wrap and close button */
.custom-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-bottom: 14px;
}

.custom-modal-title-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
}

/* Icon Badge - soft background matching modern premium styles */
.custom-modal-icon {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid transparent;
}

.custom-modal-icon.confirm {
  background-color: #ffe4e6; /* Soft red background to match button */
  color: #d73737; /* Brand red text */
  border-color: #fecdd3; /* Light red border */
}

.custom-modal-icon.warning {
  background-color: #ffedd5; /* Soft orange background */
  color: #ea580c;
  border-color: #fed7aa; /* Light orange border */
}

.custom-modal-icon.error {
  background-color: #fee2e2; /* Soft red background */
  color: #dc2626;
  border-color: #fca5a5; /* Light red border */
}

.custom-modal-icon svg {
  width: 20px;
  height: 20px;
}

/* Title & Content */
.custom-modal-title {
  font-size: 19px;
  font-weight: 600;
  color: #111827; /* Slate-900 */
  margin: 0;
  padding: 0;
}

.custom-modal-close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: 4px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.custom-modal-close-btn:hover {
  color: #4b5563;
  background-color: #f3f4f6;
}

.custom-modal-close-btn svg {
  width: 18px;
  height: 18px;
}

/* Dialog body and indentation under the title text */
.custom-modal-body {
  width: 100%;
  padding-left: 48px; /* Aligns text perfectly with the title text: icon size 34px + gap 14px = 48px */
  box-sizing: border-box;
}

.custom-modal-text {
  font-size: 15px;
  color: #4b5563; /* Slate-600 */
  line-height: 1.6;
  margin: 0 0 24px 0;
  padding: 0;
}

/* Action buttons alignment */
.custom-modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

/* Buttons matching style of screenshot but brand color */
.btn-custom-confirm {
  position: relative;
  overflow: hidden;
  background-color: #d73737; /* Brand red */
  color: #ffffff;
  font-weight: 600; /* Match DanhMuc.vue font weight */
  font-size: 14px; /* Match DanhMuc.vue font size */
  padding: 10px 24px; /* Match DanhMuc.vue padding */
  border-radius: 4px; /* Tối đa 5px */
  border: none; /* Match DanhMuc.vue border styling */
  cursor: pointer;
  transition: all 0.2s; /* Match DanhMuc.vue transition duration */
  outline: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.btn-custom-confirm:hover {
  background-color: #b92b2b;
}

.btn-custom-confirm:active {
  transform: scale(0.98);
}

.btn-custom-confirm.error {
  background-color: #dc2626;
}

.btn-custom-confirm.error:hover {
  background-color: #b91c1c;
}

.btn-custom-confirm.warning {
  background-color: #ea580c;
}

.btn-custom-confirm.warning:hover {
  background-color: #c2410c;
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

.btn-custom-confirm:hover .btn-light-sweep {
  left: 150%;
  transition: 0.6s ease-in-out;
}

.btn-custom-cancel {
  background-color: #ffffff;
  color: #374151;
  font-weight: 500;
  font-size: 14.5px;
  padding: 9px 22px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  cursor: pointer;
  transition: all 0.15s ease;
  outline: none;
}

.btn-custom-cancel:hover {
  background-color: #f9fafb;
  border-color: #9ca3af;
  color: #111827;
}

.btn-custom-cancel:active {
  transform: scale(0.98);
}

/* --- TOAST STYLING --- */
/* --- TOAST STYLING --- */
/* SUCCESS TOAST: Top Right */
.custom-toast-box-right {
  position: fixed;
  top: 24px;
  right: 24px;
  background: #ffffff;
  border-radius: 6px;
  padding: 14px 22px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 99999999; /* Always on top of modals */
  max-width: 480px;
  overflow: hidden;
  font-family: "Inter", sans-serif;
  border: 1px solid #e5e7eb;
}

/* ERROR TOAST: Top Center Wrapper */
.custom-toast-wrapper-center {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 99999999; /* Always on top of modals */
  pointer-events: none;
  display: flex;
  justify-content: center;
  width: 100%;
}

.custom-toast-box-center {
  background: #ffffff;
  border-radius: 6px;
  padding: 14px 22px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
  pointer-events: auto;
  max-width: 480px;
  overflow: hidden;
  font-family: "Inter", sans-serif;
  border: 1px solid #e5e7eb;
  border-color: #fec2c2;
}

.custom-toast-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.custom-toast-icon.success {
  background: #dcfce7;
}

.custom-toast-icon.error {
  background: #dc2626;
  width: 28px;
  height: 28px;
}

.custom-toast-icon.error svg {
  width: 16px;
  height: 16px;
}

.custom-toast-icon.success svg {
  width: 16px;
  height: 16px;
}

.custom-toast-text {
  font-size: 15.5px;
  font-weight: 500;
  color: #1a1a1a;
}

/* Toast Progress timer bar */
.custom-toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  width: 100%;
  animation: progressTimer 2s linear forwards; /* 2s auto-dismiss */
}

.custom-toast-progress.success {
  background: #16a34a;
}

.custom-toast-progress.error {
  background: #dc2626;
}

@keyframes progressTimer {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

/* --- TRANSITIONS --- */
/* Overlay Fade */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* Modal Scale-up */
.modal-scale-enter-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.modal-scale-leave-active {
  transition: transform 0.15s ease, opacity 0.15s ease;
}

.modal-scale-enter-from {
  opacity: 0;
  transform: scale(0.96) translateY(5px);
}

.modal-scale-leave-to {
  opacity: 0;
  transform: scale(0.98);
}

/* Toast Slide in from top-right (Success) */
.toast-slide-right-enter-active {
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.25s ease;
}

.toast-slide-right-leave-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.toast-slide-right-enter-from {
  opacity: 0;
  transform: translateX(120%);
}

.toast-slide-right-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Toast Slide down/up from top-center (Error) */
.toast-slide-center-enter-active {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.3s ease;
}

.toast-slide-center-leave-active {
  transition: transform 0.3s cubic-bezier(0.25, 1, 0.5, 1), opacity 0.3s ease;
}

.toast-slide-center-enter-from {
  opacity: 0;
  transform: translateY(-40px);
}

.toast-slide-center-leave-to {
  opacity: 0;
  transform: translateY(-40px); /* trượt ngược từ dưới lên trên và biến mất */
}
</style>
