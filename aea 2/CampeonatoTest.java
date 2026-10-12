import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CampeonatoTest {

    // ===================== PRUEBAS UNITARIAS =====================

    // Unitaria 1: un jugador sin experiencia solo puede iniciar en quinta categoria
    @Test
    public void jugadorSinExperiencia_noPuedeInscribirseEnTerceraCategoria() {
        // Arrange
        PagoService pagoMock = mock(PagoService.class);
        Campeonato campeonato = new Campeonato("CAMP-3RA", Categoria.TERCERA, pagoMock);
        Jugador novato = new Jugador("45678912", "Luis", "Ramirez"); // ranking 0

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                campeonato.inscribirJugador(novato, "PAG-001"));
        assertEquals("Un jugador sin experiencia solo puede inscribirse en la quinta categoria.",
                ex.getMessage());

        // La validacion ocurre antes del cobro: el pago nunca debe procesarse
        verifyNoInteractions(pagoMock);
        assertEquals(0, campeonato.getInscripciones().size());
    }

    // Unitaria 2: no se puede competir en una categoria ya campeonada ni en una inferior
    @Test
    public void jugadorCampeonDeTercera_noPuedeInscribirseEnCuarta() {
        // Arrange
        PagoService pagoMock = mock(PagoService.class);
        Campeonato campeonato = new Campeonato("CAMP-4TA", Categoria.CUARTA, pagoMock);
        Jugador campeon = new Jugador("41234567", "Ana", "Torres");
        campeon.registrarHistorial(12, 40);
        campeon.setCategoriaCampeonada(Categoria.TERCERA);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                campeonato.inscribirJugador(campeon, "PAG-002"));
        assertEquals("El jugador no puede inscribirse en una categoria igual o inferior a la que ya campeono.",
                ex.getMessage());

        verifyNoInteractions(pagoMock);
    }

    // Unitaria 3: un jugador inhabilitado por conducta no deportiva queda fuera
    @Test
    public void jugadorInhabilitado_noPuedeInscribirse() {
        // Arrange
        PagoService pagoMock = mock(PagoService.class);
        Campeonato campeonato = new Campeonato("CAMP-5TA", Categoria.QUINTA, pagoMock);
        Jugador sancionado = new Jugador("40111222", "Pedro", "Rojas");
        sancionado.inhabilitar();

        // Act + Assert
        assertThrows(IllegalStateException.class, () ->
                campeonato.inscribirJugador(sancionado, "PAG-003"));

        verifyNoInteractions(pagoMock);
        assertEquals(0, campeonato.getInscripciones().size());
    }

    // Unitaria 4 (complementaria): sin pago confirmado no hay inscripcion
    @Test
    public void pagoNoConfirmado_noRegistraLaInscripcion() {
        // Arrange
        PagoService pagoMock = mock(PagoService.class);
        when(pagoMock.confirmarPago("44556677", "PAG-XXX")).thenReturn(false);
        Campeonato campeonato = new Campeonato("CAMP-5TA", Categoria.QUINTA, pagoMock);
        Jugador jugador = new Jugador("44556677", "Marco", "Diaz");

        // Act + Assert
        assertThrows(IllegalStateException.class, () ->
                campeonato.inscribirJugador(jugador, "PAG-XXX"));

        verify(pagoMock, times(1)).confirmarPago("44556677", "PAG-XXX");
        assertEquals(0, campeonato.getInscripciones().size());
    }

    // ===================== PRUEBA INTEGRAL =====================

    // Integral: inscripcion de 8 jugadores con el servicio real y sorteo de grupos
    // Verifica la regla de que dos cabezas de serie no pueden caer en el mismo grupo
    @Test
    public void flujoCompleto_inscripcionYSorteoDeGruposSinDosCabezasDeSerie() {
        // Arrange
        PagoService pagoReal = new PagoServiceImpl();
        Campeonato campeonato = new Campeonato("CAMP-2DA", Categoria.SEGUNDA, pagoReal);

        // Dos jugadores de ranking alto (futuras cabezas de serie) y seis regulares
        Jugador top1 = new Jugador("10000001", "Jorge", "Salas");
        top1.registrarHistorial(20, 80);
        Jugador top2 = new Jugador("10000002", "Carla", "Mendoza");
        top2.registrarHistorial(18, 70);

        campeonato.inscribirJugador(top1, "PAG-1001");
        campeonato.inscribirJugador(top2, "PAG-1002");

        for (int i = 3; i <= 8; i++) {
            Jugador jugador = new Jugador("1000000" + i, "Jugador" + i, "Apellido" + i);
            jugador.registrarHistorial(i, i * 2);
            campeonato.inscribirJugador(jugador, "PAG-100" + i);
        }

        // Act
        List<Grupo> grupos = campeonato.sortearGrupos();
        List<Jugador> cabezas = campeonato.obtenerCabezasDeSerie();

        // Assert
        assertEquals(8, campeonato.getInscripciones().size());
        assertEquals(2, grupos.size());
        assertEquals(2, cabezas.size());
        assertTrue(cabezas.contains(top1));
        assertTrue(cabezas.contains(top2));

        for (Grupo grupo : grupos) {
            assertEquals(4, grupo.getJugadores().size());
            assertEquals(1, grupo.cantidadCabezasDeSerie(cabezas));
        }
    }
}
