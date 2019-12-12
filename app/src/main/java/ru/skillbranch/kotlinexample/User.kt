package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.regex.Pattern

class User private constructor(
    private val firstName: String,
    private val lastName: String?=null,
    private val email: String?=null,
    rawPhone: String?=null,
    private val meta: MutableMap<String,Any>?=null
) {
    var userInfo: String
    private val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").trim().capitalize()
    private val initials: String
        get() = listOfNotNull(firstName, lastName)?.map {
            if (it?.isNullOrBlank()) "" else
            it?.first()?.toUpperCase()
        }?.joinToString(" ")?.trim()
    private var phone: String? = null
        set(value) {
            field = value?.replace("[^+\\d]".toRegex(), "")
        }
    private var _login: String? = null
    public var login: String
        set(value) {
            _login = value?.toLowerCase()
        }
        get() = _login!!

    private var _salt: String ?= ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()

    public var salt: String
    /*by lazy {
        ByteArray(16).also { SecureRandom().nextBytes(it) }.toString() } .also { _salt=salt }*/
        set(value){
            _salt=value?.toString()
        }
    get()=_salt!!

    private lateinit var passwordHash: String
    fun setPasswordHash(value: String){passwordHash=value}
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public lateinit var accessCode: String

    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) : this(firstName, lastName, email = email, meta = mutableMapOf("auth" to "password")) {
        println("Secondary email constructor")
        passwordHash = encrypt(password)
    }


    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mutableMapOf("auth" to "sms")) {
        println("Secondory phone constructor")
        val code: String = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUse(rawPhone, code)
    }


    init {
        println("First init block,primary constructor was called");
        check(!firstName.isBlank()) { "First Name must be is not blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) { "Phone or Email must be is not blank" }

        phone = rawPhone
        login = email ?: phone!!
        println("First init block, primary constructor was called")
        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    private fun updateUserInfo()
    {
        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    //fun checkPassword(pass: String) = encrypt(pass) == passwordHash
    private val emailRegex = Pattern.compile(
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

    fun markCsvMeta(){
        meta?.put("src","csv")
        meta?.remove("auth")
        updateUserInfo()
    }

    fun checkPassword(pass: String):Boolean
        {
            //this.login.isEmail()
            //email.isEmail()

            if (pass.isNullOrBlank()) return false
            else
            return when (meta?.get("auth")) {
            "sms" -> accessCode == pass
            "password"-> encrypt(pass) == passwordHash
            else -> false
        }
        }

    fun changePassword(oldPass: String, pass: String){
        if (checkPassword(oldPass)) passwordHash=encrypt(pass)
        else throw IllegalArgumentException("The entered password does not match to entered password")
    }

    public  fun renewAccessCode(): String {
        accessCode=generateAccessCode();
        return userInfo
    }

    private  fun generateAccessCode(): String {
        var possible = "ABCDEFGHIJKLMNOPQRSTUQXYZabcdefghijklmnopqrstuqxyz0123456789"
        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        } .toString()
    }

    private fun encrypt(password: String):String=salt.plus(password).md5()

    private fun sendAccessCodeToUse(phone: String?,code:String){
        println("Send aceess code $code to phone $phone")
    }

    private fun String.md5():String{
        val md: MessageDigest= MessageDigest.getInstance("MD5")
        val digest: ByteArray=md.digest(toByteArray());
        val hexString: String= BigInteger(1,digest).toString(16)
        return hexString.padStart(32,'0')
    }

    companion object Factory{
        fun  makeUser(
            fullName: String,
            email: String?=null,
            password: String?=null,
            phone: String?=null
            //John Doe ;JohnDoe@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;
        ): User{
            val (firstName:String,lastName:String?)=fullName.fullNameToPair()
            //if (lastName?.isBlank()) lastName=firstName
            return when {
                !phone.isNullOrBlank()->User(firstName,lastName,phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName=firstName,lastName=lastName,email=email,password=password)
                else -> throw java.lang.IllegalArgumentException(" Email or phone must be not null or blank.")
            }
        }

        private fun String.fullNameToPair(): Pair<String,String?>{
        return this.split(" ")
            .filter { it.isNotBlank() }
            .run{
                when (size) {
                    1->first() to null
                    2->first() to last()
                    else-> throw IllegalArgumentException("FullName must contain only FirstName and LastName, current split result ${this@fullNameToPair}")
                }
            }
        }


    }

}