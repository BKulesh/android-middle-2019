package ru.skillbranch.kotlinexample

import android.service.autofill.RegexValidator
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

    if (!login?.trim()?.isEmail()) alogin=login?.replace("[^+\\d]".toRegex(), "")
        else alogin=login

    return map[alogin.trim()]?.run {
            if (checkPassword(password)) this.userInfo
        else null
        }
    }

    fun requestAccessCode(rawPhone: String):String?{
        val phone=rawPhone?.replace("[^+\\d]".toRegex(),"")
        return map[phone]?.run {renewAccessCode()}

        }

}