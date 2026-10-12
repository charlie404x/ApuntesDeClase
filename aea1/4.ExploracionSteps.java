import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ExploracionSteps {

    private ConcesionExploracion concesion;
    private Map<String, Terreno> terrenos = new HashMap<>();
    private Map<String, PlataformaExploracion> plataformas = new HashMap<>();
    private ExploracionMinera exploracion;
    private PermisoService permisoService = new PermisoServiceImpl();
    private String error;
    private String resultado;

    // ============ GIVEN ============

    @Dado("que la empresa cuenta con la siguiente concesión:")
    public void registrarConcesion(DataTable tabla) {
        Map<String, String> fila = tabla.asMaps().get(0);
        concesion = new ConcesionExploracion(
                fila.get("numero"),
                LocalDate.parse(fila.get("fechaVencimiento")));
    }

    @Dado("que existe el terreno {string} con los siguientes datos:")
    public void registrarTerreno(String idTerreno, DataTable tabla) {
        Map<String, String> fila = tabla.asMaps().get(0);
        Terreno terreno = new Terreno(
                idTerreno,
                TipoZona.valueOf(fila.get("tipoZona")),
                Boolean.parseBoolean(fila.get("autorizacionEstado")));
        terrenos.put(idTerreno, terreno);
    }

    @Dado("que el terreno {string} tiene los siguientes permisos:")
    public void registrarPermisos(String idTerreno, DataTable tabla) {
        Terreno terreno = terrenos.get(idTerreno);
        for (Map<String, String> fila : tabla.asMaps()) {
            terreno.agregarPermiso(new Permiso(
                    TipoPermiso.valueOf(fila.get("tipo")),
                    LocalDate.parse(fila.get("fechaVencimiento"))));
        }
    }

    @Dado("que inicio la exploración {string} en el terreno {string}")
    public void iniciarExploracionDado(String idExploracion, String idTerreno) {
        exploracion = new ExploracionMinera(
                idExploracion, terrenos.get(idTerreno), concesion, permisoService);
    }

    @Dado("que registro las siguientes muestras de roca:")
    public void registrarMuestras(DataTable tabla) {
        for (Map<String, String> fila : tabla.asMaps()) {
            String codigo = fila.get("plataforma");
            PlataformaExploracion plataforma = plataformas.get(codigo);
            if (plataforma == null) {
                plataforma = new PlataformaExploracion(codigo);
                plataformas.put(codigo, plataforma);
                exploracion.agregarPlataforma(plataforma);
            }
            plataforma.agregarMuestra(new MuestraRoca(
                    Integer.parseInt(fila.get("profundidad")),
                    Double.parseDouble(fila.get("cantidadMineral")),
                    Double.parseDouble(fila.get("ley"))));
        }
    }

    @Dado("que registro una muestra con ley {double}")
    public void registrarUnaMuestra(double ley) {
        PlataformaExploracion plataforma = new PlataformaExploracion("PLT-01");
        plataforma.agregarMuestra(new MuestraRoca(10, 100.0, ley));
        exploracion.agregarPlataforma(plataforma);
    }

    // ============ WHEN ============

    @Cuando("inicio la exploración {string} en el terreno {string}")
    public void iniciarExploracion(String idExploracion, String idTerreno) {
        exploracion = new ExploracionMinera(
                idExploracion, terrenos.get(idTerreno), concesion, permisoService);
    }

    @Cuando("intento iniciar la exploración {string} en el terreno {string}")
    public void intentarIniciarExploracion(String idExploracion, String idTerreno) {
        try {
            exploracion = new ExploracionMinera(
                    idExploracion, terrenos.get(idTerreno), concesion, permisoService);
            resultado = "PERMITIDA";
        } catch (RuntimeException e) {
            error = e.getMessage();
            resultado = "RECHAZADA";
        }
    }

    @Cuando("finalizo la exploración")
    public void finalizarExploracion() {
        exploracion.finalizarExploracion();
    }

    @Cuando("intento finalizar la exploración")
    public void intentarFinalizarExploracion() {
        try {
            exploracion.finalizarExploracion();
        } catch (RuntimeException e) {
            error = e.getMessage();
        }
    }

    // ============ THEN ============

    @Entonces("el estado de la exploración debe ser {string}")
    public void validarEstado(String estadoEsperado) {
        assertEquals(EstadoExploracion.valueOf(estadoEsperado), exploracion.getEstado());
    }

    @Entonces("la ley promedio debe ser {double}")
    public void validarLeyPromedio(double leyEsperada) {
        assertEquals(leyEsperada, exploracion.calcularLeyPromedio(), 0.01);
    }

    @Entonces("la cantidad de mineral debe ser {double}")
    public void validarCantidadMineral(double cantidadEsperada) {
        assertEquals(cantidadEsperada, exploracion.calcularCantidadMineral(), 0.01);
    }

    @Entonces("el sistema debe mostrar el error: {string}")
    public void validarError(String mensajeEsperado) {
        assertEquals(mensajeEsperado, error);
    }

    @Entonces("el resultado de la exploración debe ser {string}")
    public void validarResultado(String resultadoEsperado) {
        assertEquals(resultadoEsperado, resultado);
    }
}
