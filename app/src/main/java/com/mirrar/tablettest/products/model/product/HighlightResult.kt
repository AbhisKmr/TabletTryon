package com.mirrar.tablettest.products.model.product

data class HighlightResult(
    val brand: Brand,
    val dufryColour: DufryColour,
    val gender: Gender,
    val glassesFrameType: GlassesFrameType,
    val metaKeywords: MetaKeywords,
    val name: Name,
    val priceDutyFree: PriceDutyFree,
    val priceDutyPaid: PriceDutyPaid
)