package com.railprosfs.railsapp.utility;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Base64;
import androidx.exifinterface.media.ExifInterface;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.railprosfs.railsapp.service.WebServiceModels.*;
import static com.railprosfs.railsapp.utility.Constants.*;

public class Functions {

    /**
     *  The Assignments need to be built from data in a number of places across the
     *  application. No one is going to keep them all consistent, so we need to have
     *  this one place where we map the data.
     *
     *  The dates come from the API (mostly) in the form of a date with time of midnight
     *  and set to zulu(Z) offset.  The intention is to be the specific day in local time.
     *  We therefore convert those into the day + time + local offset.
     *
     *  **NOTE: When creating an Assignment that does not have a shift on the backend,
     *  the shift.id should equal zero (0).
     */
    public static AssignmentTbl BuildAssignment(String user,
                                         int propertyKey,
                                         JobDetailResponse job,
                                         List<JobCostCenter> costCenters,
                                         AssignmentWS workItem,
                                         AssignmentShiftWS shift) {
        try {

            AssignmentTbl assignment = new AssignmentTbl();
            assignment.AssignmentId = 0;   // Assigned by database.
            assignment.TimeLine = 0;       // Not important to new record.
            assignment.UserId = user;

            assignment.ShiftId = shift.id;  // Should be zero (0) when no shift supplied by backend.
            String holdShiftDate = DefaultForNull(shift.day, KTime.ParseNow(KTime.KT_fmtDateOnlyRPFS).toString());
            holdShiftDate = holdShiftDate.replace(MIDNIGHT, DefaultForNull(shift.startTime, MIDMORNING));
            assignment.ShiftDate = KTime.ParseToFormat(holdShiftDate, KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
            assignment.StartDate = KTime.ParseToFormat(DefaultForNull(workItem.start,  KTime.ParseNow(KTime.KT_fmtDateOnlyRPFS).toString()), KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
            assignment.EndDate =KTime.ParseToFormat(DefaultForNull(workItem.end, KTime.ParseNow(KTime.KT_fmtDateOnlyRPFS).toString()), KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
            assignment.ShiftNotes = DefaultForNull(shift.notes);
            assignment.ServiceType = shift.serviceType != null ? shift.serviceType.id : RP_FLAGGING_SERVICE;
            assignment.JobSetup = shift.needsJobSetupForm;
            // Requirements are for multiple contacts, but will leaves these in as default.
            assignment.FieldContactName = DefaultForNull(workItem.fieldContactName);
            assignment.FieldContactPhone = DefaultForNull(workItem.fieldContactPhone);
            assignment.FieldContactEmail = DefaultForNull(workItem.fieldContactEmail);
            assignment.Restrictions = "";
            if(job.isNonBillable) {
                assignment.Restrictions = JOB_RESTRIC_BILLABLE;
            }
            // Contacts are a little complicated, since we want to pack them in there.
            if(job.fieldContacts != null && job.fieldContacts.size() > 0) {
                StringBuilder contactNames = new StringBuilder();
                StringBuilder contactPhones = new StringBuilder();
                StringBuilder contactEmails = new StringBuilder();

                for (ContactWS contact:job.fieldContacts) {
                    contactNames.append(DefaultForNull(contact.firstName.trim())).append(" ").append(DefaultForNull(contact.lastName.trim())).append(DELIMIT_CONTACTS);
                    if(contact.phoneNumbers != null && contact.phoneNumbers.size() > 0) {
                        contactPhones.append(DefaultForNull(contact.phoneNumbers.get(0).areaCode)).append(DefaultForNull(contact.phoneNumbers.get(0).number)).append(DELIMIT_CONTACTS);
                    } else {
                        contactPhones.append(DELIMIT_CONTACTS_PH).append(DELIMIT_CONTACTS);
                    }
                    if(contact.emailAddresses != null && contact.emailAddresses.size() > 0) {
                        contactEmails.append(DefaultForNull(contact.emailAddresses.get(0).email)).append(DELIMIT_CONTACTS);
                    } else {
                        contactEmails.append(DELIMIT_CONTACTS_PH).append(DELIMIT_CONTACTS);
                    }
                }
                assignment.FieldContactName = contactNames.toString();
                assignment.FieldContactPhone = contactPhones.toString();
                assignment.FieldContactEmail = contactEmails.toString();
            }
            assignment.JobId = job.id;
            assignment.RailroadId = propertyKey;
            assignment.JobStartDate = KTime.ParseToFormat(DefaultForNull(job.start,  KTime.ParseNow(KTime.KT_fmtDateOnlyRPFS).toString()), KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
            assignment.JobEndDate = KTime.ParseToFormat(DefaultForNull(job.end, KTime.ParseNow(KTime.KT_fmtDateOnlyRPFS).toString()), KTime.KT_fmtDateOnlyRPFS, TimeZone.getDefault().getID(), KTime.KT_fmtDate3339k, TimeZone.getDefault().getID()).toString();
            assignment.JobDescription = DefaultForNull(job.description);
            assignment.CustomerName = DefaultForNull(job.customer.name);
            // The customer phone & email are not currently used.
            assignment.CustomerPhone = "";
            assignment.CustomerEmail = "";
            assignment.LocationName = DefaultForNull(job.location);
            if(job.latitude == 0 && job.longitude == 0) {
                assignment.LocationLink = "";   // When no location information is entered on backend, the lon/lat are both zero.
            } else {
                assignment.LocationLink = String.format(MAP_WITH_COORDINATES, job.latitude, job.longitude,
                        Uri.encode(String.format(MAP_NAME_COORDINATES, DefaultForNull(job.number)))); // geo:0,0?q=<latitude>,<longitude>(Job #)
            }
            assignment.Subdivision = job.subdivision != null ? DefaultForNull(job.subdivision.name) : "";
            assignment.MilePostStart = DefaultForNull(job.startingMilePost);
            assignment.SupervisorRP = job.isSupervisorJob ? SUPERVISOR_JOB : "";
            assignment.JobNumber = DefaultForNull(job.number);
            assignment.Notes = DefaultForNull(job.notes);
            assignment.EquipmentDescription = DefaultForNull(job.equipmentDescription);
            assignment.DistanceFromTracks = DefaultForNull(job.distanceFromTracks);
            assignment.PermitNumber = DefaultForNull(job.permitNumber);
            assignment.TrackSupervisor = DefaultForNull(job.trackSupervisor);
            assignment.CostCenters = CompactCostCenters(costCenters);
            return assignment;

        } catch (Exception ex) {
            ExpClass.LogEX(ex, user);
            return new AssignmentTbl();
        }
    }

    /**
     * Cost centers are packed into a string.
     */
    public static String CompactCostCenters(List<JobCostCenter> costCenters){
        if(costCenters != null && costCenters.size() > 0){
            StringBuilder allCostCenters = new StringBuilder();
            for (JobCostCenter costCenter:costCenters) {
                String clCode = costCenter.code.replace(DELIMIT_COSTCENTER_ITEM, " ").replace(DELIMIT_COSTCENTER, " ").trim();
                String clDesc = costCenter.description.replace(DELIMIT_COSTCENTER_ITEM, " ").replace(DELIMIT_COSTCENTER, " ").trim();
                String clPost = costCenter.milePostRange.replace(DELIMIT_COSTCENTER_ITEM, " ").replace(DELIMIT_COSTCENTER, " ").trim();
                String holdCost = String.format(Locale.US, COSTCENTER_LINE, costCenter.id, clCode, clDesc, clPost);
                allCostCenters.append(holdCost);
            }
            return allCostCenters.toString();
        } else {
            return "";
        }
    }

    /**
     * Returns a list of compacted cost centers.
     */
    public static List<String> RawCostCenters(String raw){
        return Arrays.asList(raw.split(DELIMIT_COSTCENTER_REG));
    }

    /**
     * Returns a display list of cost centers from the compacted version stored in the Assignment.
     * This list will always have +1 items more than the raw data, since the choice of "None" is
     * added to the list.
     */
    public static List<String> DisplayCostCenters(Context ctx, String raw){
        List<String> costcenters = new ArrayList<String>();
        costcenters.add(ctx.getResources().getString(R.string.msg_no_cost_center));
        try {
            if (raw == null || raw.length() == 0) return costcenters;
            String[] rows = raw.split(DELIMIT_COSTCENTER_REG);
            for (String item : rows) {
                if (item.length() > 0) {
                    String[] values = item.split(DELIMIT_COSTCENTER_ITEM);
                    String clDbid = values[0];
                    String clCode = values[1];
                    String clDesc = values[2];
                    String clPost = values[3];
                    costcenters.add(String.format(Locale.US, ctx.getResources().getString(R.string.format_cost_centers), clCode, clDesc, clPost));
                }
            }
            return costcenters;
        } catch (Exception ex) {
            ExpClass.LogEX(ex, raw);
            return costcenters;
        }
    }

    /**
     *  Build out a list of cost centers as expected for DWR input.
     *  Note that there is only 1 cost center per DWR at this time.
     */
    public static List<DwrCostCenter> BuildCostCenter(String rawCostCenter, String startTime, String endTime, int specialCostCenterKey){
        List<DwrCostCenter> costCenterList = new ArrayList<>();
        if(rawCostCenter == null || rawCostCenter.length() == 0) { return costCenterList; }
        try {
            DwrCostCenter dwrCostCenter = new DwrCostCenter();
            dwrCostCenter.startTime = startTime;
            dwrCostCenter.endTime = endTime;
            dwrCostCenter.id = specialCostCenterKey;
            JobCostCenter jobCostCenter = new JobCostCenter();
            jobCostCenter.job = null;
            String[] rows = rawCostCenter.split(DELIMIT_COSTCENTER_REG);
            for (String item : rows) {
                if (item.length() > 0) {
                    String[] values = item.split(DELIMIT_COSTCENTER_ITEM);
                    String clDbid = values[0];
                    String clCode = values[1];
                    String clDesc = values[2];
                    String clPost = values[3];
                    jobCostCenter.id = Integer.parseInt(clDbid);
                    jobCostCenter.code = clCode;
                    jobCostCenter.description = clDesc;
                    jobCostCenter.milePostRange = clPost;
                }
            }
            dwrCostCenter.jobCostCenter = jobCostCenter;
            costCenterList.add(dwrCostCenter);
            return costCenterList;
        } catch (Exception ex) {
            ExpClass.LogEX(ex, rawCostCenter);
            return costCenterList;
        }
    }

    // The API is not particularly disciplined and ofter returns null.
    public static String DefaultForNull(String value){ return DefaultForNull(value, ""); }
    public static String DefaultForNull(String value, String backup){
        return value != null ? value : backup;
    }

    // Safe way to extract number from string.
    public static Float GetFloatFromString(String hours) {
        try {
            if (hours == null) {
                return 0.0F;
            }
            if (hours.length() == 0) {
                return 0.0F;
            }
            return Float.parseFloat(hours.replaceAll("[^\\d.]", ""));
        } catch (Exception ex) {
            return 0.0F;
        }
    }

    /* *****************************************************************************************
     * Below are some common methods in dealing with the file system.  There are three specific
     * items that are stored locally in files.  The Pictures taken for various reasons by the
     * RWIC, the signatures collected by the RWIC and any documents downloaded from the server.
     * The downloaded files are usually PDF files associated with a job, although when
     * synchronizing data, the downloaded data are images (pictures & signatures).
     */

    /**
     * The official way to find the full path file names for images, pdfs, etc.  There is a
     * version of this that does not require a context, as not all directories need it.
     * @param document  What type of document, job, picture, signature, etc.
     * @param name      The actual file name.
     * @return          Full path that can be used to access the file.
     */
    public static String GetStorageName(int document, String name) throws ExpClass { return GetStorageName(document, name, null); }
    public static String GetStorageName(int document, String name, Context ctx) throws ExpClass {
        if (name != null) {
            return GetStorageDir(document, ctx) + FILE_SEPARATOR + name;
        } else {
            throw new ExpClass(ExpClass.FILE_ISSUES, FILE_SYS_ISSUE, "Attempted to get the fully qualified file name for directory of doctype: " + document + " with a null file name.", "");
        }
    }

    /**
     * It is highly suggested to try and use this method to get the directory to use
     * when dealing with files in the app.  Having this be the source of truth for
     * directory names simplifies future maintenance as it is easy to tell where the
     * files are all stored.
     */
    private static String GetStorageDir(int document) throws ExpClass { return GetStorageDir(document, null); }
    private static String GetStorageDir(int document, Context ctx) throws ExpClass {
        switch (document){
            case DOC_IMAGE_DWR:
            case DOC_IMAGE_FLASH:
            case DOC_IMAGE_SIGNIN:
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            case DOC_SIGNATURES:
                return Environment.getExternalStorageDirectory() + SIGNATURE_FILE_PATH;
            case DOC_OWNER_JOB:
                // The job documents are stored in the app private directory (found below).
                // To save files to this location you only use the file name, not this directory.
                if(ctx != null){
                    return ctx.getFilesDir().getAbsoluteFile().toString();
                } else {
                    throw new ExpClass(ExpClass.FILE_ISSUES, FILE_SYS_ISSUE, "Attempted to get the storage directory of doctype: " + document + " without context.", "");
                }
            default:
                throw new ExpClass(ExpClass.FILE_ISSUES, FILE_SYS_ISSUE, "Attempted to get the storage directory of doctype: " + document + " that is not unknown.", "");
        }
    }

    // Generates a unique file name with the suggested prefix and the image extension.
    public static File GetTempImageFile(int document, String prefix) throws IOException, SecurityException, ExpClass {
        File storageDir = new File(GetStorageDir(document));
        storageDir.mkdirs();
        return File.createTempFile(
                prefix,
                IMGAGE_EXTENTION,
                storageDir
        );
    }

    // Simple file delete.
    public static boolean DeleteFile(int document, String name, Context ctx){
        try {
            File file = new File(GetStorageName(document, name, ctx));
            return file.delete();
        } catch (ExpClass exp) {
            ExpClass.LogEXP(exp, name);
            return false;
        } catch (Exception ex) {
            ExpClass.LogEX(ex, name);
            return false;
        }
    }

    /**
     * Method can be used to save a specific type of bitmap document to file storage with a
     * specified compression.  If a duplicate file name is supplied, this new file replaces
     * the previous file.
     * @param document  What type of document, job, picture, signature, etc.
     * @param name      The actual file name.
     * @param picture   The image in question.
     * @param compression The backend is not designed to store images, so we shrink them a little.
     */
    public static void SaveImageToFile(int document, String name, Bitmap picture, int compression) throws ExpClass, IOException {

        if(picture == null) {
            throw new ExpClass(ExpClass.FILE_ISSUES, FILE_SYS_ISSUE, "Bitmap file provided is null.", name);
        }
            // Create the directory if it does not exist.
        File path = new File(GetStorageDir(document));
        path.mkdirs();  // if the path is not there, create it

        // Delete the previous file if there is one.
        File file = new File(path, name);
        if (file.exists()) { file.delete(); }

        // Save the bitmap to file.
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            picture.compress(Bitmap.CompressFormat.JPEG, compression, out);
            out.flush();
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "SaveImageToFile - " + file.getPath() + " - " + name);
        } finally {
            if(out != null) { out.close(); }
        }
    }

    /**
     * Method can be used to save a general type of file for which there are bits.  For
     * example, if a downloaded base64 encoded file is decoded into bits, it can be save
     * directly with this method.
     * @param document  What type of document, job, picture, signature, etc.
     * @param name      The actual file name.
     * @param bits      The contents of the file.
     */
    public static void SaveRawToFile(int document, String name, byte[] bits) throws ExpClass, IOException {

        // Create the directory if it does not exist.
        File path = new File(GetStorageDir(document));
        path.mkdirs();  // if the path is not there, create it

        // Delete the previous file if there is one.
        File file = new File(path, name);
        if (file.exists()) { file.delete(); }

        // Save the bitmap to file.
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(bits);
            out.flush();
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "SaveRawToFile - " + file.getPath() + " - " + name);
        } finally {
            if(out != null) { out.close(); }
        }
    }

