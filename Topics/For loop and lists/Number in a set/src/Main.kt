fun main() {
    val size = readln().toInt()
    val mutList: MutableList<Int> = mutableListOf()
    repeat(size) {
        mutList.add(readln().toInt())
    }
    val num = readln().toInt()

    println(if (num in mutList) "YES" else "NO")
}
