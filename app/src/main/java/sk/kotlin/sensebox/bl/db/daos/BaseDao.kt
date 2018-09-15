package sk.kotlin.sensebox.bl.db.daos

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Update
import sk.kotlin.sensebox.bl.db.entities.BaseEntity

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
interface BaseDao<T : BaseEntity> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBulk(vararg data: T): Array<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSingle(data: T): Long

    @Update
    fun update(data: T): Int

    @Delete
    fun delete(data: T): Int
}