    // Rip the file name from the uri.
    public static String UriFileName(Context context, Uri uriPath) {
        try {
            String CONTENT = "content";

            if (uriPath == null) { return ""; }

            String result = null;
            String uriScheme = uriPath.getScheme();
            String path = uriPath.getPath();

            // Try to get a file name.
            if (uriScheme != null && uriScheme.equalsIgnoreCase(CONTENT)) {
                try (Cursor cursor = context.getContentResolver().query(uriPath, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }
            }
            // Try harder to get a file name.
            if (result == null && path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    result = path.substring(cut + 1);
                }
            }
            return result != null ? result : "";
        } catch (Exception ex) {
            ExpClass.LogEX(ex, "getFileName - " + uriPath.toString());
            return "";
        }
    }

    /**
     * Check if the images needs to be rotated for proper viewing.
     * @param document  What type of document, job, picture, signature, etc.
     * @param name      The actual file name.
     * @return          How much should this be rotated in degrees.
     */
    public static int CalcRotationDegrees(int document, String name){
        String path = "";
        try {
            path = GetStorageName(document, name);
            ExifInterface exif = new ExifInterface(path);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            return ExifToDegrees(rotation);
        } catch (Exception ex) {
            String extra = path.length() > 0 ? path : "No Document Path";
            ExpClass.LogEX(ex, "calcRotationDegrees - " + extra);
            return 0;
        }
    }

