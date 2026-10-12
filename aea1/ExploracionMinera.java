import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// ===================== ENUMERACIONES =====================
enum EstadoExploracion { EN_CURSO, VIABLE, NO_VIABLE }

enum TipoPermiso { EXPLORACION, AGUAS, EXPLOSIVOS }

enum TipoZona { LIBRE, CERCADA, ARQUEOLOGICA, URBANA, RESERVA_NACIONAL }

// ===================== CLASE CRITICA: main business logic =====================
class ExploracionMinera {

    private static final double LEY_MINIMA_VIABLE = 0.50;

    private String idExploracion;
    private Terreno terreno;
    private ConcesionExploracion concesion;
    private List<PlataformaExploracion> plataformas = new ArrayList<>();
    private EstadoExploracion estado;

    public ExploracionMinera(String idExploracion, Terreno terreno,
                             ConcesionExploracion concesion, PermisoService permisoService) {
        // Regla 1: se requiere concesion vigente otorgada por el Estado
        if (!concesion.estaVigente()) {
            throw new IllegalStateException("La concesion de exploracion no esta vigente.");
        }
        // Regla 2: no se puede intervenir una zona restringida sin autorizacion
        if (!terreno.puedeIntervenirse()) {
            throw new IllegalStateException("El terreno es zona restringida y no cuenta con autorizacion.");
        }
        // Regla 3: se requiere permiso de exploracion vigente sobre el terreno
        if (!permisoService.tienePermisoVigente(terreno, TipoPermiso.EXPLORACION)) {
            throw new IllegalArgumentException("El terreno no cuenta con permiso vigente.");
        }
        this.idExploracion = idExploracion;
        this.terreno = terreno;
        this.concesion = concesion;
        this.estado = EstadoExploracion.EN_CURSO;
    }

    public void agregarPlataforma(PlataformaExploracion plataforma) {
        plataformas.add(plataforma);
    }

    // Regla 4: la ley es el promedio de las muestras de todas las plataformas
    public double calcularLeyPromedio() {
        double suma = 0;
        int total = 0;
        for (PlataformaExploracion p : plataformas) {
            for (MuestraRoca m : p.getMuestras()) {
                suma += m.getLey();
                total++;
            }
        }
        return total == 0 ? 0.0 : suma / total;
    }

    public double calcularCantidadMineral() {
        double total = 0;
        for (PlataformaExploracion p : plataformas) {
            for (MuestraRoca m : p.getMuestras()) {
                total += m.getCantidadMineral();
            }
        }
        return total;
    }

    // Regla 5: la exploracion es viable si la ley promedio alcanza el minimo
    public boolean esViable() {
        return calcularLeyPromedio() >= LEY_MINIMA_VIABLE;
    }

    // Regla 6: no se puede cerrar una exploracion sin muestras analizadas
    public void finalizarExploracion() {
        if (calcularLeyPromedio() == 0.0) {
            throw new IllegalStateException("No se puede finalizar una exploracion sin muestras.");
        }
        this.estado = esViable() ? EstadoExploracion.VIABLE : EstadoExploracion.NO_VIABLE;
    }

    public EstadoExploracion getEstado() { return estado; }
    public String getIdExploracion() { return idExploracion; }
    public Terreno getTerreno() { return terreno; }
    public ConcesionExploracion getConcesion() { return concesion; }
    public List<PlataformaExploracion> getPlataformas() { return plataformas; }
}

// ===================== CLASES DE APOYO =====================
class PlataformaExploracion {
    private String codigo;
    private List<MuestraRoca> muestras = new ArrayList<>();

    public PlataformaExploracion(String codigo) { this.codigo = codigo; }

    public void agregarMuestra(MuestraRoca muestra) { muestras.add(muestra); }

    public List<MuestraRoca> getMuestras() { return muestras; }
    public String getCodigo() { return codigo; }
}

class MuestraRoca {
    private int profundidad;
    private double cantidadMineral;
    private double ley;

    public MuestraRoca(int profundidad, double cantidadMineral, double ley) {
        this.profundidad = profundidad;
        this.cantidadMineral = cantidadMineral;
        this.ley = ley;
    }

    public int getProfundidad() { return profundidad; }
    public double getCantidadMineral() { return cantidadMineral; }
    public double getLey() { return ley; }
}

class Terreno {
    private String idTerreno;
    private TipoZona tipoZona;
    private boolean autorizacionEstado;
    private List<Permiso> permisos = new ArrayList<>();

    public Terreno(String idTerreno, TipoZona tipoZona, boolean autorizacionEstado) {
        this.idTerreno = idTerreno;
        this.tipoZona = tipoZona;
        this.autorizacionEstado = autorizacionEstado;
    }

    public boolean esZonaRestringida() { return tipoZona != TipoZona.LIBRE; }

    public boolean puedeIntervenirse() { return !esZonaRestringida() || autorizacionEstado; }

    public void agregarPermiso(Permiso permiso) { permisos.add(permiso); }

    public List<Permiso> getPermisos() { return permisos; }
    public String getIdTerreno() { return idTerreno; }
    public TipoZona getTipoZona() { return tipoZona; }
}

class Permiso {
    private TipoPermiso tipo;
    private LocalDate fechaVencimiento;

    public Permiso(TipoPermiso tipo, LocalDate fechaVencimiento) {
        this.tipo = tipo;
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean estaVigente() { return fechaVencimiento.isAfter(LocalDate.now()); }
    public TipoPermiso getTipo() { return tipo; }
}

class ConcesionExploracion {
    private String numero;
    private LocalDate fechaVencimiento;

    public ConcesionExploracion(String numero, LocalDate fechaVencimiento) {
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean estaVigente() { return fechaVencimiento.isAfter(LocalDate.now()); }
    public String getNumero() { return numero; }
}

// ===================== SERVICIO (se mockea en las unitarias) =====================
interface PermisoService {
    boolean tienePermisoVigente(Terreno terreno, TipoPermiso tipo);
}

class PermisoServiceImpl implements PermisoService {
    @Override
    public boolean tienePermisoVigente(Terreno terreno, TipoPermiso tipo) {
        if (terreno == null || tipo == null) {
            return false;
        }
        for (Permiso p : terreno.getPermisos()) {
            if (p.getTipo() == tipo && p.estaVigente()) {
                return true;
            }
        }
        return false;
    }
}
