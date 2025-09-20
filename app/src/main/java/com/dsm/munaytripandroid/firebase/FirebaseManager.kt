// app/src/main/java/com/dsm/munaytripandroid/firebase/FirebaseManager.kt
package com.dsm.munaytripandroid.firebase

// ✅ IMPORTS NECESARIOS - Agregar estos al inicio del archivo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
//import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {

    // ✅ Instancias principales
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    //val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    // ✅ Referencias frecuentes a colecciones
    val usersCollection get() = firestore.collection("users")
    val providersCollection get() = firestore.collection("providers")
    val touristsCollection get() = firestore.collection("tourists")
    val offersCollection get() = firestore.collection("offers")
    val bookingsCollection get() = firestore.collection("bookings")
    val reviewsCollection get() = firestore.collection("reviews")
    val interactionsCollection get() = firestore.collection("interactions")
    val analyticsCollection get() = firestore.collection("provider_analytics")

    // ✅ Inicializar Firebase (llamar en Application class)
    fun initialize() {
        // Configuraciones opcionales para Firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Cache offline
            .build()
        firestore.firestoreSettings = settings
    }

    // ✅ Función helper para verificar si hay usuario logueado
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // ✅ Función helper para obtener ID del usuario actual
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ✅ Función helper para obtener email del usuario actual
    fun getCurrentUserEmail(): String? = auth.currentUser?.email
}