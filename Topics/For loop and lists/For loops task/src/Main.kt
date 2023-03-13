fun main() {
    val size = readln().toInt()
    val mutList: MutableList<Int> = mutableListOf()
    repeat(size) {
        mutList.add(readln().toInt())
    }
    val (p, m) = readln().split(' ').map { it.toInt() }

    println(if (p in mutList && m in mutList) "YES" else "NO")
}
