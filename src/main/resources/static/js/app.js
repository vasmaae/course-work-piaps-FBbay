// auto-dismiss alerts after 4 seconds
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.alert-success, .alert-info').forEach(el => {
        setTimeout(() => el.classList.remove('show'), 4000);
    });
});
