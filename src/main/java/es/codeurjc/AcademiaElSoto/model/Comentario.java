package es.codeurjc.AcademiaElSoto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String descripcion;
    private String usuario;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;

    public Comentario() {
    }

    public Comentario(String descripcion, String usuario, Curso curso) {
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.curso = curso;
    }

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUsuario() {
        return usuario;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}