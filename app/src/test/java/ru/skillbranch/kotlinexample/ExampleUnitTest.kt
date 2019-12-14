package ru.skillbranch.kotlinexample

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import ru.skillbranch.kotlinexample.extensions.dropLastUntil

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun register_user(){
        val holder: UserHolder=UserHolder
        holder.registerUser("John Doe","John_Doe@unknown.com","testPass")
        val expectedInfo="""
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        val failResult:String?=holder.loginUser("John_Doe@unknown.com","testPass")
        val successResult:String?=holder.loginUser("john_doe@unknown.com","testPass")

        //Assert.assertEquals(null,failResult)
        Assert.assertEquals(expectedInfo,successResult)

    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_blank() {
        val holder = UserHolder
        holder.registerUser("", "John_Doe@unknown.com","testPass")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_name() {
        val holder = UserHolder
        holder.registerUser("John Jr Doe", "John_Doe@unknown.com","testPass")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_fail_illegal_exist() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")
        holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")
    }

    @Test
    fun register_user_by_phone_success() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971 11-11")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        Assert.assertEquals(expectedInfo, user.userInfo)
        Assert.assertNotNull(user.accessCode)
        Assert.assertEquals(6, user.accessCode?.length)
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_blank() {
        val holder = UserHolder
        holder.registerUserByPhone("", "+7 (917) 971 11-11")
    }

    @Test(expected = IllegalArgumentException::class)
    fun register_user_by_phone_fail_illegal_name() {
        val holder = UserHolder
        holder.registerUserByPhone("John Jr Doe", "+7 (XXX) XX XX-XX")
    }


    @Test(expected = IllegalArgumentException::class)
    fun register_user_failby_phone_illegal_exist() {
        val holder = UserHolder
        holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
    }

    @Test
    fun login_user_success() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")
        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: john_doe@unknown.com
            fullName: John Doe
            initials: J D
            email: John_Doe@unknown.com
            phone: null
            meta: {auth=password}
        """.trimIndent()

        val successResult =  holder.loginUser("john_doe@unknown.com", "testPass")


        Assert.assertEquals(expectedInfo, successResult)
    }


    @Test
    fun login_user_by_phone_success() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John", "+7 (917) 971-11-11")
        val expectedInfo = """
            firstName: John
            lastName: null
            login: +79179711111
            fullName: John
            initials: J
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult =  holder.loginUser("+7 (917) 971-11-11", user.accessCode!!)
        println(successResult)
        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun login_user_not_found() {
        val holder = UserHolder
        holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")

        val failResult =  holder.loginUser("john_cena@unknown.com", "test")

        Assert.assertNull(failResult)
    }


    @Test
    fun request_access_code() {
        val holder = UserHolder
        val user = holder.registerUserByPhone("John Doe", "+7 (917) 971-11-11")
        val oldAccess = user.accessCode
        holder.requestAccessCode("+7 (917) 971-11-11")

        val expectedInfo = """
            firstName: John
            lastName: Doe
            login: +79179711111
            fullName: John Doe
            initials: J D
            email: null
            phone: +79179711111
            meta: {auth=sms}
        """.trimIndent()

        val successResult =  holder.loginUser("+7 (917) 971-11-11", user.accessCode!!)

        Assert.assertNotEquals(oldAccess, user.accessCode!!)
        Assert.assertEquals(expectedInfo, successResult)
    }

    @Test
    fun testi_mportUsers(){
        val holder = UserHolder
        val csv=listOf(" John Doe ;JohnDoe@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;",
            " John1 Doe1 ;JohnDoe1@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;")
        val userList=holder.importUsers(csv)
        //for (i in userList.indices){
        //    println(userList[i].userInfo)
        //    println(userList[i].salt)
        //}
        //UserHolder.loginUser("JohnDoe1@unknow.com","[B@7591083d")
        //holder.
        //JohnDoe1@unknow.com

    }

    @Test
    fun test_lambda(){
        val holder=UserHolder
        //val user = holder.registerUser("John Doe", "John_Doe@unknown.com","testPass")
        holder.dlu {it.userInfo.indexOf("Doe")>0}

    }

    @Test
    fun test_dropall(){
        val holder = UserHolder
        val csv=listOf(" John Doe ;JohnDoe@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;",
            " John1 Doe1 ;JohnDoe1@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;",
            " John1 Doe2 ;JohnDoe2@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;",
            " John1 Doe3 ;JohnDoe3@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;",
            " John1 Doe4 ;JohnDoe4@unknow.com;[B@7591083d:c6adb4becdc64e92857e1e2a0fd6af84;;")
        val userList=holder.importUsers(csv)
        val newUserList=userList.dropLastUntil { it.login== "JohnDoe2@unknow.com".toLowerCase()}
        for (i in newUserList.indices){
            println("i["+i.toString()+"]="+newUserList[i].login)
        }

    }

}
