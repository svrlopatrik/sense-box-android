package sk.kotlin.sensebox.bl.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
abstract class BaseEntity(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = BaseEntity.COLUMN_ID)
        var id: String = ""
) {
    companion object {
        @Ignore
        const val COLUMN_ID = "id"
    }
}