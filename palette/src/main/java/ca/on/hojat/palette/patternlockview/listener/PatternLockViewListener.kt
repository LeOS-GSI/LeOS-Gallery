package ca.on.hojat.palette.patternlockview.listener

import ca.on.hojat.palette.patternlockview.PatternLockView.Dot

/**
 * The callback interface for detecting patterns
 * entered by the user.
 */
interface PatternLockViewListener {

    /**
     * Fired when the pattern drawing has just started.
     */
    fun onStarted()

    /**
     * Fired when the pattern is still being drawn and
     * progressed to one more.
     */
    fun onProgress(progressPattern: List<Dot>?)

    /**
     * Fired when the user has completed drawing the
     * pattern and has moved their finger away from the view.
     */
    fun onComplete(pattern: List<Dot>?)

    /**
     * Fired when the patten has been cleared from the view.
     */
    fun onCleared()
}