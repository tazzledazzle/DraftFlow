package com.tazzledazzle.draftflow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class DraftFlowApplication

fun main(args: Array<String>) {
//    runApplication<DraftFlowApplication>(*args)
    val converterFileName = "ODAFileConverter_QT6_lnxX64_8.3dll_25.12.AppImage"
    val dwgFile = "src/test/resources/2023-800 Columbia (6105)-XREF-Base Details.dwg"
    File("src/main/resources/").walk().forEach {
        ProcessBuilder("$it/$converterFileName", dwgFile).start().waitFor()
    }
}