    // Get the actual amount of rotation from the key.
    private static int ExifToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    // Since files to encode are images, easiest to just use the bitmap class.
    public static String EncodeImagePictureBase64(int document, String name, int compress) {
        String path = "";
        try {
            path = GetStorageName(document, name);
            int rotationDegrees = Functions.CalcRotationDegrees(document, name);

            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if(rotationDegrees > 0) bitmap = RotateBitmap(bitmap, rotationDegrees);
            bitmap.compress(Bitmap.CompressFormat.JPEG, compress, bStream);
            byte[] imageBytes = bStream.toByteArray();

            return Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } catch (Exception ex) {
            String extra = path.length() > 0 ? path : "No Document Path";
            ExpClass.LogEX(ex, "EncodeImagePictureBase64 - " + extra);
            return "";
        }
    }

    // This will spin the image "angle" number of degrees.
    private static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.preRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Map DWR status codes to icons
    public static int GetDwrIcon(int status) {
        int pic;
        switch (status) {
            case DWR_STATUS_NEW:
                pic = R.drawable.outline_create_24px;
                break;
            case DWR_STATUS_DRAFT:
                pic = R.drawable.draft;
                break;
            case DWR_STATUS_QUEUED:
                pic = R.drawable.disconnected;
                break;
            case DWR_STATUS_PENDING:
                pic = R.drawable.pending;
                break;
            case DWR_STATUS_BOUNCED:
                pic = R.drawable.declined;
                break;
            case DWR_STATUS_APPROVED:
                pic = R.drawable.complete;
                break;
            case DWR_STATUS_PROCESSED:  // There is no icon once processed
            default:
                pic = -1;
                break;
        }
        return pic;
    }


}

