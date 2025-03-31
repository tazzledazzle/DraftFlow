package com.northshore.services

import com.northshore.dto.ExcelTokenRequestDto
import com.northshore.dto.LoginRequestDto
import com.northshore.dto.LoginResponseDto
import org.springframework.stereotype.Service

interface AuthService {

    fun authenticate(loginRequest: LoginRequestDto): LoginResponseDto
    fun generateExcelToken(tokenRequest: ExcelTokenRequestDto): String
    fun validateExcelToken(token: String): Boolean

    fun refreshToken(token: String): LoginResponseDto
}

@Service
class AuthServiceImpl() : AuthService {

    override fun authenticate(loginRequest: LoginRequestDto): LoginResponseDto {
        return LoginResponseDto("token", "username", "role", 0)
    }

    override fun generateExcelToken(tokenRequest: ExcelTokenRequestDto): String {
        TODO("Not yet implemented")
    }

    override fun validateExcelToken(token: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun refreshToken(token: String): LoginResponseDto {
        TODO("Not yet implemented")
    }
}