import io.kotest.core.annotation.AutoScan
import io.kotest.core.spec.style.FunSpec

@AutoScan
class AppTests: FunSpec({
    test("test") {
        val app = App()
        app.main()
    }
})