package com.mirrar.tablettryon.view.fragment.tryon.dataModel

data class Product(
    val _highlightResult: HighlightResult,
    val age: String,
    val alcoholByVolume: String,
    val appellation: String,
    val benefits: String,
    val brand: String,
    val brandColour: String,
    val categories: String,
    val chocolateType: String,
    val codGamma: String,
    val cognacAge: String,
    val colourVariant: Boolean,
    val concentration: String,
    val concerns: String,
    val countryOfOrigin: String,
    val coverage: String,
    val createdAt: String,
    val currency: String,
    val dcisCategory: String,
    val dcisGroup: String,
    val dcisSubgroup: String,
    val description: String,
    val doNotSell: Int,
    val dufryColour: String,
    val finish: String,
    val flavour: String,
    val fragranceBottleType: String,
    val fragranceNotes: Any,
    val fragranceNotesBase: String,
    val fragranceNotesHeart: String,
    val fragranceNotesTop: String,
    val gender: String,
    val glassesFrameType: String,
    val globalItemCode: String,
    val howToApply: String,
    val howToEnjoy: String,
    val imageExtra1: String?,
    val imageExtra2: String?,
    val imageSmall: String?,
    val imageThumbnail: String?,
    val imageUrlBase: String?,
    val ingredients: String,
    val isNovelty: Boolean,
    val limitedEdition: Any,
    val liquorColour: String,
    val localItemCode: String,
    val makeUpType: String,
    val massInGrams: String,
    val metaKeywords: String,
    val name: String,
    val numberOfPieces: String,
    val objectID: String,
    val olfactoryFamily: String,
    val packSizeText: String,
    val packagingType: String,
    val parentProductId: Any,
    val priceDutyFree: Float,
    val priceDutyPaid: Float,
    val quotaCategory: Any,
    val reasonsToBuy: String,
    val rsvCode: String,
    val sapCode: String,
    val shortDescription: Any,
    val size: String,
    val sizeVariant: Boolean,
    val skinCareType: String,
    val skinType: String,
    val skincareArea: String,
    val skincareFormat: String,
    val skincareTime: String,
    val sku: String,
    val specialDiet: String,
    val specificsAwards: String,
    val sunProtectionFactor: String,
    val tastingNotesBody: String,
    val tastingNotesFinish: String,
    val tastingNotesNose: String,
    val tastingNotesPalate: String,
    val travelExclusive: Any,
    val ukHexColourCode1: Any,
    val updatedAt: String,
    val virtualTryOn: String,
    val volumeContribution: String,
    val volumeInMl: String,
    val watchCaseColour: Any,
    val watchCaseMaterial: Any,
    val watchDigitalAnalogue: Any,
    val watchFaceDiameter: Any,
    val watchShape: String,
    val watchStrapColour: Any,
    val watchStrapMaterial: String,
    val waterResistant: String,
    val waterproof: String,
    val weight: Any,
    val whiskeyVtoStyle: String,
    val whiskyRegion: Any,
    val whiskyStyle: String,
    val wineRegion: Any,
    val year: String,
    val productUrl: String?,
    val imageFrontView: String?
) {
    var isBookmarked = false
    var isRecommended = false
}