# language: es
Característica: Inscripción de jugadores a un campeonato de Tenis Champions

  Como organizador de campeonatos amateur
  Quiero que el sistema valide las inscripciones y sortee los grupos
  Para que los jugadores compitan en la categoría que les corresponde

  Antecedentes:
    Dado que existe el campeonato "CAMP-5TA" de categoría "QUINTA"

  Escenario: Inscripción válida de un jugador novato en quinta categoría
    Dado que existe el jugador con los siguientes datos:
      | dni      | nombres | apellidos | partidosGanados | gamesGanados | estado      |
      | 45678912 | Luis    | Ramirez   | 0               | 0            | HABILITADO  |
    Cuando el jugador "45678912" se inscribe con el código de pago "PAG-001"
    Entonces la inscripción debe quedar "CONFIRMADA"
    Y el campeonato debe tener 1 jugador inscrito

  Escenario: Un jugador sin experiencia no puede inscribirse en una categoría superior
    Dado que existe el campeonato "CAMP-3RA" de categoría "TERCERA"
    Y que existe el jugador con los siguientes datos:
      | dni      | nombres | apellidos | partidosGanados | gamesGanados | estado     |
      | 45678913 | Mario   | Quispe    | 0               | 0            | HABILITADO |
    Cuando el jugador "45678913" intenta inscribirse con el código de pago "PAG-002"
    Entonces el sistema debe mostrar el error: "Un jugador sin experiencia solo puede inscribirse en la quinta categoria."

  Escenario: Un jugador inhabilitado por conducta no deportiva no puede inscribirse
    Dado que existe el jugador con los siguientes datos:
      | dni      | nombres | apellidos | partidosGanados | gamesGanados | estado        |
      | 40111222 | Pedro   | Rojas     | 5               | 20           | INHABILITADO  |
    Cuando el jugador "40111222" intenta inscribirse con el código de pago "PAG-003"
    Entonces el sistema debe mostrar el error: "El jugador se encuentra inhabilitado por conducta no deportiva."

  Escenario: El pago no confirmado impide registrar la inscripción
    Dado que existe el jugador con los siguientes datos:
      | dni      | nombres | apellidos | partidosGanados | gamesGanados | estado     |
      | 44556677 | Marco   | Diaz      | 3               | 10           | HABILITADO |
    Cuando el jugador "44556677" intenta inscribirse con el código de pago "XXX"
    Entonces el sistema debe mostrar el error: "El pago de la inscripcion no pudo confirmarse."
    Y el campeonato debe tener 0 jugadores inscritos

  Escenario: Sorteo de grupos sin dos cabezas de serie juntas
    Dado que existe el campeonato "CAMP-2DA" de categoría "SEGUNDA"
    Y que se inscriben los siguientes jugadores:
      | dni      | nombres  | apellidos | partidosGanados | gamesGanados | codigoPago |
      | 10000001 | Jorge    | Salas     | 20              | 80           | PAG-1001   |
      | 10000002 | Carla    | Mendoza   | 18              | 70           | PAG-1002   |
      | 10000003 | Ruben    | Flores    | 3               | 12           | PAG-1003   |
      | 10000004 | Diana    | Vargas    | 4               | 14           | PAG-1004   |
      | 10000005 | Said     | Castro    | 5               | 16           | PAG-1005   |
      | 10000006 | Elena    | Paredes   | 6               | 18           | PAG-1006   |
      | 10000007 | Tomas    | Ibanez    | 7               | 20           | PAG-1007   |
      | 10000008 | Rosa     | Chavez    | 8               | 22           | PAG-1008   |
    Cuando se realiza el sorteo de grupos
    Entonces se deben conformar 2 grupos de 4 jugadores
    Y ningún grupo debe tener más de una cabeza de serie

  Esquema del escenario: Restricción por la categoría que el jugador ya campeonó
    Dado que existe el campeonato "CAMP-X" de categoría "<categoriaCampeonato>"
    Y que existe un jugador con experiencia que campeonó en la categoría "<categoriaCampeonada>"
    Cuando ese jugador intenta inscribirse con el código de pago "PAG-900"
    Entonces el resultado de la inscripción debe ser "<resultado>"

    Ejemplos:
      | categoriaCampeonato | categoriaCampeonada | resultado |
      | QUINTA              | TERCERA             | RECHAZADA |
      | CUARTA              | TERCERA             | RECHAZADA |
      | TERCERA             | TERCERA             | RECHAZADA |
      | SEGUNDA             | TERCERA             | ACEPTADA  |
      | PRIMERA             | TERCERA             | ACEPTADA  |
