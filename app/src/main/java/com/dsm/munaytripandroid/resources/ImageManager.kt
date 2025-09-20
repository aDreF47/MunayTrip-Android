package com.dsm.munaytripandroid.resources

import android.widget.ImageView
import com.dsm.munaytripandroid.data.models.Offer
import com.dsm.munaytripandroid.data.models.Provider

object ImageManager {

    fun loadOfferImage(imageView: ImageView, offer: Offer) {
        // Prioridad 1: URLs personalizadas
        if (offer.customImageUrls.isNotEmpty()) {
            loadUrlImage(imageView, offer.customImageUrls.first())
            return
        }

        // Prioridad 2: Recursos predefinidos
        if (offer.imageResources.isNotEmpty()) {
            imageView.setImageResource(offer.imageResources.first())
            return
        }

        // Prioridad 3: Imagen por defecto según categoría
//        val defaultImage = IconProvider.getCategoryImage(offer.categoria.name)
//        imageView.setImageResource(defaultImage)
    }

    fun loadProviderLogo(imageView: ImageView, provider: Provider) {
        // Prioridad 1: URL personalizada
        if (!provider.logoUrl.isNullOrBlank()) {
            loadUrlImage(imageView, provider.logoUrl)
            return
        }

        // Prioridad 2: Ícono por tipo
//        val iconResource = IconProvider.getProviderIcon(provider.tipo.name.lowercase())
//        imageView.setImageResource(iconResource)
    }

    private fun loadUrlImage(imageView: ImageView, url: String) {
        // Usar Glide o Picasso para cargar URLs
        // Por ahora, implementación básica
        // Glide.with(imageView.context).load(url).into(imageView)
    }
}