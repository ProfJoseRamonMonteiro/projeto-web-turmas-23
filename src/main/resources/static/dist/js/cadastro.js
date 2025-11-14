// =========================== VALIDADOR DE SENHA ========================================= //
const inputSenha = document.querySelector('#senha')
const inputConfirmarSenha = document.querySelector('#confirmarSenha')
const botaoCadastrar = document.querySelector('#btn-cadastrar')

inputConfirmarSenha.addEventListener('keyup', () => {
    validarSenha()
})

function validarSenha() {
    let senha = inputSenha.value
    let confirmarSenha = inputConfirmarSenha.value
    if (senha !== confirmarSenha) {
        botaoCadastrar.disabled = true
    } else {
        botaoCadastrar.disabled = false
    }
}

botaoCadastrar.addEventListener('mouseenter', () => {
    validarSenha()
})