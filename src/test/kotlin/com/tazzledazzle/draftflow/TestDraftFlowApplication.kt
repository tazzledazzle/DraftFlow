package com.tazzledazzle.draftflow

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<DraftFlowApplication>().with(TestcontainersConfiguration::class).run(*args)
}
