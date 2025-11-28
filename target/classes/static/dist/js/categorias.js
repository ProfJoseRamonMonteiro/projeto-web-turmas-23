//categorias.js
document.addEventListener("DOMContentLoaded", () => {

    // Elementos comuns
    const formCadastro = document.getElementById("formCategoria");
    const toastContainer = document.getElementById("toastContainer");

    // FUNÇÃO: showToast
    function showToast(msg, type = "info", duration = 4000) {
        if (!toastContainer) return;
        const toast = document.createElement("div");
        toast.className = `toast ${type}`;
        toast.innerHTML = `<span>${msg}</span><button aria-label="Fechar">×</button>`;
        toastContainer.appendChild(toast);
        setTimeout(() => toast.classList.add("show"), 50);

        toast.querySelector("button").addEventListener("click", () => {
            toast.remove();
        });

        setTimeout(() => {
            toast.classList.remove("show");
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }

    function removerCategoria() {
    if (confirm("Deseja realmente excluir esta categoria?")) {
        document.getElementById("formRemoverCat").submit();
    }
}

    // SUBMIT VIA AJAX (FormData) — converte BRL => número com ponto
    if (formCadastro) {
        formCadastro.addEventListener("submit", async (e) => {
            e.preventDefault();

            const nome = formCadastro.querySelector("input[name='nome']").value.trim();

            if (!nome) {
                showToast("Informe o nome do categoria", "error");
                return;
            }

            const action = formCadastro.getAttribute("action") || window.location.href;

            try {
                const resp = await fetch(action, {
                    method: formCadastro.getAttribute("method") || "post",
                    body: formData
                });

                if (resp.ok) {
                    alert("Operação realizada com sucesso!");
                    setTimeout(() => window.location.href = "/admin/dashboard", 900);
                } else {
                    const text = await resp.text();
                    showToast("Erro: " + (text || resp.statusText), "error");
                }
            } catch (err) {
                console.error(err);
                showToast("Erro de rede ao enviar formulário", "error");
            }
        });
    }

});