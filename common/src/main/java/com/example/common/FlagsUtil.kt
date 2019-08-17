package com.example.common

object FlagsUtil {
    fun isFlagSet(value: Int, flag: Int):Boolean =
        (value.and(flag)) == flag

    fun setFlag(value: Int, flag: Int) =
            value.or(flag)
}