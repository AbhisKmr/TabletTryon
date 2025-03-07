package com.mirrar.tablettryon.view.fragment.tryon.dataModel

import kotlinx.serialization.Serializer

data class HighlightResult(
    val Brand: Brand,
    val Description: Description,
    val FaceShapeOp1: FaceShapeOp1,
    val FaceShapeOp2: FaceShapeOp1,
    val FrameShape_1: FrameShape1,
    val Primary: Primary,
    val ProductLink: ProductLink
)