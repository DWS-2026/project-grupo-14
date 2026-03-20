package es.codeurjc.AcademiaElSoto.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

import java.util.List;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Carrito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String producto; // Opcional: si quieres darle un nombre al carrito
    private int precio;      // Opcional: para guardar el precio total del carrito

    @OneToOne
    private Usuario usuario;

    // Usamos ManyToMany para que un mismo curso pueda estar en los carritos de varios alumnos
    @ManyToMany
    private List<Curso> cursos = new ArrayList<>();

    // 1. Constructor por defecto (Obligatorio para Spring)
    public Carrito() {
    }

    // Constructor con parámetros
    public Carrito(String producto, int precio){
        this.precio = precio;
        this.producto = producto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId (){
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    // Getters y Setters para la lista de cursos
    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    // Método de conveniencia (opcional pero muy útil) para añadir un curso a la lista
    public void addCurso(Curso curso) {
        this.cursos.add(curso);
        // Aquí podrías sumar automáticamente el precio del curso al total del carrito
        this.precio += curso.getPrecio(); 
    }
}