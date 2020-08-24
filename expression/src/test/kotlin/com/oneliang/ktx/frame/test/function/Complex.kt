package com.oneliang.ktx.frame.test.function

import com.oneliang.ktx.frame.expression.ExpressionException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin

val INVALID_COMPLEX = Complex()

/**
 * The Class Complex.
 */
class Complex {
    companion object {

        /**
         * add.
         * @param a the a
         * @param b the b
         * @return the complex
         */
        fun add(a: Complex, b: Complex): Complex {
            val real = a.real + b.real
            val imaginary = a.imaginary + b.imaginary
            return Complex(real, imaginary)
        }

        /**
         * add.
         * @param real the real
         * @param c the c
         * @return the complex
         */
        fun add(real: Double, c: Complex): Complex {
            return Complex(c.real + real, c.imaginary)
        }

        /**
         * sub.
         * @param a the a
         * @param b the b
         * @return the complex
         */
        fun sub(a: Complex, b: Complex): Complex {
            val real = a.real - b.real
            val imaginary = a.imaginary - b.imaginary
            return Complex(real, imaginary)
        }

        /**
         * Sub.
         * @param real the real
         * @param c the c
         * @return the complex
         */
        fun sub(real: Double, c: Complex): Complex {
            return Complex(c.real - real, c.imaginary)
        }

        /**
         * multiply.
         * @param a the a
         * @param b the b
         * @return the complex
         */
        fun mul(a: Complex, b: Complex): Complex {
            val real = a.real * b.real - a.imaginary * b.imaginary
            val imaginary = a.imaginary * b.real + a.real * b.imaginary
            return Complex(real, imaginary)
        }

        /**
         * conjugate.
         * @param c the c
         * @return the complex
         */
        fun conjugate(c: Complex): Complex {
            return Complex(c.real, -c.imaginary)
        }

        /**
         * div.
         * @param a the a
         * @param b the b
         * @return the complex
         * @throws ExpressionException the calculator exception
         */
        @Throws(ExpressionException::class)
        fun div(a: Complex, b: Complex): Complex {
            if (b.real == 0.0 && b.imaginary == 0.0) {
                throw ExpressionException("The complex number b is 0")
            }
            val c = Math.pow(b.real, 2.0)
            val d = Math.pow(b.imaginary, 2.0)
            var real: Double = a.real * b.real + a.imaginary * b.imaginary
            real /= c + d
            var imaginary: Double = a.imaginary * b.real - a.real * b.imaginary
            imaginary /= c + d
            return Complex(real, imaginary)
        }

        /**
         * abs.
         * @param z the z
         * @return the double
         */
        fun abs(z: Complex): Double {
            val x: Double
            val y: Double
            val ans: Double
            val temp: Double
            x = kotlin.math.abs(z.real)
            y = kotlin.math.abs(z.imaginary)
            when {
                x == 0.0 -> {
                    ans = y
                }
                y == 0.0 -> {
                    ans = x
                }
                x > y -> {
                    temp = y / x
                    ans = x * kotlin.math.sqrt(1.0 + temp * temp)
                }
                else -> {
                    temp = x / y
                    ans = y * kotlin.math.sqrt(1.0 + temp * temp)
                }
            }
            return ans
        }

        /**
         * sqrt.
         * @param c the c
         * @return the complex
         */
        fun sqrt(c: Complex): Complex {
            val real: Double
            val imaginary: Double
            val x: Double
            val y: Double
            val w: Double
            val r: Double
            var result = Complex()
            if (c.real == 0.0 && c.imaginary == 0.0) {
                result = Complex()
            } else {
                x = kotlin.math.abs(c.real)
                y = kotlin.math.abs(c.imaginary)
                if (x >= y) {
                    r = y / x
                    w = kotlin.math.sqrt(x) * kotlin.math.sqrt(0.5 * (1.0 + kotlin.math.sqrt(1.0 + r * r)))
                } else {
                    r = x / y
                    w = kotlin.math.sqrt(y) * kotlin.math.sqrt(0.5 * (r + kotlin.math.sqrt(1.0 + r * r)))
                }
                if (c.real >= 0.0) {
                    real = w
                    imaginary = c.imaginary / (2.0 * w)
                } else {
                    imaginary = if (c.imaginary >= 0) w else -w
                    real = c.imaginary / (2.0 * imaginary)
                }
                result = Complex(real, imaginary)
            }
            return result
        }

        /**
         * Complex.
         * @param x the x
         * @param c the c
         * @return the complex
         */
        fun mul(x: Double, c: Complex): Complex {
            val result = Complex()
            result.real = c.real * x
            result.imaginary = c.imaginary * x
            return result
        }

        /**
         * div.
         * @param x the x
         * @param c the c
         * @return the complex
         * @throws ExpressionException the calculator exception
         */
        @Throws(ExpressionException::class)
        fun div(x: Double, c: Complex): Complex {
            if (x == 0.0) {
                throw ExpressionException("scalar is 0")
            }
            val result = Complex()
            result.real = c.real / x
            result.imaginary = c.imaginary / x
            return result
        }

        /**
         * pow
         * @param c
         * @param exp
         * @return
         */
        fun pow(c: Complex, exp: Double): Complex {
            return c.pow(exp)
        }

        /**
         * power.
         * @param c the c
         * @param exp the exp
         * @return the complex
         */
        fun pow(c: Complex, exp: Complex): Complex {
            return c.pow(exp)
        }

        /**
         * Cbrt.
         * @param a the a
         * @return the complex
         */
        fun cbrt(a: Complex): Complex {
            var z = Complex()
            if (a.imaginary != 0.0) {
                z.real = Math.cbrt(abs(a)) * cos(a.arg() / 3.0)
                z.imaginary = Math.cbrt(abs(a)) * sin(a.arg() / 3.0)
            } else {
                z = Complex(Math.cbrt(a.real), 0.0)
            }
            return z
        }
    }

