package es.codeurjc.AcademiaElSoto.model;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.persistence.*;

@Entity
@SessionScope
@Component
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String info;

    private String userName;
    private String email;
    private String password;
    private String apellidos;

    public Usuario() {
    }

    public Usuario(String userName, String apellidos, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.apellidos = apellidos;
    }

    public void setApellidos(String apellidos){
        this.apellidos = apellidos;
    }

    public String getApellidos(){
        return apellidos;
    }

    

    public Long getId() {
        return id;
    }

    public String getInfo(){
        return info;
    }

    public void setInfo(String info){
        this.info = info;
    }

    public String getNombre() {
        return userName;
    }

    public void setNombre(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}