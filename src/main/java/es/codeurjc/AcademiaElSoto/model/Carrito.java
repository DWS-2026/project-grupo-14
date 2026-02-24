package es.codeurjc.AcademiaElSoto.model;

public class Carrito {

    private String producto;
    private int precio;

    public Carrito (String producto, int precio){
        this.precio=precio;
        this.producto=producto;
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
