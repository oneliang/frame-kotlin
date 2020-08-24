package com.oneliang.ktx.frame.test.function

import com.oneliang.ktx.frame.test.ParserManager
import com.oneliang.ktx.frame.expression.ExpressionException
import java.util.*

// TODO: Auto-generated Javadoc
/**
 * The Class ComplexFunction.
 */
class ComplexFunction(f: String) {

    companion object {
        /** The Constant SIN.  */
        const val SIN = "sin"
        /** The Constant COS.  */
        const val COS = "cos"
        /** The Constant SINH.  */
        const val SINH = "sinh"
        /** The Constant COSH.  */
        const val COSH = "cosh"
        /** The Constant TAN.  */
        const val TAN = "tan"
        /** The Constant TANH.  */
        const val TANH = "tanh"
        /** The Constant ASIN.  */
        const val ASIN = "asin"
        /** The Constant ACOS.  */
        const val ACOS = "acos"
        /** The Constant ATAN.  */
        const val ATAN = "atan"
        /** The Constant E.  */
        const val E = "e"
        /** The Constant PI.  */
        const val PI = "pi"
        /** The Constant LN.  */
        const val LN = "ln"
        /** The Constant LOG.  */
        const val LOG = "log"
        /** The Constant SQRT.  */
        const val SQRT = "sqrt"
        /** The Constant CBRT.  */
        const val CBRT = "cbrt"
    }

    var degree = false
    var f: String

    /**
     * FunctionXs.
     * @param f the f
     */
    init {
        this.f = f.trim { it <= ' ' }.replace(" ".toRegex(), "")
        degree = ParserManager.instance?.isDeegre ?: false
    }

