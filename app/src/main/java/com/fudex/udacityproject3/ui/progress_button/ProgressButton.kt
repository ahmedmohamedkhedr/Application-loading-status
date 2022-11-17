package com.fudex.udacityproject3.ui.progress_button

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.fudex.udacityproject3.R
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.properties.Delegates

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var buttonDefaultBackgroundColor = 0
    private var buttonBackgroundColor = 0
    private var buttonDefaultText: CharSequence = ""
    private var buttonText: CharSequence = ""
    private var buttonTextColor = 0
    private var progressCircleBackgroundColor = 0


    private var mWidth = 0
    private var mHeight = 0
    private var currentBtnAnimationValue = 0f
    private var currentProgressAnimationValue = 0f
    private var btnText = ""


    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val btnTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
    }

    private var progressValueAnimator = ValueAnimator()
    private var btnValueAnimator = ValueAnimator()
    private val progressCRect = RectF()
    private lateinit var btnTxtBounds: Rect
    private var progressCSize = 0f

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.ProgressButton_V2) {
            buttonDefaultBackgroundColor =
                getColor(R.styleable.ProgressButton_V2_buttonDefaultBackgroundColor, 0)
            buttonBackgroundColor = getColor(R.styleable.ProgressButton_V2_buttonBackgroundColor, 0)
            buttonDefaultText = getText(R.styleable.ProgressButton_V2_buttonDefaultText)
            buttonText = getText(R.styleable.ProgressButton_V2_buttonText)
            buttonTextColor = getColor(R.styleable.ProgressButton_V2_buttonTextColor, 0)
        }.also {
            btnText = buttonDefaultText.toString()
            progressCircleBackgroundColor = ContextCompat.getColor(context, R.color.teal_200)
        }

        startAnimation()
    }

    private val animatorSetBtn: AnimatorSet = AnimatorSet().apply {
        duration = TimeUnit.SECONDS.toMillis(3)
        doOnStart { isEnabled = false }
        doOnEnd { isEnabled = true }
    }

    private var progressButtonState: ProgressButtonState by Delegates.observable(ProgressButtonState.Completed) { _, _, state ->

        when (state) {
            ProgressButtonState.Loading -> {
                btnText = buttonText.toString()

                animatorSetBtn.start()
                if (!::btnTxtBounds.isInitialized) {
                    btnTxtBounds = Rect()
                    btnTextPaint.getTextBounds(btnText, 0, btnText.length, btnTxtBounds)
                    val hCenter = (btnTxtBounds.right + btnTxtBounds.width() + 16f)
                    val vCenter = (mHeight / 2f)

                    progressCRect.set(
                        hCenter - progressCSize,
                        vCenter - progressCSize,
                        hCenter + progressCSize,
                        vCenter + progressCSize
                    )

                }
                invalidate()
                requestLayout()
            }
            else -> {
                btnText = buttonDefaultText.toString()
                animatorSetBtn.cancel()
                invalidate()
                requestLayout()
            }
        }
    }

    fun changeButtonState(state: ProgressButtonState) {
        progressButtonState = state
    }


    private fun startAnimation() {
        progressValueAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            addUpdateListener {
                currentProgressAnimationValue = this.animatedValue as Float
                invalidate()
                requestLayout()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        btnValueAnimator = ValueAnimator.ofFloat(0f, mWidth.toFloat()).apply {
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.RESTART
            addUpdateListener {
                currentBtnAnimationValue = it.animatedValue as Float
                invalidate()
                requestLayout()
            }
        }

        animatorSetBtn.playTogether(progressValueAnimator, btnValueAnimator)
        progressCSize = (min(w, h) / 2f) * 0.4f

    }

    override fun performClick(): Boolean {
        super.performClick()
        if (progressButtonState == ProgressButtonState.Completed) {
            progressButtonState = ProgressButtonState.Clicked
            invalidate()
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            setButtonBackground()
            setButtonText()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        mWidth = w
        mHeight = h
        setMeasuredDimension(w, h)
    }

    private fun TextPaint.computeTxtOffset() = ((descent() - ascent()) / 2) - descent()

    private fun Canvas.setButtonBackground() {
        when (progressButtonState) {
            ProgressButtonState.Loading -> {
                btnPaint.apply {
                    color = buttonBackgroundColor
                    drawRect(0f, 0f, currentBtnAnimationValue, mHeight.toFloat(), this)
                    color = buttonDefaultBackgroundColor
                    drawRect(
                        currentBtnAnimationValue,
                        0f,
                        mWidth.toFloat(),
                        mHeight.toFloat(),
                        this
                    )
                }
                btnPaint.color = progressCircleBackgroundColor
                drawArc(progressCRect, 0f, currentProgressAnimationValue, true, btnPaint)
            }
            else -> {
                drawColor(buttonDefaultBackgroundColor)
            }
        }
    }

    private fun Canvas.setButtonText() {
        btnTextPaint.color = buttonTextColor
        drawText(
            btnText,
            (mWidth / 2f),
            (mHeight / 2f) + btnTextPaint.computeTxtOffset(),
            btnTextPaint
        )
    }


}