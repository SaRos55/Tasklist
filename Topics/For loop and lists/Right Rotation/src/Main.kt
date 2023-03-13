fun main() {
    val numElements = readln().toInt()
    val list = MutableList(numElements) { readln().toInt() }
    val numPosition = readln().toInt()
    val shift = numPosition % numElements
    val newList = MutableList(numElements) {0}
    for (i in list.indices) {
        newList[i] = if (i - shift < 0) list[numElements - shift + i] else list[i - shift]
    }
    newList.forEach {
        print("$it ")
    }
}