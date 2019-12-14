package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T>{
    if (isEmpty())return emptyList()
    var j:Int=-1
    for (i in 0..size-1){
        if (predicate(get(size-1-i))) {j=size-1-i
                                        break}
    }
    //println("j="+j.toString())
    if (j<=0) return emptyList()
    else return take(j)
}
