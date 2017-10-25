package com.example.natel.project3logins;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.regex.Pattern;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


/**
 * Created by natel on 10/5/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String cypher = "ThisIsPassword00";

    //Define Database file and version
    private static final String DATABASE_NAME = "accountInfo.db";
    private static final int DATABASE_VERSION = 3;

    //Define table of users
    private static final String USER_TABLE = "Users";
    private static final String ID = "User_ID";
    private static final String EMAIL = "Email";
    private static final String NAME = "Name";
    private static final String ENCRYPTED_PASSWORD = "Encrypted_Password";
    private static final String LAST_LOGIN_DATE = "Last_Logged_in";
    private static final String CREATED_DATE = "Date_Created";
    private static final String ADDRESS = "Address";
    private static final String STATE = "State";


    //Define table of sessions
    private static final String SESSION_TABLE = "Sessions";
    private static final String SESSION_ID = "Session_ID";
    private static final String SESSION_USER_ID = "Session_User";
    private static final String SESSION_DATE_CREATED = "Session_Created";
    private static final String SESSION_DATE_EXPIRED = "Session_Expires";


    //Set a basic regex for email
    Pattern emailPattern = Pattern.compile(".+[.-z]+\\@[.-z]+\\.[cnoce][doero][umtg.][u]?[k]?");

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create a table for the users

        sqLiteDatabase.execSQL("CREATE TABLE " + USER_TABLE + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EMAIL + " TEXT NOT NULL, " +
                NAME + " TEXT NOT NULL, " +
                ENCRYPTED_PASSWORD + " TEXT NOT NULL, " +
                ADDRESS + " TEXT NOT NULL, " +
                STATE + " TEXT NOT NULL, " +
                LAST_LOGIN_DATE + " TIMESTAMP NOT NULL, " +
                CREATED_DATE + " TIMESTAMP NOT NULL);");

        sqLiteDatabase.execSQL("CREATE TABLE " + SESSION_TABLE + "(" +
                SESSION_ID + " TEXT PRIMARY KEY, " +
                SESSION_USER_ID + " INTEGER NOT NULL, " +
                SESSION_DATE_CREATED + " TIMESTAMP NOT NULL, " +
                SESSION_DATE_EXPIRED + " TIMESTAMP NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SESSION_TABLE);
        onCreate(sqLiteDatabase);
    }

    public int logIntoUser(String email, String password) {
        int errorCode = 0;

        /**
         * Guide to ERROR CODES
         * 0 = Good to go!
         * 1 = Email does not exist
         * 2 = Password does not match
         */

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor emails = db.rawQuery("SELECT " + EMAIL + ", " + ENCRYPTED_PASSWORD + "  FROM " + USER_TABLE + " WHERE " + EMAIL + " == '" + email + "';", null);

        if (emails.getCount() == 0) {
            errorCode = 1;
        } else {
            emails.moveToFirst();
            Log.i("PW", emails.getString(1));
            if (!BCrypt.checkpw(password, emails.getString(1))){
                errorCode = 2;
            }
        }
        return errorCode;
    }

    public String createSession(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor users = db.rawQuery("SELECT " + ID + " FROM " + USER_TABLE + " WHERE " + EMAIL + " == '" + email + "';", null);
        users.moveToFirst();
        int userID = users.getInt(0);
        SecureRandom ran = new SecureRandom();
        String secureSessionID = String.valueOf(ran.nextInt());
        Log.w("Secure Session ID", secureSessionID);
        try {
            db.execSQL("INSERT INTO " + SESSION_TABLE + "(" + SESSION_ID +
                    ", " + SESSION_USER_ID +
                    ", " + SESSION_DATE_CREATED +
                    ", " + SESSION_DATE_EXPIRED +
                    ") VALUES('" + secureSessionID +
                    "', " + String.valueOf(userID) +
                    ", " + String.valueOf(System.currentTimeMillis()) +
                    ", " + String.valueOf(System.currentTimeMillis() + 1000 * 60 * 5) + ");");
        } catch (Exception e) {
            Log.e("Error Creating Session", e.getMessage());
        }
        return secureSessionID;
    }

    public int part1CreateUser(String email, String textPassword){
        /**
         * GUIDE TO ERROR CODES!
         * 0 = Good to go!
         * 1 = Email failed regex
         * 2 = Email already exists
         * 3 = Password is not long enough(8 characters in length)
         */

        int errorCode = 0;
        EncryptDecrypt enc = new EncryptDecrypt();
        //check for valid email
        Log.i("tag",email);
        if (!emailPattern.matcher(enc.decrypt(email,cypher)).matches()) {
            Log.i("tag",(enc.decrypt(email,cypher)));
            errorCode = 1;
        }

        //Check if email exists
        else if (logIntoUser(email, "string") != 1) {
            errorCode = 2;
        } else if (textPassword.length() < 8) {
            errorCode = 3;
        }

        return errorCode;

    }



    boolean emailAlreadyExists(String email){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor emails = db.rawQuery("SELECT " + EMAIL + ", " + ENCRYPTED_PASSWORD + "  FROM " + USER_TABLE + " WHERE " + EMAIL + " == '" + email + "';", null);
        return(emails.getCount()>0);

    }
    //Create a new user

    public int createUser(String email, String name, String password, String address, String state) {
        /**
         * GUIDE TO ERROR CODES!
         * 0 = Good to go!
         * 1 = Email failed regex
         * 2 = Email already exists
         * 3 = Password is not long enough(8 characters in length)
         * 4 = Name is not long enough (First and Last combined should be at least 7 characters including space
         * 5 = Misc Database error; check logs for more details :(
         */

        EncryptDecrypt enc = new EncryptDecrypt();
        int errorCode = 0;

        //check for valid email
        if (!emailPattern.matcher(enc.decrypt(email,DatabaseHelper.cypher)).matches()) {
            errorCode = 1;
        }
        //Check if email exists
        else if (emailAlreadyExists(enc.decrypt(email,DatabaseHelper.cypher))) {
            errorCode = 2;
        } else if (password.length() < 8) {
            errorCode = 3;
        } else if (name.length() < 7) {
            errorCode = 4;
        }

        if (errorCode == 0) {

            SQLiteDatabase db = this.getWritableDatabase();

            try {
                db.execSQL("INSERT INTO " + USER_TABLE + "( " +
                        EMAIL + ", " +
                        NAME + ", " +
                        ENCRYPTED_PASSWORD + ", " +
                        ADDRESS + ", " +
                        STATE + ", " +
                        LAST_LOGIN_DATE + ", " +
                        CREATED_DATE +
                        ") VALUES('" +
                        email + "', '" + enc.encrypt(name,DatabaseHelper.cypher) + "', '" + password + "', '" + enc.encrypt(address, cypher) + "', '" + enc.encrypt(state,cypher) + "', "+ String.valueOf(System.currentTimeMillis()) + ", " + String.valueOf(System.currentTimeMillis()) + ");");
            } catch (Exception e) {
                errorCode = 5;
                Log.w("Database Error", e.getMessage());
            }
        }

        return errorCode;
    }
    public String getEmailFromSession(String session){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor info = db.rawQuery("SELECT " + SESSION_USER_ID + " , " + EMAIL + " , " + NAME + " , " + ENCRYPTED_PASSWORD + " , " + LAST_LOGIN_DATE + " , " + CREATED_DATE + " , " +
                SESSION_DATE_EXPIRED + " FROM " + USER_TABLE + ", " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + session + " AND " + SESSION_USER_ID + " == " +
                ID, null);
        info.moveToFirst();
        return info.getString(1);
    }
    public String getNameFromSession(String session){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor info = db.rawQuery("SELECT " + SESSION_USER_ID + " , " + EMAIL + " , " + NAME + " , " + ENCRYPTED_PASSWORD + " , " + LAST_LOGIN_DATE + " , " + CREATED_DATE + " , " +
                SESSION_DATE_EXPIRED + " FROM " + USER_TABLE + ", " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + session + " AND " + SESSION_USER_ID + " == " +
                ID, null);
        info.moveToFirst();
        return info.getString(2);
    }

    public void updateLastLoginTime(String session){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor idCur = db.rawQuery("SELECT " + SESSION_USER_ID + " FROM " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + session, null);
        idCur.moveToFirst();
        int id = idCur.getInt(0);
        db.execSQL("UPDATE " + USER_TABLE + " SET " + LAST_LOGIN_DATE + " = " + System.currentTimeMillis() + " WHERE " + ID + " == " + id);
    }
    public String getEncryptedPasswordFromSession(String session){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor info = db.rawQuery("SELECT " + SESSION_USER_ID + " , " + EMAIL + " , " + NAME + " , " + ENCRYPTED_PASSWORD + " , " + LAST_LOGIN_DATE + " , " + CREATED_DATE + " , " +
                SESSION_DATE_EXPIRED + " FROM " + USER_TABLE + ", " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + session + " AND " + SESSION_USER_ID + " == " +
                ID, null);
        info.moveToFirst();
        return info.getString(3);
    }
    public String getEncryptedLocationDetails(String session){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor info = db.rawQuery("SELECT " + SESSION_USER_ID + " , " + ADDRESS + ", " + STATE + " , " + NAME + " , " + ENCRYPTED_PASSWORD + " , " + LAST_LOGIN_DATE + " , " + CREATED_DATE + " , " +
                SESSION_DATE_EXPIRED + " FROM " + USER_TABLE + ", " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + session + " AND " + SESSION_USER_ID + " == " +
                ID, null);
        info.moveToFirst();
        return "Address: " + info.getString(1) + "\n\nState: " + info.getString(2);
    }
    public String getSessionExpirationFromSession(String session){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor info = db.rawQuery("SELECT " + SESSION_USER_ID + " , " + EMAIL + " , " + NAME + " , " + ENCRYPTED_PASSWORD + " , " + LAST_LOGIN_DATE + " , " + CREATED_DATE + " , " +
                SESSION_DATE_EXPIRED + " FROM " + USER_TABLE + ", " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + session + " AND " + SESSION_USER_ID + " == " +
                ID, null);
        info.moveToFirst();
        return info.getString(6);
    }
    public String getLastLogInDateFromSession(String session){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor info = db.rawQuery("SELECT " + SESSION_USER_ID + " , " + EMAIL + " , " + NAME + " , " + ENCRYPTED_PASSWORD + " , " + LAST_LOGIN_DATE + " , " + CREATED_DATE + " , " +
                SESSION_DATE_EXPIRED + " FROM " + USER_TABLE + ", " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + session + " AND " + SESSION_USER_ID + " == " +
                ID, null);
        info.moveToFirst();
        return info.getString(4);
    }

    public boolean sessionExists(String session){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor sessions = db.rawQuery("SELECT " + SESSION_ID + ", " + SESSION_DATE_EXPIRED + " FROM " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + session,null);

            sessions.moveToFirst();
            return ( sessions.getCount() > 0 && System.currentTimeMillis() < sessions.getLong(1));
    }


    public void deleteOldSessions(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor sessions = db.rawQuery("SELECT " + SESSION_ID + ", " + SESSION_DATE_EXPIRED + " FROM " + SESSION_TABLE,null);
        sessions.moveToFirst();
        for(int x = 0; x < sessions.getCount(); x++) {
            if (sessions.getInt(1) > System.currentTimeMillis()) {
                db.execSQL("DELETE FROM " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + sessions.getInt(0));
                if (x != sessions.getCount() - 1) {
                    sessions.moveToNext();
                }
            }
        }
    }
    public void deleteSession(String ses){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + SESSION_TABLE + " WHERE " + SESSION_ID + " == " + ses);
    }
}
