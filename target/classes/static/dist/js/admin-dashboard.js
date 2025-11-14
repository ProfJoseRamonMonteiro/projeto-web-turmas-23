document.addEventListener("DOMContentLoaded", async function () {
    const ctx = document.getElementById("graficoVendas");

    try {
        const resposta = await fetch("/admin/api/vendas-mensais");
        const dados = await resposta.json();

        const meses = Object.keys(dados);
        const totais = Object.values(dados);

        new Chart(ctx, {
            type: "bar",
            data: {
                labels: meses,
                datasets: [{
                    label: "Vendas (R$)",
                    data: totais,
                    backgroundColor: "rgba(54, 162, 235, 0.6)",
                    borderColor: "rgba(54, 162, 235, 1)",
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    title: { display: true, text: "Vendas por Mês" }
                },
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });

    } catch (error) {
        console.error("Erro ao carregar dados do gráfico:", error);
    }
});