    /** real.  */
    var real = 0.0
    /** imaginary.  */
    var imaginary = 0.0

    /**
     * Complex.
     * @param r the r
     * @param i the i
     */
    constructor(r: Double, i: Double) {
        this.real = r
        this.imaginary = i
    }

    /**
     * Complex.
     */
    constructor() {
        real = 0.0
        imaginary = 0.0
    }

    /**
     * inverse.
     * @return the complex
     */
    fun inverse(): Complex {
        val result = Complex()
        val a = real * real
        val b = imaginary * imaginary
        if (a == 0.0 && b == 0.0) {
            result.real = 0.0
            result.imaginary = 0.0
        } else {
            result.real = real / (a + b)
            result.imaginary = imaginary / (a + b)
        }
        return result
    }

    /**
     * module.
     * @return the double
     */
    fun module(): Double {
        return kotlin.math.sqrt(real * real + imaginary * imaginary)
    }

    /**
     * arg.
     * @return the double
     */
    fun arg(): Double {
        var angle = atan2(imaginary, real)
        if (angle < 0) {
            angle += 2 * Math.PI
        }
        return angle * 180 / Math.PI
    }

    /**
     * negate.
     * @return the complex
     */
    fun negate(): Complex {
        return Complex(-real, -imaginary)
    }

    /**
     * exp.
     * @return the complex
     */
// E^c
    fun exp(): Complex {
        val exp_x = kotlin.math.exp(real)
        return Complex(exp_x * cos(imaginary), exp_x * sin(imaginary))
    }

    /**
     * log10().
     * @return the complex
     */
    fun log10(): Complex {
        val rpart = kotlin.math.sqrt(real * real + imaginary * imaginary)
        var ipart = atan2(imaginary, real)
        if (ipart > Math.PI) {
            ipart -= 2.0 * Math.PI
        }
        return Complex(kotlin.math.log10(rpart), 1 / ln(10.0) * ipart)
    }

    /**
     * log natural log.
     * @return the complex
     */
    fun log(): Complex {
        return Complex(ln(abs(this)), atan2(imaginary, real))
    }

    /**
     * sqrt.
     * @return the complex
     */
    fun sqrt(): Complex {
        val r = kotlin.math.sqrt(real * real + imaginary * imaginary)
        val rpart = kotlin.math.sqrt(0.5 * (r + this.real))
        var ipart = kotlin.math.sqrt(0.5 * (r - this.real))
        if (imaginary < 0.0) {
            ipart = -ipart
        }
        return Complex(rpart, ipart)
    }

    /**
     * pow.
     * @param exp the exp
     * @return the complex
     */
    fun pow(exp: Complex): Complex {
        var a = log()
        a = mul(exp, a)
        return a.exp()
    }

    /**
     * pow.
     * @param exp the exp
     * @return the complex
     */
    fun pow(exp: Double): Complex {
        var a = log()
        a = mul(exp, a)
        return a.exp()
    }

    /**
     * sin.
     * @return the complex
     */
    fun sin(): Complex {
        return Complex(sin(real) * kotlin.math.cosh(imaginary), cos(real) * kotlin.math.sinh(imaginary))
    }

    /**
     * cos.
     * @return the complex
     */
    fun cos(): Complex {
        return Complex(cos(real) * kotlin.math.cosh(imaginary), -StrictMath.sin(real) * kotlin.math.sinh(imaginary))
    }

    /**
     * tan.
     * @return the complex
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    fun tan(): Complex {
        return div(sin(), cos())
    }

    /**
     * asin.
     * @return the complex
     */
    fun asin(): Complex {
        val IM = Complex(0.0, -1.0)
        val ZP = mul(this, IM)
        val ZM = add(sub(Complex(1.0, 0.0), mul(this, this)).sqrt(), ZP)
        return mul(ZM.log(), Complex(0.0, 1.0))
    }

    /**
     * acos.
     * @return the complex
     */
    fun acos(): Complex {
        val IM = Complex(0.0, -1.0)
        val ZM = add(mul(sub(Complex(1.0, 0.0), mul(this, this)).sqrt(), IM), this)
        return mul(ZM.log(), Complex(0.0, 1.0))
    }

    /**
     * atan.
     * @return the complex
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    fun atan(): Complex {
        val IM = Complex(0.0, -1.0)
        val ZP = Complex(real, imaginary - 1.0)
        val ZM = Complex(-real, -imaginary - 1.0)
        return div(2.0, mul(IM, div(ZP, ZM).log()))
    }

    /**
     * sinh.
     * @return the complex
     */
    fun sinh(): Complex {
        return Complex(kotlin.math.sinh(real) * cos(imaginary), kotlin.math.cosh(real) * sin(imaginary))
    }

    /**
     * cosh.
     * @return the complex
     */
    fun cosh(): Complex {
        return Complex(kotlin.math.cosh(real) * cos(imaginary), kotlin.math.sinh(real) * sin(imaginary))
    }

    /**
     * tanh.
     * @return the complex
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    fun tanh(): Complex {
        return div(sinh(), cosh())
    }

    /**
     * atanh.
     * @return the complex
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    fun atanh(): Complex {
        return sub(add(1.0, this).log(), div(2.0, sub(1.0, this).negate().log()))
    }
}