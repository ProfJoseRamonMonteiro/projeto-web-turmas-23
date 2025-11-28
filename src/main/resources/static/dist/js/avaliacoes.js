document.addEventListener("DOMContentLoaded", function () {

    function showToast(msg, type = "info", duration = 5000) {
        const container = document.getElementById("toastContainer") || (() => {
            const c = document.createElement('div'); c.id = 'toastContainer'; document.body.appendChild(c); return c;
        })();
        const toast = document.createElement("div");
        toast.className = `toast ${type}`;
        toast.innerHTML = `<span>${msg}</span><button onclick="this.parentElement.remove()"><i class="fa fa-times"></i></button>`;
        container.appendChild(toast);
        setTimeout(() => toast.classList.add("show"), 50);
        setTimeout(() => { toast.classList.remove("show"); setTimeout(() => toast.remove(), 300); }, duration);
    }

    const form = document.getElementById("formAvaliacao");

    if (!form) return;

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const comentario = document.getElementById("comentarioAvaliacao").value;
        const idProduto = document.getElementById("idProduto").value;
        const estrelas = document.querySelector("input[name='rating']:checked");

        if (!estrelas) {
            showToast("Selecione de 1 a 5 estrelas.", "warning");
            return;
        }

        let dados = new FormData();
        dados.append("idProduto", idProduto);
        dados.append("comentario", comentario);
        dados.append("estrelas", estrelas.value);

        fetch("/avaliacoes/salvar", {
            method: "POST",
            body: dados
        })
            .then(response => response.text())
            .then(msg => {
                form.reset();

                const formReload = document.createElement("form");
                formReload.method = "POST";
                formReload.action = "/produto/detalhado";

                const input = document.createElement("input");
                input.type = "hidden";
                input.name = "idProduto";
                input.value = idProduto;

                formReload.appendChild(input);
                document.body.appendChild(formReload);
                formReload.submit();
                showToast(msg, "success");

            })
            .catch(error => {
                showToast("Erro ao enviar avaliação: " + error.message, "error");
            });
    });

});