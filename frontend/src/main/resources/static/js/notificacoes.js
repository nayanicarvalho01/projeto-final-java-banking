// Configuração
const contaId = document.getElementById('contaId').value;
const notificationsContainer = document.getElementById('notificationsContainer');
const statusDot = document.getElementById('statusDot');
const statusText = document.getElementById('statusText');

let eventSource = null;

// Atualizar relógio
function updateClock() {
    const now = new Date();

    const timeStr = now.toLocaleTimeString('pt-BR', {
        hour: '2-digit',
        minute: '2-digit'
    });

    const dateStr = now.toLocaleDateString('pt-BR', {
        weekday: 'long',
        day: 'numeric',
        month: 'long'
    });

    document.getElementById('currentTime').textContent = timeStr;
    document.getElementById('currentDate').textContent = dateStr;
}

// Calcular tempo relativo
function getRelativeTime(instant) {
    const now = Date.now();
    const eventTime = instant * 1000; // Converter para ms
    const diff = Math.floor((now - eventTime) / 1000); // segundos

    if (diff < 60) return 'agora';
    if (diff < 3600) return `há ${Math.floor(diff / 60)}m`;
    if (diff < 86400) return `há ${Math.floor(diff / 3600)}h`;
    return `há ${Math.floor(diff / 86400)}d`;
}

// Criar elemento de notificação
function createNotification(message) {
    const notification = document.createElement('div');
    notification.className = 'notification';

    // Parse da mensagem (vem como string do backend)
    const isApproved = message.includes('APROVADA');
    notification.classList.add(isApproved ? 'aprovada' : 'negada');

    notification.innerHTML = `
        <div class="notification-icon">🏦</div>
        <div class="notification-content">
            <div class="notification-header">
                <span class="notification-title">Agibank</span>
                <span class="notification-time">agora</span>
            </div>
            <div class="notification-message">${message}</div>
        </div>
    `;

    // Click para ir ao dashboard
    notification.addEventListener('click', () => {
        window.location.href = `/dashboard?contaId=${contaId}`;
    });

    return notification;
}

// Conectar SSE
function connectSSE() {
    const sseUrl = `http://localhost:8083/api/notificacoes/stream/${contaId}`;
    console.log('🔌 Conectando SSE:', sseUrl);

    eventSource = new EventSource(sseUrl);

    eventSource.onopen = () => {
        console.log('✅ SSE Conectado');
        statusDot.classList.add('connected');
        statusText.textContent = 'Conectado';
    };

    eventSource.addEventListener('notificacao', (event) => {
        console.log('📩 Notificação recebida:', event.data);

        const notification = createNotification(event.data);
        notificationsContainer.insertBefore(notification, notificationsContainer.firstChild);

        // Limitar a 10 notificações
        while (notificationsContainer.children.length > 10) {
            notificationsContainer.removeChild(notificationsContainer.lastChild);
        }
    });

    eventSource.onerror = (error) => {
        console.error('❌ Erro SSE:', error);
        statusDot.classList.remove('connected');
        statusText.textContent = 'Desconectado';

        // Tentar reconectar após 5 segundos
        setTimeout(() => {
            console.log('🔄 Tentando reconectar...');
            eventSource.close();
            connectSSE();
        }, 5000);
    };
}

// Inicializar
updateClock();
setInterval(updateClock, 1000);
connectSSE();

// Cleanup ao sair
window.addEventListener('beforeunload', () => {
    if (eventSource) {
        eventSource.close();
    }
});