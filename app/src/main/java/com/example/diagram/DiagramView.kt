package com.example.diagram

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.properties.Delegates

class DiagramView @JvmOverloads constructor(
    context : Context,
    attrs : AttributeSet? = null,
    defStyleAttr: Int = 0
) : View (context, attrs, defStyleAttr){

    private var mSectorsList = mutableListOf<Sector>()

    private val mMainTextPaint : Paint = Paint()
    private val mSecondaryTextPaint : Paint = Paint()
    private var mInnerCirclePaint: Paint = Paint()

    private var mInnerRadius by Delegates.notNull<Float>()
    private var mDiameter by Delegates.notNull<Int>()

    private var mAllValueSum: Float = 0f
    private var mSelectedIndex: Int = 0

    private var mCenterX by Delegates.notNull<Float>()
    private var mCenterY by Delegates.notNull<Float>()

    private var mTotalBounds: RectF
    private var mStandartBounds: RectF
    private var mSelectedBounds: RectF
    private var mMainTextBounds: Rect

    private var mGestureDetector: GestureDetector


    init {
        mMainTextPaint.color = Color.BLACK
        mMainTextPaint.textAlign = Paint.Align.CENTER
        mMainTextPaint.isAntiAlias = true
        mMainTextPaint.textSize = resources.getDimensionPixelSize(R.dimen.main_text_size).toFloat()

        mSecondaryTextPaint.color = Color.BLACK
        mSecondaryTextPaint.textAlign = Paint.Align.CENTER
        mSecondaryTextPaint.isAntiAlias = true
        mSecondaryTextPaint.textSize = resources.getDimensionPixelSize(R.dimen.secondary_text_size).toFloat()



        mInnerCirclePaint.color = Color.WHITE
        mInnerCirclePaint.style = Paint.Style.FILL
        mInnerCirclePaint.isAntiAlias = true

        mSectorsList = ArrayList<Sector>(5)
        mSectorsList.add(Sector(5f,  "Company A",ContextCompat.getColor(context, R.color.chart_1)))
        mSectorsList.add(Sector(30f, "Company B", ContextCompat.getColor(context, R.color.chart_2)))
        mSectorsList.add(Sector(15f, "Company C", ContextCompat.getColor(context, R.color.chart_3)))
        mSectorsList.add(Sector(40f, "Company D", ContextCompat.getColor(context, R.color.chart_4)))
        mSectorsList.add(Sector(10f, "Company E",ContextCompat.getColor(context, R.color.chart_5)))

        for (sector in mSectorsList) {
            mAllValueSum += sector.mValue
        }

        for (sector in mSectorsList) {
            sector.calculate(mAllValueSum)
        }

        mStandartBounds = RectF()
        mTotalBounds = RectF()
        mSelectedBounds = RectF()
        mMainTextBounds = Rect()

        mGestureDetector = GestureDetector(context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent?): Boolean {
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    return true
                }
            })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mInnerRadius = mMainTextPaint.measureText("100 %")
        mDiameter = (mInnerRadius * 4).toInt()

        mMainTextPaint.getTextBounds("1", 0, 1, mMainTextBounds)

        val mW = resolveSize(mDiameter, widthMeasureSpec)
        val mH = resolveSize(mDiameter, heightMeasureSpec)

        mCenterX = (mW / 2).toFloat()
        mCenterY = (mH / 2).toFloat()

        ///Вся область диагр.
        mTotalBounds.set(0f, 0f, mW.toFloat(), mH.toFloat())
        mStandartBounds.set(mTotalBounds)
        //уменьшаем размер области на половину innerRadius
        mStandartBounds.inset(mInnerRadius * 0.5f,mInnerRadius * 0.5f)

        mSelectedBounds.set(mTotalBounds)
        mSelectedBounds.inset(mInnerRadius * 0.25f, mInnerRadius * 0.25f)

        setMeasuredDimension(mW,mH)

    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        val cx: Float = (width / 2).toFloat()
        val cy: Float = (height / 2).toFloat()

        canvas?.save()
        canvas?.rotate(-90f, cx, cy)

        var startAngle = 0f


        var drawBounds: RectF
        for(sectorPos in mSectorsList.indices){
            if(sectorPos == mSelectedIndex){
                drawBounds = mSelectedBounds
            }else{
                drawBounds = mStandartBounds
            }
            startAngle = mSectorsList[sectorPos].draw(canvas, drawBounds, startAngle)
        }

        canvas?.drawCircle(cx, cy, mInnerRadius, mInnerCirclePaint)
        canvas?.restore()
        canvas?.drawText(mSectorsList[mSelectedIndex].getPercent(), cx,cy, mMainTextPaint)
        canvas?.drawText(mSectorsList[mSelectedIndex].getName(), cx,cy + mMainTextBounds.height(), mSecondaryTextPaint)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mGestureDetector.onTouchEvent(event) && checkTappedSector(event!!)
    }

    //Проверяем в какой сектор нажали
    private fun checkTappedSector(event: MotionEvent) : Boolean{
        val tappedAngle : Float = getAngle(event.x, event.y)

        for(i in mSectorsList.indices){
            if(i != mSelectedIndex && mSectorsList[i].isAngleInSector(tappedAngle)){
                mSelectedIndex = i
                invalidate()
                return true
            }
        }
        return false
    }

    private fun getAngle(touchX: Float, touchY: Float): Float {
        val angle: Float
        val x2: Float = touchX - mCenterX
        val y2: Float = touchY - mCenterY
        val d1: Double = sqrt(mCenterY * mCenterY).toDouble()
        val d2: Double = sqrt(x2 * x2 + y2 * y2).toDouble()
        if (touchX >= mCenterX) {
            angle = Math.toDegrees(acos((-mCenterY * y2) / (d1 * d2))).toFloat()
        } else {
            angle = (360 - Math.toDegrees(acos((-mCenterY * y2) / (d1 * d2)))).toFloat()
        }
        return angle
    }


    private class Sector constructor(
        val mValue : Float,
        val mName : String,
        mColor : Int
    ){
        private val mPaint: Paint = Paint()
        private var mAngle: Float = 0f
        private var mPercent: Float = 0f

        private var mStartAngle by Delegates.notNull<Float>()
        private var mEndAngle by Delegates.notNull<Float>()

        init {
            mPaint.style = Paint.Style.FILL
            mPaint.isAntiAlias = true
            mPaint.color = mColor
        }

        internal fun draw(canvas: Canvas?, bounds: RectF, startAngle: Float): Float {
            canvas?.drawArc(bounds, startAngle, mAngle, true, mPaint)
            mStartAngle = startAngle
            mEndAngle = startAngle + mAngle
            return mEndAngle
        }

        internal fun calculate(mAllValueSum: Float) {
            mAngle = mValue / mAllValueSum * 360f
            mPercent = mValue / mAllValueSum * 100f
        }

        internal fun getPercent() : String{
            return String.format(Locale.ENGLISH, "%.1f%%", mPercent)
        }

        internal fun getName() : String{
            return mName
        }

        internal fun isAngleInSector(tappedAngle : Float) : Boolean{
            return mStartAngle < tappedAngle && tappedAngle < mEndAngle
        }
    }

}