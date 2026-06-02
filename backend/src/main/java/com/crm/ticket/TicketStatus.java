package com.crm.ticket;

public enum TicketStatus {
    PENDING,   // na fila, aguardando atendente
    OPEN,      // em atendimento
    RESOLVED,  // resolvido (aguardando fechamento/CSAT)
    CLOSED     // encerrado
}
