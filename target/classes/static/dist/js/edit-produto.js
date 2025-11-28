// Exibir/esconder preço promocional
document.addEventListener("DOMContentLoaded", () => {

    const checkPromo = document.getElementById("emPromocao");
    const campoPromo = document.getElementById("grupo-promocao");

    checkPromo.addEventListener("change", () => {
        campoPromo.style.display = checkPromo.checked ? "block" : "none";
    });

    // Preview da nova imagem
    const inputImagem = document.getElementById("imagem");
    const preview = document.getElementById("previewNovaImagem");

    inputImagem.addEventListener("change", () => {
        preview.innerHTML = ""; // limpa

        const file = inputImagem.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = e => {
            const img = document.createElement("img");
            img.src = e.target.result;
            preview.appendChild(img);
        };

        reader.readAsDataURL(file);
    });
});