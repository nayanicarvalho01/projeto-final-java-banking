package com.banking.frontend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class NotificacaoViewController {

    @GetMapping("/")
    public String index() {
        return "redirect:/notificacoes?contaId=123";
    }

    @GetMapping("/notificacoes")
    public String notificacoes(@RequestParam String contaId, Model model) {
        log.info("Acessando tela de notificações - Conta: {}", contaId);
        model.addAttribute("contaId", contaId);
        return "notificacoes";
    }
}