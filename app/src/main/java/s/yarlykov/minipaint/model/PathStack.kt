/**
 * Ref: https://github.com/gazolla/Kotlin-Algorithm/tree/master/Stack
 */

package s.yarlykov.minipaint.model

import android.graphics.Path

class PathStack : Iterable<Path> {

    private val items = mutableListOf<Path>()

    override fun iterator(): Iterator<Path> {
        return items.iterator()
    }

    fun isEmpty(): Boolean = this.items.isEmpty()

    fun count(): Int = this.items.count()

    fun push(element : Path) {
        val position = this.count()
        this.items.add(position, element)
    }

    fun pop(): Path? {
        if (this.isEmpty()) {
            return null
        } else {
            val item = this.items.count() - 1
            return this.items.removeAt(item)
        }
    }

    fun peek(): Path? {
        if (isEmpty()) {
            return null
        } else {
            return this.items[this.items.count() - 1]
        }
    }

    fun clear() {
        items.clear()
    }

    override fun toString() = this.items.toString()
}