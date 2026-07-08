package shopzen.presentation.catalog.viewmodel

import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product

internal fun homeBannerImages(): List<String> = listOf(
    "https://images.unsplash.com/photo-1619134778706-7015533a6150?q=80&w=1200",
)

internal fun fallbackHomeBrands(): List<Brand> = listOf(
    Brand(name = "Fine Jewelry"),
    Brand(name = "Watches"),
    Brand(name = "Handbags"),
)

internal fun fallbackHomeCategories(): List<Category> = listOf(
    Category(
        id = "fine-jewelry",
        title = "Fine Jewelry",
        imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?q=80&w=600",
    ),
    Category(
        id = "watches",
        title = "Watches",
        imageUrl = "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?q=80&w=600",
    ),
)

internal fun fallbackHomeProducts(): List<Product> = listOf(
    Product(
        id = "1",
        title = "Aethelgard Diamond Ring",
        vendor = "LUXE",
        productType = "Fine Jewelry",
        price = "1,200",
        imageUrl = "https://images.unsplash.com/photo-1605100804763-247f67b3557e?q=80&w=600",
    ),
    Product(
        id = "2",
        title = "Obsidian Chronograph",
        vendor = "LUXE",
        productType = "Watches",
        price = "4,500",
        imageUrl = "https://images.unsplash.com/photo-1522312346375-d1a52e2b99b3?q=80&w=600",
    ),
    Product(
        id = "3",
        title = "Ivory Leather Tote",
        vendor = "LUXE",
        productType = "Handbags",
        price = "2,800",
        imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?q=80&w=600",
    ),
    Product(
        id = "4",
        title = "Aura Pearl Hoops",
        vendor = "LUXE",
        productType = "Fine Jewelry",
        price = "850",
        imageUrl = "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?q=80&w=600",
    ),
)
