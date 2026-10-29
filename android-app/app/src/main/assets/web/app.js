const loader = document.getElementById('pageLoader');
const toast = document.getElementById('toast');
let toastTimer;

window.addEventListener('load', () => {
  window.setTimeout(() => loader?.classList.add('is-hidden'), 520);
});

function showToast(message) {
  if (!toast) return;
  toast.textContent = message;
  toast.classList.add('show');
  window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => toast.classList.remove('show'), 3600);
}

function openChannel() {
  if (window.AhmedSport && typeof window.AhmedSport.openChannel === 'function') {
    window.AhmedSport.openChannel();
    return;
  }
  showToast('تعذر فتح المشغل حالياً. أعد تشغيل التطبيق وحاول مرة أخرى.');
}
