const contaId = document.getElementById('contaId').value;

function downloadExtrato() {
    const mes = new Date().toISOString().slice(0, 7); // YYYY-MM
    const url = `/dashboard/extrato/download?contaId=${contaId}&mes=${mes}`;

    console.log('📄 Baixando extrato...');
    window.location.href = url;
}

function downloadFatura() {
    const mes = new Date().toISOString().slice(0, 7); // YYYY-MM
    const url = `/dashboard/fatura/download?contaId=${contaId}&mes=${mes}`;

    console.log('📋 Baixando fatura...');
    window.location.href = url;
}

// Atualizar página a cada 30 segundos
setInterval(() => {
    console.log('🔄 Atualizando dashboard...');
    location.reload();
}, 30000);