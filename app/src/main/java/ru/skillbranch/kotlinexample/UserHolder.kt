package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
        return User.makeUser(fullName, email = email, password = password)
            .also { user ->
                if (map.containsKey(user.login)) throw IllegalArgumentException("A user with this email already exists")
                else map[user.login] = user
            }
    }

    fun registerUserByPhone(
        fullName: String,
        phone: String
    ): User {
        return User.makeUser(fullName, phone = phone)
            .also { user ->
                when {
                    (map.containsKey(user.login)) -> throw IllegalArgumentException("A user with this phone already exists")
                    user.login.length != 12 -> throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
                    else -> map[user.login] = user
                }
            }
    }

    fun loginUser(login: String, password: String): String? {
        val handledLogin = handleLogin(login)
        return map[handledLogin]?.let {
            if (it.checkPassword(password)) it.userInfo
            else null
        }
    }

    fun requestAccessCode(phone: String) {
        map[phone.replace("[^+\\d]".toRegex(), "")]?.requestAccessCode()
    }

    fun importUsers(list: List<String>) : List<User> {
        val users = mutableListOf<User>()

        list.forEach {
            val values = it.split(";")

            if (values.size < 4) return@forEach
            if (values[0].isEmpty() || values[2].isEmpty()) return@forEach
            if (values[1].isEmpty() && values[3].isEmpty()) return@forEach
            if (values[2].split(":").size < 2) return@forEach

            val names = values[0].split(" ")
            val firstName = names[0]
            val lastName = if (names.size > 1) names[1] else null
            val email = if (values[1].isEmpty()) null else values[1]
            val rawPhone = if (values[3].isEmpty()) null else values[3]
            val passwordHash = values[2].split(":")[1]

            val user = User(firstName, lastName, email, rawPhone, passwordHash)

            users.add(user)
        }

        return users
    }

    private fun handleLogin(login: String): String {
        return if (login.contains('@')) login.trim()
        else login.replace("[^+\\d]".toRegex(), "")
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }
}