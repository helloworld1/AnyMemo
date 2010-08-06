#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include "sqlite3.h"

#define TAG "native_db_helper.c"

static char fulldbpath[255];
void get_full_dbpath(JNIEnv* env, jobject obj){
    jfieldID dbname_fid;
    jfieldID dbpath_fid;
    jclass cls;
    jstring dbpath_jstr;
    jstring dbname_jstr;
    char* dbpath;
    char* dbname;
    cls = (*env) -> GetObjectClass(env, obj);
    dbname_fid = (*env) -> GetFieldID(env, cls, "dbName", "Ljava/lang/String;");
    dbpath_fid = (*env) -> GetFieldID(env, cls, "dbPath", "Ljava/lang/String;");
    dbname_jstr = (*env) -> GetObjectField(env, obj, dbname_fid);
    dbpath_jstr = (*env) -> GetObjectField(env, obj, dbpath_fid);
    dbname = (char*)(*env) -> GetStringUTFChars(env, dbname_jstr, JNI_FALSE);
    dbpath = (char*)(*env) -> GetStringUTFChars(env, dbpath_jstr, JNI_FALSE);
    
    bzero(fulldbpath, 255);
    strcat(fulldbpath, dbpath);
    strcat(fulldbpath, "/");
    strcat(fulldbpath, dbname);
    (*env) -> ReleaseStringUTFChars(env, dbname_jstr, dbname);
    (*env) -> ReleaseStringUTFChars(env, dbpath_jstr, dbpath);
}

int getCount(sqlite3* database){
    int count;
    sqlite3_stmt* stmt;
    /* count the number of _id */
    if(sqlite3_prepare_v2(database, "SELECT COUNT(_id) FROM dict_tbl", -1, &stmt, NULL) != SQLITE_OK){
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Error when count total number of records");
        __android_log_write(ANDROID_LOG_ERROR, TAG, sqlite3_errmsg(database));
        return -1;
    }
    sqlite3_step(stmt);
    count = sqlite3_column_int(stmt, 0);
    sqlite3_finalize(stmt);
    return count;
}

void Java_org_liberty_android_fantastischmemo_DatabaseHelper_removeDuplicatesNative(JNIEnv* env, jobject obj)
{
    int i;
    sqlite3* database;
    char* errMsg = NULL;
    char buf[250];

    get_full_dbpath(env, obj);

    __android_log_write(ANDROID_LOG_INFO, TAG, fulldbpath);

    /* Open databases */
    if(sqlite3_open(fulldbpath, &database)){
        __android_log_write(ANDROID_LOG_ERROR, TAG, sqlite3_errmsg(database));
        sqlite3_close(database);
        return;
    }
    __android_log_write(ANDROID_LOG_INFO, TAG, "Success open here");

    /* Remove duplicates */
    if(sqlite3_exec(database, "DELETE FROM dict_tbl WHERE _id NOT IN (SELECT MIN(_id) FROM dict_tbl GROUP BY question)", NULL, NULL, &errMsg) != SQLITE_OK){
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Error when deleting duplicates");
        __android_log_write(ANDROID_LOG_ERROR, TAG, errMsg);
        return;
    }
    if(sqlite3_exec(database, "DELETE FROM learn_tbl WHERE _id NOT IN (SELECT _id FROM dict_tbl)", NULL, NULL, &errMsg) != SQLITE_OK){
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Error when deleting duplicates");
        __android_log_write(ANDROID_LOG_ERROR, TAG, errMsg);
        return;
    }

    /* Maintain ID coherence */

    if(sqlite3_exec(database, 
        "CREATE TABLE IF NOT EXISTS tmp_count \
        (id INTEGER PRIMARY KEY AUTOINCREMENT, _id INTEGER); \
        INSERT INTO tmp_count(_id) SELECT _id FROM dict_tbl; \
        UPDATE dict_tbl SET _id = (SELECT tmp_count.id FROM tmp_count WHERE tmp_count._id = dict_tbl._id); \
        UPDATE learn_tbl SET _id = (SELECT tmp_count.id FROM tmp_count WHERE tmp_count._id = learn_tbl._id); \
        DROP TABLE IF EXISTS tmp_count;"
        , NULL, NULL, &errMsg) != SQLITE_OK){
        __android_log_write(ANDROID_LOG_ERROR, TAG, "Error when maintaining the coherence of _id");
        __android_log_write(ANDROID_LOG_ERROR, TAG, errMsg);
        return;
    }
    


    

    /* Clean up */
    sqlite3_close(database);
}


