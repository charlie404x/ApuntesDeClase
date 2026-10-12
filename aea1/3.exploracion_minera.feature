# language: es
Característica: Gestión de exploración minera

  Como empresa minera
  Quiero registrar las exploraciones y las muestras de roca obtenidas
  Para determinar si una zona es viable para convertirse en mina

  Antecedentes:
    Dado que la empresa cuenta con la siguiente concesión:
      | numero  | fechaVencimiento |
      | CON-001 | 2027-12-31       |

  Escenario: Iniciar una exploración en un terreno libre con permiso vigente
    Dado que existe el terreno "TER-001" con los siguientes datos:
      | tipoZona | autorizacionEstado |
      | LIBRE    | false              |
    Y que el terreno "TER-001" tiene los siguientes permisos:
      | tipo        | fechaVencimiento |
      | EXPLORACION | 2027-06-30       |
      | AGUAS       | 2027-06-30       |
    Cuando inicio la exploración "EXP-001" en el terreno "TER-001"
    Entonces el estado de la exploración debe ser "EN_CURSO"

  Escenario: No se puede explorar una zona restringida sin autorización del Estado
    Dado que existe el terreno "TER-002" con los siguientes datos:
      | tipoZona      | autorizacionEstado |
      | ARQUEOLOGICA  | false              |
    Y que el terreno "TER-002" tiene los siguientes permisos:
      | tipo        | fechaVencimiento |
      | EXPLORACION | 2027-06-30       |
    Cuando intento iniciar la exploración "EXP-002" en el terreno "TER-002"
    Entonces el sistema debe mostrar el error: "El terreno es zona restringida y no cuenta con autorizacion."

  Escenario: No se puede explorar un terreno sin permiso vigente
    Dado que existe el terreno "TER-003" con los siguientes datos:
      | tipoZona | autorizacionEstado |
      | LIBRE    | false              |
    Y que el terreno "TER-003" tiene los siguientes permisos:
      | tipo        | fechaVencimiento |
      | EXPLORACION | 2020-01-15       |
    Cuando intento iniciar la exploración "EXP-003" en el terreno "TER-003"
    Entonces el sistema debe mostrar el error: "El terreno no cuenta con permiso vigente."

  Escenario: Calcular la ley promedio de una exploración con varias plataformas
    Dado que existe el terreno "TER-004" con los siguientes datos:
      | tipoZona | autorizacionEstado |
      | LIBRE    | false              |
    Y que el terreno "TER-004" tiene los siguientes permisos:
      | tipo        | fechaVencimiento |
      | EXPLORACION | 2027-06-30       |
    Y que inicio la exploración "EXP-004" en el terreno "TER-004"
    Y que registro las siguientes muestras de roca:
      | plataforma | profundidad | cantidadMineral | ley  |
      | PLT-01     | 10          | 100.0           | 0.60 |
      | PLT-01     | 20          | 150.0           | 0.90 |
      | PLT-02     | 15          | 120.0           | 0.75 |
    Cuando finalizo la exploración
    Entonces la ley promedio debe ser 0.75
    Y la cantidad de mineral debe ser 370.0
    Y el estado de la exploración debe ser "VIABLE"

  Escenario: No se puede finalizar una exploración sin muestras analizadas
    Dado que existe el terreno "TER-005" con los siguientes datos:
      | tipoZona | autorizacionEstado |
      | LIBRE    | false              |
    Y que el terreno "TER-005" tiene los siguientes permisos:
      | tipo        | fechaVencimiento |
      | EXPLORACION | 2027-06-30       |
    Y que inicio la exploración "EXP-005" en el terreno "TER-005"
    Cuando intento finalizar la exploración
    Entonces el sistema debe mostrar el error: "No se puede finalizar una exploracion sin muestras."

  Esquema del escenario: Viabilidad de la exploración según la ley promedio
    Dado que existe el terreno "TER-006" con los siguientes datos:
      | tipoZona | autorizacionEstado |
      | LIBRE    | false              |
    Y que el terreno "TER-006" tiene los siguientes permisos:
      | tipo        | fechaVencimiento |
      | EXPLORACION | 2027-06-30       |
    Y que inicio la exploración "EXP-006" en el terreno "TER-006"
    Y que registro una muestra con ley <ley>
    Cuando finalizo la exploración
    Entonces el estado de la exploración debe ser "<estado>"

    Ejemplos:
      | ley  | estado    |
      | 0.49 | NO_VIABLE |
      | 0.50 | VIABLE    |
      | 0.51 | VIABLE    |
      | 0.90 | VIABLE    |

  Esquema del escenario: Intervención del terreno según el tipo de zona
    Dado que existe el terreno "TER-007" con los siguientes datos:
      | tipoZona   | autorizacionEstado   |
      | <tipoZona> | <autorizacionEstado> |
    Y que el terreno "TER-007" tiene los siguientes permisos:
      | tipo        | fechaVencimiento |
      | EXPLORACION | 2027-06-30       |
    Cuando intento iniciar la exploración "EXP-007" en el terreno "TER-007"
    Entonces el resultado de la exploración debe ser "<resultado>"

    Ejemplos:
      | tipoZona         | autorizacionEstado | resultado |
      | LIBRE            | false              | PERMITIDA |
      | URBANA           | false              | RECHAZADA |
      | URBANA           | true               | PERMITIDA |
      | RESERVA_NACIONAL | false              | RECHAZADA |
      | CERCADA          | true               | PERMITIDA |
