import { reactive } from "vue";

export const modalState = reactive({
  isOpen: false,
  title: "",
  message: "",
  type: "confirm", // 'success' | 'error' | 'warning' | 'confirm'
  confirmButtonText: "Đồng ý",
  cancelButtonText: "Hủy",
  resolve: null,
});

export const toastState = reactive({
  isOpen: false,
  message: "",
  type: "success", // 'success' | 'error' | 'warning'
});

let toastTimeout = null;

export const modalStore = {
  confirm(message, title = "Xác nhận") {
    modalState.title = title;
    modalState.message = message;
    modalState.type = "confirm";
    modalState.confirmButtonText = "Đồng ý";
    modalState.cancelButtonText = "Hủy bỏ";
    modalState.isOpen = true;

    return new Promise((resolve) => {
      modalState.resolve = resolve;
    });
  },

  warning(message, title = "Cảnh báo") {
    modalState.title = title;
    modalState.message = message;
    modalState.type = "warning";
    modalState.confirmButtonText = "Đồng ý";
    modalState.cancelButtonText = "";
    modalState.isOpen = true;

    return new Promise((resolve) => {
      modalState.resolve = resolve;
    });
  },

  error(message) {
    if (toastTimeout) clearTimeout(toastTimeout);

    toastState.message = message;
    toastState.type = "error";
    toastState.isOpen = true;

    toastTimeout = setTimeout(() => {
      toastState.isOpen = false;
    }, 2000);
  },

  success(message) {
    if (toastTimeout) clearTimeout(toastTimeout);

    toastState.message = message;
    toastState.type = "success";
    toastState.isOpen = true;

    toastTimeout = setTimeout(() => {
      toastState.isOpen = false;
    }, 2000);
  },

  close(result) {
    modalState.isOpen = false;
    if (modalState.resolve) {
      modalState.resolve(result);
      modalState.resolve = null;
    }
  },
};
