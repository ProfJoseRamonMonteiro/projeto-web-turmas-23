document.addEventListener("DOMContentLoaded", function () {

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

    // =========================================================
    // CARREGAR JSON DO GRÁFICO (OK)
    // =========================================================

    const labelsScript = document.getElementById("labelsMeses");
    const dadosScript = document.getElementById("dadosTotais");

    let labels = [];
    let valores = [];

    try {
        if (!labelsScript || !dadosScript) return;

        labels = JSON.parse(labelsScript.textContent.trim());
        valores = JSON.parse(dadosScript.textContent.trim());

    } catch (e) {
        console.error("Erro gráfico:", e);
    }

    const ctx = document.getElementById("graficoMensal");

    if (ctx) {
        if (window.graficoMensalInstance) {
            window.graficoMensalInstance.destroy();
        }

        window.graficoMensalInstance = new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                    label: "Total vendido (R$)",
                    data: valores,
                    borderWidth: 0,
                    backgroundColor: "rgba(53, 124, 40, 0.65)",
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
        });
    }

    // =========================================================
    // FILTRO DO DASHBOARD
    // =========================================================
    const btnFiltro = document.getElementById("btnFiltrar");

    if (btnFiltro) {
        btnFiltro.addEventListener("click", function () {

            const mes = document.getElementById("filtroMes").value;
            const ano = document.getElementById("filtroAno").value;

            let url = `/admin/dashboard?`;

            if (ano) url += `ano=${ano}`;
            if (mes) url += `&mes=${mes}`;

            window.location.href = url;
        });
    }

    // =========================================================
    // SISTEMA DE TABS
    // =========================================================
    const tabButtons = document.querySelectorAll(".tab-btn");
    const tabPanels = document.querySelectorAll(".tab-panel");

    tabButtons.forEach(btn => {
        btn.addEventListener("click", () => {

            tabButtons.forEach(b => b.classList.remove("active"));
            tabPanels.forEach(panel => panel.classList.remove("active"));

            btn.classList.add("active");
            document.getElementById(btn.dataset.tab).classList.add("active");
        });
    });

    // == FUNÇÃO DETALHAR PEDIDO == 
    const tabela = document.getElementById("tabelaPedidos");
    let linhaAberta = null;

    // Função para buscar itens do pedido
    async function carregarItens(id) {
        const response = await fetch(`/admin/pedidos/${id}/itens`);
        if (!response.ok) throw new Error("Erro ao carregar itens");
        return await response.json();
    }

    tabela.addEventListener("click", async (event) => {

        if (!event.target.classList.contains("btn-toggle")) return;

        const btn = event.target;
        const idPedido = btn.getAttribute("data-id");
        const linhaPedido = document.getElementById("pedido-" + idPedido);

        // Se já existe uma linha aberta → fecha
        if (linhaAberta) {
            linhaAberta.remove();

            // Se clicou a mesma linha → só fecha
            if (linhaAberta.dataset.id == idPedido) {
                linhaAberta = null;

                btn.innerHTML = `<i class="fa fa-eye"></i> Detalhar`;
                btn.classList.add("btn-info");
                btn.classList.remove("btn-danger");

                return;
            }
        }

        // Clona o template
        const template = document.getElementById("template-detalhes");
        const novaLinha = template.cloneNode(true);
        novaLinha.style.display = "table-row";
        novaLinha.dataset.id = idPedido;

        // Ajusta o título do pedido
        novaLinha.querySelector(".titulo-pedido").textContent =
            `Itens do Pedido #${idPedido}`;

        // Busca itens
        const corpoItens = novaLinha.querySelector(".itens-container");
        corpoItens.innerHTML = `<tr><td colspan="4">Carregando...</td></tr>`;

        try {
            const itens = await carregarItens(idPedido);

            corpoItens.innerHTML = "";

            if (itens.length === 0) {
                corpoItens.innerHTML = "<tr><td colspan='4'>Nenhum item.</td></tr>";
            } else {
                itens.forEach(i => {
                    corpoItens.innerHTML += `
                    <tr>
                        <td>${i.nomeProduto}</td>
                        <td>${i.quantidade}</td>
                        <td>R$ ${parseFloat(i.preco).toFixed(2)}</td>
                        <td>R$ ${parseFloat(i.subtotal).toFixed(2)}</td>
                    </tr>
                `;
                });
            }
        } catch (e) {
            corpoItens.innerHTML = "<tr><td colspan='4'>Erro ao carregar.</td></tr>";
        }

        // Insere a linha abaixo da linha clicada
        linhaPedido.insertAdjacentElement("afterend", novaLinha);

        // Guarda referência da linha aberta
        linhaAberta = novaLinha;

        // Atualiza todos os botões para o estado "Detalhar"
        const todosBotoes = document.querySelectorAll(".btn-toggle");
        todosBotoes.forEach(b => {
            b.innerHTML = `<i class="fa fa-eye"></i> Detalhar`;
            b.classList.add("btn-info");
            b.classList.remove("btn-danger");
        });

        // Botão clicado vira "Recolher"
        btn.innerHTML = `<i class="fa fa-eye-slash"></i> Recolher`;
        btn.classList.remove("btn-info");
        btn.classList.add("btn-danger");
    });

    tabela.addEventListener("click", async (event) => {

        const btn = event.target.closest(".btn-enviar");
        if (!btn) return;

        const idPedido = btn.dataset.id;

        if (!confirm("Confirmar envio do pedido #" + idPedido + "?")) return;

        try {
            const response = await fetch(`/admin/pedidos/${idPedido}/enviar`, {
                method: "POST"
            });

            if (!response.ok) throw new Error("Erro ao enviar pedido.");

            // Atualiza status no HTML
            const linha = document.getElementById("pedido-" + idPedido);
            linha.querySelector(".status").textContent = "Enviado";

            // Desabilita o botão
            btn.disabled = true;
            btn.innerHTML = `<i class="fa fa-check"></i> Enviado`;
            btn.classList.remove("btn-success");
            btn.classList.add("btn-primary");

            showToast("Pedido enviado com sucesso!", "success");

        } catch (err) {
            showToast("Erro ao enviar pedido!", "error");
            console.error(err);
        }

    });

    const tabelaClientes = document.getElementById("tabela-clientes");

    tabelaClientes.addEventListener("click", async (event) => {

        const btn = event.target.closest(".btn-toggle-cliente");
        if (!btn) return;

        const idUsuario = btn.dataset.id;

        //if (!confirm("Deseja alterar o status do cliente?")) return;

        try {
            const response = await fetch(`/admin/cliente/toggle/${idUsuario}`, {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) throw new Error("Erro ao alterar status do cliente.");

            const data = await response.json();

            if (!data.success) {
                showToast("Erro: " + data.message, "error");
                return;
            }

            // Atualiza status na tabela
            const linha = document.getElementById("cliente-" + idUsuario);
            const statusCell = linha.querySelector(".habilitado");

            if (data.habilitado) {
                statusCell.textContent = "Habilitado";
                btn.innerHTML = `<i class="fa fa-toggle-off"></i> Desabilitar`;
                btn.classList.replace('btn-success', 'btn-danger');
                showToast("Cliente habilitado!", "success");
            } else {
                statusCell.textContent = "Desabilitado";
                btn.innerHTML = `<i class="fa fa-toggle-on"></i> Habilitar`;
                btn.classList.replace('btn-danger', 'btn-success');
                showToast("Cliente desabilitado!", "info");
            }

        } catch (err) {
            showToast("Erro ao alterar status do cliente!", "error");
            console.error(err);
        }

    });

}); // <-- FIM DO DOMContentLoaded
