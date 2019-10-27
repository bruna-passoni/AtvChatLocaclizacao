package br.com.bossini.fatec_ipi_noite_pdm_firebase_auth_firestore;

import java.util.Date;

class Mensagem implements Comparable <Mensagem> {

    @Override
    public int compareTo(Mensagem mensagem) {
        return this.data.compareTo(mensagem.data);
    }

    private String texto;
    private Date data;
    private String email;
    private Boolean isLocation;
    private double f1;
    private double f2;

    public Mensagem() {
    }

    public Mensagem(String texto, Date data, String email) {
        this.texto = texto;
        this.data = data;
        this.email = email;
        this.isLocation = false;
    }

    public Mensagem(String texto, Date data, String email, Boolean isLocation) {
        this.texto = texto;
        this.data = data;
        this.email = email;
        this.isLocation = isLocation;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsLocation() {
        return isLocation;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public double getF1() {
        return f1;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public double getF2() {
        return f2;
    }

}
