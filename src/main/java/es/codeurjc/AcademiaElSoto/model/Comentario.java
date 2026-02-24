package es.codeurjc.AcademiaElSoto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String descripcion;
    private String usuario;

    public Comentario (String descripcion, String usuario){
        this.descripcion=descripcion;
        this.usuario=usuario;
    }

    public Long getId(){
        return id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUsuario() {
        return usuario;
    }
}
