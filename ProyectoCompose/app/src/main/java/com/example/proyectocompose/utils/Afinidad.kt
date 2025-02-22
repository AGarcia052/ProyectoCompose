package com.example.proyectocompose.utils

import android.util.Log
import com.example.proyectocompose.model.Formulario
import com.example.proyectocompose.model.User
import kotlin.math.abs

object Afinidad {

    /**
     *
     * Algoritmo de afinidad
     *
     * Compara las preferencias del usuario y devuelve un resultado entre 2 y 10
     *
     * calcularAfinidad() devolverá true si el candidato tiene una puntuación mayor o igual a 6.5
     *
     * **/



    private var hijos = 0.0
    private var gustos = 0.0


    fun calcularAfinidad(usuario: User, candidato: User): Boolean {

        val formUsuario = usuario.formulario
        val formCandidato = candidato.formulario


        val parteInteresSexual =
            parteInteresSexual(formUsuario = formUsuario, formCandidato = formCandidato)
        val parteGustos = parteGustos(formUsuario = formUsuario, formCandidato = formCandidato)
        val parteHijos = parteHijos(formUsuario = formUsuario, formCandidato = formCandidato)

        val scoreFinal = parteHijos + parteGustos + parteInteresSexual

        hijos = 0.0
        gustos = 0.0


        val resultado = scoreFinal >= 6.5
        Log.i(Constantes.TAG,"Algoritmo afinidad: RESULTADO DE AFINIDAD: ${scoreFinal},${resultado}")
        return resultado

    }

    /**
     * Cálculo de la afinidad en hijos.
     *
     * La puntuación varía del resultado de InteresSexual(3;1,3;1).
     *
     * Se puntúa más en caso de que ambos quieran una relación seria, en los demás casos su
     * peso se reduce.
     *
     * **/
    private fun parteHijos(formUsuario: Formulario, formCandidato: Formulario): Double {

        fun tieneHijos(isComparacionNegativa: Boolean): Double =
            if (isComparacionNegativa) 0.0 else if (hijos == 3.0) 1.0 else 0.3

        fun quiereHijos(isComparacionNegativa: Boolean): Double =
            if (isComparacionNegativa) 0.0 else when (hijos) {
                3.0 -> 2.0
                1.3 -> 1.0
                else -> 0.7
            }

        // Usuario no quiere hijos pero candidato tiene
        val casoUno = tieneHijos(!formUsuario.quiereHijos && formCandidato.tieneHijos)
        // Candidato no quiere hijos pero usuario tiene
        val casoDos = tieneHijos(!formCandidato.quiereHijos && formUsuario.tieneHijos)
        // Usuario quiere hijos pero candidato no
        val casoTres = quiereHijos(formUsuario.quiereHijos && !formCandidato.quiereHijos)
        // Candidato quiere hijos pero usuario no
        val casoCuatro = quiereHijos(formCandidato.quiereHijos && !formUsuario.quiereHijos)

        val totalTiene =(casoUno + casoDos)/2

        val totalQuiere = (casoTres + casoCuatro)/2
        Log.d(Constantes.TAG,"TOTAL TIENE: $totalTiene, TOTAL QUIERE: $totalQuiere")

        return  totalTiene+ totalQuiere


    }

    /**
     * Cálculo de afinidad sexual
     *
     * Puntuación total: 3
     *
     * Se comprueba si el interesSexual y el sexo de la otra persona son compatibles y viceversa.
     *
     * Este cálculo afecta los demás, variando la puntuación máxima del resto.
     *
     * En caso de ambos no querer una relación seria,
     * **/

    private fun parteInteresSexual(formUsuario: Formulario,formCandidato: Formulario): Double{

        val interesGeneral: Double

        val interesSexualCompatible = (formUsuario.interesSexual == "Ambos" || formUsuario.interesSexual == formCandidato.sexo) &&
            (formCandidato.interesSexual == "Ambos" || formCandidato.interesSexual == formUsuario.sexo)


        val coincideRelacion = formUsuario.relacionSeria == formCandidato.relacionSeria


        if (interesSexualCompatible){

            if(coincideRelacion && formUsuario.relacionSeria){
                 hijos = 3.0
                gustos = 4.0
                interesGeneral = 3.0
            }else{
                 hijos = 1.3
                interesGeneral = 3.0
                gustos = 5.7
            }

        }
        else{
            hijos = 1.0
            interesGeneral = 1.0
            gustos = 6.0
        }

        return interesGeneral

    }


    /**
     * Calculo de afinidad de gustos
     *
     * La puntuación varía del resultado de InteresSexual (6; 5,7; 4).
     *
     * Se obtiene el valor absoluto de la diferencia de la puntuación registrada en sus formularios,
     * que es restado a 100, para obtener un valor porcentual.
     *
     *
     **/

    private fun parteGustos(formUsuario: Formulario, formCandidato: Formulario): Double{
        val politica = calcAfinidadGustos(formUsuario.politica,formCandidato.politica)/3
        val arte = calcAfinidadGustos(formUsuario.arte,formCandidato.arte)/3
        val deporte = calcAfinidadGustos(formUsuario.deportes,formCandidato.deportes)/3

        Log.d(Constantes.TAG,"Politica: $politica, Arte: $arte, Deporte: $deporte")

        return (politica+arte+deporte)
    }

    private fun calcAfinidadGustos(numUsuario: Int, numCandidato: Int): Double {
        val afinidad = 100 - abs(numUsuario - numCandidato)

        fun calcularScore(afinidad: Int, max: Double, medio: Double, bajo: Double, min: Double): Double {
            return when {
                afinidad >= 75 -> max
                afinidad >= 50 -> medio
                afinidad >= 25 -> bajo
                else -> min
            }
        }

        return when (gustos) {
            6.0  -> calcularScore(afinidad, max = 6.0, medio = 4.0, bajo = 2.0, min = 1.0)
            5.7  -> calcularScore(afinidad, max = 5.7, medio = 3.5, bajo = 2.0, min = 1.0)
            else -> calcularScore(afinidad, max = 4.0, medio = 3.0, bajo = 1.75, min = 1.0)
        }
    }
}