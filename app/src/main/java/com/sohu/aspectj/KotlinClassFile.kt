package com.sohu.aspectj

/**
 * Created by allenzhang on 2019/4/19.
 */

//@AspectLog(checkCode = 3,checkString = ["a","b","c"])
class KotlinClassFile {
    var name:String
    var first: Boolean

    public constructor(name: String, first: Boolean) {
        this.name = name
        this.first = first
    }

    @AspectLog(checkCode = 3,checkString = ["a","b","c"])
    public fun test(name: String,first: Boolean):Boolean{
        return false
    }
}