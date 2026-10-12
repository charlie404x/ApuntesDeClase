import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// ===================== ENUMERACIONES =====================
enum Categoria {
    PRIMERA(1), SEGUNDA(2), TERCERA(3), CUARTA(4), QUINTA(5);

    private final int nivel;

    Categoria(int nivel) { this.nivel = nivel; }

    // Mientras mayor el nivel, mas baja es la categoria (QUINTA es la de inicio)
    public int getNivel() { return nivel; }
}

enum EstadoJugador { HABILITADO, INHABILITADO }

enum EstadoInscripcion { PENDIENTE, CONFIRMADA }

// ===================== CLASE CRITICA: main business rule =====================
class Campeonato {

    private static final int JUGADORES_POR_GRUPO = 4;

    private String codigo;
    private Categoria categoria;
    private List<Inscripcion> inscripciones = new ArrayList<>();
    private List<Grupo> grupos = new ArrayList<>();
    private PagoService pagoService;

    public Campeonato(String codigo, Categoria categoria, PagoService pagoService) {
        this.codigo = codigo;
        this.categoria = categoria;
        this.pagoService = pagoService;
    }

    public Inscripcion inscribirJugador(Jugador jugador, String codigoPago) {
        // Regla 1: un jugador inhabilitado por conducta no deportiva no puede competir
        if (!jugador.estaHabilitado()) {
            throw new IllegalStateException("El jugador se encuentra inhabilitado por conducta no deportiva.");
        }
        // Regla 2: un jugador sin experiencia solo puede iniciar en la quinta categoria
        if (!jugador.tieneExperiencia() && categoria != Categoria.QUINTA) {
            throw new IllegalArgumentException("Un jugador sin experiencia solo puede inscribirse en la quinta categoria.");
        }
        // Regla 3: no puede competir en una categoria que ya campeono ni en una inferior
        if (jugador.getCategoriaCampeonada() != null
                && categoria.getNivel() >= jugador.getCategoriaCampeonada().getNivel()) {
            throw new IllegalArgumentException("El jugador no puede inscribirse en una categoria igual o inferior a la que ya campeono.");
        }
        // Regla 4: no se admite doble inscripcion del mismo jugador
        if (estaInscrito(jugador)) {
            throw new IllegalStateException("El jugador ya se encuentra inscrito en el campeonato.");
        }
        // Regla 5: la inscripcion solo queda confirmada si el pago fue validado
        if (!pagoService.confirmarPago(jugador.getDni(), codigoPago)) {
            throw new IllegalStateException("El pago de la inscripcion no pudo confirmarse.");
        }

        Inscripcion inscripcion = new Inscripcion(jugador);
        inscripcion.confirmar();
        inscripciones.add(inscripcion);
        return inscripcion;
    }

    // Regla 6: los grupos se sortean de 4 jugadores y no pueden tener dos cabezas de serie
    public List<Grupo> sortearGrupos() {
        List<Jugador> inscritos = getJugadoresConfirmados();

        if (inscritos.size() < JUGADORES_POR_GRUPO || inscritos.size() % JUGADORES_POR_GRUPO != 0) {
            throw new IllegalStateException("El numero de inscritos debe ser multiplo de 4 para conformar los grupos.");
        }

        int numeroGrupos = inscritos.size() / JUGADORES_POR_GRUPO;
        List<Jugador> cabezas = obtenerCabezasDeSerie();
        List<Jugador> resto = new ArrayList<>(inscritos);
        resto.removeAll(cabezas);

        Collections.shuffle(cabezas);
        Collections.shuffle(resto);

        grupos = new ArrayList<>();
        for (int i = 0; i < numeroGrupos; i++) {
            Grupo grupo = new Grupo(i + 1);
            grupo.agregarJugador(cabezas.get(i));   // una sola cabeza de serie por grupo
            grupos.add(grupo);
        }

        int indice = 0;
        for (Jugador jugador : resto) {
            grupos.get(indice % numeroGrupos).agregarJugador(jugador);
            indice++;
        }
        return grupos;
    }

