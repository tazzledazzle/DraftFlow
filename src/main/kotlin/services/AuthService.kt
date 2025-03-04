package com.northshore.services

import com.northshore.dto.ExcelTokenRequestDto
import com.northshore.dto.LoginRequestDto
import com.northshore.dto.LoginResponseDto

interface AuthService {

    fun authenticate(loginRequest: LoginRequestDto): LoginResponseDto
    fun generateExcelToken(tokenRequest: ExcelTokenRequestDto): String
    fun validateExcelToken(token: String): Boolean

    fun refreshToken(token: String): LoginResponseDto
}