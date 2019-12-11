package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?=null,
    email: String?=null,
    rawPhone: String?=null,
    private val meta: Map<String,Any>?=null
) {
    val userInfo: String
    private val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").capitalize()
    private val initials: String
        get() = listOfNotNull(firstName, lastName)?.map {
            it?.first()?.toUpperCase()
        }?.joinToString(" ")
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

    private val salt: String by lazy {
        ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
    }
    private lateinit var passwordHash: String
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public lateinit var accessCode: String

    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) : this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("Secondary email constructor")
        passwordHash = encrypt(password)
    }


    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
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

    //fun checkPassword(pass: String) = encrypt(pass) == passwordHash

    fun checkPassword(pass: String):Boolean
        {
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
        ): User{
            val (firstName:String,lastName:String?)=fullName.fullNameToPair()

            return when {
                !phone.isNullOrBlank()->User(firstName,lastName,phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName=firstName,lastName=lastName,email=email,password=password)
                else -> throw java.lang.IllegalArgumentException(" Email or phone must be not null or blank.")
            }
        }

        private fun String.fullNameToPair(): Pair<String,String>{
        return this.split(" ")
            .filter { it.isNotBlank() }
            .run{
                when (size) {
                    1->first() to ""
                    2->first() to last()
                    else-> throw IllegalArgumentException("FullName must contain only FirstName and LastName, current split result ${this@fullNameToPair}")
                }
            }
        }


    }

}