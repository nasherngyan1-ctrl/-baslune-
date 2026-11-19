package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserMemberEntity::class,
        ClientEntity::class,
        ClientCommunicationEntity::class,
        ProjectEntity::class,
        ProjectMilestoneEntity::class,
        TaskEntity::class,
        ServiceEntity::class,
        OpportunityEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        PaymentEntity::class,
        TransactionEntity::class,
        CampaignEntity::class,
        ContentItemEntity::class,
        DocumentEntity::class,
        KnowledgeArticleEntity::class,
        ActivityLogEntity::class,
        NotificationReadEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(BasluneTypeConverters::class)
abstract class BasluneDatabase : RoomDatabase() {
    abstract fun basluneDao(): BasluneDao

    companion object {
        @Volatile
        private var INSTANCE: BasluneDatabase? = null

        fun getInstance(context: Context): BasluneDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BasluneDatabase::class.java,
                    "baslune_os.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
