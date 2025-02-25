package com.tazzledazzle.draftflow

//import org.springframework.boot.autoconfigure.SpringBootApplication
import com.apose.cad.Image
//import org.springframework.boot.runApplication
import java.io.File

//@SpringBootApplication
class DraftFlowApplication

fun main(args: Array<String>) {
    val dwgFile = "src/test/resources/2023-800 Columbia (6105)-XREF-Base Details.dwg"
    val outputFile = "src/test/resources/dxf/2023-800 Columbia (6105)-XREF-Base Details.dxf"

    val cadImage = Image.load(dwgFile)
}
