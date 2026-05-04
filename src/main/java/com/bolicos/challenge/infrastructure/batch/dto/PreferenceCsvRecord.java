package com.bolicos.challenge.infrastructure.batch.dto;

public class PreferenceCsvRecord {

    private String preferenciaCanalComunicacao;
    private String email;
    private String tipo;
    private Boolean verificado;

    public String getPreferenciaCanalComunicacao() {
        return preferenciaCanalComunicacao;
    }

    public void setPreferenciaCanalComunicacao(String preferenciaCanalComunicacao) {
        this.preferenciaCanalComunicacao = preferenciaCanalComunicacao;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getVerificado() {
        return verificado;
    }

    public void setVerificado(Boolean verificado) {
        this.verificado = verificado;
    }
}
