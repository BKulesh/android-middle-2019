package ru.skillbranch.kotlinexample

import android.service.autofill.RegexValidator
import org.jetbrains.annotations.NotNull
import java.util.regex.Pattern.compile

object UserHolder {

    private val map = mutableMapOf<String,User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User{
        return User.makeUser(fullName,email=email,password=password).also {
            it->map[it.login]=it
        }
    }

    fun registerUserByPhone(
        fullName: String,
        phone: String
    ): User{
        return User.makeUser(fullName,phone=phone).also {
                it->map[it.login]=it
        }
    }


    private val emailRegex = compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    fun String.isEmail() : Boolean {
        return emailRegex.matcher(this).matches()
    }

    fun loginUser(login: String,password: String): String?{

        val alogin: String?

    if (!login?.trim()?.isEmail()) alogin=login?.replace("[^+\\d]".toRegex(), "")?.trim()
        else alogin=login?.trim()?.toLowerCase()
    val u=map[alogin]
    return map[alogin.trim()]?.run {
            if (checkPassword(password)) this.userInfo
        else null
        }
    }

    fun requestAccessCode(rawPhone: String):String?{
        val phone=rawPhone?.replace("[^+\\d]".toRegex(),"")
        return map[phone]?.run {renewAccessCode()}

        }

    fun importUsers(list: List<String?>): List<User>
    {
        var user: User?
        var fullName: String=""
        var email: String?=null
        var phone: String?=null
        val password: String?="test"
        var passwordHash: String?=null
        var securityStr: String?=null
        var salt: String?=null

        var userList= emptyList<User>()
        for (j in list.indices) {
            fullName=""
            email=null
            phone=null
            salt=null
            val(fullName,email,securityStr,phone)=list[j]!!.split(";");
            val (salt,passwordHash)=securityStr.split(":")
            //if (!email.isNullOrBlank())
                userList+=User.makeUser(fullName,email=email,password=password,phone=phone).also { user->user.salt=salt
                                                                                                                user.setPasswordHash(passwordHash)
                                                                                                                user.markCsvMeta()
                                                                                                                map[user.login]=user
                                                                                                                //println(map.size.toString())
                                                                                                    }
        }
        //for (u in map)
        //{
        //    println("imp key="+u.key.toString())
        //    //println("imp value="+u.value.userInfo)
        //}
        //val u=map["JohnDoe1@unknow.com".toLowerCase()]
        //println("imp key="+u?.login)
        //println("imp value="+u?.userInfo)

        return userList
    }



}