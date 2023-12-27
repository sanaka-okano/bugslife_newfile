package com.example.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        // セッションからアクセス履歴を削除
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("urlRecording");
        }

        // ログアウト成功後のリダイレクト先を指定
        response.sendRedirect("/auth/login?logout");
    }
}