    /**
     * getValue f(x0,y0,z0...)
     * @param values (sort the values taking into account the variables)
     * @param variables x,y,z etc
     * @return the value
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    fun getValue(values: List<Complex>, variables: List<String>): Complex {
        val vars: MutableList<String> = ArrayList()
        for (string in variables) {
            vars.add(string.toLowerCase())
        }
        return eval(f, values, vars)
    }

    /**
     * eval.
     * @param f the f
     * @param values the values
     * @param variables the variables
     * @return the complex
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    private fun eval(f: String, values: List<Complex>, variables: List<String>): Complex {
        var f = f
        f = f.trim { it <= ' ' }.toLowerCase()
        var value = Complex(0.0, 0.0)
        var number = ""
        var function = ""
        var hasNumber = false
        var hasFunction = false
        var isImaginary = false
        var i = 0
        run loop@{
            while (i < f.length) {
                val character = f[i]
                when (character) {
                    '*' -> if (hasNumber && !isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.mul(Complex(numb, 0.0), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasNumber = false
                        number = ""
                    } else if (hasNumber && isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.mul(Complex(0.0, numb), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasNumber = false
                        isImaginary = false
                        number = ""
                    } else if (hasFunction) {
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.mul(eval(function, values, variables), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasFunction = false
                        function = ""
                    } else {
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.mul(value, eval(new_f, values, variables))
                        i = i + new_f.length
                    }
                    '+' -> if (hasNumber && !isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = f.substring(i + 1, f.length)
                        value = Complex.add(Complex(numb, 0.0), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasNumber = false
                        number = ""
                    } else if (hasNumber && isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = f.substring(i + 1, f.length)
                        value = Complex.add(Complex(0.0, numb), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasNumber = false
                        isImaginary = false
                        number = ""
                    } else if (hasFunction) {
                        val new_f = f.substring(i + 1, f.length)
                        value = Complex.add(eval(function, values, variables), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasFunction = false
                        function = ""
                    } else {
                        val new_f = f.substring(i + 1, f.length)
                        value = Complex.add(value, eval(new_f, values, variables))
                        i = i + new_f.length
                    }
                    '-' -> if (hasNumber && !isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = nextMinusFunction(f.substring(i + 1, f.length))
                        value = Complex.sub(Complex(numb, 0.0), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasNumber = false
                        number = ""
                    } else if (hasNumber && isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = nextMinusFunction(f.substring(i + 1, f.length))
                        value = Complex.sub(Complex(0.0, numb), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasNumber = false
                        isImaginary = false
                        number = ""
                    } else if (hasFunction) {
                        val new_f = nextMinusFunction(f.substring(i + 1, f.length))
                        value = Complex.sub(eval(function, values, variables), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasFunction = false
                        function = ""
                    } else {
                        val new_f = nextMinusFunction(f.substring(i + 1, f.length))
                        value = Complex.sub(value, eval(new_f, values, variables))
                        i = i + new_f.length
                    }
                    '/' -> if (hasNumber && !isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.div(Complex(numb, 0.0), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasNumber = false
                        number = ""
                    } else if (hasNumber && isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.div(Complex(0.0, numb), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasNumber = false
                        isImaginary = false
                        number = ""
                    } else if (hasFunction) {
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.div(eval(function, values, variables), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasFunction = false
                        function = ""
                    } else {
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.div(value, eval(new_f, values, variables))
                        i = i + new_f.length
                    }
                    '^' -> if (hasNumber && !isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.pow(eval(new_f, values, variables), numb)
                        i = i + new_f.length
                        hasNumber = false
                        number = ""
                    } else if (hasNumber && isImaginary) {
                        val numb: Double = number.toDouble()
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.pow(eval(new_f, values, variables), Complex(0.0, numb))
                        i = i + new_f.length
                        hasNumber = false
                        isImaginary = false
                        number = ""
                    } else if (hasFunction) {
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.pow(eval(function, values, variables), eval(new_f, values, variables))
                        i = i + new_f.length
                        hasFunction = false
                        function = ""
                    } else {
                        val new_f = nextFunction(f.substring(i + 1, f.length))
                        value = Complex.pow(value, eval(new_f, values, variables))
                        i = i + new_f.length
                    }
                    '0' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '1' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '2' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '3' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '4' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '5' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '6' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '7' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '8' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '9' -> {
                        hasNumber = true
                        number = number + character
                        if (i == f.length - 1) {
                            value = Complex(number.toDouble(), 0.0)
                            number = ""
                            hasNumber = false
                        }
                    }
                    '.' -> {
                        if (i == f.length - 1) {
                            throw ExpressionException("The function is not well-formed")
                        }
                        if (hasNumber && number.length > 0) {
                            number = number + character
                        }
                    }
                    '(' -> {
                        if (i == f.length - 1) {
                            throw ExpressionException("The function is not well-formed")
                        }
                        val new_f = f.substring(i + 1, nextBracket(f))
                        if (hasFunction) {
                            value = if (function == SIN) {
                                eval(new_f, values, variables).sin()
                            } else if (function == COS) {
                                eval(new_f, values, variables).cos()
                            } else if (function == TAN) {
                                eval(new_f, values, variables).tan()
                            } else if (function == SINH) {
                                eval(new_f, values, variables).sinh()
                            } else if (function == COSH) {
                                eval(new_f, values, variables).cosh()
                            } else if (function == TANH) {
                                eval(new_f, values, variables).tanh()
                            } else if (function == ASIN) {
                                eval(new_f, values, variables).asin()
                            } else if (function == ACOS) {
                                eval(new_f, values, variables).acos()
                            } else if (function == ATAN) {
                                eval(new_f, values, variables).atan()
                            } else if (function == LN) {
                                eval(new_f, values, variables).log()
                            } else if (function == LOG) {
                                eval(new_f, values, variables).log10()
                            } else if (function == SQRT) {
                                eval(new_f, values, variables).sqrt()
                            } else if (function == CBRT) {
                                Complex.cbrt(eval(new_f, values, variables))
                            } else {
                                throw ExpressionException("The function is not well-formed")
                            }
                            hasFunction = false
                            function = ""
                        } else {
                            value = eval(new_f, values, variables)
                        }
                        i = i + new_f.length + 1
                    }
                    ')' -> throw ExpressionException(" '(' is not finished ")
                    ' ' -> {
                    }
                    'i' -> if (!hasFunction) {
                        if (hasNumber) {
                            value = Complex(0.0, number.toDouble())
                            number = ""
                            isImaginary = true
                        } else {
                            value = Complex(0.0, 1.0)
                            isImaginary = true
                        }
                    } else {
                        function = function + character
                        hasFunction = true
                        if (i == f.length - 1) {
                            value = if (function == E) {
                                Complex(Math.E, 0.0)
                            } else if (function == PI) {
                                Complex(Math.PI, 0.0)
                            } else {
                                if (function.length == 1) {
                                    val n = variables.indexOf(function)
                                    if (n >= 0) {
                                        values[n]
                                    } else {
                                        throw ExpressionException("function is not well defined")
                                    }
                                } else {
                                    throw ExpressionException("function is not well defined")
                                }
                            }
                        }
                    }
                    'j' -> if (!hasFunction) {
                        if (hasNumber) {
                            value = Complex(0.0, number.toDouble())
                            isImaginary = true
                        } else {
                            value = Complex(0.0, 1.0)
                            isImaginary = true
                        }
                    } else {
                        function = function + character
                        hasFunction = true
                        if (i == f.length - 1) {
                            value = if (function == E) {
                                Complex(Math.E, 0.0)
                            } else if (function == PI) {
                                Complex(Math.PI, 0.0)
                            } else {
                                if (function.length == 1) {
                                    val n = variables.indexOf(function)
                                    if (n >= 0) {
                                        values[n]
                                    } else {
                                        throw ExpressionException("function is not well defined")
                                    }
                                } else {
                                    throw ExpressionException("function is not well defined")
                                }
                            }
                        }
                        return@loop
                    }
                    else -> if (isValidCharacter(character)) {
                        function = function + character
                        hasFunction = true
                        if (i == f.length - 1) {
                            value = if (function == E) {
                                Complex(Math.E, 0.0)
                            } else if (function == PI) {
                                Complex(Math.PI, 0.0)
                            } else {
                                if (function.length == 1) {
                                    val n = variables.indexOf(function)
                                    if (n >= 0) {
                                        values[n]
                                    } else {
                                        throw ExpressionException("function is not well defined")
                                    }
                                } else {
                                    throw ExpressionException("function is not well defined")
                                }
                            }
                        }
                    } else {
                        throw ExpressionException("Invalid character")
                    }
                }
                i++
            }
        }
        return value
    }

    /**
     * nextFunction.
     *
     * @param f the f
     * @return the string
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    private fun nextFunction(f: String): String {
        var f = f
        var result = ""
        f = f.trim { it <= ' ' }.toLowerCase()
        var i = 0
        while (i < f.length) {
            val character = f[i]
            when (character) {
                '*' -> i = f.length
                '/' -> i = f.length
                '+' -> i = f.length
                '-' -> i = f.length
                '^' -> result = result + character
                '.' -> result = result + character
                '(' -> {
                    val new_f = f.substring(i, nextBracket(f) + 1)
                    result = result + new_f
                    i = i + new_f.length - 1
                }
                ')' -> throw ExpressionException(" '(' is not finished ")
                ' ' -> result = result + character
                else -> result = if (isValidNumericAndCharacter(character)) {
                    result + character
                } else {
                    throw ExpressionException("Invalid character")
                }
            }
            i++
        }
        return result
    }

    /**
     * nextMinusFunction.
     *
     * @param f the f
     * @return the string
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    private fun nextMinusFunction(f: String): String {
        var f = f
        var result = ""
        f = f.trim { it <= ' ' }.toLowerCase()
        var i = 0
        while (i < f.length) {
            val character = f[i]
            when (character) {
                '*' -> result = result + character
                '/' -> result = result + character
                '+' -> i = f.length
                '-' -> i = f.length
                '^' -> result = result + character
                '.' -> result = result + character
                '(' -> {
                    val new_f = f.substring(i, nextBracket(f) + 1)
                    result = result + new_f
                    i = i + new_f.length - 1
                }
                ')' -> throw ExpressionException(" '(' is not finished ")
                ' ' -> result = result + character
                else -> result = if (isValidNumericAndCharacter(character)) {
                    result + character
                } else {
                    throw ExpressionException("Invalid character")
                }
            }
            i++
        }
        return result
    }

    /**
     * isValidCharacter.
     *
     * @param character the character
     * @return true, if is valid character
     */
    private fun isValidCharacter(character: Char): Boolean {
        var result = false
        result = when (character) {
            'a' -> true
            'b' -> true
            'c' -> true
            'd' -> true
            'e' -> true
            'f' -> true
            'g' -> true
            'h' -> true
            'k' -> true
            'l' -> true
            'm' -> true
            'n' -> true
            'o' -> true
            'p' -> true
            'q' -> true
            'r' -> true
            's' -> true
            't' -> true
            'u' -> true
            'v' -> true
            'w' -> true
            'x' -> true
            'y' -> true
            'z' -> true
            else -> false
        }
        return result
    }

