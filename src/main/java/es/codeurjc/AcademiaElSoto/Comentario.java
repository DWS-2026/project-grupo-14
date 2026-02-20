package es.codeurjc.AcademiaElSoto;

public class Comentario {
    private String descripcion;
    private String usuario;

    public Comentario (String descripcion, String usuario){
        this.descripcion=descripcion;
        this.usuario=usuario;
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
