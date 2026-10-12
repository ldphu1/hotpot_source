export default {
  mounted(el, binding) {
    let tooltip = null;

    const createTooltip = () => {
      if (tooltip) return;

      tooltip = document.createElement("div");

      Object.assign(tooltip.style, {
        position: "fixed",
        background: "linear-gradient(135deg, #c56060, #c56060)",
        color: "#fff",
        padding: "8px 12px",
        borderRadius: "8px",
        fontSize: "13px",
        fontWeight: "500",
        pointerEvents: "none",
        whiteSpace: "nowrap",
        zIndex: "9999",
        opacity: "0",
        transform: "translateY(6px) scale(0.95)",
        transition: "opacity 0.15s ease, transform 0.15s ease",
        boxShadow: "0 10px 25px #c56060",
      });

      document.body.appendChild(tooltip);
    };

    const move = (e) => {
      if (!tooltip) return;
      tooltip.style.left = e.clientX + 12 + "px";
      tooltip.style.top = e.clientY + 12 + "px";
    };

    const show = (e) => {
      const text = el.getAttribute('data-tooltip-text');
      if (!text) return;

      createTooltip();
      if (tooltip) {
        tooltip.textContent = text;
        // Phải set vị trí ban đầu ngay lúc show, nếu không tooltip sẽ bay lên góc màn hình
        if (e) {
          tooltip.style.left = e.clientX + 12 + "px";
          tooltip.style.top = e.clientY + 12 + "px";
        }
        tooltip.style.opacity = "1";
        tooltip.style.transform = "translateY(0) scale(1)";
      }
    };

    const hide = () => {
      if (tooltip) {
        tooltip.style.opacity = "0";
        tooltip.style.transform = "translateY(6px) scale(0.95)";
      }
    };

    el.addEventListener("mouseenter", show);
    el.addEventListener("mousemove", move);
    el.addEventListener("mouseleave", hide);

    el._tooltipCleanup = () => {
      el.removeEventListener("mouseenter", show);
      el.removeEventListener("mousemove", move);
      el.removeEventListener("mouseleave", hide);

      if (tooltip && tooltip.parentNode) {
        tooltip.parentNode.removeChild(tooltip);
        tooltip = null;
      }
    };

    if (binding.value) {
      el.setAttribute('data-tooltip-text', binding.value);
    } else {
      el.removeAttribute('data-tooltip-text');
    }
  },

  updated(el, binding) {
    if (binding.value) {
      el.setAttribute('data-tooltip-text', binding.value);
    } else {
      el.removeAttribute('data-tooltip-text');
    }

    // Nếu tooltip đang hiện và giá trị thay đổi thành rỗng, ẩn nó đi
    if (!binding.value && el._tooltipCleanup) {
      // Gọi sự kiện leave để tự động ẩn
      const event = new Event('mouseleave');
      el.dispatchEvent(event);
    }
  },

  unmounted(el) {
    el._tooltipCleanup && el._tooltipCleanup();
  },
};
