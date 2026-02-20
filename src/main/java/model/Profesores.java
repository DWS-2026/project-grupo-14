package model;

@Entity
public class Profesores {

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;
    
    private String nombre;
    private String titulacion;

    public Profesores (String titulacion, String nombre){
        this.nombre=nombre;
        this.titulacion=titulacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTitulacion() {
        return titulacion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTitulacion(String titulacion) {
        this.titulacion = titulacion;
    }

    
}
