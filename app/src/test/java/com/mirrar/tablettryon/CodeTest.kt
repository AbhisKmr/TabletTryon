package com.mirrar.tablettest

import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

fun main() {

    fun plusMinus(arr: Array<Int>): Unit {
        var zero = 0
        var pos = 0
        var nag = 0
        val len = arr.size.toDouble()

        arr.forEach { i ->
            if (i < 0) {
                nag += 1
            } else if (i > 0) {
                pos += 1
            } else {
                zero += 1
            }
        }

        println("%.6f".format(pos / len))
        println("%.6f".format(nag / len))
        println("%.6f".format(zero / len))
    }

    fun staircase(n: Int): Unit {
        // Write your code here
        for (i in 1 until n) {
            var s = ""
            val whiteSpace = n - i
            for (w in 0 until whiteSpace) {
                s += " "
            }

            for (w in 0 until i) {
                s += "#"
            }

            println(s)
        }
        var s = ""
        for (w in 0 until n) {
            s += "#"
        }
        println(s)
    }

    fun staircase(n: Int, b: Boolean) {
        for (i in 1..n) {
            val spaces = " ".repeat(n - i)
            val hashes = "#".repeat(i)
            println(spaces + hashes)
        }
    }

    fun miniMaxSum(arr: Array<Int>): Unit {
        val ar = arr.sorted()

        val min = ar.take(4).sumOf { it.toLong() }
        val max = ar.takeLast(4).sumOf { it.toLong() }

        println("${min} ${max}")


    }

    fun timeConversion(s: String): String {
        // Write your code here
        val inputFormat = SimpleDateFormat("hh:mm:ssa", Locale.ENGLISH) // Including seconds
        val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH) // 24-hour format

        val date = inputFormat.parse(s) // Convert string to Date
        return outputFormat.format(date)

    }

    fun extraLongFactorials(n: Int): Unit {
        var l = 1.toBigInteger()

        for (i in 1 until n + 1) {
            l *= i.toBigInteger()
        }
        println(l.toString())
    }

    val n = 4
    var pre = 0
    var lst = 1

//    1, 2, 3, 4, 5, 6, 7, 8
//    1, 2, 3, 5, 8, 13, 21, 34

    for (i in 1..n) {

        val temp = pre
        val new = i + pre

        pre = new - temp
        val res = pre + i
        lst = pre + i
    }

    fun star(num: Int) {
        if (num < 5) return print("Invalid number")

        for (i in 1..num) {
            for (j in 1..num) {
                if (i == 1 || j == 1 || i == num || j == num || i == j || i + j == num + 1) {
                    print("*")
                } else {
                    print(" ")
                }
            }
            println("")
        }
    }

    fun isPalindrome(s: String): Boolean {
        var fi = 0
        var li = s.length - 1

        while (fi < li) {

            if (s[fi] != s[li]) {
                return false
            }

            fi++
            li--
        }


        return true
    }

    fun sumFirstAndLastDigit(number: Int): Int {
        if (number < 0) {
            return -1
        } else if (number < 10) {
            return number + number
        }

        val ss = number.toString()

        val fn = ss[0]
        val ln = ss[ss.length - 1]

        return (fn - '0') + (ln - '0')
    }

    fun getEvenDigitSum(number: Int): Int {
        return if (number < 0) {
            -1
        } else {

            val numString = number.toString()

            var sum = 0
            repeat(numString.length) {
                val nn = numString[it] - '0'
                if (nn % 2 == 0) {
                    sum += nn
                }
            }
            sum
        }
    }

    fun isFibonacci(num: Int): Boolean {
        if (num == 0 || num == 1) return true
        var a = 0
        var b = 1
        var fib = a + b
        while (fib < num) {
            a = b
            b = fib
            fib = a + b
        }
        return fib == num
    }

    fun printFibonacciMatrix(n: Int) {
        // Traverse each index of the matrix
        for (i in 0 until n) {
            for (j in 0 until n) {
                // Calculate the index position in the matrix (linear index)
                val index = i * n + j
                // Check if the index is a Fibonacci number
                if (isFibonacci(index)) {
                    print("*")
                } else {
                    print(" ")
                }
            }
            println() // New line after each row
        }
    }


    fun hasSharedDigit(first: Int, second: Int): Boolean {
        if (first < 10 || first > 99 || second < 10 || second > 99) {
            return false
        }

        val fl = first.toString()
        val sl = second.toString()

        fl.forEach {
            if (sl.contains(it, false)) {
                return true
            }
        }
        return false
    }

    fun canPack(bigCan: Int, smallCan: Int, goal: Int): Boolean {
        if (bigCan < 0 || smallCan < 0 || goal < 0) {
            return false
        }

        val minBigCan = min(goal / 5, bigCan)
        val remaining = goal - (minBigCan * 5)

        return smallCan >= remaining
    }

    fun drawStar(num: Int) {
        for (i in 1..num) {
            for (j in 1..num) {
//                if (i == 1 || j == 1 || i == num || j == num || i == j || i + j == num + 1)

                if (i == 1 || i == num || j == 1 || j == num || i == j || i + j == num + 1) {
                    print(" * ")
                } else {
                    print("   ")
                }
            }
            println()
        }
    }


    fun starAgain(num: Int) {
        for (i in 0 until num) {
            for (j in 0 until num) {
                if (i == 0 || j == 0 || i == num - 1 || j == num - 1|| j == i || i + j == num - 1) {
                    print("*")
                } else {
                    print(" ")
                }
            }
            println()
        }
    }

    val number = 41
//    println(star(7))
    println(starAgain(7))

    /*
    for (i in 1..number) {
        for (j in 1..number) {
            val f = i == 1 // first raw
            val s = i == number // last raw
            val ff = j == number // last column
            val t = j == 1 // first column


            val fff = i == j
            val ss =  i + j == number + 1
            if (i == 1 || i == number || j == 1 || j == number || i == j || i + j == number + 1) {
                print("*")
            } else {
                print(" ")
            }
        }
        println()
    }

     */
}