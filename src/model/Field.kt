package model

enum class FieldEvents { OPENING, SELECTION, DESELECTION, EXPLOSION, RESET };

public data class Field(val line: Int, val column: Int) {
    private val neighbors = ArrayList<Field>();
    private val callbacks = ArrayList<(Field, FieldEvents) -> Unit>();

    var marked: Boolean = false;
    var open: Boolean = false;
    var mined: Boolean = false;

    //Read Only
    val dismarked: Boolean get() = !marked;
    val closed: Boolean get() = !open;
    val secure: Boolean get() = !mined;
    val goalAchieved: Boolean get() = secure && open || mined && marked;
    val qntMinedNeighbors: Int get() = neighbors.filter { it.mined }.size;
    val secureNeighborhood: Boolean
        get() = neighbors
            .map { it.secure }
            .reduce { result, secure -> result && secure };

    public fun addNeighbor(neighbor: Field) {
        neighbors.add(neighbor);
    }

    public fun onEvent(callback: (Field, FieldEvents) -> Unit) {
        callbacks.add(callback);
    }

    public fun open() {
        if (closed) {
            open = true;
            if (mined) {
                callbacks.forEach { it(this, FieldEvents.EXPLOSION) };
            } else {
                callbacks.forEach { it(this, FieldEvents.OPENING) };
                neighbors.filter { it.closed && it.secure && secureNeighborhood }.forEach { it.open() };
            }
        }
    }

    public fun changeMark() {
        if (closed) {
            marked = !marked;
            val event = if (marked) FieldEvents.SELECTION else FieldEvents.DESELECTION;
            callbacks.forEach { it(this, event) };
        }
    }

    public fun mine() {
        mined = true;
    }

    public fun reset() {
        open = false;
        mined = false;
        marked = false;
        callbacks.forEach { it(this, FieldEvents.RESET) };
    }

}