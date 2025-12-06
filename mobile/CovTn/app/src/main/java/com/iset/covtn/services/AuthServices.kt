package com.iset.covtn.services

import android.util.Log
import com.google.gson.Gson
import com.iset.covtn.models.AuthObj
import com.iset.covtn.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

class AuthServices  {


    companion object{
        @Volatile
        private var instance : AuthServices? = null

        val baseUrl = "http://192.168.100.101:9092/api"

        fun getAuthService() : AuthServices{
            return instance ?: synchronized(this){
                instance = AuthServices()
                return instance!!
            }
        }
    }


    val gson = Gson()
    fun login(email: String, pass: String): AuthObj? {
        val url = URI.create("$baseUrl/auth/login").toURL()
        val conn = url.openConnection() as HttpURLConnection
        var authobj: AuthObj? = null

        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")

            // JSON body
            val body = """{"username": "$email", "password": "$pass"}"""

            conn.outputStream.use { os ->
                os.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            Log.i("HTTP_CODE", responseCode.toString())

            if (responseCode in 200..299) {
                val str = conn.inputStream.bufferedReader().use { it.readText() }
                authobj = gson.fromJson(str, AuthObj::class.java)

            }
            conn.disconnect()
            return authobj


        } catch (e: Exception) {
            Log.e("LOGIN_ERROR", e.toString())
            return authobj

        } finally {
            conn.disconnect()

        }
    }

     fun signin(user: User): AuthObj {
        val url = URI.create("$baseUrl/auth/register").toURL()
        val conn = url.openConnection() as HttpURLConnection
        var authobj = AuthObj("server error","","500",null)

        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")

            // JSON body

            conn.outputStream.use { os ->
                os.write(gson.toJson(user).toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            Log.i("HTTP_CODE", responseCode.toString())

            if (responseCode in 200..299) {
                val str = conn.inputStream.bufferedReader().use { it.readText() }
                authobj = gson.fromJson(str, AuthObj::class.java)

            } else {
                authobj =
                    AuthObj(conn.errorStream.bufferedReader().use { it.readText() }, "", responseCode.toString(), "")
            }
            conn.disconnect()
            return authobj


        } catch (e: Exception) {
            Log.e("LOGIN_ERROR", e.toString())
            return authobj

        }
    }

     fun verifyMail(obj: AuthObj) : Boolean {
        val url = URI.create("$baseUrl/auth/validateMail").toURL()
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")

            Log.i("TAG", "verifyMail: "+gson.toJson(obj))

            conn.outputStream.use { os ->
                    os.write(gson.toJson(obj).toByteArray(Charsets.UTF_8))
            }
            val httpcode = conn.responseCode
            if (httpcode in 200..299) {
                val str = conn.inputStream.bufferedReader().use { it.readText() }
                obj.token = str
                conn.disconnect()
                return true
            }
            else{
                val str = conn.errorStream.bufferedReader().use { it.readText() }
                obj.token = str;
                conn.disconnect()
                return false
            }
        }catch (e : Exception){
            Log.e("LOGIN_ERROR", e.toString())
            return false
        }

    }
     suspend fun authentify(token : String) : User? {
         var user: User? = null
         return withContext(Dispatchers.IO) {
             val url = URI.create("$baseUrl/auth/me").toURL()
             val conn = url.openConnection() as HttpURLConnection
             try {
                 conn.requestMethod = "GET"
                 conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                 conn.setRequestProperty("Accept", "application/json")
                 conn.setRequestProperty("Authorization", "Bearer ${token}")


                 val httpcode = conn.responseCode
                 if (httpcode in 200..299) {
                     val str = conn.inputStream.bufferedReader().use { it.readText() }
                     user = gson.fromJson(str, User::class.java)
                 } else {
                     val str = conn.errorStream.bufferedReader().use { it.readText() }
                 }
             } catch (e: Exception) {
                 Log.e("LOGIN_ERROR", e.toString())
             }
             conn.disconnect()
             user
         }

     }


}