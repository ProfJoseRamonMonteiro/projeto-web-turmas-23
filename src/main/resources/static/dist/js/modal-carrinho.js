// =====================================================================
// modal-carrinho.js — Versão robusta com diagnóstico
// Substitua todo o arquivo por este para testes.
// =====================================================================

console.log("JS EXECUTANDO...");

const SESSION_STORAGE_KEY = 'carrinhoSimulacao';

// elementos (alguns podem ser null até o DOM estar pronto)
let modal = null;
let btnFecharModal = null;
let btnConfirmarAdd = null;
let btnDropdown = null;
let carrinhoConteudo = null;
let listaItensCarrinho = null;
let btnFinalizarPedido = null;
let contadorItensCarrinho = null;
let carrinhoVazioMsg = null;
let valorTotalCompra = null;
let btnRemoverItem = null;

let produtoEmModal = {};

function logDebug(...args) { console.debug('[CARRINHO]', ...args); }
function logInfo(...args) { console.info('[CARRINHO]', ...args); }
function logError(...args) { console.error('[CARRINHO]', ...args); }

function showToast(msg, type = "info", duration = 5000) {
    const container = document.getElementById("toastContainer") || (() => {
        const c = document.createElement('div'); c.id = 'toastContainer'; document.body.appendChild(c); return c;
    })();
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${msg}</span><button onclick="this.parentElement.remove()">×</button>`;
    container.appendChild(toast);
    setTimeout(() => toast.classList.add("show"), 50);
    setTimeout(() => { toast.classList.remove("show"); setTimeout(() => toast.remove(), 300); }, duration);
}

// ---------- helpers ----------
function formatarMoeda(valor) {
    if (typeof valor === 'string') valor = parseFloat(valor);
    if (isNaN(valor)) return 'R$ 0,00';
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

function criarItemCarrinhoHTML(item) {
    const precoTotal = item.quantidade * item.preco;
    return `
        <div class="item-carrinho" data-id="${item.id}" data-preco-unitario="${item.preco}" data-estoque="${item.estoque}">
            <img src="${item.endpointImagem}" alt="Miniatura" class="mini-imagem">
            <span style="flex-grow: 1;">${item.nome}</span>
            <input type="number" value="${item.quantidade}" min="1" max="${item.estoque}" data-id="${item.id}" class="input-quantidade-carrinho">
            <span class="preco-total-item">${formatarMoeda(precoTotal)}</span>
            <button class="btn-remover" data-id="${item.id}">Remover</button>
        </div>
    `;
}

function salvarCarrinhoNaSessao() {
    if (!listaItensCarrinho) return;
    const itensCarrinho = [];
    listaItensCarrinho.querySelectorAll('.item-carrinho').forEach(itemElement => {
        if (itemElement.classList.contains('removendo')) return;
        itensCarrinho.push({
            id: itemElement.dataset.id,
            nome: itemElement.querySelector('span:not(.preco-total-item)')?.textContent || '',
            quantidade: parseInt(itemElement.querySelector('.input-quantidade-carrinho')?.value || '1'),
            preco: parseFloat(itemElement.dataset.precoUnitario || '0'),
            estoque: parseInt(itemElement.dataset.estoque || '0'),
            endpointImagem: itemElement.querySelector('.mini-imagem')?.src || ''
        });
    });
    localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(itensCarrinho));
    logDebug('salvarCarrinhoNaSessao ->', itensCarrinho);
}

function carregarCarrinhoDaSessao() {
    if (!listaItensCarrinho) return;
    const carrinhoJSON = localStorage.getItem(SESSION_STORAGE_KEY);
    if (!carrinhoJSON) { logDebug('Sem carrinho em sessão'); return; }
    try {
        const items = JSON.parse(carrinhoJSON);
        items.forEach(item => listaItensCarrinho.insertAdjacentHTML('beforeend', criarItemCarrinhoHTML(item)));
        logDebug('carregarCarrinhoDaSessao ->', items);
    } catch (err) {
        logError('Erro parse carrinho JSON:', err);
    }
}

function calcularTotalCompra() {
    if (!listaItensCarrinho || !valorTotalCompra) return 0;
    let total = 0;
    listaItensCarrinho.querySelectorAll('.item-carrinho:not(.removendo)').forEach(itemElement => {
        const quantidade = parseInt(itemElement.querySelector('.input-quantidade-carrinho')?.value || '1');
        const precoUnitario = parseFloat(itemElement.dataset.precoUnitario || '0');
        total += quantidade * precoUnitario;
    });
    valorTotalCompra.textContent = formatarMoeda(total);
    return total;
}

function atualizarContadorECarrinhoVazio() {
    if (!listaItensCarrinho || !contadorItensCarrinho || !carrinhoVazioMsg) return;
    const totalItens = listaItensCarrinho.querySelectorAll('.item-carrinho:not(.removendo)').length;
    contadorItensCarrinho.textContent = totalItens;
    carrinhoVazioMsg.style.display = totalItens > 0 ? 'none' : 'block';
    calcularTotalCompra();
}

function fecharCarrinho() {
    if (!carrinhoConteudo) return;
    carrinhoConteudo.style.display = 'none';
}

function esvaziarCarrinho() {
    if (!listaItensCarrinho) return;
    listaItensCarrinho.innerHTML = '';
    localStorage.removeItem(SESSION_STORAGE_KEY);
    atualizarContadorECarrinhoVazio();
    fecharCarrinho();
}

// ---------- core (adicionar/remover/atualizar) ----------
function adicionarProdutoAoCarrinho(item) {
    if (!listaItensCarrinho) { logError('listaItensCarrinho indefinido'); return; }

    const itemExistente = listaItensCarrinho.querySelector(`.item-carrinho[data-id="${item.id}"]`);
    if (itemExistente) {
        const inputQuant = itemExistente.querySelector('.input-quantidade-carrinho');
        const spanPrecoTotal = itemExistente.querySelector('.preco-total-item');
        const estoqueMax = parseInt(itemExistente.dataset.estoque || '0');
        let novaQuantidade = parseInt(inputQuant.value || '0') + (item.quantidade || 1);
        if (novaQuantidade > estoqueMax) {
            showToast(`Estoque máximo atingido. Disponível: ${estoqueMax}`, 'warning');
            novaQuantidade = estoqueMax;
        }
        inputQuant.value = novaQuantidade;
        spanPrecoTotal.textContent = formatarMoeda(novaQuantidade * parseFloat(itemExistente.dataset.precoUnitario || '0'));
    } else {
        listaItensCarrinho.insertAdjacentHTML('beforeend', criarItemCarrinhoHTML(item));
    }
    salvarCarrinhoNaSessao();
    atualizarContadorECarrinhoVazio();
    if (modal) modal.style.display = 'none';
    if (carrinhoConteudo) carrinhoConteudo.style.display = 'block';
}

function removerProdutoDoCarrinho(idProduto) {
    if (!listaItensCarrinho) return;
    const itemElement = listaItensCarrinho.querySelector(`.item-carrinho[data-id="${idProduto}"]`);
    if (!itemElement) { logDebug('item para remover não encontrado', idProduto); return; }
    itemElement.classList.add('removendo');
    setTimeout(() => {
        itemElement.remove();
        salvarCarrinhoNaSessao();
        atualizarContadorECarrinhoVazio();
    }, 500);
}

function atualizarQuantidade(idProduto, novaQuantidade) {
    if (!listaItensCarrinho) return;
    const itemElement = listaItensCarrinho.querySelector(`.item-carrinho[data-id="${idProduto}"]`);
    if (!itemElement) return;
    const estoqueMax = parseInt(itemElement.dataset.estoque || '0');
    if (novaQuantidade > estoqueMax) { showToast(`Estoque insuficiente. Disponível: ${estoqueMax}`, 'warning'); novaQuantidade = estoqueMax; }
    if (novaQuantidade < 1) novaQuantidade = 1;
    const precoUnitario = parseFloat(itemElement.dataset.precoUnitario || '0');
    itemElement.querySelector('.input-quantidade-carrinho').value = novaQuantidade;
    itemElement.querySelector('.preco-total-item').textContent = formatarMoeda(novaQuantidade * precoUnitario);
    salvarCarrinhoNaSessao();
    calcularTotalCompra();
}

// ---------- diagnóstico de sobreposição e visibilidade ----------
function elementoVisivel(el) {
    if (!el) return false;
    const rect = el.getBoundingClientRect();
    if (rect.width === 0 && rect.height === 0) return false;
    const style = window.getComputedStyle(el);
    if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') return false;
    return true;
}

function detectarOverlaySobreElemento(el) {
    const rect = el.getBoundingClientRect();
    const cx = rect.left + rect.width / 2;
    const cy = rect.top + rect.height / 2;
    const topEl = document.elementFromPoint(cx, cy);
    if (!topEl) return null;
    return topEl === el ? null : topEl;
}

// ---------- função de tratamento do envio do pedido com logs ----------
async function enviarPedido() {
    try {
        const carrinhoJSON = localStorage.getItem(SESSION_STORAGE_KEY);
        const itens = carrinhoJSON ? JSON.parse(carrinhoJSON) : [];
        if (itens.length === 0) { showToast('Seu carrinho está vazio!', 'warning'); logInfo('EnviarPedido abortado: carrinho vazio'); return; }

        for (const item of itens) {
            if (item.quantidade > item.estoque) { showToast(`"${item.nome}" sem estoque`, 'warning'); logInfo('Estoque insuficiente', item); return; }
        }

        const itensAjustados = itens.map(i => ({ idProduto: i.id, quantidade: i.quantidade }));
        logDebug('Enviando pedido ->', itensAjustados);

        const response = await fetch('/pedido/salvar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(itensAjustados)
        });

        logDebug('Resposta fetch:', response.status, response.statusText);
        if (response.ok) {
            showToast('Pedido realizado com sucesso!', 'success');
            esvaziarCarrinho();
            setTimeout(() => window.location.href = '/', 700);
        } else {
            const text = await response.text().catch(()=>null);
            showToast('Erro ao finalizar pedido: ' + (text || response.statusText), 'error', 7000);
            logError('Erro fetch:', response.status, text || response.statusText);
        }
    } catch (err) {
        showToast('Erro no envio do pedido: ' + (err.message || err), 'error', 7000);
        logError('Exception enviarPedido:', err);
    }
}

// ---------- bindings seguros (delegation) ----------
function inicializarBindings() {
    // recuperar elementos (agora DOM deve estar pronto)
    modal = document.getElementById('modalAddCarrinho');
    btnFecharModal = document.querySelector('.fechar-modal');
    btnConfirmarAdd = document.getElementById('btnConfirmarAdd');
    btnDropdown = document.getElementById('btnDropdown');
    carrinhoConteudo = document.getElementById('carrinhoConteudo');
    listaItensCarrinho = document.getElementById('listaItensCarrinho');
    btnFinalizarPedido = document.getElementById('btnFinalizarPedido');
    contadorItensCarrinho = document.getElementById('contadorItensCarrinho');
    carrinhoVazioMsg = document.getElementById('carrinhoVazioMsg');
    valorTotalCompra = document.getElementById('valorTotalCompra');

    logInfo('Bindings inicializados. btnFinalizarPedido ->', btnFinalizarPedido);

    // Delegation para cliques em .btn-add-carrinho (pode haver muitos)
    document.addEventListener('click', (e) => {
        const target = e.target.closest && e.target.closest('.btn-add-carrinho');
        if (target) {
            // abrir modal para este produto
            const btn = target;
            produtoEmModal = {
                id: btn.dataset.idproduto,
                nome: btn.dataset.nome,
                descricao: btn.dataset.descricao,
                preco: parseFloat(btn.dataset.preco || '0'),
                estoque: parseInt(btn.dataset.estoque || '0'),
                endpointImagem: btn.dataset.endpointimagem || btn.dataset.endpointImagem || ''
            };
            logDebug('btn-add-carrinho clicado', produtoEmModal);
            // preencher modal
            const imgEl = document.getElementById('modalImagem');
            const nomeEl = document.getElementById('modalNome');
            const descEl = document.getElementById('modalDescricao');
            const precoEl = document.getElementById('modalPreco');
            const estoqueEl = document.getElementById('modalEstoque');
            const inputQuant = document.getElementById('inputQuantidade');

            if (imgEl) imgEl.src = produtoEmModal.endpointImagem;
            if (nomeEl) nomeEl.textContent = produtoEmModal.nome;
            if (descEl) descEl.textContent = produtoEmModal.descricao;
            if (precoEl) precoEl.textContent = formatarMoeda(produtoEmModal.preco);
            if (estoqueEl) estoqueEl.textContent = `Em estoque: ${produtoEmModal.estoque}`;
            if (inputQuant) { inputQuant.value = 1; inputQuant.min = 1; inputQuant.max = produtoEmModal.estoque; }
            const erroEl = document.querySelector('.erro-quantidade'); if (erroEl) erroEl.textContent = '';
            if (modal) modal.style.display = 'block';
        }
    });

    // Confirmar adição (se existir)
    if (btnConfirmarAdd) {
        btnConfirmarAdd.addEventListener('click', (e) => {
            e.preventDefault();
            const quantidade = parseInt(document.getElementById('inputQuantidade')?.value || '1');
            const erroMsg = document.querySelector('.erro-quantidade');
            if (quantidade < 1 || quantidade > (produtoEmModal.estoque || 0)) {
                if (erroMsg) erroMsg.textContent = `Quantidade inválida. Mínimo 1, Máximo ${produtoEmModal.estoque}`;
                return;
            }
            adicionarProdutoAoCarrinho({ ...produtoEmModal, quantidade });
        });
    } else {
        logDebug('btnConfirmarAdd não encontrado no DOM');
    }

    // Fechar modal (delegation)
    document.addEventListener('click', (e) => {
        if (e.target === btnFecharModal) {
            if (modal) modal.style.display = 'none';
        }
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });

    // Delegation para remover item e alterar quantidade
    document.addEventListener('click', (e) => {
        const rem = e.target.closest && e.target.closest('.btn-remover');
        if (rem && listaItensCarrinho) {
            removerProdutoDoCarrinho(rem.dataset.id);
        }
    });

    document.addEventListener('change', (e) => {
        const input = e.target.closest && e.target.closest('.input-quantidade-carrinho');
        if (input) {
            let novaQuantidade = parseInt(input.value || '1');
            const max = parseInt(input.max || '1');
            if (novaQuantidade < 1) novaQuantidade = 1;
            if (novaQuantidade > max) { showToast(`Estoque máximo disponível: ${max}`, 'info'); novaQuantidade = max; }
            input.value = novaQuantidade;
            atualizarQuantidade(input.dataset.id, novaQuantidade);
        }
    });

    // Proteção: impedir fechamento do dropdown quando clicar no botão finalizar
    document.addEventListener('click', (e) => {
        const btn = e.target.closest && e.target.closest('#btnFinalizarPedido');
        if (btn) {
            e.stopPropagation();
            e.preventDefault && e.preventDefault(); // impedir comportamento default se for form
            // diagnóstico: checar visibilidade e overlay
            if (!elementoVisivel(btn)) {
                showToast('Botão Finalizar não visível (visibilidade=false). Veja console.', 'warning');
                logDebug('Botão invisível:', btn, 'visível?', elementoVisivel(btn));
            }
            const over = detectarOverlaySobreElemento(btn);
            if (over) {
                showToast('Elemento interceptando o clique: ' + (over.className || over.id || over.tagName), 'warning');
                logDebug('Elemento sobreposto:', over);
            }
            // aciona envio de pedido com debounce curto
            setTimeout(() => enviarPedido(), 10);
        }
    }, true); // capture true para pegar antes de outros handlers

    // Alternar dropdown (btnDropdown) — se existir
    if (btnDropdown && carrinhoConteudo) {
        btnDropdown.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            carrinhoConteudo.style.display = carrinhoConteudo.style.display === 'block' ? 'none' : 'block';
        });
        // impedir fechamento quando clicar dentro do conteúdo do carrinho
        carrinhoConteudo.addEventListener('click', (e) => e.stopPropagation());
    } else {
        logDebug('btnDropdown ou carrinhoConteudo não definidos', btnDropdown, carrinhoConteudo);
    }

    // Fechar dropdown ao clicar fora (exceto botão finalizar)
    window.addEventListener('click', (e) => {
        try {
            if (!carrinhoConteudo) return;
            if (!carrinhoConteudo.contains(e.target) && !(btnDropdown && btnDropdown.contains(e.target))) {
                // se target for btnFinalizarPedido, não fechar
                if (e.target && e.target.id === 'btnFinalizarPedido') return;
                fecharCarrinho();
            }
        } catch (err) { logError('Erro ao processar fechamento dropdown:', err); }
    });

    // carregar dados do carrinho
    carregarCarrinhoDaSessao();
    atualizarContadorECarrinhoVazio();
    fecharCarrinho();
}

// inicializa quando DOM pronto
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', inicializarBindings);
} else {
    inicializarBindings();
}

// Para diagnóstico rápido (abra console e rode): 
// document.querySelectorAll('#btnFinalizarPedido, .btn-finalizar, .btn-finalizar *')
// e ver se há elementos e listeners.
// Também verifique a aba Network ao clicar para checar se /pedido/salvar foi chamado.