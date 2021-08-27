package com.railprosfs.railsapp.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.LogUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 *  The WebServices Class holds methods that can be used to communicate with the JSON
    based REST Web Service APIs.
 */
public class WebServices implements IWebServices {

    private int LocalTimeout;   // Set this in the constructor if expecting lots of data.
    private Gson LocalParse;

    public WebServices(Gson localParse){
        LocalParse = localParse;
        LocalTimeout = API_TIMEOUT;
    }

    public WebServices(int localTimeout, Gson localParse){
        LocalParse = localParse;
        LocalTimeout = localTimeout;
    }

    /**
     *  This method can be used to check for connectivity prior to making API calls.
     * @return  True => Ok to use network.
     */
    @Override
    public boolean IsNetwork(Context context) {
        try {
            NetworkInfo priNet = null;
            ConnectivityManager net = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(net != null){ priNet = net.getActiveNetworkInfo(); }
            return (priNet!=null && priNet.isConnected());
        } catch (Exception e) {
            return false;
        }
    }

    /*
        Retrieve a resource.  Since this is a GET, there is no request data other than the
        supplied path parameter.  Expectation is that dynamic information and parameters
        have already been included by the time this is called.  For authentication, the
        token is provided separately and included in the Authorization header.  Since this
        is a GET, is requires that something be returned (use POST for side effect APIs).

        In the case that something goes wrong, when the Web Service call returns other than
        a success(2xx) code, the error response stream is parsed and an instance of the
        ExpClass is thrown. This will contain the HTTP status code and/or the actual exception.
    */
    @Override
    public <U> U CallGetApi(String path, Class<U> typeU, String token) throws ExpClass {
        String MethodName = this.getClass().getName() + ".CallGetApi-" + path;
        String bearer = API_BEARER + token;
        HttpURLConnection webC = null;

        try {
            // Set up the connection
            URL web = new URL(path);
            webC = (HttpURLConnection) web.openConnection();
            webC.setRequestMethod("GET"); // Available: POST, PUT, DELETE, OPTIONS, HEAD and TRACE
            webC.setRequestProperty("Accept", API_HEADER_ACCEPT);
            webC.setRequestProperty("Authorization", bearer);
            webC.setUseCaches(false);
            webC.setAllowUserInteraction(false);
            webC.setConnectTimeout(LocalTimeout);
            webC.setReadTimeout(LocalTimeout);
            webC.connect();
            // Process the response
            int status = webC.getResponseCode();
            LogUtil.debug(LogUtil.LOG_API_REQUEST + " method = " + path + " status = " +status );
            if (status >= 200 && status < 300) {
                BufferedReader br = new BufferedReader(new InputStreamReader(webC.getInputStream(), API_ENCODING));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                try {
                    return LocalParse.fromJson(sb.toString(), typeU);
                } catch (Exception ex) {
                    throw new ExpClass(ExpClass.PARSE_EXP, MethodName, ex.toString(), ex);
                }
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(webC.getErrorStream(), API_ENCODING));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                ErrorResponse er = CheckErrResponse(sb.toString());
                throw new ExpClass(status, er.Message, MethodName);
            }
        }
        catch (IOException ex){
            throw new ExpClass(ExpClass.NETWORK_EXP, MethodName, ex.toString(), ex);
        }
        finally {
            if (webC != null) {
                try {
                    webC.disconnect();
                }
                catch (Exception ex) {
                    throw new ExpClass(ExpClass.GENERAL_EXP, MethodName, ex.toString(), ex);
                }
            }
        }
    }

    /*
        The CallPostApi is a general method used to process Authenticated Rest POST calls.
        To use it, supply the path, a request object, authorization token and if there
        is one, the TypeWS of the return object. This returns an instance of the specified
        type upon success.
        If no response is expected, the polymorphic wrapper method can be used (e.g. the
        response object not included in the signature) and the returned data is ignored.

        In the case that something goes wrong, when the Web Service call returns other than
        a success(2xx) code, the error response stream is parsed and an instance of the
        ExpClass is thrown. This will contain the HTTP status code and/or the actual exception.
     */
    @Override
    public <T> void CallPostApi(String path, final T input, String token) throws ExpClass {
        CallPostApi(path,  input,  Nothing.class, token);
    }
    @Override
    public <T, U> U CallPostApi(String path, final T input, Class<U> typeU, String token) throws ExpClass {
        String MethodName = this.getClass().getName() + ".CallPostApi-" + path;
        String bearer = API_BEARER + token;
        HttpURLConnection webC = null;
        Type typeOfRequest = new TypeToken<T>(){}.getType();

        try {
            // Set up the connection
            URL web = new URL(path);
            webC = (HttpURLConnection) web.openConnection();
            webC.setRequestMethod("POST"); // Available: POST, PUT, DELETE, OPTIONS, HEAD and TRACE
            webC.setRequestProperty("Accept", API_HEADER_ACCEPT);
            webC.setRequestProperty("Authorization", bearer);
            webC.setRequestProperty("Content-type", API_HEADER_CONTENT);
            webC.setDoOutput(true);
            webC.setUseCaches(false);
            webC.setAllowUserInteraction(false);
            webC.setConnectTimeout(LocalTimeout);
            webC.setReadTimeout(LocalTimeout);
            webC.connect();
            // Create the payload
            final OutputStream body = new BufferedOutputStream(webC.getOutputStream());
            final JsonWriter jwrite = new JsonWriter(new OutputStreamWriter(body, API_ENCODING));
            LocalParse.toJson(input, typeOfRequest, jwrite);
            jwrite.flush();
            jwrite.close();
            // Process the response
            int status = webC.getResponseCode();
            LogUtil.debug(LogUtil.LOG_API_REQUEST + " method = " + path + " status = " +status );

            if (status >= 200 && status < 300) {
                if(typeU.equals(Nothing.class)){ return null; } // No return data is expected, so none returned.
                BufferedReader br = new BufferedReader(new InputStreamReader(webC.getInputStream(), API_ENCODING));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                try {
                    return LocalParse.fromJson(sb.toString(), typeU);
                } catch (Exception ex) {
                    throw new ExpClass(ExpClass.PARSE_EXP, MethodName, ex.getMessage(), ex);
                }
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(webC.getErrorStream(), API_ENCODING));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                ErrorResponse er = CheckErrResponse(sb.toString());
                throw new ExpClass(status, er.Message, MethodName);
            }
        }
        catch (IOException ex){
            throw new ExpClass(ExpClass.NETWORK_EXP, MethodName, ex.toString(), ex);
        }
        finally {
            if (webC != null) {
                try {
                    webC.disconnect();
                }
                catch (Exception ex) {
                    throw new ExpClass(ExpClass.GENERAL_EXP, MethodName, ex.toString(), ex);
                }
            }
        }
    }

    /*
    The CallLoginApi is a special case to handle the non-JSON authentication method.
    Also, since it is the authentication method, it does not need the security token.
    Other than that, it is pretty similar to CallPostApi.

    In the case that something goes wrong, like the Web Service call returns other than
    a success(2xx) code, the error response stream is parsed and an instance of the
    ExpClass is thrown. This will contain the HTTP status code and/or the actual exception.
   */
    public WebServiceModels.LoginResponse CallLoginApi(String path, String user, String password) throws ExpClass{
        String MethodName = this.getClass().getName() + ".CallLoginApi-" + path;
        HttpURLConnection webC = null;

        HashMap<String, String> params = new HashMap<>();
        params.put("username", user);
        params.put("password", password);
        params.put("grant_type", "password");

        try {
            // Set up the connection
            URL web = new URL(path);
            webC = (HttpURLConnection) web.openConnection();
            webC.setRequestMethod("POST"); // Available: POST, PUT, DELETE, OPTIONS, HEAD and TRACE
            webC.setRequestProperty("Accept", API_HEADER_ACCEPT);
            webC.setRequestProperty("Content-type", API_LOGIN_CONTENT);
            webC.setDoOutput(true);
            webC.setUseCaches(false);
            webC.setAllowUserInteraction(false);
            webC.setConnectTimeout(LocalTimeout);
            webC.setReadTimeout(LocalTimeout);
            webC.connect();
            // Create the payload
            OutputStream os = webC.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, API_ENCODING));
            writer.write(getPostDataString(params));
            writer.flush();
            writer.close();
            os.close();
            // Processed the response
            int status = webC.getResponseCode();
            LogUtil.debug(LogUtil.LOG_API_REQUEST + " method = " + path + " status = " +status );

            if (status >= 200 && status < 300) {
                BufferedReader br = new BufferedReader(new InputStreamReader(webC.getInputStream(), API_ENCODING));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                return new Gson().fromJson(sb.toString(), WebServiceModels.LoginResponse.class);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(webC.getErrorStream(), API_ENCODING));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                ErrorResponse er = CheckErrResponse(sb.toString());
                throw new ExpClass(status, er.Message, MethodName);
            }
        }
        catch (IOException ex){
            throw new ExpClass(ExpClass.NETWORK_EXP, MethodName, ex.toString(), ex);
        }
        finally {
            if (webC != null) {
                try {
                    webC.disconnect();
                }
                catch (Exception ex) {
                    ExpClass.LogEX(ex, MethodName);
                    throw new ExpClass(ExpClass.GENERAL_EXP, MethodName, ex.toString(), ex);
                }
            }
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    /*
     * Use this method to customize the parsing of failed API calls.
     * A good API will always return proper JSON even if there is a problem,
     * but it is always possible to just get back some HTML (and an exception
     * from the json parser).
     */
    private ErrorResponse CheckErrResponse(String message){
        ErrorResponse er = new ErrorResponse();
        try {
            er = LocalParse.fromJson(message, ErrorResponse.class);
        } catch (Exception ex) { er.Message = message; }
        if(er.Message == null) { er.Message = ""; }
        if(er.Message.length() == 0) { er.Message = ExpClass.DEFAULT_DESC; }
        return er;
    }

    /*
     * This class is specific to the error return information from the API.
     * If the API returns detailed error data, this class can be expanded
     * to deal with it (from a parsing standpoint).
     */
    private static class ErrorResponse
    {
        String Message;
    }

    // These classes are placeholders to allow simpler method signatures.
    private static class Nothing
    {
        NoData data;
    }
    private static class NoData
    {
        String filler;
    }

}