    /**
     * isValidNumericAndCharacter.
     *
     * @param character the character
     * @return true, if is valid numeric and character
     */
    private fun isValidNumericAndCharacter(character: Char): Boolean {
        var result = false
        result = when (character) {
            'a' -> true
            'b' -> true
            'c' -> true
            'd' -> true
            'e' -> true
            'f' -> true
            'g' -> true
            'h' -> true
            'i' -> true
            'j' -> true
            'k' -> true
            'l' -> true
            'm' -> true
            'n' -> true
            'o' -> true
            'p' -> true
            'q' -> true
            'r' -> true
            's' -> true
            't' -> true
            'u' -> true
            'v' -> true
            'w' -> true
            'x' -> true
            'y' -> true
            'z' -> true
            '0' -> true
            '1' -> true
            '2' -> true
            '3' -> true
            '4' -> true
            '5' -> true
            '6' -> true
            '7' -> true
            '8' -> true
            '9' -> true
            else -> false
        }
        return result
    }

    /**
     * nextBracket.
     * @param f the f
     * @return the int
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    private fun nextBracket(f: String): Int {
        var result = 0
        var count = 0
        for (i in 0 until f.length) {
            val character = f[i]
            when (character) {
                '(' -> {
                    result = i
                    count++
                }
                ')' -> {
                    result = i
                    count--
                    if (count == 0) {
                        return i
                    }
                }
                else -> result = i
            }
        }
        if (count != 0) {
            throw ExpressionException("( is not finished")
        }
        return result
    }
}