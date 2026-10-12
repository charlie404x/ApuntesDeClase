import io.cucumber.datatable.DataTable;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CampeonatoSteps {

    private PagoService pagoService = new PagoServiceImpl();
    private Campeonato campeonato;
    private Map<String, Jugador> jugadores = new HashMap<>();
    private Jugador jugadorActual;
    private Inscripcion inscripcion;
    private List<Grupo> grupos;
    private String error;
    private String resultado;

    // ============ GIVEN ============

    @Dado("que existe el campeonato {string} de categoría {string}")
    public void crearCampeonato(String codigo, String categoria) {
        campeonato = new Campeonato(codigo, Categoria.valueOf(categoria), pagoService);
    }

    @Dado("que existe el jugador con los siguientes datos:")
    public void crearJugador(DataTable tabla) {
        Map<String, String> fila = tabla.asMaps().get(0);
        Jugador jugador = new Jugador(
                fila.get("dni"), fila.get("nombres"), fila.get("apellidos"));
        jugador.registrarHistorial(
                Integer.parseInt(fila.get("partidosGanados")),
                Integer.parseInt(fila.get("gamesGanados")));
        if (EstadoJugador.valueOf(fila.get("estado")) == EstadoJugador.INHABILITADO) {
            jugador.inhabilitar();
        }
        jugadores.put(jugador.getDni(), jugador);
        jugadorActual = jugador;
    }

    @Dado("que existe un jugador con experiencia que campeonó en la categoría {string}")
    public void crearJugadorCampeon(String categoriaCampeonada) {
        Jugador jugador = new Jugador("41234567", "Ana", "Torres");
        jugador.registrarHistorial(12, 40);
        jugador.setCategoriaCampeonada(Categoria.valueOf(categoriaCampeonada));
        jugadores.put(jugador.getDni(), jugador);
        jugadorActual = jugador;
    }

    @Dado("que se inscriben los siguientes jugadores:")
    public void inscribirJugadores(DataTable tabla) {
        for (Map<String, String> fila : tabla.asMaps()) {
            Jugador jugador = new Jugador(
                    fila.get("dni"), fila.get("nombres"), fila.get("apellidos"));
            jugador.registrarHistorial(
                    Integer.parseInt(fila.get("partidosGanados")),
                    Integer.parseInt(fila.get("gamesGanados")));
            jugadores.put(jugador.getDni(), jugador);
            campeonato.inscribirJugador(jugador, fila.get("codigoPago"));
        }
    }

    // ============ WHEN ============

    @Cuando("el jugador {string} se inscribe con el código de pago {string}")
    public void inscribirJugador(String dni, String codigoPago) {
        inscripcion = campeonato.inscribirJugador(jugadores.get(dni), codigoPago);
    }

    @Cuando("el jugador {string} intenta inscribirse con el código de pago {string}")
    public void intentarInscribirJugador(String dni, String codigoPago) {
        try {
            inscripcion = campeonato.inscribirJugador(jugadores.get(dni), codigoPago);
            resultado = "ACEPTADA";
        } catch (RuntimeException e) {
            error = e.getMessage();
            resultado = "RECHAZADA";
        }
    }

    @Cuando("ese jugador intenta inscribirse con el código de pago {string}")
    public void intentarInscribirJugadorActual(String codigoPago) {
        try {
            inscripcion = campeonato.inscribirJugador(jugadorActual, codigoPago);
            resultado = "ACEPTADA";
        } catch (RuntimeException e) {
            error = e.getMessage();
            resultado = "RECHAZADA";
        }
    }

    @Cuando("se realiza el sorteo de grupos")
    public void sortearGrupos() {
        grupos = campeonato.sortearGrupos();
    }

    // ============ THEN ============

    @Entonces("la inscripción debe quedar {string}")
    public void validarEstadoInscripcion(String estadoEsperado) {
        assertEquals(EstadoInscripcion.valueOf(estadoEsperado), inscripcion.getEstado());
    }

    @Entonces("el campeonato debe tener {int} jugador inscrito")
    public void validarUnInscrito(int cantidad) {
        assertEquals(cantidad, campeonato.getJugadoresConfirmados().size());
    }

    @Entonces("el campeonato debe tener {int} jugadores inscritos")
    public void validarInscritos(int cantidad) {
        assertEquals(cantidad, campeonato.getJugadoresConfirmados().size());
    }

    @Entonces("el sistema debe mostrar el error: {string}")
    public void validarError(String mensajeEsperado) {
        assertEquals(mensajeEsperado, error);
    }

    @Entonces("el resultado de la inscripción debe ser {string}")
    public void validarResultado(String resultadoEsperado) {
        assertEquals(resultadoEsperado, resultado);
    }

    @Entonces("se deben conformar {int} grupos de {int} jugadores")
    public void validarGrupos(int cantidadGrupos, int jugadoresPorGrupo) {
        assertEquals(cantidadGrupos, grupos.size());
        for (Grupo grupo : grupos) {
            assertEquals(jugadoresPorGrupo, grupo.getJugadores().size());
        }
    }

    @Entonces("ningún grupo debe tener más de una cabeza de serie")
    public void validarCabezasDeSerie() {
        List<Jugador> cabezas = campeonato.obtenerCabezasDeSerie();
        for (Grupo grupo : grupos) {
            assertTrue("El grupo " + grupo.getNumero() + " tiene mas de una cabeza de serie",
                    grupo.cantidadCabezasDeSerie(cabezas) <= 1);
        }
    }
}
