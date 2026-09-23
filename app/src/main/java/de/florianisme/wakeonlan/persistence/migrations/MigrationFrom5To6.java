package de.florianisme.wakeonlan.persistence.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class MigrationFrom5To6 extends Migration {

    public MigrationFrom5To6() {
        super(5, 6);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE 'Devices' ADD COLUMN 'wake_via_ssh' INTEGER DEFAULT 0 NOT NULL"); // Booleans are stored as Integer
        database.execSQL("ALTER TABLE 'Devices' ADD COLUMN 'relay_ssh_address' TEXT");
        database.execSQL("ALTER TABLE 'Devices' ADD COLUMN 'relay_ssh_port' INTEGER");
        database.execSQL("ALTER TABLE 'Devices' ADD COLUMN 'relay_ssh_user' TEXT");
        database.execSQL("ALTER TABLE 'Devices' ADD COLUMN 'relay_ssh_password' TEXT");
        database.execSQL("ALTER TABLE 'Devices' ADD COLUMN 'relay_ssh_command' TEXT");
    }

}
