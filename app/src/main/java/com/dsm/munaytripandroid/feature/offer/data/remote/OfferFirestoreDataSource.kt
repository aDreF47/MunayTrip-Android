package com.dsm.munaytripandroid.feature.offer.data.remote

import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.dsm.munaytripandroid.feature.offer.domain.model.OfferHorario
import com.dsm.munaytripandroid.feature.offer.domain.model.OfferLocation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OfferFirestoreDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val OFFERS_COLLECTION = "offers"
    }

    /**
     * Crear oferta
     */
    suspend fun createOffer(offer: Offer, providerId: String): String {
        val offerId = firestore.collection(OFFERS_COLLECTION).document().id

        val offerMap = hashMapOf(
            "offerId" to offerId,
            "providerId" to providerId,
            "titulo" to offer.titulo,
            "descripcionCorta" to offer.descripcionCorta,
            "descripcionLarga" to offer.descripcionLarga,
            "categoria" to offer.categoria.name.lowercase(),
            "etiquetas" to offer.etiquetas,
            "ubicacion" to hashMapOf(
                "lat" to offer.ubicacion.lat,
                "lng" to offer.ubicacion.lng,
                "nombre" to offer.ubicacion.nombre,
                "direccion" to offer.ubicacion.direccion
            ),
            "duracion" to offer.duracion,
            "horarios" to offer.horarios.map { horario ->
                hashMapOf(
                    "dia" to horario.dia,
                    "horaInicio" to horario.horaInicio,
                    "horaFin" to horario.horaFin
                )
            },
            "precio" to offer.precio,
            "esGratis" to offer.esGratis,
            "capacidadMaxima" to offer.capacidadMaxima,
            "cuposDisponibles" to offer.cuposDisponibles,
            "incluye" to offer.incluye,
            "recomendaciones" to offer.recomendaciones,
            "descuento" to offer.descuento,
            "imageUrls" to offer.imageUrls,
            "thumbnailUrls" to offer.thumbnailUrls,
            "tipoOferta" to offer.tipoOferta.name.lowercase(),
            "requiereConfirmacion" to offer.requiereConfirmacion,
            "instruccionesPago" to offer.instruccionesPago,
            "estado" to "activo",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(OFFERS_COLLECTION)
            .document(offerId)
            .set(offerMap)
            .await()

        return offerId
    }

    /**
     * Obtener ofertas de un proveedor
     */
    fun getProviderOffers(providerId: String): Flow<List<Offer>> = callbackFlow {
        val listener = firestore.collection(OFFERS_COLLECTION)
            .whereEqualTo("providerId", providerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val offers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toOffer()
                } ?: emptyList()

                trySend(offers)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtener todas las ofertas (para clientes)
     */
    fun getAllOffers(): Flow<List<Offer>> = callbackFlow {
        val listener = firestore.collection(OFFERS_COLLECTION)
            .whereEqualTo("estado", "activo")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val offers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toOffer()
                } ?: emptyList()

                trySend(offers)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtener ofertas por categoría
     */
    fun getOffersByCategory(category: String): Flow<List<Offer>> = callbackFlow {
        val listener = firestore.collection(OFFERS_COLLECTION)
            .whereEqualTo("estado", "activo")
            .whereEqualTo("categoria", category.lowercase())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val offers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toOffer()
                } ?: emptyList()

                trySend(offers)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtener oferta por ID
     */
    suspend fun getOfferById(offerId: String): Offer? {
        return try {
            val doc = firestore.collection(OFFERS_COLLECTION)
                .document(offerId)
                .get()
                .await()

            doc.toOffer()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualizar oferta
     */
    suspend fun updateOffer(offerId: String, updates: Map<String, Any>) {
        val updatesWithTimestamp = updates.toMutableMap().apply {
            put("updatedAt", FieldValue.serverTimestamp())
        }

        firestore.collection(OFFERS_COLLECTION)
            .document(offerId)
            .update(updatesWithTimestamp)
            .await()
    }

    /**
     * Eliminar oferta (soft delete)
     */
    suspend fun deleteOffer(offerId: String) {
        firestore.collection(OFFERS_COLLECTION)
            .document(offerId)
            .update(
                mapOf(
                    "estado" to "eliminado",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    /**
     * Buscar ofertas por texto
     */
    suspend fun searchOffers(query: String): List<Offer> {
        // Nota: Firestore no tiene búsqueda full-text nativa
        // Esta es una búsqueda básica por título
        val results = firestore.collection(OFFERS_COLLECTION)
            .whereEqualTo("estado", "activo")
            .get()
            .await()

        return results.documents
            .mapNotNull { it.toOffer() }
            .filter { offer ->
                offer.titulo.contains(query, ignoreCase = true) ||
                        offer.descripcionCorta.contains(query, ignoreCase = true) ||
                        offer.etiquetas.any { it.contains(query, ignoreCase = true) }
            }
    }

    // Helper para convertir DocumentSnapshot a Offer
    private fun com.google.firebase.firestore.DocumentSnapshot.toOffer(): Offer? {
        return try {
            val ubicacionMap = get("ubicacion") as? Map<*, *>
            val horarioslist = get("horarios") as? List<*>

            Offer(
                offerId = getString("offerId") ?: "",
                providerId = getString("providerId") ?: "",
                titulo = getString("titulo") ?: "",
                descripcionCorta = getString("descripcionCorta") ?: "",
                descripcionLarga = getString("descripcionLarga") ?: "",
                categoria = (getString("categoria") ?: "cultural").let { cat ->
                    try {
                        com.dsm.munaytripandroid.feature.offer.domain.model.OfferCategory.valueOf(cat.uppercase())
                    } catch (e: Exception) {
                        com.dsm.munaytripandroid.feature.offer.domain.model.OfferCategory.CULTURAL
                    }
                },
                etiquetas = (get("etiquetas") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                ubicacion = OfferLocation(
                    lat = (ubicacionMap?.get("lat") as? Double) ?: 0.0,
                    lng = (ubicacionMap?.get("lng") as? Double) ?: 0.0,
                    nombre = (ubicacionMap?.get("nombre") as? String) ?: "",
                    direccion = (ubicacionMap?.get("direccion") as? String) ?: ""
                ),
                duracion = getString("duracion") ?: "",
                horarios = (horarioslist?.mapNotNull { horario ->
                    val map = horario as? Map<*, *>
                    if (map != null) {
                        OfferHorario(
                            dia = (map["dia"] as? String) ?: "",
                            horaInicio = (map["horaInicio"] as? String) ?: "",
                            horaFin = (map["horaFin"] as? String) ?: ""
                        )
                    } else null
                }) ?: emptyList(),
                precio = getDouble("precio") ?: 0.0,
                esGratis = getBoolean("esGratis") ?: false,
                capacidadMaxima = getLong("capacidadMaxima")?.toInt() ?: 0,
                cuposDisponibles = getLong("cuposDisponibles")?.toInt() ?: 0,
                incluye = (get("incluye") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                recomendaciones = (get("recomendaciones") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                descuento = getLong("descuento")?.toInt() ?: 0,
                imageUrls = (get("imageUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                thumbnailUrls = (get("thumbnailUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                tipoOferta = (getString("tipoOferta") ?: "event").let { type ->
                    try {
                        com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.valueOf(type.uppercase())
                    } catch (e: Exception) {
                        com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.EVENT
                    }
                },
                requiereConfirmacion = getBoolean("requiereConfirmacion") ?: false,
                instruccionesPago = getString("instruccionesPago") ?: "",
                createdAt = getTimestamp("createdAt"),
                updatedAt = getTimestamp("updatedAt"),
                estado = getString("estado") ?: "activo"
            )
        } catch (e: Exception) {
            null
        }
    }
}