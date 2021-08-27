package com.railprosfs.railsapp.data;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.railprosfs.railsapp.data_layout.AnswerDao;
import com.railprosfs.railsapp.data_layout.AnswerTbl;
import com.railprosfs.railsapp.data_layout.AssignmentDao;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DocumentDao;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrDao;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.FieldPlacementDao;
import com.railprosfs.railsapp.data_layout.FieldPlacementTbl;
import com.railprosfs.railsapp.data_layout.JobDao;
import com.railprosfs.railsapp.data_layout.JobSetupDao;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.data_layout.RailRoadDao;
import com.railprosfs.railsapp.data_layout.RailRoadTbl;
import com.railprosfs.railsapp.data_layout.WorkflowDao;
import com.railprosfs.railsapp.data_layout.WorkflowTbl;

/**
 * The Schedule database is the primary holder of the RWIC's work schedule, within
 * a set of time boxed limits.  The Assignments are really job summaries with an
 * extra bit of data specifying when an RWIC is gong to work the job.
 * <p>
 * Additionally, to reduce the need for duplicate data, the database is also used
 * to hold general information about a job.  Specifically, the DWRs and Docs linked
 * to a job are also stored in this area.  This allows the app to take the schedule
 * data and shape it to the more general job data.
 * <p>
 * For example, to display a specific job, the data in this table is trimmed and
 * combined to show a job (which has summary data, DWRs and Documents).
 * <p>
 * Finally, a Supervisor view is really a list of jobs related to a property independent
 * of schedule.  For that we can just finesse the scheduling information and show job data.
 */
@Database(entities = {AssignmentTbl.class, DwrTbl.class, DocumentTbl.class, FieldPlacementTbl.class,
        AnswerTbl.class, WorkflowTbl.class, JobSetupTbl.class, JobTbl.class, RailRoadTbl.class},
        version = 22,
        exportSchema = false)
public abstract class ScheduleDB extends RoomDatabase {
    public abstract AssignmentDao assignmentDao();

    public abstract DwrDao dwrDao();

    public abstract DocumentDao documentDao();

    public abstract FieldPlacementDao fieldPlacementDao();

    public abstract AnswerDao answerDao();

    public abstract WorkflowDao workflowDao();

    public abstract JobSetupDao jobSetupDao();

    public abstract JobDao jobDao();

    public abstract RailRoadDao railRoadDao();

    private static ScheduleDB INSTANCE;
    private static final String DB_NAME = "assignment.db";


    // See https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
    public static ScheduleDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ScheduleDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), ScheduleDB.class, DB_NAME)
                            .addMigrations(MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                                    MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                                    MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16,
                                    MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19,
                                    MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN NotPresentOnTrack INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN NotPresentOnTrackKey INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `RailRoadTbl` (`id` INTEGER NOT NULL DEFAULT 0, "
                    + "`railroadId` INTEGER NOT NULL DEFAULT 0, "
                    + "`companyName` TEXT, "
                    + "`code` TEXT, "
                    + "`divisionName` TEXT, "
                    + "`divisionId` INTEGER NOT NULL DEFAULT 0, "
                    + "`subdivisionName` TEXT, "
                    + "`subdivisionId` INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY(`id`))");
        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN FieldContactName TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN FieldContactPhone TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN FieldContactEmail TEXT NOT NULL DEFAULT ''");
        }
    };

    // WMATA Migration
    private static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputWMLine TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputWMLineKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputWMStation TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputWMStationKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputWMStationName TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputWMStationNameKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputWMTrack INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputWMTrackKey INTEGER NOT NULL DEFAULT 0");
        }
    };

    // CSX Migration
    private static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN RwicPhone TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN RwicPhoneKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXShiftNew INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXShiftNewKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXShiftRelief INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXShiftReliefKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXShiftRelieved TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXShiftRelievedKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN WorkLunchTime TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN WorkLunchTimeKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXPeopleRow INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXPeopleRowKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXEquipmentRow INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN CSXEquipmentRowKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN DescWeatherHigh INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN DescWeatherHighKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN DescWeatherLow INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN DescWeatherLowKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN WorkBriefTime TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN WorkBriefTimeKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN RoadMasterPhone TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN RoadMasterPhoneKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN DescWorkPlanned TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN DescWorkPlannedKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN DescSafety TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN DescSafetyKey INTEGER NOT NULL DEFAULT 0");

        }
    };

    // WorkFlowTbl Status Flag
    private static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE WorkflowTbl ADD COLUMN Uploading INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN EquipmentDescription TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN DistanceFromTracks TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN PermitNumber TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN Notes TEXT DEFAULT ''");
        }
    };

    private static final Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN TrackSupervisor TEXT DEFAULT ''");
        }
    };

    private static final Migration MIGRATION_15_16 = new Migration(15, 16) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN TypeOfVehicle TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN TypeOfVehicleKey INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN VersionInformation TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN VersionInformationKey INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_16_17 = new Migration(16, 17) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN PerformedTraining INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE JobSetupTbl ADD COLUMN StatusMessage TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN StatusMessage TEXT DEFAULT ''");
        }
    };

    private static final Migration MIGRATION_18_19 = new Migration(18, 19) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE AnswerTbl ADD COLUMN ServerId INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN Restrictions TEXT DEFAULT ''");
            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN ServiceType INTEGER NOT NULL DEFAULT 0");
        }
    };

    /**
     * Every update to Room seems to require a different migration strategy.  For this one it
     * decided that every new field had to support a null value.  That required removal of any
     * Not Null and Default SQL, and required the data type to be Integer (not int).
     */
    static final Migration MIGRATION_19_20 = new Migration(19, 20) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN ConstructionDay TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN ConstructionDayKey INTEGER");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputTotalWorkDays TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputTotalWorkDaysKey INTEGER");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputDescWeatherWind TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputDescWeatherWindKey INTEGER");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputDescWeatherRain TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN InputDescWeatherRainKey INTEGER");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN SpecialCostCenter TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN SpecialCostCenterKey INTEGER");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN RailroadContact TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN RailroadContactKey INTEGER");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN WorkingTrack TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN WorkingTrackKey INTEGER");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN RailSignatureName TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN RailSignatureDate TEXT");

            database.execSQL("ALTER TABLE AssignmentTbl ADD COLUMN CostCenters TEXT");
        }
    };

    private static final Migration MIGRATION_20_21 = new Migration(20, 21) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN District TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN DistrictKey INTEGER");
        }
    };

    /**
     * - Need a new DWR Form displayed for a new service type, this new DWR Form is going to contain 3 new fields additional to (older fields - more details to come from the client)
     * - Ability to submit the new form.
     * - Keep the offline caching active for this.
     * - In the mobile app, add the new fields specified in (4) to the DWR layout, and show them only for the new railroad and service type.
     * - New Fields: 82T number, street name and mile posts for the street.
     * - Hide "standard" and "meals only" options for the new railroad and service type.
     * - This feature should function like any of the forms currently being processed by the mobile application.
     */
    static final Migration MIGRATION_21_22 = new Migration(21, 22) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN HasRoadwayFlagging INTEGER NOT NULL DEFAULT 0");

            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN EightyTwoT TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN EightyTwoTKey INTEGER");

            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN StreetName TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN StreetNameKey INTEGER");

            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN MilePostsForStreet TEXT");
            database.execSQL("ALTER TABLE DwrTbl ADD COLUMN MilePostsForStreetKey INTEGER");
        }
    };
}
