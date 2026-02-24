package es.codeurjc.AcademiaElSoto.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Carrito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String producto;
    private int precio;

    public Carrito (String producto, int precio){
        this.precio=precio;
        this.producto=producto;
    }

    public Long getId (){
        return id;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public String getProducto() {
        return producto;
    }

    public int getPrecio() {
        return precio;
    }

    
}
