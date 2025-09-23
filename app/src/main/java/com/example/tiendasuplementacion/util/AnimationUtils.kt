package com.example.tiendasuplementacion.util

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable

object AnimationUtils {

    @Composable
    fun fadeInAnimation(durationMillis: Int = 300): EnterTransition {
        return fadeIn(animationSpec = tween(durationMillis))
    }

    @Composable
    fun fadeOutAnimation(durationMillis: Int = 300): ExitTransition {
        return fadeOut(animationSpec = tween(durationMillis))
    }

    @Composable
    fun slideInFromLeft(durationMillis: Int = 300): EnterTransition {
        return slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(durationMillis))
    }

    @Composable
    fun slideOutToRight(durationMillis: Int = 300): ExitTransition {
        return slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(durationMillis))
    }
}