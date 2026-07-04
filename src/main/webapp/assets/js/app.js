const topbar = document.querySelector("[data-glass]");

const savedTheme = window.localStorage.getItem("booknest-theme") || "light";
document.body.dataset.theme = savedTheme;

document.querySelectorAll("[data-theme-select]").forEach((select) => {
    select.value = savedTheme;
    if (select.value !== savedTheme) {
        select.value = "light";
        document.body.dataset.theme = "light";
    }
    select.addEventListener("change", () => {
        document.body.dataset.theme = select.value;
        window.localStorage.setItem("booknest-theme", select.value);
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
