import nprogress from "nprogress";
import "nprogress/nprogress.css";
import { modalStore } from "@/utils/modalStore";

nprogress.configure({ showSpinner: false, trickleSpeed: 200 });

export const useUtils = () => {
  const notify = {
    success: (msg) => {
      modalStore.success(msg);
    },
    error: (msg) => {
      modalStore.error(msg);
    },
    warning: (msg) => {
      return modalStore.warning(msg);
    },
    confirm: (msg) => {
      return modalStore.confirm(msg);
    },
  };

  const formatCurrency = (value) => {
    if (value === null || value === undefined || value === "") return "0";
    return Number(value).toLocaleString("vi-VN");
  };

  const parseCurrency = (value) => {
    if (!value) return 0;
    return Number(value.toString().replace(/\D/g, ""));
  };

  return { notify, nprogress, formatCurrency, parseCurrency };
};
