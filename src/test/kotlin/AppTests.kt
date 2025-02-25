import io.kotest.core.spec.style.FunSpec

class AppTests: FunSpec({
    test("test") {
        val app = App()
        app.main()
    }
})