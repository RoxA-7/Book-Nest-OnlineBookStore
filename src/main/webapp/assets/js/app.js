const topbar = document.querySelector("[data-glass]");

const savedTheme = window.localStorage.getItem("booknest-theme") || "light";
document.body.dataset.theme = savedTheme;

const themeNames = {
    light: "浅色",
    dark: "深色",
    ink: "漆黑",
    ochre: "暖黄",
    graphite: "石墨",
    olive: "大禹",
    rosewood: "紫檀"
};

document.querySelectorAll("[data-theme-menu]").forEach((menu) => {
    const trigger = menu.querySelector("[data-theme-trigger]");
    const label = menu.querySelector("[data-theme-label]");
    const options = menu.querySelector("[data-theme-options]");
    const syncLabel = () => {
        label.textContent = themeNames[document.body.dataset.theme] || themeNames.light;
        options.querySelectorAll("[data-theme-value]").forEach((button) => {
            button.classList.toggle("active", button.dataset.themeValue === document.body.dataset.theme);
        });
    };
    syncLabel();
    trigger.addEventListener("click", () => {
        const isOpen = menu.classList.toggle("open");
        trigger.setAttribute("aria-expanded", String(isOpen));
    });
    options.querySelectorAll("[data-theme-value]").forEach((button) => {
        button.addEventListener("click", () => {
            document.body.dataset.theme = button.dataset.themeValue;
            window.localStorage.setItem("booknest-theme", button.dataset.themeValue);
            menu.classList.remove("open");
            trigger.setAttribute("aria-expanded", "false");
            syncLabel();
        });
    });
    document.addEventListener("click", (event) => {
        if (!menu.contains(event.target)) {
            menu.classList.remove("open");
            trigger.setAttribute("aria-expanded", "false");
        }
    });
});

const syncTopbar = () => {
    if (!topbar) {
        return;
    }
    topbar.classList.toggle("is-scrolled", window.scrollY > 12);
};

syncTopbar();
window.addEventListener("scroll", syncTopbar, { passive: true });

const revealObserver = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
        if (entry.isIntersecting) {
            entry.target.classList.add("is-visible");
            revealObserver.unobserve(entry.target);
        }
    });
}, { threshold: 0.12 });

document.querySelectorAll(".reveal").forEach((element) => revealObserver.observe(element));

document.querySelectorAll("[data-toast]").forEach((toast) => {
    window.setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateY(-8px)";
    }, 2600);
    window.setTimeout(() => toast.remove(), 3100);
});

document.querySelectorAll("[data-modal]").forEach((modal) => {
    modal.querySelectorAll("[data-modal-close]").forEach((button) => {
        button.addEventListener("click", () => modal.remove());
    });
    modal.addEventListener("click", (event) => {
        if (event.target === modal) {
            modal.remove();
        }
    });
});

document.querySelectorAll(".book-card").forEach((card) => {
    card.addEventListener("pointermove", (event) => {
        const rect = card.getBoundingClientRect();
        const x = ((event.clientX - rect.left) / rect.width - 0.5) * 3;
        const y = ((event.clientY - rect.top) / rect.height - 0.5) * -3;
        card.style.transform = `translateY(-5px) rotateX(${y}deg) rotateY(${x}deg)`;
    });
    card.addEventListener("pointerleave", () => {
        card.style.transform = "";
    });
});

document.querySelectorAll("[data-confirm]").forEach((button) => {
    button.addEventListener("click", (event) => {
        const message = button.getAttribute("data-confirm");
        if (message && !window.confirm(message)) {
            event.preventDefault();
        }
    });
});
