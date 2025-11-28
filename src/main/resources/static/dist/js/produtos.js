//produtos.js
document.addEventListener("DOMContentLoaded", () => {

    // Elementos comuns
    const inputImagem = document.getElementById("imagem");
    const previewBox = document.getElementById("imagePreviewBox");
    const previewImg = document.getElementById("imagePreviewImg");
    const checkPromocao = document.getElementById("promocaoCheckbox");
    const campoPromocao = document.getElementById("campoPromocao");
    const formCadastro = document.getElementById("formProduto");
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

    // PREVIEW DE IMAGEM
    if (inputImagem && previewImg && previewBox) {
        inputImagem.addEventListener("change", () => {
            const file = inputImagem.files && inputImagem.files[0];
            if (!file) {
                previewImg.src = "";
                previewBox.style.display = "none";
                return;
            }
            const reader = new FileReader();
            reader.onload = (e) => {
                previewImg.src = e.target.result;
                previewBox.style.display = "flex";
            };
            reader.readAsDataURL(file);
        });
    }

    // TOGGLE PROMOÇÃO
    if (checkPromocao && campoPromocao) {
        checkPromocao.addEventListener("change", () => {
            campoPromocao.style.display = checkPromocao.checked ? "block" : "none";
        });
    }

    // ============================
    //  MÁSCARA BRL – SOLUÇÃO 2
    //  1 -> R$ 1,00  (NÃO divide por 100)
    // ============================
    function formatBRL(value) {
        if (!value) return "";

        // remove "R$", espaços e caracteres inválidos
        let v = value.replace(/\s/g, "")
                     .replace("R$", "")
                     .replace(/[^0-9,]/g, "");

        // evita múltiplas vírgulas
        const parts = v.split(",");
        if (parts.length > 2) {
            v = parts[0] + "," + parts.slice(1).join("");
        }

        let [int, dec] = v.split(",");

        // remove zeros à esquerda
        int = int ? int.replace(/^0+(?!$)/, "") : "";

        // adiciona separador de milhar
        int = int.replace(/\B(?=(\d{3})+(?!\d))/g, ".");

        // limita decimais a 2 dígitos
        if (dec) dec = dec.substring(0, 2);

        return dec !== undefined ? `R$ ${int},${dec}` : `R$ ${int}`;
    }

    function applyMaskToInput(input) {
        input.addEventListener("input", () => {
            const pos = input.selectionStart;
            const oldLength = input.value.length;

            input.value = formatBRL(input.value);

            // tentar manter o cursor estável
            const newLength = input.value.length;
            const diff = newLength - oldLength;
            input.selectionEnd = pos + diff;
        });

        // inicializa caso tenha valor
        if (input.value) input.value = formatBRL(input.value);
    }

    const inputPreco = document.getElementById("preco");
    const inputPrecoPromo = document.getElementById("precoPromocional");

    if (inputPreco) applyMaskToInput(inputPreco);
    if (inputPrecoPromo) applyMaskToInput(inputPrecoPromo);

    // SUBMIT VIA AJAX (FormData) — converte BRL => número com ponto
    if (formCadastro) {
        formCadastro.addEventListener("submit", async (e) => {
            e.preventDefault();

            const nome = formCadastro.querySelector("input[name='nome']").value.trim();
            const precoRaw = inputPreco ? inputPreco.value.trim() : "";

            if (!nome) {
                showToast("Informe o nome do produto", "error");
                return;
            }
            if (!precoRaw) {
                showToast("Informe o preço do produto", "error");
                return;
            }

            // converte "R$ 1.234,56" -> "1234.56"
            function brlToNumber(str) {
                if (!str) return "";
                let s = str.replace(/\s/g, "")
                           .replace("R$", "")
                           .replace(/\./g, "")
                           .replace(",", ".");
                return s;
            }

            const formData = new FormData(formCadastro);

            if (inputPreco) formData.set("preco", brlToNumber(inputPreco.value));
            if (inputPrecoPromo) formData.set("precoPromocional", brlToNumber(inputPrecoPromo.value));

            const action = formCadastro.getAttribute("action") || window.location.href;

            try {
                const resp = await fetch(action, {
                    method: formCadastro.getAttribute("method") || "post",
                    body: formData
                });

                if (resp.ok) {
                    showToast("Operação realizada com sucesso!", "success");
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

    // Preview simples de imagem
    const imagemFile = document.getElementById("imagemFile");
    if (imagemFile) {
        imagemFile.addEventListener("change", function () {
            const file = this.files[0];
            const preview = document.getElementById("previewImage");

            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    preview.style.display = "block";
                    preview.src = e.target.result;
                };
                reader.readAsDataURL(file);
            } else {
                preview.style.display = "none";
                preview.src = "";
            }
        });
    }

});