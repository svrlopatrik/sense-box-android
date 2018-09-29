package sk.kotlin.sensebox.bl.db.entities

import android.arch.persistence.room.*

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@Entity(tableName = Record.TABLE_NAME,
        indices = [Index(Record.COLUMN_FK_FILE)],
        foreignKeys = [ForeignKey(entity = File::class,
                parentColumns = [BaseEntity.COLUMN_ID],
                childColumns = [Record.COLUMN_FK_FILE],
                onDelete = ForeignKey.CASCADE)])
data class Record(
        @ColumnInfo(name = Record.COLUMN_FK_FILE)
        var fkFile: String,
        @ColumnInfo(name = Record.COLUMN_TEMPERATURE)
        var temperature: Float,
        @ColumnInfo(name = Record.COLUMN_HUMIDITY)
        var humidity: Float,
        @ColumnInfo(name = Record.COLUMN_SECOND)
        var second: Int,
        @ColumnInfo(name = Record.COLUMN_MINUTE)
        var minute: Int,
        @ColumnInfo(name = Record.COLUMN_HOUR)
        var hour: Int
) : BaseEntity() {

    companion object {
        @Ignore
        const val TABLE_NAME = "record"

        @Ignore
        const val COLUMN_FK_FILE = "fk_file"
        @Ignore
        const val COLUMN_TEMPERATURE = "temperature"
        @Ignore
        const val COLUMN_HUMIDITY = "humidity"
        @Ignore
        const val COLUMN_SECOND = "second"
        @Ignore
        const val COLUMN_MINUTE = "minute"
        @Ignore
        const val COLUMN_HOUR = "hour"
    }
}