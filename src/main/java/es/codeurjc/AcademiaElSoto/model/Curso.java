package es.codeurjc.AcademiaElSoto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String profesor;
    private String nombreCurso;
    private int precio;
    private String descripcion;

    public Curso (String profesor, String nombreCurso, int precio, String descripcion){
        this.descripcion=descripcion;
        this.nombreCurso=nombreCurso;
        this.precio=precio;
        this.profesor=profesor;
    }

    public Long getId () {
        return id;
    }
    
    public String getProfesor() {
        return profesor;
    }

    public String getNombreCurso() {
        return nombreCurso;
    }

    public int getPrecio() {
        return precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public void setNombreCurso(String nombreCurso) {
        this.nombreCurso = nombreCurso;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


}
