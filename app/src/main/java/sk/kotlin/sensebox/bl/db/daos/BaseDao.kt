package sk.kotlin.sensebox.bl.db.daos

import android.arch.persistence.room.*
import sk.kotlin.sensebox.bl.db.entities.BaseEntity

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
abstract class BaseDao<T : BaseEntity> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(data: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(data: List<T>): Array<Long>

    @Update
    abstract fun update(data: T): Int

    @Update
    abstract fun update(data: List<T>): Int

    @Transaction
    open fun insertOrUpdate(data: T) {
        if (insert(data) == -1L) {
            update(data)
        }
    }

    @Transaction
    open fun insertOrUpdate(data: List<T>) {
        val insertResult = insert(data)
        val updateList = ArrayList<T>()

        insertResult.forEachIndexed { index, result ->
            if (result == -1L) {
                updateList.add(data[index])
            }
        }

        if (updateList.isNotEmpty()) {
            update(updateList)
        }
    }

    @Transaction
    open fun insertAndUpdate(inserts: List<T>, updates: List<T>) {
        insert(inserts)
        update(updates)
    }

    @Delete
    abstract fun delete(data: T): Int
}