apply(from = "repositories.gradle.kts")

include(":core")

// Load library
File(rootDir, "lib").eachDir { include("lib:${it.name}") }
File(rootDir, "lib-multisrc").eachDir { include("lib-multisrc:${it.name}") }

// Daftarkan ekstensi Alqanime secara langsung
include(":src:id:alqanime")

fun File.eachDir(block: (File) -> Unit) {
    val files = listFiles() ?: return
    for (file in files) {
        if (file.isDirectory && file.name != ".gradle" && file.name != "build") {
            block(file)
        }
    }
}
