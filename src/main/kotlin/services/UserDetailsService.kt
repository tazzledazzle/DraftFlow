package com.northshore.services

import com.northshore.models.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl(
    val id: Long,
    private val username: String,
    val email: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    constructor(user: User) : this(
        id = user.id ?: throw IllegalArgumentException("User ID cannot be null"),
        username = "${user.firstName}:${user.lastName}" ?: throw IllegalArgumentException("User First Name cannot be null"),
        email = user.email,
        password = user.password,
        authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role}"))
    )

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserDetailsImpl) return false

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}