package com.oneliang.ktx.frame.test.function

import com.oneliang.ktx.frame.test.ParserManager
import com.oneliang.ktx.frame.expression.ExpressionException

/**
 * The Class FunctionX.
 */
class FunctionX(f_x: String) {
    /** setup.  */
    private var degree = false
    /**
     * getter f(x).
     *
     * @return the f x
     */
    /**
     * setter f(x).
     *
     * @param f_x the new f x
     */
    /** f(x).  */
    var f_x: String

    /**
     * get f(x0).
     *
     * @param xo point
     * @return the f xo
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    fun getF_xo(xo: Double): Double {
        return eval(f_x, xo)
    }

    /**
     * eval.
     *
     * @param f_x the f x
     * @param xi the xi
     * @return the double
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    private fun eval(f_x: String, xi: Double): Double {
        var f_x = f_x
        f_x = f_x.trim { it <= ' ' }.toLowerCase()
        var value = 0.0
        var number = ""
        var function = ""
        var hasNumber = false
        var hasFunction = false
        var i = 0
        while (i < f_x.length) {
            val character = f_x[i]
            when (character) {
                '*' -> if (hasNumber) {
                    val numb: Double = number.toDouble()
                    val new_f_x = nextFunction(f_x.substring(i + 1, f_x.length))
                    value = numb * eval(new_f_x, xi)
                    i = i + new_f_x.length
                    hasNumber = false
                    number = ""
                } else if (hasFunction) {
                    val new_f_x = nextFunction(f_x.substring(i + 1, f_x.length))
                    value = eval(function, xi) * eval(new_f_x, xi)
                    i = i + new_f_x.length
                    hasFunction = false
                    function = ""
                } else {
                    val new_f_x = nextFunction(f_x.substring(i + 1, f_x.length))
                    value = value * eval(new_f_x, xi)
                    i = i + new_f_x.length
                }
                '+' -> if (hasNumber) {
                    val numb: Double = number.toDouble()
                    val new_f_x = f_x.substring(i + 1, f_x.length)
                    value = numb + eval(new_f_x, xi)
                    i = i + new_f_x.length
                    hasNumber = false
                    number = ""
                } else if (hasFunction) {
                    val new_f_x = f_x.substring(i + 1, f_x.length)
                    value = eval(function, xi) + eval(new_f_x, xi)
                    i = i + new_f_x.length
                    hasFunction = false
                    function = ""
                } else {
                    val new_f_x = f_x.substring(i + 1, f_x.length)
                    value = value + eval(new_f_x, xi)
                    i = i + new_f_x.length
                }
                '-' -> if (hasNumber) {
                    val numb: Double = number.toDouble()
                    val new_f_x = nextMinusFunction(f_x.substring(i + 1, f_x.length))
                    value = numb - eval(new_f_x, xi)
                    i = i + new_f_x.length
                    hasNumber = false
                    number = ""
                } else if (hasFunction) {
                    val new_f_x = nextMinusFunction(f_x.substring(i + 1, f_x.length))
                    value = eval(function, xi) - eval(new_f_x, xi)
                    i = i + new_f_x.length
                    hasFunction = false
                    function = ""
                } else {
                    val new_f_x = nextMinusFunction(f_x.substring(i + 1, f_x.length))
                    value = value - eval(new_f_x, xi)
                    i = i + new_f_x.length
                }
                '/' -> if (hasNumber) {
                    val numb: Double = number.toDouble()
                    val new_f_x = nextFunction(f_x.substring(i + 1, f_x.length))
                    value = numb / eval(new_f_x, xi)
                    i = i + new_f_x.length
                    hasNumber = false
                    number = ""
                } else if (hasFunction) {
                    val new_f_x = nextFunction(f_x.substring(i + 1, f_x.length))
                    value = eval(function, xi) / eval(new_f_x, xi)
                    i = i + new_f_x.length
                    hasFunction = false
                    function = ""
                } else {
                    val new_f_x = nextFunction(f_x.substring(i + 1, f_x.length))
                    value = value / eval(new_f_x, xi)
                    i = i + new_f_x.length
                }
                '^' -> if (hasNumber) {
                    val numb: Double = number.toDouble()
                    val new_f_x = nextFunction(f_x.substring(i + 1, f_x.length))
                    value = Math.pow(numb, eval(new_f_x, xi))
                    i = i + new_f_x.length
                    hasNumber = false
                    number = ""
                } else if (hasFunction) {
                    val new_f_x = nextFunction(f_x.substring(i + 1, f_x.length))
                    value = Math.pow(eval(function, xi), eval(new_f_x, xi))
                    i = i + new_f_x.length
                    hasFunction = false
                    function = ""
                } else {
                    val new_f_x = nextFunction(f_x.substring(i + 1, f_x.length))
                    value = Math.pow(value, eval(new_f_x, xi))
                    i = i + new_f_x.length
                }
                '0' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '1' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '2' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '3' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '4' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '5' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '6' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '7' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '8' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '9' -> {
                    hasNumber = true
                    number = number + character
                    if (i == f_x.length - 1) {
                        value = number.toDouble()
                        number = ""
                        hasNumber = false
                    }
                }
                '.' -> {
                    if (i == f_x.length - 1) {
                        throw ExpressionException("The function is not well-formed")
                    }
                    if (hasNumber && number.length > 0) {
                        number = number + character
                    }
                }
                '(' -> {
                    if (i == f_x.length - 1) {
                        throw ExpressionException("The function is not well-formed")
                    }
                    val new_f_x = f_x.substring(i + 1, nextBracket(f_x))
                    if (hasFunction) {
                        value = if (function == SIN) {
                            if (degree) {
                                Math.sin(Math.toRadians(eval(new_f_x, xi)))
                            } else {
                                Math.sin(eval(new_f_x, xi))
                            }
                        } else if (function == COS) {
                            if (degree) {
                                Math.cos(Math.toRadians(eval(new_f_x, xi)))
                            } else {
                                Math.cos(eval(new_f_x, xi))
                            }
                        } else if (function == TAN) {
                            if (degree) {
                                Math.tan(Math.toRadians(eval(new_f_x, xi)))
                            } else {
                                Math.tan(eval(new_f_x, xi))
                            }
                        } else if (function == SINH) {
                            Math.sinh(eval(new_f_x, xi))
                        } else if (function == COSH) {
                            Math.cosh(eval(new_f_x, xi))
                        } else if (function == TANH) {
                            Math.tanh(eval(new_f_x, xi))
                        } else if (function == ASIN) {
                            if (degree) {
                                Math.asin(eval(new_f_x, xi)) * (180 / Math.PI)
                            } else {
                                Math.asin(eval(new_f_x, xi))
                            }
                        } else if (function == ACOS) {
                            if (degree) {
                                Math.acos(eval(new_f_x, xi)) * (180 / Math.PI)
                            } else {
                                Math.acos(eval(new_f_x, xi))
                            }
                        } else if (function == ATAN) {
                            if (degree) {
                                Math.atan(eval(new_f_x, xi)) * (180 / Math.PI)
                            } else {
                                Math.atan(eval(new_f_x, xi))
                            }
                        } else if (function == LN) {
                            Math.log(eval(new_f_x, xi))
                        } else if (function == LOG) {
                            Math.log10(eval(new_f_x, xi))
                        } else if (function == SQRT) {
                            Math.sqrt(eval(new_f_x, xi))
                        } else if (function == CBRT) {
                            Math.cbrt(eval(new_f_x, xi))
                        } else {
                            throw ExpressionException("The function is not well-formed")
                        }
                        hasFunction = false
                        function = ""
                    } else {
                        value = eval(new_f_x, xi)
                    }
                    i = i + new_f_x.length + 1
                }
                ')' -> throw ExpressionException(" '(' is not finished ")
                ' ' -> {
                }
                else -> if (isValidCharacter(character)) {
                    function = function + character
                    hasFunction = true
                    if (i == f_x.length - 1) {
                        value = if (function == E) {
                            Math.E
                        } else if (function == PI) {
                            Math.PI
                        } else {
                            if (function.length == 1) {
                                xi
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
        return value
    }

    /**
     * Next function.
     *
     * @param f_x the f x
     * @return the string
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    private fun nextFunction(f_x: String): String {
        var f_x = f_x
        var result = ""
        f_x = f_x.trim { it <= ' ' }.toLowerCase()
        var i = 0
        while (i < f_x.length) {
            val character = f_x[i]
            when (character) {
                '*' -> i = f_x.length
                '/' -> i = f_x.length
                '+' -> i = f_x.length
                '-' -> i = f_x.length
                '^' -> result = result + character
                '.' -> result = result + character
                '(' -> {
                    val new_f_x = f_x.substring(i, nextBracket(f_x) + 1)
                    result = result + new_f_x
                    i = i + new_f_x.length - 1
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
     * Next minus function.
     *
     * @param f_x the f x
     * @return the string
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    private fun nextMinusFunction(f_x: String): String {
        var f_x = f_x
        var result = ""
        f_x = f_x.trim { it <= ' ' }.toLowerCase()
        var i = 0
        while (i < f_x.length) {
            val character = f_x[i]
            when (character) {
                '*' -> result = result + character
                '/' -> result = result + character
                '+' -> i = f_x.length
                '-' -> i = f_x.length
                '^' -> result = result + character
                '.' -> result = result + character
                '(' -> {
                    val new_f_x = f_x.substring(i, nextBracket(f_x) + 1)
                    result = result + new_f_x
                    i = i + new_f_x.length - 1
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
     *
     * @param f_x f(x)
     * @return the int
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    private fun nextBracket(f_x: String): Int {
        var result = 0
        var count = 0
        for (i in 0 until f_x.length) {
            val character = f_x[i]
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

    /**
     * FunctionX.
     *
     * @param f_x f(x)
     */
    init {
        this.f_x = f_x.trim { it <= ' ' }.replace(" ".toRegex(), "")
        degree = ParserManager.instance?.isDeegre ?: false
    }
}