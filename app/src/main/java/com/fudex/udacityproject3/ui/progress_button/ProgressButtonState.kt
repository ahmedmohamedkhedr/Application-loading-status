package com.fudex.udacityproject3.ui.progress_button

sealed class ProgressButtonState {
    object Clicked : ProgressButtonState()
    object Loading : ProgressButtonState()
    object Completed : ProgressButtonState()
}