    // Las cabezas de serie son los mejores del ranking, uno por grupo
    public List<Jugador> obtenerCabezasDeSerie() {
        List<Jugador> ordenados = new ArrayList<>(getJugadoresConfirmados());
        ordenados.sort(Comparator.comparingInt(Jugador::getRanking).reversed());
        int numeroGrupos = ordenados.size() / JUGADORES_POR_GRUPO;
        return new ArrayList<>(ordenados.subList(0, numeroGrupos));
    }

    public List<Jugador> getJugadoresConfirmados() {
        List<Jugador> jugadores = new ArrayList<>();
        for (Inscripcion i : inscripciones) {
            if (i.getEstado() == EstadoInscripcion.CONFIRMADA) {
                jugadores.add(i.getJugador());
            }
        }
        return jugadores;
    }

    private boolean estaInscrito(Jugador jugador) {
        for (Inscripcion i : inscripciones) {
            if (i.getJugador().getDni().equals(jugador.getDni())) {
                return true;
            }
        }
        return false;
    }

    public String getCodigo() { return codigo; }
    public Categoria getCategoria() { return categoria; }
    public List<Inscripcion> getInscripciones() { return inscripciones; }
    public List<Grupo> getGrupos() { return grupos; }
}

// ===================== CLASES DE APOYO =====================
class Inscripcion {
    private Jugador jugador;
    private EstadoInscripcion estado;

    public Inscripcion(Jugador jugador) {
        this.jugador = jugador;
        this.estado = EstadoInscripcion.PENDIENTE;
    }

    public void confirmar() { this.estado = EstadoInscripcion.CONFIRMADA; }

    public Jugador getJugador() { return jugador; }
    public EstadoInscripcion getEstado() { return estado; }
}

class Grupo {
    private int numero;
    private List<Jugador> jugadores = new ArrayList<>();

    public Grupo(int numero) { this.numero = numero; }

    public void agregarJugador(Jugador jugador) { jugadores.add(jugador); }

    public int cantidadCabezasDeSerie(List<Jugador> cabezas) {
        int total = 0;
        for (Jugador j : jugadores) {
            if (cabezas.contains(j)) {
                total++;
            }
        }
        return total;
    }

    public int getNumero() { return numero; }
    public List<Jugador> getJugadores() { return jugadores; }
}

class Jugador {
    private String dni;
    private String nombres;
    private String apellidos;
    private EstadoJugador estado;
    private Categoria categoriaCampeonada;
    private int partidosGanados;
    private int gamesGanados;

    public Jugador(String dni, String nombres, String apellidos) {
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.estado = EstadoJugador.HABILITADO;
    }

    // El ranking se determina por partidos ganados y games ganados
    public int getRanking() { return (partidosGanados * 10) + gamesGanados; }

    public boolean tieneExperiencia() { return getRanking() > 0; }

    public boolean estaHabilitado() { return estado == EstadoJugador.HABILITADO; }

    public void registrarHistorial(int partidosGanados, int gamesGanados) {
        this.partidosGanados = partidosGanados;
        this.gamesGanados = gamesGanados;
    }

    public void inhabilitar() { this.estado = EstadoJugador.INHABILITADO; }

    public void setCategoriaCampeonada(Categoria categoria) { this.categoriaCampeonada = categoria; }

    public Categoria getCategoriaCampeonada() { return categoriaCampeonada; }
    public String getDni() { return dni; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public EstadoJugador getEstado() { return estado; }
}

// ===================== SERVICIO (se mockea en las unitarias) =====================
interface PagoService {
    boolean confirmarPago(String dni, String codigoPago);
}

class PagoServiceImpl implements PagoService {
    @Override
    public boolean confirmarPago(String dni, String codigoPago) {
        if (dni == null || codigoPago == null) {
            return false;
        }
        return codigoPago.startsWith("PAG") && codigoPago.length() >= 6;
    }
}
