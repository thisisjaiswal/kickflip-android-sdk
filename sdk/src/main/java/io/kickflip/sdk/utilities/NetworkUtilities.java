package io.kickflip.sdk.utilities;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.Html;

import io.kickflip.sdk.KickflipApplication;

public class NetworkUtilities {
    private static final String encoding = "UTF-8";
    private static List<IConnectivityListener> mListeners = new ArrayList<IConnectivityListener>();
    private static String NetworkType = null;
    private static boolean Online = false;
    private static boolean Wifi = false;

    private NetworkUtilities() {

    }

    public static String getNetworkType() {
        return NetworkType;
    }

    public static boolean isOnline() {
        return Online;
    }

    public static boolean isWifi() {
        return Wifi;
    }

    public static void setNetworkStatus() {
        try {
            ConnectivityManager cm = (ConnectivityManager) KickflipApplication.instance()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean newOnline = false;
            NetworkType = null;
            Wifi = false;
            if (cm.getActiveNetworkInfo() != null) {
                NetworkType = cm.getActiveNetworkInfo().getTypeName();
                newOnline = cm.getActiveNetworkInfo().isConnected();
                Wifi = (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI || cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIMAX);
            }
            if (newOnline != Online) {
                notifyListeners(newOnline);
            }
            Online = newOnline;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String post2_getPayload(Map<String, String> vars) {
        if (vars == null || vars.isEmpty())
            return "";
        StringBuilder encodedData = new StringBuilder();

        for (String key : vars.keySet()) {
            String val = encode(vars.get(key));
            encodedData.append(encode(key));
            encodedData.append('=');
            encodedData.append(val);
            encodedData.append('&');
        }
        encodedData.deleteCharAt(encodedData.length() - 1);
        return encodedData.toString();
    }

    public static String convertStreamToString(InputStream is) {
        InputStreamReader isr;
        try {
            isr = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            isr = new InputStreamReader(is);
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(isr);
        {
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return sb.toString();
        }
    }

    public static String toString(InputStream inputStream) {
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            reader = new InputStreamReader(inputStream);
        }
        return toString(reader);
    }

    public static String toString(Reader reader) throws RuntimeException {
        try {
            // Buffer if not already buffered
            reader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
            StringBuilder output = new StringBuilder();
            while (true) {
                int c = reader.read();
                if (c == -1)
                    break;
                output.append((char) c);
            }
            close(reader);
            return output.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            close(reader);
        }
    }

    public static void close(Closeable input) {
        if (input == null)
            return;
        // Flush (annoying that this is not part of Closeable)
        try {
            Method m = input.getClass().getMethod("flush");
            m.invoke(input);
        } catch (Exception e) {
            // Ignore
        }
        // Close
        try {
            input.close();
            input = null;
        } catch (IOException e) {
            // Ignore
        }
    }

    public static final void disconnect(HttpURLConnection connection) {
        if (connection == null)
            return;
        try {
            connection.disconnect();
        } catch (Throwable t) {
            // ignore
        }
    }

    public static String encode(String text) {
        try {
            return URLEncoder.encode(text, encoding);
        } catch (Exception e) {
            return text;
        }
    }

    public static String HTMLDecode(String sHTML) {
        return Html.fromHtml(sHTML).toString();
    }

    public static void registertListener(IConnectivityListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public static void unRegistertListener(IConnectivityListener listener) {
        mListeners.remove(listener);
    }

    public static void notifyListeners(boolean newOnline) {
        for (IConnectivityListener listener : mListeners) {
            listener.onConnectivityChanged(newOnline);
        }
    }

    public interface IConnectivityListener {
        void onConnectivityChanged(boolean online);
    }

}