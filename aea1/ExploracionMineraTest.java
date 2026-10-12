import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExploracionMineraTest {

    // ===================== PRUEBAS UNITARIAS =====================

    // Unitaria 1: la ley promedio define si la exploracion es viable
    @Test
    public void leyPromedioMayorAlMinimo_debeSerViable() {
        // Arrange
        Terreno terreno = new Terreno("TER-001", TipoZona.LIBRE, false);
        ConcesionExploracion concesion =
                new ConcesionExploracion("CON-001", LocalDate.now().plusYears(1));
        PermisoService permisoMock = mock(PermisoService.class);
        when(permisoMock.tienePermisoVigente(terreno, TipoPermiso.EXPLORACION)).thenReturn(true);

        ExploracionMinera exploracion =
                new ExploracionMinera("EXP-001", terreno, concesion, permisoMock);

        PlataformaExploracion plataforma = new PlataformaExploracion("PLT-01");
        plataforma.agregarMuestra(new MuestraRoca(10, 100.0, 0.60));
        plataforma.agregarMuestra(new MuestraRoca(20, 150.0, 0.80));
        exploracion.agregarPlataforma(plataforma);

        // Act
        double ley = exploracion.calcularLeyPromedio();
        boolean viable = exploracion.esViable();

        // Assert
        assertEquals(0.70, ley, 0.01);
        assertTrue(viable);
        assertEquals(250.0, exploracion.calcularCantidadMineral(), 0.01);
    }

    // Unitaria 2: no se puede explorar con la concesion vencida
    @Test
    public void concesionVencida_debeLanzarIllegalStateException() {
        // Arrange
        Terreno terreno = new Terreno("TER-002", TipoZona.LIBRE, false);
        ConcesionExploracion concesion =
                new ConcesionExploracion("CON-002", LocalDate.now().minusDays(1));
        PermisoService permisoMock = mock(PermisoService.class);

        // Act + Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                new ExploracionMinera("EXP-002", terreno, concesion, permisoMock));
        assertEquals("La concesion de exploracion no esta vigente.", ex.getMessage());

        // La concesion se valida primero: el servicio de permisos no debe consultarse
        verifyNoInteractions(permisoMock);
    }

    // Unitaria 3: no se puede intervenir una zona restringida sin autorizacion
    @Test
    public void zonaRestringidaSinAutorizacion_debeLanzarIllegalStateException() {
        // Arrange
        Terreno terreno = new Terreno("TER-003", TipoZona.ARQUEOLOGICA, false);
        ConcesionExploracion concesion =
                new ConcesionExploracion("CON-003", LocalDate.now().plusYears(1));
        PermisoService permisoMock = mock(PermisoService.class);

        // Act + Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                new ExploracionMinera("EXP-003", terreno, concesion, permisoMock));
        assertEquals("El terreno es zona restringida y no cuenta con autorizacion.", ex.getMessage());

        verifyNoInteractions(permisoMock);
    }

    // Unitaria 4: no se puede explorar un terreno sin permiso vigente
    @Test
    public void terrenoSinPermisoVigente_debeLanzarIllegalArgumentException() {
        // Arrange
        Terreno terreno = new Terreno("TER-004", TipoZona.LIBRE, false);
        ConcesionExploracion concesion =
                new ConcesionExploracion("CON-004", LocalDate.now().plusYears(1));
        PermisoService permisoMock = mock(PermisoService.class);
        when(permisoMock.tienePermisoVigente(terreno, TipoPermiso.EXPLORACION)).thenReturn(false);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                new ExploracionMinera("EXP-004", terreno, concesion, permisoMock));

        verify(permisoMock, times(1)).tienePermisoVigente(terreno, TipoPermiso.EXPLORACION);
    }

    // ===================== PRUEBA INTEGRAL =====================

    // Integral: flujo completo con las clases reales
    // terreno con permiso -> exploracion -> plataformas con muestras -> cierre
    @Test
    public void flujoCompleto_exploracionConMuestrasDebeFinalizarComoViable() {
        // Arrange
        Terreno terreno = new Terreno("TER-005", TipoZona.LIBRE, false);
        terreno.agregarPermiso(new Permiso(TipoPermiso.EXPLORACION, LocalDate.now().plusMonths(6)));
        terreno.agregarPermiso(new Permiso(TipoPermiso.AGUAS, LocalDate.now().plusMonths(6)));

        ConcesionExploracion concesion =
                new ConcesionExploracion("CON-005", LocalDate.now().plusYears(2));
        PermisoService permisoReal = new PermisoServiceImpl();

        ExploracionMinera exploracion =
                new ExploracionMinera("EXP-005", terreno, concesion, permisoReal);

        PlataformaExploracion p1 = new PlataformaExploracion("PLT-01");
        p1.agregarMuestra(new MuestraRoca(10, 100.0, 0.60));
        p1.agregarMuestra(new MuestraRoca(20, 150.0, 0.90));

        PlataformaExploracion p2 = new PlataformaExploracion("PLT-02");
        p2.agregarMuestra(new MuestraRoca(15, 120.0, 0.75));

        exploracion.agregarPlataforma(p1);
        exploracion.agregarPlataforma(p2);

        // Act
        exploracion.finalizarExploracion();

        // Assert
        assertEquals(0.75, exploracion.calcularLeyPromedio(), 0.01);
        assertEquals(370.0, exploracion.calcularCantidadMineral(), 0.01);
        assertEquals(EstadoExploracion.VIABLE, exploracion.getEstado());
        assertEquals(2, exploracion.getPlataformas().size());
    }
}
