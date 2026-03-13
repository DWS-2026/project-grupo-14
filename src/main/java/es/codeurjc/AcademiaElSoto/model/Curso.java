package es.codeurjc.AcademiaElSoto.model;

import java.sql.Blob;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String profesor;
    private String nombreCurso;
    private int precio;
    private String descripcion;
    private int alumnos;

    @Lob
    private Blob imageFile;

    //Importante poner el constructor por defecto:

    public Curso(){

    }

    public Curso (String profesor, String nombreCurso, int precio, String descripcion, int alumnos){
        super();
        this.descripcion=descripcion;
        this.nombreCurso=nombreCurso;
        this.precio=precio;
        this.profesor=profesor;
        this.alumnos = alumnos;
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

    public int getAlumnos() {
        return alumnos;
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

    public void setAlumnos(int alumnos) {
        this.alumnos = alumnos;
    }
    
    public Blob getImageFile() {
		return imageFile;
	}

	public void setImageFile(Blob imageFile) {
		this.imageFile = imageFile;
	}

    public void setId(Long id){
        this.id = id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


}
